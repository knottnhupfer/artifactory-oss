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

import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Yinon Avraham.
 */
public class PathDownloadsStats {

    private final RepoPath repoPath;
    private final long downloads;

    public PathDownloadsStats(@Nonnull RepoPath repoPath, long downloads) {
        this.repoPath = Objects.requireNonNull(repoPath, "repoPath is required");
        this.downloads = downloads;
    }

    @Nonnull
    public RepoPath getRepoPath() {
        return repoPath;
    }

    public long getDownloads() {
        return downloads;
    }
}
