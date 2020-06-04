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

package org.artifactory.ui.rest.service.distribution;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseArtifactInternalModel;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.component.ComponentDetails;
import org.artifactory.api.component.ComponentDetailsFetcher;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseArtifact;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseBundleModel;
import org.artifactory.bundle.BundleType;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.exception.BadRequestException;
import org.artifactory.ui.rest.model.distribution.ReleaseArtifactIModel;
import org.artifactory.ui.rest.model.distribution.ReleaseBundleIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author Tomer Mayost
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetReleaseBundleService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetReleaseBundleService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private ComponentDetailsFetcher componentDetailsFetcher;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        String name = request.getPathParamByKey("name");
        String version = request.getPathParamByKey("version");
        String type = request.getPathParamByKey("type");
        BundleType bundleType = BundleType.SOURCE.name().equalsIgnoreCase(type) ? BundleType.SOURCE : BundleType.TARGET;
        ReleaseBundleModel bundle = releaseBundleAddon.getBundleModel(name, version, bundleType);
        if (!"complete".equalsIgnoreCase(bundle.getStatus())) {
            throw new BadRequestException(name + ":" + version + " bundle is incomplete");
        }
        List<ReleaseArtifactInternalModel> releaseArtifacts = releaseBundleAddon
                .getReleaseArtifactsUsingAql(name, version, bundleType);
        ReleaseBundleIModel imodel = new ReleaseBundleIModel();
        String storingRepo =
                bundleType.equals(BundleType.SOURCE) ? releaseBundleAddon.getStoringRepo(name, version, bundleType) :
                        null;
        populateModel(bundle, imodel, bundleType, storingRepo, releaseArtifacts);
        try {
            response.iModel(JacksonWriter.serialize(imodel));
        } catch (IOException e) {
            log.error("Failed to serialize response ", e);
            throw new IllegalArgumentException(e);
        }
    }

    private void populateModel(ReleaseBundleModel bundle, ReleaseBundleIModel imodel, BundleType bundleType,
            String storingRepo,
            List<ReleaseArtifactInternalModel> releaseArtifacts) {
        imodel.setCreated(bundle.getCreated());
        imodel.setDesc(bundle.getDescription());
        imodel.setName(bundle.getName());
        imodel.setVersion(bundle.getVersion());
        imodel.setStatus(bundle.getStatus());
        List<ReleaseArtifact> artifacts = bundle.getArtifacts();
        imodel.setNumberOfArtifacts(artifacts.size());
        releaseArtifacts.subList(0, Math.min(artifacts.size(), ConstantValues.maxReleaseBundleSizeToDisplay.getInt())).forEach(artifact -> {
            ReleaseArtifactIModel artifactIModel = new ReleaseArtifactIModel();
            RepoPath repoPath;
            if (bundleType.equals(BundleType.SOURCE)) {
                String path = String.join("/", bundle.getName(), bundle.getVersion(), artifact.getPath(), artifact.getName());
                repoPath = RepoPathFactory.create(storingRepo, path);
            } else {
                repoPath = RepoPathFactory.create(String.join("/", artifact.getPath(), artifact.getName()));
            }
            artifactIModel.setSize(artifact.getSize());
            artifactIModel.setCreated(artifact.getCreatedBy());
            artifactIModel.setName(artifact.getName());
            artifactIModel.setPath(String.join("/", artifact.getPath(), artifact.getName()));

            try {
                ComponentDetails componentDetails = componentDetailsFetcher.calcComponentDetails(repoPath);
                artifactIModel.setComponentName(componentDetails.getName());
                artifactIModel.setComponentVersion(componentDetails.getVersion());
            } catch (Exception e) {
                log.info("Could not get component details for artifact '{}'. {}", artifactIModel.getPath(), e.getMessage());
            }
            imodel.getArtifacts().add(artifactIModel);
        });

        long totalSize = releaseArtifacts.stream().mapToLong(ReleaseArtifactInternalModel::getSize).sum();
        imodel.setSize(totalSize);
    }
}
