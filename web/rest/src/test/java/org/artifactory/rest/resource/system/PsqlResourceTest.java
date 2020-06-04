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

package org.artifactory.rest.resource.system;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.conversion.version.v213.V213ConversionFailFunction;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.storage.fs.service.PropertiesService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.storage.DbType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.*;

/**
 * @author Shay Bagants
 */
@Test
public class PsqlResourceTest extends ArtifactoryHomeBoundTest {

    private PsqlResource psqlResource;

    @Mock
    private PropertiesService propertiesService;
    @Mock
    private JdbcHelper jdbcHelper;
    @Mock
    private DataSource dataSource;
    @Mock
    private ConfigsService configsService;
    @Mock
    private ArtifactoryContext artifactoryContext;
    @Mock
    private ArtifactoryDbProperties dbProperties;
    @Mock
    private Connection connection;
    @Mock
    private Statement statement;


    @BeforeMethod
    private void beforeClass() throws SQLException {
        MockitoAnnotations.initMocks(this);
        when(artifactoryContext.beanForType(ArtifactoryDbProperties.class)).thenReturn(dbProperties);
        when(artifactoryContext.beanForType(JdbcHelper.class)).thenReturn(jdbcHelper);
        when(jdbcHelper.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test(dataProvider = "provideDbType")
    public void fixIndex(boolean isPsqlDb) {
        psqlResource = new PsqlResource(propertiesService, configsService);
        when(dbProperties.getDbType()).thenReturn(isPsqlDb ? DbType.POSTGRESQL : DbType.DERBY);
        when(configsService.hasConfig(V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER)).thenReturn(true);
        boolean fixIndex = psqlResource.fixIndex();
        Assert.assertEquals(fixIndex, isPsqlDb);

        VerificationMode times = times(isPsqlDb ? 1 : 0);
        verify(configsService, times).hasConfig(V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER);
        verify(configsService, times).deleteConfig(V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER);
    }

    @DataProvider
    public static Object[][] provideDbType() {
        return new Object[][]{
                {true},
                {false},
        };
    }
}