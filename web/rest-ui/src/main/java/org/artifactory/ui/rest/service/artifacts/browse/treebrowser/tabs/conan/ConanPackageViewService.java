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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.conan;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.conan.ConanAddon;
import org.artifactory.addon.conan.info.ConanPackageInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.conan.ConanPackageInfoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;

/**
 * @author Yinon Avraham
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ConanPackageViewService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ConanPackageViewService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BaseArtifactInfo artifactInfo = (BaseArtifactInfo) request.getImodel();
        String path = artifactInfo.getPath();
        String repoKey = artifactInfo.getRepoKey();
        RepoPath packagePath = InternalRepoPathFactory.create(repoKey, path);
        try {
            ConanAddon conanAddon = addonsManager.addonByType(ConanAddon.class);
            ConanPackageInfo packageInfo = conanAddon.getPackageInfo(packagePath);
            ConanPackageInfoModel packageInfoModel = new ConanPackageInfoModel();
            packageInfoModel.setOs(defaultIfBlank(packageInfo.getOs(), "Any"));
            packageInfoModel.setArch(defaultIfBlank(packageInfo.getArchitecture(), "Any"));
            packageInfoModel.setBuildType(defaultIfBlank(packageInfo.getBuildType(), "Any"));
            packageInfoModel.setCompiler(defaultIfBlank(packageInfo.getCompiler(), "Any"));
            packageInfoModel.setCompilerVersion(packageInfo.getCompilerVersion());
            packageInfoModel.setCompilerRuntime(packageInfo.getCompilerRuntime());
            packageInfoModel.setSettings(packageInfo.getSettings());
            packageInfoModel.setOptions(packageInfo.getOptions());
            packageInfoModel.setRequires(packageInfo.getRequires());
            response.iModel(packageInfoModel);
        } catch (Exception e) {
            String err = "Unable to extract Conan package information for '" + packagePath;
            response.error(err);
            log.error(err);
            log.debug(err, e);
        }
    }

}
