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

package org.artifactory.security;

/**
 * @author Yuval Reches
 */
public interface PermissionTarget {
    String DELIMITER = ",";

    String ANY_PERMISSION_TARGET_NAME = "Anything";
    String ANY_REMOTE_PERMISSION_TARGET_NAME = "Any Remote";
    String ANY_PATH = "**";
    String ANY_REPO = "ANY";
    String ANY_LOCAL_REPO = "ANY LOCAL";
    String ANY_REMOTE_REPO = "ANY REMOTE";
    String ANY_DISTRIBUTION_REPO = "ANY DISTRIBUTION";
    String DEFAULT_BUILD_PERMISSION_TARGET_NAME = "artifactory-system-default-build-permission";

    String getName();

}
