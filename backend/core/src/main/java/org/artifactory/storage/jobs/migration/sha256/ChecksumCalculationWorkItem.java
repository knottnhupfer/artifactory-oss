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

package org.artifactory.storage.jobs.migration.sha256;

import org.artifactory.api.repo.WorkItem;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Dan Feldman
 */
public class ChecksumCalculationWorkItem extends WorkItem {

    private final String sha1;
    private final Collection<RepoPath> paths;
    private final Sha256MigrationJobDelegate delegate;

    public ChecksumCalculationWorkItem(String sha1, Collection<RepoPath> paths, Sha256MigrationJobDelegate delegate) {
        this.sha1 = sha1;
        this.paths = paths;
        this.delegate = delegate;
    }

    public String getSha1() {
        return sha1;
    }

    public Collection<RepoPath> getPaths() {
        return paths;
    }

    public Sha256MigrationJobDelegate getDelegate() {
        return delegate;
    }

    @Nonnull
    @Override
    public String getUniqueKey() {
        //Only one thread is allowed to run on a single sha1 value so we don't do double calculations
        return sha1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChecksumCalculationWorkItem)) {
            return false;
        }
        ChecksumCalculationWorkItem workItem = (ChecksumCalculationWorkItem) o;
        return Objects.equals(getSha1(), workItem.getSha1());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSha1());
    }
}
