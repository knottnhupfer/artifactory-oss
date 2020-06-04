package org.artifactory.storage.db.event.service;

import org.artifactory.storage.db.event.dao.NodeEventsCursorDao;
import org.artifactory.storage.db.event.entity.DbNodeEventCursor;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.jfrog.common.ClockUtils;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Uriah Levy
 */
@Test
public class NodeEventCursorServiceImplTest {

    private static final String METADATA_OPERATOR_ID = "metadata-dispatcher";

    public void updateCursor() throws SQLException {
        NodeEventsCursorDao nodeEventsCursorDao = mock(NodeEventsCursorDao.class);
        NodeEventCursorServiceImpl service = new NodeEventCursorServiceImpl(nodeEventsCursorDao);
        long eventTimestamp = ClockUtils.epochMillis();

        service.updateOrInsertCursor(new NodeEventCursor(METADATA_OPERATOR_ID, eventTimestamp, NodeEventCursorType.METADATA_MIGRATION));
        DbNodeEventCursor eventCursor = DbNodeEventCursor.builder()
                .eventMarker(eventTimestamp)
                .operatorId(METADATA_OPERATOR_ID)
                .type(NodeEventCursorType.METADATA_MIGRATION)
                .build();

        verify(nodeEventsCursorDao).update(eventCursor);
    }

    public void cursorForOperatorId() throws SQLException {
        NodeEventsCursorDao nodeEventsCursorDao = mock(NodeEventsCursorDao.class);
        NodeEventCursorServiceImpl service = new NodeEventCursorServiceImpl(nodeEventsCursorDao);
        long eventTimestamp = ClockUtils.epochMillis();
        DbNodeEventCursor dbNodeEventCursor = DbNodeEventCursor.builder()
                .eventMarker(eventTimestamp)
                .operatorId(METADATA_OPERATOR_ID)
                .type(NodeEventCursorType.METADATA_MIGRATION)
                .build();

        when(nodeEventsCursorDao.cursorForOperator(METADATA_OPERATOR_ID)).thenReturn(Optional.of(dbNodeEventCursor));

        assertEquals(service.cursorForOperator(METADATA_OPERATOR_ID), new NodeEventCursor(METADATA_OPERATOR_ID, eventTimestamp, NodeEventCursorType.METADATA_MIGRATION));
    }

}