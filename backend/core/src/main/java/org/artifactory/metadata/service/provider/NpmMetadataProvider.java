package org.artifactory.metadata.service.provider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.jfrog.client.util.PathUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
@Component
class NpmMetadataProvider extends AbstractMetadataProvider {

    private static final RepoType type = RepoType.Npm;
    private static final String LEAD_FILE_EXTENSION = "tgz";

    NpmMetadataProvider() {
        super(type);
        setTypeSpecificMetadataTemplate(new NpmTypeSpecificMetadataTemplate());
    }

    @Override
    public boolean isLeadArtifact(RepoPath repoPath) {
        String extension = PathUtils.getExtension(repoPath.getPath());
        return StringUtils.isNotBlank(extension) && extension.equals(LEAD_FILE_EXTENSION);
    }

    @Override
    public Optional<AqlApiItem> buildVersionFilesQuery(@Nonnull RepoPath repoPath) {
        return Optional.empty();
    }

    @Override
    protected String getType() {
        return type.getType();
    }

    @Data
    class NpmTypeSpecificMetadataTemplate implements TypeSpecificMetadataTemplate {
        private static final String NAME_SUFFIX = ".name";
        private static final String VERSION_SUFFIX = ".version";
        private final String descriptionKey = type.getType() + ".description";
        private Function<String, String> qualifierValueResolver;
        private Map<String, String> qualifierKeysToMdsKeys = ImmutableMap.of(type.getType() + NAME_SUFFIX, "npm_scope");
        private String tagKey = type.getType() + ".keywords";
        private static final String TAG_SEPARATOR = ",";
        private Set<String> excludedUserProperties = ImmutableSet
                .of(type.getType() + NAME_SUFFIX, type.getType() + VERSION_SUFFIX, descriptionKey,
                        type.getType() + ".keywords");

        NpmTypeSpecificMetadataTemplate() {
        }

        @Override
        public Optional<String> getNameKey() {
            return Optional.of(type.getType() + NAME_SUFFIX);
        }

        @Override
        public Optional<String> getVersionKey() {
            return Optional.of(type.getType() + VERSION_SUFFIX);
        }

        @Override
        public Optional<String> getDescriptionKey() {
            return Optional.of(descriptionKey);
        }

        @Override
        public Map<String, String> getQualifierKeysToMdsKeys() {
            return qualifierKeysToMdsKeys;
        }

        @Override
        public Optional<String> getTagKey() {
            return Optional.ofNullable(tagKey);
        }

        @Override
        public Function<String, String> getQualifierValueResolver() {
            return rawValue -> {
                if (StringUtils.isNotBlank(rawValue) && rawValue.startsWith("@")) {
                    // "@scope/pkg" -> "scope"
                    return StringUtils.substringBefore(rawValue, "/").substring(1);
                }
                return "";
            };
        }

        @Override
        public Function<String, Set<String>> getTagValueResolver() {
            return tagValue -> {
                if (StringUtils.isNotBlank(tagValue)) {
                    String normalizedTags = tagValue.replaceAll("[\\[\\]\"]", "");
                    String[] tags = normalizedTags.split(TAG_SEPARATOR);
                    if (tags.length > 0) {
                        return Arrays.stream(tags)
                                .map(String::trim)
                                .filter(tag -> !tag.isEmpty())
                                .collect(Collectors.toSet());
                    }
                }
                return new HashSet<>();
            };
        }

        @Override
        public Set<String> getLeadFileExtensions() {
            return ImmutableSet.of(LEAD_FILE_EXTENSION);
        }

        @Nullable
        @Override
        public String getPkgidSuffix(RepoPath repoPath, Properties properties) {
            return getNameKey().map(nameKey -> valueForKey(properties, nameKey)).orElse(null);
        }
    }
}
