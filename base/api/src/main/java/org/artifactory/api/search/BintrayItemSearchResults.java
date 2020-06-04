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

package org.artifactory.api.search;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.List;

/**
 * Bintray Search result
 *
 * @author Gidi Shabat
 */
public class BintrayItemSearchResults<T> implements Serializable {

    @XStreamImplicit(itemFieldName = "searchResult")
    private List<T> results;
    private long rangeLimitTotal;

    public BintrayItemSearchResults(List<T> results, long rangeLimitTotal) {
        this.results = results;
        this.rangeLimitTotal = rangeLimitTotal;
    }

    public List<T> getResults() {
        return results;
    }

    public long getRangeLimitTotal() {
        return rangeLimitTotal;
    }
}