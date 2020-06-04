package org.artifactory.storage.db.event.itest.dao;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.artifactory.storage.db.event.dao.NodeEventsCursorDao;
import org.artifactory.storage.db.event.entity.DbNodeEventCursor;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.jfrog.common.ClockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

/**
 * @author Uriah Levy
 */
@Test
public class NodeEventsCursorDaoTest extends DbBaseTest {

    @Autowired
    private NodeEventsCursorDao dao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void allEvents() throws SQLException {
        assertEquals(dao.getAll().size(), 2);
    }

    public void cursorForOperationId() throws SQLException {
        Optional<DbNodeEventCursor> dbNodeEventCursor = dao.cursorForOperator("metadata-dispatcher");
        assertTrue(dbNodeEventCursor.isPresent());
        assertEquals(dbNodeEventCursor.get().getEventMarker(), 1515653250000L);
    }

    @Test(dependsOnMethods = "allEvents")
    public void createCursor() throws SQLException {
        String operatorId = "replication-ams-nj4";
        long now = ClockUtils.epochMillis();
        dao.insert(DbNodeEventCursor.builder()
                .operatorId(operatorId)
                .eventMarker(now)
                .type(NodeEventCursorType.METADATA_MIGRATION)
                .build());
        assertEquals(dao.getAll().size(), 3);
        Optional<DbNodeEventCursor> cursor = dao.cursorForOperator(operatorId);
        assertTrue(cursor.isPresent());
        assertEquals(cursor.get().getEventMarker(), now);
    }


    @Test(dependsOnMethods = {"createCursor"}, expectedExceptions = SQLException.class)
    public void createExistingCursor() throws SQLException {
        String operatorId = "replication-ams-nj4";
        final DbNodeEventCursor cursor = DbNodeEventCursor.builder()
                .operatorId(operatorId)
                .eventMarker(ClockUtils.epochMillis())
                .type(NodeEventCursorType.METADATA_MIGRATION)
                .build();
        // primary key violation
        dao.insert(cursor);
    }

    @Test(dependsOnMethods = {"createCursor"})
    public void oldestMarkerForTypes() throws SQLException {
        List<NodeEventCursorType> types = Arrays.asList(new NodeEventCursorType[]{NodeEventCursorType.INCREMENTAL_REPLICATION_PROGRESS, NodeEventCursorType.METADATA_MIGRATION});
        long marker = dao.oldestMarkerForTypes(types);
        assertEquals(marker, 1515653250000L);
    }

    @Test
    public void testDeleteById() throws SQLException {
        String operatorId = "some-operator-id";
        long now = ClockUtils.epochMillis();
        dao.insert(DbNodeEventCursor.builder()
                .operatorId(operatorId)
                .eventMarker(now)
                .type(NodeEventCursorType.METADATA_MIGRATION)
                .build());
        assertTrue(dao.cursorForOperator(operatorId).isPresent());
        dao.delete(operatorId);
        assertFalse(dao.cursorForOperator(operatorId).isPresent());
    }
}
