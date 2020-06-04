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

package org.artifactory.test;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.mime.MimeTypes;
import org.artifactory.mime.MimeTypesReader;
import org.artifactory.version.ArtifactoryVersion;
import org.jfrog.common.ResourceUtils;
import org.jfrog.config.ConfigurationManager;
import org.jfrog.config.watch.FileWatchingManager;
import org.jfrog.config.wrappers.ConfigurationManagerAdapter;
import org.jfrog.config.wrappers.ConfigurationManagerImpl;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.InputStream;

/**
 * A convenience class for tests that require bind/unbind of {@link ArtifactoryHome} (usually for system
 * properties and mime types).
 *
 * @author Yossi Shaul
 */
@SuppressWarnings("squid:S2187")
public class ArtifactoryHomeBoundTest {
    protected MimeTypes mimeTypes;
    protected ArtifactoryHomeStub homeStub;
    protected ConfigurationManager configurationManager;

    @BeforeClass
    public void readMimeTypes() {
        // read and keep the default mime types
        InputStream mimeTypesFile = ResourceUtils.getResource("/META-INF/default/" + ArtifactoryHome.MIME_TYPES_FILE_NAME);
        mimeTypes = new MimeTypesReader().read(mimeTypesFile);
    }

    @BeforeMethod
    public ConfigurationManager bindArtifactoryHome() {
        ArtifactoryHomeStub home = getOrCreateArtifactoryHomeStub();
        if(configurationManager == null) {
            ConfigurationManagerAdapter adapter = new MockConfigurationManagerAdapter(home);
            FileWatchingManager fileWatchingManager = Mockito.mock(FileWatchingManager.class);
            configurationManager = ConfigurationManagerImpl.create(adapter, fileWatchingManager);
        }
        configurationManager.initDbProperties();
        configurationManager.initDefaultFiles();
        home.initPropertiesAndReload();
        ArtifactoryHome.bind(home);
        return configurationManager;
    }

    protected ArtifactoryHomeStub getOrCreateArtifactoryHomeStub() {
        if (homeStub == null) {
            homeStub = new ArtifactoryHomeStub();
            homeStub.setMimeTypes(mimeTypes);
            loadAndBindArtifactoryProperties(homeStub);
        }
        return homeStub;
    }

    private void loadAndBindArtifactoryProperties(ArtifactoryHomeStub artifactory) {
        artifactory.loadSystemProperties();
        artifactory.setProperty(ConstantValues.artifactoryVersion, ArtifactoryVersion.getCurrent().getVersion());
    }

    public void setStringSystemProperty(ConstantValues cv, String value) {
        homeStub.setProperty(cv, value);
    }

    @AfterMethod
    public void unbindArtifactoryHome() {
        ArtifactoryHome.unbind();
    }

    @AfterClass
    public void cleanEnv() {
        if (configurationManager != null) {
            configurationManager.destroy();
        }
    }

    protected ArtifactoryHomeStub getBound() {
        return (ArtifactoryHomeStub) ArtifactoryHomeStub.get();
    }
}
