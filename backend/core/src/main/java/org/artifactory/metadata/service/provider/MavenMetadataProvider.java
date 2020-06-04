package org.artifactory.metadata.service.provider;

import com.google.common.collect.ImmutableSet;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Uriah Levy
 */
@Component
public class MavenMetadataProvider extends AbstractMetadataProvider {
    private static final Logger log = LoggerFactory.getLogger(MavenMetadataProvider.class);
    private static final RepoType type = RepoType.Maven;
    private static final String LEAD_FILE_EXTENSION = "pom";
    private InternalRepositoryService repositoryService;

    @Autowired
    MavenMetadataProvider(InternalRepositoryService repositoryService) {
        super(type);
        setTypeSpecificMetadataTemplate(new MavenTypeSpecificMetadataTemplate());
        this.repositoryService = repositoryService;
    }

    @Override
    public boolean isLeadArtifact(RepoPath repoPath) {
        String extension = PathUtils.getExtension(repoPath.getPath());
        return StringUtils.isNotBlank(extension) && extension.equals(LEAD_FILE_EXTENSION);
    }

    @Override
    public Optional<AqlApiItem> buildVersionFilesQuery(@Nonnull RepoPath repoPath) {
        if (repoPath.getParent() == null) {
            // Root is not supported.
            return Optional.empty();
        }
        return getModuleInfo(repoPath, repositoryService)
                .flatMap(info -> AqlUtils.getDirectChildrenByModuleAndVersionQuery(repoPath, info));
    }

    @Override
    protected String getType() {
        return type.getType();
    }

    @Data
    class MavenTypeSpecificMetadataTemplate implements TypeSpecificMetadataTemplate {
        @Override
        public boolean isPropertyBased() {
            return false;
        }

        @Override
        public Set<String> getExcludedUserProperties() {
            return new HashSet<>();
        }

        @Override
        public Set<String> getLeadFileExtensions() {
            return ImmutableSet.of(LEAD_FILE_EXTENSION);
        }

        @Override
        public String getName(RepoPath repoPath) {
            return getModuleInfo(repoPath, repositoryService).map(ModuleInfo::getModule).orElse(null);
        }

        @Override
        public String getVersion(RepoPath repoPath) {
            return getModuleInfo(repoPath, repositoryService).map(ModuleInfo::getBaseRevision).orElse(null);
        }

        @Override
        public String getPkgidSuffix(RepoPath repoPath, Properties properties) {
            return getModuleInfo(repoPath, repositoryService).map(info -> getPkgIdSuffixFromLayout(info, repoPath))
                    .orElse(null);
        }
    }
}
