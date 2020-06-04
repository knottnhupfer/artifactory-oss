package org.artifactory.storage.db.replication.errors.dao;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.replication.errors.entity.ReplicationErrorRecord;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.replication.errors.InternalReplicationEventType;
import org.jfrog.storage.util.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A data access object for the replication_errors table.
 *
 * @author Shay Bagants
 */
@Repository
public class ReplicationErrorsDao extends BaseDao {
    private static final String TABLE_NAME = "replication_errors";

    @Autowired
    public ReplicationErrorsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    /**
     * Add a new error to the db
     *
     * @param record - the error to add to the db
     * @throws SQLException
     */
    public void create(ReplicationErrorRecord record) throws SQLException {
        jdbcHelper.executeUpdate("INSERT INTO " + TABLE_NAME + " " + "(error_id, first_error_time, " +
                        "last_error_time, error_count, error_message, replication_key, task_time, task_type, " +
                        "task_path) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                record.getErrorId(), record.getFirstErrorTime(), record.getLastErrorTime(), record.getErrorCount(),
                record.getErrorMessage(), record.getReplicationKey(), record.getTaskTime(), record.getTaskType().code(),
                record.getTaskPath());
    }

    /**
     * Retrieve a list of all errors sorted primarily by their initial creation time and secondly by error id
     *
     * @return list of error records
     *
     * @throws SQLException
     */
    public List<ReplicationErrorRecord> getAllErrors() throws SQLException {
        ResultSet rs = null;
        try {
            List<ReplicationErrorRecord> entries = Lists.newArrayList();
            rs = jdbcHelper.executeSelect("SELECT * FROM " + TABLE_NAME + " ORDER BY task_time ASC, error_id ASC");
            while (rs.next()) {
                entries.add(recordsFromResultSet(rs));
            }
            return entries;
        } finally {
            DbUtils.close(rs);
        }
    }

    /**
     * Retrieve a list of all errors sorted primarily by their initial creation time and secondly by error id
     *
     * @return list of error records
     *
     * @throws SQLException
     */
    public List<ReplicationErrorRecord> getErrorsByReplicationKey(String replicationKey) throws SQLException {
        ResultSet rs = null;
        try {
            List<ReplicationErrorRecord> entries = Lists.newArrayList();
            rs = jdbcHelper.executeSelect(
                    "SELECT * FROM " + TABLE_NAME + " WHERE replication_key = ? ORDER BY task_time ASC, error_id ASC",
                    replicationKey);
            while (rs.next()) {
                entries.add(recordsFromResultSet(rs));
            }
            return entries;
        } finally {
            DbUtils.close(rs);
        }
    }

    /**
     * Delete an error from the db
     *
     * @param taskType       - error type
     * @param taskPath       - path of the artifact that failed to replicate
     * @param replicationKey - key representing the source and target
     * @return true if the deletion succeeded and false otherwise
     *
     * @throws SQLException
     */
    public boolean delete(InternalReplicationEventType taskType, String taskPath, String replicationKey)
            throws SQLException {
        int errorTypeCode = taskType.code();
        int deleted = jdbcHelper.executeUpdate("DELETE FROM " + TABLE_NAME + " WHERE task_type = ? AND " +
                "task_path = ? AND replication_key = ?", errorTypeCode, taskPath, replicationKey);
        return deleted > 0;
    }

    /**
     * Delete an error from the db (tests and internal use only - error id should not be exposed)
     *
     * @param id - id of the error record to delete
     * @return true if the deletion succeeded and false otherwise
     *
     * @throws SQLException
     */
    public boolean delete(long id) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM " + TABLE_NAME + " WHERE error_id = ?", id);
        return deleted > 0;
    }

    /**
     * Delete all replication errors records that match a specific replication-key
     *
     * @param replicationKey - the replication key whose errors should be deleted
     * @return number of deleted rows
     */
    public int deleteAllByKey(String replicationKey) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM " + TABLE_NAME + " WHERE replication_key = ?", replicationKey);
    }

    /**
     * Delete all replication errors records from the given type that match a specific replication-key
     *
     * @param replicationKey - the replication key whose errors should be deleted
     * @param taskType       - the type of errors to delete
     * @return number of deleted rows
     */
    public int deleteAllByKeyAndType(String replicationKey, InternalReplicationEventType taskType) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM " + TABLE_NAME + " WHERE replication_key = ? AND task_type = ?",
                replicationKey, taskType.code());
    }

    /**
     * Retrieves the error record from the db with the given type, path and replication key
     *
     * @param taskType       - error type
     * @param taskPath       - error artifact path
     * @param replicationKey - error replication key
     * @return the error if one exists or null otherwise
     *
     * @throws SQLException
     */
    public ReplicationErrorRecord get(InternalReplicationEventType taskType, String taskPath, String replicationKey)
            throws SQLException {
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect(
                    "SELECT * FROM " + TABLE_NAME + " WHERE task_type = ? AND task_path = ? AND replication_key = ?",
                    taskType.code(), taskPath, replicationKey);
            if (rs.next()) {
                return recordsFromResultSet(rs);
            } else {
                return null;
            }
        } finally {
            DbUtils.close(rs);
        }
    }

    /**
     * Updates an existing error record with same errorRecord's path type and replication key
     *
     * @param errorRecord - the error record to update
     * @return the number of rows affected
     *
     * @throws SQLException
     */
    public int update(ReplicationErrorRecord errorRecord) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE " + TABLE_NAME + " SET " +
                        "error_count = ?, " +
                        "error_message = ?," +
                        "last_error_time = ? " +
                        " WHERE " +
                        "replication_key = ? AND " +
                        "task_type = ? AND " +
                        "task_path = ?",
                errorRecord.getErrorCount(), errorRecord.getErrorMessage(), errorRecord.getLastErrorTime(),
                errorRecord.getReplicationKey(), errorRecord.getTaskType().code(), errorRecord.getTaskPath());
    }

    private ReplicationErrorRecord recordsFromResultSet(ResultSet rs) throws SQLException {
        return ReplicationErrorRecord.builder()
                .errorId(rs.getLong("error_id"))
                .firstErrorTime(rs.getLong("first_error_time"))
                .lastErrorTime(rs.getLong("last_error_time"))
                .errorCount(rs.getInt("error_count"))
                .errorMessage(rs.getString("error_message"))
                .replicationKey(rs.getString("replication_key"))
                .taskTime(rs.getLong("task_time"))
                .taskType(InternalReplicationEventType.fromCode(rs.getInt("task_type")))
                .taskPath(rs.getString("task_path"))
                .build();
    }

}
