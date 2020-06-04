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

package org.artifactory.storage.jobs.migration.pathchecksum;

import org.artifactory.api.repo.WorkItem;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author gidis
 */
public class RepoPathChecksumCalculationWorkItem extends WorkItem {

    private RepoPath repoPath;
    private RepoPathChecksumMigrationJobDelegate delegate;

    RepoPathChecksumCalculationWorkItem(RepoPath repoPath, RepoPathChecksumMigrationJobDelegate delegate) {
        this.repoPath = repoPath;
        this.delegate = delegate;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public RepoPathChecksumMigrationJobDelegate getDelegate() {
        return delegate;
    }

    @Nonnull
    @Override
    public String getUniqueKey() {
        //Only one thread is allowed to run on a single sha1 value so we don't do double calculations
        return repoPath.toPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RepoPathChecksumCalculationWorkItem)) {
            return false;
        }
        RepoPathChecksumCalculationWorkItem workItem = (RepoPathChecksumCalculationWorkItem) o;
        return Objects.equals(repoPath, workItem.getRepoPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRepoPath());
    }
}
