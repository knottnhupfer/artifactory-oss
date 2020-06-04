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

import org.artifactory.aql.result.AqlEagerResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.artifactory.aql.model.AqlItemTypeEnum.file;

/**
 * @author Shay Yaakov
 */
@Test
public class AqlEscapingTest extends AqlAbstractServiceTest {

    @BeforeClass
    @Override
    public void setup() {
        importSql("/sql/aql_escaping.sql");
        ReflectionTestUtils.setField(aqlService, "permissionProvider", new AdminPermissions());
    }

    @Test
    public void findItemWithEscapedCharacter() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\": \"repo1\" , \"type\" :\"file\", \"path\":{\"$match\":\"org/yossis/ui_tool?\"}})");
        assertSize(queryResult, 1);
        assertItem(queryResult, "repo1", "org/yossis/ui_tools", "test.bin", file);
    }

    @Test
    public void findItemWithCaret() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\": \"repo1\" , \"type\" :\"file\", \"path\":{\"$match\":\"a^_*\"}})");
        assertSize(queryResult, 1);
        assertItem(queryResult, "repo1", "a^_/ant/1.5", "an^t-1.5.jar", file);
    }

    @Test
    public void findArtifactsByPropertyAndMatch() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"@property\"  : {\"$match\" : \"bla_bl*\"}})");
        assertSize(queryResult, 1);
        assertItem(queryResult, "repo1", "org/yossis/ui_tools", "test.bin", file);
    }

    @Test
    public void findArtifactsByPropertyAndNotMatchOnProperty() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"@property\"  : {\"$nmatch\" : \"bla_bl?\"}})");
        assertSize(queryResult, 2);
        assertItem(queryResult, "repo1", "a^_/ant/1.5", "an^t-1.5.jar", file);
    }

    @Test
    public void findArtifactsByPropertyAndNotMatchOnField() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\"  : {\"$nmatch\" : \"_bla_bl_\"}})");
        assertSize(queryResult, 3);
        assertItem(queryResult, "repo1", "a^_/ant/1.5", "an^t-1.5.jar", file);
    }

    @Test
    public void findArtifactsByPropertyAndNotMatchOnFieldUsingStarEscaping() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\"  : {\"$nmatch\" : \"_bla_bl*\"}})");
        assertSize(queryResult, 3);
        assertItem(queryResult, "repo1", "a^_/ant/1.5", "an^t-1.5.jar", file);
    }

    @Test

    public void findArtifactsByPropertyAndMatchOnFieldUsingStarEscaping() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\"  : {\"$match\" : \"_bla_bl*\"}})");
        assertSize(queryResult, 0);
    }
    @Test
    public void msp() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$msp\":[{\"@property\" :{\"$match\":\"bla_bl*\"}},{\"repo\" :{\"$match\":\"repo1\"}}]})");
        assertSize(queryResult, 1);
        assertItem(queryResult, "repo1", "org/yossis/ui_tools", "test.bin", file);
    }
}
