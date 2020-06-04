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

package org.artifactory.webapp.servlet;

import org.artifactory.common.ArtifactoryConfigurationAdapter;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.MasterKeyBootstrapUtil;
import org.artifactory.converter.ConverterManager;
import org.artifactory.converter.ConvertersManagerImpl;
import org.artifactory.converter.VersionProviderImpl;
import org.artifactory.version.VersionProvider;
import org.jfrog.config.ConfigurationManager;

import javax.servlet.ServletContext;

import static org.jfrog.config.wrappers.ConfigurationManagerImpl.create;

/**
 * @author Fred Simon on 9/16/16.
 */
public class BasicConfigManagers {

    public final ArtifactoryHome artifactoryHome;
    public final ConfigurationManager configurationManager;
    public final VersionProvider versionProvider;
    public final ConverterManager convertersManager;
    private final MasterKeyBootstrapUtil masterKeyBootstrapUtil;

    BasicConfigManagers(ServletContext servletContext) {
        this.artifactoryHome = (ArtifactoryHome) servletContext.getAttribute(ArtifactoryHome.SERVLET_CTX_ATTR);
        this.versionProvider = (VersionProvider) servletContext
                .getAttribute(ArtifactoryHome.ARTIFACTORY_VERSION_PROVIDER_OBJ);
        this.convertersManager = (ConverterManager) servletContext
                .getAttribute(ArtifactoryHome.ARTIFACTORY_CONVERTER_OBJ);
        this.configurationManager = (ConfigurationManager) servletContext
                .getAttribute(ArtifactoryHome.ARTIFACTORY_CONFIG_MANAGER_OBJ);
        this.masterKeyBootstrapUtil = new MasterKeyBootstrapUtil(artifactoryHome, configurationManager);
    }

    public BasicConfigManagers(ArtifactoryHome artifactoryHome) {
        this(artifactoryHome, create(new ArtifactoryConfigurationAdapter(artifactoryHome)));
    }

    public BasicConfigManagers(ArtifactoryHome artifactoryHome, ConfigurationManager configurationManager) {
        this.artifactoryHome = artifactoryHome;
        // Create configuration manager that will synchronize the shared files
        this.configurationManager = configurationManager;
        // Create the version provider managing running and previous versions
        this.versionProvider = new VersionProviderImpl(artifactoryHome);
        // Create the converter manager that will convert the needed configurations, files and DB tables
        this.convertersManager = new ConvertersManagerImpl(artifactoryHome, versionProvider, configurationManager);
        this.masterKeyBootstrapUtil = new MasterKeyBootstrapUtil(artifactoryHome, configurationManager);
    }

    public void addServletAttributes(ServletContext servletContext) {
        // Add the artifactory home to the servlet context
        servletContext.setAttribute(ArtifactoryHome.SERVLET_CTX_ATTR, artifactoryHome);
        // Add the converterManager to the servlet context
        servletContext.setAttribute(ArtifactoryHome.ARTIFACTORY_CONVERTER_OBJ, convertersManager);
        // Add the version provider to the servlet context
        servletContext.setAttribute(ArtifactoryHome.ARTIFACTORY_VERSION_PROVIDER_OBJ, versionProvider);
        // Add the configuration manager to the servlet context
        servletContext.setAttribute(ArtifactoryHome.ARTIFACTORY_CONFIG_MANAGER_OBJ, configurationManager);
    }

    /**
     * Extract specific init-params from the servlet context and set them in their corresponding {@link ConstantValues}
     * (only if a value exists - non <code>null</code>).
     *
     * @param servletContext the servlet context
     * @see ConstantValues#accessClientServerUrlOverride
     */
    void inheritInitParamsAsConstantValues(ServletContext servletContext) {
        String propertyName = ConstantValues.accessClientServerUrlOverride.getPropertyName();
        String value = servletContext.getInitParameter(propertyName);
        if (value != null) {
            artifactoryHome.getArtifactoryProperties().setProperty(propertyName, value);
        }
    }

    public void initialize() {
        initHomes();
        // Now that we have converted configuration we can load the properties/configuration into memory
        artifactoryHome.initPropertiesAndReload();
    }

    private void initHomes() {
        // Do environment conversion
        versionProvider.initOriginalHomeVersion();
        // assert that all the pre init and home converters that should run satisfy their preconditions before running
        convertersManager.assertPreInitNoConversionBlock();
        convertersManager.convertPreInit();
        // Need to ensure db.properties exist at this point.
        configurationManager.initDbProperties();
        // wait until the DB key exists
        masterKeyBootstrapUtil.handleMasterKey();
        // DBChannel is required for version resolution, now we can create it after the config manager inited
        versionProvider.init(configurationManager.getDBChannel());
        // assert that all the home sync and post init converters that should run satisfy their preconditions before running
        convertersManager.assertPostInitNoConversionBlock();
        // Prepare files and DB before sync
        convertersManager.convertHomeSync();
        // Sync or create default files after mandatory env conversion is done
        configurationManager.startSync();
        convertersManager.convertHome();
    }
}
