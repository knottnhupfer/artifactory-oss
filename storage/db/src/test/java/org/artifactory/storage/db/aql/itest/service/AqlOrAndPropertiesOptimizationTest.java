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
@Test
public class AqlOrAndPropertiesOptimizationTest extends AqlAbstractServiceTest {

    @Test
    public void orOptimizationWithPropertyKeyFields() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"property.key\" : \"test\"},{\"property.key\" : \"file\"}]})");
        assertSize(queryResult, 0);
    }

    @Test
    public void orOptimizationWithResult() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"@*\" : {\"$eq\" : \"ant\"} , \"@build.name\" : {\"$eq\" : \"*\"}})");
        assertSize(queryResult, 1);
    }

    @Test
    public void OrOptimizationWithPropertyValueFields() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"property.value\" : \"test\"},{\"property.value\" : \"file\"}]})");
        assertSize(queryResult, 0);
    }

    @Test
    public void OrOptimizationWithPropertyValueAndPropertyKeyFields() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"property.value\" : \"test\"},{\"property.value\" : \"file\"},{\"property.key\" : \"file\"}]})");
        assertSize(queryResult, 0);
    }

    @Test
    public void OrOptimizationWithPropertyCriterias() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"@a\" : \"test\"},{\"@b\" : \"file\"},{\"@c\" : \"c\"}]})");
        assertSize(queryResult, 0);
    }

    /**
     * Note that without the optimization the query duration is grater than 5 minutes
     * and with the optimisation it is seconds
     */
    @Test
    public void QueryTimeImprovementTest() {
        long start = System.nanoTime();
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[" +
                        "{\"@aaaaaaaaaaaaa\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@ccccccccccccc\" : \"c\"}," +
                        "{\"@ddddddddddddd\" : \"d\"}," +
                        "{\"@eeeeeeeeeeeee\" : \"e\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}" +
                        "]})");
        long end = System.nanoTime();
        Assert.assertTrue(end-start<5*1000*1000*1000);
        assertSize(queryResult, 0);
    }

    @Test
    public void OrOptimizationWithSimpleCriterias() {
        long start = System.nanoTime();
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[" +
                        "{\"path\" : \"test\"}," +
                        "{\"@aaaaaaaaaaaaa\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"path\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@ccccccccccccc\" : \"c\"}," +
                        "{\"@ddddddddddddd\" : \"d\"}," +
                        "{\"@eeeeeeeeeeeee\" : \"e\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"path\" : \"j\"}" +
                        "]})");
        long end = System.nanoTime();
        Assert.assertTrue(end - start < 5 * 1000 * 1000 * 1000);
        assertSize(queryResult, 0);
    }

    @Test
    public void derbyPreferMarchThanEqual() {
        long start = System.nanoTime();
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[" +
                        "{\"path\" : \"test\"}," +
                        "{\"@aaaaaaaaaaaaa\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"path\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@ccccccccccccc\" : \"c\"}," +
                        "{\"@ddddddddddddd\" : \"d\"}," +
                        "{\"@eeeeeeeeeeeee\" : \"e\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"path\" : \"j\"}" +
                        "]})");
        long end = System.nanoTime();
        Assert.assertTrue(end - start < 5 * 1000 * 1000 * 1000);
        assertSize(queryResult, 0);
    }

}