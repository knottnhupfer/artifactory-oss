/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.db.lock.itest;

import ch.qos.logback.classic.Level;
import com.google.common.collect.Lists;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.locks.provider.DbMapLockWrapper;
import org.artifactory.storage.db.locks.service.DbLocksServiceImpl;
import org.artifactory.storage.fs.lock.FsItemsVaultCacheImpl;
import org.artifactory.storage.fs.lock.LockEntryId;
import org.artifactory.storage.fs.lock.provider.LockWrapper;
import org.artifactory.test.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.*;

//TODO [by shayb]: I've added DbLocksDaoTest, but we still need similar test to the actual service, therefore, this needs to be converted to DbLockServiceTest and we should add all the service functionallities  to this test
/**
 * @author gidis
 */
public class LockTest extends DbBaseTest {

    @Autowired
    private DbLocksServiceImpl dbLocksService;

    @BeforeClass
    public void setup() {
        importSql("/sql/aql_test.sql");
        TestUtils.setLoggingLevel(DbMapLockWrapper.class, Level.ERROR);
    }

    // Perform concurrent try lock operations on the DB locking mechanism and make sure
    @Test
    public void concurrentDBLockTest() throws TimeoutException, ExecutionException, InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(40, 100, 100L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
        FsItemsVaultCacheImpl vaultCache = new FsItemsVaultCacheImpl(repoPath -> new DbMapLockWrapper(repoPath, dbLocksService, "server"));
        List<Future> futures = Lists.newArrayList();
        for (int i = 0; i < 60; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < 50; j++) {
                    LockEntryId lock = vaultCache.getLock(RepoPathFactory.create("repo", "test"));
                    LockWrapper lockWrapper = lock.getLock();
                    if (lockWrapper.isHeldByCurrentThread()) {
                        throw new RuntimeException("Current thread should not be the owner of a lock!");
                    }
                    if (lockWrapper.tryLock(1, TimeUnit.MILLISECONDS)) {
                        try {
                            Thread.sleep(100);
                            if (!lockWrapper.isHeldByCurrentThread()) {
                                throw new RuntimeException("Current thread must be owner of this lock!");
                            }
                        } finally {
                            lockWrapper.unlock();
                        }
                        if (lockWrapper.isHeldByCurrentThread()) {
                            throw new RuntimeException("Current thread should not be the owner of a lock!");
                        }
                    }

                }
                return true;
            }));
        }

        for (Future future : futures) {
            future.get();
        }
    }
}
