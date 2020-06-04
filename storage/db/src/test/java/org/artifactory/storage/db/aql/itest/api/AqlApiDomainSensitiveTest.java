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

package org.artifactory.storage.db.aql.itest.api;

import org.artifactory.api.jackson.JacksonFactory;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.api.domain.sensitive.*;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlJsonStreamer;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.*;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.aql.itest.service.AqlAbstractServiceTest.AdminPermissions;
import org.artifactory.storage.db.aql.itest.service.AqlAbstractServiceTest.EmptyRepoProvider;
import org.artifactory.storage.db.aql.itest.service.AqlServiceImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.jfrog.security.util.Pair;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.internal.AqlBase.*;
import static org.artifactory.aql.model.AqlComparatorEnum.matches;
import static org.artifactory.aql.model.AqlItemTypeEnum.file;
import static org.artifactory.aql.model.AqlItemTypeEnum.folder;
import static org.artifactory.storage.db.aql.itest.service.AqlAbstractServiceTest.dummyRepoProvider;
import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Gidi Shabat
 */
public class AqlApiDomainSensitiveTest extends DbBaseTest {

    @Autowired
    private AqlServiceImpl aqlService;

    @BeforeClass
    public void setup() {
        importSql("/sql/aql_test.sql");
        ReflectionTestUtils.setField(aqlService, "permissionProvider", new AdminPermissions());
    }

    @BeforeMethod
    public void setupTest() {
        ReflectionTestUtils.setField(aqlService, "repoProvider", dummyRepoProvider);
    }

    /*Artifacts search*/
    @Test
    public void findAllItemsTest() throws AqlException {
        AqlApiItem aql = AqlApiItem.create();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
    }

    @Test
    public void findAllSortedItemsTest() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().addSortElement(AqlApiItem.repo());
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
    }

    @Test
    public void findItemsWithSort() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.path().matches("org*")
                ).
                addSortElement(AqlApiItem.name()).
                addSortElement(AqlApiItem.repo()).
                asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 6);
        assertItem(result, 17, "repo-copy", "org/shayy/badmd5", "badmd5.jar", file);
    }

    @Test
    public void findItemsWithSha1AndMd5() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.sha1Actual().matches("*"),
                                AqlApiItem.sha1Orginal().matches("*"),
                                AqlApiItem.md5Actual().matches("*"),
                                AqlApiItem.md5Orginal().matches("*")
                        )
                ).
                addSortElement(AqlApiItem.name()).
                addSortElement(AqlApiItem.repo()).
                asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
        assertItem(result, 11, "repo1", "org/yossis/tools", "test.bin", file);
    }

    @Test
    public void findItemsWithSha2() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.sha2().matches("*")
                        )
                ).
                addSortElement(AqlApiItem.name()).
                addSortElement(AqlApiItem.repo()).
                asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
        assertItem(result, 11, "repo1", "org/yossis/tools", "test.bin", file);
    }

    @Test
    public void findItemsWithSha2AndSha1() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.sha1Actual().matches("*"),
                                AqlApiItem.sha1Orginal().matches("*"),
                                AqlApiItem.sha2().matches("*")
                        )
                ).
                addSortElement(AqlApiItem.name()).
                addSortElement(AqlApiItem.repo()).
                asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
        assertItem(result, 11, "repo1", "org/yossis/tools", "test.bin", file);
    }

    @Test
    public void findItemsWithOr() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        or(
                                and(
                                        AqlApiItem.property().key().equal("yossia"),
                                        AqlApiItem.property().key().matches("*d")
                                ),
                                AqlApiItem.property().value().matches("ant")
                        )
                ).
                addSortElement(AqlApiItem.name()).
                desc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertItem(result, 5, "repo1", "ant/ant/1.5", "ant-1.5.jar", file);
    }

    @Test
    public void findItemsWithAndOr() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(

                        and(
                                AqlApiItem.type().equal("any"),
                                freezeJoin(
                                        AqlApiItem.property().key().equal("yossis"),
                                        AqlApiItem.property().value().matches("*ue1")
                                ),
                                or(
                                        AqlApiItem.property().value().matches("value1"),
                                        and(
                                                freezeJoin(
                                                        AqlApiItem.property().key().equal("yossis"),
                                                        AqlApiItem.property().value().matches("*df")
                                                )
                                        )
                                )
                        )
                );
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertItem(result, 9, "repo1", "org", "yossis", folder);
    }

    @Test
    public void findItemsWithAndOrUsingPropertyCriteriaClause() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().property("yossis", matches, "*ue1"),
                                or(
                                        AqlApiItem.property().value().matches("value1"),
                                        AqlApiItem.property().property("yossis", matches, "*df")
                                )
                        )
                );
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertItem(result, 9, "repo1", "org", "yossis", folder);
    }

    @Test
    public void multipleCriteriaOnSamePropertyRow() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        freezeJoin(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().key().matches("jun*"),
                                AqlApiItem.property().key().matches("*gle")
                        )
                ).
                addSortElement(AqlApiItem.name()).
                addSortElement(AqlApiItem.repo());
                AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertItem(result, 9, "repo1", "org", "yossis", folder);
    }

    @Test
    public void findAllProperties() throws AqlException {
        AqlApiProperty aql = AqlApiProperty.create();
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
    }

    @Test
    public void findPropertiesWithFieldFilter() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        and(
                                AqlApiProperty.item().repo().equal("repo1"),
                                propertyResultFilter(
                                        AqlApiProperty.key().equal("yossis")
                                )
                        )
                ).
                addSortElement(AqlApiProperty.key());
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        assertProperty(result, "yossis", "pdf", null);
        assertProperty(result, "yossis", "value1", null);
    }

    @Test
    public void findPropertiesWithPropertyIdInclusion() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1' and value 'pdf'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        and(
                                AqlApiProperty.item().repo().equal("repo1"),
                                propertyResultFilter(
                                        AqlApiProperty.key().equal("yossis"),
                                        AqlApiProperty.value().equal("pdf")
                                )
                        )
                        //)
                ).include(AqlApiProperty.propertyId());
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertProperty(result, "yossis", "pdf", (long) 9);
    }

    /**
     * Usually the AQL results return all props with the same node_id of the rows that match the query filter.
     * Using propertyResultFilter will result in filtering only by props without anything related to Artifacts.
     */
    @Test
    public void findPropertiesOnlyTest() throws AqlException {
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        propertyResultFilter(AqlApiProperty.key().equal("jungle"))
                );
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1); // Validating we get only 1 result instead of the wrong result we got before (3)
        assertProperty(result, "jungle", "value2", null);
    }

    @Test
    public void singleWildCardMaching() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        and(
                                AqlApiProperty.item().repo().matches("rep?1"),
                                propertyResultFilter(
                                        AqlApiProperty.key().equal("yossis")
                                )
                        )
                ).
                addSortElement(AqlApiProperty.key());
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        assertProperty(result, "yossis", "pdf", null);
        assertProperty(result, "yossis", "value1", null);
    }

    @Test
    public void archiveEntry() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiEntry aql = AqlApiEntry.create().
                filter(
                        and(
                                AqlApiEntry.archive().item().repo().matches("rep?1"),
                                AqlApiEntry.name().matches("*")
                        )
                ).include(AqlApiEntry.archive().item().property().key()).
                addSortElement(AqlApiEntry.archive().item().property().key());
        AqlEagerResult<AqlArchiveEntryItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 18);
        assertArchive(result, "test.me", "another");
        assertArchive(result, "Test", ".");
    }

    @Test
    public void itemWithArchive() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.archive().entry().name().matches("*")
                        )
                ).
                include(AqlApiItem.archive().entry().path(), AqlApiItem.archive().entry().name()).
                addSortElement(AqlApiItem.archive().entry().path()).
                addSortElement(AqlApiItem.archive().entry().name());
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 8);
        assertItem(result, 11, "repo1", "org/yossis/tools", "test.bin", file);
    }

    @Test
    public void itemIncludeVirtualRepos() throws AqlException {
        ReflectionTestUtils.setField(aqlService, "repoProvider", new EmptyRepoProvider() {
            @Override
            public List<String> getVirtualRepoKeysContainingRepo(String repoKey) {
                assertEquals(repoKey, "repo1");
                return Arrays.asList("v1", "v2", "v3");
            }
            @Override
            public boolean isRepoPathAccepted(RepoPath repoPath) {
                return !"v2".equals(repoPath.getRepoKey());
            }
        });
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.repo().equal("repo1")
                        )
                ).
                include(AqlApiItem.repo(), AqlApiItem.path(), AqlApiItem.name(), AqlApiItem.virtualRepos());
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 4);
        assertItem(result, 11, "repo1", "org/yossis/tools", "test.bin", file);
        result.getResults().forEach(item -> assertEquals(item.getVirtualRepos(), new String[] {"v1", "v3"})
        );
    }

    @Test(enabled = false)
    public void underscoreEscape() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        and(
                                AqlApiProperty.item().repo().matches("rep_1"),
                                propertyResultFilter(
                                        AqlApiProperty.key().equal("yossis")
                                )
                        )
                ).
                addSortElement(AqlApiProperty.key());
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 0);
    }

    @Test(enabled = true)
    public void nullUsage() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().key().equal(null)
                        )
                ).
                include(AqlApiItem.property().key(), AqlApiItem.property().value()).
                addSortElement(AqlApiItem.property().key()).asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 19);
    }

    @Test(enabled = true)
    public void notNullUsage() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().key().notEquals((String) null)
                        )
                ).
                include(AqlApiItem.property().key(), AqlApiItem.property().value()).
                addSortElement(AqlApiItem.property().key()).asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 31);
    }

    @Test
    public void doNotHaveProperty() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().key().notMatches("*")
                        )
                ).
                include(AqlApiItem.property().key(), AqlApiItem.property().value()).
                addSortElement(AqlApiItem.property().key()).asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 19);
    }

    @Test(enabled = false)
    public void percentEscape() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        and(
                                AqlApiProperty.item().repo().matches("rep%1"),
                                propertyResultFilter(
                                        AqlApiProperty.key().equal("yossis")
                                )
                        )
                ).
                addSortElement(AqlApiProperty.key());
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 0);
    }

    @Test
    public void queryWithDateTest() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        AqlApiProperty.item().created().greater(new DateTime(0))
                ).
                addSortElement(AqlApiProperty.key());
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
    }

    @Test
    public void buildArtifactChecksums() {
        AqlApiItem itemByMd5 = AqlApiItem.create()
                .filter(
                    AqlApiItem.artifact().md5().equal("302a360ecad98a34b59863c1e65bcf71")
                );
        AqlApiItem itemBySha1 = AqlApiItem.create()
                .filter(
                        AqlApiItem.artifact().sha1().equal("dcab88fc2a043c2479a6de676a2f8179e9ea2167")
                );
        // Kicked out build artifacts sha2 from db because of performance
        /*AqlApiItem itemBySha2 = AqlApiItem.create()
                .filter(
                        AqlApiItem.artifact().sha2().equal("dbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbd")
                );*/
        AqlEagerResult<AqlItem> resultByMd5 = aqlService.executeQueryEager(itemByMd5);
        AqlEagerResult<AqlItem> resultBySha1 = aqlService.executeQueryEager(itemBySha1);
        //AqlEagerResult<AqlItem> resultBySha2 = aqlService.executeQueryEager(itemBySha2);
        assertSize(resultByMd5, 1);
        assertItem(resultByMd5, 11, "repo1", "org/yossis/tools", "test.bin", file);
        assertSize(resultBySha1, 1);
        assertItem(resultBySha1, 5, "repo1", "ant/ant/1.5", "ant-1.5.jar", file);
        //assertSize(resultBySha2, 1);
        //assertItem(resultBySha2, 12, "repo1", "org/yossis/tools", "file2.bin", file);
    }

    @Test
    public void nonDistinctBuildArtifactChecksums() {
        AqlApiItem itemByMd5 = AqlApiItem.create()
                .distinct(false)
                .filter(
                        AqlApiItem.artifact().md5().equal("302a360ecad98a34b59863c1e65bcf71")
                );
        AqlEagerResult<AqlItem> resultByMd5 = aqlService.executeQueryEager(itemByMd5);
        assertSize(resultByMd5, 2);
        assertItem(resultByMd5, 11, "repo1", "org/yossis/tools", "test.bin", file);
    }

    @Test
    public void buildDependencyChecksums() {
        AqlApiItem itemByMd5 = AqlApiItem.create()
                .filter(
                        AqlApiItem.artifact().module().dependecy().md5().equal("502a360ecad98a34b59863c1e6accf71")
                );
        AqlApiItem itemBySha1 = AqlApiItem.create()
                .filter(
                        AqlApiItem.artifact().module().dependecy().sha1().equal("dcab88fc2a043c2479a6de676a2f8179e9ea2167")
                );
        // Kicked out build artifacts sha2 from db because of performance
        /*AqlApiItem itemBySha2 = AqlApiItem.create()
                .filter(
                        AqlApiItem.artifact().module().dependecy().sha2().equal("dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd")
                );*/
        AqlEagerResult<AqlItem> resultByMd5 = aqlService.executeQueryEager(itemByMd5);
        AqlEagerResult<AqlItem> resultBySha1 = aqlService.executeQueryEager(itemBySha1);
        //AqlEagerResult<AqlItem> resultBySha2 = aqlService.executeQueryEager(itemBySha2);
        assertSize(resultByMd5, 1);
        assertItem(resultByMd5, 5, "repo1", "ant/ant/1.5", "ant-1.5.jar", file);
        assertSize(resultBySha1, 1);
        assertItem(resultBySha1, 5, "repo1", "ant/ant/1.5", "ant-1.5.jar", file);
        //assertSize(resultBySha2, 3);
        //assertItem(resultBySha2, 17, "repo-copy", "org/shayy/badmd5", "badmd5.jar", file);
        //assertItem(resultBySha2, 16, "repo-copy", "org/shayy/trustme", "trustme.jar", file);
        //assertItem(resultBySha2, 11, "repo1", "org/yossis/tools", "test.bin", file);
    }

    @Test
    public void lazyResultJsonInMemoryStream() throws Exception {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem item = AqlApiItem.create();
        item.filter(
                AqlApiItem.artifact().module().build().name().matches("ba")
        );

        String result = executeAndGetResult(item);
        assertTrue(result.contains("\"size\" : 43434"));
        assertTrue(result.contains("\"repo\" : \"repo1\""));
        assertTrue(result.contains("\"actual_md5\" : \"302a360ecad98a34b59863c1e65bcf71\""));
        assertTrue(result.contains("\"created_by\" : \"yossis-1\""));
        assertTrue(result.contains("\"depth\" : 4"));
        assertTrue(result.contains("\"original_md5\" : \"302a360ecad98a34b59863c1e65bcf71\""));
        assertTrue(result.contains("\"actual_sha1\" : \"acab88fc2a043c2479a6de676a2f8179e9ea2167\""));
        assertTrue(result.contains("\"sha256\" : \"bbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbb\""));
        assertTrue(result.contains("\"path\" : \"org/yossis/tools\""));
    }

    @Test
    public void lazyResultStream() throws Exception {
        AqlApiItem item = AqlApiItem.create();
        item.filter(
                //or(
                AqlApiItem.artifact().module().build().name().matches("ba")
                //)
        ).limit(2000);

        String result = executeAndGetResult(item);
        try (JsonParser jsonParser = JacksonFactory.createJsonParser(result.getBytes())) {
            JsonNode jsonNode = jsonParser.readValueAsTree().get("results");
            String[] results = jsonNode.toString().split("},\\{");
            assertEquals(results.length, 8);
            JsonNode node = findNodeWithValue(jsonNode, pair("path", "org/shayy/badmd5"));
            assertResultJsonNode(node.toString(), "repo-copy", "org/shayy/badmd5", "badmd5.jar",
                    "43434", "yossis-1", "4", "502a360ecad98a34b59863c1e6accf71",
                    "dddd88fc2a043c2479a6de676a2f7179e9eaddac", "502a360ecad98a34b59863c1e65bcf32",
                    "NO_ORIG", "dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60ddac");
            node = findNodeWithValue(jsonNode, pair("path", "org/shayy/trustme"));
            assertResultJsonNode(node.toString(), "repo-copy", "org/shayy/trustme", "trustme.jar",
                    "43434", "yossis-1", "4", "502a360ecad98a34b59863c1e6accf71",
                    "dddd88fc2a043c2479a6de676a2f7179e9eaddac", "NO_ORIG",
                    "NO_ORIG", "dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60ddac");
            node = findNodeWithValue(jsonNode, pair("path","org/yossis/tools"), pair("name", "file2.bin"));
            assertResultJsonNode(node.toString(), "repo1", "org/yossis/tools", "file2.bin",
                    "43434", "yossis-1", "4", "402a360ecad98a34b59863c1e65bcf71",
                    "bbbb88fc2a043c2479a6de676a2f8179e9eabbbb", "402a360ecad98a34b59863c1e65bcf71",
                    "bcab88fc2a043c2479a6de676a2f8179e9ea2167", "dbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbd");
            node = findNodeWithValue(jsonNode, pair("path","org/yossis/tools"), pair("name", "test.bin"));
            assertResultJsonNode(node.toString(), "repo1", "org/yossis/tools", "test.bin",
                    "43434", "yossis-1", "4", "302a360ecad98a34b59863c1e65bcf71",
                    "acab88fc2a043c2479a6de676a2f8179e9ea2167", "302a360ecad98a34b59863c1e65bcf71",
                    "acab88fc2a043c2479a6de676a2f8179e9ea2167", "bbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbb");
            node = findNodeWithValue(jsonNode, pair("path", "a"));
            assertResultJsonNode(node.toString(), "repo2", "a", "ant-1.5.jar",
                    "716139", "yossis-2201", "4", "503a360ecad98a34b59863c1e6accf71",
                    "dddd89fc2a043c2479a6de676a2f7179e9eaddac", "503a360ecad98a34b59863c1e6accf71",
                    "dddd89fc2a043c2479a6de676a2f7179e9eaddac", "addd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60fbac");
            node = findNodeWithValue(jsonNode, pair("path", "a/b"));
            assertResultJsonNode(node.toString(), "repo2", "a/b", "ant-1.5.jar",
                    "716139", "yossis-2201", "4", "503a360ecad98a34b59863c1e6accf71",
                    "dddd89fc2a043c2479a6de676a2f7179e9eaddac", "503a360ecad98a34b59863c1e6accf71",
                    "dddd89fc2a043c2479a6de676a2f7179e9eaddac", "addd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60fbac");
            node = findNodeWithValue(jsonNode, pair("path", "aa"));
            assertResultJsonNode(node.toString(), "repo2", "aa", "ant-1.5.jar",
                    "716139", "yossis-2201", "4", "503a360ecad98a34b59863c1e6accf71",
                    "dddd89fc2a043c2479a6de676a2f7179e9eaddac", "503a360ecad98a34b59863c1e6accf71",
                    "dddd89fc2a043c2479a6de676a2f7179e9eaddac", "addd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60fbac");
            node = findNodeWithValue(jsonNode, pair("path", "aa/b"));
            assertResultJsonNode(node.toString(), "repo2", "aa/b", "ant-1.5.jar",
                    "716139", "yossis-2201", "4", "503a360ecad98a34b59863c1e6accf71",
                    "dddd89fc2a043c2479a6de676a2f7179e9eaddac", "503a360ecad98a34b59863c1e6accf71",
                    "dddd89fc2a043c2479a6de676a2f7179e9eaddac", "addd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60fbac");
        }
        assertTrue(result.contains("\"total\" : 8"));
        assertTrue(result.contains("\"limit\" : 2000"));
    }

    private Pair<String, String> pair(String key, String value) {
        return new Pair<>(key, value);
    }

    @Test
    public void lazyResultStreamWithLimit() throws Exception {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem item = AqlApiItem.create();
        item.filter(
                AqlApiItem.and(
                        AqlApiItem.artifact().module().build().name().matches("ba"),
                        AqlApiItem.repo().matches("repo-copy")
                )
        ).limit(2);

        String result = executeAndGetResult(item);
        try (JsonParser parser = JacksonFactory.createJsonParser(result.getBytes())) {
            parser.nextToken(); //START_OBJECT
            parser.nextToken(); //START_ARRAY

            parser.nextToken(); //START_OBJECT
            JsonNode resultNode = parser.readValueAsTree();
            String[] results = resultNode.toString().split("},\\{");
            assertEquals(results.length, 2);
            JsonNode node = findNodeWithValue(resultNode, pair("path", "org/shayy/badmd5"));
            assertResultJsonNode(node.toString(), "repo-copy", "org/shayy/badmd5", "badmd5.jar",
                    "43434", "yossis-1", "4", "502a360ecad98a34b59863c1e6accf71",
                    "dddd88fc2a043c2479a6de676a2f7179e9eaddac", "502a360ecad98a34b59863c1e65bcf32",
                    "NO_ORIG", "dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60ddac");
            node = findNodeWithValue(resultNode, pair("path", "org/shayy/trustme"));
            assertResultJsonNode(node.toString(), "repo-copy", "org/shayy/trustme", "trustme.jar",
                    "43434", "yossis-1", "4", "502a360ecad98a34b59863c1e6accf71",
                    "dddd88fc2a043c2479a6de676a2f7179e9eaddac", "NO_ORIG",
                    "NO_ORIG", "dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60ddac");
        }
        assertTrue(result.contains("\"total\" : 2"));
        assertTrue(result.contains("\"limit\" : 2"));
    }

    private void assertResultJsonNode(String resultNode, String repo, String path, String name, String size,
            String created, String depth, String md5Act, String sha1Act, String md5Orig, String sha1Orig, String sha2) {
        assertParameterInJson(resultNode, "\"size\":" + size + "");
        assertParameterInJson(resultNode, "\"repo\":\"" + repo + "\"");
        assertParameterInJson(resultNode, "\"path\":\"" + path + "\"");
        assertParameterInJson(resultNode, "\"name\":\"" + name + "\"");
        assertParameterInJson(resultNode, "\"original_md5\":\"" + md5Orig + "\"");
        assertParameterInJson(resultNode, "\"actual_md5\":\"" + md5Act + "\"");
        assertParameterInJson(resultNode, "\"original_sha1\":\"" + sha1Orig + "\"");
        assertParameterInJson(resultNode, "\"actual_sha1\":\"" + sha1Act + "\"");
        assertParameterInJson(resultNode, "\"sha256\":\"" + sha2 + "\"");
        assertParameterInJson(resultNode, "\"created_by\":\"" + created + "\"");
        assertParameterInJson(resultNode, "\"depth\":" + depth + "");
    }

    private void assertParameterInJson(String resultNode, String parameter) {
        assertTrue(resultNode.contains(parameter),
                "Expected [" + parameter + "] not found in Json <" + resultNode + ">");
    }

    private String executeAndGetResult(AqlApiItem item) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (AqlLazyResult<AqlItem> result = aqlService.executeQueryLazy(item);
             AqlJsonStreamer streamResult = new AqlJsonStreamer(result)) {

            byte[] read = streamResult.read();
            while (read != null) {
                builder.append(new String(read));
                read = streamResult.read();
            }
        }
        return builder.toString();
    }

    @Test
    public void findArtifactsBiggerThan() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                );
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 5);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }

    @Test
    public void findArtifactsBiggerThanWithLimit() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .limit(2);
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }

    @Test
    public void findArtifactsBiggerThanWithOffset() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .offset(4);
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }

    @Test
    public void findArtifactsBiggerThanWithOffsetAndLimit() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .limit(1)
                .offset(1);
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }


    @Test
    public void findArtifactsUsinfInclude() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .limit(2).include(AqlApiItem.created());
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }

    @Test
    public void usageOffTypeEqualAll() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.type().equal("any")
                )
                .include(AqlApiItem.created());
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 26);
    }

    @Test
    public void findBuild() throws AqlException {
        AqlApiBuild aql = AqlApiBuild.create().
                filter(
                        AqlApiBuild.url().equal("http://myserver/jenkins/bb/1")
                );
        AqlEagerResult<AqlBuild> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertBuild(result, "1", "bb");
    }

    @Test
    public void findBuildByStartedTime() throws AqlException {
        AqlApiBuild aql = AqlApiBuild.create().
                filter(
                        AqlApiBuild.started().greater(0L)
                );
        AqlEagerResult<AqlBuild> result = aqlService.executeQueryEager(aql);
        assertSize(result, 5);

        aql = AqlApiBuild.create().
                filter(
                        AqlApiBuild.started().greaterEquals(1349004000000L)
                );
        result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertBuild(result, "3", "ba");
        assertThat(result.getResult(0).getBuildStarted().getTime()).isGreaterThanOrEqualTo(1349004000000L);
    }

    @Test
    public void findAqlApiBuildProperty() throws AqlException {
        AqlApiBuildProperty aql = AqlApiBuildProperty.create().
                filter(
                        AqlApiBuildProperty.value().matches("*")
                );
        AqlEagerResult<AqlBuildProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 6);
        assertBuildProperty(result, "start", "0");
        assertBuildProperty(result, "start", "1");
        assertBuildProperty(result, "start", "4");
        assertBuildProperty(result, "status", "bad");
        assertBuildProperty(result, "status", "good");
        assertBuildProperty(result, "status", "not-too-bad");
    }

    @Test
    public void findAqlApiDependency() throws AqlException {
        AqlApiDependency aql = AqlApiDependency.create().
                filter(
                        AqlApiDependency.name().matches("*")
                );
        AqlEagerResult<AqlBuildDependency> result = aqlService.executeQueryEager(aql);
        assertSize(result, 5);
        assertDependency(result, "ba1mod3-art1", "dll");
    }

    @Test
    public void findAqlApiStatistics() throws AqlException {
        AqlApiStatistic aql = AqlApiStatistic.create().
                filter(
                        AqlApiStatistic.downloads().greater(1)
                );
        AqlEagerResult<AqlStatistics> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        assertStatistic(result, 9, "yossis");
        assertStatistic(result, 15, "yossis");
    }

    @Test
    public void findAqlApiRemoteStatistics() {
        AqlApiStatistic aql = AqlApiStatistic.create().
                filter(
                        AqlApiStatistic.remoteDownloads().greater(1)
                )
                .include(
                        AqlApiStatistic.remoteDownloads(),
                        AqlApiStatistic.remoteDownloadBy(),
                        AqlApiStatistic.remoteOrigin(),
                        AqlApiStatistic.remotePath()
                );
        AqlEagerResult<AqlStatistics> result = aqlService.executeQueryEager(aql);
        // Returns 2 although there are 3 matching rows because distinct merges two identical rows.
        // Currently we don't see a valid use case where it matters.
        assertSize(result, 2);
        assertRemoteStatistic(result, 17, "dodo", "remote-host1", "path/a");
        assertRemoteStatistic(result, 11, "dodo", "remote-host2", "path/b");
    }

    @Test
    public void findArtifact() throws AqlException {
        AqlApiArtifact aql = AqlApiArtifact.create().
                filter(
                        AqlApiArtifact.type().equal("dll")
                );
        AqlEagerResult<AqlBuildArtifact> result = aqlService.executeQueryEager(aql);
        assertSize(result, 5);
        assertArtifact(result, "ba1mod1-art1", "dll");
    }

    @Test
    public void itemsByBuildPromotions() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.artifact().module().build().promotion().userName().equal("me")
                );
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 0);

    }

    @Test
    public void buildPromotionsByItem() throws AqlException {
        AqlApiBuildPromotion aql = AqlApiBuildPromotion.create().
                filter(
                        AqlApiBuildPromotion.and(
                            AqlApiBuildPromotion.build().module().artifact().item().name().matches("*"),
                            AqlApiBuildPromotion.userName().matches("*")
                        )
                );
        AqlEagerResult<AqlBuildPromotion> result = aqlService.executeQueryEager(aql);
        assertSize(result, 0);
    }

    @Test
    public void buildPromotions() throws AqlException {
        AqlApiBuildPromotion aql = AqlApiBuildPromotion.create().
                filter(
                        AqlApiBuildPromotion.and(
                                AqlApiBuildPromotion.userName().matches("*"),
                                AqlApiBuildPromotion.repo().notEquals((String)null)
                        )
                );
        AqlEagerResult<AqlBuildPromotion> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        assertBuildPromotion(result, "promoter", "me");
    }

    @Test
    public void findItemsUsingIncludeStatistics() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .limit(2).include(AqlApiItem.created(), AqlApiItem.statistic().downloads());
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }

    @Test
    public void findAllReleases() throws AqlException {
        AqlApiReleaseBundle aql = AqlApiReleaseBundle.create();
        AqlEagerResult<AqlReleaseBundle> result = aqlService.executeQueryEager(aql);
        assertSize(result, 3);
    }

    @Test
    public void findAllReleasesByArtifactPath() throws AqlException {
        AqlApiReleaseBundle aql = AqlApiReleaseBundle.create()
                .filter( // test dynamic fields
                        AqlApiReleaseBundle.releaseArtifact().release().releaseArtifact().repoPath().equal("repo1:ant/ant/1.5/ant-1.5.jar")
                );
        AqlEagerResult<AqlReleaseBundle> result = aqlService.executeQueryEager(aql);
        assertSize(result, 3);
    }

    @Test
    public void findAllReleaseArtifacts() throws AqlException {
        AqlApiReleaseBundleFile aql = AqlApiReleaseBundleFile.create();
        AqlEagerResult<AqlReleaseBundleFile> result = aqlService.executeQueryEager(aql);
        assertSize(result, 5);
    }

    @Test
    public void findAllReleaseArtifactsFullInclude() throws AqlException {
        AqlBase aql = AqlApiReleaseBundleFile.create()
                .filter(
                        AqlApiReleaseBundleFile.release().name().equal("bundle1")
                )
                .include(
                        AqlApiReleaseBundleFile.release().name(),
                        AqlApiReleaseBundleFile.release().id(),
                        AqlApiReleaseBundleFile.release().signature(),
                        AqlApiReleaseBundleFile.release().bundleType(),
                        AqlApiReleaseBundleFile.release().status(),
                        AqlApiReleaseBundleFile.release().version(),
                        AqlApiReleaseBundleFile.release().created()
                );
        AqlEagerResult<AqlBaseFullRowImpl> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        AqlBaseFullRowImpl fullRow = result.getResult(0);
        assertThat(fullRow.getReleaseName()).isEqualTo("bundle1");
        assertThat(fullRow.getReleaseVersion()).isEqualTo("1.0.0");
        assertThat(fullRow.getRepoPath()).isEqualTo("repo1:ant/ant/1.5/ant-1.5.jar");
        assertThat(fullRow.getReleaseSignature()).isEqualTo("sig1");
        assertThat(fullRow.getReleaseType()).isEqualTo("TARGET");
        assertThat(fullRow.getReleaseId()).isEqualTo(1L);
        assertThat(fullRow.getReleaseArtifactNodeId()).isEqualTo(5L);
        assertThat(fullRow.getReleaseId()).isEqualTo(1L);
        assertThat(fullRow.getReleaseArtifactId()).isEqualTo(1L);
        assertThat(fullRow.getReleaseCreated().toString()).startsWith("Sun Sep 30 1");
        assertThat(fullRow.getReleaseStatus()).isEqualTo("STATUS");
    }

    @Test
    public void findReleasesMatchNameWithInclude() throws AqlException {
        AqlBase aql = AqlApiReleaseBundle.create()
                .filter(
                        AqlApiReleaseBundle.name().matches("bundle*")
                )
                .include(AqlApiReleaseBundle.releaseArtifact().repoPath())
                .limit(2);
        AqlEagerResult<AqlBaseFullRowImpl> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        assertThat(result.getResults().get(0).getRepoPath()).isEqualTo("repo1:ant/ant/1.5/ant-1.5.jar");
    }

    @Test
    public void findReleaseArtifactsByReleaseName() throws AqlException {
        AqlApiReleaseBundleFile aql = AqlApiReleaseBundleFile.create()
                .filter(
                        AqlApiReleaseBundleFile.release().name().equal("bundle2")
                );
        AqlEagerResult<AqlReleaseBundleFile> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
    }

    @Test
    public void findReleaseArtifactsMatchReleaseName() throws AqlException {
        AqlBase aql = AqlApiReleaseBundleFile.create()
                .filter(
                        AqlApiReleaseBundleFile.release().name().matches("bundle*")
                )
                .include(AqlApiReleaseBundleFile.release().name());
        AqlEagerResult<AqlBaseFullRowImpl> result = aqlService.executeQueryEager(aql);
        assertSize(result, 3);
        assertThat(result.getResult(0).getReleaseName()).matches("bundle\\d");
        assertThat(result.getResult(1).getReleaseName()).matches("bundle\\d");
        assertThat(result.getResult(2).getReleaseName()).matches("bundle\\d");
    }

    @Test
    public void findReleaseArtifactWithFullRow() throws AqlException {
        AqlBase aql = AqlApiReleaseBundle.create()
                .filter(
                        AqlApiReleaseBundle.name().matches("bundle2")
                )
                .include(AqlApiReleaseBundle.releaseArtifact().repoPath());
        AqlEagerResult<AqlBaseFullRowImpl> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        AqlBaseFullRowImpl fullRow = result.getResult(0);
        assertThat(fullRow.getReleaseName()).isEqualTo("bundle2");
        assertThat(fullRow.getReleaseId()).isEqualTo(2L);
        assertThat(fullRow.getReleaseSignature()).isEqualTo("sig2");
        assertThat(fullRow.getReleaseType()).isEqualTo("TARGET");
        assertThat(fullRow.getReleaseVersion()).isEqualTo("1.0.1");
        assertThat(fullRow.getReleaseCreated().toString()).startsWith("Sun Sep 30 1");
        List<String> repoPaths = result.getResults().stream()
                .map(AqlBaseFullRowImpl::getRepoPath)
                .collect(Collectors.toList());
        assertThat(repoPaths).containsOnly("repo1:ant/ant/1.5/ant-1.5.jar", "repo1:org/yossis/tools/test.bin");
    }

    @Test
    public void findReleaseWithFullRow() throws AqlException {
        AqlBase aql = AqlApiReleaseBundleFile.create()
                .filter(
                        AqlApiReleaseBundleFile.release().name().matches("bundle1")
                );
        AqlEagerResult<AqlBaseFullRowImpl> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        AqlBaseFullRowImpl fullRow = result.getResult(0);
        assertThat(fullRow.getReleaseArtifactId()).isEqualTo(1L);
        assertThat(fullRow.getReleaseArtifactNodeId()).isEqualTo(5L);
        assertThat(fullRow.getReleaseArtifactReleaseId()).isEqualTo(1L);
        assertThat(fullRow.getRepoPath()).isEqualTo("repo1:ant/ant/1.5/ant-1.5.jar");
    }

    @Test
    public void findReleaseArtifactsByNodeSha2() throws AqlException {
        AqlApiReleaseBundleFile aql = AqlApiReleaseBundleFile.create()
                .filter(
                        AqlApiReleaseBundleFile.item().sha2().equal("dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280")
                );
        AqlEagerResult<AqlReleaseBundleFile> result = aqlService.executeQueryEager(aql);
        assertSize(result, 3);
    }

    @Test
    public void findItemsByRelease() throws AqlException {
        AqlApiItem aql = AqlApiItem.create()
                .filter(
                        AqlApiItem.releaseArtifact().release().name().equal("bundle1")
                );
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
    }

    private void assertSize(AqlEagerResult queryResult, int i) {
        assertEquals(queryResult.getSize(), i);
    }

    private void assertItem(AqlEagerResult queryResult, long id, String repo, String path, String name, AqlItemTypeEnum type) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResult(j);
            if (row.getNodeId() == id &&
                    row.getRepo().equals(repo) && row.getName().equals(name) &&
                    row.getPath().equals(path) && row.getType() == type) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertProperty(AqlEagerResult queryResult, String key, String value, Long propertyId) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlProperty row = (AqlProperty) queryResult.getResult(j);
            boolean validId = propertyId == null || row.getPropertyId().equals(propertyId);
            if (row.getKey().equals(key) && row.getValue().equals(value) && validId) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertDependency(AqlEagerResult queryResult, String name, String type) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildDependency row = (AqlBuildDependency) queryResult.getResult(j);
            if (row.getBuildDependencyName().equals(name) &&
                    row.getBuildDependencyType().equals(type)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertBuild(AqlEagerResult queryResult, String buildNumber, String buildName) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuild row = (AqlBuild) queryResult.getResult(j);
            if (row.getBuildName().equals(buildName) &&
                    row.getBuildNumber().equals(buildNumber)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertArtifact(AqlEagerResult queryResult, String name, String type) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildArtifact row = (AqlBuildArtifact) queryResult.getResult(j);
            if (row.getBuildArtifactName().equals(name) &&
                    row.getBuildArtifactType().equals(type)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertArchive(AqlEagerResult queryResult, String name, String path) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlArchiveEntryItem row = (AqlArchiveEntryItem) queryResult.getResult(j);
            if (row.getEntryName().equals(name) &&
                    row.getEntryPath().equals(path)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertStatistic(AqlEagerResult queryResult, int downloads, String downloadBy) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlStatistics row = (AqlStatistics) queryResult.getResult(j);
            if (row.getDownloads() == downloads &&
                    row.getDownloadedBy().equals(downloadBy)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertRemoteStatistic(AqlEagerResult queryResult, int downloads, String downloadBy, String origin, String path) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlStatistics row = (AqlStatistics) queryResult.getResult(j);
            if (row.getRemoteDownloads() == downloads && row.getRemoteDownloadedBy().equals(downloadBy) &&
                    row.getRemoteOrigin().equals(origin) && row.getRemotePath().equals(path)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertBuildProperty(AqlEagerResult queryResult, String key, String value) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildProperty row = (AqlBuildProperty) queryResult.getResult(j);
            if (row.getBuildPropKey().equals(key) &&
                    row.getBuildPropValue().equals(value)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertBuildPromotion(AqlEagerResult queryResult, String createdBy, String userName) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildPromotion row = (AqlBuildPromotion) queryResult.getResult(j);
            if (row.getBuildPromotionCreatedBy().equals(createdBy) &&
                    row.getBuildPromotionUser().equals(userName)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private JsonNode findNodeWithValue(JsonNode jsonNode, Pair<String, String> ... keyValuePairs) throws IOException {
        Iterator<JsonNode> results = jsonNode.getElements();
        boolean found;
        while (results.hasNext()) {
            found = true;
            JsonNode next = results.next();
            for (Pair<String, String> keyValue: keyValuePairs) {
                if (!next.get(keyValue.getFirst()).asText().equals(keyValue.getSecond())) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return next;
            }
        }
        return null;
    }
}