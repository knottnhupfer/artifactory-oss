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

package org.artifactory.fs;

import org.artifactory.common.Info;

/**
 * Date: 8/3/11
 * Time: 1:24 PM
 *
 * @author Fred Simon
 */
public interface StatsInfo extends Info {
    String ROOT = "artifactory.stats";

    long getDownloadCount();
    long getLastDownloaded();
    String getLastDownloadedBy();

    long getRemoteDownloadCount();
    long getRemoteLastDownloaded();
    String getRemoteLastDownloadedBy();

    String getPath();
    String getOrigin();
}
