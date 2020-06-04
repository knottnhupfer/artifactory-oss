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

package org.artifactory.search;

import org.artifactory.api.search.stats.StatsSearchResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.service.InternalRepositoryService;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Saffi Hartal
 * Helper for getting artifact stats using VisibleAqlItemsSearchHelper as stream
 */
public class AqlItemsToStatsHelper {

    private final VisibleAqlItemsSearchHelper visibleAqlItemsHelper;

    public AqlItemsToStatsHelper(@Nonnull VisibleAqlItemsSearchHelper visibleAqlItemsHelper) {
        this.visibleAqlItemsHelper = visibleAqlItemsHelper;
    }

    /**
     * stats conversion
     */
    public Stream<StatsSearchResult> toStatsSearchResultStream(Stream<AqlItem> stream) {
        InternalRepositoryService repoService = visibleAqlItemsHelper.repoService;

        Function<ItemInfo, StatsSearchResult> itemInfoToStatsSearchResult =
                item -> new StatsSearchResult(item, repoService.getStatsInfo(item.getRepoPath()));

        return visibleAqlItemsHelper.visibleItemInfo(stream).map(itemInfoToStatsSearchResult);
    }
}
