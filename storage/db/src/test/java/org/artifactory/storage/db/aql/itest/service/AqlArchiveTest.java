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
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.aql.result.rows.AqlTestHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Gidi Shabat
 */
public class AqlArchiveTest extends AqlAbstractServiceTest {

    @Test
    public void archiveWithBuilds() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "archive.entries.find({\"archive.item.repo\" : \"repo1\"," +
                        "\"archive.item.artifact.module.build.name\" : {\"$eq\":\"ba\"}})");
        assertSize(queryResult, 3);
        assertArchive(queryResult,  ".","Test");
        assertArchive(queryResult,  "path","file.file");
        assertArchive(queryResult,  "another","test.me");
    }


    @Test
    public void buildsWithArchive() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "builds.find({\"module.artifact.item.archive.entry.name\" :{\"$match\":\"*\"}})");
        assertSize(queryResult, 3);
        assertBuild(queryResult, "ba", "1");
        assertBuild(queryResult, "bb", "1");
        assertBuild(queryResult, "ba", "2");
    }

    @Test
    public void archiveWithBuildsWithInclude() {
        String query = "archive.entries.find({\"archive.item.repo\" : \"repo1\"," +
                "\"archive.item.artifact.module.build.name\" : {\"$match\":\"bb\"}})" +
                ".include(\"archive.item\")";
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                query);
        assertSize(queryResult, 6);
        assertArchive(queryResult,  "META-INF","LICENSE.txt");
        assertArchive(queryResult,  "META-INF","MANIFEST.MF");
        assertArchive(queryResult,  "org/apache/tools/ant/filters","BaseFilterReader.class");
        assertArchive(queryResult,  "org/apache/tools/ant/filters","BaseParamFilterReader.class");
        assertArchive(queryResult,  "org/apache/tools/mail","MailMessage.class");
        assertArchive(queryResult,  "path","file.file");
        assertMatchAsStream(query, queryResult);

    }


    @Test
    public void sameEagerAndStream() {
        String query = "archive.entries.find({\"archive.item.repo\" : \"repo1\"," +
                "\"archive.item.artifact.module.build.name\" : {\"$eq\":\"ba\"}})";
        AqlEagerResult queryResult = aqlService.executeQueryEager(query);
        assertMatchAsStream(query, queryResult);
    }

    private void assertMatchAsStream(String query, AqlEagerResult queryResult) {
        Consumer<Exception> exceptionConsumer = e -> {
            if (e != null) {
                throw new RuntimeException(e);
            }
        };
        Stream<AqlRowResult> queryResultStream2 = aqlService.executeQueryLazy(query).asStream(exceptionConsumer);
        AqlTestHelper.compareResults(Assert::assertEquals, queryResult, queryResultStream2);
    }

}