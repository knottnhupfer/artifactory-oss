package org.artifactory.storage.db.event.dao;

import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.event.entity.StoragePrioritizedNodeEvent;
import org.jfrog.storage.JdbcHelper;
import org.jfrog.storage.SqlDaoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * @author Uriah Levy
 */
@Repository
public class NodeEventPrioritiesDao {
    private static final String EVENT_PRIORITIES_TABLE = "node_event_priorities";
    private static final String OPERATOR_ID_COLUMN = "operator_id";
    private static final String PRIORITY_COLUMN = "priority";
    private static final String PRIORITY_ID_COLUMN = "priority_id";
    private static final String RETRY_COUNT_COLUMN = "retry_count";
    private static final String PATH_COLUMN = "path";
    private static final String TYPE_COLUMN = "type";
    private static final String TIMESTAMP_COLUMN = "timestamp";

    private final JdbcHelper jdbcHelper;
    private final SqlDaoHelper<StoragePrioritizedNodeEvent> daoHelper;
    private static final String INSERT_EVENT_PRIORITY_SQL =
            "INSERT INTO " + EVENT_PRIORITIES_TABLE +
                    " (priority_id, path, type, operator_id, priority, timestamp, retry_count) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String GET_EVENTS_BY_PRIORITY_AND_OPERATOR_SQL =
            "SELECT * FROM " + EVENT_PRIORITIES_TABLE + " WHERE " + PRIORITY_COLUMN + " = ? AND " + OPERATOR_ID_COLUMN +
                    " = ?";
    private static final String GET_MINIMAL_PRIORITY_BY_OPERATOR_ID_SQL =
            "SELECT MIN(" + PRIORITY_COLUMN + ") AS priority FROM " + EVENT_PRIORITIES_TABLE + " WHERE " +
                    OPERATOR_ID_COLUMN + " = ? AND retry_count < 5";
    private static final String COUNT_EVENT_BY_OPERATOR_ID_SQL =
            "SELECT COUNT(" + PRIORITY_COLUMN + ") AS count FROM " + EVENT_PRIORITIES_TABLE + " WHERE " +
                    OPERATOR_ID_COLUMN + " = ? " + "AND retry_count < 5";

    @Autowired
    public NodeEventPrioritiesDao(JdbcHelper jdbcHelper, ArtifactoryDbProperties dbProperties) {
        this.jdbcHelper = jdbcHelper;
        this.daoHelper = new SqlDaoHelper<>(jdbcHelper, dbProperties.getDbType(),
                EVENT_PRIORITIES_TABLE, this::getFromResultSet, 100);
    }

    public void insertEventPriority(@Nonnull StoragePrioritizedNodeEvent eventPriority) throws SQLException {
        jdbcHelper.executeUpdate(INSERT_EVENT_PRIORITY_SQL, eventPriority.getPriorityId(), eventPriority.getPath(),
                eventPriority.getType(),
                eventPriority.getOperatorId(), eventPriority.getPriority(), eventPriority.getTimestamp(),
                eventPriority.getRetryCount());
    }

    public void deleteEventPriorityById(long priorityId) throws SQLException {
        daoHelper.deleteByProperty(PRIORITY_ID_COLUMN, priorityId);
    }

    public Optional<StoragePrioritizedNodeEvent> findPriorityById(long priorityId) throws SQLException {
        return daoHelper.findFirstByProperty(PRIORITY_ID_COLUMN, priorityId);
    }

    public List<StoragePrioritizedNodeEvent> getEventsByPriorityAndOperatorId(int priority, String operatorId)
            throws SQLException {
        return daoHelper.findAllByQuery(GET_EVENTS_BY_PRIORITY_AND_OPERATOR_SQL, priority, operatorId);
    }

    public Optional<Integer> countRetryableEventsByOperatorId(String operatorId) throws SQLException {
        return daoHelper.findFirstValueByQuery(rs -> rs.getInt("count"), COUNT_EVENT_BY_OPERATOR_ID_SQL, operatorId);
    }

    public Optional<Integer> getMinimalPriorityByOperatorId(String operatorId) throws SQLException {
        return daoHelper
                .findFirstValueByQuery(rs -> rs.getInt(PRIORITY_COLUMN), GET_MINIMAL_PRIORITY_BY_OPERATOR_ID_SQL,
                        operatorId);
    }

    public void deleteAll() throws SQLException {
        daoHelper.deleteAll();
    }

    public List<StoragePrioritizedNodeEvent> getAll() throws SQLException {
        return daoHelper.getAll();
    }

    public void updateRetryCountById(long priorityId, int retryCount) throws SQLException {
        daoHelper.updateByProperty(Pair.of(PRIORITY_ID_COLUMN, priorityId), Pair.of(RETRY_COUNT_COLUMN, retryCount));
    }

    private StoragePrioritizedNodeEvent getFromResultSet(ResultSet resultSet) throws SQLException {
        return new StoragePrioritizedNodeEvent(resultSet.getLong(PRIORITY_ID_COLUMN),
                resultSet.getString(PATH_COLUMN),
                resultSet.getString(TYPE_COLUMN),
                resultSet.getString(OPERATOR_ID_COLUMN),
                resultSet.getInt(PRIORITY_COLUMN),
                resultSet.getLong(TIMESTAMP_COLUMN),
                resultSet.getInt(RETRY_COUNT_COLUMN));
    }
}
