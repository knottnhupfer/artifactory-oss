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

import javax.annotation.Nonnull;

/**
 * @author gidis
 */
public class MavenMetadataPluginWorkItem extends WorkItem {
    private final String localRepo;

    public MavenMetadataPluginWorkItem(String localRepo) {
        this.localRepo = localRepo;
    }

    public String getLocalRepo() {
        return localRepo;
    }

    @Override
    public String toString() {
        return "MavenMetadataPluginWorkItem{" +
                "localRepo='" + localRepo + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MavenMetadataPluginWorkItem that = (MavenMetadataPluginWorkItem) o;

        return localRepo != null ? localRepo.equals(that.localRepo) : that.localRepo == null;

    }

    @Override
    public int hashCode() {
        return localRepo != null ? localRepo.hashCode() : 0;
    }

    @Override
    @Nonnull
    public String getUniqueKey() {
        return localRepo;
    }
}
