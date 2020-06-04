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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.util;

/**
 * @author Lior Gur
 */
public interface PackageNativeConstants {
    String SORT_BY_LAST_MODIFIED = "lastModified";
    String ORDER_DESC = "desc";
    String EMPTY_KEYWORD = "no_content";
    //npm props
    String NPM_NAME = "npm.name";
    String NPM_VERSION = "npm.version";
    String NPM_KEYWORDS = "npm.keywords";
    String NPM_DESCRIPTION = "npm.description";

    String ARTIFACTORY_LIC = "artifactory.licenses";

    String NPM_PACKAGE_SCOPES = "npmScope";

}
