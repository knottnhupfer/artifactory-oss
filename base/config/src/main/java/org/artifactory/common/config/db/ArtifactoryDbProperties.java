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

package org.artifactory.common.config.db;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.common.property.LinkedProperties;
import org.jfrog.storage.DbType;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * @author Gidi Shabat
 */
public class ArtifactoryDbProperties {

    public static final int DEFAULT_MAX_ACTIVE_CONNECTIONS = 98;
    public static final int DEFAULT_MAX_IDLE_CONNECTIONS = 98;
    private final LinkedProperties props;
    private final DbType dbType;
    private final Optional<DbType> lockingSpecificDbType;
    private ArtifactoryHome home;
    private File dbPropertiesFile;

    public ArtifactoryDbProperties(ArtifactoryHome home) {
        this(home, home.getDBPropertiesFile());
    }

    public ArtifactoryDbProperties(ArtifactoryHome home, File dbPropertiesFile) {
        this.dbPropertiesFile = dbPropertiesFile;
        if (!dbPropertiesFile.exists()) {
            throw new RuntimeException("Artifactory can't start without DB properties file! File not found at '" +
                    dbPropertiesFile.getAbsolutePath() + "'");
        }
        this.home = home;
        try {
            props = new LinkedProperties();
            try (FileInputStream pis = new FileInputStream(dbPropertiesFile)) {
                props.load(pis);
            }

            trimValues();
            assertMandatoryProperties();

            dbType = DbType.parse(getProperty(Key.type));
            if (StringUtils.isNotBlank(getProperty(Key.lockingDbType))) {
                lockingSpecificDbType = Optional.ofNullable(DbType.parse(getProperty(Key.lockingDbType)));
            } else {
                lockingSpecificDbType = Optional.empty();
            }

            // configure embedded derby
            if (dbType == DbType.DERBY) {
                System.setProperty("derby.stream.error.file",
                        new File(home.getLogDir(), "derby.log").getAbsolutePath());
                String url = getConnectionUrl();
                String dbHome = props.getProperty("db.home", home.getDataDir().getAbsolutePath() + "/derby");
                url = url.replace("{db.home}", dbHome);
                props.setProperty("db.home", dbHome);
                props.setProperty(Key.url.key, url);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load artifactory DB properties from '"
                    + dbPropertiesFile.getAbsolutePath() + "' due to :" + e.getMessage(), e);
        }
    }

    public File getDbPropertiesFile() {
        return dbPropertiesFile;
    }

    public String getUsername() {
        return props.getProperty(Key.username.key);
    }

    public String getLockingDbUsername() {
        return props.getProperty(Key.lockingDbUsername.key);
    }

    public String getSchema() {
        return props.getProperty(Key.schema.key);
    }

    public String getConnectionUrl() {
        return props.getProperty(Key.url.key);
    }

    public String getLockingDbConnectionUrl() {
        return props.getProperty(Key.lockingDbUrl.key);
    }

    public String getPassword() {
        return decryptPassInternal(Key.password);
    }

    public String getLockingDbPassword() {
        return decryptPassInternal(Key.lockingDbPassword);
    }

    private String decryptPassInternal(Key password) {
        String originalPass = getProperty(password);
        if (StringUtils.isBlank(originalPass)) {
            return originalPass;
        }
        // decrypt with master
        String decryptedPass = CryptoHelper.decryptWithMasterKeyIfNeeded(home, originalPass);
        if (decryptedPass.equals(originalPass)) {
            // if no decryption occurred, fallback and try with the Artifactory key just in case
            decryptedPass = CryptoHelper.decryptIfNeeded(home, decryptedPass);
            if (!decryptedPass.equals(originalPass)) {
                // if the password still encrypted with the Artifactory key file, we re-encrypt and save the file
                reEncryptAndSaveFile(decryptedPass);
            }
        }
        return decryptedPass;
    }

    private void reEncryptAndSaveFile(String decryptedPass) {
        try {
            String encryptedPasswordToSave = CryptoHelper.encryptWithMasterKeyIfNeeded(home, decryptedPass);
            setPassword(encryptedPasswordToSave);
            updateDbPropertiesFile(dbPropertiesFile);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed re-encrypting password in " + dbPropertiesFile.getAbsolutePath());
        }
    }

    public void setPassword(String updatedPassword) {
        props.setProperty(Key.password.key, updatedPassword);
    }

    public DbType getDbType() {
        return dbType;
    }

    public Optional<DbType> getLockingDbSpecificType() {
        return lockingSpecificDbType;
    }

    private void trimValues() {
        Iterator<Map.Entry<String, String>> iter = props.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String value = entry.getValue();
            if (!StringUtils.trimToEmpty(value).equals(value)) {
                entry.setValue(StringUtils.trim(value));
            }
        }
    }

    private void assertMandatoryProperties() {
        Key[] mandatory = {Key.type, Key.url, Key.driver};
        validateMandatoryParamValues(mandatory);

        assertLockingParamsIfNeeded();
    }

    private void assertLockingParamsIfNeeded() {
        if (StringUtils.isNotBlank(getProperty(Key.lockingDbType)) ||
                StringUtils.isNotBlank(getProperty(Key.lockingDbUrl)) ||
                StringUtils.isNotBlank(getProperty(Key.lockingDbDriver))) {
            Key[] mandatoryForLocking = {Key.lockingDbType, Key.lockingDbUrl, Key.lockingDbDriver};
            validateMandatoryParamValues(mandatoryForLocking);
        }
    }

    private void validateMandatoryParamValues(Key[] mandatoryForlocking) {
        for (Key mandatoryProperty : mandatoryForlocking) {
            String value = getProperty(mandatoryProperty);
            if (StringUtils.isBlank(value)) {
                throw new IllegalStateException("Mandatory storage property '" + mandatoryProperty.key() + "' doesn't exist");
            }
        }
    }

    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public String getProperty(Key property) {
        return props.getProperty(property.key, property.defaultValue);
    }

    public String getDriverClass() {
        return getProperty(Key.driver);
    }

    public String getLockingDbDriverClass() {
        return getProperty(Key.lockingDbDriver);
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(key, defaultValue + ""));
    }

    public int getIntProperty(String key, int defaultValue) {
        return Integer.parseInt(getProperty(key, defaultValue + ""));
    }

    public int getMaxActiveConnections() {
        return getIntProperty(Key.maxActiveConnections.key, DEFAULT_MAX_ACTIVE_CONNECTIONS);
    }

    public int getMaxIdleConnections() {
        return getIntProperty(Key.maxIdleConnections.key, DEFAULT_MAX_IDLE_CONNECTIONS);
    }

    public long getLongProperty(String key, long defaultValue) {
        return Long.parseLong(getProperty(key, defaultValue + ""));
    }

    public boolean isPostgres() {
        return dbType == DbType.POSTGRESQL;
    }

    /**
     * update storage properties file;
     */
    public void updateDbPropertiesFile(File updateStoragePropFile) throws IOException {
        if (props != null) {
            try (OutputStream outputStream = new FileOutputStream(updateStoragePropFile)) {
                props.store(outputStream, "");
            }
        }
    }

    public enum Key {
        username, password, type, url, driver, schema,
        maxActiveConnections("pool.max.active", null),
        maxIdleConnections("pool.max.idle", null),
        poolType("pool.type", "tomcat-jdbc"),
        lockingDbUsername("lockingdb.username", null),
        lockingDbPassword("lockingdb.password", null),
        lockingDbType("lockingdb.type", null),
        lockingDbUrl("lockingdb.url", null),
        lockingDbDriver("lockingdb.driver", null);

        private final String key;
        private final String defaultValue;

        Key() {
            this.key = name();
            this.defaultValue = null;
        }

        Key(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String key() {
            return key;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

    }
}

