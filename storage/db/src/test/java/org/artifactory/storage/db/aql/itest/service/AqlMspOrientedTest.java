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
import org.artifactory.aql.result.rows.AqlBaseItem;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.fest.assertions.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;

/**
 * @author gidis
 */
public class AqlMspOrientedTest extends DbBaseTest {

    @Autowired
    private AqlServiceImpl aqlService;

    @BeforeClass
    public void setup() {
        importSql("/sql/aql_msp.sql");
        ReflectionTestUtils.setField(aqlService, "permissionProvider", new AqlAbstractServiceTest.AdminPermissions());
    }

    @Test
    public void noneMsp() {
        // without $smp the result will include the item with the LGPL license because it has another property
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({"+
                            "\"@license\":{\"$match\": \"*GPL*\"}, " +
                            "\"@license\":{\"$nmatch\": \"LGPL-V5*\"}" +
                        "})"
        );

        List<AqlBaseItem> items = results.getResults();
        Assertions.assertThat(items).hasSize(1);
        Assertions.assertThat(items.get(0).getName()).isEqualTo("ant-1.5.jar");
    }

    @Test
    public void msp() {
        // without $smp the result will include the item with the LGPL license because it has another property
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"$msp\": [" +
                            "{\"@license\":{\"$match\": \"*GPL*\"}}," +
                            "{\"@license\":{\"$ne\": \"LGPL-V5\"}}" +
                        "]})"
        );

        List<AqlBaseItem> items = results.getResults();
        Assertions.assertThat(items).hasSize(2);
        items.sort(Comparator.comparing(AqlBaseItem::getName));
        Assertions.assertThat(items.get(0).getName()).isEqualTo("ant-1.5.jar");
    }

}
