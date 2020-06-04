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
import org.artifactory.aql.result.AqlEagerResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlValueTypesTest extends AqlAbstractServiceTest {

    @Test
    public void badIntFormat() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$eq\":\"null\"}})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "AQL Expect integer value but found:null\n");
        }
    }

    @Test
    public void dateFormat() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"modified\":{\"$eq\":\"12-12-12\"}})");
        assertSize(queryResult, 0);
    }

    @Test
    public void longFormat() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"size\":{\"$eq\":\"1111111111111\"}})");
        assertSize(queryResult, 0);
    }

    @Test
    public void intFormat() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$eq\":\"1\"}})");
        assertSize(queryResult, 0);
    }

    @Test
    public void badDateFormat() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"modified\":{\"$eq\":\"null\"}})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "Invalid Date format: null, AQL expect ISODateTimeFormat or long number");
        }
    }

    @Test
    public void badLongFormat() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"size\":{\"$eq\":\"null\"}})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "AQL Expect long value but found:null\n");
        }
    }

    @Test
    public void badFileTypeFormat() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"type\":{\"$eq\":\"null\"}})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "Invalid file type: null, valid types are : file, folder, any");
        }
    }

    @Test
    public void fileTypeFormat() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"type\":{\"$eq\":\"any\"}})");
        assertSize(queryResult, 14);
    }
}
