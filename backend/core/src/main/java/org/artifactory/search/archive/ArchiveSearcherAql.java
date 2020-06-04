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

package org.artifactory.search.archive;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.archive.ArchiveSearchControls;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.aql.AqlConverts;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlArchiveEntryItem;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.search.AqlSearcherBase;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Searches for archive entries inside archive files.
 *
 * @author Gidi Shabat
 * @author Yossi Shaul
 */
public class ArchiveSearcherAql extends AqlSearcherBase<ArchiveSearchControls, ArchiveSearchResult> {

    @Override
    public ItemSearchResults<ArchiveSearchResult> doSearch(ArchiveSearchControls controls) {
        AqlApiItem aql = AqlApiItem.create().filter(
                AqlApiItem.and(
                        AqlApiItem.archive().entry().name().equal(controls.getName()),
                        AqlApiItem.archive().entry().path().matches(controls.getPath())
                )).include(AqlApiItem.archive().entry().name(), AqlApiItem.archive().entry().path());

        AqlEagerResult<AqlItem> result = executeQuery(aql, controls);
        Set<ArchiveSearchResult> results = Sets.newLinkedHashSet();
        results.addAll(result.getResults().stream()
                .map(aqlArtifact -> new ArchiveSearchResult(AqlConverts.toItemInfo.apply(aqlArtifact),
                        ((AqlArchiveEntryItem) aqlArtifact).getEntryName(),
                        ((AqlArchiveEntryItem) aqlArtifact).getEntryPath() + "/" +
                                ((AqlArchiveEntryItem) aqlArtifact).getEntryName(), true))
                .collect(Collectors.toList()));

        return new ItemSearchResults<>(Lists.newArrayList(results));
    }
}
