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

package org.artifactory.storage.db.aql.itest.service;

import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.model.AqlRepoProvider;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.*;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Gidi Shabat
 */
@Test
public class AqlAbstractServiceTest extends DbBaseTest {

    @Autowired
    protected AqlServiceImpl aqlService;

    @BeforeClass
    public void setup() {
        importSql("/sql/aql_test.sql");
        ReflectionTestUtils.setField(aqlService, "permissionProvider", new AdminPermissions());
    }

    @BeforeMethod
    public void setupTest() {
        ReflectionTestUtils.setField(aqlService, "repoProvider", dummyRepoProvider);
    }

    protected void assertItem(AqlEagerResult queryResult, String repo, String path, String name, AqlItemTypeEnum type) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBaseItem row = (AqlBaseItem) queryResult.getResult(j);
            if (row.getRepo().equals(repo) && row.getName().equals(name) &&
                    row.getPath().equals(path) && row.getType() == type) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    void assertRepoPathChecksum(AqlEagerResult queryResult, String name, String repoPathChecksum) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResult(j);
            if (row.getRepoPathChecksum().trim().equals(repoPathChecksum.trim()) && row.getName().equals(name)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertProperty(AqlEagerResult queryResult, String key, String value) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlProperty row = (AqlProperty) queryResult.getResult(j);
            if (row.getKey().equals(key) && (StringUtils.isBlank(row.getValue()) && StringUtils.isBlank(value) ||
                    (!StringUtils.isBlank(row.getValue()) && row.getValue().equals(value)))) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    void assertDependencies(AqlEagerResult queryResult, String buildDependencyName,
            String buildDependencyScope, String buildDependencyType) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildDependency row = (AqlBuildDependency) queryResult.getResult(j);
            if (row.getBuildDependencyName().equals(buildDependencyName) &&
                    row.getBuildDependencyScope().equals(buildDependencyScope) &&
                    row.getBuildDependencyType().equals(buildDependencyType)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    void assertArchive(AqlEagerResult queryResult, String path, String name) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlArchiveEntryItem row = (AqlArchiveEntryItem) queryResult.getResult(j);
            if (row.getEntryPath().equals(path) &&
                    row.getEntryName().equals(name)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    void assertBuildArtifacts(AqlEagerResult queryResult, String buildArtifactsName,
            String buildArtifactsType) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildArtifact row = (AqlBuildArtifact) queryResult.getResult(j);
            if (row.getBuildArtifactName().equals(buildArtifactsName) &&
                    row.getBuildArtifactType().equals(buildArtifactsType)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    void assertStatistics(AqlEagerResult queryResult, int downloads, String downloadBy) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlStatisticItem row = (AqlStatisticItem) queryResult.getResult(j);
            if (Objects.equals(row.getDownloadedBy(), downloadBy) && Objects.equals(row.getDownloads(), downloads)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    void assertStatisticsRemote(AqlEagerResult queryResult, int downloads, String downloadBy, String origin,
            String path) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlStatisticItem row = (AqlStatisticItem) queryResult.getResult(j);
            if (Objects.equals(row.getRemoteDownloadedBy(), downloadBy) && row.getRemoteDownloads() == downloads &&
                    Objects.equals(row.getRemoteOrigin(), origin) && Objects.equals(row.getRemotePath(), path)) {
                found = true;
            }
        }
        Assert.assertTrue(found, String.format(
                "Remote stats entry not found: downloads:%d, downloadBy:%s, origin:%s, path:%s",
                downloads, downloadBy, origin, path));
    }

    void assertBuild(AqlEagerResult queryResult, String buildName, String buildNumber) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuild row = (AqlBuild) queryResult.getResult(j);
            if (row.getBuildName().equals(buildName) &&
                    row.getBuildNumber().equals(buildNumber)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    void assertBuildPromotion(AqlEagerResult queryResult, String createdBy, String userName) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildPromotion row = (AqlBuildPromotion) queryResult.getResult(j);
            if (row.getBuildPromotionCreatedBy().equals(createdBy) &&
                    row.getBuildPromotionUser().equals(userName)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    void assertModule(AqlEagerResult queryResult, String moduleName) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildModule row = (AqlBuildModule) queryResult.getResult(j);
            if ( row.getBuildModuleName().equals(moduleName)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertSize(AqlEagerResult queryResult, int i) {
        Assert.assertEquals(queryResult.getSize(), i);
    }

    public static class AdminPermissions implements AqlPermissionProvider {

        @Override
        public boolean canRead(RepoPath repoPath) {
            return true;
        }

        @Override
        public boolean isAdmin() {
            return true;
        }

        @Override
        public boolean isOss() {
            return false;
        }
    }

    public static final AqlRepoProvider dummyRepoProvider = new EmptyRepoProvider();

    public static class EmptyRepoProvider implements AqlRepoProvider {
        @Override
        public List<String> getVirtualRepoKeysContainingRepo(String repoKey) {
            return Collections.emptyList();
        }
        @Override
        public boolean isRepoPathAccepted(RepoPath repoPath) {
            return false;
        }
        @Override
        public List<String> getVirtualRepoKeys() {
            return Collections.emptyList();
        }
        @Override
        public List<String> getVirtualResolvedLocalAndCacheRepoKeys(String virtualRepoKey) {
            return Collections.emptyList();
        }
    }
}
