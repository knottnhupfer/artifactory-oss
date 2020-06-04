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

package org.artifactory.ui.utils;

import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.PropertiesArtifactInfo;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Omri Ziv
 */
@Test
public class PropertySetsConverterTest {

    public void testOnePropertySet() {
        PropertySet propertySet = newPropertySet("set1");
        Property prop1 = newProperty("prop1");
        Property prop2 = newProperty("prop2");
        propertySet.setProperties(Arrays.asList(prop1, prop2));
        List<PropertiesArtifactInfo> propertiesArtifactInfos = PropertySetsConverter.toPropertiesArtifactInfo(propertySet);

        assertEquals(2, propertiesArtifactInfos.size());
        assertEquals("prop1", propertiesArtifactInfos.get(0).getProperty().getName());
        assertEquals("set1", propertiesArtifactInfos.get(0).getParent().getName());
        assertEquals("prop2", propertiesArtifactInfos.get(1).getProperty().getName());
        assertEquals("set1", propertiesArtifactInfos.get(1).getParent().getName());
    }

    public void testOnePropertySetOnListMethod() {
        PropertySet propertySet = newPropertySet("set1");
        Property prop1 = newProperty("prop1");
        Property prop2 = newProperty("prop2");
        propertySet.setProperties(Arrays.asList(prop1, prop2));
        List<PropertiesArtifactInfo> propertiesArtifactInfos = PropertySetsConverter.toPropertiesArtifactInfoList(Collections.singletonList(propertySet));

        assertEquals(2, propertiesArtifactInfos.size());
        assertEquals("prop1", propertiesArtifactInfos.get(0).getProperty().getName());
        assertEquals("set1", propertiesArtifactInfos.get(0).getParent().getName());
        assertEquals("prop2", propertiesArtifactInfos.get(1).getProperty().getName());
        assertEquals("set1", propertiesArtifactInfos.get(1).getParent().getName());
    }

    public void testMultiPropertySet() {
        PropertySet propertySet1 = newPropertySet("set1");
        Property prop1 = newProperty("prop1");
        PropertySet propertySet2 = newPropertySet("set2");
        Property prop2 = newProperty("prop2");
        propertySet1.setProperties(Arrays.asList(prop1, prop2));
        propertySet2.setProperties(Collections.singletonList(prop1));
        List<PropertiesArtifactInfo> propertiesArtifactInfos = PropertySetsConverter.toPropertiesArtifactInfoList(Arrays.asList(propertySet1, propertySet2));

        assertEquals(3, propertiesArtifactInfos.size());
        assertEquals("prop1", propertiesArtifactInfos.get(0).getProperty().getName());
        assertEquals("set1", propertiesArtifactInfos.get(0).getParent().getName());
        assertEquals("prop2", propertiesArtifactInfos.get(1).getProperty().getName());
        assertEquals("set1", propertiesArtifactInfos.get(1).getParent().getName());
        assertEquals("prop1", propertiesArtifactInfos.get(2).getProperty().getName());
        assertEquals("set2", propertiesArtifactInfos.get(2).getParent().getName());
    }

    public void testEmptyPropertySet() {
        List<PropertiesArtifactInfo> propertiesArtifactInfos = PropertySetsConverter.toPropertiesArtifactInfoList(
                Collections.emptyList());

        assertTrue(propertiesArtifactInfos.isEmpty());
    }

    public void testNullPropertySet() {
        List<PropertiesArtifactInfo> propertiesArtifactInfos = PropertySetsConverter.toPropertiesArtifactInfoList(
                null);

        assertTrue(propertiesArtifactInfos.isEmpty());
    }

    public void testProperty() {
        PropertySet propertySet1 = newPropertySet("set1");
        Property prop1 = newProperty("prop1");
        propertySet1.setProperties(Collections.singletonList(prop1));

        PropertiesArtifactInfo propertiesArtifactInfo = PropertySetsConverter.toPropertiesArtifactInfo(
                prop1, propertySet1);

        assertEquals("prop1", propertiesArtifactInfo.getProperty().getName());
        assertEquals("set1", propertiesArtifactInfo.getParent().getName());
    }

    private PropertySet newPropertySet(String propertySetName) {
        PropertySet propertySet = new PropertySet();
        propertySet.setName(propertySetName);
        return propertySet;
    }

    private Property newProperty(String propertyName) {
        Property property = new Property();
        property.setName(propertyName);
        return property;
    }

}
