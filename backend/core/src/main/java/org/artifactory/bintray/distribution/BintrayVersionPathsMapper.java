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

package org.artifactory.bintray.distribution;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.bintray.BintrayUploadInfo;
import org.artifactory.api.bintray.distribution.DistributionService;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.bintray.distribution.resolver.DistributionCoordinatesResolver;
import org.artifactory.api.bintray.distribution.resolver.DistributionRuleFilterType;
import org.artifactory.api.bintray.distribution.rule.DistributionRuleTokens;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.distribution.rule.DistributionCoordinates;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.artifactory.bintray.distribution.util.DistributionUtils.*;

/**
 * @author Dan Feldman
 */
class BintrayVersionPathsMapper {
    private static final Logger log = LoggerFactory.getLogger(BintrayVersionPathsMapper.class);

    private static final String DUMMY_EXISTING_COORDINATES_RULE_NAME = "Existing Coordinates on path";
    private DistributionReporter status;
    private DistributionRepoDescriptor targetRepoDescriptor;
    private final DistributionService distributionService;
    private final RepositoryService repoService;

    BintrayVersionPathsMapper(DistributionRepoDescriptor targetRepoDescriptor, DistributionService distributionService,
            RepositoryService repoService, DistributionReporter status) {
        this.targetRepoDescriptor = targetRepoDescriptor;
        this.status = status;
        this.distributionService = distributionService;
        this.repoService = repoService;
    }

    Multimap<BintrayUploadInfo, DistributionCoordinatesResolver> mapPaths(List<String> requestedPaths) {
        List<RepoPath> paths = distributionService.inputPathsToRepoPaths(requestedPaths, status);
        status.debug("Requested paths: " + paths.toString(), log);
        //Get repo/package/version -> coordinates mapping, all paths are aggregated by target bintray version
        Multimap<BintrayUploadInfo, DistributionCoordinatesResolver> distCoordinates = coordinatesForPaths(paths);
        status.debug("Paths resolved to coordinates: " + distCoordinates.toString(), log);
        return distCoordinates;
    }

    /**
     * Tries to match each path from {@param paths} to the repo's {@link DistributionRule} (passed as {@param repoRules})
     * that matches it most either by type (and path filtration if defined) or solely by path filtration.
     * If no rules of the same type match an attempt to match against the generic rules will be made, if any were defined
     * for the repo.
     * Also populates each of the coordinate's {@link BintrayUploadInfo} that will
     * be used eventually when pushing artifacts to Bintray using the {@param descriptor}
     *
     * @return The mapping of path in Artifactory to it's path ({@link DistributionCoordinates}) in Bintray.
     */
    private Multimap<BintrayUploadInfo, DistributionCoordinatesResolver> coordinatesForPaths(List<RepoPath> paths) {
        String productName = targetRepoDescriptor.getProductName();
        Map<RepoPath, Properties> pathInfo = distributionService.getPathInformation(paths, productName, status);
        status.debug("Populating coordinate tokens with artifact coordinates and layout data.", log);
        List<DistributionCoordinatesResolver> resolvers =
                getCoordinatesForPaths(pathInfo, targetRepoDescriptor.getRules(), productName, status)
                        .stream()
                        .map(coordinate -> coordinate.resolve(status))
                        .filter(Objects::nonNull)
                        .map(this::typeSpecificAdjustments)
                        .map(coordinate -> coordinate.populateUploadInfo(targetRepoDescriptor))
                        .collect(Collectors.toList());
        return mergeVersions(resolvers);
    }

    /**
     * Constructs A {@link DistributionCoordinatesResolver} for each path given in {@param pathProperties} based on
     * the path's type and if it matched any {@link DistributionRule} from the given list {@param rules}
     */
    private List<DistributionCoordinatesResolver> getCoordinatesForPaths(Map<RepoPath, Properties> pathProperties,
            List<DistributionRule> rules, @Nullable String productName, DistributionReporter status) {
        List<DistributionCoordinatesResolver> coordinates = Lists.newArrayList();
        Map<String, RepoType> repoTypes = Maps.newHashMap();
        for (Map.Entry<RepoPath, Properties> pathProps : pathProperties.entrySet()) {
            RepoPath path = pathProps.getKey();
            String repoKey = path.getRepoKey();
            populateRepoKeyToTypeMap(repoTypes, repoKey);
            Properties properties = pathProps.getValue();
            DistributionCoordinatesResolver resolver =
                    getResolverByPathAndProperties(rules, repoTypes.get(path.getRepoKey()), path, properties, status);
            if (resolver != null) {
                //Add product name token if this repo distributes a product, only for new rules, not existing coordinates.
                if (StringUtils.isNotBlank(productName) && !DUMMY_EXISTING_COORDINATES_RULE_NAME.equals(resolver.ruleName)) {
                    resolver.tokens.add(DistributionRuleTokens.getProductNameToken());
                }
                //Add layout tokens according to the layout defined for the repo
                coordinates.add(resolver);
            }
        }
        return coordinates;
    }

    private void populateRepoKeyToTypeMap(Map<String, RepoType> repoTypes, String repoKey) {
        if (repoTypes.get(repoKey) == null) {
            RepoBaseDescriptor descriptor = repoService.localOrCachedRepoDescriptorByKey(repoKey);
            if (descriptor != null) {
                repoTypes.putIfAbsent(repoKey, descriptor.getType());
            }
        }
    }

    private DistributionCoordinatesResolver getResolverByPathAndProperties(List<DistributionRule> rules, RepoType type,
            RepoPath path, Properties properties, DistributionReporter status) {
        DistributionCoordinatesResolver resolver;
        if (DistributionCoordinatesResolver.pathHasDistributionCoordinates(properties)) {
            status.debug("Found existing Bintray Properties on path " + path.toPath() + ", returning dummy rule which " +
                    "will populate coordinates with values later on.", log);
            DistributionCoordinates dummyCoordinates = new DistributionCoordinates("", "", "", "");
            DistributionRule dummyRule = new DistributionRule(DUMMY_EXISTING_COORDINATES_RULE_NAME, type, "", "", dummyCoordinates);
            resolver = new DistributionCoordinatesResolver(dummyRule, path, properties, getRepoLayout(path.getRepoKey()));
        } else {
            resolver = matchRuleToArtifact(rules, type, properties, path, status);
        }
        return resolver;
    }

    private DistributionCoordinatesResolver matchRuleToArtifact(List<DistributionRule> rules, RepoType repoType,
            Properties pathProps, RepoPath path, DistributionReporter status) {
        DistributionCoordinatesResolver resolver = null;
        RepoType artifactType = getArtifactType(pathProps, repoType, path, status);
        if (artifactType != null) {
            resolver = matchPathToRule(rules, path, artifactType, pathProps, status);
        } else {
            status.error("Can't match any rule to artifact '" + path.toPath() + "'.", SC_BAD_REQUEST, log);
        }
        return resolver;
    }

    /**
     * Matches the first rule from the ordered rules list {@param rules} to the give {@param path} based on it's
     * {@param artifactType} and the rule's {@link DistributionRule#pathFilter} if specified.
     */
    private DistributionCoordinatesResolver matchPathToRule(List<DistributionRule> rules, RepoPath path,
            RepoType artifactType, Properties pathProps, DistributionReporter status) {
        for (DistributionRule rule : rules) {
            //First rule that matches, either of same type or generic
            if (rule.getType().equals(artifactType) || rule.getType().equals(RepoType.Generic)) {
                DistributionCoordinatesResolver resolver = new DistributionCoordinatesResolver(rule, path, pathProps,
                        getRepoLayout(path.getRepoKey()));
                boolean repoMatches = distributionService.addCaptureGroupsToRuleResolverIfMatches(rule,
                        DistributionRuleFilterType.repo, rule.getRepoFilter(), path.getRepoKey(), resolver, status);
                if (!repoMatches) {
                    continue;
                }
                boolean pathMatches = distributionService.addCaptureGroupsToRuleResolverIfMatches(rule,
                        DistributionRuleFilterType.path, rule.getPathFilter(), path.getPath(), resolver, status);
                if (pathMatches) {
                    return resolver;
                }
            }
        }
        String fullPath = path.toPath();
        status.error(fullPath, "Failed to match any rule for artifact " + fullPath + " with type " + artifactType
                + ". It will not be distributed.", SC_BAD_REQUEST, log);
        return null;
    }

    /**
     * Intervention point for any type-specific changes that should be made to coordinates.
     *
     * Note: since there's currently only one such required intervention (see commit that introduced this for details)
     * I'm just adding this method as the groundwork for a better mechanism, on the next constraint this method needs
     * to be replaced with interceptor-like logic.
     */
    private DistributionCoordinatesResolver typeSpecificAdjustments(DistributionCoordinatesResolver coordinate) {
        if (RepoType.Docker.equals(coordinate.type)) {
            coordinate.setRepo(coordinate.getRepo().toLowerCase());
        }
        return coordinate;
    }

    /**
     * Merges the {@link BintrayUploadInfo} of resolvers that point to the same repo/package/version from
     * {@param resolvers} and aggregates all attributes into the same version so that we only create it once when
     * distributing.
     */
    private Multimap<BintrayUploadInfo, DistributionCoordinatesResolver> mergeVersions(List<DistributionCoordinatesResolver> resolvers) {
        Multimap<BintrayUploadInfo, DistributionCoordinatesResolver> mergedResolvers = HashMultimap.create();
        resolvers.stream()
                .filter(resolver -> !mergedResolvers.containsKey(resolver.uploadInfo))
                .forEach(resolver -> {
                    List<DistributionCoordinatesResolver> sameCoordinates = getResolversWithSameUploadInfo(resolvers, resolver);
                    BintrayUploadInfo mergedUploadInfo = getMergedUploadInfo(sameCoordinates);
                    if (mergedUploadInfo != null) {
                        mergedResolvers.putAll(mergedUploadInfo, sameCoordinates);
                    } else {
                        //If something went wrong fall back to upload info per coordinate
                        sameCoordinates.forEach(coordinate -> mergedResolvers.put(coordinate.uploadInfo, coordinate));
                    }
                });
        //If something went wrong fall back to upload info per coordinate
        if (mergedResolvers.isEmpty()) {
            resolvers.forEach(coordinate -> mergedResolvers.put(coordinate.uploadInfo, coordinate));
        }
        return mergedResolvers;
    }

    private RepoLayout getRepoLayout(String repoKey) {
        RepoDescriptor descriptor = repoService.repoDescriptorByKey(repoKey);
        return descriptor == null ? null : descriptor.getRepoLayout();
    }
}
