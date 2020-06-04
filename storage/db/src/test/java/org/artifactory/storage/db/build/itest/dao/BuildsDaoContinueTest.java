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

package org.artifactory.storage.db.build.itest.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.UnmodifiableIterator;
import org.artifactory.api.build.GeneralBuild;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.api.rest.common.model.continues.util.Direction;
import org.artifactory.build.BuildId;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.db.build.dao.BuildArtifactsDao;
import org.artifactory.storage.db.build.dao.BuildDependenciesDao;
import org.artifactory.storage.db.build.dao.BuildModulesDao;
import org.artifactory.storage.db.build.dao.BuildsDao;
import org.artifactory.storage.db.build.entity.BuildEntity;
import org.artifactory.storage.db.build.entity.BuildIdEntity;
import org.artifactory.storage.db.build.entity.BuildPromotionStatus;
import org.artifactory.storage.db.build.entity.BuildProperty;
import org.artifactory.storage.db.build.service.BuildIdImpl;
import org.fest.assertions.Assertions;
import org.jfrog.build.api.release.PromotionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

/**
 *
 * @author Omri Ziv
 */
@Test
public class BuildsDaoContinueTest extends BuildsDaoBaseTest {

    @Autowired
    protected BuildModulesDao buildModulesDao;

    @Autowired
    protected BuildsDao buildsDao;

    @Autowired
    protected BuildArtifactsDao buildArtifactsDao;

    @Autowired
    protected BuildDependenciesDao buildDependenciesDao;

    @BeforeClass
    public void setup() throws SQLException {
        buildArtifactsDao.deleteAllBuildArtifacts();
        buildDependenciesDao.deleteAllBuildDependencies();
        buildModulesDao.deleteAllBuildModules();
        buildsDao.deleteAllBuilds(false);
        importSql("/sql/builds.sql");
    }

    @Test
    public void testGetLatestBuildIdsWithContinue() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        BuildId buildId = new BuildIdImpl(5, "ba", "3", 1349004000000L);
        continueBuildFilter.setContinueBuildId(buildId);
        continueBuildFilter.setDirection(Direction.DESC);
        List<BuildIdEntity> latestBuildIds = buildsDao.getLatestBuildIds(continueBuildFilter);
        assertEquals(latestBuildIds.size(), 1);
        Map<String, BuildIdEntity> builds = latestBuildIds.stream()
                .collect(Collectors.toMap(BuildIdEntity::getBuildName, build -> build));
        assertEquals(builds.get("bb").getBuildId(), 4);
        assertEquals(builds.get("bb").getBuildNumber(), "2");
        assertEquals(builds.get("bb").getBuildDate(), 1349003000000L);
        assertNull(builds.get("ba"));
    }

    @Test
    public void testGetLatestBuildIdsOrderByNameWithContinue() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        BuildId buildId = new BuildIdImpl(5, "ba", "3", 1349004000000L);
        continueBuildFilter.setContinueBuildId(buildId);
        continueBuildFilter.setDirection(Direction.ASC);
        continueBuildFilter.setOrderBy(ContinueBuildFilter.OrderBy.BUILD_NAME);
        List<BuildIdEntity> latestBuildIds = buildsDao.getLatestBuildIds(continueBuildFilter);
        assertEquals(latestBuildIds.size(), 1);
        Map<String, BuildIdEntity> builds = latestBuildIds.stream()
                .collect(Collectors.toMap(BuildIdEntity::getBuildName, build -> build));
        assertEquals(builds.get("bb").getBuildId(), 4);
        assertEquals(builds.get("bb").getBuildNumber(), "2");
        assertEquals(builds.get("bb").getBuildDate(), 1349003000000L);
        assertNull(builds.get("ba"));
    }

    @Test
    public void testGetLatestBuildIdsOrderByNameWithContinueEnd() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        BuildId buildId = new BuildIdImpl(4, "bb", "2", 1349003000000L);
        continueBuildFilter.setContinueBuildId(buildId);
        continueBuildFilter.setDirection(Direction.ASC);
        continueBuildFilter.setOrderBy(ContinueBuildFilter.OrderBy.BUILD_NAME);
        List<BuildIdEntity> latestBuildIds = buildsDao.getLatestBuildIds(continueBuildFilter);
        assertEquals(latestBuildIds.size(), 0);
    }


    @Test
    public void testGetLatestBuildIdsfilteredBySearchText() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setSearchStr("ba");
        List<BuildIdEntity> latestBuildIds = buildsDao.getLatestBuildIds(continueBuildFilter);
        assertEquals(latestBuildIds.size(), 1);
        Map<String, BuildIdEntity> builds = latestBuildIds.stream()
                .collect(Collectors.toMap(BuildIdEntity::getBuildName, build -> build));
        assertNull(builds.get("bb"));
        assertEquals(builds.get("ba").getBuildId(), 5);
        assertEquals(builds.get("ba").getBuildNumber(), "3");
        assertEquals(builds.get("ba").getBuildDate(), 1349004000000L);
    }

    @Test
    public void testGetLatestBuildIdsfilteredByLimit() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setLimit(1L);
        continueBuildFilter.setDirection(Direction.DESC);
        List<BuildIdEntity> latestBuildIds = buildsDao.getLatestBuildIds(continueBuildFilter);
        assertEquals(latestBuildIds.size(), 1);
        Map<String, BuildIdEntity> builds = latestBuildIds.stream()
                .collect(Collectors.toMap(BuildIdEntity::getBuildName, build -> build));
        assertNull(builds.get("bb"));
        assertEquals(builds.get("ba").getBuildId(), 5);
        assertEquals(builds.get("ba").getBuildNumber(), "3");
        assertEquals(builds.get("ba").getBuildDate(), 1349004000000L);
    }

    @Test
    public void testGetLatestBuildIdsfilteredByZeroLimit() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setLimit(0L);
        continueBuildFilter.setDirection(Direction.DESC);
        List<BuildIdEntity> latestBuildIds = buildsDao.getLatestBuildIds(continueBuildFilter);
        assertEquals(latestBuildIds.size(), 0);
    }

    @Test
    public void testGetLatestBuildIdsOrderedByNameAsc() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setOrderBy(ContinueBuildFilter.OrderBy.BUILD_NAME);
        List<BuildIdEntity> latestBuildIds = buildsDao.getLatestBuildIds(continueBuildFilter);
        assertEquals(latestBuildIds.size(), 2);
        Map<String, BuildIdEntity> builds = latestBuildIds.stream()
                .collect(Collectors.toMap(BuildIdEntity::getBuildName, build -> build));
        assertEquals(latestBuildIds.get(0).getBuildName(), "ba");
        assertEquals(latestBuildIds.get(1).getBuildName(), "bb");
        assertEquals(builds.get("bb").getBuildId(), 4);
        assertEquals(builds.get("bb").getBuildNumber(), "2");
        assertEquals(builds.get("bb").getBuildDate(), 1349003000000L);
        assertEquals(builds.get("ba").getBuildId(), 5);
        assertEquals(builds.get("ba").getBuildNumber(), "3");
        assertEquals(builds.get("ba").getBuildDate(), 1349004000000L);
    }

    @Test
    public void testGetLatestBuildIdsOrderedByNameDesc() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setOrderBy(ContinueBuildFilter.OrderBy.BUILD_NAME);
        continueBuildFilter.setDirection(Direction.DESC);
        List<BuildIdEntity> latestBuildIds = buildsDao.getLatestBuildIds(continueBuildFilter);
        assertEquals(latestBuildIds.size(), 2);
        Map<String, BuildIdEntity> builds = latestBuildIds.stream()
                .collect(Collectors.toMap(BuildIdEntity::getBuildName, build -> build));
        assertEquals(latestBuildIds.get(0).getBuildName(), "bb");
        assertEquals(latestBuildIds.get(1).getBuildName(), "ba");
    }

    @Test
    public void testGetLatestBuildIdsOrderedByNumberDesc() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setOrderBy(ContinueBuildFilter.OrderBy.BUILD_NUMBER);
        continueBuildFilter.setDirection(Direction.DESC);
        List<BuildIdEntity> latestBuildIds = buildsDao.getLatestBuildIds(continueBuildFilter);
        assertEquals(latestBuildIds.size(), 2);
        Map<String, BuildIdEntity> builds = latestBuildIds.stream()
                .collect(Collectors.toMap(BuildIdEntity::getBuildName, build -> build));
        assertEquals(latestBuildIds.get(0).getBuildNumber(), "3");
        assertEquals(latestBuildIds.get(1).getBuildNumber(), "2");
    }

    @Test
    public void testGetBuildForName() throws SQLException {
        List<GeneralBuild> generalBuilds = buildsDao.getBuildForName("ba", new ContinueBuildFilter());
        assertEquals(generalBuilds.size(), 3);
    }

    @Test
    public void testGetBuildForNameWithLimit() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setLimit(1L);
        List<GeneralBuild> generalBuilds = buildsDao.getBuildForName("ba", continueBuildFilter);
        assertEquals(generalBuilds.size(), 1);
    }

    @Test
    public void testGetBuildForNameWithZeroLimit() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setLimit(0L);
        List<GeneralBuild> generalBuilds = buildsDao.getBuildForName("ba", continueBuildFilter);
        assertEquals(generalBuilds.size(), 0);
    }

    @Test
    public void testGetBuildForNameWithWithContinue() throws SQLException {
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        BuildId buildId = new BuildIdImpl(0L, "ba", "3", 1349004000000L);
        continueBuildFilter.setContinueBuildId(buildId);
        List<GeneralBuild> generalBuilds = buildsDao.getBuildForName("ba", continueBuildFilter);
        assertEquals(generalBuilds.size(), 2);
    }

}
