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

package org.artifactory.ui.rest.service.artifacts.search.versionsearch;

import org.artifactory.api.properties.PropertiesService;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.NativeProperty;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.PropertiesNativeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.PATH;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeGetPropsService implements RestService {

    private PropertiesService propertiesService;

    @Autowired
    public VersionNativeGetPropsService(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String path = request.getQueryParamByKey(PATH);
        Properties properties = propertiesService.getProperties(RepoPathFactory.create(path));

        Map<String, NativeProperty> propResults = new HashMap<>();
        properties.entries().forEach(entry -> mergeProps(propResults, entry.getKey(), entry.getValue()));

        response.iModel(new PropertiesNativeModel(new ArrayList<>(propResults.values())));
    }

    private void mergeProps(Map<String, NativeProperty> allProps, String propName, String propValue) {
        if (allProps.get(propName) == null) {
            NativeProperty property = new NativeProperty();
            setProperty(property, propName, propValue);
            allProps.put(propName, property);
        } else {
            allProps.get(propName).addPropValue(propValue);
        }
    }

    private void setProperty(NativeProperty property, String propName, String propValue) {
        property.setKey(propName);
        property.addPropValue(propValue);
    }
}
