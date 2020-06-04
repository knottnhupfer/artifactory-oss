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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlExcludeFilesTest extends AqlAbstractServiceTest {
    /**
     * Exclusion test: ensure that the result from exclusion plus opposite results should be equals to items  result
     */
    @Test
    public void test() {
        AqlEagerResult queryResult = aqlService.executeQueryEager("items.find()");
        int all = queryResult.getResults().size();
        queryResult = aqlService.executeQueryEager("items.find({\"property.value\" : {\"$match\" : \"*is is st*\"}})");
        int matchFilter = queryResult.getResults().size();
        queryResult = aqlService.executeQueryEager("items.find({\"property.value\" : {\"$nmatch\" : \"*is is st*\"}})");
        int notMatchFilter = queryResult.getResults().size();
        Assert.assertEquals(matchFilter + notMatchFilter, all, "Expecting that the result of two opposite criterias " +
                "(same criteria with opposite comparator exp equal and not equal) will be equal to al items result");
    }
}