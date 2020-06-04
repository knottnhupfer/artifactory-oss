package org.artifactory.storage.db.validators;


import org.artifactory.common.ConstantValues;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.test.ArtifactoryHomeBoundTest;

import org.jfrog.storage.DbType;
import org.testng.annotations.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test
public class DBCollationValidatorsTest extends ArtifactoryHomeBoundTest {

    @BeforeMethod
    void changeShutDownProp() {
        homeStub.setProperty(ConstantValues.shutDownOnInvalidDBScheme, "true");
    }

    @Test(expectedExceptions = CollationException.class)
    void testBadMysqlCollation() throws SQLException {
        DBSchemeCollationValidatorFactory
                .create(mockProps(DbType.MYSQL), mockCollation("some_collation_ci")).validate();
    }

    @Test
    void testGoodMysqlCollation() throws SQLException {
        DBSchemeCollationValidatorFactory
                .create(mockProps(DbType.MYSQL), mockCollation("some_collation_cs")).validate();
    }

    @Test
    void testGoodOracleCollation() throws SQLException {
        DBSchemeCollationValidatorFactory
                .create(mockProps(DbType.ORACLE), mockCollation("Sensitive")).validate();
    }

    @Test(expectedExceptions = CollationException.class)
    void testBadOracleCollation() throws SQLException {
        DBSchemeCollationValidatorFactory
                .create(mockProps(DbType.ORACLE), mockCollation("Insensitive")).validate();
    }

    @Test
    void testGoodMSSQLCollation() throws SQLException {
        DBSchemeCollationValidatorFactory
                .create(mockMssqlProps(), mockCollation("SOME_COLLATION_BIN")).validate();
    }

    @Test(expectedExceptions = CollationException.class)
    void testBadMSSQLCollation() throws SQLException {
        DBSchemeCollationValidatorFactory
                .create(mockMssqlProps(), mockCollation("SOME_COLLATION")).validate();
    }

    @Test
    void testGoodDerbyCollation() throws SQLException {
        DBSchemeCollationValidatorFactory
                .create(mockDerbyProps(true), mockCollation("SOME_COLLATION")).validate();
    }

    @Test(expectedExceptions = CollationException.class)
    void testBadDerbyCollation() throws SQLException {
        DBSchemeCollationValidatorFactory
                .create(mockDerbyProps(false), mockCollation("SOME_COLLATION")).validate();
    }

    private JdbcHelper mockCollation(String collation) throws SQLException {
        JdbcHelper jdbcMock = mock(JdbcHelper.class);
        ResultSet rsMock = mock(ResultSet.class);
        when(rsMock.getString(1)).thenReturn(collation);
        when(jdbcMock.executeSelect(anyString())).thenReturn(rsMock);
        when(jdbcMock.executeSelect(anyString(), anyString())).thenReturn(rsMock);
        return jdbcMock;
    }

    private ArtifactoryDbProperties mockProps(DbType type) {
        ArtifactoryDbProperties mock = mock(ArtifactoryDbProperties.class);
        when(mock.getDbType()).thenReturn(type);
        return mock;
    }

    private ArtifactoryDbProperties mockDerbyProps(boolean good) {
        ArtifactoryDbProperties mock = mock(ArtifactoryDbProperties.class);
        when(mock.getDbType()).thenReturn(DbType.DERBY);
        if (good) {
            when(mock.getConnectionUrl())
                    .thenReturn("dbc:derby:abcDB;create=true;territory=es_MX;collation=UCS_BASIC");
        } else {
            when(mock.getConnectionUrl())
                    .thenReturn("dbc:derby:abcDB;create=true;territory=es_MX;collation=BAD_COLLATION");
        }
        return mock;
    }

    private ArtifactoryDbProperties mockMssqlProps() {
        ArtifactoryDbProperties mock = mock(ArtifactoryDbProperties.class);
        when(mock.getDbType()).thenReturn(DbType.MSSQL);
        when(mock.getConnectionUrl()).thenReturn(
                "jdbc:sqlserver://localhost:1433;databaseName=artifactory;sendStringParametersAsUnicode=false;applicationName=Artifactory");
        return mock;
    }


}