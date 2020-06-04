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

package org.artifactory.storage.db.version.converter;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.storage.db.conversion.version.v213.V213ConversionFailFunction;
import org.artifactory.storage.db.conversion.version.v213.V213ConversionPredicate;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.test.ArtifactoryHomeStub;
import org.jfrog.storage.DbType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.artifactory.storage.db.conversion.version.v213.V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER;
import static org.artifactory.version.ArtifactoryVersionProvider.v633;
import static org.mockito.Mockito.*;

/**
 * @author Shay Bagants
 */
@Test
public class OptionalDBConverterTest {

    private OptionalDBConverter optionalDBConverter;

    @Mock
    private JdbcHelper jdbcHelper;
    @Mock
    private DataSource dataSource;
    @Mock
    private ConfigsService configsService;
    @Mock
    private ArtifactoryContext artifactoryContext;

    @BeforeMethod
    private void beforeClass() throws SQLException {
        initHome();
        MockitoAnnotations.initMocks(this);
        when(jdbcHelper.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("Could not get connection"));
        when(artifactoryContext.beanForType(ConfigsService.class)).thenReturn(configsService);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        V213ConversionFailFunction failFunction = new V213ConversionFailFunction();
        optionalDBConverter = new OptionalDBConverter(v633.get().getVersion(), new V213ConversionPredicate(), failFunction);
    }

    private void initHome() {
        ArtifactoryHome artifactoryHome = new ArtifactoryHomeStub();
        ArtifactoryHome.bind(artifactoryHome);
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
        ArtifactoryHome.unbind();
    }

    public void testConvert() {
        optionalDBConverter.convert(jdbcHelper, DbType.POSTGRESQL);
        when(configsService.hasConfig(PSQL_NODE_PROPS_INDEX_MISSING_MARKER)).thenReturn(false);
        // make sure that the fail function which tries to add config to the DB on failure is called
        verify(configsService, times(1))
                .addConfig(eq(PSQL_NODE_PROPS_INDEX_MISSING_MARKER), eq("."), anyLong());
    }
}