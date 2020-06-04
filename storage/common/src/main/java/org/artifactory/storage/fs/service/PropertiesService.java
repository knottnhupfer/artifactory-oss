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

package org.artifactory.storage.fs.service;

import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.GCCandidate;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A business service to interact with the node properties table.
 *
 * @author Yossi Shaul
 */
public interface PropertiesService {

    //TODO: [by YS] use properties info

    /**
     * @param itemInfo The item to fetch properties for
     * @return The properties of the specific item.
     */
    @Nonnull
    Properties getProperties(ItemInfo itemInfo);

    /**
     * @param repoPath The repo path to fetch properties for
     * @return The properties of the specific repo path.
     */
    @Nonnull
    Properties getProperties(RepoPath repoPath);

    /**
     * @return a mapping of nodeId -> {@link Properties} that are retrieved for all nodes under {@param repoKey}
     * that have property {@param propKey} with any values from {@param propValues}
     */
    Map<Long, Properties> getAllProperties(String repoKey, String propKey, List<String> propValues);

    /**
     * Get all properties that their value size is longer than the requested parameter
     *
     * @param size size to use when returning the long properties
     * @return all properties that their values is longer than the requested size
     */
    Map<String, List<String>> getAllPropsLongerThan(int size);

    /**
     * Trim property values that are longer than the max allowed length. This method affect PostgreSQL only.
     */
    int trimPropertyValuesToAllowedLength();

    boolean hasProperties(RepoPath repoPath);

    /**
     * Sets the given properties on the node id. Existing properties are overridden. An empty object will cause a
     * removal of all the properties on this node.
     *
     * @param nodeId     Id of the node to set properties on
     * @param properties The properties to set
     * @return true if the database has changed as part of this method call
     */
    boolean setProperties(long nodeId, Properties properties);

    /**
     * Sets the given properties on the node id. Existing properties are overridden. An empty object will cause a
     * removal of all the properties on this node.
     *
     * @param repoPath   RepoPath of the node
     * @param properties The properties to set
     */
    void setProperties(RepoPath repoPath, Properties properties);

    int deleteProperties(long nodeId);

    List<GCCandidate> getGCCandidatesFromTrash(String validFrom);

    Map<Long, Set<String>> getProperties(List<Long> nodeIdList, String propKey);
}
