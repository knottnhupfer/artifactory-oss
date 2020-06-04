package org.artifactory.common.config.db;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.SystemUtils;
import org.artifactory.util.Files;
import org.jfrog.storage.DbType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.artifactory.common.config.db.ArtifactoryDbProperties.Key;
import static org.testng.Assert.*;

/**
 * @author Shay Bagants
 */
@Test
public class ArtifactoryDbPropertiesTest {

    private File tmpDir;

    @BeforeMethod
    private void setup() {
        tmpDir = Files.createRandomDir(SystemUtils.getJavaIoTmpDir(), this.getClass().getName());
    }

    @AfterMethod
    private void cleanup() {
        if (tmpDir != null && tmpDir.exists()) {
            tmpDir.delete();
        }
    }

    @Test(dataProvider = "partialDifferentLockingParams", expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*Mandatory storage property 'lockingdb.* doesn't exist")
    public void testDifferentDbForLockingNoMinParams(List<String> params) throws IOException {
        Properties prop = new Properties();
        setDbParams(prop);

        // set properties of different DB for locking
        params.forEach(param -> prop.setProperty(param, "xyz"));
        File dbPropertiesFile = storePropertiesFile(prop);
        new ArtifactoryDbProperties(null, dbPropertiesFile);
    }

    public void testDifferentDbForLocking() throws IOException {
        Properties prop = new Properties();
        setDbParams(prop);

        // set properties of different DB for locking
        prop.setProperty(Key.lockingDbType.key(), DbType.ORACLE.name());
        prop.setProperty(Key.lockingDbUrl.key(), "ext-url");
        prop.setProperty(Key.lockingDbUsername.key(), "ext-user");
        prop.setProperty(Key.lockingDbPassword.key(), "ext-pass");
        prop.setProperty(Key.lockingDbDriver.key(), "ext-driver");
        File dbPropertiesFile = storePropertiesFile(prop);
        ArtifactoryDbProperties artifactoryDbProperties = new ArtifactoryDbProperties(null, dbPropertiesFile);
        assertEquals(artifactoryDbProperties.getLockingDbSpecificType().get().name(), DbType.ORACLE.name());
        assertEquals(artifactoryDbProperties.getLockingDbConnectionUrl(), "ext-url");
        assertEquals(artifactoryDbProperties.getLockingDbUsername(), "ext-user");
        assertEquals(artifactoryDbProperties.getLockingDbPassword(), "ext-pass");
        assertEquals(artifactoryDbProperties.getLockingDbDriverClass(), "ext-driver");
        assertDbPropsParams(artifactoryDbProperties);
    }

    public void testNoDifferentDbForLocking() throws IOException {
        Properties prop = new Properties();
        setDbParams(prop);
        File dbPropertiesFile = storePropertiesFile(prop);
        ArtifactoryDbProperties artifactoryDbProperties = new ArtifactoryDbProperties(null, dbPropertiesFile);
        assertFalse(artifactoryDbProperties.getLockingDbSpecificType().isPresent());
        assertNull(artifactoryDbProperties.getLockingDbConnectionUrl());
        assertNull(artifactoryDbProperties.getLockingDbPassword());
        assertNull(artifactoryDbProperties.getLockingDbUsername());
        assertNull(artifactoryDbProperties.getLockingDbDriverClass());
        assertDbPropsParams(artifactoryDbProperties);
    }

    @DataProvider()
    private Object[][] partialDifferentLockingParams() {
        return new Object[][]{
                {ImmutableList.of(Key.lockingDbType.key())},
                {ImmutableList.of(Key.lockingDbType.key(), Key.lockingDbDriver.key())},
                {ImmutableList.of(Key.lockingDbType.key(), Key.lockingDbUrl.key())},
                {ImmutableList.of(Key.lockingDbDriver.key())},
                {ImmutableList.of(Key.lockingDbDriver.key(), Key.lockingDbType.key())},
                {ImmutableList.of(Key.lockingDbDriver.key(), Key.lockingDbUrl.key())},
                {ImmutableList.of(Key.lockingDbUrl.key())},
                {ImmutableList.of(Key.lockingDbUrl.key(), Key.lockingDbDriver.key())},
                {ImmutableList.of(Key.lockingDbUrl.key(), Key.lockingDbType.key())}
        };
    }

    private void setDbParams(Properties prop) {
        prop.setProperty(Key.type.key(), DbType.MSSQL.name());
        prop.setProperty(Key.username.key(), "someuser");
        prop.setProperty(Key.password.key(), "somepass");
        prop.setProperty(Key.url.key(), "someurl");
        prop.setProperty(Key.driver.key(), "mssqldriver");
        prop.setProperty(Key.schema.key(), "mssqlschema");
        prop.setProperty(Key.maxActiveConnections.key(), "300");
        prop.setProperty(Key.maxIdleConnections.key(), "299");
    }

    private void assertDbPropsParams(ArtifactoryDbProperties dbProperties) {
        assertEquals(dbProperties.getDbType().name(), DbType.MSSQL.name());
        assertEquals(dbProperties.getUsername(), "someuser");
        assertEquals(dbProperties.getPassword(), "somepass");
        assertEquals(dbProperties.getConnectionUrl(), "someurl");
        assertEquals(dbProperties.getDriverClass(), "mssqldriver");
        assertEquals(dbProperties.getSchema(), "mssqlschema");
        assertEquals(dbProperties.getMaxActiveConnections(), 300);
        assertEquals(dbProperties.getMaxIdleConnections(), 299);

    }

    private File storePropertiesFile(Properties prop) throws IOException {
        File dbPropertiesFile = new File(tmpDir, "db.properties");
        try (FileOutputStream fos = new FileOutputStream(dbPropertiesFile)) {
            prop.store(fos, null);
        }
        return dbPropertiesFile;
    }
}