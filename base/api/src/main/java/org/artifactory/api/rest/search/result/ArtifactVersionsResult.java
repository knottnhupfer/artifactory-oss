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

package org.artifactory.api.rest.search.result;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Search result object to be returned by REST artifact versions searches
 *
 * @author Shay Yaakov
 */
public class ArtifactVersionsResult {
    private List<VersionEntry> results;

    /**
     * Default constructor for JSON parsing
     */
    public ArtifactVersionsResult() {
    }

    public ArtifactVersionsResult(Collection<VersionEntry> versionEntries, final Comparator<String> versionComparator) {
        if (versionEntries == null) {
            this.results = Collections.emptyList();
        } else {
            // sort according to the input string version comparator
            this.results = versionEntries.stream()
                    .sorted((version1, version2) -> versionComparator.compare(version2.getVersion(), version1.getVersion()))
                    .collect(Collectors.toList());
        }
    }

    @Nonnull
    public List<VersionEntry> getResults() {
        return results;
    }

    public void setResults(List<VersionEntry> results) {
        this.results = results;
    }
}
