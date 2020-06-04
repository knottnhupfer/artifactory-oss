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
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.Lock;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author Chen  Keinan
 */
public interface LockableAddProperties {

    /**
     * Recursively adds (and stores) a property to the item at the repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet Property set to add - can be null
     * @param propertyMapFromRequests    Property map from request
     */
    @Lock
    void addPropertyInternalMultiple(RepoPath repoPath, @Nullable PropertySet propertySet,
                                     Map<Property, List<String>> propertyMapFromRequests,boolean updateAccessLogger);

    /**
     * @deprecated Sha2 now available by default after passing the required conversion
     */
    @Lock
    @Deprecated
    void addSha256PropertyInternalMultiple(FileInfo itemInfo);
}