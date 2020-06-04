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
 * Interface with the most basic methods that are common to the local/remote/virtual repository configurations.
 * This interface is used for returning the user the repository configurations through the REST-API and must not contain
 * any information that is readable by admin user, but not by a regular user.
 *
 * @author Shay Bagants
 */
public interface CommonRepoConfig {

    String getDescription();

    String getType();

    String getPackageType();

    String getKey();
}
