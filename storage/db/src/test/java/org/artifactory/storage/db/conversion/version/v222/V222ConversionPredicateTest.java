package org.artifactory.storage.db.conversion.version.v222;

import org.artifactory.storage.db.conversion.DbConversionException;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author AndreiK.
 */
public class V222ConversionPredicateTest {

    @Test(dataProvider = "resultsProvider")
    public void V222ConversionPredicate32ColumnSizeTest(int expectedColumnSize, boolean shouldConvert)
            throws SQLException {
        V222ConversionPredicate v222ConversionPredicate = new V222ConversionPredicate();
        JdbcHelper jdbcHelperMock = getJdbcHelperMock(true, expectedColumnSize);
        assertEquals(v222ConversionPredicate.condition().test(jdbcHelperMock, DbType.DERBY), shouldConvert);
    }

    @Test(expectedExceptions = DbConversionException.class, expectedExceptionsMessageRegExp = "Cannot run conversion 'v222' - Failed to retrieve schema metadata.*")
    public void V222ConversionPredicateSqlExceptionTest() throws SQLException {
        V222ConversionPredicate v222ConversionPredicate = new V222ConversionPredicate();
        JdbcHelper jdbcHelperMock = getJdbcHelperMock(false, 30);
        v222ConversionPredicate.condition().test(jdbcHelperMock, DbType.DERBY);
    }


    @DataProvider
    public static Object[][] resultsProvider() {
        return new Object[][] {
                {30, true},
                {33, true},
                {32, false}
        };
    }

    private JdbcHelper getJdbcHelperMock(boolean successfulColumnRetrieval, int expectedColumnSize)
            throws SQLException {
        DatabaseMetaData databaseMetaDataMock = mock(DatabaseMetaData.class);
        when(databaseMetaDataMock.getConnection()).thenReturn(mock(Connection.class));

        if (successfulColumnRetrieval) {
            ResultSet resultSetMock = mock(ResultSet.class);
            when(resultSetMock.next()).thenReturn(true);
            when(resultSetMock.getInt(any())).thenReturn(expectedColumnSize);
            when(databaseMetaDataMock.getColumns(any(), any(), any(), any())).thenReturn(resultSetMock);
        } else {
            when(databaseMetaDataMock.getColumns(any(), any(), any(), any())).thenThrow(SQLException.class);
        }

        Connection connectionMock = mock(Connection.class);
        when(connectionMock.getMetaData()).thenReturn(databaseMetaDataMock);

        DataSource dataSourceMock = mock(DataSource.class);
        when(dataSourceMock.getConnection()).thenReturn(connectionMock);

        JdbcHelper jdbcHelperMock = mock(JdbcHelper.class);
        when(jdbcHelperMock.getDataSource()).thenReturn(dataSourceMock);
        return jdbcHelperMock;
    }
}
