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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds a list of search results
 *
 * @author Noam Tenne
 */
@XStreamAlias("searchResults")
public class ItemSearchResults<T extends ItemSearchResult> implements Serializable {

    @XStreamImplicit(itemFieldName = "searchResult")
    private Stream<T> results;
    private List<T> resultsList = null;

    private long fullResultsCount;

    private long time;

    /**
     * Constructor - A list of SearchResult objects
     *
     * @param results
     */
    public ItemSearchResults(List<T> results) {
        this.results = results.stream();
        this.fullResultsCount = -1L;
    }

    public ItemSearchResults(Stream<T> results) {
        this.results = results;
        this.fullResultsCount = -1L;
    }

    /**
     * Constructor
     *
     * @param results - A list of SearchResult objects
     * @param count   - The number of the complete amount of search results (including the excluded results, like
     *                checksums)
     */
    public ItemSearchResults(List<T> results, long count) {
        this.results = results.stream();
        this.fullResultsCount = count;
    }

    /**
     * Returns List the result group
     *
     * @return List<ResultEntry> - A list of SearchResult objects
     */
    public List<T> getResults() {
        if (resultsList == null) {
            resultsList = results.collect(Collectors.toList());
        }
        return resultsList;
    }

    /**
     * Sets the result group
     *
     * @param results - A list of SearchResult objects
     */
    public void setResults(List<T> results) {
        this.results = results.stream();
        this.resultsList = null;
    }

    /**
     * Results as stream
     *
     * @return
     */
    public Stream<T> getStream() {
        return results;
    }

    /**
     * Returns the counter of the full amount of results that were returned in the search (including filtered results).
     * May return -1 if the total number of results in unknown for performance reasons.
     *
     * @return long - Full size of results, or -1 if unkonwn
     */
    public long getFullResultsCount() {
        return fullResultsCount;
    }

    /**
     * Sets the amount for the counter of all the results returned from the search
     *
     * @param fullResultsCount - Full amount of results
     */
    public void setFullResultsCount(long fullResultsCount) {
        this.fullResultsCount = fullResultsCount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}