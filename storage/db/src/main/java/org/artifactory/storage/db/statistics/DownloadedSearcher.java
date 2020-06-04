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

package org.artifactory.storage.db.statistics;

import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.domain.sensitive.AqlApiStatistic;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.rows.AqlStatistics;
import org.artifactory.repo.RepoPath;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.artifactory.aql.api.domain.sensitive.AqlApiStatistic.*;
import static org.artifactory.aql.api.internal.AqlBase.and;

/**
 * @author Saffi Hartal
 */
public class DownloadedSearcher {
    private final AqlService aqlService;

    public DownloadedSearcher(AqlService aqlService) {
        this.aqlService = aqlService;
    }

    public Stream<AqlStatistics> downloadedAfter(RepoPath rootRepoPath, long after, Consumer<Exception> onFail,
            long limit) {
        AqlBase.AndClause<AqlApiStatistic> filterBy = and(
                item().repo().equal(rootRepoPath.getRepoKey())
        );

        if (!rootRepoPath.isRoot()) {
            filterBy.append(item().path().matches(rootRepoPath.getPath() + "*"));
        }

        filterBy.append(downloaded().greater(after));

        return aqlService.executeQueryLazy(
                create()
                        .filter(filterBy)
                        .include(AqlApiItem.repo(),
                                AqlApiItem.path(),
                                AqlApiItem.name()
                        )
                        .addSortElement(downloaded())
                        .limit(limit)
                        .asc()
        ).asStream(onFail);
    }

    public Stream<AqlStatistics> remoteDownloadedAfter(RepoPath match, long after, Consumer<Exception> onFail,
            long limit) {
        AqlBase.AndClause<AqlApiStatistic> filterBy = and(
                item().repo().equal(match.getRepoKey())
        );

        if (!StringUtils.isBlank(match.getPath())) {
            filterBy.append(item().path().matches(match.getPath() + "*"));
        }

        filterBy.append(remoteDownloaded().greater(after));

        return aqlService.executeQueryLazy(
                create()
                        .filter(filterBy)
                        .include(AqlApiItem.repo(),
                                AqlApiItem.path(),
                                AqlApiItem.name(),
                                remoteDownloaded()
                        )
                        .addSortElement(remoteDownloaded())
                        .limit(limit)
                        .asc()
        ).asStream(onFail);
    }

    public Stream<AqlStatistics> downloadedOnTimestamp(RepoPath match, long timestamp, boolean remote,
            Consumer<Exception> onFail) {
        AqlBase.AndClause<AqlApiStatistic> filterBy = and(
                item().repo().equal(match.getRepoKey())
        );

        if (!StringUtils.isBlank(match.getPath())) {
            filterBy.append(item().path().matches(match.getPath() + "*"));
        }

        filterBy.append((remote ? remoteDownloaded() : downloaded()).equals(timestamp));

        return aqlService.executeQueryLazy(
                create()
                        .filter(filterBy)
                        .include(AqlApiItem.repo(),
                                AqlApiItem.path(),
                                AqlApiItem.name(),
                                remote ? remoteDownloaded() : downloaded())
        ).asStream(onFail);
    }
}
