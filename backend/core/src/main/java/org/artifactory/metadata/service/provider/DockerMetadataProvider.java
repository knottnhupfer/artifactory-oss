package org.artifactory.metadata.service.provider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Uriah Levy
 */
@Component
public class DockerMetadataProvider extends AbstractMetadataProvider {

    private static final RepoType type = RepoType.Docker;
    private static final String LEAD_FILE_EXTENSION = "json";

    public DockerMetadataProvider() {
        super(type);
        setTypeSpecificMetadataTemplate(new DockerTypeSpecificMetadataTemplate());
    }

    @Override
    public boolean isLeadArtifact(RepoPath repoPath) {
        return repoPath.getName().equals("manifest.json");
    }

    @Override
    public Optional<AqlApiItem> buildVersionFilesQuery(@Nonnull RepoPath repoPath) {
        if (repoPath.getParent() == null) {
            // Root is not supported.
            return Optional.empty();
        }
        return Optional.ofNullable(AqlUtils.getAllDirectChildrenOfParentQuery(repoPath));
    }

    @Override
    protected String getType() {
        return type.getType();
    }

    @Data
    class DockerTypeSpecificMetadataTemplate implements TypeSpecificMetadataTemplate {
        private static final String NAME_SUFFIX = ".repoName";
        private static final String VERSION_SUFFIX = ".manifest";
        private Map<String, String> qualifierKeys = ImmutableMap
                .of("docker.manifest.type", "manifest_version", "docker.manifest.digest", "manifest_digest");
        private Set<String> excludedUserProperties = ImmutableSet
                .of(type.getType() + NAME_SUFFIX, type.getType() + VERSION_SUFFIX);

        DockerTypeSpecificMetadataTemplate() {
        }

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
        public Map<String, String> getQualifierKeysToMdsKeys() {
            return qualifierKeys;
        }

        @Override
        public Set<String> getLeadFileExtensions() {
            return ImmutableSet.of(LEAD_FILE_EXTENSION);
        }
    }
}
