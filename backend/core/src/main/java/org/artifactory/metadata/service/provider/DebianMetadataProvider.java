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
public class DebianMetadataProvider extends AbstractMetadataProvider {
    private static final RepoType type = RepoType.Debian;
    private static final String LEAD_FILE_EXTENSION = "deb";

    DebianMetadataProvider() {
        super(type);
        setTypeSpecificMetadataTemplate(new DebianTypeSpecificMetadataTemplate());
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
    class DebianTypeSpecificMetadataTemplate implements TypeSpecificMetadataTemplate {
        private static final String DEB = "deb";
        private static final String NAME_SUFFIX = ".name";
        private static final String VERSION_SUFFIX = ".version";
        private static final String DISTRIBUTION_SUFFIX = ".distribution";
        private static final String ARCH_SUFFIX = ".architecture";
        private Set<String> excludedUserProperties = ImmutableSet
                .of(DEB + NAME_SUFFIX, DEB + VERSION_SUFFIX);

        @Override
        public Optional<String> getNameKey() {
            return Optional.of(DEB + NAME_SUFFIX);
        }

        @Override
        public Optional<String> getVersionKey() {
            return Optional.of(DEB + VERSION_SUFFIX);
        }

        @Override
        public Set<String> getExcludedUserProperties() {
            return excludedUserProperties;
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
