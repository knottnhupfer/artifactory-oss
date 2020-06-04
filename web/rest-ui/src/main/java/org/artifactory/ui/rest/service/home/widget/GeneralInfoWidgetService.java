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

package org.artifactory.ui.rest.service.home.widget;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.version.VersionHolder;
import org.artifactory.api.version.VersionInfoService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.home.HomeWidgetModel;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GeneralInfoWidgetService implements RestService {

    private static final String ARTIFACTORY_ACCOUNT_MANAGEMENT_URL = "artifactory.accountManagement.url";
    private static final String DEFAULT_ACCOUNT_MANAGEMENT_URL = "http://localhost:8086/dashboard/webapp";

    private String accountManagementUrl = DEFAULT_ACCOUNT_MANAGEMENT_URL;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private VersionInfoService versionInfoService;

    @PostConstruct
    private void initialize() {
        ArtifactorySystemProperties artifactorySystemProperties = ArtifactoryHome.get().getArtifactoryProperties();
        accountManagementUrl = artifactorySystemProperties
                .getProperty(ARTIFACTORY_ACCOUNT_MANAGEMENT_URL, DEFAULT_ACCOUNT_MANAGEMENT_URL);
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        HomeWidgetModel model = new HomeWidgetModel("General Info");
        populateGeneralInfo(model);
        populateArtifactoryVersion(request, model);
        response.iModel(model);
    }

    private void populateGeneralInfo(HomeWidgetModel model) {
        if (authorizationService.isAdmin()) {
            model.addData("upTime", getUptime());
        }
        model.addData("accountManagementLink", getAccountManagementLink());
        model.addData("displayAccountManagementLink", displayAccountManagementLink());
    }

    private void populateArtifactoryVersion(ArtifactoryRestRequest request, HomeWidgetModel widgetModel) {
        Map<String, String> headersMap = RequestUtils.getHeadersMap(request.getServletRequest());
        VersionHolder versionHolder = versionInfoService.getLatestVersion(headersMap, true);
        CentralConfigDescriptor configDescriptor = configService.getDescriptor();

        // Current version
        widgetModel.addData("version", ConstantValues.artifactoryVersion.getString());

        if (ConstantValues.versionQueryEnabled.getBoolean() && !configDescriptor.isOfflineMode()) {
            String latestVersion = versionHolder.getVersion();
            String latestVersionUrl = versionHolder.getDownloadUrl();
            if (latestVersion != null && !latestVersion.equals("NA")) {
                widgetModel.addData("latestRelease", latestVersion);
            }
            widgetModel.addData("latestReleaseLink", latestVersionUrl);
        }
    }

    /**
     * return system up time
     *
     * @return up time as string
     */
    private String getUptime() {
        long uptime = ContextHelper.get().getUptime();
        return DurationFormatUtils.formatDuration(uptime, "d'd' H'h' m'm' s's'");
    }

    /**
     * if true - aol license
     */
    private boolean isAol() {
        return ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class).isAol();
    }

    /**
     * display account managements link
     */
    private boolean displayAccountManagementLink() {
        return isAol() && ConstantValues.aolDisplayAccountManagementLink.getBoolean();
    }

    /**
     * get account managements link
     */
    private String getAccountManagementLink() {
        return accountManagementUrl;
    }
}
