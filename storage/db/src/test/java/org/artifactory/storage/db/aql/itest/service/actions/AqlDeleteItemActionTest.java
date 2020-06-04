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

package org.artifactory.storage.db.aql.itest.service.actions;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.aql.itest.service.AqlAbstractServiceTest;
import org.slf4j.helpers.NOPLogger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.artifactory.storage.db.aql.itest.service.actions.AqlAbstractActionTestHelper.executeQuery;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Dan Feldman
 */
@Test
public class AqlDeleteItemActionTest extends AqlAbstractServiceTest {

    private RepositoryService repoService = mock(RepositoryService.class);
    private AuthorizationService authService = mock(AuthorizationService.class);

    @BeforeClass
    @Override
    public void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();
        addBean(repoService, RepositoryService.class);
        addBean(authService, AuthorizationService.class);
    }

    @BeforeClass(dependsOnMethods = "springTestContextPrepareTestInstance")
    public void setup() {
        super.setup();
    }

    @BeforeMethod
    public void cleanUp() {
        reset(repoService, authService);
    }

    @Test(enabled = false)
    public void testDeleteStringQuery() throws Exception {
        when(repoService.undeployMultiTransaction(any(RepoPath.class))).thenReturn(new BasicStatusHolder());
        String result = executeQuery(aqlService,
                "items.delete({\"repo\": \"repo1\" , \"type\" :\"file\"}).dryRun(\"false\")");

        assertThat(result).contains("\"repo\" : \"repo1\",\n  \"path\" : \"ant/ant/1.5\",\n  \"name\" : \"ant-1.5.jar\",");
        assertThat(result).contains("\"repo\" : \"repo1\",\n  \"path\" : \"org/yossis/tools\",\n  \"name\" : \"file2.bin\",");
        assertThat(result).contains("\"repo\" : \"repo1\",\n  \"path\" : \"org/yossis/tools\",\n  \"name\" : \"file3.bin\",");
        assertThat(result).contains("\"repo\" : \"repo1\",\n  \"path\" : \"org/yossis/tools\",\n  \"name\" : \"test.bin\",");

        // There should be 4 deletes 1 for each result row
        verify(repoService, atLeast(4)).undeployMultiTransaction(any(RepoPath.class));
        verify(repoService, atMost(4)).undeployMultiTransaction(any(RepoPath.class));
    }

    @Test
    public void testDeleteFailed() throws Exception {
        BasicStatusHolder badHolder = new BasicStatusHolder();
        badHolder.error("Y U NO DELETE", NOPLogger.NOP_LOGGER);
        when(repoService.undeployMultiTransaction(any(RepoPath.class))).thenReturn(badHolder);

        String result = executeQuery(aqlService,
                "items.delete({\"repo\": \"repo1\" , \"type\" :\"file\"}).dryRun(\"false\")");
        assertThat(result).contains("\"total\" : 0");
        assertThat(result).contains("\"results\" : [  ]");

        // There should be 4 deletes that failed.
        verify(repoService, atLeast(4)).undeployMultiTransaction(any(RepoPath.class));
        verify(repoService, atMost(4)).undeployMultiTransaction(any(RepoPath.class));
    }

    @Test
    public void testDryRun() throws Exception {
        when(authService.canDelete(any(RepoPath.class))).thenReturn(false, false, true, true);

        String result = executeQuery(aqlService,
                "items.delete({\"repo\": \"repo1\" , \"type\" :\"file\"}).dryRun(\"true\")");
        assertThat(result).contains("\"total\" : 2");

        // There should be 0 deletes in dry run mode
        verify(repoService, atLeast(0)).undeployMultiTransaction(any(RepoPath.class));
        verify(repoService, atMost(0)).undeployMultiTransaction(any(RepoPath.class));

        // Auth service should have been queried 4 times in dry run mode
        verify(authService, atLeast(4)).canDelete(any(RepoPath.class));
        verify(authService, atMost(4)).canDelete(any(RepoPath.class));
    }

    // Only works with the result streamer for now
    @Test(enabled = false)
    public void testDeleteApiQuery() {

    }
}
