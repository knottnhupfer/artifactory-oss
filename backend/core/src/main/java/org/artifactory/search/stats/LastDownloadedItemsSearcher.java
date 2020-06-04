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

package org.artifactory.search.stats;

import org.artifactory.api.search.StreamingSearcher;
import org.artifactory.api.search.stats.StatsSearchControls;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.search.VisibleAqlItemsSearchHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.artifactory.aql.api.internal.AqlBase.and;
import static org.artifactory.aql.api.internal.AqlBase.or;

/**
 * @author Saffi Hartal - a searcher object which builds the query according to the controls
 * uses an helper which does the actual query and filter the visible valid results.
 */
public class LastDownloadedItemsSearcher implements StreamingSearcher<StatsSearchControls, AqlItem> {
    private static final Logger log = LoggerFactory.getLogger(LastDownloadedItemsSearcher.class);

    private VisibleAqlItemsSearchHelper searcher;

    public LastDownloadedItemsSearcher(@Nonnull VisibleAqlItemsSearchHelper searcher) {
        this.searcher = searcher;
    }

    @Override
    public Stream<AqlItem> searchAsStream(StatsSearchControls controls, Consumer<Exception> onFinish) {
        AqlBase.AndClause<AqlApiItem> query = buildQuery(controls);

        return searcher.searchVisibleArtifactsAsStream(query, onFinish);
    }

    private AqlBase.AndClause<AqlApiItem> buildQuery(StatsSearchControls controls) {
        AqlBase.OrClause<AqlApiItem> reposToSearchFilter = searcher.getSelectedReposForSearchClause(controls);

        long since = controls.getDownloadedSince().getTimeInMillis();
        //If createdBefore is not specified will only return artifacts that were created before downloadedSince
        long createdBefore = since;
        if (controls.hasCreatedBefore()) {
            createdBefore = controls.getCreatedBefore().getTimeInMillis();
        }
        AqlBase.AndClause<AqlApiItem> query = and();
        if (!reposToSearchFilter.isEmpty()) {
            log.debug("Filtering not used since search by repos: {}", controls.getSelectedRepoForSearch());
            query.append(reposToSearchFilter);
        }
        query.append(
                or(
                        AqlApiItem.statistic().downloaded().less(since),
                        AqlApiItem.statistic().downloaded().equals(null)
                )
        );
        query.append(
                or(
                        AqlApiItem.statistic().remoteDownloaded().less(since),
                        AqlApiItem.statistic().remoteDownloaded().equals(null)
                )
        );
        query.append(AqlApiItem.created().less(createdBefore));
        return query;
    }
}
