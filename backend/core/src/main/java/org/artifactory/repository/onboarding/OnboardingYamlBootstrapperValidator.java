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

package org.artifactory.repository.onboarding;

import com.google.common.base.Strings;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.addon.license.ArtifactoryHaLicenseDetails;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.security.FirstStartupHelper;
import org.artifactory.util.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * @author nadavy
 */
class OnboardingYamlBootstrapperValidator {
    private static final Logger log = LoggerFactory.getLogger(OnboardingYamlBootstrapper.class);

    private MutableCentralConfigDescriptor centralConfigDescriptor;
    private final String importYamlFilename;

    OnboardingYamlBootstrapperValidator(
            MutableCentralConfigDescriptor centralConfigDescriptor, String importYamlFilename) {
        this.centralConfigDescriptor = centralConfigDescriptor;
        this.importYamlFilename = importYamlFilename;
    }

    /**
     * Check the artifactory is in "vanilla" state, and that the desired settings are valid
     */
    boolean areConfigurationActionsValid(ConfigurationYamlModel configurationYamlModel) {
        // check if default proxy is already configured, base url is already configured, or doesn't have only empty default repo
        if (centralConfigDescriptor.getDefaultProxy() != null &&
                configurationYamlModel.GeneralConfiguration.proxies != null) {
            log.error("can't import file {} - default proxy already exists", importYamlFilename);
            return false;
        }
        if (!validateBaseUrl(configurationYamlModel)) {
            return false;
        }
        return true;
    }

    /**
     * Validate that if base url is given in the yaml file, it's a valid url
     */
    private boolean validateBaseUrl(ConfigurationYamlModel configurationYamlModel) {
        if (configurationYamlModel.GeneralConfiguration == null) {
            return true;
        }
        if (centralConfigDescriptor.getUrlBase() != null &&
                configurationYamlModel.GeneralConfiguration.baseUrl != null) {
            log.error("can't import file {} - default base url already been set", importYamlFilename);
            return false;
        }
        if (!Strings.isNullOrEmpty(configurationYamlModel.GeneralConfiguration.baseUrl)) {
            UrlValidator urlValidator = new UrlValidator("http", "https");
            try {
                urlValidator.validate(configurationYamlModel.GeneralConfiguration.baseUrl);
            } catch (UrlValidator.UrlValidationException e) {
                log.error("can't import file {} - base url is not a valid url", importYamlFilename);
                log.debug("can't import file {} - base url is not a valid url", importYamlFilename, e);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if user is trying to add a license while a license is already installed and it's not the same one,
     * or if given license isn't valid.
     * return false in those cases, otherwise true
     */
    boolean isYamlLicenseActionValid(ConfigurationYamlModel configurationYamlModel) {
        // check if given license is installed (or not the same one in yaml) and valid
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        if (addonsManager.getArtifactoryRunningMode().isHa()) {
            return isHaLicenseActionValid(configurationYamlModel, addonsManager);
        } else {
            return isNoHaLicenseActionValid(configurationYamlModel, addonsManager);
        }

    }

    /**
     * No HA license should not be different then the one installed and should be valid
     * if no license is installed, a license must be provided in the yaml
     */
    private boolean isNoHaLicenseActionValid(ConfigurationYamlModel configurationYamlModel,
            AddonsManager addonsManager) {
        GeneralConfigurationYamlModel generalConfiguration = configurationYamlModel.GeneralConfiguration;
        if ((addonsManager.getArtifactoryRunningMode().isOss() || addonsManager.getArtifactoryRunningMode().isConan() || addonsManager.getArtifactoryRunningMode().isJCR()) && (generalConfiguration == null ||
                generalConfiguration.licenseKey == null)) {
            return true;
        }
        String yamlLicense = (generalConfiguration == null) ? null : generalConfiguration.licenseKey;
        if (!Strings.isNullOrEmpty(yamlLicense)) {
            String installedLicense = addonsManager.getLicenseKey();
            if (addonsManager.isLicenseInstalled() && !formattedLicensesAreEqual(yamlLicense, installedLicense)) {
                log.error("can't import {} - license already installed", importYamlFilename);
                return false;
            } else if (!addonsManager.isLicenseKeyValid(yamlLicense, true, false).isValid()) {
                log.error("can't import file {} - license isn't valid", importYamlFilename);
                return false;
            }
        } else if (!addonsManager.isLicenseInstalled() && !(addonsManager instanceof OssAddonsManager)) {
                log.error("can't import file {} - no license is installed and none provided", importYamlFilename);
                return false;
        }
        return true;
    }

    /**
     * Formats and checks if installed license and yaml license are the same license
     */
    private boolean formattedLicensesAreEqual(String yamlLicense, String installedLicense) {
        return installedLicense.replaceAll(" |\\r?\\n", "").equals(
                yamlLicense.replaceAll(" |\\r?\\n", ""));
    }

    /**
     * Check if current and given license are valid
     */
    private boolean isHaLicenseActionValid(ConfigurationYamlModel configurationYamlModel, AddonsManager addonsManager) {
        Set<String> licenseKeys = configurationYamlModel.GeneralConfiguration.licenseKeys;
        if (licenseKeys != null && !licenseKeys.isEmpty()) {
            if (checkValidHaClusterLicenseExists(addonsManager)) {
                return false;
            }
            boolean anyLicenseNotValid = licenseKeys.stream()
                    .anyMatch(licenseKey -> !addonsManager.isLicenseKeyValid(licenseKey, false, false).isValid());
            return !anyLicenseNotValid;
        }
        return true;
    }

    /**
     * Check that the cluster license pool doesn't have any valid non trial license in it
     */
    private boolean checkValidHaClusterLicenseExists(AddonsManager addonsManager) {
        List<ArtifactoryHaLicenseDetails> clusterLicensesDetails = addonsManager.getClusterLicensesDetails();
        return !clusterLicensesDetails.isEmpty() && clusterLicensesDetails.stream().anyMatch(this::checkLicenseHaAndValid);
    }

    private boolean checkLicenseHaAndValid(ArtifactoryHaLicenseDetails license) {
        return license.getType().equalsIgnoreCase("Enterprise") || license.getType().equals("Trial");
    }

    /**
     * @return true if there are only 2 repo in artifactory - the starting example repo and the build-info repo
     */
    boolean hasOnlyEmptyDefaultRepos() {
        return FirstStartupHelper.onlyDefaultReposExist(centralConfigDescriptor);
    }

}
