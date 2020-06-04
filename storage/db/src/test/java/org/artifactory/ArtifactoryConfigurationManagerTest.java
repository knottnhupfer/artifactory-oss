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

//package org.artifactory;
//
//import org.apache.commons.io.FileUtils;
//import org.artifactory.common.ArtifactoryConfigurationAdapter;
//import org.artifactory.common.ArtifactoryHome;
//import org.artifactory.storage.db.itest.DbBaseTest;
//import org.jfrog.config.ConfigurationManager;
//import org.jfrog.config.wrappers.ConfigurationManagerImpl;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.sql.SQLException;
//
//import static org.artifactory.ConfigurationManagerTestHelper.*;
//
///**
// * @author gidis
// */
////TODO [by shayb]: find the problem on this test and fix it
//@Test()
//public class ArtifactoryConfigurationManagerTest extends DbBaseTest {
//
//    @Test
//    public void firstProStartUpTableNotExist() throws IOException, SQLException {
//        ArtifactoryHome home = createProEnvironmentWithoutDb();
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(adapter);
//        configurationManager.startSync();
//        assertDefaultConfigsReady(home);
//    }
//
//    @Test()
//    public void firstProStartWithProvidedDbProperties() throws IOException, SQLException {
//        ArtifactoryHome home = createProEnvironmentWithoutDb();
//        createDBPropertiesFile(home);
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManagerImpl.create(adapter);
//        home.initDBProperties();
//        assertDefaultConfigsReady(home);
//        assertProvidedDBPropertiesExist(home);
//    }
//
//    @Test()
//    public void firstProStartWithProvidedBinaryStoreXml() throws IOException, SQLException {
//        ArtifactoryHome home = createProEnvironmentWithoutDb();
//        createBinaryStoreXmlFile(home);
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManagerImpl.create(adapter);
//        assertDefaultConfigsReady(home);
//        assertProvidedBinaryStoreXmlIsValid(home);
//    }
//
//    @Test()
//    public void encryptionConfigOnPro() throws IOException, SQLException {
//        ArtifactoryHome home = createProEnvironmentWithDb();
//        cleanDb(home);
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(adapter);
//        configurationManager.startSync();
//        createCommonicationKey(home);
//        home.unsetArtifactoryEncryptionWrapper();
//        createArtifactoryKey(home);
//        assertDefaultConfigsReady(home);
//        assertArtifactoryKeyExist(home, configurationManager);
//        ArtifactoryHome.unbind();
//    }
//
//    @Test()
//    public void configIsDownloadedLocalyOnPro() throws IOException, SQLException {
//        ArtifactoryHome home = createProEnvironmentWithDb();
//        cleanDb(home);
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(adapter);
//        configurationManager.startSync();
//        createCommonicationKey(home);
//        createMasterKey(home);
//        home.unsetArtifactoryEncryptionWrapper();
//        createArtifactoryKey(home);
//        assertDefaultConfigsReady(home);
//        assertArtifactoryKeyExist(home, configurationManager);
//        home.getArtifactoryKey().delete();
//        Assert.assertFalse(home.getArtifactoryKey().exists());
//        configurationManager = ConfigurationManagerImpl.create(adapter);
//        configurationManager.startSync();
//        Assert.assertTrue(home.getArtifactoryKey().exists());
//        ArtifactoryHome.unbind();
//    }
//
//    @Test()
//    public void protectedOnPro() throws IOException, SQLException {
//        ArtifactoryHome home = createProEnvironmentWithDb();
//        cleanDb(home);
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(adapter);
//        configurationManager.startSync();
//        createCommonicationKey(home);
//        home.unsetArtifactoryEncryptionWrapper();
//        createArtifactoryKey(home);
//        // Keep original file;
//        String originalKey = FileUtils.readFileToString(home.getArtifactoryKey());
//        assertDefaultConfigsReady(home);
//        assertArtifactoryKeyExist(home, configurationManager);
//        String corruptedFile = "Some content";
//        FileUtils.writeStringToFile(home.getArtifactoryKey(), corruptedFile);
//        Assert.assertTrue(home.getArtifactoryKey().exists());
//        String dbContent = getArtifactoryKeyFromDb(home, configurationManager);
//        String fileContent = FileUtils.readFileToString(home.getArtifactoryKey());
//        Assert.assertNotEquals(dbContent, fileContent);
//        Assert.assertEquals(dbContent, originalKey);
//        ArtifactoryHome.unbind();
//    }
//
//    @Test
//    public void notEncryptionConfigOnPro() throws IOException, SQLException, InterruptedException {
//        ArtifactoryHome home = createProEnvironmentWithDb();
//        cleanDb(home);
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(adapter);
//        configurationManager.startSync();
//        createCommonicationKey(home);
//        home.unsetArtifactoryEncryptionWrapper();
//        // Keep original file;
//        String originalConfig = FileUtils.readFileToString(home.getArtifactorySystemPropertiesFile());
//        assertDefaultConfigsReady(home);
//        String corruptedFile = "Some content";
//        Thread.sleep(1000);
//        FileUtils.writeStringToFile(home.getArtifactorySystemPropertiesFile(), corruptedFile, false);
//        assertExpectedSystemPropertiesExist(home, configurationManager, corruptedFile);
//        Assert.assertTrue(home.getArtifactorySystemPropertiesFile().exists());
//        String dbContent = getArtifactorySystemPropertiesFromDb(home, configurationManager);
//        String fileContent = FileUtils.readFileToString(home.getArtifactorySystemPropertiesFile());
//        Assert.assertEquals(dbContent, fileContent);
//        Assert.assertNotEquals(dbContent, originalConfig);
//        ArtifactoryHome.unbind();
//    }
//
//    @Test
//    public void startOnHaSlave() throws IOException, SQLException, InterruptedException {
//        ArtifactoryHome home = createHaEnvironmentWithDb(false);
//        cleanDb(home);
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(adapter);
//        configurationManager.startSync();
//        createCommonicationKey(home);
//        home.unsetArtifactoryEncryptionWrapper();
//        try {
//            getArtifactorySystemPropertiesFromDb(home, configurationManager);
//            Assert.fail();
//        }catch (Exception e){
//            Assert.assertTrue(true);
//        }
//        home.getArtifactoryHaNodePropertiesFile().delete();
//        ArtifactoryHome.unbind();
//    }
//
//    @Test
//    public void startOnHaPrimary() throws IOException, SQLException, InterruptedException {
//        ArtifactoryHome home = createHaEnvironmentWithDb(true);
//        cleanDb(home);
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(adapter);
//        configurationManager.startSync();
//        createCommonicationKey(home);
//        home.unsetArtifactoryEncryptionWrapper();
//        try {
//            getArtifactorySystemPropertiesFromDb(home, configurationManager);
//        }catch (Exception e){
//            Assert.fail();
//        }
//        home.getArtifactoryHaNodePropertiesFile().delete();
//        ArtifactoryHome.unbind();
//    }
//
//    @Test
//    public void configurationChangeOnSlave() throws IOException, SQLException, InterruptedException {
//        ArtifactoryHome home = createHaEnvironmentWithDb(false);
//        cleanDb(home);
//        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
//        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(adapter);
//        configurationManager.startSync();
//        createCommonicationKey(home);
//        home.unsetArtifactoryEncryptionWrapper();
//        try {
//            getArtifactorySystemPropertiesFromDb(home, configurationManager);
//            Assert.fail();
//        }catch (Exception e){
//            Assert.assertTrue(true);
//        }
//        String corruptedFile = "Some content";
//        FileUtils.writeStringToFile(home.getArtifactorySystemPropertiesFile(), corruptedFile);
//        Assert.assertTrue(home.getArtifactorySystemPropertiesFile().exists());
//        Thread.sleep(10*1000);
//        try {
//            String content = getArtifactorySystemPropertiesFromDb(home, configurationManager);
//            Assert.assertEquals(content,corruptedFile);
//        }catch (Exception e){
//            Assert.fail();
//        }
//        home.getArtifactoryHaNodePropertiesFile().delete();
//        ArtifactoryHome.unbind();
//    }
//
////    @Test
////    public void configurationChangeOnPrimary() throws IOException, SQLException, InterruptedException {
////        ArtifactoryHome home = createHaEnvironmentWithDb(true);
////        cleanDb(home);
////        ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(home);
////        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(adapter);
////        configurationManager.startSync();
////        home.unsetArtifactoryEncryptionWrapper();
////        try {
////            getArtifactorySystemPropertiesFromDb(home, configurationManager);
////        }catch (Exception e){
////            Assert.fail();
////        }
////        Thread.sleep(1000);
////        String corruptedFile = "Some content";
////        FileUtils.writeStringToFile(home.getArtifactorySystemPropertiesFile(), corruptedFile);
////        Assert.assertTrue(home.getArtifactorySystemPropertiesFile().exists());
////        assertArtifactorySystemPropertiesExists(home,configurationManager,"Some content");
////        home.getArtifactoryHaNodePropertiesFile().delete();
////        ArtifactoryHome.unbind();
////    }
//
//
//}
