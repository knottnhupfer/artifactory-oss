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

package org.artifactory.storage.jobs.migration.sha256;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.jfrog.storage.binstore.common.BinaryElementImpl;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

/**
 * @author Uriah Levy
 * This test facilitates a relationship between the {@link Sha256MigrationJob} and the
 * {@link org.artifactory.api.repo.RepositoryService}, which provides the main functionality for setting SHA2 on nodes.
 */
public class Sha256MigrationJobWorkItemTest extends Sha256MigrationJobTestBase {

    /**
     * Expect:
     * 1. The migration state to be OK regardless to an {@link ItemNotFoundRuntimeException} being thrown during
     * sha2 update procedure
     * 2. The migration to fail due to other types of exceptions (i.e a standard {@link RuntimeException}
     */
    @Test(dataProvider = "provideSha256MigrationJobData")
    public void testMigrationStateOkWithNoneFatalError(String sha1, String sha2, String md5, String repoKey,
                                                       BinaryElementImpl binaryElement, RepoPath repoPath,
                                                       Throwable exception)
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, SQLException {
        initStubs(sha1, sha2, md5, repoKey, binaryElement, repoPath, exception);
        Sha256MigrationJob sha256MigrationJob = new Sha256MigrationJob();
        // Test subject
        ChecksumCalculationWorkItem workItem = createWorkItem(sha1, repoPath, sha256MigrationJob);
        // Manipulates test subject
        repoService.updateSha2(workItem);
        if (exception.getClass().isAssignableFrom(RuntimeException.class)) {
            // Should fail
            Assert.assertTrue(getErrorMapFieldSize(sha256MigrationJob, "fatalNodeErrors") == 1);
            Assert.assertTrue(!sha256MigrationJob.stateOk(), "Migration should fail!");
        } else if (exception.getClass().isAssignableFrom(ItemNotFoundRuntimeException.class)) {
            // Should succeed
            Assert.assertTrue(getErrorMapFieldSize(sha256MigrationJob, "noneFatalNodeErrors") == 1,
                    "Migration job should have at least one none-fatal error");
            Assert.assertTrue(sha256MigrationJob.stateOk(), "Migration state should be OK!");
        }
    }

    private ChecksumCalculationWorkItem createWorkItem(String sha1, RepoPath repoPath,
                                                       Sha256MigrationJob sha256MigrationJob) {
        ListMultimap<String, RepoPath> multimap = ArrayListMultimap.create();
        multimap.put(sha1, repoPath);
        return new ChecksumCalculationWorkItem(sha1, multimap.get(sha1), sha256MigrationJob);
    }

    @DataProvider
    public static Object[][] provideSha256MigrationJobData() {

        return new Object[][]{
                {"380ef5226de2c85ff3b38cbfefeea881c5fce09d",
                        "c1e127ba48d2830896df411b6ce1c03bbad19e43eb7a7c1aec7394b7a20f6800",
                        "40be305ee91fb6a7a385eeb260c7c08a", "libs-release-local",
                        new BinaryElementImpl("380ef5226de2c85ff3b38cbfefeea881c5fce09d", null, null, -1),
                        RepoPathFactory.create("libs-release-local", "foo.ext"),
                        new ItemNotFoundRuntimeException("Repository libs-release-local not local, cache or virtual")},

                {"380ef5226de2c85ff3b38cbfefeea881c5fce09d",
                        "c1e127ba48d2830896df411b6ce1c03bbad19e43eb7a7c1aec7394b7a20f6800",
                        "40be305ee91fb6a7a385eeb260c7c08a", "libs-release-local",
                        new BinaryElementImpl("380ef5226de2c85ff3b38cbfefeea881c5fce09d", null, null, -1),
                        RepoPathFactory.create("libs-release-local", "foo.ext"),
                        new RuntimeException("Bad bad bad!")}
        };
    }

}