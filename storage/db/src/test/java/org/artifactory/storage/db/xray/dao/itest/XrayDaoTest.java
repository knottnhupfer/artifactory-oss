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

package org.artifactory.storage.db.xray.dao.itest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.fs.dao.PropertiesDao;
import org.artifactory.storage.db.fs.entity.NodeBuilder;
import org.artifactory.storage.db.fs.entity.NodeProperty;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.xray.dao.XrayDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;
import static org.testng.Assert.*;

/**
 * @author Yinon Avraham
 */
public class XrayDaoTest extends DbBaseTest {

    @Autowired
    private NodesDao nodesDao;

    @Autowired
    private PropertiesDao propsDao;

    @Autowired
    private XrayDao xrayDao;

    private final int queryLimit = 100;

    @BeforeClass
    public void setup() throws Exception {
        //repo1 nodes
        nodesDao.create(new NodeBuilder().nodeId(11).file(true).repo("repo1").path("path/to").name("file1.ext").build());
        nodesDao.create(new NodeBuilder().nodeId(12).file(true).repo("repo1").path("path/to").name("file2.ext1").build());
        nodesDao.create(new NodeBuilder().nodeId(13).file(true).repo("repo1").path("path/to/other").name("file3.ext1").build());
        nodesDao.create(new NodeBuilder().nodeId(14).file(true).repo("repo1").path("path/to/other").name("file4.ext2").build());
        nodesDao.create(new NodeBuilder().nodeId(15).file(true).repo("repo1").path("path/to/other").name("file5.ext2").build());
        nodesDao.create(new NodeBuilder().nodeId(16).file(true).repo("repo1").path("path/to/other").name("file6.ext2").build());
        nodesDao.create(new NodeBuilder().nodeId(17).file(true).repo("repo1").path("path/to/other").name(MANIFEST_FILENAME).build());
        nodesDao.create(new NodeBuilder().nodeId(18).file(true).repo("repo1").path("path/to/another").name("file77.ext8").build());
        nodesDao.create(new NodeBuilder().nodeId(19).file(true).repo("repo1").path("path/to/another").name("file88.ext7").build());

        //repo2 nodes
        nodesDao.create(new NodeBuilder().nodeId(21).file(true).repo("repo2").path("path/to/mother").name("file1.ext1").build());
        nodesDao.create(new NodeBuilder().nodeId(22).file(true).repo("repo2").path("path/to/earth").name("file2.ext2").build());
        nodesDao.create(new NodeBuilder().nodeId(23).file(true).repo("repo2").path("path/to/earth").name("file3.ext2").build());
        nodesDao.create(new NodeBuilder().nodeId(24).file(true).repo("repo2").path("path/to/earth2").name("file3.ext3").build());
        nodesDao.create(new NodeBuilder().nodeId(25).file(true).repo("repo2").path("path/to/earth2").name("file4.ext3").build());
        nodesDao.create(new NodeBuilder().nodeId(26).file(true).repo("repo2").path("path/to/earth2").name("file5.ext3").build());

        long propId = 1;
        //repo1 props
        propsDao.create(new NodeProperty(propId++, 11, "xray.12345.index.status", "Indexed"));
        propsDao.create(new NodeProperty(propId++, 11, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));
        propsDao.create(new NodeProperty(propId++, 12, "xray.12345.index.status", "Indexing"));
        propsDao.create(new NodeProperty(propId++, 12, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));
        propsDao.create(new NodeProperty(propId++, 13, "xray.12345.index.status", "Indexed"));
        propsDao.create(new NodeProperty(propId++, 13, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));
        propsDao.create(new NodeProperty(propId++, 14, "xray.12345.index.status", "Indexed"));
        propsDao.create(new NodeProperty(propId++, 14, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));

        propsDao.create(new NodeProperty(propId++, 18, "xray.12345.index.status", "Scanned"));
        propsDao.create(new NodeProperty(propId++, 18, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));
        propsDao.create(new NodeProperty(propId++, 19, "xray.12345.index.status", "Scanned"));
        propsDao.create(new NodeProperty(propId++, 19, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));

        //repo2 props
        propsDao.create(new NodeProperty(propId++, 21, "xray.12345.index.status", "Indexed"));
        propsDao.create(new NodeProperty(propId++, 21, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));
        propsDao.create(new NodeProperty(propId++, 22, "xray.12345.index.status", "Indexing"));
        propsDao.create(new NodeProperty(propId++, 22, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));
        propsDao.create(new NodeProperty(propId++, 23, "xray.12345.index.status", "Indexing"));
        propsDao.create(new NodeProperty(propId++, 23, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));
        propsDao.create(new NodeProperty(propId++, 24, "xray.12345.index.status", "Indexing"));
        propsDao.create(new NodeProperty(propId++, 24, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));
        propsDao.create(new NodeProperty(propId++, 25, "xray.12345.index.status", "Indexed"));
        propsDao.create(new NodeProperty(propId++, 25, "xray.12345.index.lastUpdated", "" + System.currentTimeMillis()));

        // Dummy property for another node
        propsDao.create(new NodeProperty(propId++, 26, "dummy.key", "You shall not pass"));

        // Dummy property for xray node
        propsDao.create(new NodeProperty(propId++, 23, "dummy.key", "You shall not pass"));
    }

    @Test
    public void testDeleteXrayProperties() throws SQLException {
        // Validating Xray properties are present
        assertTrue(propsDao.hasNodeProperties(11), "Node expected to hold properties");
        assertTrue(propsDao.hasNodeProperties(21), "Node expected to hold properties");
        assertTrue(propsDao.hasNodeProperties(23), "Node expected to hold properties");

        // Performing delete on specific properties
        xrayDao.bulkDeleteXrayProperties(Lists.newArrayList(1L,2L,13L,14L,17L,18L), queryLimit);

        // Validating Xray properties are NOT present
        assertFalse(propsDao.hasNodeProperties(11), "Node expected to hold no properties");
        assertFalse(propsDao.hasNodeProperties(21), "Node expected to hold no properties");

        // Validating other properties are still present on a node that had Xray properties
        assertTrue(propsDao.getNodeProperties(23).size() == 1, "Node expected to hold 1 property");

        // Validating other properties are still present on a node that didn't have Xray properties
        assertTrue(propsDao.hasNodeProperties(26), "Node expected to hold properties");
    }

    @Test
    public void testGetDeleteQueryMultipleValuesSql() throws Exception {
        String res = xrayDao.getDeleteQueryMultipleValuesSql(5);
        assertEquals(res, "DELETE FROM node_props WHERE prop_id IN (?, ?, ?, ?, ?)");
    }

    @Test
    public void testGetPotentialForIndex() {
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("ext"), Collections.emptySet()), 1);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("ext1"), Collections.emptySet()), 2);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("ext2"), Collections.emptySet()), 3);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("ext", "ext2"), Collections.emptySet()), 4);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("ext1", "ext2"), Collections.emptySet()), 5);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("ext", "ext1", "ext2"), Collections.emptySet()), 6);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("non"), Collections.emptySet()), 0);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("non", "bon"), Collections.emptySet()), 0);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("non", "bon"), Sets.newHashSet(MANIFEST_FILENAME)), 1);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("ext1", "ext2"), Sets.newHashSet(MANIFEST_FILENAME)), 6);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("ext", "ext1"), Sets.newHashSet(MANIFEST_FILENAME)), 4);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Sets.newHashSet("ext", "ext1"), Sets.newHashSet("manifest.jsonXYZ")), 3);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Collections.emptySet(), Sets.newHashSet(MANIFEST_FILENAME)), 1);
        assertEquals(xrayDao.getPotentialForIndex("repo1", Collections.emptySet(), Sets.newHashSet("manifest.jsonXYZ")), 0);

        assertEquals(xrayDao.getPotentialForIndex("repo2", Sets.newHashSet("ext1"), Collections.emptySet()), 1);
        assertEquals(xrayDao.getPotentialForIndex("repo2", Sets.newHashSet("ext2"), Collections.emptySet()), 2);
        assertEquals(xrayDao.getPotentialForIndex("repo2", Sets.newHashSet("ext3"), Collections.emptySet()), 3);
        assertEquals(xrayDao.getPotentialForIndex("repo2", Sets.newHashSet("ext1", "ext2"), Collections.emptySet()), 3);
        assertEquals(xrayDao.getPotentialForIndex("repo2", Sets.newHashSet("ext2", "ext3"), Collections.emptySet()), 5);
        assertEquals(xrayDao.getPotentialForIndex("repo2", Sets.newHashSet("ext1", "ext2", "ext3"), Collections.emptySet()), 6);
    }
}