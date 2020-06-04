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

package org.artifactory.ui.rest.model.artifacts.search.classsearch;

import org.artifactory.ui.rest.model.artifacts.search.BaseSearch;

/**
 * @author Chen Keinan
 */
public class ClassSearch extends BaseSearch {

    private String name;
    private String path;
    private boolean searchClassOnly;
    private boolean excludeInnerClasses;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSearchClassOnly() {
        return searchClassOnly;
    }

    public void setSearchClassOnly(boolean searchClassOnly) {
        this.searchClassOnly = searchClassOnly;
    }

    public boolean isExcludeInnerClasses() {
        return excludeInnerClasses;
    }

    public void setExcludeInnerClasses(boolean excludeInnerClasses) {
        this.excludeInnerClasses = excludeInnerClasses;
    }
}