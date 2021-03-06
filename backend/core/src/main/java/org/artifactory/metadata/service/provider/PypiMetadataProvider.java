package org.artifactory.metadata.service.provider;

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
import java.util.Optional;
import java.util.Set;

/**
 * @author Uriah Levy
 */
@Component
public class PypiMetadataProvider extends AbstractMetadataProvider {
    private static final RepoType type = RepoType.Pypi;
    private static final Set<String> LEAD_FILE_EXTENSIONS = ImmutableSet.of("tar.gz", "tgz", "whl");

    PypiMetadataProvider() {
        super(type);
        setTypeSpecificMetadataTemplate(new PypiTypeSpecificMetadataTemplate());
    }

    @Override
    public boolean isLeadArtifact(RepoPath repoPath) {
        String extension = PathUtils.getTarFamilyExtension(repoPath.getPath());
        return StringUtils.isNotBlank(extension) && LEAD_FILE_EXTENSIONS.contains(extension);
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
    class PypiTypeSpecificMetadataTemplate implements TypeSpecificMetadataTemplate {
        private static final String NAME_SUFFIX = ".name";
        private static final String VERSION_SUFFIX = ".version";
        private static final String DESCRIPTION_SUFFIX = ".summary";
        private Set<String> excludedUserProperties = ImmutableSet
                .of(type.getType() + NAME_SUFFIX, type.getType() + VERSION_SUFFIX, type.getType() + DESCRIPTION_SUFFIX);

        @Nullable
        @Override
        public String getPkgidSuffix(RepoPath repoPath, Properties properties) {
            return getNameKey().map(nameKey -> valueForKey(properties, nameKey)).orElse(null);
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
            return Optional.of(type.getType() + DESCRIPTION_SUFFIX);
        }

        @Override
        public Set<String> getExcludedUserProperties() {
            return excludedUserProperties;
        }

        @Override
        public Set<String> getLeadFileExtensions() {
            return LEAD_FILE_EXTENSIONS;
        }
    }
}
