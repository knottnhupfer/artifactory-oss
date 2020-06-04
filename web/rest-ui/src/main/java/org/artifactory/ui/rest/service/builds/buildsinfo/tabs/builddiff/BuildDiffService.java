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

package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.build.ArtifactBuildAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.model.diff.BuildsDiffBaseFileModel;
import org.artifactory.api.build.model.diff.BuildsDiffDependencyModel;
import org.artifactory.api.rest.build.diff.BuildsDiff;
import org.artifactory.build.BuildRun;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.ForbiddenException;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.ui.rest.model.builds.BuildDiffModel;
import org.artifactory.ui.rest.model.builds.BuildPropsModel;
import org.artifactory.ui.rest.model.builds.ModuleArtifactModel;
import org.artifactory.ui.rest.model.builds.ModuleDependencyModel;
import org.artifactory.ui.utils.DateUtils;
import org.artifactory.util.HttpUtils;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BuildDiffService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(BuildDiffService.class);

    private BuildService buildService;
    private AddonsManager addonsManager;

    @Autowired
    public BuildDiffService(BuildService buildService, AddonsManager addonsManager) {
        this.buildService = buildService;
        this.addonsManager = addonsManager;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String name = request.getPathParamByKey("name");
            String buildNumber = request.getPathParamByKey("number");
            String comparedBuildNum = request.getQueryParamByKey("otherNumber");
            String comparedDate = DateUtils.formatBuildDate(Long.parseLong(request.getQueryParamByKey("otherDate")));
            String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
            // We validate the permissions here and not later in order to avoid useless DB queries before reaching the diff
            buildService.assertReadPermissions(name, buildNumber, buildStarted);
            // Exclude dependencies checkbox
            Boolean exDep = Boolean.valueOf(request.getQueryParamByKey("exDep"));
            BuildRun buildRun = buildService.getBuildRun(name, buildNumber, buildStarted);
            BuildDiffModel buildDiffModel = new BuildDiffModel();
            // fetch build diff data
            if (!exDep) {
                BuildsDiff buildsDiff = fetchBuildsDiffData(request, name, comparedBuildNum, comparedDate, buildRun);
                List<ModuleArtifactModel> moduleArtifactModels = updateArtifact(buildsDiff);
                List<BuildPropsModel> buildPropsModels = updateProps(buildsDiff);
                List<ModuleDependencyModel> moduleDependencyModels = updateDependency(buildsDiff);
                updateDiffModel(moduleArtifactModels, buildPropsModels, moduleDependencyModels, buildDiffModel);
            } else {
                updateDependenciesWithExcludeInternal(name, comparedBuildNum, comparedDate, buildRun, buildDiffModel);
            }
            // update build diff model
            response.iModel(buildDiffModel);
        } catch (ForbiddenWebAppException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.toString());
            log.debug(e.toString(), e);
            response.error("error running build diff");
        }
    }

    /**
     * update dependencies with exclude internal
     *
     * @param name             - dependency name
     * @param comparedBuildNum - compared build num
     * @param comparedDate     - compared build date
     * @param buildRun         - build run
     * @param buildDiffModel   -- build diff model
     */
    private void updateDependenciesWithExcludeInternal(String name, String comparedBuildNum, String comparedDate,
            BuildRun buildRun, BuildDiffModel buildDiffModel) {
        BuildRun secondBuildRun = buildService.getBuildRun(name, comparedBuildNum, comparedDate);
        ArtifactBuildAddon artifactBuildAddon = addonsManager.addonByType(ArtifactBuildAddon.class);
        Build build = buildService.getBuild(buildRun);
        Build secondBuild = buildService.getBuild(secondBuildRun);
        Map<String, BuildsDiffBaseFileModel> artifactMap = new HashMap<>();
        List<BuildsDiffBaseFileModel> artifactDiff = artifactBuildAddon.compareArtifacts(build, secondBuild);
        artifactDiff.forEach(artifact -> artifactMap.put(artifact.getSha1(), artifact));
        List<BuildsDiffBaseFileModel> dependencyDiff = artifactBuildAddon.compareDependencies(build, secondBuild);
        List<ModuleDependencyModel> moduleDependencies = new ArrayList<>();
        dependencyDiff.forEach(dependency -> {
            if (artifactMap.get(dependency.getSha1()) == null) {
                moduleDependencies.add(new ModuleDependencyModel((BuildsDiffDependencyModel) dependency));
            }
        });
        buildDiffModel.setDependencies(moduleDependencies);
    }

    /**
     * update diff model
     *
     * @param moduleArtifactModels   - module artifact model
     * @param buildPropsModels       - builds props model
     * @param moduleDependencyModels - module dependency model
     * @param buildDiffModel         -  build diff model
     */
    private void updateDiffModel(List<ModuleArtifactModel> moduleArtifactModels, List<BuildPropsModel> buildPropsModels,
            List<ModuleDependencyModel> moduleDependencyModels, BuildDiffModel buildDiffModel) {
        buildDiffModel.setArtifacts(moduleArtifactModels);
        buildDiffModel.setDependencies(moduleDependencyModels);
        buildDiffModel.setProps(buildPropsModels);
    }

    /**
     * fetch build diff via comparator
     * @param artifactoryRequest - encapsulate data related to request
     * @param name - build name
     * @param comparedBuildNum - compared build num
     * @param comparedDate - compared build date
     * @param buildRun - build run
     * @return
     */
    private BuildsDiff fetchBuildsDiffData(ArtifactoryRestRequest artifactoryRequest, String name,
            String comparedBuildNum, String comparedDate, BuildRun buildRun) {
        BuildRun secondBuildRun = buildService.getBuildRun(name, comparedBuildNum, comparedDate);
        ArtifactBuildAddon artifactBuildAddon = addonsManager.addonByType(ArtifactBuildAddon.class);
        HttpServletRequest request = artifactoryRequest.getServletRequest();
        Build build = buildService.getBuild(buildRun);
        Build secondBuild = buildService.getBuild(secondBuildRun);
        return artifactBuildAddon.getBuildsDiff(build, secondBuild, HttpUtils.getServletContextUrl(request));
    }

    /**
     * update artifact diff list
     *
     * @param buildsDiff - build diff artifact list
     */
    private List<ModuleArtifactModel> updateArtifact(BuildsDiff buildsDiff) {
        List<ModuleArtifactModel> artifacts = new ArrayList<>();
        buildsDiff.artifacts.newItems.forEach(artifact -> artifacts.add(new ModuleArtifactModel(artifact)));
        buildsDiff.artifacts.removed.forEach(artifact -> artifacts.add(new ModuleArtifactModel(artifact)));
        buildsDiff.artifacts.unchanged.forEach(artifact -> artifacts.add(new ModuleArtifactModel(artifact)));
        buildsDiff.artifacts.updated.forEach(artifact -> artifacts.add(new ModuleArtifactModel(artifact)));
        return artifacts;
    }

    /**
     * update dependency diff list
     *
     * @param buildsDiff - build diff artifact list
     */
    private List<ModuleDependencyModel> updateDependency(BuildsDiff buildsDiff) {
        List<ModuleDependencyModel> dependencyList = new ArrayList<>();
        buildsDiff.dependencies.newItems.forEach(dependency -> dependencyList.add(new ModuleDependencyModel(dependency)));
        buildsDiff.dependencies.removed.forEach(dependency -> dependencyList.add(new ModuleDependencyModel(dependency)));
        buildsDiff.dependencies.unchanged.forEach(dependency -> dependencyList.add(new ModuleDependencyModel(dependency)));
        buildsDiff.dependencies.updated.forEach(dependency -> dependencyList.add(new ModuleDependencyModel(dependency)));
        return dependencyList;
    }

    /**
     * update props diff list
     *
     * @param buildsDiff - build diff artifact list
     */
    private List<BuildPropsModel> updateProps(BuildsDiff buildsDiff) {
        List<BuildPropsModel> props = new ArrayList<>();
        buildsDiff.properties.newItems.forEach(property -> props.add(new BuildPropsModel(property)));
        buildsDiff.properties.removed.forEach(property -> props.add(new BuildPropsModel(property)));
        buildsDiff.properties.unchanged.forEach(property -> props.add(new BuildPropsModel(property)));
        buildsDiff.properties.updated.forEach(property -> props.add(new BuildPropsModel(property)));
        return props;
    }
}
