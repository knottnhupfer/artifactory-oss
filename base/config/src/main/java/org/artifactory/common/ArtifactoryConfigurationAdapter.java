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

package org.artifactory.common;

import com.google.common.collect.Lists;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.jfrog.config.BroadcastChannel;
import org.jfrog.config.DbChannel;
import org.jfrog.config.Home;
import org.jfrog.config.LogChannel;
import org.jfrog.config.broadcast.TemporaryBroadcastChannelImpl;
import org.jfrog.config.db.CommonDbProperties;
import org.jfrog.config.db.TemporaryDBChannel;
import org.jfrog.config.log.PermanentLogChannel;
import org.jfrog.config.log.TemporaryLogChannel;
import org.jfrog.config.wrappers.*;
import org.jfrog.security.util.Pair;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.artifactory.common.ArtifactoryHome.*;

/**
 * @author gidis
 */
public class ArtifactoryConfigurationAdapter implements ConfigurationManagerAdapter {

    private static final String SECURITY_PREFIX = "artifactory.security.";
    public static final String SIGNING_KEY_DB_CONFIG_NAME = SECURITY_PREFIX + "url.signing.key";

    private static final String META_INF_DEFAULT_PATH = "/META-INF/default/";

    protected DbChannel dbChannel;
    protected LogChannel logChannel;
    protected BroadcastChannel broadcastChannel;
    protected boolean ha;
    protected boolean primary;
    protected final ArtifactoryHome home;

    private final List<MetaInfFile> defaultConfigs;
    private final List<SharedConfigMetadata> sharedConfigs;
    private final List<SharedFolderMetadata> sharedFolders;

    private static final List<String> blackList = Lists.newArrayList(
            SECURITY_PREFIX + "binstore",
            SECURITY_PREFIX + COMMUNICATION_KEY_FILE_NAME,
            SECURITY_PREFIX + COMMUNICATION_KEY_FILE_NAME + ".old",
            SECURITY_PREFIX + COMMUNICATION_TOKEN_FILE_NAME,
            SECURITY_PREFIX + ARTIFACTORY_KEY_DEFAULT_TEMP_FILE_NAME,
            SECURITY_PREFIX + MASTER_KEY_DEFAULT_FILE_NAME,
            SECURITY_PREFIX + JOIN_KEY_DEFAULT_FILE_NAME,
            SECURITY_PREFIX + "access",
            SIGNING_KEY_DB_CONFIG_NAME,
            "artifactory.service_id",
            "artifactory.config.xml"
    );

    /**
     * Initialize JavaFilesWatcher and register the shared files in order to receive events on file changes and then
     * synchronize the changes with the database and the other nodes
     */
    public ArtifactoryConfigurationAdapter(Home home) {
        this.home = (ArtifactoryHome) home;
        this.defaultConfigs = initDefaultConfigs();
        this.sharedConfigs = initSharedConfigs();
        this.sharedFolders = initSharedFolders();
    }

    private List<MetaInfFile> initDefaultConfigs() {
        return Lists.newArrayList(
                new MetaInfFile(META_INF_DEFAULT_PATH + MIME_TYPES_FILE_NAME, home.getMimeTypesFile()),
                new MetaInfFile(META_INF_DEFAULT_PATH + ARTIFACTORY_SYSTEM_PROPERTIES_FILE, home.getArtifactorySystemPropertiesFile()),
                new MetaInfFile(META_INF_DEFAULT_PATH + BINARY_STORE_FILE_NAME, home.getBinaryStoreXmlFile()),
                new MetaInfFile(META_INF_DEFAULT_PATH + LOGBACK_CONFIG_FILE_NAME, home.getLogbackConfig())
        );
    }

    private List<SharedConfigMetadata> initSharedConfigs() {
        List<SharedConfigMetadata> sharedConfigList = Lists.newArrayList(
                // Artifactory system properties
                new SharedConfigMetadata(home.getArtifactorySystemPropertiesFile(), "artifactory.system.properties",
                        META_INF_DEFAULT_PATH + ARTIFACTORY_SYSTEM_PROPERTIES_FILE,
                        true, false, false),
                // mimetypes.xml
                new SharedConfigMetadata(home.getMimeTypesFile(), "artifactory.mimeType",
                        META_INF_DEFAULT_PATH + MIME_TYPES_FILE_NAME,
                        true, false, false),
                // binarystore.xml
                new SharedConfigMetadata(home.getBinaryStoreXmlFile(), "artifactory.binarystore.xml",
                        META_INF_DEFAULT_PATH + BINARY_STORE_FILE_NAME,
                        true, true, false),
                new SharedConfigMetadata(home.getBinaryStoreGcpCredentialsFile(), "artifactory.binarystore.gcp.credentials.json",
                        null, false, true, false),
                // Artifactory encryption key
                new SharedConfigMetadata(home.getArtifactoryKey(), "artifactory.security.artifactory.key",
                        null, false, true, false),
                // Access creds
                new SharedConfigMetadata(home.getAccessAdminCredsFile(),
                        "artifactory.security.access/keys/access.creds",
                        null, false, true, false),
                // shared admin token
                new SharedConfigMetadata(home.getAccessAdminTokenFile(),
                        "artifactory.security.access/access.admin.token",
                        null, false, true, false)
        );
        if (home.isHaConfigured()) {
            // Artifactory cluster License
            sharedConfigList.add(new SharedConfigMetadata(home.getLicenseFile(), "artifactory.cluster.license",
                    null, false, true, false));
        }
        return sharedConfigList;
    }

    private List<SharedFolderMetadata> initSharedFolders() {
        return Lists.newArrayList(
                // Plugins dir
                new SharedFolderMetadata(home.getPluginsDir(), "artifactory.plugin.", false, false),
                // UI logo dir
                new SharedFolderMetadata(home.getLogoDir(), "artifactory.ui.", false, false),
                new SharedFolderMetadata(home.getSecurityDir(), SECURITY_PREFIX, true, false)
        );
    }

    @Override
    public void initialize() {
        this.home.initArtifactorySystemProperties();
        //getBooleanProperty does not explode.
        this.logChannel = new TemporaryLogChannel(home.getArtifactoryProperties().getBooleanProperty(ConstantValues.bootstrapLoggerDebug));
        this.broadcastChannel = new TemporaryBroadcastChannelImpl();
        this.primary = home.getArtifactoryHaNodePropertiesFile().exists() && home.getHaNodeProperties() != null
                && home.getHaNodeProperties().isPrimary();
        this.ha = home.getArtifactoryHaNodePropertiesFile().exists();
    }

    @Override
    public List<String> getBlackListConfigs() {
        return blackList;
    }

    @Override
    public List<MetaInfFile> getDefaultConfigs() {
        return defaultConfigs;
    }

    /**
     * The method register the shared files in the JavaFilesWatcher to receive file change on the files
     */
    @Override
    public List<SharedConfigMetadata> getSharedConfigs() {
        return sharedConfigs;
    }

    @Override
    public List<SharedFolderMetadata> getSharedFolders() {
        return sharedFolders;
    }

    @Override
    public Home getHome() {
        return home;
    }

    @Override
    public void unbind() {
        ArtifactoryHome.unbind();
    }

    @Override
    public void bind() {
        ArtifactoryHome.bind(home);
    }

    @Override
    public boolean allowDbUpdate() {
        return !ha || primary;
    }

    @Override
    public LogChannel getLogChannel() {
        return logChannel;
    }

    @Override
    public DbChannel getDbChannel() {
        if (dbChannel == null) {
            ArtifactoryDbProperties dbProperties = this.home.initDBProperties();
            blockIfHAWithDerby(dbProperties);
            this.dbChannel = new TemporaryDBChannel(new CommonDbProperties(dbProperties.getPassword(),
                    dbProperties.getConnectionUrl(), dbProperties.getUsername(), dbProperties.getDbType(),
                    dbProperties.getDriverClass()));
        }
        return dbChannel;
    }

    @Override
    public BroadcastChannel getBroadcastChannel() {
        return broadcastChannel;
    }

    @Override
    public void destroy() {
        if (broadcastChannel != null) {
            broadcastChannel.destroy();
        }
        if (dbChannel != null) {
            dbChannel.close();
        }
    }

    @Override
    public String getName() {
        return home.getHaAwareHostId();
    }

    /**
     * The method replaces the initial log channel into the permanent implementation
     */
    @Override
    public void setPermanentLogChannel() {
        // Replace log channel
        Logger logger = LoggerFactory.getLogger(ConfigurationManagerImpl.class);
        logChannel = new PermanentLogChannel(logger);
    }

    /**
     * The method replaces the initial broadcast channel into the permanent implementation
     */
    @Override
    public void setPermanentBroadcastChannel(BroadcastChannel broadcastChannel) {
        // Make sure that this can be called only once
        if (this.broadcastChannel instanceof TemporaryBroadcastChannelImpl) {
            // Replace broadcastChannel but first fire accumulated events.
            TemporaryBroadcastChannelImpl initialBroadcastChannel = (TemporaryBroadcastChannelImpl) this.broadcastChannel;
            // Replace broadcastChannel
            this.broadcastChannel = broadcastChannel;
            // Fire accumulated notifications events
            Set<Pair<String, FileEventType>> notifications = initialBroadcastChannel.getNotifications();
            notifications.forEach(pair -> broadcastChannel.notifyConfigChanged(pair.getFirst(), pair.getSecond()));
        } else {
            // Can reach here only on reload
            this.broadcastChannel = broadcastChannel;
        }
    }

    /**
     * The method replaces the initial db channel into the permanent implementation
     */
    @Override
    public void setPermanentDBChannel(DbChannel permanentDbChannel) {
        try {
            DbChannel tempDBChanel = dbChannel;
            dbChannel = permanentDbChannel;
            // Replace DB channel
            if (tempDBChanel instanceof TemporaryDBChannel) {
                getLogChannel().info("Replacing temporary DB channel with permanent DB channel");
                tempDBChanel.close();
                getLogChannel().info("Successfully closed temporary DB channel");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to replace temporary db channel with the permanent one due to: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public int getRetryAmount() {
        return ConstantValues.configurationManagerRetryAmount.getInt();
    }

    // except from devenv, it is not allowed to use HA with derby
    private void blockIfHAWithDerby(ArtifactoryDbProperties dbProperties) {
        if (DbType.DERBY == dbProperties.getDbType() && home.isHaConfigured() && !ConstantValues.devHa.getBoolean()) {
            throw new IllegalStateException("Cannot use Derby as the database type in HA mode, please check "
                    + home.getDBPropertiesFile().getAbsolutePath());
        }
    }
}
