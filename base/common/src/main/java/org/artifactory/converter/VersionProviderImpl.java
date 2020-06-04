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

package org.artifactory.converter;


import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.config.db.ArtifactoryCommonDbPropertiesService;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.config.db.DbVersionDataAccessObject;
import org.artifactory.common.storage.db.properties.DbVersionInfo;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionReader;
import org.artifactory.version.CompoundVersionDetails;
import org.artifactory.version.VersionProvider;
import org.jfrog.config.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;

/**
 * @author Gidi Shabat
 */
public class VersionProviderImpl implements VersionProvider {
    private static final Logger log = LoggerFactory.getLogger(VersionProviderImpl.class);

    // Test use only
    private static final String FROM_VERSION_OVERRIDE_SYSTEM_PROP = "artifactory.debug.fromVersion";

    /**
     * The current running version, discovered during runtime.
     */
    private final CompoundVersionDetails runningVersion;

    /**
     * Denotes the original version this instance is coming from, if there was one, null if no previous version data
     * was available.
     */
    private CompoundVersionDetails originalDbVersion;

    private ArtifactoryHome artifactoryHome;
    private CompoundVersionDetails originalHomeVersion;

    public VersionProviderImpl(ArtifactoryHome artifactoryHome) {
        this.artifactoryHome = artifactoryHome;
        this.runningVersion = artifactoryHome.getRunningArtifactoryVersion();
    }

    @Override
    public void initOriginalHomeVersion() {
        resolveOriginalVersionFromOverridePropertyForTest();
        if (originalHomeVersion != null) {
            //Original version resolved from debug property - good to go.
            return;
        }
        try {
            loadOriginalHomeVersion();
            log.debug("Last Artifactory database version is: {}", originalHomeVersion == null ? "New Installation" :
                    originalHomeVersion.getVersion().getVersion());
        } catch (Exception e) {
            log.error("Failed to resolve version information: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Loads the original version from the database.
     */
    @Override
    public void init(DbChannel dbChannel) {
        // Used for test only!
        resolveOriginalVersionFromOverridePropertyForTest();
        if (originalDbVersion != null) {
            //Original version resolved from debug property - good to go.
            return;
        }
        try {
            loadOriginalDbVersion(dbChannel);
            log.debug("Last Artifactory database version is: {}", originalDbVersion == null ? "New Installation" :
                    originalDbVersion.getVersion().getVersion());
        } catch (Exception e) {
            log.error("Failed to resolve version information: {}", e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void loadOriginalDbVersion(DbChannel dbChannel) {
        DbVersionInfo dbVersionInfo = getVersionInfoFromDbIfExists(dbChannel);
        // Handling the version is based on the existence of the db_properties table
        if (dbVersionInfo != null) {
            populateExistingVersionFromDb(dbVersionInfo);
        } else {
            log.debug("Resolved original db version '{}' from existing local artifactory.properties file",
                    originalHomeVersion.getVersion().getVersion());
            originalDbVersion = originalHomeVersion;
        }
    }

    /**
     * Populates version information from the db version information if it exists.
     */
    private void populateExistingVersionFromDb(DbVersionInfo dbProperties) {
        if (dbProperties == null) {
            // Special case for test - use latest version
            if (Boolean.valueOf(System.getProperty(ConstantValues.test.getPropertyName()))) {
                // If test get the version before the latest one to force most current conversion to run on the tests
                ArtifactoryVersion artifactoryVersion = ArtifactoryVersion.getCurrent();
                originalDbVersion = new CompoundVersionDetails(artifactoryVersion, "TEST", 0L);
                log.debug("Resolved original db version '{}' from db", originalHomeVersion.getVersion().getVersion());
            } else {
                log.warn("db_properties table exists in database but contains no information - version information will"
                        + " be resolved based on the local filesystem if such information exists.");
            }
        } else {
            originalDbVersion = getDbCompoundVersionDetails(dbProperties);
        }
    }

    /**
     * Provides an access interface to the db_properties table, depending on the state of the application's context
     * returns null if no version information is stored in the db or if the table does not exist
     */
    private DbVersionInfo getVersionInfoFromDbIfExists(DbChannel dbChannel) {
        DbVersionInfo versionInfo = null;
        // Context is null --> still in conversion phase, need to use temp db channel
        if (ContextHelper.get() == null) {
            versionInfo = tryToResolveFromDbUsingTempChannel(dbChannel);
        } else {
            try {
                ArtifactoryCommonDbPropertiesService dbPropsService =
                        ContextHelper.get().beanForType(ArtifactoryCommonDbPropertiesService.class);
                if (dbPropsService.isDbPropertiesTableExists()) {
                    versionInfo = dbPropsService.getDbVersionInfo();
                }
            } catch (Exception e) {
                log.warn("Failed to retrieve version information from DB using DbPropertiesService");
                if (Boolean.valueOf(System.getProperty(ConstantValues.test.getPropertyName()))) {
                    versionInfo = tryToResolveFromDbUsingTempChannel(dbChannel);
                } else {
                    throw e;
                }
            }
        }
        return versionInfo;
    }

    private void loadOriginalHomeVersion() {
        File artifactoryPropertiesFile = artifactoryHome.getArtifactoryPropertiesFile();
        if (!artifactoryPropertiesFile.exists()) {
            // Conversion was probably not done yet, try to get the file from the previous location
            artifactoryPropertiesFile = artifactoryHome.getArtifactoryOldPropertiesFile();
            if (!artifactoryPropertiesFile.exists()) {
                // If the properties file doesn't exists, then create it in the new location
                artifactoryPropertiesFile = artifactoryHome.getArtifactoryPropertiesFile();
                artifactoryHome.writeArtifactoryProperties();
            }
        }
        // Load the original home version
        originalHomeVersion = ArtifactoryVersionReader.readFromFileAndFindVersion(artifactoryPropertiesFile);
    }

    private DbVersionInfo tryToResolveFromDbUsingTempChannel(DbChannel dbChannel) {
        DbVersionDataAccessObject versionInfoDao;
        DbVersionInfo versionInfo = null;
        // This will run after local environment conversion for sure so we should have db.properties by now
        // if this was an upgrade, null means this is a new installation.
        ArtifactoryDbProperties dbConfig = artifactoryHome.getDBProperties();
        if (dbConfig != null) {
            versionInfoDao = new DbVersionDataAccessObject(dbChannel);
            if (versionInfoDao.isDbPropertiesTableExists()) {
                versionInfo = versionInfoDao.getDbVersion();
            }
        }
        return versionInfo;
    }

    @Override
    public CompoundVersionDetails getRunning() {
        return runningVersion;
    }

    /**
     * The originalServiceVersion value is null until access to db is allowed
     */
    public CompoundVersionDetails getOriginalDbVersion() {
        return originalDbVersion;
    }

    @Nullable
    @Override
    public CompoundVersionDetails getOriginalHomeVersion() {
        return originalHomeVersion;
    }


    /**
     * <b>Used for test purposes</b>, gives you the ability to control the original version (and thus what conversions will
     * run) by using the {@link this#FROM_VERSION_OVERRIDE_SYSTEM_PROP} property that you can pass to the jvm when
     * starting the instance.
     */
    private void resolveOriginalVersionFromOverridePropertyForTest() {
        String versionOverride = System.getProperty(FROM_VERSION_OVERRIDE_SYSTEM_PROP);
        if (StringUtils.isNotBlank(versionOverride)) {
            log.warn("Version override property detected - 'From Version' will be set to {}", versionOverride);
            ArtifactoryVersion resolvedFromVersion;
            try {
                resolvedFromVersion = ArtifactoryVersion.valueOf(versionOverride);
                originalDbVersion = new CompoundVersionDetails(resolvedFromVersion,"UNDEFINED", 0L);
                originalHomeVersion = originalDbVersion;
            } catch (IllegalArgumentException iae) {
                log.error("Bad version value {} !", versionOverride);
            }
        }
    }

    private CompoundVersionDetails getDbCompoundVersionDetails(DbVersionInfo dbProperties) {
        return ArtifactoryVersionReader.getCompoundVersionDetails(
                dbProperties.getArtifactoryVersion(), getRevisionStringFromInt(dbProperties.getArtifactoryRevision()),
                "" + dbProperties.getArtifactoryRelease());
    }

    private String getRevisionStringFromInt(int rev) {
        if (rev <= 0 || rev == Integer.MAX_VALUE) {
            return "" + Integer.MAX_VALUE;
        }
        return "" + rev;
    }
}