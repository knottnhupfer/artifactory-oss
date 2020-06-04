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

package org.artifactory.storage.db.fs.itest.dao;

import com.google.common.collect.Lists;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.storage.db.fs.dao.StatsDao;
import org.artifactory.storage.db.fs.entity.Stat;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Tests {@link org.artifactory.storage.db.fs.dao.StatsDao}.
 *
 * @author Yossi Shaul
 */
public class StatsDaoTest extends DbBaseTest {

    @Autowired
    private StatsDao statsDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void getFileStats() throws SQLException {
        Stat stat = statsDao.getStats(6, false);
        assertNotNull(stat);
        assertEquals(stat.getNodeId(), 6L);
        assertEquals(stat.getLocalDownloadCount(), 15);
        assertEquals(stat.getLocalLastDownloaded(), 1340283207850L);
        assertEquals(stat.getLocalLastDownloadedBy(), "yossis");
    }

    public void getFileStatsWithNoDownloads() throws SQLException {
        assertNull(statsDao.getStats(11, false));
    }

    public void hasStatsFileWithStats() throws SQLException {
        assertTrue(statsDao.hasStats(6));
    }

    public void hasStatsFileWithoutStats() throws SQLException {
        assertFalse(statsDao.hasStats(11));
    }

    public void hasStatsFolder() throws SQLException {
        assertFalse(statsDao.hasStats(2), "Folders don't have stats");
    }

    public void hasStatsNonExistentId() throws SQLException {
        assertFalse(statsDao.hasStats(2375437583L));
    }

    public void createStatsFileWithoutStats() throws SQLException {
        long lastDownloaded = System.currentTimeMillis();
        int updateCount = statsDao.createStats(new Stat(12, 3, lastDownloaded, "yoyo"), false);
        assertEquals(updateCount, 1);
        Stat stats = statsDao.getStats(12, false);
        assertNotNull(stats);
        assertEquals(stats.getNodeId(), 12L);
        assertEquals(stats.getLocalDownloadCount(), 3);
        assertEquals(stats.getLocalLastDownloaded(), lastDownloaded);
        assertEquals(stats.getLocalLastDownloadedBy(), "yoyo");
    }

    @Test(dependsOnMethods = "createStatsFileWithoutStats", expectedExceptions = SQLException.class)
    public void createStatsFileWithStats() throws SQLException {
        statsDao.createStats(new Stat(12, 5, System.currentTimeMillis(), "lolo"), false);
    }

    @Test(dependsOnMethods = "getFileStats")
    public void updateStatsFileWithStats() throws SQLException {
        long time = System.currentTimeMillis();
        int updateCount = statsDao.updateStats(new Stat(6, 23, time, "yoyo"), false);

        assertEquals(updateCount, 1);
        Stat stat = statsDao.getStats(6, false);
        assertNotNull(stat);
        assertEquals(stat.getNodeId(), 6L);
        assertEquals(stat.getLocalDownloadCount(), 23);
        assertEquals(stat.getLocalLastDownloaded(), time);
        assertEquals(stat.getLocalLastDownloadedBy(), "yoyo");
    }

    public void updateStatsFileWithoutStats() throws SQLException {
        int updateCount = statsDao.updateStats(new Stat(11, 23, System.currentTimeMillis(), "yoyo"), false);
        assertEquals(updateCount, 0);
        assertNull(statsDao.getStats(11, false));
    }

    public void updateRemoteStatsFileWithoutStats() throws SQLException {
        int updateCount = statsDao
                .updateStats(new Stat(14, 23, System.currentTimeMillis(), "yoyo",
                                0, 0, "gandalf",
                                "somewhere", "nope"),
                        true);
        assertEquals(updateCount, 1);
        assertNull(statsDao.getStats(14, false));
    }

    @Test(dependsOnMethods = "createStatsFileWithStats")
    public void deleteStatsFileWithStats() throws SQLException {
        assertEquals(statsDao.deleteStats(12, false), 1);
    }

    public void deleteStatsFileWithoutStats() throws SQLException {
        assertEquals(statsDao.deleteStats(13, false), 0);
    }

    public void deleteStatsNonExistingNode() throws SQLException {
        assertEquals(statsDao.deleteStats(343434, false), 0);
    }

    @Test(dependsOnMethods = "deleteStatsFileWithStats")
    public void testGetTopLocalStats() throws Exception {
        Stat stats6 = statsDao.getStats(6, true);
        statsDao.deleteStats(6, true);
        try {
            TOP_LOCAL_STATS.values().forEach(stat -> {
                try {
                    assertEquals(statsDao.createStats(stat, false), 1);
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to prepare test data", e);
                }
            });
            //assertTopLocalStats(0);
            assertTopLocalStats(1);
            assertTopLocalStats(2);
            assertTopLocalStats(5);
            assertTopLocalStats(TOP_LOCAL_STATS.size());
            assertTopLocalStats(TOP_LOCAL_STATS.size() + 1);
            assertTopLocalStats(TOP_LOCAL_STATS.size() + 5);
        } finally {
            TOP_LOCAL_STATS.values().stream().map(Stat::getNodeId).forEach(nodeId -> {
                try {
                    statsDao.deleteStats(nodeId, false);
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to cleanup after test", e);
                }
            });
            statsDao.createStats(stats6, true);
        }
    }

    private void assertTopLocalStats(int limit) throws SQLException {
        int expectedCount = limit <= TOP_LOCAL_STATS.size() ? limit : TOP_LOCAL_STATS.size();
        List<Stat> topStats = statsDao.getTopLocalStats(limit);
        assertEquals(topStats.size(), expectedCount);
        List<Stat> expectedTopStats = Lists.newArrayList();
        TOP_LOCAL_STATS.values().stream()
                .sorted((s1, s2) -> -Long.compare(s1.getLocalDownloadCount(), s2.getLocalDownloadCount()))
                .forEach(expectedTopStats::add);
        for (int i = 0; i < topStats.size(); i++) {
            assertEquals(topStats.get(i).getNodeId(), expectedTopStats.get(i).getNodeId(), "Stat at position " + i + " not as expected");
        }
    }

    private static final Map<RepoPath, Stat> TOP_LOCAL_STATS = new HashMap<RepoPath, Stat>() {{
        put(repoPath("repo-copy", "org/yossis/tools/file3.bin"), new Stat(15, 7, now(), "someone"));
        put(repoPath("repo-copy", "org/shayy/trustme/trustme.jar"), new Stat(16, 5, now(), "someone"));
        put(repoPath("repo-copy", "org/shayy/badmd5/badmd5.jar"), new Stat(17, 10, now(), "someone"));
        put(repoPath("repo2", "a/ant-1.5.jar"), new Stat(21, 20, now(), "someone"));
        put(repoPath("repo2", "a/b/ant-1.5.jar"), new Stat(22, 22, now(), "someone"));
        put(repoPath("repo2", "aa/ant-1.5.jar"), new Stat(25, 18, now(), "someone"));
        put(repoPath("repo2", "aa/b/ant-1.5.jar"), new Stat(26, 3, now(), "someone"));
        put(repoPath("repo3", "a/b/c/g.txt"), new Stat(31, 14, now(), "someone"));
        put(repoPath("repo3", "a/B/c/f.txt"), new Stat(32, 31, now(), "someone"));
    }};

    private static long now() {
        return System.currentTimeMillis();
    }

    private static RepoPath repoPath(String repoKey, String path) {
        return RepoPathFactory.create(repoKey, path);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetTopLocalStatsForZeroLimit() throws Exception {
        statsDao.getTopLocalStats(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetTopLocalStatsForNegativeLimit() throws Exception {
        statsDao.getTopLocalStats(-1);
    }
}
