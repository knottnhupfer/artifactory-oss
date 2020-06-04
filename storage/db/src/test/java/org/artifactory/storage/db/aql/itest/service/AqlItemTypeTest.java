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
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlItemTypeTest extends AqlAbstractServiceTest {
    /**
     * By default return only files
     */
    @Test
    public void returnByDefaultOnlyFieles() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find()");
        assertSize(queryResult, 11);
    }

    /**
     * Return both files and folder using all
     */
    @Test
    public void returnFileAndFoldersUsingaAll() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"type\":\"any\"})");
        assertSize(queryResult, 26);
    }

    /**
     * Return both files and folder using file/folder
     */
    @Test
    public void returnFileAndFoldersUsingFileAnFolder() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"type\":\"file\"},{\"type\":\"folder\"}]})");
        assertSize(queryResult, 26);
    }

    /**
     * Return files
     */
    @Test
    public void returnFiles() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"type\":\"file\"})");
        assertSize(queryResult, 11);
    }

    /**
     * Return folders
     */
    @Test
    public void returnFolder() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"type\":\"folder\"})");
        assertSize(queryResult, 15);
    }

    /**
     * Return folders
     */
    @Test
    public void causingRecursiveOptimisationCleanup() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$and\":[{\"type\" : \"any\"}],\"$or\":[{\"repo\" : \"repo1\", \"repo\" : \"repo2\" }]})");
        assertSize(queryResult, 23);
    }

    /**
     * Return folders
     */
    @Test
    public void replaceAnyCriteriaWithFileOrFolderCriterias() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"type\" : \"any\"},{\"type\" : \"file\"}],\"$or\":[{\"repo\" : \"repo1\", \"repo\" : \"repo2\" }]})");
        assertSize(queryResult, 23);
    }




}