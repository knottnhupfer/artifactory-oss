package org.artifactory.storage.db.replication.errors.itest.service;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.addon.replication.event.ReplicationEventType;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.replication.errors.ReplicationErrorInfo;
import org.artifactory.storage.replication.errors.ReplicationErrorsStorageService;
import org.artifactory.util.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.addon.replication.event.ReplicationEventType.*;
import static org.artifactory.util.IdUtils.createReplicationKey;
import static org.testng.Assert.*;

/**
 * @author Shay Bagants
 */
@Test
public class ReplicationErrorsStorageServiceImplTest extends DbBaseTest {

    @Autowired
    private ReplicationErrorsStorageService replicationErrorsStorageService;

    private final static String REPLICATION_KEY = createReplicationKey("target-local",
            "http://localhost:8080/artifactory/ext-release-local");

    @AfterMethod
    private void cleanup() {
        replicationErrorsStorageService.getErrors().forEach(error -> replicationErrorsStorageService
                .delete(error.getTaskType(), error.getTaskPath(), error.getReplicationKey()));
    }

    @Test(dataProvider = "errorRecords")
    public void add(long eventTime, int errorCount, String errorMessage, String replicationKey,
            long firstErrorTime, long lastErrorTime, ReplicationEventType eventType, RepoPath repoPath) {
        ReplicationErrorInfo errorInfo = createErrorInfo(firstErrorTime, lastErrorTime, errorCount, errorMessage, replicationKey,
                eventTime, eventType, repoPath);
        replicationErrorsStorageService.add(errorInfo);
        List<ReplicationErrorInfo> errors = replicationErrorsStorageService.getErrors();
        assertEquals(errors.size(), 1);
        assertTrue(errors.contains(errorInfo));
    }

    @Test
    public void delete() {
        long currentTime = System.currentTimeMillis();
        ReplicationErrorInfo errorInfo = createErrorInfo(currentTime - 5000, currentTime, 0, "my-error-message",
                IdUtils.createReplicationKey("my-repo", "http://www.artifactoy.com/artifactory/aaaaa/"),
                System.currentTimeMillis(),
                DEPLOY, RepoPathFactory.create("my-repo", "foo/aaa.zip"));
        replicationErrorsStorageService.add(errorInfo);
        List<ReplicationErrorInfo> errors = replicationErrorsStorageService.getErrors();
        assertEquals(errors.size(), 1);
        assertTrue(errors.contains(errorInfo));
        boolean deleted = replicationErrorsStorageService
                .delete(errorInfo.getTaskType(), errorInfo.getTaskPath(), errorInfo.getReplicationKey());
        assertTrue(deleted);
        errors = replicationErrorsStorageService.getErrors();
        assertEquals(errors.size(), 0);
    }

    @Test
    public void deleteAllByKey() {
        long currentTime = System.currentTimeMillis();
        String replicationKey1 = IdUtils
                .createReplicationKey("my-repo", "http://www.artifactoy.com/artifactory/aaaaa/");
        String replicationKey2 = IdUtils
                .createReplicationKey("my-other-repo", "http://www.artifactoy.com/artifactory/aaaaa/");
        ReplicationErrorInfo errorInfo1 = createErrorInfo(currentTime - 9000, currentTime, 0, "my-error-message",
                replicationKey1,
                System.currentTimeMillis(),
                DEPLOY, RepoPathFactory.create("my-repo", "foo/aaa.zip"));
        ReplicationErrorInfo errorInfo2 = createErrorInfo(currentTime - 9000, currentTime, 0, "my-error-message",
                replicationKey1,
                System.currentTimeMillis(),
                DELETE, RepoPathFactory.create("my-repo", "foo/bbb.zip"));
        ReplicationErrorInfo errorInfo3 = createErrorInfo(currentTime - 9000, currentTime, 0, "my-error-message",
                replicationKey2,
                System.currentTimeMillis(),
                DELETE, RepoPathFactory.create("my-repo", "foo/ccc.zip"));
        replicationErrorsStorageService.add(errorInfo1);
        replicationErrorsStorageService.add(errorInfo2);
        replicationErrorsStorageService.add(errorInfo3);
        List<ReplicationErrorInfo> errors = replicationErrorsStorageService.getErrors();
        assertEquals(errors.size(), 3);
        int deleted = replicationErrorsStorageService.deleteAllByKey(replicationKey1);
        assertEquals(2, deleted);
        errors = replicationErrorsStorageService.getErrors();
        assertEquals(errors.size(), 1);
        assertTrue(errors.contains(errorInfo3));
    }

    @Test(expectedExceptions = StorageException.class)
    public void testDeleteByKeyAndTypeInvalidKey() {
        replicationErrorsStorageService.deleteAllByKeyAndType(null, DELETE);
    }

    @Test(expectedExceptions = StorageException.class)
    public void testDeleteByKeyAndTypeEmptyKey() {
        replicationErrorsStorageService.deleteAllByKeyAndType("", DELETE);
    }

    @Test(expectedExceptions = StorageException.class)
    public void testDeleteByKeyAndTypeInvalidType() {
        replicationErrorsStorageService.deleteAllByKeyAndType("valid-key", null);
    }

    @Test
    public void testDeleteAllByKeyAndType() {
        long firstErrorTime = System.currentTimeMillis() - 90000;
        long secondErrorTime = System.currentTimeMillis();
        String replicationKey1 = IdUtils
                .createReplicationKey("my-repo", "http://www.artifactoy.com/artifactory/aaaaa/");
        String replicationKey2 = IdUtils
                .createReplicationKey("my-other-repo", "http://www.artifactoy.com/artifactory/aaaaa/");
        replicationErrorsStorageService.add(createErrorInfo(firstErrorTime, secondErrorTime, 0, "my-error-message",
                replicationKey1, System.currentTimeMillis(), DELETE, RepoPathFactory.create("my-repo", "foo/aaa.zip")));
        replicationErrorsStorageService.add(createErrorInfo(firstErrorTime, secondErrorTime, 0, "my-error-message",
                replicationKey1, System.currentTimeMillis(), DELETE, RepoPathFactory.create("my-repo", "foo/bbb.zip")));
        replicationErrorsStorageService.add(createErrorInfo(firstErrorTime, firstErrorTime, 0, "my-error-message",
                replicationKey2, System.currentTimeMillis(), DEPLOY, RepoPathFactory.create("my-repo", "foo/ccc.zip")));
        replicationErrorsStorageService.add(createErrorInfo(firstErrorTime, firstErrorTime, 0, "my-error-message",
                replicationKey2, System.currentTimeMillis(), PROPERTY_CHANGE,
                RepoPathFactory.create("my-repo", "foo/ddd.zip")));
        assertEquals(replicationErrorsStorageService.getErrors().size(), 4);
        assertEquals(replicationErrorsStorageService.deleteAllByKeyAndType(replicationKey1, PROPERTY_CHANGE), 0);
        assertEquals(replicationErrorsStorageService.getErrors().size(), 4);
        assertEquals(replicationErrorsStorageService.deleteAllByKeyAndType("other", DELETE), 0);
        assertEquals(replicationErrorsStorageService.getErrors().size(), 4);
        assertEquals(replicationErrorsStorageService.deleteAllByKeyAndType(replicationKey1, DELETE), 2);
        assertEquals(replicationErrorsStorageService.getErrors().size(), 2);
        assertEquals(replicationErrorsStorageService.deleteAllByKeyAndType(replicationKey2, DELETE), 0);
        assertEquals(replicationErrorsStorageService.getErrors().size(), 2);
        assertEquals(replicationErrorsStorageService.deleteAllByKeyAndType(replicationKey2, DEPLOY), 1);
        assertEquals(replicationErrorsStorageService.getErrors().size(), 1);
        assertEquals(replicationErrorsStorageService.deleteAllByKeyAndType(replicationKey2, PROPERTY_CHANGE), 1);
        assertEquals(replicationErrorsStorageService.getErrors().size(), 0);
    }

    @Test(expectedExceptions = StorageException.class)
    public void deleteAllByKeyEmptyReplicationKey() {
        int deleted = replicationErrorsStorageService.deleteAllByKey("");
    }

    @Test(expectedExceptions = StorageException.class)
    public void deleteAllByKeyNullReplicationKey() {
        int deleted = replicationErrorsStorageService.deleteAllByKey(null);
    }

    @Test
    public void getAll() {
        List<ReplicationErrorInfo> createdErrors = createErrorInfos();
        List<ReplicationErrorInfo> returnedErrors = replicationErrorsStorageService.getErrors();
        //assert exactly all expected returnedErrors are in db
        assertEquals(new HashSet<>(createdErrors), new HashSet<>(returnedErrors));
        assertTrue(returnedErrors.size() > 2);

        //assert ordered result
        for (int i = 0; i < returnedErrors.size() - 1; i++) {
            ReplicationErrorInfo current = returnedErrors.get(i);
            ReplicationErrorInfo next = returnedErrors.get(i + 1);
            assertTrue(current.getTaskTime() <= next.getTaskTime());
        }
    }

    @Test
    public void getAllNotExist() {
        List<ReplicationErrorInfo> replicationErrorInfos = replicationErrorsStorageService.getErrors();
        assertTrue(replicationErrorInfos.isEmpty());
    }

    @Test(dataProvider = "errorRecords")
    public void testUpdate(long taskTime, int errorCount, String errorMessage, String replicationKey,
            long firstErrorTime, long lastErrorTime, ReplicationEventType eventType, RepoPath repoPath) {
        ReplicationErrorInfo errorInfo = createErrorInfo(firstErrorTime, lastErrorTime, errorCount, errorMessage,
                replicationKey, taskTime, eventType, repoPath);
        replicationErrorsStorageService.add(errorInfo);
        ReplicationErrorInfo original = replicationErrorsStorageService.getError(eventType, repoPath, replicationKey);
        assertEquals(original, errorInfo);
        ReplicationErrorInfo newError = createErrorInfo(0, 1, 2, "new-message", replicationKey,
                5, eventType, repoPath);
        int updatedRows = replicationErrorsStorageService.update(newError);
        List<ReplicationErrorInfo> actualErrorsInDb = replicationErrorsStorageService.getErrors();
        assertEquals(actualErrorsInDb.size(), 1);
        ReplicationErrorInfo updatedError = actualErrorsInDb.get(0);
        assertEquals(updatedRows, 1);
        Collection<String> excludeFields = Stream.of("taskTime", "firstErrorTime").collect(Collectors.toSet());
        assertTrue(EqualsBuilder.reflectionEquals(newError, updatedError, excludeFields));
        assertEquals(updatedError.getTaskTime(), taskTime);
        assertEquals(updatedError.getFirstErrorTime(), firstErrorTime);
    }

    @Test
    public void testUpdateNotExist() {
        ReplicationErrorInfo errorInfo = createErrorInfo(90, 1, 2, "old", "key",
                4, DEPLOY, RepoPathFactory.create("a", "b"));
        replicationErrorsStorageService.add(errorInfo);
        ReplicationErrorInfo original = replicationErrorsStorageService
                .getError(DEPLOY, RepoPathFactory.create("a", "b"), "key");
        assertEquals(original, errorInfo);
        ReplicationErrorInfo similarButDifferentError1 = createErrorInfo(1, 1, 2, "new-message", "other-key", 5, DEPLOY,
                RepoPathFactory.create("a", "b"));
        ReplicationErrorInfo similarButDifferentError2 = createErrorInfo(2, 1, 2, "new-message", "key", 5, DELETE,
                RepoPathFactory.create("a", "b"));
        ReplicationErrorInfo similarButDifferentError3 = createErrorInfo(3, 1, 2, "new-message", "key", 5, DEPLOY,
                RepoPathFactory.create("a", "other"));
        List<ReplicationErrorInfo> errors = Stream
                .of(similarButDifferentError1, similarButDifferentError2, similarButDifferentError3)
                .collect(Collectors.toList());
        for (ReplicationErrorInfo error : errors) {
            int rowsUpdated = replicationErrorsStorageService.update(error);
            assertEquals(rowsUpdated, 0);
            ReplicationErrorInfo resultError = replicationErrorsStorageService
                    .getError(DEPLOY, RepoPathFactory.create("a", "b"), "key");
            assertEquals(original, resultError);
        }
    }

    @Test
    public void testGetNotExist() {
        ReplicationErrorInfo errorInfo = replicationErrorsStorageService
                .getError(DEPLOY, RepoPathFactory.create("a", "b"), "key");
        assertNull(errorInfo);
    }

    @Test(dataProvider = "errorRecords")
    public void getError(long taskTime, int errorCount, String errorMessage, String replicationKey,
            long firstErrorTime, long lastErrorTime, ReplicationEventType eventType, RepoPath repoPath) {
        ReplicationErrorInfo errorInfo = createErrorInfo(firstErrorTime, lastErrorTime, errorCount, errorMessage,
                replicationKey, taskTime, eventType, repoPath);
        replicationErrorsStorageService.add(errorInfo);
        ReplicationErrorInfo result = replicationErrorsStorageService.getError(eventType, repoPath, replicationKey);
        assertEquals(result, errorInfo);
    }

    private List<ReplicationErrorInfo> createErrorInfos() {
        List<ReplicationErrorInfo> errorInfos = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            //necessarily create errors with decreasing time to make sure order is respected not by chance
            ReplicationErrorInfo errorInfo = createErrorInfo(i, (7 - i) * 1000, 1, "my-error" + i,
                    IdUtils.createReplicationKey("my-local-repo", "http://arto.com/artifactory/something"),
                    System.currentTimeMillis(),
                    values()[(i % 4)],
                    RepoPathFactory.create("my-local-repo", "aaa/bbb/a" + i));
            replicationErrorsStorageService.add(errorInfo);
            errorInfos.add(errorInfo);
        }
        return errorInfos;
    }

    private ReplicationErrorInfo createErrorInfo(long firstErrorTime, long lastErrorTime, int errorCount, String errorMessage,
            String replicationKey, long taskTime,
            ReplicationEventType eventType, RepoPath repoPath) {
        return ReplicationErrorInfo.builder()
                .firstErrorTime(firstErrorTime)
                .lastErrorTime(lastErrorTime)
                .errorCount(errorCount)
                .errorMessage(errorMessage)
                .replicationKey(replicationKey)
                .taskTime(taskTime)
                .taskType(eventType)
                .taskPath(repoPath)
                .build();
    }

    @DataProvider
    private Object[][] errorRecords() {
        return new Object[][]{
                {1399174828000L, 0, "this is an error", REPLICATION_KEY,  1000000000000L, 1499174828899L, DEPLOY, RepoPathFactory.create(
                        "my-repo", "some/path.jar")},
                {1399174828000L, 1, "this is an error!", REPLICATION_KEY, 1000000000000L, 1499174828899L, MKDIR, RepoPathFactory.create(
                        "his-repo", "some/path.jar")},
                {1399174828099L, 2, "this is an error!", REPLICATION_KEY, 1000000000000L, 1499174828800L, DELETE, RepoPathFactory.create(
                        "local-repo", "some/path.jar")},
                {1399174828000L, 3, "this is an error!", REPLICATION_KEY, 1000000000000L, 1499174828899L, PROPERTY_CHANGE, RepoPathFactory.create(
                        "repo", "some/path.jar")},
                {1399174828000L, 3, "this is an error!", REPLICATION_KEY, 1000000000000L, 1499174828899L, PROPERTY_CHANGE, RepoPathFactory.create(
                        "this-repo", "some/path.jar")},
                {1399174828000L, 3, "this is an error!", REPLICATION_KEY, 1000000000000L, 1499174828899L, PROPERTY_CHANGE, RepoPathFactory.create(
                        "myKey", "some/path.jar")},
        };
    }
}
