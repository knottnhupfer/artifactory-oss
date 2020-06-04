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

package org.artifactory.storage.db.aql.itest.service.optimizer;

import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.storage.db.aql.itest.service.AqlAbstractServiceTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.artifactory.aql.model.AqlItemTypeEnum.file;

/**
 * Checks what happens in PostgreSQL: the index for node_props.prop_value is limited to 255 chars.
 * We check that the optimizer works as expected and returns only the desired results without irrelevant rows.
 * (irrelevant rows may happen since we are checking the substring of the prop_value)
 *
 * @author Yuval Reches
 */
public class PostgresPropertySubStringOptimizationTest extends AqlAbstractServiceTest {

    @BeforeClass
    public void setup() {
        importSql("/sql/aql_postgres_property.sql");
        ReflectionTestUtils.setField(aqlService, "permissionProvider", new AdminPermissions());
    }

    @Test
    public void checkPropertyKeyField() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.key\" : \"longvalue\"})");
        assertSize(queryResult, 3);
    }

    @Test
    // SimplePropertyCriterion
    public void findArtifactsByPropertyValueAndEqualsUnique() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.value\" : {\"$eq\" : \"qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s\"}})");
        assertSize(queryResult, 1);
        assertItem(queryResult, "repo1", "ant/ant/1.7", "ant-1.7.jar", file);
    }

    @Test
    // ComplexPropertyCriterion
    public void findArtifactsByPropertyValueAndEqualsComplex() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"longvalue\" : {\"$eq\" : \"qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s\"}})");
        assertSize(queryResult, 1);
        assertProperty(queryResult, "longvalue", "qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s");
    }

    @Test
    // SimplePropertyCriterion
    public void findArtifactsByPropertyValueAndEqualsExactly255() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.value\" : {\"$eq\" : \"qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s84t4n9ms1m8kmtguk1q6yupes5h7k6ij749qge7xxxllek8mlh4e2xrd08rkld6jzamcvy29teivchghbp1sdf10urotfokt7hj3zaku5ypmusvz2tbzq8wslwzgbvh89p96gt0fn2v8s1vh0aos5hp7uvzeyl9bwbg6vaq61fa63yn66methofmv4n7zyra0gv1b9u4o0866ch9w52gr\"}})");
        assertSize(queryResult, 1);
        assertItem(queryResult, "repo1", "ant/ant/1.5", "ant-1.5.jar", file);
    }

    @Test
    // ComplexPropertyCriterion
    public void findArtifactsByPropertyValueAndEqualsExactly255Complex() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"longvalue\" : {\"$eq\" : \"qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s84t4n9ms1m8kmtguk1q6yupes5h7k6ij749qge7xxxllek8mlh4e2xrd08rkld6jzamcvy29teivchghbp1sdf10urotfokt7hj3zaku5ypmusvz2tbzq8wslwzgbvh89p96gt0fn2v8s1vh0aos5hp7uvzeyl9bwbg6vaq61fa63yn66methofmv4n7zyra0gv1b9u4o0866ch9w52gr\"}})");
        assertSize(queryResult, 1);
        assertProperty(queryResult, "longvalue",
                "qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s84t4n9ms1m8kmtguk1q6yupes5h7k6ij749qge7xxxllek8mlh4e2xrd08rkld6jzamcvy29teivchghbp1sdf10urotfokt7hj3zaku5ypmusvz2tbzq8wslwzgbvh89p96gt0fn2v8s1vh0aos5hp7uvzeyl9bwbg6vaq61fa63yn66methofmv4n7zyra0gv1b9u4o0866ch9w52gr");
    }

    @Test
    // SimplePropertyCriterion
    public void findArtifactsByPropertyValueAndEqualsMoreThan255() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.value\" : {\"$eq\" : \"qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s84t4n9ms1m8kmtguk1q6yupes5h7k6ij749qge7xxxllek8mlh4e2xrd08rkld6jzamcvy29teivchghbp1sdf10urotfokt7hj3zaku5ypmusvz2tbzq8wslwzgbvh89p96gt0fn2v8s1vh0aos5hp7uvzeyl9bwbg6vaq61fa63yn66methofmv4n7zyra0gv1b9u4o0866ch9w52gr1\"}})");
        assertSize(queryResult, 1);
        assertItem(queryResult, "repo1", "ant/ant/1.6", "ant-1.6.jar", file);
    }

    @Test
    // ComplexPropertyCriterion
    public void findArtifactsByPropertyValueAndEqualsMoreThan255Complex() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"longvalue\" : {\"$eq\" : \"qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s84t4n9ms1m8kmtguk1q6yupes5h7k6ij749qge7xxxllek8mlh4e2xrd08rkld6jzamcvy29teivchghbp1sdf10urotfokt7hj3zaku5ypmusvz2tbzq8wslwzgbvh89p96gt0fn2v8s1vh0aos5hp7uvzeyl9bwbg6vaq61fa63yn66methofmv4n7zyra0gv1b9u4o0866ch9w52gr1\"}})");
        assertSize(queryResult, 1);
        assertProperty(queryResult, "longvalue",
                "qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s84t4n9ms1m8kmtguk1q6yupes5h7k6ij749qge7xxxllek8mlh4e2xrd08rkld6jzamcvy29teivchghbp1sdf10urotfokt7hj3zaku5ypmusvz2tbzq8wslwzgbvh89p96gt0fn2v8s1vh0aos5hp7uvzeyl9bwbg6vaq61fa63yn66methofmv4n7zyra0gv1b9u4o0866ch9w52gr1");
    }

}