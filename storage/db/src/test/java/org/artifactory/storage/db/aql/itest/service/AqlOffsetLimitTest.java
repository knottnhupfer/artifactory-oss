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
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlOffsetLimitTest extends AqlAbstractServiceTest {

    /**
     * All properties for sanity
     */
    @Test
    public void allPropertiesForSanity() {
        AqlEagerResult queryResult = aqlService.executeQueryEager("properties.find()");
        assertSize(queryResult, 11);
    }

    /**
     * offset with limit test
     */
    @Test
    public void offset4LimitWith4() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().sort({\"$asc\":[\"key\"]}).offset(4).limit(4)");
        assertSize(queryResult, 4);
        assertProperty(queryResult, "string", "this is string");
    }


    /**
     * offset with limit test
     */
    @Test
    public void offset4LimitWith5() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().sort({\"$asc\":[\"key\"]}).offset(4).limit(5)");
        assertSize(queryResult, 5);
        assertProperty(queryResult, "jungle", "value2");
        assertProperty(queryResult, "null.val", null);
        assertProperty(queryResult, "string", "this is string");
        assertProperty(queryResult, "trance", "me");
        assertProperty(queryResult, "wednesday", "odin");
    }

    /**
     * just limit test
     */
    @Test
    public void justLimitTest() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().sort({\"$asc\":[\"key\"]}).limit(5)");
        assertSize(queryResult, 5);
        assertProperty(queryResult, "jungle", "value2");
    }

    /**
     * just limit And Constant value of sqlServerQueryBuilderForceAddOffsetToQuery is True
     */
    @Test
    public void justLimitAndForceOffsetTest() {
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.sqlServerQueryBuilderForceAddOffsetToQuery.getPropertyName(), "true");
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().sort({\"$asc\":[\"key\"]}).limit(5)");
        assertSize(queryResult, 5);
        assertProperty(queryResult, "jungle", "value2");
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.sqlServerQueryBuilderForceAddOffsetToQuery.getPropertyName(), "false");

    }

    /**
     * just limit test
     */
    @Test
    public void justLimitOffsetAndSortTest() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().sort({\"$asc\":[\"key\",\"value\"]}).limit(5)");
        assertSize(queryResult, 5);
        assertProperty(queryResult, "jungle", "value2");
    }

    /**
     * just limit test
     */
    @Test
    public void justOffsetTest() {
        AqlEagerResult queryResult = aqlService.executeQueryEager("properties.find().offset(4)");
        assertSize(queryResult, 7);
    }

}