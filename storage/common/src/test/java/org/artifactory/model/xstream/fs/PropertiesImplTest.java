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

package org.artifactory.model.xstream.fs;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.model.xstream.fs.PropertiesImpl.PropertyChange;
import org.artifactory.model.xstream.fs.PropertiesImpl.PropertyChangeResult;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.artifactory.util.InternalStringUtils.compareNullLast;
import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.model.xstream.fs.PropertiesImpl}.
 * Note: The tested class requires changes which are undefined yet and had no tests. Here will just test few important
 * parts until the content is defined.
 *
 * @author Yossi Shaul
 */
@Test
public class PropertiesImplTest {

    public void isEmpty() {
        assertTrue(new PropertiesImpl().isEmpty());
    }

    public void isEmptyAndNormalProperty() {
        PropertiesImpl props = new PropertiesImpl();
        props.put("test", "name");
        assertFalse(props.isEmpty());
    }

    public void testChangeTracking() {
        DummyProperty prop1 = new DummyProperty(1, "key1", "value1");
        DummyProperty prop2a = new DummyProperty(2, "key2", "value2a");
        DummyProperty prop2b = new DummyProperty(3, "key2", "value2b");
        DummyProperty prop3a = new DummyProperty(4, "key3", "value3a");
        DummyProperty prop3b = new DummyProperty(5, "key3", "value3b");
        DummyProperty prop3c = new DummyProperty(6, "key3", "value3c");
        DummyProperty prop4 = new DummyProperty(7, "key4", "value4");
        DummyProperty prop5 = new DummyProperty(8, "key5", "value5");
        List<DummyProperty> origProps = Arrays.asList(prop1, prop2a, prop2b, prop3a, prop3b, prop3c, prop4, prop5);

        PropertiesImpl props = new PropertiesImpl(7, origProps);
        assertTrue(props.isChangeTrackingEnabled());
        assertEquals(props.get("key1"), Sets.newHashSet("value1"));
        assertEquals(props.get("key2"), Sets.newHashSet("value2a", "value2b"));
        assertEquals(props.get("key3"), Sets.newHashSet("value3a", "value3b", "value3c"));
        assertEquals(props.get("key4"), Sets.newHashSet("value4"));
        assertEquals(props.get("key5"), Sets.newHashSet("value5"));

        List<PropertyChange> expectedChanges = Lists.newArrayList();
        // key1 - Keep as is
        // key2 - Delete multiple entries of the same key
        props.removeAll("key2");
        expectedChanges.add(PropertyChange.delete(prop2a));
        expectedChanges.add(PropertyChange.delete(prop2b));
        // key3 - Update existing key with less values
        props.replaceValues("key3", Arrays.asList("value3new1", "value3new2"));
        expectedChanges.add(PropertyChange.update(prop3a, "value3new1"));
        expectedChanges.add(PropertyChange.update(prop3b, "value3new2"));
        expectedChanges.add(PropertyChange.delete(prop3c));
        // key4 - Update existing key with more values
        props.put("key4", "value4b");
        expectedChanges.add(PropertyChange.create("key4", "value4b"));
        // key5 - Update existing key with the same number of values
        props.replaceValues("key5", Collections.singletonList("value5new"));
        expectedChanges.add(PropertyChange.update(prop5, "value5new"));
        // key6 - Create new key with a single value
        props.put("key6", "value6");
        expectedChanges.add(PropertyChange.create("key6", "value6"));
        // key7 - Create new key with multiple values
        props.put("key7", "value7a");
        props.put("key7", "value7b");
        expectedChanges.add(PropertyChange.create("key7", "value7a"));
        expectedChanges.add(PropertyChange.create("key7", "value7b"));

        // Sanity - make sure the properties are as expected
        assertEquals(props.get("key1"), Sets.newHashSet("value1"));
        assertEquals(props.get("key2"), Sets.newHashSet());
        assertEquals(props.get("key3"), Sets.newHashSet("value3new1", "value3new2"));
        assertEquals(props.get("key4"), Sets.newHashSet("value4", "value4b"));
        assertEquals(props.get("key5"), Sets.newHashSet("value5new"));
        assertEquals(props.get("key6"), Sets.newHashSet("value6"));
        assertEquals(props.get("key7"), Sets.newHashSet("value7a", "value7b"));

        assertPropertyChangesIgnoreIds(props.getPropertyChanges(), expectedChanges);
    }

    public void testResetChangeTracking() {
        DummyProperty prop1 = new DummyProperty(1, "key1", "value1");
        DummyProperty prop2a = new DummyProperty(2, "key2", "value2a");
        DummyProperty prop2b = new DummyProperty(3, "key2", "value2b");
        DummyProperty prop3a = new DummyProperty(4, "key3", "value3a");
        DummyProperty prop3b = new DummyProperty(5, "key3", "value3b");
        DummyProperty prop4a = new DummyProperty(6, "key4", "value4a");
        DummyProperty prop4b = new DummyProperty(7, "key4", "value4b");
        List<DummyProperty> origProps = Arrays.asList(prop1, prop2a, prop2b, prop3a, prop3b, prop4a, prop4b);
        PropertiesImpl props = new PropertiesImpl(7, origProps);

        List<PropertyChange> expectedChanges = Lists.newArrayList();
        //key1 - no change
        //key2 - create
        props.put("key2", "value2c");
        DummyProperty prop2c = new DummyProperty(-1, "key2", "value2c");
        expectedChanges.add(PropertyChange.create("key2", "value2c"));
        //key3 - update
        props.replaceValues("key3", Arrays.asList("value3new1", "value3new2"));
        expectedChanges.add(PropertyChange.update(prop3a, "value3new1"));
        expectedChanges.add(PropertyChange.update(prop3b, "value3new2"));
        //key4 - delete
        props.replaceValues("key4", Collections.singletonList("value4a"));
        expectedChanges.add(PropertyChange.delete(prop4b));

        Collection<PropertyChange> propertyChanges = props.getPropertyChanges();
        assertPropertyChangesIgnoreIds(propertyChanges, expectedChanges);

        //Reset the changes
        AtomicLong propId = new AtomicLong(8);
        List<PropertyChangeResult> changeResults = createPropertyChangeResults(propertyChanges, propId);
        props.resetChangeTracking(changeResults);

        propertyChanges = props.getPropertyChanges();
        assertPropertyChangesIgnoreIds(propertyChanges, Collections.emptyList());

        // Make changes after reset
        expectedChanges.clear();
        props.put("key5", "value5a");
        expectedChanges.add(PropertyChange.create("key5", "value5a"));
        assertPropertyChangesIgnoreIds(props.getPropertyChanges(), expectedChanges);
        // Make more changes without resetting
        props.put("key5", "value5b");
        expectedChanges.add(PropertyChange.create("key5", "value5b"));
        props.removeAll("key2");
        expectedChanges.add(PropertyChange.delete(prop2a));
        expectedChanges.add(PropertyChange.delete(prop2b));
        expectedChanges.add(PropertyChange.delete(prop2c));

        propertyChanges = props.getPropertyChanges();
        assertPropertyChangesIgnoreIds(propertyChanges, expectedChanges);

        //Reset the changes
        changeResults = createPropertyChangeResults(propertyChanges, propId);
        props.resetChangeTracking(changeResults);

        assertTrue(props.getPropertyChanges().isEmpty());

        //Change that fails is not being reset
        expectedChanges.clear();
        props.put("key6", "value6");
        expectedChanges.add(PropertyChange.create("key6", "value6"));

        propertyChanges = props.getPropertyChanges();
        assertPropertyChangesIgnoreIds(propertyChanges, expectedChanges);

        PropertyChangeResult changeResult = new PropertyChangeResult(propertyChanges.iterator().next(), false, null);
        props.resetChangeTracking(Collections.singletonList(changeResult));
        //Check no change:
        propertyChanges = props.getPropertyChanges();
        assertPropertyChangesIgnoreIds(propertyChanges, expectedChanges);
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void testGetPropertyChangesForbiddenWhenNotEnabled() {
        PropertiesImpl props = new PropertiesImpl();
        assertFalse(props.isChangeTrackingEnabled());
        props.getPropertyChanges();
        fail("Should not get here");
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void testResetPropertyChangesForbiddenWhenNotEnabled() {
        PropertiesImpl otherProps = new PropertiesImpl();
        PropertiesImpl props = new PropertiesImpl(otherProps);
        assertFalse(props.isChangeTrackingEnabled());
        props.resetChangeTracking(Collections.emptyList());
        fail("Should not get here");
    }

    public void testChangeTrackingWithNullPropValue() { //RTFACT-12058
        AtomicLong propId = new AtomicLong(1);
        DummyProperty prop1a = new DummyProperty(propId.getAndIncrement(), "key1", "value1");
        DummyProperty prop1b = new DummyProperty(propId.getAndIncrement(), "key1", null);
        List<DummyProperty> origProps = Arrays.asList(prop1a, prop1b);
        PropertiesImpl props = new PropertiesImpl(7, origProps);
        assertEquals(props.get("key1"), Sets.newHashSet("value1", null));
        List<PropertyChange> expectedChanges = Lists.newArrayList();

        props.replaceValues("key1", Sets.newHashSet("newValue1", "newValue2"));
        expectedChanges.add(PropertyChange.update(prop1a, "newValue1"));
        expectedChanges.add(PropertyChange.update(prop1b, "newValue2"));
        assertEquals(props.get("key1"), Sets.newHashSet("newValue1", "newValue2"));

        Collection<PropertyChange> propertyChanges = props.getPropertyChanges();
        assertPropertyChangesIgnoreIds(propertyChanges, expectedChanges);

        //Reset the changes
        List<PropertyChangeResult> changeResults = createPropertyChangeResults(propertyChanges, propId);
        props.resetChangeTracking(changeResults);
        assertTrue(props.getPropertyChanges().isEmpty());

        expectedChanges.clear();
        props.replaceValues("key1", Sets.newHashSet(null, "anotherValue1"));
        expectedChanges.add(PropertyChange.update(prop1a, null));
        expectedChanges.add(PropertyChange.update(prop1b, "anotherValue1"));
        assertEquals(props.get("key1"), Sets.newHashSet(null, "anotherValue1"));

        propertyChanges = props.getPropertyChanges();
        assertPropertyChangesIgnoreIds(propertyChanges, expectedChanges);
    }

    public void testPutAllProperties() {
        //Add values
        PropertiesImpl props1 = new PropertiesImpl();
        props1.put("key1", "value1");
        props1.put("key2", "value2");
        PropertiesImpl props2 = new PropertiesImpl();
        props2.put("key1", "value1");
        props2.put("key2", "value2b");
        props2.put("key3", "value3a");
        props2.put("key3", "value3b");

        assertTrue(props1.putAll(props2));
        assertEquals(props1.get("key1"), Collections.singleton("value1"));
        assertEquals(props1.get("key2"), Arrays.asList("value2", "value2b"));
        assertEquals(props1.get("key3"), Arrays.asList("value3a", "value3b"));

        //Same values
        props1 = new PropertiesImpl();
        props1.put("key1", "value1");
        props1.put("key2", "value2a");
        props1.put("key2", "value2b");
        props2 = new PropertiesImpl();
        props2.put("key1", "value1");
        props2.put("key2", "value2a");
        props2.put("key2", "value2b");

        assertFalse(props1.putAll(props2));
        assertEquals(props1.get("key1"), Collections.singleton("value1"));
        assertEquals(props1.get("key2"), Arrays.asList("value2a", "value2b"));

        //Less values
        props1 = new PropertiesImpl();
        props1.put("key1", "value1");
        props1.put("key2", "value2a");
        props1.put("key2", "value2b");
        props2 = new PropertiesImpl();
        props2.put("key2", "value2a");

        assertFalse(props1.putAll(props2));
        assertEquals(props1.get("key1"), Collections.singleton("value1"));
        assertEquals(props1.get("key2"), Arrays.asList("value2a", "value2b"));

        //No values
        props1 = new PropertiesImpl();
        props1.put("key1", "value1");
        props1.put("key2", "value2a");
        props1.put("key2", "value2b");
        props2 = new PropertiesImpl();

        assertFalse(props1.putAll(props2));
        assertEquals(props1.get("key1"), Collections.singleton("value1"));
        assertEquals(props1.get("key2"), Arrays.asList("value2a", "value2b"));
    }

    private List<PropertyChangeResult> createPropertyChangeResults(Collection<PropertyChange> propertyChanges, AtomicLong nextPropId) {
        return propertyChanges.stream().map(change -> {
            PropertyWithId newProp = null;
            switch (change.getChangeType()) {
                case CREATE:
                    newProp = new DummyProperty(nextPropId.getAndIncrement(), change.getProperty().getPropKey(), change.getProperty().getPropValue());
                    break;
                case UPDATE:
                    newProp = new DummyProperty(change.getProperty().getPropId(), change.getProperty().getPropKey(), change.getProperty().getPropValue());
                    break;
            }
            return new PropertyChangeResult(change, true, newProp);
        }).collect(Collectors.toList());
    }

    private void assertPropertyChangesIgnoreIds(Collection<PropertyChange> actual, Collection<PropertyChange> expected) {
        List<PropertyChange> sortedActual = actual.stream().sorted(propertyChangeComparator).collect(Collectors.toList());
        List<PropertyChange> sortedExpected = expected.stream().sorted(propertyChangeComparator).collect(Collectors.toList());
        String actualAsString = Joiner.on("\n").join(sortedActual.stream().map(this::toString).collect(Collectors.toList()));
        String expectedAsString = Joiner.on("\n").join(sortedExpected.stream().map(this::toString).collect(Collectors.toList()));
        assertEquals(actual.size(), expected.size(), "Number of changes is not as expected.\n" +
                "Actual:\n"+actualAsString+"\nExpected:\n"+expectedAsString+"\n");
        for (int i = 0; i < sortedActual.size(); i++) {
            PropertyChange actualChange = sortedActual.get(i);
            PropertyChange expectedChange = sortedExpected.get(i);
            if (!actualChange.getChangeType().equals(expectedChange.getChangeType()) ||
                    !actualChange.getProperty().getPropKey().equals(expectedChange.getProperty().getPropKey()) ||
                    !StringUtils.equals(actualChange.getProperty().getPropValue(), expectedChange.getProperty().getPropValue())) {
                fail("Change is not as expected at index " + i + ".\n" +
                        "Actual:\n" + actualAsString + "\nExpected:\n" + expectedAsString);
            }
        }
    }

    private String toString(PropertyChange change) {
        return "[ " + change.getChangeType() +
                ", " + change.getProperty().getPropId() +
                ", [" + change.getProperty().getPropKey() + "]" +
                ", [" + change.getProperty().getPropValue() + "]" +
                " ]";
    }

    private static final Comparator<PropertyChange> propertyChangeComparator = (c1, c2) -> {
        int result = c1.getProperty().getPropKey().compareTo(c2.getProperty().getPropKey());
        if (result != 0) return result;
        result = c1.getChangeType().compareTo(c2.getChangeType());
        if (result != 0) return result;
        result = compareNullLast(c1.getProperty().getPropValue(), c2.getProperty().getPropValue());
        return result;
    };

    private static class DummyProperty implements PropertyWithId {

        private final long propId;
        private final String propKey;
        private final String propValue;

        private DummyProperty(long propId, String propKey, String propValue) {
            this.propId = propId;
            this.propKey = propKey;
            this.propValue = propValue;
        }

        @Override
        public long getPropId() {
            return propId;
        }

        @Override
        public String getPropKey() {
            return propKey;
        }

        @Override
        public String getPropValue() {
            return propValue;
        }
    }

}
