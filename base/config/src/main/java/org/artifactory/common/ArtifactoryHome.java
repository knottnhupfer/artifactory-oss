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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.mime.MimeTypes;
import org.artifactory.mime.MimeTypesReader;
import org.artifactory.version.ArtifactoryVersionReader;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.config.Home;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.crypto.EncryptionWrapperFactory;
import org.jfrog.security.crypto.PlainTextEncryptionWrapper;
import org.jfrog.security.file.SecurityFolderHelper;
import org.jfrog.security.wrapper.ArtifactoryEncryptionKeyFileFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.dgc.VMID;
import java.util.Collection;
import java.util.Date;

import static org.jfrog.config.wrappers.ConfigurationManagerAdapter.normalizedFilesystemPath;

/**
 * @author yoavl
 */
public class ArtifactoryHome implements Home {
    public static final String LOCK_FILENAME = ".lock";
    public static final String ARTIFACTORY_PROPERTIES_FILE = "artifactory.properties";
    public static final String ARTIFACTORY_GPG_PUBLIC_KEY = "artifactory.gpg.public";
    public static final String ARTIFACTORY_GPG_PRIVATE_KEY = "artifactory.gpg.private";
    public static final String ARTIFACTORY_SSH_PUBLIC_KEY = "artifactory.ssh.public";
    public static final String ARTIFACTORY_SSH_PRIVATE_KEY = "artifactory.ssh.private";
    public static final String BINARY_STORE_FILE_NAME = "binarystore.xml";
    public static final String BINARY_STORE_GCP_CREDS_FILE_NAME = "gcp.credentials.json";
    public static final String LICENSE_FILE_NAME = "artifactory.lic";
    public static final String CLUSTER_LICENSE_FILE_NAME = "artifactory.cluster.license";
    public static final String DB_PROPS_FILE_NAME = "db.properties";
    public static final String ARTIFACTORY_KEY_DEFAULT_FILE_NAME = "artifactory.key";
    public static final String ARTIFACTORY_KEY_DEFAULT_TEMP_FILE_NAME = "artifactory.tmp.key";
    public static final String MASTER_KEY_DEFAULT_FILE_NAME = "master.key";
    public static final String JOIN_KEY_DEFAULT_FILE_NAME = "join.key";
    public static final String COMMUNICATION_KEY_FILE_NAME = "communication.key"; //Deprecated!!
    public static final String COMMUNICATION_TOKEN_FILE_NAME = "communication.token"; //Deprecated
    public static final String ACCESS_ADMIN_TOKEN_FILE_NAME = "access.admin.token";
    public static final String ACCESS_VERSION_PROPERTIES = "access.version.properties";
    public static final String ACCESS_ROOT_CRT = "root.crt";
    public static final String ACCESS_SERVICE_ID = "service_id";
    public static final String CLUSTER_ID = "cluster.id";
    public static final String SYS_PROP = "artifactory.home";
    public static final String SERVLET_CTX_ATTR = "artifactory.home.obj";
    public static final String MISSION_CONTROL_FILE_NAME = "mission.control.properties";
    public static final String ARTIFACTORY_CONVERTER_OBJ = "artifactory.converter.manager.obj";
    public static final String ARTIFACTORY_VERSION_PROVIDER_OBJ = "artifactory.version.provider.obj";
    public static final String ARTIFACTORY_CONFIG_MANAGER_OBJ = "artifactory.config.manager.obj";
    public static final String ARTIFACTORY_CONFIG_FILE = "artifactory.config.xml";
    public static final String ARTIFACTORY_CONFIG_BOOTSTRAP_FILE = "artifactory.config.bootstrap.xml";
    public static final String ARTIFACTORY_SYSTEM_PROPERTIES_FILE = "artifactory.system.properties";
    public static final String LOGBACK_CONFIG_FILE_NAME = "logback.xml";
    public static final String MIME_TYPES_FILE_NAME = "mimetypes.xml";
    public static final String ARTIFACTORY_HA_NODE_PROPERTIES_FILE = "ha-node.properties";
    public static final String ETC_DIR_NAME = "etc";
    public static final String SECURITY_DIR_NAME = "security";
    public static final String ACCESS_CLIENT_DIR_NAME = "access";
    public static final String ACCESS_KEYS_DIR_NAME = "keys";
    public static final String ACCESS_CLIENT_CREDS_FILE_NAME = "access.creds";
    public static final String TRUSTED_KEYS_FILE_NAME = "trusted.keys.json";
    public static final String ARTIFACTORY_KEY_FILE = "artifactory.key";

    private static final String DEFAULT_ENCODING = "utf-8";
    private static final String ENV_VAR = "ARTIFACTORY_HOME";
    private static final String ARTIFACTORY_CONFIG_LATEST_FILE = "artifactory.config.latest.xml";
    private static final String ARTIFACTORY_CONFIG_IMPORT_FILE = "artifactory.config.import.xml";
    private static final String ARTIFACTORY_SAML_ENCRYPTED_ASSERTION_PUBLIC_KEY = "artifactory.saml.encrypted.assertion.public";
    private static final String ARTIFACTORY_SAML_ENCRYPTED_ASSERTION_PRIVATE_KEY = "artifactory.saml.encrypted.assertion.private";
    private static final String ARTIFACTORY_BOOTSTRAP_YAML_IMPORT_FILE = "artifactory.config.import.yml";
    private static final InheritableThreadLocal<ArtifactoryHome> current = new InheritableThreadLocal<>();
    private static final EncryptionWrapper DUMMY_WRAPPER = new PlainTextEncryptionWrapper();
    private static final String VM_HOST_ID = new VMID().toString();
    public static final String ARTIFACTORY_VERSION_PROPERTIES = "/META-INF/artifactory.version.properties";
    private final File homeDir;
    private MimeTypes mimeTypes;
    private HaNodeProperties haNodeProperties;
    private EncryptionWrapper artifactoryEncryptionWrapper;
    private EncryptionWrapper masterEncryptionWrapper;
    private File etcDir;
    private File dataDir;
    private File securityDir;
    private File backupDir;
    private File tempWorkDir;
    private File supportDir;
    private File tempUploadDir;
    private File pluginsDir;
    private File logoDir;
    private File logDir;
    private File accessClientDir;
    private File bundledAccessHomeDir;
    private File externalConversionsDir;
    private File bundledReplicatorHomeDir;
    private ArtifactorySystemProperties artifactorySystemProperties;
    private ArtifactoryDbProperties dbProperties;

    /**
     * protected constructor for testing usage only.
     */
    protected ArtifactoryHome() {
        homeDir = null;
    }

    public ArtifactoryHome(SimpleLog logger) {
        String homeDirPath = findArtifactoryHome(logger);
        homeDir = new File(homeDirPath);
        create();
    }

    public ArtifactoryHome(File homeDir) {
        if (homeDir == null) {
            throw new IllegalArgumentException("Home dir path cannot be null");
        }
        this.homeDir = homeDir;
        create();
    }

    public static void bind(ArtifactoryHome props) {
        current.set(props);
    }

    public static void unbind() {
        current.remove();
    }

    public static boolean isBound() {
        return current.get() != null;
    }

    public static ArtifactoryHome get() {
        ArtifactoryHome home = current.get();
        if (home == null) {
            throw new IllegalStateException("Artifactory home is not bound to the current thread.");
        }
        return home;
    }

    private static void checkWritableDirectory(File dir) {
        if (!dir.exists() || !dir.isDirectory() || !dir.canWrite()) {
            String message = "Directory '" + dir.getAbsolutePath() + "' is not writable!";
            System.out.println(ArtifactoryHome.class.getName() + " - Warning: " + message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks the existence of the logback configuration file under the etc directory. If the file doesn't exist this
     * method will extract a default one from the war.
     */
    public File getLogbackConfig() {
        return new File(etcDir, LOGBACK_CONFIG_FILE_NAME);
    }

    public ArtifactorySystemProperties getArtifactoryProperties() {
        return artifactorySystemProperties;
    }

    public MimeTypes getMimeTypes() {
        return mimeTypes;
    }

    public File getHomeDir() {
        return homeDir;
    }

    public File getDataDir() {
        return dataDir;
    }

    public File getArtifactoryLockFile() {
        return new File(homeDir, normalizedFilesystemPath("data", LOCK_FILENAME));
    }

    @Override
    public File getEtcDir() {
        return etcDir;
    }

    @Override
    public File getSharedKeyFile() {
        return getMasterKeyFile();
    }

    /**
     * Get the HA etc directory. This method exists for backward compatibility only (mission control plugins use
     * this method)
     *
     * @return The etc dir
     * @deprecated use {@link #getEtcDir()} instead.
     */
    @Deprecated
    public File getHaAwareEtcDir() {
        return etcDir;
    }

    @Override
    public File getSecurityDir() {
        return securityDir;
    }

    public File getLogDir() {
        return logDir;
    }

    public File getBackupDir() {
        return backupDir;
    }

    public File getTempWorkDir() {
        return tempWorkDir;
    }

    public File getSupportDir() {
        return supportDir;
    }

    public File getTempUploadDir() {
        return tempUploadDir;
    }

    public File getExternalConversionsDir() {
        return externalConversionsDir;
    }

    @Override
    public File getPluginsDir() {
        return pluginsDir;
    }

    @Override
    public File getLogoDir() {
        return logoDir;
    }

    public File getAccessClientDir() {
        return accessClientDir;
    }

    public File getAccessAdminCredsFile() {
        return new File(accessClientDir,
                normalizedFilesystemPath(ACCESS_KEYS_DIR_NAME, ACCESS_CLIENT_CREDS_FILE_NAME));
    }

    public File getBundledAccessHomeDir() {
        return bundledAccessHomeDir;
    }

    public File getBundledAccessConfigFile() {
        return new File(bundledAccessHomeDir, normalizedFilesystemPath("etc", "access.config"));
    }

    public File getBundledReplicatorHomeDir() {
        return bundledReplicatorHomeDir;
    }

    @Override
    public File getDBPropertiesFile() {
        File secretDbProps = new File(etcDir, ".secrets/.temp.db.properties");
        if (secretDbProps.exists()) {
            return secretDbProps;
        }
        return new File(getEtcDir(), DB_PROPS_FILE_NAME);
    }

    public File getLicenseFile() {
        String licenseFileName = isHaConfigured() ? CLUSTER_LICENSE_FILE_NAME : LICENSE_FILE_NAME;
        return new File(getEtcDir(), licenseFileName);
    }

    public File getOrCreateSubDir(String subDirName) throws IOException {
        return getOrCreateSubDir(getHomeDir(), subDirName);
    }

    public File getArtifactorySystemPropertiesFile() {
        return new File(getEtcDir(), ARTIFACTORY_SYSTEM_PROPERTIES_FILE);
    }

    public File getArtifactoryHaNodePropertiesFile() {
        //This method is called also before the 'etcDir' member is initialized - hence use "etc" explicitly
        return new File(getHomeDir(), normalizedFilesystemPath("etc", ARTIFACTORY_HA_NODE_PROPERTIES_FILE));
    }

    public File getMimeTypesFile() {
        return new File(getEtcDir(), MIME_TYPES_FILE_NAME);
    }

    public File getBinaryStoreXmlFile() {
        return new File(getEtcDir(), BINARY_STORE_FILE_NAME);
    }

    public File getBinaryStoreGcpCredentialsFile() {
        return new File(getEtcDir(), BINARY_STORE_GCP_CREDS_FILE_NAME);
    }

    public File getArtifactoryConfigFile() {
        return new File(getEtcDir(), ARTIFACTORY_CONFIG_FILE);
    }

    public File getArtifactoryConfigLatestFile() {
        return new File(getEtcDir(), ARTIFACTORY_CONFIG_LATEST_FILE);
    }

    public File getArtifactoryConfigImportFile() {
        return new File(getEtcDir(), ARTIFACTORY_CONFIG_IMPORT_FILE);
    }

    public File getArtifactoryBootstrapYamlImportFile() {
        return new File(getEtcDir(), ARTIFACTORY_BOOTSTRAP_YAML_IMPORT_FILE);
    }

    public File getArtifactoryPropertiesFile() {
        return new File(getEtcDir(), ARTIFACTORY_PROPERTIES_FILE);
    }

    public File getArtifactoryOldPropertiesFile() {
        return new File(getDataDir(), ARTIFACTORY_PROPERTIES_FILE);
    }

    public File getArtifactoryConfigBootstrapFile() {
        return new File(getEtcDir(), ARTIFACTORY_CONFIG_BOOTSTRAP_FILE);
    }

    public File getArtifactoryConfigNewBootstrapFile() {
        return new File(getEtcDir(), "new_" + ArtifactoryHome.ARTIFACTORY_CONFIG_BOOTSTRAP_FILE);
    }

    public File getArtifactoryKey() {
        return new File(getSecurityDir(), ArtifactoryHome.ARTIFACTORY_KEY_FILE);
    }

    public File getJoinKeyFile() {
        return new File(securityDir, JOIN_KEY_DEFAULT_FILE_NAME);
    }

    /**
     * Return the Master key file
     *
     * @return The Master key file
     */
    public File getMasterKeyFile() {
        return new File(securityDir, MASTER_KEY_DEFAULT_FILE_NAME);
    }

    @Deprecated //comm.key replaced by master.key
    public File getCommunicationKeyFile() {
        return new File(securityDir, COMMUNICATION_KEY_FILE_NAME);
    }


    public File getAccessAdminTokenFile() {
        return new File(accessClientDir, ACCESS_ADMIN_TOKEN_FILE_NAME);
    }

    public static Path getPartialPathForAccessAdminToken() {
        return Paths.get(ETC_DIR_NAME, SECURITY_DIR_NAME, ACCESS_CLIENT_DIR_NAME, ACCESS_ADMIN_TOKEN_FILE_NAME);
    }

    public File getArtifactoryGpgPublicKeyFile() {
        return new File(getSecurityDir(), ARTIFACTORY_GPG_PUBLIC_KEY);
    }

    public File getArtifactoryGpgPrivateKeyFile() {
        return new File(getSecurityDir(), ARTIFACTORY_GPG_PRIVATE_KEY);
    }

    public File getArtifactorySshPublicKeyFile() {
        return new File(getEtcDir(), normalizedFilesystemPath("ssh", ARTIFACTORY_SSH_PUBLIC_KEY));
    }

    public File getArtifactorySshPrivateKeyFile() {
        return new File(getEtcDir(), normalizedFilesystemPath("ssh", ARTIFACTORY_SSH_PRIVATE_KEY));
    }

    public File getArtifactorySAMLEncryptedAssertionPublicKeyFile() {
        return new File(getSecurityDir(), ARTIFACTORY_SAML_ENCRYPTED_ASSERTION_PUBLIC_KEY);
    }

    public File getArtifactorySAMLEncryptedAssertionPrivateKeyFile() {
        return new File(getSecurityDir(), ARTIFACTORY_SAML_ENCRYPTED_ASSERTION_PRIVATE_KEY);
    }

    @Override
    public EncryptionWrapper getMasterEncryptionWrapper() {
        try {
            if (masterEncryptionWrapper == null) {
                String key = System.getProperty("jfrog.master.key");
                if (StringUtils.isNotBlank(key)) {
                    masterEncryptionWrapper = EncryptionWrapperFactory.aesKeyWrapperFromString(key);
                } else if (getMasterKeyFile().exists()) {
                    masterEncryptionWrapper = EncryptionWrapperFactory.aesKeyWrapperFromFile(getMasterKeyFile());
                } else {
                    // Artifactory should not generate the key by itself, they key should be either provided by a user, or by access itself on bundled mode
                    throw new RuntimeException("Could not load master key. No master key found under the '" + homeDir.getAbsolutePath() + "' dir");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return masterEncryptionWrapper;
    }

    public EncryptionWrapper getArtifactoryEncryptionWrapper() {
        // Temporary fix for the HA master key synchronization issue. We always check if the key exist to ensure that
        // the dummy key will not get 'stuck' in the memory while another ha node re-created master encryption key
        File artifactoryKey = getArtifactoryKey();
        if ((artifactoryEncryptionWrapper == null || artifactoryEncryptionWrapper instanceof PlainTextEncryptionWrapper) && artifactoryKey.exists()) {
            int numOfFallbackKeys = ConstantValues.securityArtifactoryKeyNumOfFallbackKeys.getInt(this);
            artifactoryEncryptionWrapper = EncryptionWrapperFactory
                    .createArtifactoryKeyWrapper(artifactoryKey, artifactoryKey.getParentFile(), numOfFallbackKeys,
                            new ArtifactoryEncryptionKeyFileFilter(
                                    ConstantValues.securityArtifactoryKeyLocation.getString(this)));
        } else if (artifactoryEncryptionWrapper == null) {
            artifactoryEncryptionWrapper = DUMMY_WRAPPER;
        }
        return artifactoryEncryptionWrapper;
    }

    public void unsetArtifactoryEncryptionWrapper() {
        this.artifactoryEncryptionWrapper = null;
    }

    /**
     * Return DB configuration
     */
    public ArtifactoryDbProperties getDBProperties() {
        return dbProperties;
    }

    public ArtifactoryDbProperties initDBProperties() {
        File dbPropertiesFile = getDBPropertiesFile();
        if (!dbPropertiesFile.exists()) {
            throw new IllegalStateException("Artifactory could not start because db.properties could not be found.");
        }
        dbProperties = new ArtifactoryDbProperties(this);
        return dbProperties;
    }

    /**
     * Calculate a unique id for the VM to support Artifactories with the same ip (e.g. accross NATs)
     */
    public String getHostId() {
        if (artifactorySystemProperties != null) {
            String result = artifactorySystemProperties.getProperty(ConstantValues.hostId);
            if (StringUtils.isNotBlank(result)) {
                return result;
            }
        }
        // TODO: Should support the HA Node host id system
        return VM_HOST_ID;
    }

    /**
     * Takes node id from ha properties into account first, used mainly for logs until we sort out RTFACT-13003
     */
    public String getHaAwareHostId() {
        if (haNodeProperties != null) {
            return haNodeProperties.getServerId();
        } else {
            return getHostId();
        }
    }

    /**
     * @return the {@link HaNodeProperties} object that represents the
     * {@link #ARTIFACTORY_HA_NODE_PROPERTIES_FILE} contents, or null if HA was not configured properly
     */
    @Nullable
    public HaNodeProperties getHaNodeProperties() {
        return haNodeProperties;
    }

    /**
     * Returns the content of the artifactory.config.import.xml file
     *
     * @return Content of artifactory.config.import.xml if exists, null if not
     */
    public String getImportConfigXml() {
        File importConfigFile = getArtifactoryConfigImportFile();
        if (importConfigFile.exists()) {
            try {
                String configContent = FileUtils.readFileToString(importConfigFile, DEFAULT_ENCODING);
                if (StringUtils.isNotBlank(configContent)) {
                    File bootstrapConfigFile = getArtifactoryConfigBootstrapFile();
                    org.artifactory.util.Files.switchFiles(importConfigFile, bootstrapConfigFile);
                    return configContent;
                }
            } catch (IOException e) {
                throw toRuntimeExceptionOnFileReadFailure(importConfigFile.getAbsolutePath(), e);
            }
        }
        return null;
    }

    public void renameInitialConfigFileIfExists() {
        File initialConfigFile = getArtifactoryConfigFile();
        if (initialConfigFile.isFile()) {
            org.artifactory.util.Files.switchFiles(initialConfigFile,
                    getArtifactoryConfigBootstrapFile());
        }
    }

    public String getBootstrapConfigXml() {
        File oldLocalConfig = getArtifactoryConfigFile();
        File newBootstrapConfig = getArtifactoryConfigBootstrapFile();
        String result;
        if (newBootstrapConfig.exists()) {
            try {
                result = FileUtils.readFileToString(newBootstrapConfig, DEFAULT_ENCODING);
            } catch (IOException e) {
                throw toRuntimeExceptionOnFileReadFailure(newBootstrapConfig.getAbsolutePath(), e);
            }
        } else if (oldLocalConfig.exists()) {
            try {
                result = FileUtils.readFileToString(oldLocalConfig, DEFAULT_ENCODING);
            } catch (IOException e) {
                throw toRuntimeExceptionOnFileReadFailure(oldLocalConfig.getAbsolutePath(), e);
            }
        } else {
            String resPath = "/META-INF/default/" + ARTIFACTORY_CONFIG_FILE;
            InputStream is = ArtifactoryHome.class.getResourceAsStream(resPath);
            if (is == null) {
                throw new RuntimeException("Could read the default configuration from classpath at " + resPath);
            }
            try {
                result = IOUtils.toString(is, DEFAULT_ENCODING);
            } catch (IOException e) {
                throw toRuntimeExceptionOnFileReadFailure(resPath, e);
            }
        }
        return result;
    }

    private RuntimeException toRuntimeExceptionOnFileReadFailure(String fileLocation, Exception e) {
        return new RuntimeException("Could not read data from '" + fileLocation +
                "' file due to: " + e.getMessage(), e);
    }

    /**
     * return true only if both HA property files are configures HA node properties and cluster properties
     */
    public boolean isHaConfigured() {
        //haNodeProperties is essentially the first thing to be inited when home is constructed, it's not null if the
        //ha-node.properties file existed when init occurred, it's also inited a second time on the reload after
        //startup so we're extra-safe.
        return haNodeProperties != null;
    }

    //Be careful with this, The config manager init calls this when db is not available so it must be taken from META-INF
    public CompoundVersionDetails getRunningArtifactoryVersion() {
        try (InputStream inputStream = ArtifactoryHome.class.getResourceAsStream(ARTIFACTORY_VERSION_PROPERTIES)) {
            CompoundVersionDetails details = ArtifactoryVersionReader
                    .readAndFindVersion(inputStream, ARTIFACTORY_VERSION_PROPERTIES);
            //Sanity check
            if (!details.isCurrent()) {
                throw new IllegalStateException("Running version is not the current version. Running: " + details
                        + " Current: " + details.getVersion());
            }
            return details;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unexpected exception occurred: Fail to load artifactory.properties from class resource", e);
        }
    }

    private void create() {
        try {
            // We need to load the HA node properties in order to fetch the the legacy ha-data and ha-backup location
            initHaNodeProperties();
            // Init home properties
            dataDir = getOrCreateSubDir("data");
            backupDir = getOrCreateSubDir("backup");
            supportDir = getOrCreateSubDir("support");
            etcDir = getOrCreateSubDir(ETC_DIR_NAME);
            securityDir = getOrCreateSubDir(getEtcDir(), SECURITY_DIR_NAME);
            accessClientDir = getOrCreateSubDir(getSecurityDir(), ACCESS_CLIENT_DIR_NAME);
            bundledAccessHomeDir = getOrCreateSubDir("access");
            bundledReplicatorHomeDir = getOrCreateSubDir("replicator");
            externalConversionsDir = getSubDir(getEtcDir(), "conversion");
            SecurityFolderHelper.setPermissionsOnSecurityFolder(securityDir);
            logDir = getOrCreateSubDir("logs");
            File tempRootDir = getOrCreateSubDir(dataDir, "tmp");
            tempWorkDir = getOrCreateSubDir(tempRootDir, "work");
            tempUploadDir = getOrCreateSubDir(tempRootDir, "artifactory-uploads");

            //Check the write access to all directories that need it
            checkWritableDirectory(dataDir);
            checkWritableDirectory(logDir);
            checkWritableDirectory(backupDir);
            checkWritableDirectory(supportDir);
            checkWritableDirectory(tempRootDir);
            checkWritableDirectory(tempWorkDir);
            checkWritableDirectory(tempUploadDir);

            pluginsDir = getOrCreateSubDir(getEtcDir(), "plugins");
            logoDir = getOrCreateSubDir(getEtcDir(), "ui");
            checkWritableDirectory(pluginsDir);
            checkWritableDirectory(logoDir);
            try {
                // Never delete all files because in HA shared env (shared data dir), new nodes that starting might
                // delete files that older nodes just created and yet read/used them.
                AgeFileFilter ageFileFilter = new AgeFileFilter(DateUtils.addDays(new Date(), -1));
                Collection<File> files = FileUtils.listFiles(tempRootDir, ageFileFilter, DirectoryFileFilter.DIRECTORY);
                for (File childFile : files) {
                    FileUtils.forceDelete(childFile);
                }
                // Don't clean up all empty directories blindly because it includes required folders (e.g. work & uploads)
                org.artifactory.util.Files.cleanupEmptyDirectories(tempRootDir, file ->
                        !file.equals(tempWorkDir) && !file.equals(tempUploadDir)
                );
            } catch (Exception e) {
                System.out.println(ArtifactoryHome.class.getName() +
                        " - Warning: unable to clean temporary directories. Cause: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Could not initialize artifactory home directory due to: " + e.getMessage(), e);
        }
    }

    public void initPropertiesAndReload() {
        initArtifactorySystemProperties();
        reloadDbAndHaProperties();
        // Reload the data and backup dirs the directories might change after the NoNFS converter to support legacy
        reloadDataAndBackupDir();
        initMimeTypes();
    }

    /**
     * Reloads the ha-node.properties and db.properties files that may have changed by the no-nfs converters
     */
    private void reloadDbAndHaProperties() {
        initHaNodeProperties();
        initDBProperties();
    }

    /**
     * Reload the data and backup dirs the directories might change after the no-nfs converters to support legacy
     * ha-data and ha-backup dirs
     */
    private void reloadDataAndBackupDir() {
        try {
            dataDir = getOrCreateSubDir("data");
            backupDir = getOrCreateSubDir("backup");
        } catch (Exception e) {
            throw new RuntimeException("Failed to reload the data and backup directories.", e);
        }
    }

    /**
     * loads ha-node.properties into memory if they exist
     */
    public void initHaNodeProperties() {
        //If ha props exist, load them
        File haPropertiesFile = getArtifactoryHaNodePropertiesFile();
        if (haPropertiesFile.exists()) {
            //load ha properties
            haNodeProperties = new HaNodeProperties();
            haNodeProperties.load(haPropertiesFile);
        }
    }

    public void initArtifactorySystemProperties() {
        try {
            File file = getArtifactorySystemPropertiesFile();
            // avoid unstable state - reload to new instance and then replace reference atomically
            ArtifactorySystemProperties newProps = new ArtifactorySystemProperties();
            newProps.loadArtifactorySystemProperties(file, getRunningArtifactoryVersion());
            artifactorySystemProperties = newProps;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse artifactory system properties from: " +
                    getArtifactorySystemPropertiesFile().getAbsolutePath(), e);
        }
    }

    private void initMimeTypes() {
        File mimeTypesFile = getMimeTypesFile();
        try {
            String mimeTypesXml = FileUtils.readFileToString(mimeTypesFile);
            mimeTypes = new MimeTypesReader().read(mimeTypesXml);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse mime types file from: " + mimeTypesFile.getAbsolutePath(), e);
        }
    }

    private File getOrCreateSubDir(File parent, String subDirName) throws IOException {
        File subDir = getEffectiveSubDir(parent, subDirName);
        FileUtils.forceMkdir(subDir);
        return subDir;
    }

    private File getSubDir(File parent, String subDirName) {
        return getEffectiveSubDir(parent, subDirName);
    }

    private File getEffectiveSubDir(File parent, String subDirName) {
        String path = null;
        HaNodeProperties haNodeProperties = getHaNodeProperties();
        if (haNodeProperties != null) {
            path = haNodeProperties.getProperty(subDirName);
        }
        File subDir;
        if (StringUtils.isNotBlank(path)) {
            File userPath = new File(path);
            if (userPath.isAbsolute()) {
                // If the user provided absolute path then there is no need for the parent dir.
                subDir = userPath;
            } else {
                // If the user provided relative path then the "subDir" should be merge of "parent" dir and "userPath".
                subDir = new File(parent, path);
            }
        } else {
            // If the user didn't provide userPath then use the default name
            subDir = new File(parent, subDirName);
        }
        return subDir;
    }

    private String findArtifactoryHome(SimpleLog logger) {
        String home = System.getProperty(SYS_PROP);
        String artHomeSource = "System property";
        if (home == null) {
            //Try the environment var
            home = System.getenv(ENV_VAR);
            artHomeSource = "Environment variable";
            if (home == null) {
                home = new File(System.getProperty("user.home", "."), ".artifactory").getAbsolutePath();
                artHomeSource = "Default (user home)";
            }
        }
        home = home.replace('\\', '/');
        logger.log("Using artifactory.home at '" + home + "' resolved from: " + artHomeSource);
        return home;
    }

    public void writeArtifactoryProperties() {
        File artifactoryPropertiesFile = getArtifactoryPropertiesFile();
        //Copy the artifactory.properties file into the data folder
        try {
            //Copy from default
            URL resource = ArtifactoryHome.class.getResource("/META-INF/" + ARTIFACTORY_PROPERTIES_FILE);
            FileUtils.copyURLToFile(resource, artifactoryPropertiesFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not copy " + ARTIFACTORY_PROPERTIES_FILE + " to " +
                    artifactoryPropertiesFile.getAbsolutePath(), e);
        }
    }

    @Nonnull
    public File getAccessEmigrateMarkerFile() {
        return new File(etcDir, ".emigrate.marker");
    }

    @Nonnull
    public File getAccessResourceTypeConverterMarkerFile() {
        return new File(etcDir, ".resource.type.converter.marker");
    }

    @Nonnull
    public File getAccessUserCustomDataDecryptionMarkerFile() {
        return new File(etcDir, ".custom.data.decryption.marker");
    }

    @Nonnull
    public File getCreateDefaultBuildPermissionMarkerFile() {
        return new File(etcDir, ".default.build.permission.marker");
    }

    @Nonnull
    public File getCreateBackupExcludedBuildNames() {
        return new File(etcDir, ".backup.builds.excluded.marker");
    }

    public File getSkipVerifyPrivilegesMarkerFile() {
        return new File(etcDir, ".skip.priv.check.marker");
    }

    protected void setMimeTypes(MimeTypes mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    /**
     * Missing Closure ;-)
     */
    public interface SimpleLog {
        void log(String message);
    }
}
