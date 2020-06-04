package org.artifactory.storage.db.event.itest.dao;

import org.artifactory.storage.db.event.dao.NodeEventPrioritiesDao;
import org.artifactory.storage.db.event.entity.StoragePrioritizedNodeEvent;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.jfrog.common.ClockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
@Test
public class NodeEventPrioritiesDaoTest extends DbBaseTest {
    @Autowired
    private NodeEventPrioritiesDao dao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void testGetEventByPriority() throws SQLException {
        assertEquals(dao.getEventsByPriorityAndOperatorId(2, "metadata-dispatcher").size(), 2);
        assertEquals(dao.getEventsByPriorityAndOperatorId(3, "metadata-dispatcher").size(), 2);
    }

    public void testInsertEventPriority() throws SQLException {
        dao.insertEventPriority(getNodeEventPriority(111222333444L));
        assertEquals(dao.getEventsByPriorityAndOperatorId(4, "metadata-dispatcher").size(), 1);
        dao.deleteEventPriorityById(111222333444L);
    }

    public void deleteEventPriorityById() throws SQLException {
        dao.insertEventPriority(getNodeEventPriority(111222333446L));
        dao.deleteEventPriorityById(111222333446L);
        assertFalse(dao.findPriorityById(111222333446L).isPresent());
    }

    @Test(expectedExceptions = SQLException.class)
    public void testPrimaryKeyConstraintViolated() throws SQLException {
        dao.insertEventPriority(getNodeEventPriority(111222333445L));
        dao.insertEventPriority(getNodeEventPriority(111222333445L));
    }

    public void testCountEventsByOperatorId() throws SQLException {
        assertTrue(dao.countRetryableEventsByOperatorId("metadata-dispatcher").isPresent());
        assertEquals(dao.countRetryableEventsByOperatorId("metadata-dispatcher").get().intValue(), 3);
    }

    public void testGetMinimalPriorityByOperatorId() throws SQLException {
        assertTrue(dao.getMinimalPriorityByOperatorId("metadata-dispatcher").isPresent());
        assertEquals(dao.getMinimalPriorityByOperatorId("metadata-dispatcher").get().intValue(), 2);
        StoragePrioritizedNodeEvent nodeEventPriority = getNodeEventPriority(111222333445L);
        nodeEventPriority.setPriority(1);
        nodeEventPriority.setPriorityId(1234567891L);
        dao.insertEventPriority(nodeEventPriority);
        assertEquals(dao.getMinimalPriorityByOperatorId("metadata-dispatcher").get().intValue(), 1);
        dao.deleteAll();
        // no values -> minimal priority is 1
        assertTrue(dao.getAll().isEmpty());
        assertEquals(dao.getMinimalPriorityByOperatorId("metadata-dispatcher").get().intValue(), 0);
    }

    public void testFindPriorityById() throws SQLException {
        dao.insertEventPriority(getNodeEventPriority(111222333446L));
        assertTrue(dao.findPriorityById(getNodeEventPriority(111222333446L).getPriorityId()).isPresent());
    }

    private StoragePrioritizedNodeEvent getNodeEventPriority(long priorityId) {
        return new StoragePrioritizedNodeEvent(priorityId, "a/b/c", "create", "metadata-dispatcher", 4,
                ClockUtils.epochMillis(), 0);
    }
}
