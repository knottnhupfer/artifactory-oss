package org.artifactory.storage.db.security.service;

import org.artifactory.common.ConstantValues;
import org.artifactory.storage.security.service.UserLockInMemoryService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Nadav Yogev
 */
public class UserLockInMemoryServiceTest extends ArtifactoryHomeBoundTest {

    @Test
    public void testNextLoginTimes() {
        UserLockInMemoryServiceImpl lockInMemoryService = createUserLockService(128, 129, 2);
        getNextLoginTime(lockInMemoryService, 1, -1);
        getNextLoginTime(lockInMemoryService, 2, 128);
        getNextLoginTime(lockInMemoryService, 3, 129);
        getNextLoginTime(lockInMemoryService, 4, 129);
        getNextLoginTime(lockInMemoryService, 100, 129);

        lockInMemoryService = createUserLockService(128, 512, 3);
        getNextLoginTime(lockInMemoryService, 1, -1);
        getNextLoginTime(lockInMemoryService, 2, -1);
        getNextLoginTime(lockInMemoryService, 3, 128);
        getNextLoginTime(lockInMemoryService, 4, 256);
        getNextLoginTime(lockInMemoryService, 5, 512);
        getNextLoginTime(lockInMemoryService, 6, 512);

        lockInMemoryService = createUserLockService(128, 256, 1);
        getNextLoginTime(lockInMemoryService, 1, 128);
        getNextLoginTime(lockInMemoryService, 2, 256);
        getNextLoginTime(lockInMemoryService, 4, 256);

        lockInMemoryService = createUserLockService(500, 5000, 2); //default behaviour
        getNextLoginTime(lockInMemoryService, 1, -1);
        getNextLoginTime(lockInMemoryService, 2, 500);
        getNextLoginTime(lockInMemoryService, 3, 1000);
        getNextLoginTime(lockInMemoryService, 4, 2000);
        getNextLoginTime(lockInMemoryService, 5, 4000);
        getNextLoginTime(lockInMemoryService, 6, 5000);
        getNextLoginTime(lockInMemoryService, 7, 5000);
        getNextLoginTime(lockInMemoryService, 10, 5000);
    }

    @Test
    public void testZeroBlockDelay() {
        UserLockInMemoryServiceImpl lockInMemoryService = createUserLockService(0, 1000, 2);
        getNextLoginTime(lockInMemoryService, 1, -1);
        getNextLoginTime(lockInMemoryService, 2, -1);
        getNextLoginTime(lockInMemoryService, 10, -1);
        getNextLoginTime(lockInMemoryService, 100, -1);
    }

    @Test
    public void testZeroMaxDelay() {
        UserLockInMemoryServiceImpl lockInMemoryService = createUserLockService(128, 0, 2);
        getNextLoginTime(lockInMemoryService, 1, -1);
        getNextLoginTime(lockInMemoryService, 2, -1);
        getNextLoginTime(lockInMemoryService, 10, -1);
        getNextLoginTime(lockInMemoryService, 100, -1);
    }

    @Test
    public void testStressNextLoginTimes() {
        UserLockInMemoryServiceImpl lockInMemoryService = createUserLockService(500, 5000, 2);
        long start = System.currentTimeMillis();
        for (int i = 1; i < 100_000; i++) {
            lockInMemoryService.getNextLoginTime(i, start);
        }
        assertThat(System.currentTimeMillis() - start).isLessThan(250)
                .overridingErrorMessage("100,000 failed request should take no more than a few dozens of ms.");
    }

    private UserLockInMemoryServiceImpl createUserLockService(int loginBlockDelay, int loginMaxDelay, int maxAttempts) {
        getBound().setProperty(ConstantValues.loginBlockDelay, String.valueOf(loginBlockDelay));
        getBound().setProperty(ConstantValues.loginMaxBlockDelay, String.valueOf(loginMaxDelay));
        getBound().setProperty(ConstantValues.maxIncorrectLoginAttempts, String.valueOf(maxAttempts));
        return new UserLockInMemoryServiceImpl();
    }


    private void getNextLoginTime(UserLockInMemoryService lockInMemoryService, int attempt, int lockoutTime) {
        long now = System.currentTimeMillis();
        long nextLoginTime = lockInMemoryService.getNextLoginTime(attempt, now);
        assertThat(nextLoginTime).isEqualTo(lockoutTime == -1 ? -1 : now + lockoutTime);
    }
}