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

package org.artifactory.storage.db.bundle.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import static org.jfrog.common.ArgUtils.*;

/**
 * @author Tomer Mayost
 */
@Data
@NoArgsConstructor
public class BundleNode {
    long id;
    long nodeId;
    String repoPath;
    long bundleId;
    String originalFileDetails;
    private static final int MAX_COMPONENT_DETAILS_LENGTH_IN_BYTES = 1000;

    public void validate() {
        validate(nodeId, bundleId);
    }

    public void validate(long nodeId, long bundleId) {
        this.nodeId = requirePositive(nodeId, "nodeId must be positive");
        this.bundleId = requirePositive(bundleId, "id must be positive");
        this.repoPath = requireNonBlank(repoPath, "nodePath must not be blank");
        this.originalFileDetails = requireNullOrJson(originalFileDetails, originalFileDetails + " is not a valid json");
        this.originalFileDetails = requireMaxLength(originalFileDetails,
                MAX_COMPONENT_DETAILS_LENGTH_IN_BYTES, originalFileDetails + " is not a valid json");
    }
}
