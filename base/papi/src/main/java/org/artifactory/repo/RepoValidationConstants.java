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

package org.artifactory.repo;

/**
 * This is what they call "air conditioning preparation" for the bright day in which we will have ONE unified validator
 * to confirm config descriptor data is valid via REST/UI/YAML updates.
 *
 * @author Yuval Reches
 */
public interface RepoValidationConstants {

    // Download redirect
    String DOWNLOAD_REDIRECT_AND_STORE_LOCALLY_ERROR = "Cannot set download redirect enabled while store artifacts " +
            "locally disabled";
    String DOWNLOAD_REDIRECT_NO_ENTERPRISE_PLUS_EDGE_ERROR = "Download redirect is configured for repository '%s' but is " +
            "only available on Enterprise Plus or Edge licensed Artifactory instances.";
}
