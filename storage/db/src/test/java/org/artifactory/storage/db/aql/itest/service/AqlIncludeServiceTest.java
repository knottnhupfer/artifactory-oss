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

import com.google.common.base.Joiner;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlJsonStreamer;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.artifactory.aql.model.AqlItemTypeEnum.file;
import static org.testng.Assert.assertEquals;

/**
 * @author Gidi Shabat
 */
public class AqlIncludeServiceTest extends AqlAbstractServiceTest {

    /**
     * Add extra field from other domain and add result filter on the property key
     */
    @Test
    public void includeWithExtraFieldFromOtherDomainAndPropertyKeyFilter() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"value\" : {\"$match\" : \"*is is st*\"}}).include(\"item.name\",\"string\")");
        assertSize(queryResult, 1);
        assertProperty(queryResult, "string", "this is string");
    }

    /**
     * Override the default fields by adding extra field that belong to the main domain
     */
    @Test
    public void includeWithExtraFieldFromSameDomainAndPropertyKeyFilter() {
        // Should remove the default fields and add only the fields from the include and property filter
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.value\" : {\"$match\" : \"*is is st*\"}}).include(\"name\",\"@string\")");
        assertSize(queryResult, 1);
        String repo = ((AqlBaseFullRowImpl) queryResult.getResults().get(0)).getRepo();
        Assert.assertNull(repo);
    }

    /**
     * return result that contains all the field in the domain
     */
    @Test
    public void includeExpendDomain() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.value\" : {\"$match\" : \"*is is st*\"}}).include(\"*\",\"@string\")");
        assertSize(queryResult, 1);
        String actualMd5 = ((AqlBaseFullRowImpl) queryResult.getResults().get(0)).getActualMd5();
        assertEquals(actualMd5, "902a360ecad98a34b59863c1e65bcf71");
    }

    /**
     * return result that contains all the field in the domain
     */
    @Test
    public void includeDomainWithoutAsterisk() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.value\" : {\"$match\" : \"*is is st*\"}}).include(\"stat\")");
        assertSize(queryResult, 1);
        int downloads = ((AqlBaseFullRowImpl) queryResult.getResults().get(0)).getDownloads();
        assertEquals(downloads, 9);
    }

    /**
     * return result that contains all the domain's default fields and the build fields
     */
    @Test
    public void includeExtraDomain() {
        AqlEagerResult<AqlBaseFullRowImpl> queryResult = aqlService.executeQueryEager(
                "items.find({\"artifact.module.build.number\" : {\"$match\" : \"2\"}}).include(\"artifact.module.build.*\")");
        assertSize(queryResult, 5);
        long buildArtifactCount = queryResult.getResults().stream()
                .filter(row -> row.getBuildNumber().equals("2"))
                .filter(row -> row.getBuildName().equals("ba"))
                .count();
        assertEquals(buildArtifactCount, 5, "Expected 5 artifacts to match build 'ba' #2");
    }

    /**
     * return result that contains all the domain's default fields and extra fields
     */
    @Test
    public void findPropertiesUsingNamesExtensionWithOneExtension() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"value\" : {\"$match\" : \"*is is st*\"}}).include(\"item.name\")");
        assertSize(queryResult, 3);

        AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResults().get(0);
        // Make sure that the extra field (item.name)is not null;
        Assert.assertTrue(row.getName() != null);
        Assert.assertTrue(row.getPath() == null);
        assertProperty(queryResult, "build.name", "ant");
    }

    /**
     * return result that contains all the domain's default fields and two extra fields
     */
    @Test
    public void findPropertiesUsingNamesExtensionWithTwoExtensions() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"value\" : {\"$match\" : \"*is is st*\"}}).include(\"item.name\",\"item.path\")");
        assertSize(queryResult, 3);

        AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResults().get(0);
        // Make sure that the extra field (item.name)is not null;
        Assert.assertTrue(row.getName() != null);
        Assert.assertTrue(row.getPath() != null);
        assertProperty(queryResult, "build.name", "ant");
    }

    /**
     * Test the sort mechanism with the include mechanism
     */
    @Test
    public void ensureSortOnIncludedFields() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"value\" : {\"$match\" : \"*is is st*\"}})." +
                        "include(\"item.name\",\"item.path\",\"item.size\")." +
                        "sort({\"$asc\": [\"item.size\"]})");
        assertSize(queryResult, 3);

        AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResults().get(0);
        // Make sure that the extra field (item.name)is not null;
        Assert.assertTrue(row.getName() != null);
        Assert.assertTrue(row.getPath() != null);
        assertProperty(queryResult, "build.name", "ant");
    }

    /**
     * Test the sort mechanism with the include domain mechanism
     */
    @Test
    public void ensureSortOnIncludedDomain() {
        AqlEagerResult<AqlBaseFullRowImpl> queryResult = aqlService.executeQueryEager(
                "items.find({\"artifact.module.build.number\" : {\"$match\" : \"2\"}}).include(\"property.*\").sort({\"$asc\" : [\"property.key\"]})");
        assertBuildArtifactsWithProp(queryResult);
    }

    /**
     * Test the sort mechanism with the include domain mechanism
     */
    @Test
    public void includePropertiesWithAtAndAsterisk() {
        AqlEagerResult<AqlBaseFullRowImpl> queryResult = aqlService.executeQueryEager(
                "items.find({\"artifact.module.build.number\" : {\"$match\" : \"2\"}})." +
                        "include(\"@*\").sort({\"$asc\" : [\"property.key\"]})");
        assertBuildArtifactsWithProp(queryResult);
    }

    private void assertBuildArtifactsWithProp(AqlEagerResult<AqlBaseFullRowImpl> queryResult) {
        assertSize(queryResult, 5);
        AqlBaseFullRowImpl result = queryResult.getResults().stream()
                .filter(row -> row.getRepo().equals("repo2"))
                .filter(row -> row.getPath().equals("a/b"))
                .filter(row -> row.getName().equals("ant-1.5.jar"))
                .findFirst()
                .get();

        Assert.assertTrue(result.getKey().equals("wednesday"));
        Assert.assertTrue(result.getValue().equals("odin"));
        // Make sure that the extra field (item.name)is not null;

        assertItem(queryResult, "repo1", "org/yossis/tools", "file2.bin", file);
        assertItem(queryResult, "repo2", "a", "ant-1.5.jar", file);
        assertItem(queryResult, "repo2", "a/b", "ant-1.5.jar", file);
        assertItem(queryResult, "repo2", "aa", "ant-1.5.jar", file);
        assertItem(queryResult, "repo2", "aa/b", "ant-1.5.jar", file);
    }

    /**
     * Test the sort mechanism with the include domain mechanism
     */
    @Test
    public void multipleResultFilter() {
        AqlEagerResult<AqlBaseFullRowImpl> queryResult = aqlService.executeQueryEager(
                "items.find()." +
                        "include(\"@build.name\",\"@build.number\")");
        assertSize(queryResult, 2);
        for (AqlBaseFullRowImpl row : queryResult.getResults()) {
            Assert.assertTrue("build.number".equals(row.getKey()) || "build.name".equals(row.getKey()));
        }
    }

    //Bug: RTFACT-12035 AQL with .include("*", "property.*") returns each property as a separated item
    @Test
    public void testItemsFindIncludeMultipleProperties() throws Exception {
        AqlLazyResult<AqlRowResult> aqlResult = aqlService.executeQueryLazy(
                "items.find({" +
                        "\"type\":\"any\"," +
                        "\"repo\":\"repo1\"," +
                        "\"path\":\"org\"," +
                        "\"name\":\"yossis\"})" +
                        ".include(\"*\",\"property.*\")");
        try (AqlJsonStreamer stream = new AqlJsonStreamer(aqlResult)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] bytes;
            while ((bytes = stream.read()) != null) {
                out.write(bytes);
            }
            Map parsedJson = JacksonReader.bytesAsClass(out.toByteArray(), Map.class);
            List results = (List) parsedJson.get("results");
            assertEquals(results.size(), 1);
            Map resultItem = (Map) results.get(0);
            assertEquals(resultItem.get("repo"), "repo1");
            assertEquals(resultItem.get("path"), "org");
            assertEquals(resultItem.get("name"), "yossis");
            List<Map> properties = (List<Map>) resultItem.get("properties");
            assertEquals(properties.size(), 3);
            String propResult = Joiner.on(",").join(
                    properties.stream().map(prop -> prop.get("key") + ":" + prop.get("value")).sorted().iterator());
            assertEquals(propResult, "jungle:value2,trance:me,yossis:value1");
        }
    }
}
