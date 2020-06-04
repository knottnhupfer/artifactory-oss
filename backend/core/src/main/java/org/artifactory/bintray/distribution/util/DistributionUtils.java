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

package org.artifactory.bintray.distribution.util;

import com.google.common.collect.Maps;
import com.jfrog.bintray.client.api.BintrayCallException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.artifactory.api.bintray.BintrayUploadInfo;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.bintray.distribution.resolver.DistributionCoordinatesResolver;
import org.artifactory.api.bintray.distribution.rule.DistributionRulePropertyToken;
import org.artifactory.api.bintray.distribution.rule.DistributionRuleToken;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.AutoTimeoutRegexCharSequence;
import org.artifactory.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.util.distribution.DistributionConstants.ARTIFACT_TYPE_OVERRIDE_PROP;
import static org.artifactory.util.distribution.DistributionConstants.PRODUCT_NAME_DUMMY_PROP;

/**
 * @author Dan Feldman
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DistributionUtils {
    private static final Logger log = LoggerFactory.getLogger(DistributionUtils.class);

    /**
     * Adds application/x-www-form-urlencoded header required by the spec
     * https://tools.ietf.org/html/rfc6749#section-4.1.3
     */
    public static Map<String, String> getFormEncodedHeader() {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        return headers;
    }

    /**
     * Creates a matcher that times out after the time specified in {@link org.artifactory.common.ConstantValues} has
     * passed to protect Artifactory from freezing a Thread over a user's regex causing catastrophic backtracking
     *
     * @param stringToMatch String that the regex will match.
     * @param regexPattern  Pattern to match against input string.
     */
    public static Matcher createTimingOutMatcher(String stringToMatch, Pattern regexPattern) {
        CharSequence charSequence = new AutoTimeoutRegexCharSequence(stringToMatch, stringToMatch,
                regexPattern.pattern(), ConstantValues.bintrayDistributionRegexTimeoutMillis.getInt());
        return regexPattern.matcher(charSequence);
    }

    public static String getValueFromToken(DistributionCoordinatesResolver coordinates, String tokenKey) {
        return coordinates.tokens.stream()
                .filter(token -> tokenKey.equals(token.getToken()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Could not resolve the value of token '" + tokenKey +
                        "' for artifact '" + coordinates.artifactPath + "' Artifactory cannot distribute without it."))
                .getValue();
    }

    /**
     * The product name token piggy-backs on the property token, it's inserted as a path property here to be evaluated
     * by the token regex mechanism later on.
     */
    public static void insertProductNameDummyProp(@Nullable String productName, Map<RepoPath, Properties> pathProperties) {
        if (StringUtils.isNotBlank(productName)) {
            pathProperties.values().forEach(pathProps -> pathProps.put(PRODUCT_NAME_DUMMY_PROP, productName));
        }
    }

    public static boolean getIsPremiumFromResponse(HttpResponse bintrayPlanResponse) throws IOException {
        return JacksonReader.streamAsTree(bintrayPlanResponse.getEntity().getContent()).get("premium").asBoolean();
    }

    public static List<String> getLicensesFromResponse(HttpResponse bintrayLicensesResponse) throws IOException {
        return JacksonReader.streamAsTree(bintrayLicensesResponse.getEntity().getContent()).findValuesAsText("name");
    }

    /**
     * @return the path's {@link RepoType} either from the cached list of repo->type, given as
     * {@param containingRepoType} or by the type override property
     * {@link org.artifactory.util.distribution.DistributionConstants#ARTIFACT_TYPE_OVERRIDE_PROP} if it was set on the path.
     */
    public static RepoType getArtifactType(Properties pathProperties, RepoType containingRepoType, RepoPath path,
            DistributionReporter status) {
        RepoType artifactType = null;
        String typeFromProp = pathProperties.getFirst(ARTIFACT_TYPE_OVERRIDE_PROP);
        if (StringUtils.isNotBlank(typeFromProp)) {
            String fullPath = path.toPath();
            try {
                artifactType = RepoType.fromType(typeFromProp);
                status.debug("Found artifact type override property on path " + fullPath + ", overridden to type: "
                        + artifactType, log);
            } catch (Exception e) {
                status.error(fullPath, "Invalid artifact type override property set on path " + fullPath + ": "
                        + typeFromProp, HttpStatus.SC_BAD_REQUEST, e, log);
            }
        } else {
            artifactType = containingRepoType;
        }
        return artifactType;
    }

    public static String getTokenValueByPropKey(DistributionCoordinatesResolver resolver, String propKey) throws ItemNotFoundRuntimeException {
        return resolver.tokens.stream()
                .filter(token -> token instanceof DistributionRulePropertyToken)
                .filter(token -> propKey.equals(((DistributionRulePropertyToken) token).getPropertyKey()))
                .map(DistributionRuleToken::getValue)
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new ItemNotFoundRuntimeException("Can't find property value for " + propKey +
                        " on path: " + resolver.artifactPath.toPath() + " which is required for resolving its " +
                        "distribution coordinates"));
    }

    public static List<DistributionCoordinatesResolver> getResolversWithSameUploadInfo(
            List<DistributionCoordinatesResolver> resolvers, DistributionCoordinatesResolver resolver) {
        return resolvers.stream()
                .filter(current -> current.getRepo().equals(resolver.getRepo()))
                .filter(current -> current.getPkg().equals(resolver.getPkg()))
                .filter(current -> current.getVersion().equals(resolver.getVersion()))
                .collect(Collectors.toList());
    }

    public static BintrayUploadInfo getMergedUploadInfo(List<DistributionCoordinatesResolver> sameCoordinates) {
        return sameCoordinates.stream()
                .map(DistributionCoordinatesResolver::getBintrayUploadInfo)
                .reduce(DistributionUtils::mergeUploadInfo)
                .orElse(null);
    }

    private static BintrayUploadInfo mergeUploadInfo(BintrayUploadInfo left, BintrayUploadInfo right) {
        //None of this is supposed to be null, there's a fallback if something goes wrong
        try {
            left.getVersionDetails().setAttributes(
                    Stream.concat(left.getVersionDetails().getAttributes().stream(),
                            right.getVersionDetails().getAttributes().stream())
                            .distinct()
                            .collect(Collectors.toList()));
        } catch (Exception e) {
            log.debug("Failed to merge upload info: {}<->{}", left != null ? left.toString() : "null",
                    right != null ? right.toString() : "null");
            log.debug("", e);
        }
        return left;
    }

    public static void handleException(@Nullable String fullPath, String errMsg, @Nonnull Exception e,
            @Nonnull DistributionReporter status, Logger log) {
        //returns ok even if the throwable itself is BCE and not just one of its causes.
        Throwable btCause = ExceptionUtils.getCauseOfType(e, BintrayCallException.class);
        if (btCause != null) {
            BintrayCallException bce = (BintrayCallException) btCause;
            errMsg += ": " + bce.toString();
            if (StringUtils.isNotBlank(fullPath)) {
                status.error(fullPath, errMsg, bce.getStatusCode(), bce, log);
            } else {
                status.error(errMsg, bce.getStatusCode(), bce, log);
            }
        } else {
            errMsg += ": " + e.getMessage();
            if (StringUtils.isNotBlank(fullPath)) {
                status.error(fullPath, errMsg, HttpStatus.SC_CONFLICT, e, log);
            } else {
                status.error(errMsg, HttpStatus.SC_CONFLICT, e, log);
            }
        }
    }
}
