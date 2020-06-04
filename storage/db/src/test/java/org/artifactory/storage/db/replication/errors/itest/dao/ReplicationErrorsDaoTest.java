package org.artifactory.storage.db.replication.errors.itest.dao;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.replication.errors.dao.ReplicationErrorsDao;
import org.artifactory.storage.db.replication.errors.entity.ReplicationErrorRecord;
import org.artifactory.storage.replication.errors.InternalReplicationEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.storage.replication.errors.InternalReplicationEventType.*;
import static org.artifactory.util.IdUtils.createReplicationKey;
import static org.testng.Assert.*;

/**
 * @author Shay Bagants
 * @author Yoaz Menda
 */
@Test
public class ReplicationErrorsDaoTest extends DbBaseTest {

    private static final List<String> URLS = ImmutableList.of(
            "https://my.awesomesecondfrogger.site/artifactory/whoami",
            "https://my.secondfrogger.site/artifactory/whoareyou",
            "http://internal.artifactoryserver.com/artifactory/ext-release-local");
    private AtomicInteger idGenerator = new AtomicInteger(1);

    @Autowired
    private ReplicationErrorsDao replicationErrorsDao;

    @AfterMethod
    public void cleanup() throws SQLException {
        replicationErrorsDao.getAllErrors().forEach(error -> {
            try {
                replicationErrorsDao.delete(error.getErrorId());
            } catch (SQLException e) {
                fail("Failed while cleaning replication_errors table");
            }
        });
    }

    @Test(dataProvider = "errorRecords")
    public void create(long errorId, long taskTime, int errorCount, String errorMessage, String replicationKey,
            long firstErrorTime, long lastErrorTime, InternalReplicationEventType eventType, String eventPath)
            throws SQLException {
        ReplicationErrorRecord record = buildErrorRecord(errorId, taskTime, errorCount, errorMessage, replicationKey,
                firstErrorTime, lastErrorTime, eventType, eventPath);
        replicationErrorsDao.create(record);

        List<ReplicationErrorRecord> allErrors = replicationErrorsDao.getAllErrors();
        assertEquals(allErrors.size(), 1);
        assertEquals(allErrors.get(0), record);
    }

    @Test
    public void getAllErrors() throws SQLException {
        String replicationKey = createReplicationKey("my-local-repo", "http://localhost/artifactory/what");
        String replicationKey2 = createReplicationKey("my-second-repo", "http://localhost/artifactory/what");
        String replicationKey3 = createReplicationKey("my-third-repo", "http://localhost/artifactory/what");
        List<ReplicationErrorRecord> createdFirstErrors = createErrors(5, replicationKey, null);
        List<ReplicationErrorRecord> createdSecondErrors = createErrors(7, replicationKey2, null);
        List<ReplicationErrorRecord> createdThirdErrors = createErrors(13, replicationKey3, null);
        List<ReplicationErrorRecord> returnedErrors = replicationErrorsDao.getAllErrors();
        List<ReplicationErrorRecord> createdErrors = Stream
                .of(createdFirstErrors, createdSecondErrors, createdThirdErrors).flatMap(
                        Collection::stream).collect(Collectors.toList());

        //assert all exist
        assertEquals(new HashSet<>(returnedErrors), new HashSet<>(createdErrors), "records don't match");
        assertSortedErrorEvents(returnedErrors);
    }

    @Test
    public void getErrorsByReplicationKey() throws SQLException {
        String replicationKey = createReplicationKey("my-local-repo", "http://localhost/artifactory/what");
        String replicationKey2 = createReplicationKey("my-second-repo", "http://localhost/artifactory/what");
        String replicationKey3 = createReplicationKey("my-third-repo", "http://localhost/artifactory/what");
        List<ReplicationErrorRecord> createdFirstErrors = createErrors(5, replicationKey, null);
        List<ReplicationErrorRecord> createdSecondErrors = createErrors(7, replicationKey2, null);
        List<ReplicationErrorRecord> createdThirdErrors = createErrors(13, replicationKey3, null);

        List<ReplicationErrorRecord> returnedErrors = replicationErrorsDao.getErrorsByReplicationKey(replicationKey);
        //assert all exist
        assertEquals(new HashSet<>(returnedErrors), new HashSet<>(createdFirstErrors), "records don't match");
        //assert ordered result
        assertSortedErrorEvents(returnedErrors);

        returnedErrors = replicationErrorsDao.getErrorsByReplicationKey(replicationKey2);
        //assert all exist
        assertEquals(new HashSet<>(returnedErrors), new HashSet<>(createdSecondErrors), "records don't match");
        assertSortedErrorEvents(returnedErrors);

        returnedErrors = replicationErrorsDao.getErrorsByReplicationKey(replicationKey3);
        //assert all exist
        assertEquals(new HashSet<>(returnedErrors), new HashSet<>(createdThirdErrors), "records don't match");
        assertSortedErrorEvents(returnedErrors);
    }

    @Test(dataProvider = "errorRecords")
    public void getError(long errorId, long taskTime, int errorCount, String errorMessage, String replicationKey,
            long firstErrorTime, long lastErrorTime, InternalReplicationEventType eventType, String eventPath)
            throws SQLException {
        ReplicationErrorRecord record = buildErrorRecord(errorId, taskTime, errorCount, errorMessage, replicationKey,
                firstErrorTime, lastErrorTime, eventType, eventPath);
        replicationErrorsDao.create(record);

        ReplicationErrorRecord errorRecord = replicationErrorsDao.get(eventType, eventPath, replicationKey);
        assertNotNull(errorRecord);
        assertEquals(errorRecord, record);
    }

    @Test
    public void getErrorNotExists() throws SQLException {
        ReplicationErrorRecord errorRecord = replicationErrorsDao.get(DEPLOY, "not-existing", "not-existing-key");
        assertNull(errorRecord);
    }

    @Test
    public void delete() throws SQLException {
        int recordsToCreate = values().length;
        String replicationKey = createReplicationKey("my-local-repo", "http://localhost/artifactory/what");
        createErrors(recordsToCreate, replicationKey, null);
        // ensure all records were created
        List<ReplicationErrorRecord> allErrors = replicationErrorsDao.getAllErrors();
        assertEquals(allErrors.size(), recordsToCreate);
        // delete specific record
        ReplicationErrorRecord firstRecord = allErrors.get(0);
        boolean deleted = replicationErrorsDao
                .delete(firstRecord.getTaskType(), firstRecord.getTaskPath(), firstRecord.getReplicationKey());
        assertTrue(deleted);

        // ensure service now return all records but first
        allErrors = replicationErrorsDao.getAllErrors();
        assertEquals(allErrors.size(), recordsToCreate - 1);
        assertFalse(allErrors.contains(firstRecord));
    }

    @Test
    public void deleteAllByKey() throws SQLException {
        String replicationKey1 = createReplicationKey("my-local-repo", "http://localhost/artifactory/what");
        createErrors(5, replicationKey1, null);
        String replicationKey2 = createReplicationKey("my-other-repo", "http://localhost/artifactory/why");
        createErrors(2, replicationKey2, null);
        String keyWithNoErrors = createReplicationKey("my-non-existing-repo", "http://localhost/artifactory/where");
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey(replicationKey1).size(), 5);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey(replicationKey2).size(), 2);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey(keyWithNoErrors).size(), 0);
        assertEquals(replicationErrorsDao.deleteAllByKey(keyWithNoErrors), 0);

        //key with errors
        assertEquals(replicationErrorsDao.deleteAllByKey(replicationKey1), 5);
        assertEquals(replicationErrorsDao.deleteAllByKey(replicationKey1), 0);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey(replicationKey1).size(), 0);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey(replicationKey2).size(), 2);
        assertEquals(replicationErrorsDao.deleteAllByKey(replicationKey2), 2);
        assertEquals(replicationErrorsDao.deleteAllByKey(replicationKey2), 0);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey(replicationKey2).size(), 0);
    }

    @Test
    public void testDeleteAllByKeyAndType() throws SQLException {
        createErrors(2, "replicationKey-1", DEPLOY);
        createErrors(2, "replicationKey-1", DELETE);
        createErrors(2, "replicationKey-1", PROPERTY_CHANGE);
        createErrors(2, "replicationKey-2", DEPLOY);
        createErrors(2, "replicationKey-2", DELETE);
        createErrors(2, "replicationKey-2", PROPERTY_CHANGE);
        assertEquals(replicationErrorsDao.deleteAllByKeyAndType("replicationKey-3", PROPERTY_CHANGE), 0);
        assertEquals(replicationErrorsDao.getAllErrors().size(), 12);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey("replicationKey-1").size(), 6);
        assertEquals(replicationErrorsDao.deleteAllByKeyAndType("replicationKey-1", DEPLOY), 2);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey("replicationKey-1").size(), 4);
        assertEquals(replicationErrorsDao.deleteAllByKeyAndType("replicationKey-1", DELETE), 2);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey("replicationKey-1").size(), 2);
        assertEquals(replicationErrorsDao.deleteAllByKeyAndType("replicationKey-1", PROPERTY_CHANGE), 2);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey("replicationKey-1").size(), 0);

        assertEquals(replicationErrorsDao.getErrorsByReplicationKey("replicationKey-2").size(), 6);
        assertEquals(replicationErrorsDao.deleteAllByKeyAndType("replicationKey-2", DEPLOY), 2);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey("replicationKey-2").size(), 4);
        assertEquals(replicationErrorsDao.deleteAllByKeyAndType("replicationKey-2", DELETE), 2);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey("replicationKey-2").size(), 2);
        assertEquals(replicationErrorsDao.deleteAllByKeyAndType("replicationKey-2", PROPERTY_CHANGE), 2);
        assertEquals(replicationErrorsDao.getErrorsByReplicationKey("replicationKey-2").size(), 0);

    }

    @Test
    public void update() throws SQLException {
        long creationTime = System.currentTimeMillis() - 5000;
        long firstErrorTime = creationTime + 5000;
        long previousRetryTime = System.currentTimeMillis() + 25000;
        String taskPath = "old-path";
        String errorMessage = "old-message";
        String replicationKey = "old-key";
        InternalReplicationEventType taskType = DEPLOY;
        ReplicationErrorRecord record = buildErrorRecord(1, creationTime, 2, errorMessage, replicationKey,
                firstErrorTime, previousRetryTime, taskType, taskPath);
        replicationErrorsDao.create(record);
        assertEquals(replicationErrorsDao.getAllErrors().size(), 1);

        ReplicationErrorRecord errorToUpdate = buildErrorRecord(5L, 0L, 777, "new-message", replicationKey,
                90, 99, taskType, taskPath);
        int updatedRows = replicationErrorsDao.update(errorToUpdate);
        assertEquals(updatedRows, 1);
        List<ReplicationErrorRecord> errorsInDb = replicationErrorsDao.getAllErrors();
        assertEquals(errorsInDb.size(), 1);
        ReplicationErrorRecord updated = errorsInDb.get(0);
        Collection<String> excludeFields = Stream.of("errorId", "taskTime", "firstErrorTime")
                .collect(Collectors.toSet());
        assertTrue(EqualsBuilder.reflectionEquals(errorToUpdate, updated, excludeFields));
        assertEquals(updated.getErrorId(), 1);
        assertEquals(updated.getTaskTime(), creationTime);
        // first error time should never be updated
        assertEquals(updated.getFirstErrorTime(), firstErrorTime);
    }

    @Test
    public void updateNotExist() throws SQLException {
        long creationTime = System.currentTimeMillis() - 5000;
        long firstErrorTime = creationTime + 5000;
        long previousRetryTime = System.currentTimeMillis() + 25000;
        String taskPath = "old-path";
        String errorMessage = "old-message";
        String replicationKey = "old-key";
        InternalReplicationEventType taskType = DEPLOY;
        ReplicationErrorRecord record = buildErrorRecord(1, creationTime, 2, errorMessage, replicationKey,
                firstErrorTime, previousRetryTime, taskType, taskPath);
        replicationErrorsDao.create(record);
        assertEquals(replicationErrorsDao.getAllErrors().size(), 1);

        ReplicationErrorRecord errorToUpdate = buildErrorRecord(5L, 0L, 777, "new-message", "another-key",
                80, 99, taskType, taskPath);
        int updatedRows = replicationErrorsDao.update(errorToUpdate);
        assertEquals(updatedRows, 0);
        List<ReplicationErrorRecord> errorsInDb = replicationErrorsDao.getAllErrors();
        assertEquals(errorsInDb.size(), 1);
        ReplicationErrorRecord updated = errorsInDb.get(0);
        assertEquals(updated, record);
    }

    @Test
    public void deleteById() throws SQLException {
        ReplicationErrorRecord record = buildErrorRecord(1, System.currentTimeMillis(), 1, "this is error",
                createReplicationKey("my-repo", "http://localhost/artifactory/what"),
                System.currentTimeMillis() - 100000, System.currentTimeMillis(),
                DEPLOY, "aaa");
        replicationErrorsDao.create(record);
        assertTrue(replicationErrorsDao.getAllErrors().contains(record));
        replicationErrorsDao.delete(record.getErrorId());
        assertFalse(replicationErrorsDao.getAllErrors().contains(record));
    }

    /**
     * creates new errors records and inserts them into the database
     *
     * @param numOfErrorsToCreate - number of records to create
     * @param replicationKey      - replication key of records to create
     * @param type                - the event type of replication errors to create (random if null was given)
     * @throws SQLException
     */
    private List<ReplicationErrorRecord> createErrors(int numOfErrorsToCreate, String replicationKey,
            InternalReplicationEventType type) throws SQLException {
        List<ReplicationErrorRecord> records = new ArrayList<>();
        long taskTime = 0;
        for (int i = 1; i <= numOfErrorsToCreate; i++) {
            if (i % 2 != 0) {
                taskTime = new Random().nextInt(444444 - 1 + 1) + 1;
            }
            InternalReplicationEventType taskType = type != null ? type : InternalReplicationEventType
                    .fromCode(1 + (i % InternalReplicationEventType.values().length));
            ReplicationErrorRecord record = buildErrorRecord(getId(), taskTime, 1,
                    "error-message",
                    replicationKey,
                    System.currentTimeMillis(),
                    System.currentTimeMillis() + 1,
                    taskType,
                    "aaa" + i
            );
            replicationErrorsDao.create(record);
            records.add(record);
        }
        return records;
    }

    private ReplicationErrorRecord buildErrorRecord(long errorId, long taskTime, int errorCount, String errorMessage,
            String replicationKey, long firstErrorTime, long lastErrorTime, InternalReplicationEventType taskType,
            String taskPath) {
        return ReplicationErrorRecord.builder()
                .errorId(errorId)
                .firstErrorTime(firstErrorTime)
                .lastErrorTime(lastErrorTime)
                .errorCount(errorCount)
                .replicationKey(replicationKey)
                .errorMessage(errorMessage)
                .taskTime(taskTime)
                .taskType(taskType)
                .taskPath(taskPath)
                .build();
    }

    private long getId() {
        return idGenerator.getAndIncrement();
    }

    @DataProvider
    public Object[][] errorRecords() {
        return new Object[][]{
                {1L, 1399174828000L, 0, "This is error!", createReplicationKey("test-local",
                        URLS.get(0)), 600000000000L, 647098148000L, DEPLOY, "test-local/file.jar"},
                {2L, 1399174828000L, 1, "err, exception.", createReplicationKey("test-local",
                        URLS.get(1)), 600000000000L, 647098148000L, MKDIR, "test-local/dir/"},
                {3L, 1399174828099L, 2, "xyz", createReplicationKey("test-local",
                        URLS.get(2)), 600000000000L, 647098148000L, DELETE, "test-local/file.jar"},
                {4L, 1399174828000L, 3, ".", createReplicationKey("test-local",
                        URLS.get(0)), 600000000000L, 647098148000L, PROPERTY_CHANGE, "test-local/file.jar"},
                {5L, 1399174828000L, 3, "something something happened", createReplicationKey("test-local",
                        URLS.get(2)), 600000000000L, 647098148000L, PROPERTY_CHANGE, "test-local/file.jar"},
                {5L, 1399174828000L, 3, "z", createReplicationKey("test-local",
                        URLS.get(1)), 600000000000L, 647098148000L, PROPERTY_CHANGE, "test-local/file.jar"}
        };
    }

    private void assertSortedErrorEvents(List<ReplicationErrorRecord> returnedErrors) {
        for (int i = 0; i < returnedErrors.size() - 1; i++) {
            ReplicationErrorRecord current = returnedErrors.get(i);
            ReplicationErrorRecord next = returnedErrors.get(i + 1);
            String errorMessage = "Events are not sorted correctly. Current item is: " + System.lineSeparator()
                    + current + System.lineSeparator() + "while next item is: " + System.lineSeparator() + next;
            assertTrue(current.getTaskTime() <= next.getTaskTime(), errorMessage);
            if (current.getTaskTime() == next.getTaskTime()) {
                assertTrue(current.getErrorId() < next.getErrorId(), errorMessage);
            }
        }
    }
}
