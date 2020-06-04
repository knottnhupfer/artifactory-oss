package org.artifactory.storage.db.binstore.service.garbage;

import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.GCCandidate;
import org.testng.annotations.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * @author Uriah Levy
 */
@Test
public class GCStreamingQueryHelperTest {

    public void testNextBatchMaxSize() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.next()).thenReturn(true, true, true, true, true, false);
        GCStreamingQueryHelper gcStreamingQueryHelper = new GCStreamingQueryHelper(5, () -> resultSet,
                (result) -> new GCCandidate(null, null, null, null, 0));

        List<GCCandidate> gcCandidates = gcStreamingQueryHelper.nextBatch();
        assertEquals(gcCandidates.size(), 5);

        gcStreamingQueryHelper.nextBatch();

        assertTrue(gcStreamingQueryHelper.isFinished());
    }

    public void testTrashItemsOnNonTrashRepo() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.next()).thenReturn(true, true, true, true, true, false);
        AtomicInteger counter = new AtomicInteger(1);
        GCStreamingQueryHelper gcStreamingQueryHelper = new GCStreamingQueryHelper(5, () -> resultSet,
                (result) -> {
            if (counter.get() == 5) {
                return new GCCandidate(RepoPathFactory.create(TrashService.TRASH_KEY, "trashfile"), null, null, null, 0);
            }
            counter.incrementAndGet();
            return new GCCandidate(RepoPathFactory.create("some-repo", "somefile"), null, null, null, 0);
        });

        List<GCCandidate> gcCandidates = gcStreamingQueryHelper.nextBatch();
        assertEquals(gcCandidates.size(), 1);

        gcStreamingQueryHelper.nextBatch();

        assertTrue(gcStreamingQueryHelper.isFinished());
    }

    public void testNextBatchBelowSize() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.next()).thenReturn(true, true, true, true, true, false);
        GCStreamingQueryHelper gcStreamingQueryHelper = new GCStreamingQueryHelper(3, () -> resultSet,
                (result) -> new GCCandidate(null, null, null, null, 0));

        List<GCCandidate> gcCandidates = gcStreamingQueryHelper.nextBatch();
        assertEquals(gcCandidates.size(), 3);
        assertFalse(gcStreamingQueryHelper.isFinished());

        gcStreamingQueryHelper.nextBatch();
        assertTrue(gcStreamingQueryHelper.isFinished());
    }
}