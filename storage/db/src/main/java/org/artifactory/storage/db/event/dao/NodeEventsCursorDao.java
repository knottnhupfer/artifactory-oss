package org.artifactory.storage.db.event.dao;

import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.event.entity.DbNodeEventCursor;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.jfrog.storage.JdbcHelper;
import org.jfrog.storage.SqlDaoHelper;
import org.jfrog.storage.util.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
@Repository
public class NodeEventsCursorDao {
    private static final String EVENTS_CURSOR_TABLE = "node_event_cursor";

    private final JdbcHelper jdbcHelper;
    private final SqlDaoHelper<DbNodeEventCursor> daoHelper;
    private static final String INSERT_CURSOR_SQL =
            "INSERT INTO " + EVENTS_CURSOR_TABLE + " (operator_id, event_marker, type) VALUES(?, ?, ?)";
    private static final String UPDATE_CURSOR_SQL =
            "UPDATE " + EVENTS_CURSOR_TABLE + " SET event_marker = ? WHERE operator_id = ?";

    @Autowired
    public NodeEventsCursorDao(JdbcHelper jdbcHelper, ArtifactoryDbProperties dbProperties) {
        this.jdbcHelper = jdbcHelper;
        this.daoHelper = new SqlDaoHelper<>(jdbcHelper, dbProperties.getDbType(),
                EVENTS_CURSOR_TABLE, this::getFromResultSet, 100);
    }

    /**
     * Insert a new metadata cursor
     *
     * @param cursor the cursor to insert
     * @return the number of rows affected
     * @throws SQLException - SQL Exception thrown if the SQL operation failed
     */
    public int insert(@Nonnull DbNodeEventCursor cursor) throws SQLException {
        return jdbcHelper.executeUpdate(INSERT_CURSOR_SQL, cursor.getOperatorId(), cursor.getEventMarker(), cursor.getType().getName());
    }

    /**
     * Update an existing event cursor
     *
     * @param cursor the cursor to insert
     * @return the number of rows affected
     * @throws SQLException - SQL Exception thrown if the SQL operation failed
     */
    public int update(@Nonnull DbNodeEventCursor cursor) throws SQLException {
        return jdbcHelper.executeUpdate(UPDATE_CURSOR_SQL, cursor.getEventMarker(), cursor.getOperatorId());
    }

    public int update(@Nonnull String operatorId, long eventMarker) throws SQLException {
        return jdbcHelper.executeUpdate(UPDATE_CURSOR_SQL, eventMarker, operatorId);
    }

    public List<DbNodeEventCursor> getAll() throws SQLException {
        return daoHelper.getAll();
    }

    public Optional<DbNodeEventCursor> cursorForOperator(String operatorId) throws SQLException {
        return daoHelper.findFirstByQuery("SELECT * FROM " + EVENTS_CURSOR_TABLE + " WHERE operator_id = ?",
                operatorId);
    }

    public int deleteAll() throws SQLException {
        return daoHelper.deleteAll();
    }

    public long oldestMarkerForTypes(List<NodeEventCursorType> types) throws SQLException {
        ResultSet resultSet = null;
        try {
            List<String> typesNames = types.stream().map(NodeEventCursorType::getName).collect(Collectors.toList());
            resultSet = jdbcHelper.executeSelect("SELECT MIN(event_marker) FROM " + EVENTS_CURSOR_TABLE + " WHERE type in (#)", typesNames);
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public void delete(String operationId) throws SQLException {
        jdbcHelper.executeUpdate("DELETE FROM " + EVENTS_CURSOR_TABLE + " WHERE operator_id = ?", operationId);
    }

    private DbNodeEventCursor getFromResultSet(ResultSet resultSet) throws SQLException {
        return new DbNodeEventCursor(resultSet.getString("operator_id"),
                resultSet.getLong("event_marker"),
                NodeEventCursorType.forName(resultSet.getString("type")));
    }
}
