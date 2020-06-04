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

package org.artifactory.util;

import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;

import java.util.ArrayList;
import java.util.List;

import static org.artifactory.descriptor.property.PropertySet.ARTIFACTORY_RESERVED_PROP_SET;
import static org.artifactory.repo.config.RepoConfigDefaultValues.*;
import static org.artifactory.util.distribution.DistributionConstants.EDGE_UPLOADS_REPO_KEY;

/**
 * @author Rotem Kfir
 */
public class EdgeUtils {
    /**
     * Adds a local generic repository named EDGE_UPLOADS_REPO_KEY to mutableDescriptor.<p>
     * <b>NOTE:</b> Saving the altered descriptor is the responsibility of the caller
     */
    public static void addEdgeUploadsRepo(MutableCentralConfigDescriptor mutableDescriptor) {
        LocalRepoDescriptor repo = new LocalRepoDescriptor();
        repo.setKey(EDGE_UPLOADS_REPO_KEY);
        repo.setType(RepoType.Generic);
        repo.setDescription("A generic repository for uploads");
        repo.setIncludesPattern(DEFAULT_INCLUDES_PATTERN);
        repo.setRepoLayout(mutableDescriptor.getRepoLayout(DEFAULT_REPO_LAYOUT));
        repo.setBlackedOut(DEFAULT_BLACKED_OUT);
        repo.setPropertySets(getPropertySets(mutableDescriptor));

        mutableDescriptor.addLocalRepository(repo);
        mutableDescriptor.conditionallyAddToBackups(repo);
    }

    private static List<PropertySet> getPropertySets(MutableCentralConfigDescriptor mutableDescriptor) {
        PropertySet defaultPropertySet = mutableDescriptor.getPropertySets().stream()
                .filter(propertySet -> propertySet.getName().equals(ARTIFACTORY_RESERVED_PROP_SET))
                .findFirst()
                .orElse(null);
        if (defaultPropertySet == null) {
            return null;
        }
        List<PropertySet> propertySets = new ArrayList<>();
        propertySets.add(defaultPropertySet);
        return propertySets;
    }
}
