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

package org.artifactory.api.properties;

import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.Lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Yossi Shaul
 */
public interface PropertiesService {

    String FILTERED_RESOURCE_PROPERTY_NAME = "filtered";

    String MAVEN_PLUGIN_PROPERTY_NAME = "artifactory.maven.mavenPlugin";

    String CONTENT_TYPE_PROPERTY_NAME = "content-type";

    boolean hasProperties(RepoPath repoPath);

    /**
     * * Retrieves item properties by the repo path. Prefer the more efficient
     * {@link PropertiesService#getProperties(ItemInfo)} if you have the item info
     *
     * @see PropertiesService#getProperties(org.artifactory.fs.ItemInfo)
     * @param repoPath The item (repository/folder/file) repository path
     * @return The properties attached to this repo path. Empty properties if non exist.
     */
    @Nonnull
    Properties getProperties(RepoPath repoPath);

    /**
     * Retrieves item properties by the item info. Prefer this more efficient method over
     * {@link PropertiesService#getProperties(RepoPath)} whenever possible.
     *
     * @param itemInfo The item info to retrieve properties for
     * @return The item properties
     */
    @Nonnull
    Properties getProperties(ItemInfo itemInfo);

    /**
     * Retrieves propKey values from all node ids in nodeIdList.
     * @param nodeIdList List of node ids to retrieve
     * @param propKey property key to retrieve its values
     * @return node id to list of property values
     */
    @Nonnull
    Map<Long,Set<String>> getProperties(List<Long> nodeIdList, String propKey);

    /**
     * Returns map of properties for the given repo paths
     *
     * @param repoPaths     Paths to extract properties for
     * @param mandatoryKeys Any property keys that should be mandatory for resulting properties. If provided, property
     *                      objects will be added to the map only if they contain all the given keys
     * @return Map of repo paths with their corresponding properties
     */
    Map<RepoPath, Properties> getProperties(Set<RepoPath> repoPaths, String... mandatoryKeys);

    /**
     * @return a mapping of nodeId -> {@link Properties} that are retrieved for all nodes under {@param repoKey}
     * that have property {@param propKey} with any values from {@param propValues}
     */
    Map<Long, Properties> getAllProperties(String repoKey, String propKey, List<String> propValues);

    /**
     * Adds (and stores) a property to the item at the repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet Property set to add - can be null
     * @param property    Property to add
     * @param values      Property values (if null, will not add the property)
     */
    @Lock
    void addProperty(RepoPath repoPath, @Nullable PropertySet propertySet, Property property,
            boolean updateAccessLogger,String... values);

    @Lock
    void addProperty(RepoPath repoPath, @Nullable PropertySet propertySet, Property property,String... values);

    /**
     * Recursively adds (and stores) a property to the item at the repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet Property set to add - can be null
     * @param property    Property to add
     * @param values      Property values (if null, will not add the property)
     */
    @Lock
    void addPropertyRecursively(RepoPath repoPath, @Nullable PropertySet propertySet, Property property,
            boolean updateAccessLogger, String... values);

    /**
     * Recursively adds (and stores) a property to the item at the repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet Property set to add - can be null
     * @param property    Property to add
     * @param values      Property values (if null, will not add the property)
     */
    @Lock
    void addPropertyRecursively(RepoPath repoPath, @Nullable PropertySet propertySet, Property property, String... values);

    /**
     * Recursively adds (and stores) a property to the item at the repo path in multiple transaction.
     *
     * @param repoPath    The item repo path
     * @param propertySet Property set to add - can be null
     * @param propertyMapFromRequest    Properties map from request
     */
    void addPropertyRecursivelyMultiple(RepoPath repoPath, @Nullable PropertySet propertySet,
            Map<Property, List<String>> propertyMapFromRequest, boolean updateAccessLogger);

    /**
     * @deprecated remove after migration-enforcing major
     */
    @Deprecated
    void addPropertySha256RecursivelyMultiple(RepoPath repoPath);

    /**
     * Edit a property on a specific repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet The property set to edit
     * @param property    The property to add
     * @param values      Property values
     */
    @Lock
    void editProperty(RepoPath repoPath, @Nullable PropertySet propertySet, Property property,
            boolean updateAccessLogger, String... values);

    /**
     * Sets the given properties on the target existing item. Overrides any existing properties.
     * This methods checks for annotate permissions.
     *
     * @param repoPath   Repo path of the item
     * @param properties properties object to overwrite
     * @param isInternalProperties true if the properties to set are internal metadata properties(Bypass interceptors),
     *                   false if the properties are set externally(Will invoke property interceptors like afterPropertyCreate)
     * @return True if properties were set successfully
     */
    @Lock
    boolean setProperties(RepoPath repoPath, Properties properties, boolean isInternalProperties);

    /**
     * Add the given values to item properties in single transaction.
     * This method takes lock on the repoPath and checks for annotate permissions.
     *
     * @param repoPath   Repo path of the item
     * @param propKey    Property key
     * @param isInternalProperties true if the properties to set are internal metadata properties(Bypass interceptors),
     *                   false if the properties are set externally(Will invoke property interceptors like afterPropertyCreate)
     * @return True if properties were set successfully
     */
    @Lock
    boolean addProperties(RepoPath repoPath, String propKey, Set<String> valuesToAdd, boolean isInternalProperties);

    /**
     * Remove the given value from item properties in single transaction.
     * This method takes lock on the repoPath and checks for annotate permissions.
     *
     * @param repoPath   Repo path of the item
     * @param propKey    Property key
     * @param isInternalProperties true if the properties to set are internal metadata properties(Bypass interceptors),
     *                   false if the properties are set externally(Will invoke property interceptors like afterPropertyCreate)
     * @return True if properties were set successfully
     */

    @Lock
    boolean removePropertyValues(RepoPath repoPath, String propKey, Set<String> valuesToRemove, boolean isInternalProperties);

    /**
     * Update node properties by specifying a set of properties to modify.
     * will not override unspecified properties. This method will not go through interceptors.
     * @param repoPath - update props on this path
     * @param newProperties - properties to be updated/removed - if a property doesn't have value it will be removed
     */
    @Lock
    Properties updateProperties(RepoPath repoPath, Properties newProperties);

    /**
     * Deletes the property from the item.
     *
     * @param repoPath The item repo path
     * @param property Property name to delete
     */
    @Lock
    boolean deleteProperty(RepoPath repoPath, String property, boolean updateAccessLogger);

    @Lock
    boolean deleteProperty(RepoPath repoPath, String property);

    /**
     * Deletes property from the item recursively.
     *
     * @param repoPath The item repo path
     * @param property Property name to delete
     */
    @Lock
    void deletePropertyRecursively(RepoPath repoPath, String property, boolean updateAccessLogger);

    /**
     * Deletes property from the item recursively.
     *
     * @param repoPath The item repo path
     * @param property Property name to delete
     */
    @Lock
    void deletePropertyRecursively(RepoPath repoPath, String property);

    @Lock
    boolean removeProperties(RepoPath repoPath);

}