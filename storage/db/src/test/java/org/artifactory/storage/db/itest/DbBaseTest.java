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

package org.artifactory.storage.db.itest;

import ch.qos.logback.classic.util.ContextInitializer;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.DbServiceImpl;
import org.artifactory.storage.db.spring.ArtifactoryDataSource;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.TestUtils;
import org.jfrog.common.ResourceUtils;
import org.jfrog.config.ConfigurationManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.artifactory.storage.db.itest.DbTestUtils.refreshOrRecreateSchema;
import static org.artifactory.test.ChecksumTestUtils.randomHex;
import static org.jfrog.storage.util.DbStatementUtils.executeSqlStream;
import static org.jfrog.storage.util.DbUtils.doStreamWithConnection;
import static org.jfrog.storage.util.DbUtils.doWithConnection;
import static org.testng.Assert.assertEquals;

/**
 * Base class for the low level database integration tests.
 *
 * @author Yossi Shaul
 */
//@TestExecutionListeners(TransactionalTestExecutionListener.class)
//@Transactional
//@TransactionConfiguration(defaultRollback = false)
@Test(groups = "dbtest")
@ContextConfiguration(locations = {"classpath:spring/db-test-context.xml"})
public abstract class DbBaseTest extends AbstractTestNGSpringContextTests {

    static {
        // use the itest logback config
        URL url = DbBaseTest.class.getClassLoader().getResource("logback-dbtest.xml");
        if (url == null) {
            throw new RuntimeException("Could not find logback-dbtest.xml");
        }
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, url.getPath());
    }

    @Autowired
    protected JdbcHelper jdbcHelper;

    @Autowired
    protected DbServiceImpl dbService;

    @Autowired
    protected ArtifactoryDbProperties dbProperties;

    protected ArtifactoryHomeBoundTest artifactoryHomeBoundTest;
    private DummyArtifactoryContext dummyArtifactoryContext;
    protected ConfigurationManager configurationManager;

    @BeforeClass
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        artifactoryHomeBoundTest = createArtifactoryHomeTest();
        configurationManager = artifactoryHomeBoundTest.bindArtifactoryHome();
        super.springTestContextPrepareTestInstance();
        System.out.println("Setting up test class " + this.getClass().getName() + " for DB of type: " + dbProperties.getDbType());
        dummyArtifactoryContext = new DummyArtifactoryContext(applicationContext);
        doWithConnection(jdbcHelper, conn -> refreshOrRecreateSchema(LoggerFactory.getLogger(getClass()), conn, dbProperties.getDbType()));
        TestUtils.invokeMethodNoArgs(dbService, "initializeIdGenerator");
        // This is pretty much the same as having the db conversion callback run, since the dbService is
        // not inited across multiple runs of this test's inheritors i'm forced to do it like this.
        TestUtils.invokeMethodNoArgs(dbService, "verifySha256State");
    }

    @AfterClass
    public void verifyDbResourcesReleased() {
        // make sure there are no active connections
        ArtifactoryDataSource ds = (ArtifactoryDataSource) jdbcHelper.getDataSource();
        assertEquals(ds.getActiveConnectionsCount(), 0, "Found " + ds.getActiveConnectionsCount() +
                " active connections after test ended");
        artifactoryHomeBoundTest.unbindArtifactoryHome();
        // TODO: [by fsi] Derby DB cannot be shutdown in Suite since it uses the same DB for all tests
        //DbTestUtils.forceShutdownDerby(dbProperties.getProperty("db.home", null));
    }

    protected void addBean(Object bean, Class<?>... types) {
        dummyArtifactoryContext.addBean(bean, types);
    }

    protected ArtifactoryHomeBoundTest createArtifactoryHomeTest() throws IOException {
        return new ArtifactoryHomeBoundTest();
    }

    protected String randomSha1() {
        return randomHex(40);
    }

    protected String randomSha2() {
        return randomHex(64);
    }

    @BeforeMethod
    public void bindArtifactoryHome() {
        artifactoryHomeBoundTest.bindArtifactoryHome();
    }

    @AfterMethod
    public void unbindArtifactoryHome() {
        artifactoryHomeBoundTest.unbindArtifactoryHome();
    }

    @AfterClass
    public void cleanEnv() {
        artifactoryHomeBoundTest.cleanEnv();
    }

    protected void importSql(String resourcePath) {
        try (InputStream resource = ResourceUtils.getResource(resourcePath)) {
            doStreamWithConnection(jdbcHelper, conn -> executeSqlStream(conn, resource));
            // update the id generator
            TestUtils.invokeMethodNoArgs(dbService, "initializeIdGenerator");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeMethod
    public void bindDummyContext() {
        ArtifactoryContextThreadBinder.bind(dummyArtifactoryContext);
    }

    @AfterMethod
    public void unbindDummyContext() {
        ArtifactoryContextThreadBinder.unbind();
    }
}
