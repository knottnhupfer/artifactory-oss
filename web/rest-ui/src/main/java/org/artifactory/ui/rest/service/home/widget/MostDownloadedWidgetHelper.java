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

package org.artifactory.ui.rest.service.home.widget;

import org.artifactory.api.statistics.PathWithStats;
import org.artifactory.api.statistics.StatisticsService;
import org.artifactory.common.ConstantValues;
import org.jfrog.common.CachedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Yinon Avraham.
 */
@Component
public class MostDownloadedWidgetHelper {

    private static final Logger log = LoggerFactory.getLogger(MostDownloadedWidgetHelper.class);
    private static final int MOST_DOWNLOADED_LIMIT = 10;

    @Autowired
    private StatisticsService statisticsService;

    private CachedValue<List<PathDownloadsStats>> mostDownloadedCache = CachedValue
            .loadUsing(() -> this.fetchMostDownloaded(MOST_DOWNLOADED_LIMIT))
            .async(new TaskExecutorAdapter(new SyncTaskExecutor())::submit)
            .defaultValue(Collections.emptyList())
            .initialLoadTimeout(15, TimeUnit.SECONDS)
            .expireAfterRefresh(ConstantValues.mostDownloadedCacheIdleTimeSecs.getLong(), TimeUnit.SECONDS)
            .name("MostDownloaded")
            .build();

    /**
     * Get the most downloaded paths
     * @return a list of {@link PathDownloadsStats}
     */
    @Nonnull
    List<PathDownloadsStats> getMostDownloaded(boolean forceRefresh) {
        if (forceRefresh) {
            try {
                statisticsService.flushDownloadStatistics();
            } catch (Exception e) {
                log.debug("Ignoring failure to flush download statistics.", e);
            }
        }
        return mostDownloadedCache.get(forceRefresh);
    }

    private List<PathDownloadsStats> fetchMostDownloaded(int limit) {
        log.debug("Fetching most downloaded, limit={}", limit);

        List<PathWithStats> mostDownloadedPathWithStats = statisticsService.getMostDownloaded(limit);
        List<PathDownloadsStats> mostDownloaded = mostDownloadedPathWithStats.stream()
                .map(pws -> new PathDownloadsStats(pws.getPath(), pws.getStats().getDownloadCount()))
                .collect(Collectors.toList());

        log.debug("Fetched {} most downloaded results", mostDownloaded.size());
        return mostDownloaded;
    }

}
