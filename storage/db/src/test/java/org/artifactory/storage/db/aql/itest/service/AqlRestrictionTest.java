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

import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.repo.RepoPath;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlRestrictionTest extends AqlAbstractServiceTest {

    @Test
    public void usageOfSortInOssShouldBeBlocked() {
        OssVersion ossVersionPermisionProvider = new OssVersion();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", ossVersionPermisionProvider);
        try {
            aqlService.executeQueryEager("items.find().sort({\"$asc\":[\"name\"]})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "Sorting is not supported by AQL in the open source version\n");
        }
    }

    @Test
    public void usageOfPropertyResultFilterInOssShouldBeBlocked() {
        OssVersion ossVersionPermisionProvider = new OssVersion();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", ossVersionPermisionProvider);
        try {
            aqlService.executeQueryEager("items.find().include(\"@version\")");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(),
                    "Filtering properties result is not supported by AQL in the open source version\n");
        }
    }

    @Test
    public void ensureQueryWithMultiDomainResultsOnOSS() {
        OssVersion ossVersionPermisionProvider = new OssVersion();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", ossVersionPermisionProvider);
        aqlService.executeQueryEager("stats.find(\n" +
                "    {\n" +
                "        \"downloads\":{\"$gt\":2},\n" +
                "        \"downloaded_by\":{\"$match\":\"\u200B_system_\u200B\"},\n" +
                "        \"downloaded\":{\"$gt\":\"13-10-14\"}\n" +
                "    }\n" +
                "    ).include(\"item.name\", \"item.actual_sha1\")");
    }

    @Test
    public void usageOfSortInNoneOssShouldNotBeBlocked() {
        ProVersion proVersionPermisionProvider = new ProVersion();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", proVersionPermisionProvider);
        aqlService.executeQueryEager("items.find().sort({\"$asc\":[\"name\"]})");
    }

    @Test
    public void usageOfPropertyResultFilterInNoneOssShouldNotBeBlocked() {
        ProVersion proVersionPermisionProvider = new ProVersion();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", proVersionPermisionProvider);
        aqlService.executeQueryEager("items.find().include(\"@version\")");
    }


    private class OssVersion implements AqlPermissionProvider {

        @Override
        public boolean canRead(RepoPath repoPath) {
            return "repo2".equals(repoPath.getRepoKey());
        }

        @Override
        public boolean isAdmin() {
            return true;
        }

        @Override
        public boolean isOss() {
            return true;
        }
    }

    private class ProVersion implements AqlPermissionProvider {

        @Override
        public boolean canRead(RepoPath repoPath) {
            return "repo2".equals(repoPath.getRepoKey());
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

}