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

package org.artifactory.storage.db.statistics.itest;

import org.artifactory.aql.result.rows.AqlStatistics;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.storage.db.aql.itest.service.AqlServiceImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.statistics.DownloadedSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;

/**
 * @author saffih
 */
public class DownloadedSearcherTest extends DbBaseTest {
    @Autowired
    protected AqlServiceImpl aqlService;

    @BeforeClass
    public void setup() {
        importSql("/sql/statistics.sql");
    }

    @Test
    public void testDownloadedAfter() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "org/yossis/tools");
        long after = 100000 - 1;
        List<AqlStatistics> res = getDownloaded(repoPath, after);
        assertEquals(res.size(), 3);
    }

    @Test
    public void testDownloadedAfter2() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "org/yossis");
        long after = 100000 - 1;
        List<AqlStatistics> res = getDownloaded(repoPath, after);
        assertEquals(res.size(), 4);
    }

    @Test
    public void testDownloadedAfterNoPat() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "");
        long after = 100000 - 1;
        List<AqlStatistics> res = getDownloaded(repoPath, after);
        assertEquals(res.size(), 7);
    }

    @Test
    public void testDownloadedAfterAll() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "org/yossis/tools");
        long after = 100010 - 1;
        List<AqlStatistics> res = getDownloaded(repoPath, after);
        assertEquals(res.size(), 0);
    }

    @Test
    public void testDownloadedAfterAll2() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "org/yossis");
        long after = 100010 - 1;
        List<AqlStatistics> res = getDownloaded(repoPath, after);
        assertEquals(res.size(), 0);
    }

    @Test
    public void testRemDownloadedAfter() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "ant/ant/1.5");
        long after = 100000 - 1;
        List<AqlStatistics> res = getRemoteDownloaded(repoPath, after);
        assertEquals(res.size(), 1);
    }

    @Test
    public void testRemoteDownloadedAfterAll() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "org/yossis/tools");
        long after = 100010 - 1;
        List<AqlStatistics> res = getRemoteDownloaded(repoPath, after);
        assertEquals(res.size(), 0);
    }

    @Test
    public void testDownloadOnTimestamp() {
        final Exception[] ex = {null};
        Consumer<Exception> onFail = (e -> ex[0] = e);
        Stream<AqlStatistics> downloaded = new DownloadedSearcher(aqlService)
                .downloadedOnTimestamp(RepoPathFactory.create("repo1"), 100000, false, onFail);
        assertEquals(downloaded.collect(Collectors.toList()).size(), 1);
        downloaded = new DownloadedSearcher(aqlService)
                .downloadedOnTimestamp(RepoPathFactory.create("repo1"), 100004, false, onFail);
        assertEquals(downloaded.collect(Collectors.toList()).size(), 2);

        downloaded = new DownloadedSearcher(aqlService)
                .downloadedOnTimestamp(RepoPathFactory.create("repo1"), 100000, true, onFail);
        assertEquals(downloaded.collect(Collectors.toList()).size(), 1);
    }


    private List<AqlStatistics> getDownloaded(RepoPathImpl repoPath, long after) throws Exception {
        final Exception[] ex = {null};
        Consumer<Exception> onFail = (e -> ex[0] = e);
        List<AqlStatistics> res = new DownloadedSearcher(aqlService).downloadedAfter(repoPath, after, onFail, 1000).collect(Collectors.toList());
        if (ex[0] != null) {
            throw ex[0];
        }
        return res;
    }

    private List<AqlStatistics> getRemoteDownloaded(RepoPathImpl repoPath, long after) throws Exception {
        final Exception[] ex = {null};
        Consumer<Exception> onFail = (e -> ex[0] = e);
        List<AqlStatistics> res = new DownloadedSearcher(aqlService).remoteDownloadedAfter(repoPath, after, onFail, 1000).collect(Collectors.toList());
        if (ex[0] != null) {
            throw ex[0];
        }
        return res;
    }
}