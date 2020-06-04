/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.api.bintray.distribution.resolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jfrog.bintray.client.api.details.Attribute;
import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.details.RepositoryDetails;
import com.jfrog.bintray.client.api.details.VersionDetails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.bintray.BintrayUploadInfo;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.bintray.distribution.rule.DistributionRulePropertyToken;
import org.artifactory.api.bintray.distribution.rule.DistributionRuleToken;
import org.artifactory.api.bintray.distribution.rule.DistributionRuleTokens;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.distribution.rule.DistributionCoordinates;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jfrog.common.config.diff.DiffIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.api.bintray.BintrayService.*;
import static org.artifactory.api.bintray.distribution.resolver.DistributionRuleFilterType.GENERAL_CAP_GROUP_PATTERN;
import static org.artifactory.util.distribution.DistributionConstants.*;

/**
 * Distribution Coordinates implementation that holds it's own tokens (with values where applicable), capturing groups
 * if any were defined in the filters and can replace all tags in it's coordinate fields with actual values
 *
 * @author Dan Feldman
 */
public class DistributionCoordinatesResolver extends DistributionCoordinates {
    private static final Logger log = LoggerFactory.getLogger(DistributionCoordinatesResolver.class);

    //TODO [YA] Reuse NamedPattern & NamedMatcher after they are refactored to match capabilities of Java 7
    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
    private static final String REPO_FIELD = "repo";
    private static final String PACKAGE_FIELD = "package";
    private static final String VERSION_FIELD = "version";
    private static final String PATH_FIELD = "path";

    @JsonIgnore
    @DiffIgnore
    public RepoPath artifactPath; //Path (in Artifactory) of artifact that will be distributed.

    @JsonIgnore
    @DiffIgnore
    public transient Set<DistributionRuleToken> tokens; //All related tokens and their values

    @JsonIgnore
    @DiffIgnore
    public RepoType type; //Type of the distributed artifact.

    @JsonIgnore
    @DiffIgnore
    public BintrayUploadInfo uploadInfo; //Info for bintray repo/pkg/version creation

    @JsonIgnore
    @DiffIgnore
    public String ruleName; //The rule these resolver maps

    @JsonIgnore
    @DiffIgnore
    private Properties pathProperties;

    @JsonIgnore
    @DiffIgnore
    private transient Map<DistributionRuleFilterType, CaptureGroupValues> capturingGroups = initCaptureGroups(); //Values of the capturing groups extracted from the rule.

    private Map<DistributionRuleFilterType, CaptureGroupValues> initCaptureGroups() {
        Map<DistributionRuleFilterType, CaptureGroupValues> map = Maps.newHashMap();
        Stream.of(DistributionRuleFilterType.values()).forEach(filterType -> map.put(filterType, new CaptureGroupValues()));
        return Collections.unmodifiableMap(map);
    }

    public DistributionCoordinatesResolver(DistributionRule rule, RepoPath path, @Nonnull Properties pathProperties,
            @Nullable RepoLayout layout) {
        super(rule.getDistributionCoordinates());
        this.artifactPath = path;
        this.type = rule.getType();
        this.tokens = DistributionRuleTokens.tokensByType(type, layout);
        this.ruleName = rule.getName();
        this.pathProperties = pathProperties;
    }

    @JsonIgnore
    public BintrayUploadInfo getBintrayUploadInfo() {
        return uploadInfo;
    }

    /**
     * Resolves the target Bintray coordinates by populating all tokens available to this resolver with properties set
     * on the path it's supposed to resolve, or replace the fields with pre-existing Bintray coordinates that were
     * already set on the path as properties as well.
     *
     * If no pre-existing coordinates exist, and tokens are populated with values, this method also replaces all tokens
     * in each coordinate field of this resolver.
     */
    public DistributionCoordinatesResolver resolve(DistributionReporter status) {
        if (pathProperties != null) {
            if (pathHasDistributionCoordinates(pathProperties)) {
                String fullPath = artifactPath.toPath();
                status.status(fullPath, "Path " + fullPath + " has pre-existing Bintray distribution coordinates."
                        + " it will be distributed according to them.", log);
                replaceFieldsWithExistingCoordinates(status);
            } else {
                populateTokenValues(status);
                replaceTokensWithValues(status);
            }
        }
        return validateNoTokensLeftInFields(status);
    }

    /**
     * Assigns a value for each token based on what's in {@param pathProperties}
     */
    private void populateTokenValues(DistributionReporter status) {
        for (DistributionRuleToken token : tokens) {
            try {
                token.populateValue(artifactPath, pathProperties);
            } catch (Exception e) {
                log.debug("", e);
                status.debug(e.getMessage() + " - in rule " + ruleName, log);
            }
        }
    }

    /**
     * replaces all field of this resolver with the pre-existing coordinates (bintray.repo/package/version/path) in
     * {@param pathProperties} to support the re-distribute action
     * use only if all properties are set on the path!
     */
    private void replaceFieldsWithExistingCoordinates(DistributionReporter status) {
        this.repo = pathProperties.getFirst(BINTRAY_REPO);
        this.pkg = pathProperties.getFirst(BINTRAY_PACKAGE);
        this.version = pathProperties.getFirst(BINTRAY_VERSION);
        this.path = pathProperties.getFirst(BINTRAY_PATH);
        String fullPath = artifactPath.toPath();
        try {
            this.type = RepoType.fromType(pathProperties.getFirst(ARTIFACT_TYPE_OVERRIDE_PROP));
        } catch (Exception e) {
            log.debug("", e);
            status.warn(fullPath, "Failed to retrieve artifact type from expected property "
                    + ARTIFACT_TYPE_OVERRIDE_PROP + ". artifact " + fullPath + " will be distributed with the existing " +
                    "coordinates but as a generic type", 404, log);
            this.type = RepoType.Generic;
        }
        try {
            //Push to Bintray sets the repo property as 'subject/repo' distribution does not use the subject.
            if (repo.contains("/")) {
                status.debug("Found old Bintray repo coordinate property for Bintray repo: " + repo + ", removing " +
                        "subject from field for resolution for artifact " + fullPath, log);
                repo = repo.split("/")[1];
            }
        } catch (Exception e) {
            log.debug("", e);
            status.warn(fullPath, "Failed to get subject from old bintray repo coordinate property: " + repo +
                    ". Artifactory will attempt to use the repo coordinate as-is.", 400, e, log);
        }
        status.debug("Overriding coordinates by rule for path " + artifactPath.toPath() + ", based on existing " +
                "distribution coordinates set on it: repo -> " + repo + ", package -> " + pkg + ", version -> "
                + version + ", path -> " + path, log);
    }

    /**
     * Replaces all tokens in each of the coordinate fields (repo, package, version, path) with actual values,
     * according to the tokens available to this rule (given as {@param tokens}) and the capturing groups
     * (given as {@param capturingGroups}) extracted from the rule's path filter, if any.
     */
    private void replaceTokensWithValues(DistributionReporter status) {
        try {
            for (DistributionRuleToken token : tokens) {
                repo = replaceTokenInField(token, repo, REPO_FIELD, status);
                pkg = replaceTokenInField(token, pkg, PACKAGE_FIELD, status);
                version = replaceTokenInField(token, version, VERSION_FIELD, status);
                path = replaceTokenInField(token, path, PATH_FIELD, status);
            }
            for (DistributionRuleFilterType filterType : DistributionRuleFilterType.values()) {
                if (!capturingGroups.get(filterType).isEmpty()) {
                    repo = replaceCaptureGroupsInField(filterType, repo, REPO_FIELD, status);
                    pkg = replaceCaptureGroupsInField(filterType, pkg, PACKAGE_FIELD, status);
                    version = replaceCaptureGroupsInField(filterType, version, VERSION_FIELD, status);
                    path = replaceCaptureGroupsInField(filterType, path, PATH_FIELD, status);
                }
            }
        } catch (Exception e) {
            String fullPath = artifactPath.toPath();
            status.warn(fullPath, e.getMessage(), 400, e, log);
        }
    }

    private String replaceTokenInField(DistributionRuleToken token, String field, String fieldName,
            DistributionReporter status) throws Exception {
        if (field.contains(token.getToken())) {
            if (token.getValue() == null) {
                String err = "Failing rule " + ruleName + " - No value present for token " + token.getToken() +
                        " that was found in field '" + fieldName + "' for artifact " + artifactPath.toPath();
                if (token instanceof DistributionRulePropertyToken && !PRODUCT_NAME_TOKEN.equals(token.getToken())) {
                    err += ". Verify that this package has been indexed and property " +
                            ((DistributionRulePropertyToken) token).getPropertyKey() + " is set correctly.";
                }
                throw new Exception(err);
            }
            status.debug("Found token '" + token.getToken() + "' in " + fieldName + " field: '" + field + "' for " +
                    "artifact '" + artifactPath.toPath() + "'. replacing with value " + token.getValue(), log);

            Matcher fieldMatcher = Pattern.compile(token.getToken(), Pattern.LITERAL).matcher(field);
            return fieldMatcher.replaceAll(token.getValue());
        }
        return field;
    }

    private String replaceCaptureGroupsInField(DistributionRuleFilterType filterType, String field, String fieldName,
            DistributionReporter status) throws Exception {
        Matcher capGroupMatcher = filterType.getCaptureGroupPattern().matcher(field);
        while (capGroupMatcher.find()) {
            String group = capGroupMatcher.group(0);
            status.debug("Found group token " + group + "in " + fieldName + " field: '" + field + "' for artifact '"
                    + artifactPath.toPath() + "' and rule " + ruleName + ". trying to parse group number", log);
            String val = getCaptureGroupValue(filterType, group, field, fieldName);
            field = field.replaceAll(Pattern.quote(group), val);
        }
        capGroupMatcher = capGroupMatcher.reset(field);
        //Field still has unmatched capture group tokens - fail the rule
        if (capGroupMatcher.find()) {
            throw new Exception("Couldn't match all capture group tokens in field " + fieldName + ": " + field
                    + ". Failing distribution rule '" + ruleName + "' for artifact " + artifactPath.toPath() + ".");
        }
        return field;
    }

    private String getCaptureGroupValue(DistributionRuleFilterType filterType, String group, String field, String fieldName) throws Exception {
        try {
            if (filterType.isNamedGroup(group)) {
                String groupName = filterType.getGroupName(group);
                return capturingGroups.get(filterType).getByName(groupName);
            } else {
                int groupNum = filterType.getGroupNumber(group);
                return capturingGroups.get(filterType).getByNumber(groupNum);
            }
        } catch (NoSuchElementException e) {
            throw new Exception("No value found for capturing group " + group + " in field " + fieldName +
                    " : " + field + ". Failing distribution rule '" + ruleName + "' for artifact '"
                    + artifactPath.toPath() + "'.");
        }
    }

    /**
     * Adds all capture group values of the given filter type.
     * The given filter matcher is assumed to be given after calling its <code>matches()</code> method returned
     * <code>true</code>
     */
    public void addCaptureGroups(DistributionRuleFilterType filterType, Matcher filterMatcher, DistributionReporter status) {
        int groupCount = filterMatcher.groupCount();
        //Group 0 (the entire pattern) is not included in the count
        if (groupCount > 0) {
            CaptureGroupValues captureGroupValues = this.capturingGroups.get(filterType);
            for (int i = 1; i <= groupCount; i++) {
                captureGroupValues.addGroupValue(filterMatcher.group(i));
            }
            Set<String> possibleGroupNames = extractCaptureGroupNames(filterMatcher.pattern());
            possibleGroupNames.forEach(groupName -> {
                try {
                    captureGroupValues.addGroupValue(groupName, filterMatcher.group(groupName));
                } catch (IllegalArgumentException e) {
                    log.debug("", e);
                    status.debug("No value found for group named '" + groupName + "': " + e.toString(), log);
                }
            });
        }
    }

    private Set<String> extractCaptureGroupNames(Pattern pattern) {
        Set<String> groupNames = Sets.newHashSet();
        Matcher namedGroupMatcher = NAMED_GROUP_PATTERN.matcher(pattern.pattern());
        while (namedGroupMatcher.find()) {
            groupNames.add(namedGroupMatcher.group(1));
        }
        return groupNames;
    }

    /**
     * Validates no tokens are left in the coordinate fields - will fail the rule if any tokens are left.
     */
    private DistributionCoordinatesResolver validateNoTokensLeftInFields(DistributionReporter status) {
        boolean hasTokens = false;
        String fullPath = artifactPath.toPath();
        String err = "Coordinate Field %s in rule '" + ruleName + "' contains tokens that were not matched: %s" +
                " for artifact " + fullPath + ", failing this rule.";
        if (containsToken(repo)) {
            hasTokens = true;
            status.error(fullPath, String.format(err, REPO_FIELD, repo), 400, log);
        }
        if (containsToken(pkg)) {
            hasTokens = true;
            status.error(fullPath, String.format(err, PACKAGE_FIELD, pkg), 400, log);
        }
        if (containsToken(version)) {
            hasTokens = true;
            status.error(fullPath, String.format(err, VERSION_FIELD, version), 400, log);
        }
        if (containsToken(path)) {
            hasTokens = true;
            status.error(fullPath, String.format(err, PATH_FIELD, path), 400, log);
        }
        if (hasTokens) {
            return null;
        }
        return this;
    }

    /**
     * @return true if the given string (representing a field in the coordinates resolver) contains a token
     */
    private static boolean containsToken(String field) {
        return TOKEN_PATTERN.matcher(field).find() || GENERAL_CAP_GROUP_PATTERN.matcher(field).find();
    }

    @JsonIgnore
    public DistributionCoordinatesResolver populateUploadInfo(DistributionRepoDescriptor descriptor) {
        BintrayUploadInfo info = new BintrayUploadInfo();

        //repo
        RepositoryDetails btRepo = new RepositoryDetails();
        btRepo.setName(repo);
        setBintrayRepoType(btRepo);
        btRepo.setOwner(descriptor.getBintrayApplication().getOrg());
        btRepo.setIsPrivate(descriptor.isDefaultNewRepoPrivate());
        btRepo.setPremium(descriptor.isDefaultNewRepoPremium());
        btRepo.setUpdateExisting(false);
        info.setRepositoryDetails(btRepo);

        //pkg
        PackageDetails btPkg = new PackageDetails(pkg);
        if (CollectionUtils.isNotEmpty(descriptor.getDefaultLicenses())) {
            btPkg.licenses(Lists.newArrayList(descriptor.getDefaultLicenses()));
        }
        if (StringUtils.isNotBlank(descriptor.getDefaultVcsUrl())) {
            btPkg.vcsUrl(descriptor.getDefaultVcsUrl());
        }
        info.setPackageDetails(btPkg);

        //version
        VersionDetails versionDetails = new VersionDetails(version);
        versionDetails.setAttributes(getWhitelistProperties(descriptor));
        info.setVersionDetails(versionDetails);

        this.uploadInfo = info;
        return this;
    }

    private List<Attribute> getWhitelistProperties(DistributionRepoDescriptor descriptor) {
        //noinspection ConstantConditions (pathProperties::containsKey takes care of pathProperties.get(propKey) not being null)
        return descriptor.getWhiteListedProperties().stream()
                .filter(pathProperties::containsKey)
                .map(propKey -> new Attribute<>(propKey, Attribute.Type.string, getAttributes(propKey)))
                .collect(Collectors.toList());
    }

    public static boolean pathHasDistributionCoordinates(Properties pathProperties) {
        return pathProperties.containsKey(BINTRAY_REPO) && pathProperties.containsKey(BINTRAY_PACKAGE)
                && pathProperties.containsKey(BINTRAY_VERSION) && pathProperties.containsKey(BINTRAY_PATH);
    }

    private void setBintrayRepoType(RepositoryDetails btRepo) {
        switch (type) {
            case YUM:
                btRepo.setType("rpm");
                break;
            case Maven:
            case Ivy:
            case SBT:
            case Gradle:
                btRepo.setType("maven");
                break;
            case NuGet:
                btRepo.setType("nuget");
                break;
            case Vagrant:
                btRepo.setType("vagrant");
                break;
            case Conan:
                btRepo.setType("conan");
                break;
            case Debian:
                btRepo.setType("debian");
                break;
            case Opkg:
                btRepo.setType("opkg");
                break;
            case Docker:
                btRepo.setType("docker");
                break;
            default:
                btRepo.setType("generic");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DistributionCoordinatesResolver)) return false;
        if (!super.equals(o)) return false;
        DistributionCoordinatesResolver resolver = (DistributionCoordinatesResolver) o;
        return artifactPath != null ? artifactPath.equals(resolver.artifactPath) : resolver.artifactPath == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (artifactPath != null ? artifactPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return artifactPath.toPath() + " -> " + repo + "/" + pkg + "/" + version + "/" + path;
    }


    private List<String> getAttributes(String propKey) {
        return Lists.newArrayList(Optional.ofNullable(pathProperties.get(propKey)).orElse(Sets.newHashSet()));
    }
}
