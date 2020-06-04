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

package org.artifactory.api.statistics;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Yinon Avraham.
 */
public interface StatisticsService {

    /**
     * Get the top most downloaded paths.
     * @param limit the max number of results to return
     * @return an ordered list with the top most downloaded paths with their stats
     */
    @Nonnull
    List<PathWithStats> getMostDownloaded(int limit);

    /**
     * Flush and persist the stats currently collected only in memory.
     * <p>
     * <b>Use with care!!!</b>
     * </p>
     */
    void flushDownloadStatistics();
}
