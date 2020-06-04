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
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.utils.predefinevalues.PreDefineValues;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.AbstractBuildService;
import org.jfrog.build.api.Build;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ChangeBuildLicenseService extends AbstractBuildService {

    private RepositoryService repositoryService;
    private AddonsManager addonsManager;

    @Autowired
    public ChangeBuildLicenseService(RepositoryService repositoryService, AddonsManager addonsManager) {
        this.repositoryService = repositoryService;
        this.addonsManager = addonsManager;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String id = request.getQueryParamByKey("id");
        String repoKey = request.getQueryParamByKey("repoKey");
        String path = request.getQueryParamByKey("path");
        Build build = getBuild(request, response);
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        Multimap<RepoPath, ModuleLicenseModel> repoPathLicenseMultimap =
                addonsManager.addonByType(LicensesAddon.class).populateLicenseInfoSynchronously(build, false);
        Map<String, LicenseInfo> currentValues = getCurrentValues(id, repoPath, repoPathLicenseMultimap);
        PreDefineValues preDefineValues = getLicenseValues(repoPath, currentValues);
        response.iModel(preDefineValues);
    }

    /**
     * get license preDefine values
     *
     * @param repoPath - repo path
     * @return - pre define values
     */
    private PreDefineValues getLicenseValues(RepoPath repoPath, Map<String, LicenseInfo> currentValues) {
        PreDefineValues values = new PreDefineValues();
        String name = "artifactory.licenses";
        Map<String, Property> propertyItemMap = createPropertyItemMap(repoPath);
        if (!propertyItemMap.isEmpty()) {
            List<PredefinedValue> predefinedValues = propertyItemMap.get(name).getPredefinedValues();
            List<String> listOfPredefineValuesAsString = new ArrayList<>();
            List<String> selectedValues = new ArrayList<>();
            predefinedValues.forEach(predefinedValue -> {
                if (predefinedValue.isDefaultValue() || currentValues.get(predefinedValue.getValue()) != null) {
                    selectedValues.add(predefinedValue.getValue());
                } else {
                    listOfPredefineValuesAsString.add(predefinedValue.getValue());
                }
            });
            values.setSelectedValues(selectedValues);
            values.setPredefinedValues(listOfPredefineValuesAsString);
        }
        return values;
    }

    /**
     * Get all licenses that are currently on the models for a specific id and repo path
     *
     * @param id       The id of the model
     * @param repoPath The repo path of the model
     * @return The current values (licenses) for a specific id and repo path.
     */
    private Map<String, LicenseInfo> getCurrentValues(String id, RepoPath repoPath, Multimap<RepoPath, ModuleLicenseModel> LicenseMap) {
        Map<String, LicenseInfo> licenseMap = new HashMap<>();
        Iterable<ModuleLicenseModel> modelsWithSameId =
                Iterables.filter(LicenseMap.get(repoPath), new SameIdPredicate(id));
        for (ModuleLicenseModel moduleLicenseModel : modelsWithSameId) {
            LicenseInfo licenseInfo = moduleLicenseModel.getLicense();
            if (licenseInfo.isValidLicense()) {
                licenseMap.put(licenseInfo.getName(), licenseInfo);
            }
        }
        return licenseMap;
    }

    private static class SameIdPredicate implements Predicate<ModuleLicenseModel> {

        private String id;

        private SameIdPredicate(String id) {
            this.id = id;
        }

        @Override
        public boolean apply(@Nonnull ModuleLicenseModel input) {
            return input.getId().equals(id);
        }
    }

    /**
     * create property map by repo path
     *
     * @param repoPath - repo path
     * @return map of properties
     */
    private Map<String, Property> createPropertyItemMap(RepoPath repoPath) {
        Map<String, Property> propertyItemMap = new HashMap<>();
        LocalRepoDescriptor descriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        List<PropertySet> propertySets = new ArrayList<>(descriptor.getPropertySets());
        for (PropertySet propertySet : propertySets) {
            List<Property> propertyList = propertySet.getProperties();
            for (Property property : propertyList) {
                propertyItemMap.put(propertySet.getName() + "." + property.getName(), property);
            }
        }
        return propertyItemMap;
    }
}
