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

import com.google.common.collect.*;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.StringUtils;
import org.artifactory.md.Properties;
import org.artifactory.md.PropertiesInfo;
import org.artifactory.util.InternalStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static org.artifactory.util.InternalStringUtils.compareNullLast;

/**
 * A map of stringified keys and values, used for storing arbitrary key-value metadata on repository items.
 *
 * @author Yoav Landman
 */
@XStreamAlias(Properties.ROOT)
public class PropertiesImpl implements Properties {
    private static final Logger log = LoggerFactory.getLogger(PropertiesImpl.class);
    private static final long NO_ID = -1L;

    private final SetMultimap<String, String> props;
    private final SetMultimap<String, PropertyWithId> propsOriginal;
    private final Set<String> touchedKeys = Sets.newHashSet();
    private final long nodeId;
    private final boolean changeTrackingEnabled;

    public PropertiesImpl() {
        props = LinkedHashMultimap.create();
        propsOriginal = Multimaps.unmodifiableSetMultimap(LinkedHashMultimap.create());
        nodeId = NO_ID;
        changeTrackingEnabled = false;
    }

    public PropertiesImpl(PropertiesInfo properties) {
        props = LinkedHashMultimap.create();
        properties.entries().forEach(entry ->
                props.put(entry.getKey(), entry.getValue())
        );
        propsOriginal = Multimaps.unmodifiableSetMultimap(LinkedHashMultimap.create());
        nodeId = NO_ID;
        changeTrackingEnabled = false;
    }

    /**
     * Initialize the properties with original properties - to enable change tracking.
     * @param nodeId the ID of the node these properties belong to. Tracked changes will only be used for updating this node.
     * @param originalProps the original properties to compare to.
     *                      Note- the provided {@link PropertyWithId} implementation is expected to be immutable.
     */
    public PropertiesImpl(long nodeId, @Nonnull Collection<? extends PropertyWithId> originalProps) {
        SetMultimap<String, PropertyWithId> originalPropsMap = LinkedHashMultimap.create(originalProps.size(), 1);
        for (PropertyWithId nodeProp : originalProps) {
            originalPropsMap.put(nodeProp.getPropKey(), nodeProp);
        }
        this.propsOriginal = LinkedHashMultimap.create(originalPropsMap);
        this.nodeId = nodeId;
        changeTrackingEnabled = true;
        props = LinkedHashMultimap.create();
        propsOriginal.entries().forEach(entry -> props.put(entry.getKey(), entry.getValue().getPropValue()));
    }

    @Override
    public int size() {
        return props.size();
    }

    @Override
    @Nullable
    public Set<String> get(@Nonnull String key) {
        return props.get(key);
    }

    @Override
    @Nullable
    public String getFirst(@Nonnull String key) {
        Set<String> propertyValues = props.get(key);
        if (propertyValues != null) {
            Iterator<String> iterator = propertyValues.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        }

        return null;
    }

    @Override
    public boolean putAll(@Nonnull String key, Iterable<? extends String> values) {
        touchedKeys.add(key);
        return props.putAll(key, values);
    }

    @Override
    public boolean putAll(@Nullable String key, String[] values) {
        touchedKeys.add(key);
        Set<String> valuesSet = new HashSet<String>();
        Collections.addAll(valuesSet, values);
        return props.putAll(key, valuesSet);
    }

    @Override
    public boolean putAll(Multimap<? extends String, ? extends String> multimap) {
        touchedKeys.addAll(multimap.keySet());
        return props.putAll(multimap);
    }

    @Override
    public boolean putAll(Map<? extends String, ? extends String> map) {
        touchedKeys.addAll(map.keySet());
        int sizeBefore = props.size();
        map.entrySet().forEach(entry ->
                props.put(entry.getKey(), entry.getValue())
        );
        return props.size() > sizeBefore;
    }

    @Override
    public boolean putAll(PropertiesInfo properties) {
        touchedKeys.addAll(properties.keySet());
        int sizeBefore = props.size();
        properties.entries().forEach(entry ->
                props.put(entry.getKey(), entry.getValue())
        );
        return props.size() > sizeBefore;
    }

    @Override
    public Set<? extends String> replaceValues(@Nonnull String key, Iterable<? extends String> values) {
        touchedKeys.add(key);
        return props.replaceValues(key, values);
    }

    @Override
    public void clear() {
        touchedKeys.addAll(propsOriginal.keySet());
        props.clear();
    }

    @Override
    public Set<String> removeAll(@Nonnull Object key) {
        touchedKeys.add((String) key);
        return props.removeAll(key);
    }

    @Override
    public boolean put(String key, String value) {
        touchedKeys.add(key);
        return props.put(key, value);
    }

    @Override
    public Collection<String> values() {
        return props.values();
    }

    @Override
    public Set<Map.Entry<String, String>> entries() {
        return props.entries();
    }

    @Override
    public Multiset<String> keys() {
        return props.keys();
    }

    @Override
    public Set<String> keySet() {
        return props.keySet();
    }

    @Override
    public boolean isEmpty() {
        return props.isEmpty();
    }

    @Override
    public boolean hasMandatoryProperty() {
        for (String qPropKey : props.keySet()) {
            if (qPropKey != null && qPropKey.endsWith(MANDATORY_SUFFIX)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(String key) {
        return props.containsKey(key);
    }

    @Override
    public MatchResult matchQuery(Properties queryProperties) {
        if (queryProperties.isEmpty()) {
            return MatchResult.NO_MATCH;
        }
        for (String qPropKey : queryProperties.keySet()) {
            //Hack - need to model query properties together with their control flags
            boolean mandatory = false;
            String propKey = qPropKey;
            if (qPropKey != null && qPropKey.endsWith(MANDATORY_SUFFIX)) {
                mandatory = true;
                propKey = qPropKey.substring(0, qPropKey.length() - MANDATORY_SUFFIX.length());
            }

            //If the key given from the query must exist
            if (mandatory) {

                //If the current properties contain the given key
                if (containsKey(propKey)) {

                    Set<String> queryPropertyValues = clearBlankAndReturnPropertyValues(queryProperties.get(qPropKey));

                    //Only check the current property values if the request property was given with values
                    if (!queryPropertyValues.isEmpty()) {

                        //The given query properties have a value, so we should try to match
                        Set<String> currentPropertyValue = clearBlankAndReturnPropertyValues(get(propKey));
                        if (!queryPropertyValues.equals(currentPropertyValue)) {

                            //The properties don't match
                            return MatchResult.CONFLICT;
                        }
                    }
                } else {
                    //Conflict since the key given from the properties is mandatory and doesn't exist in the current properties
                    return MatchResult.CONFLICT;
                }
            } else {

                Set<String> queryPropertyValues = clearBlankAndReturnPropertyValues(queryProperties.get(qPropKey));

                if (!queryPropertyValues.isEmpty()) {
                    //If the current properties contain the given query property key
                    if (containsKey(propKey)) {

                        //The given query properties have a value, so we should try to match
                        Set<String> currentPropertyValue = clearBlankAndReturnPropertyValues(get(propKey));

                        if (!queryPropertyValues.equals(currentPropertyValue)) {

                            //The properties conflict
                            return MatchResult.CONFLICT;
                        }
                    } else {
                        //The current property doesn't have the given query property, so it does not conflict either
                        return MatchResult.NO_MATCH;
                    }
                }
            }
        }
        return MatchResult.MATCH;
    }

    public long getNodeId() {
        return nodeId;
    }

    public boolean isChangeTrackingEnabled() {
        return changeTrackingEnabled;
    }

    public Collection<PropertyChange> getPropertyChanges() {
        if (log.isDebugEnabled()) {
            log.debug("Getting property changes of touched keys: {}", touchedKeys);
        }
        checkChangeTrackingEnabled();
        //TODO [YA] Possible improvement: less updates by also finding existing entries by value
        List<PropertyChange> changes = Lists.newArrayList();
        touchedKeys.forEach(key -> {
            Set<String> currValues = props.get(key);
            Set<PropertyWithId> origProps = propsOriginal.get(key);
            if (currValues != null && !currValues.isEmpty()) {
                if (origProps == null || origProps.isEmpty()) {
                    currValues.forEach(value -> changes.add(PropertyChange.create(key, value)));
                } else {
                    // Use existing original property entries to update with new values (override).
                    // Find original properties that still exist (with the same value), new values & original properties
                    // which their value does not exist.
                    List<String> currValuesList = currValues.stream()
                            .sorted(InternalStringUtils::compareNullLast)
                            .collect(Collectors.toCollection(LinkedList::new));
                    List<PropertyWithId> origPropsList = origProps.stream()
                            .sorted((p1,p2) -> compareNullLast(p1.getPropValue(),p2.getPropValue()))
                            .collect(Collectors.toCollection(LinkedList::new));
                    List<PropertyWithId> candidatesForUpdate = Lists.newArrayList();
                    List<String> newValues = Lists.newArrayList();
                    String currValue = null;
                    PropertyWithId origValue = null;
                    while (!origPropsList.isEmpty() && !currValuesList.isEmpty()) {
                        currValue = currValuesList.get(0);
                        origValue = origPropsList.get(0);
                        int compareResult = compareNullLast(currValue, origValue.getPropValue());
                        if (compareResult < 0) { // Current value does not exist in the originals
                            newValues.add(currValue);
                            currValuesList.remove(0);
                        } else if (compareResult > 0) { // Original value does not exist in the current values
                            candidatesForUpdate.add(origValue);
                            origPropsList.remove(0);
                        } else { //else - the values are equal, no need to change anything
                            currValuesList.remove(0);
                            origPropsList.remove(0);
                        }
                    }
                    // For new values, try to reuse original property entries
                    // Delete any existing entries if the new set of values is smaller than the existing
                    Iterator<String> newValuesIter = Iterators.concat(newValues.iterator(), currValuesList.iterator());
                    Iterator<PropertyWithId> origPropsIter = Iterators.concat(candidatesForUpdate.iterator(), origPropsList.iterator());
                    while (origPropsIter.hasNext()) {
                        PropertyWithId origProp = origPropsIter.next();
                        if (newValuesIter.hasNext()) {
                            String newValue = newValuesIter.next();
                            changes.add(PropertyChange.update(origProp, newValue));
                        } else {
                            changes.add(PropertyChange.delete(origProp));
                        }
                    }
                    // Create new entries if the new set of values is larger than the existing
                    while (newValuesIter.hasNext()) {
                        String newValue = newValuesIter.next();
                        changes.add(PropertyChange.create(key, newValue));
                    }
                }
            } else if (origProps != null && !origProps.isEmpty()) {
                //Delete all existing props of this key since they do not exist in the new props
                origProps.forEach(prop -> changes.add(PropertyChange.delete(prop)));
            }
        });
        log.debug("Found {} property changes", changes.size());
        return changes;
    }

    public void resetChangeTracking(@Nonnull Collection<PropertyChangeResult> changeResults) {
        checkChangeTrackingEnabled();
        log.debug("Resetting changes using {} change results", changeResults.size());
        touchedKeys.clear();
        Multimap<String, PropertyChangeResult> resultsByKey = LinkedListMultimap.create(changeResults.size());
        changeResults.forEach(result -> resultsByKey.put(result.getChange().getProperty().getPropKey(), result));
        resultsByKey.keySet().forEach(key -> {
            Set<PropertyWithId> origProps = propsOriginal.get(key);
            Set<PropertyWithId> updatedProps = Sets.newHashSet(origProps);
            Collection<PropertyChangeResult> propChanges = resultsByKey.get(key);
            propChanges.forEach(changeResult -> {
                PropertyWithId origProperty = changeResult.getChange().getProperty();
                if (changeResult.isSuccess()) {
                    PropertyWithId existingProp;
                    PropertyChangeType changeType = changeResult.getChange().getChangeType();
                    switch (changeType) {
                        case CREATE:
                            updatedProps.add(changeResult.getProperty());
                            break;
                        case UPDATE:
                            existingProp = findPropByIdOrThrow(updatedProps, origProperty.getPropId());
                            updatedProps.remove(existingProp);
                            updatedProps.add(changeResult.getProperty());
                            break;
                        case DELETE:
                            existingProp = findPropByIdOrThrow(updatedProps, origProperty.getPropId());
                            updatedProps.remove(existingProp);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected change type: " + changeType);
                    }
                } else {
                    touchedKeys.add(origProperty.getPropKey());
                }
            });
            propsOriginal.replaceValues(key, updatedProps);
        });
        if (log.isDebugEnabled() && !touchedKeys.isEmpty()) {
            log.debug("The following property keys had failures updating: {}", touchedKeys);
        }
    }

    private void checkChangeTrackingEnabled() {
        if (!changeTrackingEnabled) {
            throw new IllegalStateException("Change tracking was not enabled for these properties. " +
                    "You must check using 'isChangeTrackingEnabled' before calling this method.");
        }
    }

    private PropertyWithId findPropByIdOrThrow(Collection<PropertyWithId> props, long id) {
        return props.stream()
                .filter(p -> p.getPropId() == id)
                .findFirst().orElseThrow(() ->
                        new IllegalStateException("Property with ID " + id + " was not found."));
    }

    @Override
    public boolean equals(@Nullable Object that) {
        if (that instanceof PropertiesImpl) {
            PropertiesImpl otherProps = (PropertiesImpl) that;
            return this.props.equals(otherProps.props);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return props.hashCode();
    }

    @Override
    public String toString() {
        return props.toString();
    }

    /**
     * Returns a copy of the given property value set after clearing any blank\null values it might contain
     *
     * @param propertyValues Property value set. Can be null
     * @return Copy of given set without the null\blank values or an Empty set if given a null set
     */
    private Set<String> clearBlankAndReturnPropertyValues(Set<String> propertyValues) {
        Set<String> clearedPropertyValues = Sets.newHashSet();
        if (propertyValues == null) {
            return clearedPropertyValues;
        }

        for (String propertyValue : propertyValues) {
            if (StringUtils.isNotBlank(propertyValue)) {
                clearedPropertyValues.add(propertyValue);
            }
        }

        return clearedPropertyValues;
    }

    public enum PropertyChangeType {
        CREATE, UPDATE, DELETE
    }

    public static class PropertyChange {
        private final PropertyChangeType changeType;
        private final PropertyWithId property;

        public PropertyChange(PropertyChangeType changeType, PropertyWithId property) {
            this.changeType = changeType;
            this.property = property;
        }

        public PropertyChangeType getChangeType() {
            return changeType;
        }

        public PropertyWithId getProperty() {
            return property;
        }

        public static PropertyChange create(String key, String value) {
            return new PropertyChange(PropertyChangeType.CREATE, new Property(Property.NO_ID, key, value));
        }

        public static PropertyChange update(PropertyWithId prop, String newValue) {
            return new PropertyChange(PropertyChangeType.UPDATE, new Property(prop.getPropId(), prop.getPropKey(), newValue));
        }

        public static PropertyChange delete(PropertyWithId prop) {
            return new PropertyChange(PropertyChangeType.DELETE, prop);
        }

        private static class Property implements PropertyWithId {
            static final long NO_ID = -1;
            private final long propId;
            private final String propKey;
            private final String propValue;

            private Property(long propId, String propKey, String propValue) {
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

    public static class PropertyChangeResult {
        private final PropertyChange change;
        private final boolean success;
        private final PropertyWithId property;

        public PropertyChangeResult(PropertyChange change, boolean success, @Nullable PropertyWithId property) {
            this.change = change;
            this.success = success;
            this.property = property;
        }

        public PropertyChange getChange() {
            return change;
        }

        public boolean isSuccess() {
            return success;
        }

        public PropertyWithId getProperty() {
            return property;
        }
    }
}