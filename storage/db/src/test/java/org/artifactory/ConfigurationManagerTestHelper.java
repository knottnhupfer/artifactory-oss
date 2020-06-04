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
package org.artifactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.ha.HaNodeProperties;
import org.jfrog.config.ConfigurationManager;
import org.jfrog.config.db.CommonDbProperties;
import org.jfrog.config.db.ConfigWithTimestamp;
import org.jfrog.config.db.TemporaryDBChannel;
import org.jfrog.config.wrappers.ConfigurationManagerImpl;
import org.jfrog.security.file.SecurityFolderHelper;
import org.jfrog.storage.DbType;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author gidis
 */
public class ConfigurationManagerTestHelper {


    static ArtifactoryHome createProEnvironmentWithDb() throws SQLException, IOException {
        ArtifactoryHome home = ArtifactoryHome.get();
        cleanHome(home);
        initPro(home);
        home.initArtifactorySystemProperties();
        ArtifactoryHome.bind(home);
        return home;
    }

    static ArtifactoryHome createHaEnvironmentWithDb(boolean primary) throws SQLException, IOException {
        ArtifactoryHome home = ArtifactoryHome.get();
        cleanHome(home);
        initHa(home,primary);
        home.initArtifactorySystemProperties();
        ArtifactoryHome.bind(home);
        return home;
    }

    static ArtifactoryHome createProEnvironmentWithoutDb() throws SQLException, IOException {
        ArtifactoryHome home = new ArtifactoryHome(new File("Configuration"));
        cleanHome(home);
        initPro(home);
        assertDefaultFilesNotExist(home);
        return home;
    }

    static ArtifactoryHome createHAEnvironmentWithoutDb(boolean primary) throws SQLException, IOException {
        ArtifactoryHome home = new ArtifactoryHome(new File("Configuration"));
        cleanHome(home);
        initHa(home,primary);
        assertDefaultFilesNotExist(home);
        return home;
    }

    static void assertDefaultConfigsReady(ArtifactoryHome home) {
        Assert.assertTrue(home.getDBPropertiesFile().exists());
        Assert.assertTrue(home.getLogbackConfig().exists());
        Assert.assertTrue(home.getMimeTypesFile().exists());
        Assert.assertTrue(home.getBinaryStoreXmlFile().exists());
        Assert.assertTrue(home.getArtifactorySystemPropertiesFile().exists());
    }

    static void assertDefaultFilesNotExist(ArtifactoryHome home) {
        Assert.assertFalse(home.getLogbackConfig().exists());
        Assert.assertFalse(home.getMimeTypesFile().exists());
        Assert.assertFalse(home.getBinaryStoreXmlFile().exists());
        Assert.assertFalse(home.getArtifactorySystemPropertiesFile().exists());
    }

    static void cleanHome(ArtifactoryHome home) throws SQLException {
        home.getCommunicationKeyFile().delete();
        home.getArtifactoryKey().delete();
        home.getLicenseFile().delete();
        home.getLogbackConfig().delete();
        home.getMimeTypesFile().delete();
        home.getBinaryStoreXmlFile().delete();
        home.getArtifactorySystemPropertiesFile().delete();
        home.getArtifactoryHaNodePropertiesFile().delete();
    }

    static void cleanDb(ArtifactoryHome home) throws SQLException {
        ArtifactoryDbProperties dbProperties = home.initDBProperties();
        TemporaryDBChannel chanel = new TemporaryDBChannel(new CommonDbProperties(dbProperties.getPassword(),
                dbProperties.getConnectionUrl(), dbProperties.getUsername(), dbProperties.getDbType(),
                dbProperties.getDriverClass()));
        Connection connection = chanel.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("delete from configs");
        preparedStatement.executeUpdate();
        connection.commit();
        connection.close();
    }

    static void createDBPropertiesFile(ArtifactoryHome home) throws IOException {
        FileUtils.writeStringToFile(home.getDBPropertiesFile(),
                "type=MSSQL\n" +
                        "url=jdbc:derby:{db.home};create=true\n" +
                        "driver=org.apache.derby.jdbc.EmbeddedDriver");
    }

    static void createBinaryStoreXmlFile(ArtifactoryHome home) throws IOException {
        FileUtils.writeStringToFile(home.getBinaryStoreXmlFile(), "<config version=\"v1\">\n" +
                "    <chain>\n" +
                "        <provider id=\"cache-fs\" type=\"cache-fs\">\n" +
                "            <test-mw>test-me</test-mw> " +
                "            <dynamic-provider id=\"file-system\" type=\"file-system\"/>\n" +
                "            <provider id=\"blob\" type=\"blob\"/>\n" +
                "        </provider>\n" +
                "    </chain>\n" +
                "\n" +
                "</config>");
    }

    static void assertProvidedDBPropertiesExist(ArtifactoryHome home) {
        Assert.assertTrue(home.getDBProperties().getDbType() == DbType.MSSQL);
    }

    static void assertProvidedBinaryStoreXmlIsValid(ArtifactoryHome home) throws IOException {
        Assert.assertTrue(FileUtils.readFileToString(home.getBinaryStoreXmlFile()).
                contains("<test-mw>test-me</test-mw>"));
    }

    static void createArtifactoryKey(ArtifactoryHome home) throws IOException {
        SecurityFolderHelper.createKeyFile(home.getArtifactoryKey());
    }

    static void createCommonicationKey(ArtifactoryHome home) throws IOException {
        SecurityFolderHelper.createKeyFile(home.getCommunicationKeyFile());
    }

    static void createMasterKey(ArtifactoryHome home) throws IOException {
        File masterKeyFile = home.getMasterKeyFile();
        Files.write(masterKeyFile.toPath(), "0c1a1554553d487466687b339cd85f3d".getBytes());
    }

    static String getArtifactoryKeyFromDb(ArtifactoryHome home, ConfigurationManager configurationManager) throws IOException {
        ConfigurationManagerImpl iml = (ConfigurationManagerImpl) configurationManager;
        ConfigWithTimestamp config = iml.getConfigsDao().getConfig("artifactory.security.artifactory.key",
                true, home);
        return IOUtils.toString(config.getBinaryStream());
    }

    static String getArtifactorySystemPropertiesFromDb(ArtifactoryHome home, ConfigurationManager configurationManager) throws IOException {
        ConfigurationManagerImpl iml = (ConfigurationManagerImpl) configurationManager;
        ConfigWithTimestamp config = iml.getConfigsDao().getConfig("artifactory.system.properties",
                true, home);
        return IOUtils.toString(config.getBinaryStream());
    }

    static void assertArtifactoryKeyExist(ArtifactoryHome home, ConfigurationManager configurationManager) throws IOException {
        Assert.assertTrue(home.getArtifactoryKey().exists());
        ConfigurationManagerImpl iml = (ConfigurationManagerImpl) configurationManager;
        long startTime = System.currentTimeMillis();
        boolean success = false;
        while (!success && startTime + 1000 * 30 > System.currentTimeMillis()) {
            Long configTimestamp = iml.getConfigsDao().getConfigTimestamp("artifactory.security.artifactory.key");
            success = configTimestamp != null;
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Assert.assertTrue(success);
        // Sending false in the encryption to get encypted file
        ConfigWithTimestamp encypted = iml.getConfigsDao().getConfig("artifactory.security.artifactory.key",
                false, home);
        ConfigWithTimestamp decypted = iml.getConfigsDao().getConfig("artifactory.security.artifactory.key",
                true, home);
        String decyptedText = IOUtils.toString(decypted.getBinaryStream());
        String encryptText = IOUtils.toString(encypted.getBinaryStream());
        String original = FileUtils.readFileToString(home.getArtifactoryKey());
        Assert.assertTrue(original.equals(decyptedText));
        Assert.assertFalse(original.equals(encryptText));
    }

    static void assertArtifactorySystemPropertiesExists(ArtifactoryHome home, ConfigurationManager configurationManager,
                                                        String content) throws IOException {
        ConfigurationManagerImpl iml = (ConfigurationManagerImpl) configurationManager;
        long startTime = System.currentTimeMillis();
        boolean success = false;
        while (!success && startTime + 1000 * 30 > System.currentTimeMillis()) {
            ConfigWithTimestamp config = iml.getConfigsDao().getConfig("artifactory.system.properties",
                    false, home);
            String dbContent= IOUtils.toString(config.getBinaryStream());
            success = content.contains(dbContent) || dbContent.contains(content);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Assert.assertTrue(success);
    }

    static void assertExpectedSystemPropertiesExist(ArtifactoryHome home, ConfigurationManager configurationManager,
                                                    String expected) throws IOException {
        Assert.assertTrue(home.getArtifactorySystemPropertiesFile().exists());
        ConfigurationManagerImpl iml = (ConfigurationManagerImpl) configurationManager;
        long startTime = System.currentTimeMillis();
        boolean success = false;
        while (!success && startTime + 1000 * 30 > System.currentTimeMillis()) {
            ConfigWithTimestamp config = iml.getConfigsDao().getConfig("artifactory.system.properties",
                    false, home);
            String dbContent = IOUtils.toString(config.getBinaryStream());
            success = dbContent.equals(expected);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Assert.assertTrue(success);
    }

    static void initHa(ArtifactoryHome home, Boolean primary) throws IOException {
        File haNodePropertiesFile = home.getArtifactoryHaNodePropertiesFile();
        if(haNodePropertiesFile.exists()){
            haNodePropertiesFile.delete();
        }
        Properties properties = new Properties();
        properties.put("primary", primary.toString());
        properties.put("context.url", "http://artifactory/test");
        properties.put("node.id", "test");
        FileOutputStream out = new FileOutputStream(haNodePropertiesFile);
        properties.store(out, "Now we have not primary HA member");
        out.flush();
        out.close();
        HaNodeProperties haNodeProperties = new HaNodeProperties();
        haNodeProperties.load(haNodePropertiesFile);
        ReflectionTestUtils.setField(home,"haNodeProperties",haNodeProperties);
    }
    static void initPro(ArtifactoryHome home) throws IOException {
        ReflectionTestUtils.setField(home,"haNodeProperties",null);
    }
}