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

package org.artifactory.addon;

import com.google.common.collect.Maps;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.RepoResource;
import org.artifactory.md.Properties;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.resource.FileResource;
import org.artifactory.rest.exception.MissingRestAddonException;
import org.artifactory.util.KeyValueList;
import org.jfrog.common.StringList;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public interface PropertiesAddon extends Addon {

    /**
     * Save properties on a certain path (which must be a valid {@link org.artifactory.repo.RepoPath})
     *
     * @param path       The path on which to set the properties
     * @param recursive  Whether the property attachment should be recursive.
     * @param properties The properties to attach as a list.
     * @return The response of the operation
     */
    default Response savePathProperties(String path, String recursive, KeyValueList properties, String atomic) {
        throw new MissingRestAddonException();
    }

    default Response savePathProperties(String path, String recursive, Map<Property, List<String>> propertyMapFromRequest,
            String atomic) {
        throw new MissingRestAddonException();
    }

    default Response deletePathProperties(String path, String recursive, StringList properties) {
        throw new MissingRestAddonException();
    }

    /**
     * Returns properties for the given repo path.
     *
     * @param repoPath Path to extract properties for
     * @return Properties of the repo path
     * @deprecated Moved from Addons to repo service
     */
    @Deprecated
    default Properties getProperties(RepoPath repoPath) {
        return (Properties) InfoFactoryHolder.get().createProperties();
    }

    /**
     * Returns map of properties for the given repo paths.
     *
     * @param repoPaths Paths to extract properties for
     * @return Map of repo paths with their corresponding properties
     */
    default Map<RepoPath, Properties> getProperties(Set<RepoPath> repoPaths) {
        return Maps.newHashMap();
    }

    /**
     * Deletes the property from the item.
     *
     * @param repoPath The item repo path
     * @param property Property name to delete
     */
    default void deleteProperty(RepoPath repoPath, String property) {
    }

    /**
     * Adds (and stores) a property to the item at the repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet Property set to add - can be null
     * @param property    Property to add
     * @param values      Property values (if null, will not add the property)
     */
    default void addProperty(RepoPath repoPath, PropertySet propertySet, Property property, String... values) {
    }

    /**
     * set properties
     * @param repoPath - node repo path
     * @param properties - node properties
     */
    default void setProperties(RepoPath repoPath, Properties properties) {
    }

    /**
     * Assemble a custom maven-metadata.xml according to the metadata definitions and matrix params in conjunction with
     * the existing properties already on the node.
     */
    default RepoResource assembleDynamicMetadata(InternalRequestContext context, RepoPath metadataRepoPath) {
        return new FileResource(ContextHelper.get().getRepositoryService().getFileInfo(metadataRepoPath));
    }

    /**
     * update remote file properties
     *
     * @param repo - repo descriptor interface
     */
    default void updateRemoteProperties(Repo repo, RepoPath repoPath) {
    }
}
