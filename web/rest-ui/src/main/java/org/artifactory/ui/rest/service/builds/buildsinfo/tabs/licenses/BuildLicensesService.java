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

package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.builds.BuildLicenseModel;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.AbstractBuildService;
import org.artifactory.util.CollectionUtils;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BuildLicensesService extends AbstractBuildService {

    private AddonsManager addonsManager;

    @Autowired
    public BuildLicensesService(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        Boolean autoFind = Boolean.valueOf(request.getQueryParamByKey("autoFind"));
        Build build = getBuild(request, response);
        if (build == null) {
            //Already logged
            return;
        }
        // fetch license
        Multimap<RepoPath, ModuleLicenseModel> repoPathLicenseModuleModel =
                addonsManager.addonByType(LicensesAddon.class).populateLicenseInfoSynchronously(build, autoFind);
        if (repoPathLicenseModuleModel != null && !repoPathLicenseModuleModel.isEmpty()) {
            Collection<ModuleLicenseModel> values = repoPathLicenseModuleModel.values();
            // fetch published modules
            Set<ModuleLicenseModel> publishedModules = getPublishedModulesFromModelList(values, build.getModules());
            // filter published modules from licenses
            publishedModules.forEach(values::remove);
            // fetch build license summary
            Set<String> scopes = getScopeMapping(values);
            BuildLicenseModel buildLicenseModel = new BuildLicenseModel(values, publishedModules, scopes);
            response.iModel(buildLicenseModel);
        }
    }

    private static Set<String> getScopeMapping(Collection<ModuleLicenseModel> models) {
        return models.stream()
                .map(ModuleLicenseModel::getScopes)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Returns all models that relate to a dependency which is also a published module
     *
     * @param models  models to filter
     * @param modules build modules to filter by
     */
    private static Set<ModuleLicenseModel> getPublishedModulesFromModelList(Collection<ModuleLicenseModel> models, final Collection<Module> modules) {
        if (CollectionUtils.isNullOrEmpty(models) || CollectionUtils.isNullOrEmpty(modules)) {
            return Sets.newHashSet();
        }
        return Sets.newHashSet(Iterables.filter(models, new PublishedModuleFilterPredicate(modules)));
    }

    private static class PublishedModuleFilterPredicate implements Predicate<ModuleLicenseModel> {
        private Set<Artifact> moduleArtifacts = Sets.newHashSet();

        private PublishedModuleFilterPredicate(Collection<Module> modules) {
            for (Module module : modules) {
                if (CollectionUtils.notNullOrEmpty(module.getArtifacts())) {
                    moduleArtifacts.addAll(module.getArtifacts());
                }
            }
        }

        @Override
        public boolean apply(@Nonnull ModuleLicenseModel input) {
            // filter published artifacts based on the checksum
            for (Artifact artifact : moduleArtifacts) {
                if (StringUtils.isNotBlank(artifact.getSha1()) && artifact.getSha1().equals(input.getSha1())) {
                    return true;
                } else if (StringUtils.isNotBlank(artifact.getMd5()) && artifact.getMd5().equals(input.getMd5())) {
                    return true;
                }
            }
            return false;
        }
    }
}
