package org.artifactory.ui.rest.service.distribution;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.ForbiddenException;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.ui.rest.model.distribution.ReleaseBundleReposIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor.RELEASE_BUNDLE_DEFAULT_REPO;

/**
 * Used in specific Permission page to retrieve the list of release-bundle repositories.
 *
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetReleaseBundleRepositoriesService implements RestService<ReleaseBundleReposIModel> {
    private static final Logger log = LoggerFactory.getLogger(GetReleaseBundleRepositoriesService.class);

    @Autowired
    private AddonsManager addonsManager;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (!authorizationService.hasReleaseBundlePermission(ArtifactoryPermission.MANAGE)) {
            throw new ForbiddenException("Only admin or users with manage permissions can access this endpoint");
        }
        List<String> releaseBundleRepos = Lists.newArrayList();
        if (addonsManager.isEnterprisePlusInstalled()) {
            releaseBundleRepos = repositoryService.getReleaseBundlesRepoDescriptors().stream()
                    .map(RepoBaseDescriptor::getKey).collect(Collectors.toList());
            if (releaseBundleRepos.isEmpty()) {
                releaseBundleRepos.add(RELEASE_BUNDLE_DEFAULT_REPO);
            }
        } else if (addonsManager.isEdgeLicensed()) {
            releaseBundleRepos.add(RELEASE_BUNDLE_DEFAULT_REPO);
        }
        try {
            ReleaseBundleReposIModel releaseBundleReposIModel = new ReleaseBundleReposIModel();
            releaseBundleReposIModel.setRepositories(releaseBundleRepos);
            response.iModel(JacksonWriter.serialize(releaseBundleReposIModel));
        } catch (IOException e) {
            log.error("Failed to serialize response ", e);
            throw new IllegalArgumentException(e);
        }
    }
}
