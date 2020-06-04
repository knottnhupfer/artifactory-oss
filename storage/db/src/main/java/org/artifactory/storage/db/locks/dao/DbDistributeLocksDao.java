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

package org.artifactory.storage.db.locks.dao;

import com.google.common.collect.Sets;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.db.locks.LockInfo;
import org.artifactory.util.CollectionUtils;
import org.jfrog.storage.util.DbStatementUtils;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.*;
import java.util.List;
import java.util.Set;

import static org.jfrog.storage.util.DbStatementUtils.parseInListQuery;
import static org.jfrog.storage.util.DbStatementUtils.setParamsToStmt;

/**
 * @author gidis
 */
@Repository
public class DbDistributeLocksDao {
    private static final Logger log = LoggerFactory.getLogger(DbDistributeLocksDao.class);

    private final DataSource uniqueLocksDataSource;

    @Autowired
    public DbDistributeLocksDao(@Qualifier("uniqueLockDataSource") DataSource uniqueLocksDataSource) {
        this.uniqueLocksDataSource = uniqueLocksDataSource;
    }

    public boolean isLocked(String category, String key) throws SQLException {
        key = normalizeKey(key);
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement("select owner from distributed_locks where lock_key=? and category=?");
            stmt.setString(1, key);
            stmt.setString(2, category);
            resultSet = stmt.executeQuery();
            return resultSet.next();
        } finally {
            close(con, stmt, resultSet);
        }
    }

    public int lockingMapSize(String category) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement("select count(*) from distributed_locks where category=?");
            stmt.setString(1, category);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return 0;
            }
        } finally {
            close(con, stmt, resultSet);
        }
    }

    public Set<String> lockingMapKeySet(String category) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Set<String> set = Sets.newHashSet();
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement("select lock_key from distributed_locks where category=?");
            stmt.setString(1, category);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                set.add(resultSet.getString(1));
            }
        } finally {
            close(con, stmt, resultSet);
        }
        return set;
    }

    public boolean deleteLock(String category, String key, String owner) throws SQLException {
        key = normalizeKey(key);
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement("delete from distributed_locks where lock_key=? and category=? and owner=? " +
                    " and owner_thread=?");
            stmt.setString(1, key);
            stmt.setString(2, category);
            stmt.setString(3, owner);
            stmt.setLong(4, Thread.currentThread().getId());
            int effectedRows = stmt.executeUpdate();
            return effectedRows > 0;
        } finally {
            close(con, stmt);
        }
    }

    public int deleteAllOwnerLocks(String owner) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement("delete from distributed_locks where owner=?");
            stmt.setString(1, owner);
            return stmt.executeUpdate();
        } finally {
            close(con, stmt);
        }
    }

    public boolean tryToAcquireLock(LockInfo lockInfo) throws SQLException {
        String key = normalizeKey(lockInfo.getKey());
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement("insert into distributed_locks values(?,?,?,?,?,?)");
            stmt.setString(1, lockInfo.getCategory());
            stmt.setString(2, key);
            stmt.setString(3, lockInfo.getOwner());
            stmt.setLong(4, lockInfo.getThreadId());
            stmt.setString(5, lockInfo.getThreadName());
            stmt.setLong(6, lockInfo.getStartedTime());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to insert lock row to the 'distributed_locks' table: {}. {}", lockInfo,
                        e.getMessage());
                log.debug("Failed to insert lock.", e);
            }
            return false;
        } finally {
            close(con, stmt);
        }
    }

    public boolean releaseForceLock(String category, String key) throws SQLException {
        key = normalizeKey(key);
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement("delete from distributed_locks where lock_key=? and category=?");
            stmt.setString(1, key);
            stmt.setString(2, category);
            return stmt.executeUpdate() == 1;
        } finally {
            close(con, stmt);
        }
    }

    private static void close(@Nullable Connection con, @Nullable Statement stmt) {
        close(con, stmt, null);
    }

    private static void close(@Nullable Connection con, @Nullable Statement stmt, @Nullable ResultSet rs) {
        try {
            DbUtils.close(rs);
        } finally {
            try {
                DbStatementUtils.close(stmt);
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        log.trace("Could not close JDBC connection", e);
                    } catch (Exception e) {
                        log.trace("Unexpected exception when closing JDBC connection", e);
                    }
                }
            }
        }
    }

    private static String normalizeKey(String key) {
        try {
            if(StringUtils.isBlank(key)){
                key=".";
            }
            if (key.length() > 255) {
                final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
                messageDigest.reset();
                messageDigest.update(key.getBytes(Charset.forName("UTF8")));
                final byte[] resultByte = messageDigest.digest();
                return new String(Hex.encodeHex(resultByte));
            } else {
                return key;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to transform lock key into SHA-1", e);
        }
    }

    public Set<LockInfo> getAllLocksNotOwnedBy(List<String> serverIds) throws SQLException {
        Set<LockInfo> lockInfos = Sets.newHashSet();
        if (CollectionUtils.isNullOrEmpty(serverIds)){
            return lockInfos;
        }
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            con = uniqueLocksDataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(parseInListQuery("SELECT * FROM distributed_locks WHERE owner NOT IN(#)", serverIds));
            stmt = pstmt;
            setParamsToStmt(pstmt, serverIds.toArray());
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                lockInfos.add(toLockInfo(resultSet));
            }
        } finally {
            close(con, stmt, resultSet);
        }
        return lockInfos;
    }

    public Set<LockInfo> getAllCurrentServerLocks(String owner) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Set<LockInfo> set = Sets.newHashSet();
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement("select category, lock_key, owner_thread, owner_thread_name, acquire_time from distributed_locks where owner=?");
            stmt.setString(1, owner);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                set.add(new LockInfo(resultSet.getString(1), resultSet.getString(2), owner,
                        resultSet.getLong(3), resultSet.getString(4), resultSet.getLong(5)));
            }
        } finally {
            close(con, stmt, resultSet);
        }
        return set;
    }

    public Set<LockInfo> getExpiredLocks(long acquireTime) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Set<LockInfo> set = Sets.newHashSet();
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement("select category, lock_key, owner, owner_thread, owner_thread_name, acquire_time " +
                    "from distributed_locks where acquire_time < ?");
            stmt.setLong(1, acquireTime);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                set.add(toLockInfo(resultSet));
            }
        } finally {
            close(con, stmt, resultSet);
        }
        return set;
    }

    public LockInfo getLockInfo(String category, String key) throws SQLException {
        key = normalizeKey(key);
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            con = uniqueLocksDataSource.getConnection();
            stmt = con.prepareStatement(
                    "select category, lock_key, owner, owner_thread, owner_thread_name, acquire_time from distributed_locks where lock_key=? and category=?");
            stmt.setString(1, key);
            stmt.setString(2, category);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                return toLockInfo(resultSet);
            }
            return null;
        } finally {
            close(con, stmt, resultSet);
        }
    }

    private LockInfo toLockInfo(ResultSet resultSet) throws SQLException {
        return new LockInfo(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                resultSet.getLong(4), resultSet.getString(5), resultSet.getLong(6));
    }
}