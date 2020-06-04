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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.builds.BuildLicenseModel;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.AbstractBuildService;
import org.jfrog.build.api.Build;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OverrideSelectedLicensesService extends AbstractBuildService {

    private AddonsManager addonsManager;

    @Autowired
    public OverrideSelectedLicensesService(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BuildLicenseModel buildLicenseModel = (BuildLicenseModel) request.getImodel();
        Build build = getBuild(request, response);
        if (build == null) {
            //Already logged
            return;
        }
        Multimap<RepoPath, ModuleLicenseModel> repoPathLicenseMultimap = addonsManager.addonByType(LicensesAddon.class)
                .populateLicenseInfoSynchronously(build, false);
        // update licenses
        updateLicenses(buildLicenseModel.getLicenses(), repoPathLicenseMultimap, response);
    }

    /**
     * Updates all licenses that had corresponding columns' 'override' checkbox checked.
     * Takes into account other columns relating to the same path so that other existing licenses properties (which were
     * not marked with override) are also saved on the path.
     */
    private void updateLicenses(Collection<ModuleLicenseModel> viewableModels, Multimap<RepoPath,
            ModuleLicenseModel> repoPathLicenseMultimap, RestResponse response) {

        List<ModuleLicenseModel> moduleLicenseModels = new ArrayList<>();
        // fetch license to override
        fetchLicenseToOverride(viewableModels, repoPathLicenseMultimap, moduleLicenseModels);

        Multimap<RepoPath, LicenseInfo> licensesToWrite = HashMultimap.create();
        //Holds all licenses that will be overridden per path - to help filter what licenses to persist on that path
        Multimap<RepoPath, LicenseInfo> overriddenLicenses = HashMultimap.create();
        for (ModuleLicenseModel checkedLicenseModel : moduleLicenseModels) {
            RepoPath repoPath = InternalRepoPathFactory.create(checkedLicenseModel.getRepoKey(),checkedLicenseModel.getPath());
            checkedLicenseModel.setRepoPath(repoPath);
            RepoPath path = checkedLicenseModel.getRepoPath();
            licensesToWrite.put(path, checkedLicenseModel.getExtractedLicense());
            overriddenLicenses.put(path, checkedLicenseModel.getLicense());
        }

        //For each of the licenses that on this path check if it's already in the overridden map - if not, preserve it
        for (RepoPath path : licensesToWrite.keySet()) {
            //License already set on path differs from the license being overridden - preserve it.
            for (ModuleLicenseModel licenseExistingOnPath : repoPathLicenseMultimap.get(path)) {
                //License already set on path differs from the license being overridden - preserve it.
                if (!overriddenLicenses.get(path).contains(licenseExistingOnPath.getLicense())) {
                    licensesToWrite.put(path, licenseExistingOnPath.getLicense());
                }
            }
        }
        //Write all chosen licenses on each path
        boolean hadErrors = false;
        for (RepoPath path : licensesToWrite.keySet()) {
            if (!setLicenseOnPath(licensesToWrite, path)) {
                hadErrors = true;
            }
        }
        if (hadErrors) {
            response.error("Failed to set properties on some artifacts, check the log for more info");
        }
    }

    private void fetchLicenseToOverride(Collection<ModuleLicenseModel> viewableModels, Multimap<RepoPath,
            ModuleLicenseModel> repoPathLicenseMultimap, List<ModuleLicenseModel> moduleLicenseModels) {
        for (ModuleLicenseModel license : viewableModels) {
            RepoPath repoPath = InternalRepoPathFactory.create(license.getRepoKey(), license.getPath());
            Collection<ModuleLicenseModel> licenses = repoPathLicenseMultimap.get(repoPath);
            for (ModuleLicenseModel subLicense : licenses) {
                if (subLicense.getLicense().getName().equals(license.getLicense().getName())) {
                    subLicense.setExtractedLicense(license.getExtractedLicense());
                    moduleLicenseModels.add(subLicense);
                }
            }
        }
    }

    private boolean setLicenseOnPath(Multimap<RepoPath, LicenseInfo> licensesToWrite, RepoPath path) {
        return addonsManager.addonByType(LicensesAddon.class).setLicensePropsOnPath(path, Sets.newHashSet(licensesToWrite.get(path)));
    }
}
