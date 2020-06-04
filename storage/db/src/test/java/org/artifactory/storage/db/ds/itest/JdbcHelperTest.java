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

package org.artifactory.storage.db.ds.itest;

import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.test.TestUtils;
import org.jfrog.storage.DbType;
import org.jfrog.storage.util.DbUtils;
import org.jfrog.storage.wrapper.ResultSetWrapper;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;

/**
 * Tests the {@link JdbcHelper}.
 *
 * @author Yossi Shaul
 */
@Test
public class JdbcHelperTest extends DbBaseTest {

    public void testTxIsolationDefault() throws SQLException {
        ResultSet rs = jdbcHelper.executeSelect("select count(1) from nodes");
        Connection con = getConnection(rs);
        assertEquals(con.getTransactionIsolation(), Connection.TRANSACTION_READ_COMMITTED);
        DbUtils.close(rs);
    }

    public void testTxIsolationResetBackRoReadCommitted() throws SQLException {
        if (dbService.getDatabaseType().equals(DbType.ORACLE)) {
            return; // dirty reads are not supported by oracle driver
        }
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("select count(1) from nodes", true, new Object[]{});
            Connection con = getConnection(rs);
            assertEquals(con.getTransactionIsolation(), Connection.TRANSACTION_READ_UNCOMMITTED);
            // return it back to the pool and expect to get back the default TX isolation
            DbUtils.close(rs);
            rs = jdbcHelper.executeSelect("select count(1) from nodes");
            assertEquals(getConnection(rs).getTransactionIsolation(), Connection.TRANSACTION_READ_COMMITTED);
        } finally {
            DbUtils.close(rs);
        }
    }

    private Connection getConnection(ResultSet rs) {
        ResultSetWrapper rsw = (ResultSetWrapper) Proxy.getInvocationHandler(rs);
        return TestUtils.getField(rsw, "con", Connection.class);
    }

}
