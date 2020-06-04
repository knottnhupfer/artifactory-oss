package org.artifactory.metadata.service.provider;

import org.apache.commons.lang3.NotImplementedException;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static org.artifactory.metadata.service.provider.AbstractMetadataProvider.ARTIFACTORY_LICENSES;

/**
 * Provides type-specific metadata logic
 *
 * @author Uriah Levy
 */
public interface TypeSpecificMetadataTemplate {

    default boolean isPropertyBased() {
        return true;
    }

    @Nullable
    String getPkgidSuffix(RepoPath repoPath, Properties properties);

    default Optional<String> getNameKey() {
        return Optional.empty();
    }

    default Optional<String> getVersionKey() {
        return Optional.empty();
    }

    default Optional<String> getIssuesKey() {
        return Optional.empty();
    }

    default Optional<String> getDescriptionKey() {
        return Optional.empty();
    }

    default String getLicenseKey() {
        return ARTIFACTORY_LICENSES;
    }

    default Map<String, String> getQualifierKeysToMdsKeys() {
        return new HashMap<>();
    }

    default Optional<String> getTagKey() {
        return Optional.empty();
    }

    default Function<String, String> getQualifierValueResolver() {
        return Function.identity();
    }

    default Function<String, Set<String>> getTagValueResolver() {
        return val -> {
            HashSet<String> tags = new HashSet<>();
            tags.add(val);
            return tags;
        };
    }

    Set<String> getExcludedUserProperties();

    Set<String> getLeadFileExtensions();

    default String getName(RepoPath repoPath) {
        throw new NotImplementedException("getName not implemented in sub-class");
    }

    default String getVersion(RepoPath repoPath) {
        throw new NotImplementedException("getVersion not implemented in sub-class");
    }
}
