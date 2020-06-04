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

import org.artifactory.addon.*;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.XrayDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.home.AddonModel;
import org.artifactory.ui.rest.model.home.HomeWidgetModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AddonInfoWidgetService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private AddonsManager addonsManager;

    private final List<String> supportedJcrAddonIds;

    public AddonInfoWidgetService() {
        supportedJcrAddonIds = Arrays.stream(AddonType.values())
                .filter(addon -> addon == AddonType.DISTRIBUTION || addon == AddonType.DOCKER || addon == AddonType.HELM ||
                        addon == AddonType.AQL || addon == AddonType.S3 || addon == AddonType.GCS ||
                        addon == AddonType.SMART_REPO || addon == AddonType.PROPERTIES)
                .map(AddonType::getConfigureUrlSuffix)
                .collect(Collectors.toList());
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<AddonInfo> installedAddons = addonsManager.getInstalledAddons(null);
        HashMap<String, AddonInfo> addonInfoMap = new HashMap<>();
        installedAddons.forEach(addonInfo -> addonInfoMap.put(addonInfo.getAddonName(), addonInfo));
        List<AddonModel> addonModels = new ArrayList<>();
        updateAddonList(addonInfoMap, addonModels);

        HomeWidgetModel model = new HomeWidgetModel("Addons");
        model.addData("addons", addonModels);
        response.iModel(model);
    }

    /**
     * update addon list data
     *
     * @param addonInfoMap - addon info map
     * @param addonModels  - addons models
     */
    private void updateAddonList(HashMap<String, AddonInfo> addonInfoMap, List<AddonModel> addonModels) {
        if (!isAol()) {
            addonModels.add(new AddonModel(AddonType.HA, addonInfoMap.get("ha"), getAddonLearnMoreUrl("ha"),
                    getAddonConfigureUrl(AddonType.HA.getConfigureUrlSuffix(), false)));
        }
        AddonState ossDefaultState;
        boolean jcrOrJcrAol = isJcrOrJcrAol();
        if (isConanCE() || jcrOrJcrAol) {
            ossDefaultState = AddonState.NOT_LICENSED;
        } else {
            ossDefaultState = AddonState.ACTIVATED;
        }

        addonModels.add(new AddonModel(AddonType.DISTRIBUTION, getAddonInfo(AddonType.DISTRIBUTION, AddonState.ACTIVATED), getAddonLearnMoreUrl("distributionrepo"), getAddonConfigureUrl(AddonType.DISTRIBUTION.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.XRAY, getXrayAddonInfo(), "https://www.jfrog.com/xray/", "https://www.jfrog.com/confluence/display/XRAY/Welcome+to+JFrog+Xray"));
        addonModels.add(new AddonModel(AddonType.JFROG_CLI, getAddonInfo(AddonType.JFROG_CLI, AddonState.ACTIVATED), "https://www.jfrog.com/article/jfrog-cli-automation-scripts/", "https://www.jfrog.com/confluence/display/CLI/Welcome+to+JFrog+CLI"));
        addonModels.add(new AddonModel(AddonType.BUILD, addonInfoMap.get("build"), getAddonLearnMoreUrl("build"), getAddonConfigureUrl(AddonType.BUILD.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.DOCKER, addonInfoMap.get("docker"), getAddonLearnMoreUrl("docker"), getAddonConfigureUrl(AddonType.DOCKER.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.REPLICATION, addonInfoMap.get("replication"), getAddonLearnMoreUrl("replication"), getAddonConfigureUrl(AddonType.REPLICATION.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.MULTIPUSH, getAddonInfo(AddonType.MULTIPUSH), String.format(ConstantValues.addonsInfoUrl.getString(), "replication"), getAddonConfigureUrl(AddonType.MULTIPUSH.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.AQL, getAqlAddonInfo(), getAddonLearnMoreUrl("aql"), getAddonConfigureUrl(AddonType.AQL.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.S3, getAddonInfo(AddonType.S3), getAddonLearnMoreUrl("filestore"), getAddonConfigureUrl(AddonType.S3.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.GCS, getAddonInfo(AddonType.GCS), getAddonLearnMoreUrl("gcs"), getAddonConfigureUrl(AddonType.GCS.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.SHARDING, getAddonInfo(AddonType.SHARDING), getAddonLearnMoreUrl("sharding"), getAddonConfigureUrl(AddonType.SHARDING.getConfigureUrlSuffix(), jcrOrJcrAol)));
        AddonInfo aolAddonPlugin;
        aolAddonPlugin = getUserPluginAddonInfo(addonInfoMap);
        addonModels.add(new AddonModel(AddonType.PLUGINS, aolAddonPlugin, getAddonLearnMoreUrl("plugins"), getAddonConfigureUrl(AddonType.PLUGINS.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.SMART_REPO, getAddonInfoForPro(AddonType.SMART_REPO), getAddonLearnMoreUrl("smart-remote-repositories"), getAddonConfigureUrl(AddonType.SMART_REPO.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.SUMOLOGIC, getAddonInfo(AddonType.SUMOLOGIC, AddonState.ACTIVATED), getAddonLearnMoreUrl("loganalytics"), getAddonConfigureUrl(AddonType.SUMOLOGIC.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.NUGET, addonInfoMap.get("nuget"), getAddonLearnMoreUrl("nuget"), getAddonConfigureUrl(AddonType.NUGET.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.NPM, addonInfoMap.get("npm"), getAddonLearnMoreUrl("npm"), getAddonConfigureUrl(AddonType.NPM.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.COMPOSER, addonInfoMap.get("composer"), getAddonLearnMoreUrl("composer"), getAddonConfigureUrl(AddonType.COMPOSER.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.BOWER, addonInfoMap.get("bower"), getAddonLearnMoreUrl("bower"), getAddonConfigureUrl(AddonType.BOWER.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.COCOAPODS, addonInfoMap.get("cocoapods"), getAddonLearnMoreUrl("cocoapods"), getAddonConfigureUrl(AddonType.COCOAPODS.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.CONAN, addonInfoMap.get("conan"), getAddonLearnMoreUrl("conan"), getAddonConfigureUrl(AddonType.CONAN.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.REST, addonInfoMap.get("rest"), getAddonLearnMoreUrl("rest"), getAddonConfigureUrl(AddonType.REST.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.GITLFS, addonInfoMap.get("git-lfs"), getAddonLearnMoreUrl("git-lfs"), getAddonConfigureUrl(AddonType.GITLFS.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.VAGRANT, addonInfoMap.get("vagrant"), getAddonLearnMoreUrl("vagrant"), getAddonConfigureUrl(AddonType.VAGRANT.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.LDAP, addonInfoMap.get("ldap"), getAddonLearnMoreUrl("ldap"), getAddonConfigureUrl(AddonType.LDAP.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.SSO, addonInfoMap.get("sso"), getAddonLearnMoreUrl("sso"), getAddonConfigureUrl(AddonType.SSO.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.OAUTH, getAddonInfoForPro(AddonType.OAUTH), getAddonLearnMoreUrl("oauth-integration"), getAddonConfigureUrl(AddonType.OAUTH.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.SSH, getAddonInfoForPro(AddonType.SSH), getAddonLearnMoreUrl("ssh"), getAddonConfigureUrl(AddonType.SSH.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.VCS, addonInfoMap.get("vcs"), getAddonLearnMoreUrl("vcs"), getAddonConfigureUrl(AddonType.VCS.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.YUM, addonInfoMap.get("rpm"), getAddonLearnMoreUrl("rpm"), getAddonConfigureUrl(AddonType.YUM.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.DEBIAN, addonInfoMap.get("debian"), getAddonLearnMoreUrl("debian"), getAddonConfigureUrl(AddonType.DEBIAN.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.GEMS, addonInfoMap.get("gems"), getAddonLearnMoreUrl("gems"), getAddonConfigureUrl(AddonType.GEMS.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.OPKG, addonInfoMap.get("opkg"), getAddonLearnMoreUrl("opkg"), getAddonConfigureUrl(AddonType.OPKG.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.PYPI, addonInfoMap.get("pypi"), getAddonLearnMoreUrl("pypi"), getAddonConfigureUrl(AddonType.PYPI.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.CHEF, addonInfoMap.get("chef"), getAddonLearnMoreUrl("chef"), getAddonConfigureUrl(AddonType.CHEF.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.PUPPET, addonInfoMap.get("puppet"), getAddonLearnMoreUrl("puppet"), getAddonConfigureUrl(AddonType.PUPPET.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.HELM, addonInfoMap.get("helm"), getAddonLearnMoreUrl("helm"), getAddonConfigureUrl(AddonType.HELM.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.GO, addonInfoMap.get("go"), getAddonLearnMoreUrl("go"), getAddonConfigureUrl(AddonType.GO.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.CRAN, addonInfoMap.get("cran"), getAddonLearnMoreUrl("cran"), getAddonConfigureUrl(AddonType.CRAN.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.CONDA, addonInfoMap.get("conda"), getAddonLearnMoreUrl("conda"), getAddonConfigureUrl(AddonType.CONDA.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.PROPERTIES, addonInfoMap.get("properties"), getAddonLearnMoreUrl("properties"), getAddonConfigureUrl(AddonType.PROPERTIES.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.SEARCH, addonInfoMap.get("search"), getAddonLearnMoreUrl("search"), getAddonConfigureUrl(AddonType.SEARCH.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.LAYOUTS, addonInfoMap.get("layouts"), getAddonLearnMoreUrl("layouts"), getAddonConfigureUrl(AddonType.LAYOUTS.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.LICENSES, addonInfoMap.get("license"), getAddonLearnMoreUrl("license"), getAddonConfigureUrl(AddonType.LICENSES.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.MAVEN_PLUGIN, getAddonInfo(AddonType.MAVEN_PLUGIN, ossDefaultState), getAddonLearnMoreUrl("maven"), getAddonConfigureUrl(AddonType.MAVEN_PLUGIN.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.GRADLE_PLUGIN, getAddonInfo(AddonType.GRADLE_PLUGIN, ossDefaultState), getAddonLearnMoreUrl("gradle"), getAddonConfigureUrl(AddonType.GRADLE_PLUGIN.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.JENKINS_PLUGIN, getAddonInfo(AddonType.JENKINS_PLUGIN, AddonState.ACTIVATED), getAddonLearnMoreUrl("build"), getAddonConfigureUrl(AddonType.JENKINS_PLUGIN.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.BAMBOO_PLUGIN, getAddonInfo(AddonType.BAMBOO_PLUGIN, AddonState.ACTIVATED), getAddonLearnMoreUrl("build"), getAddonConfigureUrl(AddonType.BAMBOO_PLUGIN.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.TC_PLUGIN, getAddonInfo(AddonType.TC_PLUGIN, AddonState.ACTIVATED), getAddonLearnMoreUrl("build"), getAddonConfigureUrl(AddonType.TC_PLUGIN.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.MSBUILD_PLUGIN, getAddonInfo(AddonType.MSBUILD_PLUGIN, AddonState.ACTIVATED), getAddonLearnMoreUrl("tfs-integration"), getAddonConfigureUrl(AddonType.MSBUILD_PLUGIN.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.SBT, getAddonInfo(AddonType.SBT, ossDefaultState), getAddonLearnMoreUrl("sbt"), getAddonConfigureUrl(AddonType.SBT.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.IVY, getAddonInfo(AddonType.IVY, ossDefaultState), getAddonLearnMoreUrl("ivy"), getAddonConfigureUrl(AddonType.IVY.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.FILTERED_RESOURCES, addonInfoMap.get("filtered-resources"), getAddonLearnMoreUrl("filtered-resources"), getAddonConfigureUrl(AddonType.FILTERED_RESOURCES.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.P2, addonInfoMap.get("p2"), getAddonLearnMoreUrl("p2"), getAddonConfigureUrl(AddonType.P2.getConfigureUrlSuffix(), jcrOrJcrAol)));
        addonModels.add(new AddonModel(AddonType.WEBSTART, addonInfoMap.get("webstart"), getAddonLearnMoreUrl("webstart"), getAddonConfigureUrl(AddonType.WEBSTART.getConfigureUrlSuffix(), jcrOrJcrAol)));
    }

    /**
     * get User plugin for aol or pro
     *
     * @param addonInfoMap - addon info map
     */
    private AddonInfo getUserPluginAddonInfo(HashMap<String, AddonInfo> addonInfoMap) {
        AddonInfo aolAddonPlugin;
        if (isAol()) {
            aolAddonPlugin = getAddonInfo(AddonType.PLUGINS, AddonState.INACTIVATED);
        } else {
            aolAddonPlugin = addonInfoMap.get("plugins");
        }
        return aolAddonPlugin;
    }

    /**
     * return add on lean more url
     *
     * @param addonId - addon id
     */
    private String getAddonLearnMoreUrl(String addonId) {
        return String.format(ConstantValues.addonsInfoUrl.getString(), addonId);
    }

    /**
     * return add on configure more url
     *
     * @param addonId - addon id
     */
    private String getAddonConfigureUrl(String addonId, boolean isJcrEnv) {
        if (isJcrEnv && supportedJcrAddonIds.contains(addonId)) {
            return String.format(ConstantValues.jcrAddonsConfigureUrl.getString(), addonId);
        } else {
            return String.format(ConstantValues.addonsConfigureUrl.getString(), addonId);
        }
    }

    private AddonInfo getAqlAddonInfo() {
        AddonInfo addonInfo = new AddonInfo(AddonType.AQL.getAddonName(),
                AddonType.AQL.getAddonDisplayName(), "", AddonState.ACTIVATED, new Properties(), 10);
        return addonInfo;
    }

    private AddonInfo getXrayAddonInfo() {
        AddonState addonState;
        if (ContextHelper.get().beanForType(AddonsManager.class).isXrayLicensed()) {
            XrayDescriptor xrayDescriptor = centralConfigService.getDescriptor().getXrayConfig();
            if (xrayDescriptor == null) {
                addonState = AddonState.NOT_CONFIGURED;
            } else if (xrayDescriptor.isEnabled()) {
                addonState = AddonState.ACTIVATED;
            } else {
                addonState = AddonState.DISABLED;
            }
        } else {
            addonState = AddonState.NOT_LICENSED;
        }
        return new AddonInfo(AddonType.XRAY.getAddonName(), AddonType.XRAY.getAddonDisplayName(), "", addonState,
                new Properties(), 10);
    }

    /**
     * get addon info
     *
     * @param type - addon type
     */
    private AddonInfo getAddonInfo(AddonType type) {
        boolean haLicensed = ContextHelper.get().beanForType(AddonsManager.class).isHaLicensed();
        return new AddonInfo(type.getAddonName(), type.getAddonDisplayName(), "",
                (haLicensed) ? AddonState.ACTIVATED : AddonState.NOT_LICENSED, new Properties(), 10);
    }

    /**
     * get addon info
     *
     * @param type - addon type
     */
    private AddonInfo getAddonInfoForPro(AddonType type) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        return new AddonInfo(type.getAddonName(), type.getAddonDisplayName(), "",
                (addonsManager.isLicenseInstalled()) ? AddonState.ACTIVATED : AddonState.NOT_LICENSED,
                new Properties(), 10);
    }
    /**
     * get addon info
     *
     * @param type - addon type
     */
    private AddonInfo getAddonInfo(AddonType type, AddonState state) {
        return new AddonInfo(type.getAddonName(), type.getAddonDisplayName(), "", state, new Properties(), 10);
    }

    /**
     * if true - aol license
     */
    private boolean isAol() {
        return ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class).isAol();
    }

    private boolean isConanCE() {
        return ContextHelper.get().beanForType(AddonsManager.class).getArtifactoryRunningMode().isConan();
    }

    private boolean isJcrOrJcrAol() {
        return ContextHelper.get().beanForType(AddonsManager.class).getArtifactoryRunningMode().isJcrOrJcrAol();
    }
}
