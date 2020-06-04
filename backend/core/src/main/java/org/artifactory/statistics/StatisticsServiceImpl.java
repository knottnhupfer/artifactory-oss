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

package org.artifactory.statistics;

import com.google.common.collect.Lists;
import org.artifactory.api.statistics.PathWithStats;
import org.artifactory.api.statistics.StatisticsService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.fs.dao.StatsDao;
import org.artifactory.storage.db.fs.entity.Stat;
import org.artifactory.storage.db.fs.service.AbstractStatsService;
import org.artifactory.storage.fs.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Yinon Avraham.
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatsDao statsDao;

    @Autowired
    @Qualifier("statsServiceImpl")
    private StatsService statsService;

    @Autowired
    private AqlService aqlService;

    @Nonnull
    @Override
    public List<PathWithStats> getMostDownloaded(int limit) {
        try {
            List<PathWithStats> mostDownloaded = Lists.newArrayList();
            List<Stat> topLocalStats = statsDao.getTopLocalStats(limit);
            topLocalStats.forEach(stat -> {
                AqlApiItem itemQuery = AqlApiItem.create().filter(AqlApiItem.itemId().equals(stat.getNodeId()));
                AqlEagerResult<AqlItem> items = aqlService.executeQueryEager(itemQuery);
                items.getResults().forEach(item -> { //There should be exactly 1, but still....
                    RepoPath path = AqlUtils.fromAql(item);
                    mostDownloaded.add(new PathWithStatsImpl(path, AbstractStatsService.statToStatsInfo(stat)));
                });
            });
            return mostDownloaded;
        } catch (SQLException e) {
            throw new RuntimeException("Could not get top most downloaded paths.", e);
        }
    }

    @Override
    public void flushDownloadStatistics() {
        statsService.flushStats();
    }
}
