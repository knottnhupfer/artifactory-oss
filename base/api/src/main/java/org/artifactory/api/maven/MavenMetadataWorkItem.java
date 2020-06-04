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

package org.artifactory.api.maven;

import org.artifactory.api.repo.WorkItem;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;

/**
 * @author gidis
 */
public class MavenMetadataWorkItem extends WorkItem {
    private final RepoPath repoPath;
    private final boolean recursive;

    public MavenMetadataWorkItem(RepoPath repoPath, boolean recursive) {
        this.repoPath = repoPath;
        this.recursive = recursive;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public String toString() {
        return "MavenMetadataWorkItem{" +
                "repoPath=" + repoPath +
                ", recursive=" + recursive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MavenMetadataWorkItem that = (MavenMetadataWorkItem) o;
        if (recursive != that.recursive) return false;
        return repoPath.equals(that.repoPath);

    }

    @Override
    public int hashCode() {
        int result = repoPath.hashCode();
        result = 31 * result + (recursive ? 1 : 0);
        return result;
    }

    @Override
    @Nonnull
    public String getUniqueKey() {
        return this.toString();
    }
}
