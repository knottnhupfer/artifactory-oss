package org.artifactory.storage.db;

import com.google.common.collect.ImmutableList;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.storage.db.properties.DbVersionInfo;
import org.artifactory.storage.db.properties.service.ArtifactoryDbPropertiesService;
import org.artifactory.storage.db.util.IdGenerator;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.storage.DbType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * @author Roman Gurevitch.
 */
@Test
public class DbServiceImplTest extends ArtifactoryHomeBoundTest {

    private DbServiceImpl dbService;

    @Mock
    private JdbcHelper jdbcHelper;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private DatabaseMetaData dbMetadata;
    @Mock
    private DataSource lockDatasource;
    @Mock
    private Connection lockDbConnection;
    @Mock
    private DatabaseMetaData lockConnectionMetadata;
    @Mock
    private ArtifactoryDbProperties dbProperties;
    @Mock
    private ArtifactoryDbPropertiesService dbPropertiesService;
    @Mock
    private IdGenerator idGenerator;

    // the ddl are here to ensure that if the file we are using is changed (conversion ddl), we will not accidentally miss it
    private final List<String> ORACLE_DISTRIBUTED_LOCK_INDEXES = ImmutableList
            .of("CREATE INDEX distributed_locks_owner ON distributed_locks (owner)",
                    "CREATE INDEX distributed_locks_owner_thread ON distributed_locks (owner_thread)");
    private final String ORACLE_DISTRIBUTED_LOCK_SQL = "CREATE TABLE distributed_locks (\n" +
            "  category       VARCHAR(64)    NOT NULL,\n" +
            "  lock_key       VARCHAR(255)   NOT NULL,\n" +
            "  owner          VARCHAR(64)    NOT NULL,\n" +
            "  owner_thread   NUMBER(19, 0)  NOT NULL,\n" +
            "  owner_thread_name     VARCHAR(64)   NOT NULL,\n" +
            "  acquire_time   NUMBER(19, 0)  NOT NULL,\n" +
            "  CONSTRAINT locks_pk PRIMARY KEY (category,lock_key)\n" +
            ")";

    private final List<String> MSSQL_DISTRIBUTED_LOCK_INDEXES = ImmutableList
            .of("CREATE INDEX distributed_locks_owner ON distributed_locks (owner)",
                    "CREATE INDEX distributed_locks_owner_thread ON distributed_locks (owner_thread)");
    private final String MSSQL_DISTRIBUTED_LOCK_SQL = "CREATE TABLE distributed_locks (\n" +
            "  category       VARCHAR(64)   NOT NULL,\n" +
            "  lock_key       VARCHAR(255)  NOT NULL,\n" +
            "  owner          VARCHAR(64)   NOT NULL,\n" +
            "  owner_thread   BIGINT        NOT NULL,\n" +
            "  owner_thread_name     VARCHAR(64)   NOT NULL,\n" +
            "  acquire_time   BIGINT        NOT NULL,\n" +
            "  CONSTRAINT locks_pk PRIMARY KEY (category,lock_key)\n" +
            ")";

    private final List<String> MYSQL_DISTRIBUTED_LOCK_INDEXES = ImmutableList
            .of("CREATE INDEX distributed_locks_owner ON distributed_locks (owner)",
                    "CREATE INDEX distributed_locks_owner_thread ON distributed_locks (owner_thread)");
    private final String MYSQL_DISTRIBUTED_LOCK_SQL = "CREATE TABLE distributed_locks (\n" +
            "  category       VARCHAR(64)   NOT NULL,\n" +
            "  lock_key       VARCHAR(255)      NOT NULL,\n" +
            "  owner          VARCHAR(64)   NOT NULL,\n" +
            "  owner_thread   BIGINT     NOT NULL,\n" +
            "  owner_thread_name     VARCHAR(64)   NOT NULL,\n" +
            "  acquire_time   BIGINT     NOT NULL,\n" +
            "  CONSTRAINT locks_pk PRIMARY KEY (category,lock_key)\n" +
            ")";

    private final List<String> PSQL_DISTRIBUTED_LOCK_INDEXES = ImmutableList
            .of("CREATE INDEX distributed_locks_owner ON distributed_locks (owner)",
                    "CREATE INDEX distributed_locks_owner_thread ON distributed_locks (owner_thread)");
    private final String PSQL_DISTRIBUTED_LOCK_SQL = "CREATE TABLE distributed_locks (\n" +
            "  category       VARCHAR(64)   NOT NULL,\n" +
            "  lock_key       VARCHAR(255)      NOT NULL,\n" +
            "  owner          VARCHAR(64)   NOT NULL,\n" +
            "  owner_thread   BIGINT     NOT NULL,\n" +
            "  owner_thread_name     VARCHAR(64)   NOT NULL,\n" +
            "  acquire_time   BIGINT     NOT NULL,\n" +
            "  CONSTRAINT locks_pk PRIMARY KEY (category,lock_key)\n" +
            ")";

    @BeforeMethod
    private void beforeClass() {
        MockitoAnnotations.initMocks(this);
        dbService = new DbServiceImpl();
        dbService.waitBetweenInitialConnectionRetry = 1;
        ReflectionTestUtils.setField(dbService, "jdbcHelper", jdbcHelper);
        ReflectionTestUtils.setField(dbService, "lockDatasource", lockDatasource);
        ReflectionTestUtils.setField(dbService, "dbProperties", dbProperties);
        ReflectionTestUtils.setField(dbService, "dbPropertiesService", dbPropertiesService);
        ReflectionTestUtils.setField(dbService, "idGenerator", idGenerator);
        ResultSet rsMock = mock(ResultSet.class);
        try {
            when(rsMock.next()).thenReturn(true);
            when(rsMock.getBoolean(1)).thenReturn(false);
            when(rsMock.getString(1)).thenReturn("Sensitive");
            when(jdbcHelper.executeSelect(anyString())).thenReturn(rsMock);
        } catch (SQLException e) {
            Assert.fail();
        }
    }

    public void testRetryConnection() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Testing retry connection"));
        when(jdbcHelper.getDataSource()).thenReturn(dataSource);
        ReflectionTestUtils.setField(dbService, "jdbcHelper", jdbcHelper);

        Assert.expectThrows(SQLException.class, dbService::initDb);
        verify(dataSource, times(3)).getConnection();
    }

    @Test(dataProvider = "provideDbType")
    public void testDifferentDbForLockingWhenTableExists(DbType dbType) throws Exception {
        testDifferentDbForLock(true, dbType);
        dbService.initDb();
    }

    @Test(dataProvider = "provideDbTypeWithSql")
    public void testDifferentDbForLockingWhenTableNotExists(DbType dbType, String expectedCreateTableSql,
            List<String> expectedIndexes) throws Exception {
        testDifferentDbForLock(false, dbType);
        Statement stmtMock = mock(Statement.class);
        when(lockDbConnection.createStatement()).thenReturn(stmtMock);
        dbService.initDb();
        verify(lockConnectionMetadata, times(1)).getTables(any(), any(), matches("distributed_locks"), any());
        verify(lockDatasource, times(1)).getConnection();
        verify(lockDbConnection, times(1)).createStatement();
        verify(stmtMock, times(1)).executeUpdate(expectedCreateTableSql);
        expectedIndexes.forEach(index -> {
            try {
                verify(stmtMock, times(1)).executeUpdate(index);
            } catch (SQLException e) {
                Assert.fail("Failed validating index creation.", e);
            }
        });
    }

    private void testDifferentDbForLock(boolean mockTableExists, DbType dbType) throws Exception {
        when(jdbcHelper.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(dbProperties.getDbType()).thenReturn(dbType);
        mockMetadata(dbMetadata, connection, true);
        when(dbPropertiesService.getDbVersionInfo()).thenReturn(
                new DbVersionInfo(111, "Z", 222, 333));

        // prepare external db lock props
        when(dbProperties.getLockingDbSpecificType()).thenReturn(Optional.of(dbType));
        when(lockDatasource.getConnection()).thenReturn(lockDbConnection);
        mockMetadata(lockConnectionMetadata, lockDbConnection, mockTableExists);
    }

    private void mockMetadata(DatabaseMetaData dbMetadata, Connection connection, boolean mockTableExists)
            throws SQLException {
        when(connection.getMetaData()).thenReturn(dbMetadata);
        when(dbMetadata.getDatabaseProductName()).thenReturn("test");
        when(dbMetadata.getDatabaseProductVersion()).thenReturn("test");
        when(dbMetadata.getDriverName()).thenReturn("test");
        when(dbMetadata.getDriverVersion()).thenReturn("test");
        when(dbMetadata.getURL()).thenReturn("url");
        when(dbMetadata.getUserName()).thenReturn("username");
        when(dbMetadata.getConnection()).thenReturn(connection);
        ResultSet resultSet = mock(ResultSet.class);
        when(dbMetadata.getTables(any(), any(), any(), any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(mockTableExists);
        if (mockTableExists) {
            when(resultSet.getString(any())).thenReturn("some table");
        }
    }

    @DataProvider
    private Object[][] provideDbTypeWithSql() {
        return new Object[][]{
                {DbType.ORACLE, ORACLE_DISTRIBUTED_LOCK_SQL, ORACLE_DISTRIBUTED_LOCK_INDEXES},
                {DbType.POSTGRESQL, PSQL_DISTRIBUTED_LOCK_SQL, PSQL_DISTRIBUTED_LOCK_INDEXES},
                {DbType.MSSQL, MSSQL_DISTRIBUTED_LOCK_SQL, MSSQL_DISTRIBUTED_LOCK_INDEXES},
                {DbType.MYSQL, MYSQL_DISTRIBUTED_LOCK_SQL, MYSQL_DISTRIBUTED_LOCK_INDEXES}
        };
    }

    @DataProvider
    private Object[][] provideDbType() {
        return new Object[][]{
                {DbType.ORACLE},
                {DbType.POSTGRESQL},
                {DbType.MSSQL},
                {DbType.MYSQL}
        };
    }
}