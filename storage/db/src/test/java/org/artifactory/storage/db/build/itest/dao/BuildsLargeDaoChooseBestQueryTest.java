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

import org.artifactory.api.build.BuildProps;
import org.artifactory.api.build.model.diff.BuildParams;
import org.artifactory.storage.db.build.entity.BuildEntity;
import org.artifactory.storage.db.build.entity.BuildEntityRecord;
import org.artifactory.storage.db.build.entity.BuildProperty;
import org.artifactory.storage.db.build.service.BuildStoreServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import static org.testng.Assert.assertEquals;

/**
 * (NadavY) Refactoring this test. if you want some of the disabled functionality, check history. no reason to leave so many disabled lines.
 *
 * @author Saffi Hartal
 */
@Test
public class BuildsLargeDaoChooseBestQueryTest extends BuildsDaoBaseTest {
    private static final Logger log = LoggerFactory.getLogger(BuildsLargeDaoChooseBestQueryTest.class);

    private static final String BUILD_SYSTEM_PROPS_BY_BUILD_ID =
            "SELECT prop_key, prop_value from  build_props\n" +
                    " WHERE build_id  = ? AND prop_key NOT LIKE 'buildInfo.env.%'";
    private static final String BUILD_SYSTEM_PROPS_BY_BUILD_ID_DISTINCT_REFERENCE =
            "SELECT distinct prop_key, prop_value FROM  build_props\n" +
                    " WHERE build_id  = ? AND prop_key NOT LIKE 'buildInfo.env.%'";
    private BuildEntity build;

    @BeforeClass
    public void setup() throws SQLException {
        importSql("/sql/builds.sql");
        Instant start = Instant.now();

        int lots = 10000 - 100;
        build = createBuildLots(lots);
        assertEquals(createBuild(build), lots + 1);
        log.info("duplicate " + lots);
        jdbcHelper.executeUpdate(
                " insert into build_props select prop_id+10000,build_id,prop_key,prop_value from build_props");
        Duration duration = Duration.between(start, Instant.now());
        log.info("setup took {}", duration);

    }

    private BuildEntity createBuildLots(int lots) {
        long now = System.currentTimeMillis();
        BuildEntityRecord build = new BuildEntityRecord((long) 15, "15", "" + (long) 15, now - 20000L, null, now,
                "this-is-me",
                (long) 15, null);
        HashSet<BuildProperty> props = new HashSet<>();
        long buildId = build.getBuildId();
        for (int i = 0; i < lots; i++) {
            String key = "buildInfo.env.";
            String value = "" + i;
            if (i % 10 == 0) {
                key = key + "is % 10 ";
            } else if (i % 10 == 1) {
                key = "1  % 10 ";
            } else if (i % 10 == 2) {
                key = "2  % 10 " + i / 10;
            } else {
                key = "" + i;
                value = "" + i;
            }
            props.add(new BuildProperty(1 + 100 + i, buildId, key, value));
        }
        return new BuildEntity(build, props, new TreeSet<>());
    }



    @Test
    public void testGetDistinctBuildPropsResultSetOnQuery() throws Exception {
        BuildParams buildParams = new BuildParams(null, build.getBuildNumber(), null,
                null, "" + build.getBuildDate(), build.getBuildName());
        Long buildId = buildsDao.getBuildId(buildParams);
        int distinct_rows = 8910;
        log.info("measure select distinct for comparision ");

        String query = BUILD_SYSTEM_PROPS_BY_BUILD_ID_DISTINCT_REFERENCE;
        Instant start = Instant.now();
        int cnt = 0;
        try (ResultSet rs = jdbcHelper.executeSelect(query, buildId)) {
            while (rs.next()) {
                cnt++;
            }
        }
        Duration duration = Duration.between(start, Instant.now());
        log.info("'{}' took {} -- #cnt {}", query, duration, cnt);
        assertEquals(cnt, distinct_rows);

        query = BUILD_SYSTEM_PROPS_BY_BUILD_ID;
        start = Instant.now();
        List<BuildProps> buildProps = buildsDao.getBuildPropsList(query, buildId);
        cnt = BuildStoreServiceImpl.distinctBuildProps(buildProps).size();
        log.info("distinct - read #cnt " + cnt);
        assertEquals(cnt, distinct_rows);
        duration = Duration.between(start, Instant.now());
        log.info("'{}' took {} -- #cnt {}", query, duration, cnt);
    }


    @AfterClass
    public void fullDelete() throws SQLException {
        assertEquals(buildArtifactsDao.deleteAllBuildArtifacts(), 6);
        assertEquals(buildDependenciesDao.deleteAllBuildDependencies(), 5);
        assertEquals(buildModulesDao.deleteAllBuildModules(), 4);
        buildsDao.deleteAllBuilds(false);
    }

}
