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

import org.artifactory.aql.result.AqlJsonStreamer;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.codehaus.jackson.map.ObjectMapper;
import org.jfrog.common.ResourceUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * @author Gidi Shabat
 */
@Test
public class AqlJSonResultTest extends AqlAbstractServiceTest {

    @Test
    public void findBuildWithItemsAndItemProperties() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"module.dependency.item.*\",\"module.dependency.item.property.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithItems.json");
        compareJsons(result, expectation);
    }

    @Test
    public void findBuildWithArtifacts() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"module.dependency.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithArtifacts.json");
        compareJsons(result, expectation);
    }

    @Test
    public void findBuildWithDependencies() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"module.dependency.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithDependency.json");
        compareJsons(result, expectation);
    }

    @Test
    public void findBuildWithModules() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"module.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithModules.json");
        compareJsons(result, expectation);
    }

    @Test
    public void itemsWithProperties() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "items.find().include(\"property.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/itemsWithProperties.json");
        compareJsons(result, expectation);
    }

    @Test
    public void buildWithProperties() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"property.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithProperties.json");
        compareJsons(result, expectation);
    }

    @Test
    public void modulesWithProperties() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "modules.find().include(\"property.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/modulesWithProperties.json");
        compareJsons(result, expectation);
    }

    @Test
    public void dependenciesWithStats() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "dependencies.find().include(\"item.stat.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/dependenciesWithStats.json");
        compareJsons(result, expectation);
    }

    @Test
    public void items() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "items.find()");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/items.json");
        compareJsons(result, expectation);
    }

    @Test
    public void itemsWithReleaseBundles() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "items.find().include(\"release_artifact.release\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/itemsWithReleaseBundles.json");
        compareJsons(result, expectation);
    }

    @Test
    public void itemsWithPropertiesStatisticAndArchive() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "items.find().include(\"stat.*\",\"property.*\",\"archive.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/itemWithPropertiesStatisticsAndArchive.json");
        compareJsons(result, expectation);
    }

    @Test
    public void itemsInReleaseBundle() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "items.find({\"release_artifact.release.version\":\"1.0.1\", \"release_artifact.release.name\" : \"bundle2\"}).include(\"release_artifact.release\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/itemsInReleaseBundle.json");
        compareJsons(result, expectation);
    }

    @Test
    public void itemsInReleaseBundleByDate() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "items.find({\"release_artifact.release.version\":\"1.0.0\", \"release_artifact.release.created\" : {\"$before\" : \"2w\"}}).include(\"release_artifact.path\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/itemsInReleaseBundleByDate.json");
        compareJsons(result, expectation);
    }

    @Test
    public void releaseBundleItems() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "releases.find({\"type\": \"TARGET\", \"release_artifact.item.sha256\":\"dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280\"}).include(\"release_artifact.item\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/releaseBundleItems.json");
        compareJsons(result, expectation);
    }

    @Test
    public void releaseBundleInBuilds() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "releases.find({\"release_artifact.item.dependency.module.build.name\": \"bb\", \"release_artifact.item.dependency.module.build.number\": 1}).include(\"release_artifact.item.dependency.module.build\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/releaseBundlesInBuilds.json");
        compareJsons(result, expectation);
    }

    @Test
    public void buildsWithReleaseBundleItems() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find({\"module.dependency.item.release_artifact.release.name\":\"bundle1\"}).include(\"module.dependency.item.release_artifact\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithReleaseArtifacts.json");
        compareJsons(result, expectation);
    }

    @Test
    public void buildsWithReleaseBundles() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find({\"module.dependency.item.release_artifact.release.name\":\"bundle2\"}).include(\"module.dependency.item.release_artifact.release.name\", \"module.dependency.item.release_artifact.release.signature\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithReleaseBundles.json");
        compareJsons(result, expectation);
    }

    @Test
    public void propertiesWithItems() throws IOException {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "properties.find().include(\"item.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/propertiesWithItems.json");
        compareJsons(result, expectation);
    }

    private void compareJsons(String result, String expected) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map resultMap = mapper.readValue(result, Map.class);
        Map expectedMap = mapper.readValue(expected, Map.class);
        assertTrue(areEquals(resultMap, expectedMap),
                "Results are not equal - expected:" + expected + " but got:" + result);
    }

    private boolean areEquals(Object result, Object expected) {
        if ((result == null && expected != null) || (result != null && expected == null)) {
            return false;
        }
        if (result == null) {
            return true;
        }
        if (result instanceof Map) {
            if (!(expected instanceof Map)) {
                return false;
            }
            if (((Map) result).size() != ((Map) expected).size()) {
                Object resultValue = ((Map) result).get("value");
                if ("".equals(resultValue)) {
                    ((Map) result).remove("value");
                    return areEquals(result, expected);
                }
                Object expectedValue = ((Map) expected).get("value");
                if ("".equals(expectedValue)) {
                    ((Map) expected).remove("value");
                    return areEquals(result, expected);
                }
                return false;
            }
            for (Object o : ((Map) result).keySet()) {
                if (!areEquals(((Map) result).get(o), ((Map) expected).get(o))) {
                    return false;
                }
            }
            return true;
        } else if (result instanceof List) {
            if (!(expected instanceof List) || ((List) result).size() != ((List) expected).size()) {
                return false;
            }
            Iterator resultIt = ((List) result).iterator();
            while (resultIt.hasNext()) {
                boolean foundMatch = false;
                Object nextResult = resultIt.next();
                Iterator expectedIt = ((List) expected).iterator();
                while (expectedIt.hasNext()) {
                    Object nextExpected = expectedIt.next();
                    if (areEquals(nextResult, nextExpected)) {
                        resultIt.remove();
                        expectedIt.remove();
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) {
                    return false;
                }
            }
            return ((List) result).isEmpty() && ((List) expected).isEmpty();
        } else if (result instanceof String) {
            if (!(expected instanceof String)) {
                return false;
            }
            try {
                if (ISODateTimeFormat.dateOptionalTimeParser().parseMillis((String) result) ==
                        ISODateTimeFormat.dateOptionalTimeParser().parseMillis((String) expected)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
            return ((String) result).trim().equals(((String) expected).trim());
        } else {
            return result.equals(expected);
        }
    }

    private String load(String fileName) {
        return ResourceUtils.getResourceAsString(fileName);
    }

    private String read(AqlJsonStreamer aqlStreamer) {
        StringBuilder builder = new StringBuilder();
        byte[] tempResult;
        while ((tempResult = aqlStreamer.read()) != null) {
            builder.append(new String(tempResult));
        }
        return builder.toString();
    }
}


