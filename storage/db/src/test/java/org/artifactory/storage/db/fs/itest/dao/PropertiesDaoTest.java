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

package org.artifactory.storage.db.fs.itest.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.db.fs.dao.PropertiesDao;
import org.artifactory.storage.db.fs.entity.NodeProperty;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.artifactory.common.ConstantValues.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Tests {@link org.artifactory.storage.db.fs.dao.PropertiesDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class PropertiesDaoTest extends DbBaseTest {

    @Autowired
    private PropertiesDao propsDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void testGetNodesPropertiesLessThanChunk() throws SQLException {
        int originalChunkValue = propertiesSearchChunkSize.getInt();
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(propertiesSearchChunkSize.getPropertyName(), "100");
        try {
            List<Long> nodeIds = Arrays.asList(1L, 2L, 3L);
            JdbcHelper jdbcHelperMock = mock(JdbcHelper.class);

            doAnswer(invocation -> {
                List<Long> chunkList = invocation.getArgument(2);
                assertEquals(chunkList, nodeIds);
                ResultSet resultSetMock = mock(ResultSet.class);
                when(resultSetMock.next()).thenReturn(true, false);
                when(resultSetMock.getLong(1)).thenReturn(1L);
                when(resultSetMock.getString(2)).thenReturn("propValue");
                return resultSetMock;
            }).when(jdbcHelperMock).executeSelect(anyString(), eq("npm.disttag"), eq(nodeIds));

            PropertiesDao propertiesDao = new PropertiesDao(jdbcHelperMock);
            Map<Long, Set<String>> nodesProperties = propertiesDao.getNodesProperties(nodeIds, "npm.disttag");
            assertEquals(nodesProperties.get(1L).size(), 1);
            assertTrue(nodesProperties.get(1L).contains("propValue"));
            verify(jdbcHelperMock, times(1)).executeSelect(anyString(), anyString(), anyList());
        } finally {
            ArtifactoryHome.get().getArtifactoryProperties()
                    .setProperty(propertiesSearchChunkSize.getPropertyName(), String.valueOf(originalChunkValue));
        }
    }

    public void testGetNodesPropertiesEqualsToChunk() throws SQLException {
        int originalChunkValue = propertiesSearchChunkSize.getInt();
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(propertiesSearchChunkSize.getPropertyName(), "3");
        try {
            List<Long> nodeIds = Arrays.asList(1L, 2L, 3L);
            JdbcHelper jdbcHelperMock = mock(JdbcHelper.class);

            doAnswer(invocation -> {
                List<Long> chunkList = invocation.getArgument(2);
                assertEquals(chunkList, nodeIds);
                ResultSet resultSetMock = mock(ResultSet.class);
                when(resultSetMock.next()).thenReturn(true, false);
                when(resultSetMock.getLong(1)).thenReturn(1L);
                when(resultSetMock.getString(2)).thenReturn("propValue");
                return resultSetMock;
            }).when(jdbcHelperMock).executeSelect(anyString(), eq("npm.disttag"), eq(nodeIds));

            PropertiesDao propertiesDao = new PropertiesDao(jdbcHelperMock);
            Map<Long, Set<String>> nodesProperties = propertiesDao.getNodesProperties(nodeIds, "npm.disttag");
            assertEquals(nodesProperties.get(1L).size(), 1);
            assertTrue(nodesProperties.get(1L).contains("propValue"));
            verify(jdbcHelperMock, times(1)).executeSelect(anyString(), anyString(), anyList());
        } finally {
            ArtifactoryHome.get().getArtifactoryProperties()
                    .setProperty(propertiesSearchChunkSize.getPropertyName(), String.valueOf(originalChunkValue));
        }
    }

    public void testGetNodesPropertiesHigherThanChunk() throws SQLException {
        int originalChunkValue = propertiesSearchChunkSize.getInt();
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(propertiesSearchChunkSize.getPropertyName(), "1");
        try {
            List<Long> nodeIds = Arrays.asList(1L, 2L, 3L);
            JdbcHelper jdbcHelperMock = mock(JdbcHelper.class);

            doAnswer(invocation -> {
                List<Long> chunkList = invocation.getArgument(2);
                assertEquals(chunkList.size(), 1);
                ResultSet resultSetMock = mock(ResultSet.class);
                when(resultSetMock.next()).thenReturn(true, false);
                when(resultSetMock.getLong(1)).thenReturn(1L);
                when(resultSetMock.getString(2)).thenReturn("propValue");
                return resultSetMock;
            }).when(jdbcHelperMock).executeSelect(anyString(), eq("npm.disttag"), anyList());

            PropertiesDao propertiesDao = new PropertiesDao(jdbcHelperMock);
            Map<Long, Set<String>> nodesProperties = propertiesDao.getNodesProperties(nodeIds, "npm.disttag");
            assertEquals(nodesProperties.get(1L).size(), 1);
            assertTrue(nodesProperties.get(1L).contains("propValue"));
            verify(jdbcHelperMock, times(3)).executeSelect(anyString(), anyString(), anyList());
        } finally {
            ArtifactoryHome.get().getArtifactoryProperties()
                    .setProperty(propertiesSearchChunkSize.getPropertyName(), String.valueOf(originalChunkValue));
        }
    }

    public void testGetNodesPropertiesEmptyNodeIdList() throws SQLException {
        Assert.assertTrue(propsDao.getNodesProperties(new ArrayList<>(), "npm.disttag").isEmpty());
    }


    public void hasPropertiesNodeWithProperties() throws SQLException {
        boolean result = propsDao.hasNodeProperties(5);
        assertTrue(result, "Node expected to hold properties");
    }

    public void hasPropertiesNodeWithoutProperties() throws SQLException {
        boolean result = propsDao.hasNodeProperties(1);
        assertFalse(result, "Node is not expected to hold properties");
    }

    public void hasPropertiesNodeNotExist() throws SQLException {
        boolean result = propsDao.hasNodeProperties(5478939);
        assertFalse(result, "Node that doesn't exist is not expected to hold properties");
    }

    public void getPropertiesNodeWithProperties() throws SQLException {
        List<NodeProperty> result = propsDao.getNodeProperties(5);
        assertNotNull(result);
        assertEquals(result.size(), 2);
        for (NodeProperty property : result) {
            assertEquals(property.getNodeId(), 5, "All results should be with the same node id");
        }

        NodeProperty buildName = getById(1, result);
        assertEquals(buildName.getPropId(), 1);
        assertEquals(buildName.getPropKey(), "build.name");
        assertEquals(buildName.getPropValue(), "ant");
    }

    public void testGetNodesPropertiesByNodeId() throws SQLException {
        List<Long> nodeIds = Lists.newArrayList(5L);
        Map<Long, Set<String>> nodesProperties = propsDao.getNodesProperties(nodeIds, "build.name");
        assertNotNull(nodesProperties);
        assertFalse(nodesProperties.isEmpty());
        assertEquals(nodesProperties.get(5L).size(), 1);
        assertTrue(nodesProperties.get(5L).contains("ant"));
    }

    public void getPropertiesNodeWithEmptyProperties() throws SQLException {
        List<NodeProperty> result = propsDao.getNodeProperties(14);
        assertNotNull(result);
        assertEquals(result.size(), 2);
        for (NodeProperty property : result) {
            assertEquals(property.getNodeId(), 14, "All results should be with the same node id");
        }

        NodeProperty emptyVal = getById(6, result);
        assertEquals(emptyVal.getPropId(), 6);
        assertEquals(emptyVal.getPropKey(), "empty.val");
        assertEquals(emptyVal.getPropValue(), "");

        NodeProperty nullVal = getById(7, result);
        assertEquals(nullVal.getPropId(), 7);
        assertEquals(nullVal.getPropKey(), "null.val");
        assertEquals(emptyVal.getPropValue(), "");
    }

    public void getPropertiesNodeWithoutProperties() throws SQLException {
        List<NodeProperty> result = propsDao.getNodeProperties(1);
        assertEquals(result.size(), 0);
    }

    public void getPropertiesNodeNotExist() throws SQLException {
        List<NodeProperty> result = propsDao.getNodeProperties(98958459);
        assertEquals(result.size(), 0);
    }

    public void insertProperty() throws SQLException {
        int createCount = propsDao.create(new NodeProperty(160, 9, "key1", "value1"));
        assertEquals(createCount, 1);
        createCount = propsDao.create(new NodeProperty(170, 9, "key2", "value2"));
        assertEquals(createCount, 1);

        List<NodeProperty> properties = propsDao.getNodeProperties(9);
        assertEquals(properties.size(), 2);
    }

    public void deletePropertiesNodeWithProperties() throws SQLException {
        // first check the properties exist
        assertEquals(propsDao.getNodeProperties(9).size(), 3);

        int deletedCount = propsDao.deleteNodeProperties(9);
        assertEquals(deletedCount, 3);
        assertEquals(propsDao.getNodeProperties(9).size(), 0);
    }

    public void deletePropertiesNodeWithNoProperties() throws SQLException {
        assertEquals(propsDao.deleteNodeProperties(1), 0);
    }

    public void deletePropertiesNonExistentNode() throws SQLException {
        assertEquals(propsDao.deleteNodeProperties(6778678), 0);
    }

    public void trimLongPropertyValue() throws SQLException {
        try {
            int indexDefaultMaxSize = 4000;
            int propSize = indexDefaultMaxSize + 20;
            String randomProp = RandomStringUtils.randomAscii(propSize);
            propsDao.create(new NodeProperty(876, 21, "trimeme", randomProp));
            List<NodeProperty> nodeProperties = propsDao.getNodeProperties(21);
            assertThat(nodeProperties.size()).isEqualTo(1);
            String trimmedValue = nodeProperties.get(0).getPropValue();
            assertThat(trimmedValue).hasSize(propsDao.getDbIndexedValueMaxSize(indexDefaultMaxSize));
            assertNotEquals(randomProp, trimmedValue);
            assertThat(randomProp).startsWith(trimmedValue);
        } finally {
            propsDao.deleteNodeProperties(21);
        }
    }

    public void testGetPostgresPropValuesLongerThanAllowed() throws SQLException {
        int psqlOriginalMaxValue = ConstantValues.dbPostgresPropertyValueMaxSize.getInt();
        try {
            NodeProperty propId8 = new NodeProperty(8, 15, "longProp", "valueIsSmallerThanFortyCharacters");
            NodeProperty propId9 = new NodeProperty(9, 15, "longProp", "thisPropertyValueIsLongerThanFortyCharacters");
            NodeProperty propId10 = new NodeProperty(10, 16, "longProp2", "againPropertyValueWhichIsLongerThanFortyCharacters");
            NodeProperty propId11 = new NodeProperty(11, 16, "differentLongKey", "anotherPropertyValueWhichIsLongerThanFortyCharacters");
            NodeProperty propId12 = new NodeProperty(14, 19, "needToBeTrimmed", "thisPropertyValueIsIsNeedsToBeTrimmedAsItIsBiggerThan50Chars");
            NodeProperty propId13 = new NodeProperty(15, 20, "needToBeTrimmed2", "thisPropertyValueIsIsAlsoNeedsToBeTrimmedAsItIsBiggerThan50Chars");

            // ensure no prop is returned since all props smaller than the default max size
            List<NodeProperty> expectedProps = Collections.emptyList();
            List<NodeProperty> returnedPropsList = propsDao.getPostgresPropValuesLongerThanAllowed();
            assertEqualProps(returnedPropsList, expectedProps);

            // ensure that only prop values longer than 40 chars are returned
            modifyPsqlMaxAllowedValueSize(40);
            if (dbProperties.getDbType() == DbType.POSTGRESQL) {
                expectedProps = Lists.newArrayList(propId9, propId10, propId11, propId12, propId13);
            }
            returnedPropsList = propsDao.getPostgresPropValuesLongerThanAllowed();
            assertEqualProps(returnedPropsList, expectedProps);

            // ensure that only prop values longer than 30 chars are returned
            modifyPsqlMaxAllowedValueSize(30);
            if (dbProperties.getDbType() == DbType.POSTGRESQL) {
                expectedProps = Lists.newArrayList(propId8, propId9, propId10, propId11, propId12, propId13);
            }
            returnedPropsList = propsDao.getPostgresPropValuesLongerThanAllowed();
            assertEqualProps(returnedPropsList, expectedProps);
        } finally {
            modifyPsqlMaxAllowedValueSize(psqlOriginalMaxValue);
        }
    }

    public void updatePropertyValue() throws SQLException {
        NodeProperty prop1 = new NodeProperty(1001, 37, "the-key1", "the-value1");
        assertEquals(propsDao.create(prop1), 1);
        NodeProperty prop2 = new NodeProperty(1002, 37, "the-key1", "the-value2");
        assertEquals(propsDao.create(prop2), 1);
        NodeProperty prop3 = new NodeProperty(1003, 37, "the-key2", "the-value3");
        assertEquals(propsDao.create(prop3), 1);
        //Update an existing property
        NodeProperty updatedProp1 = new NodeProperty(prop1.getPropId(), prop1.getNodeId(), prop1.getPropKey(), "the-new-value1");
        assertEquals(propsDao.updateValue(updatedProp1), 1);
        assertEqualProps(Arrays.asList(updatedProp1, prop2, prop3), propsDao.getNodeProperties(37));
        //Update a non-existing property
        NodeProperty updatedNonExistingProp = new NodeProperty(1004, 37, "the-key3", "the-value4");
        assertEquals(propsDao.updateValue(updatedNonExistingProp), 0, "Update should not change anything in this case");
        assertEqualProps(Arrays.asList(updatedProp1, prop2, prop3), propsDao.getNodeProperties(37));
        //Update an existing property but with mismatching key
        NodeProperty updatedPropWrongKey = new NodeProperty(prop2.getPropId(), prop2.getNodeId(), prop2.getPropKey()+"-wrong", "the-value5");
        assertEquals(propsDao.updateValue(updatedPropWrongKey), 0, "Update should not change anything in this case");
        assertEqualProps(Arrays.asList(updatedProp1, prop2, prop3), propsDao.getNodeProperties(37));
    }

    public void deleteProperty() throws SQLException {
        assertEquals(propsDao.getNodeProperties(36).size(), 0);
        NodeProperty prop1 = new NodeProperty(2001, 36, "the-key1", "the-value1");
        assertEquals(propsDao.create(prop1), 1);
        NodeProperty prop2 = new NodeProperty(2002, 36, "the-key1", "the-value2");
        assertEquals(propsDao.create(prop2), 1);
        NodeProperty prop3 = new NodeProperty(2003, 36, "the-key2", "the-value3");
        assertEquals(propsDao.create(prop3), 1);
        assertEqualProps(propsDao.getNodeProperties(36), Arrays.asList(prop1, prop2, prop3));

        propsDao.delete(prop1);
        assertEqualProps(propsDao.getNodeProperties(36), Arrays.asList(prop2, prop3));
        propsDao.delete(prop3);
        assertEqualProps(propsDao.getNodeProperties(36), Collections.singletonList(prop2));
        propsDao.delete(prop2);
        assertEqualProps(propsDao.getNodeProperties(36), Collections.emptyList());
    }

    @Test(priority = Integer.MAX_VALUE)
    public void testTrimPostgresPropValuesToMaxAllowedLength() throws SQLException {
        int psqlOriginalMaxValue = ConstantValues.dbPostgresPropertyValueMaxSize.getInt();
        try {
            // make sure there are 5 props to trip
            int maxAllowedSize = 40;
            modifyPsqlMaxAllowedValueSize(maxAllowedSize);
            List<NodeProperty> invalidPropsBeforeTrim = propsDao.getPostgresPropValuesLongerThanAllowed();
            int expectedNumOfInvalidProps = dbProperties.getDbType() == DbType.POSTGRESQL ? 5 : 0;
            assertEquals(invalidPropsBeforeTrim.size(), expectedNumOfInvalidProps, "Incorrect result: " +
                    invalidPropsBeforeTrim.stream().map(pro -> pro.getPropKey() + " : " + pro.getPropValue())
                            .collect(Collectors.toList()));

            // trim props
            int affectedRows = propsDao.trimPostgresPropValuesToMaxAllowedLength();
            assertPropsWereTrimmedCorrectly(maxAllowedSize, affectedRows);
        } finally {
            modifyPsqlMaxAllowedValueSize(psqlOriginalMaxValue);
        }
    }

    public void testGetTrashItemsToDeleteWithLimit() throws SQLException {
        long trashBatchSize = trashcanMaxSearchResults.getLong();
        try {
            int expectedNumOfResults = 2;
            ArtifactoryHome.get().getArtifactoryProperties()
                    .setProperty(trashcanMaxSearchResults.getPropertyName(), String.valueOf(expectedNumOfResults));
            List<GCCandidate> itemsToDelete = propsDao.getGCCandidatesFromTrash("1552838802");
            assertEquals(itemsToDelete.size(), expectedNumOfResults);
        } finally {
            ArtifactoryHome.get().getArtifactoryProperties()
                    .setProperty(trashcanMaxSearchResults.getPropertyName(), String.valueOf(trashBatchSize));
        }
    }

    public void testGetTrashItemsToDelete() throws SQLException {
        List<GCCandidate> itemsToDelete = propsDao.getGCCandidatesFromTrash("1552838801");
        Set<RepoPath> setOfPaths = itemsToDelete.stream()
                .map(GCCandidate::getRepoPath)
                .collect(Collectors.toSet());
        assertEquals(setOfPaths, ImmutableSet.of(
                RepoPathFactory.create("auto-trashcan", "repo2/aa/aafile1.txt"),
                RepoPathFactory.create("auto-trashcan", "repo2/a/b/c/abcfile1.txt"),
                RepoPathFactory.create("auto-trashcan", "repo2/ably.txt"))
        );

        itemsToDelete = propsDao.getGCCandidatesFromTrash("1552838399");
        assertEquals(itemsToDelete.size(), 0);

        itemsToDelete = propsDao.getGCCandidatesFromTrash("1552838802");
        setOfPaths = itemsToDelete.stream()
                .map(GCCandidate::getRepoPath)
                .collect(Collectors.toSet());
        assertEquals(setOfPaths, ImmutableSet.of(
                RepoPathFactory.create("auto-trashcan", "repo2/aa/aafile1.txt"),
                RepoPathFactory.create("auto-trashcan", "repo2/a/b/c/abcfile1.txt"),
                RepoPathFactory.create("auto-trashcan", "repo2/ably.txt"),
                RepoPathFactory.create("auto-trashcan", "repo2/something.txt"))
        );
    }

    private void assertPropsWereTrimmedCorrectly(int maxAllowedSize, int affectedRows) throws SQLException {
        if (dbProperties.getDbType() == DbType.POSTGRESQL) {
            assertEquals(affectedRows, 5);

            // build objects of original long props as it was before trimming in the DB
            NodeProperty propId9 = new NodeProperty(9, 15, "longProp", "thisPropertyValueIsLongerThanFortyCharacters");
            NodeProperty propId10 = new NodeProperty(10, 16, "longProp2", "againPropertyValueWhichIsLongerThanFortyCharacters");
            NodeProperty propId11 = new NodeProperty(11, 16, "differentLongKey", "anotherPropertyValueWhichIsLongerThanFortyCharacters");
            NodeProperty propId12 = new NodeProperty(14, 19, "needToBeTrimmed", "thisPropertyValueIsIsNeedsToBeTrimmedAsItIsBiggerThan50Chars");
            NodeProperty propId13 = new NodeProperty(15, 20, "needToBeTrimmed2", "thisPropertyValueIsIsAlsoNeedsToBeTrimmedAsItIsBiggerThan50Chars");
            List<NodeProperty> originalLongPropsToTrim = Lists
                    .newArrayList(propId9, propId10, propId11, propId12, propId13);

            // ensure that trimmed properties are smaller than the original properties and were trimmed correctly
            originalLongPropsToTrim.forEach(originalLongProp -> {
                try {
                    List<NodeProperty> trimmedPropsForNodeId = propsDao.getNodeProperties(originalLongProp.getNodeId());
                    NodeProperty trimmedProp = trimmedPropsForNodeId.stream()
                            .filter(prop -> prop.getPropId() == originalLongProp.getPropId()).findFirst()
                            .orElse(null);
                    assertNotNull(trimmedProp);
                    assertEquals(trimmedProp.getPropValue().length(), maxAllowedSize);
                    assertNotEquals(trimmedProp.getPropValue(), originalLongProp.getPropValue());
                    assertTrue(originalLongProp.getPropValue().startsWith(trimmedProp.getPropValue()));
                } catch (SQLException e) {
                    fail("Failed to retrieve property.", e);
                }
            });

            // ensure that after first trimming, there are no further propy values to trim
            assertEquals(propsDao.getPostgresPropValuesLongerThanAllowed().size(), 0);
        } else {
            assertEquals(affectedRows, 0);
        }
    }

    private void modifyPsqlMaxAllowedValueSize(int psqlOriginalMaxValue) {
        if (dbProperties.getDbType() == DbType.POSTGRESQL) {
            ArtifactoryHome.get().getArtifactoryProperties()
                    .setProperty(dbPostgresPropertyValueMaxSize.getPropertyName(), String.valueOf(psqlOriginalMaxValue));
        }
    }

    private void assertEqualProps(List<NodeProperty> actual, List<NodeProperty> expected) {
        assertEquals(actual.size(), expected.size(), "Lists are not of the same size");
        List<NodeProperty> actualSorted = actual.stream()
                .sorted((p1, p2) -> Long.compare(p1.getPropId(), p2.getPropId()))
                .collect(Collectors.toList());
        List<NodeProperty> expectedSorted = expected.stream()
                .sorted((p1, p2) -> Long.compare(p1.getPropId(), p2.getPropId()))
                .collect(Collectors.toList());
        for (int i = 0; i < actualSorted.size(); i++) {
            NodeProperty actualProp = actualSorted.get(i);
            NodeProperty expectedProp = expectedSorted.get(i);
            if (!EqualsBuilder.reflectionEquals(actualProp, expectedProp)) {
                fail("props at index " + i + " are not the same.\nActual: " + toString(actualProp) + "\nExpected: " + toString(expectedProp));
            }
        }
    }

    private String toString(NodeProperty prop) {
        return "[" + prop.getNodeId() + ", " + prop.getPropId() +", [" + prop.getPropKey() + "], [" + prop.getPropValue() + "] ]";
    }

    private NodeProperty getById(long propId, List<NodeProperty> properties) {
        for (NodeProperty property : properties) {
            if (property.getPropId() == propId) {
                return property;
            }
        }
        return null;
    }
}
