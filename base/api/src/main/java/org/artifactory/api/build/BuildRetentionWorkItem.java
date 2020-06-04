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

package org.artifactory.api.build;

import org.artifactory.api.repo.WorkItem;
import org.artifactory.build.BuildRun;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Liza Dashevski
 */
public class BuildRetentionWorkItem extends WorkItem {
    private final BuildRun buildId;
    private final boolean deleteArtifacts;

    public BuildRetentionWorkItem(BuildRun buildId, boolean deleteArtifacts) {
        this.buildId = Objects.requireNonNull(buildId);
        this.deleteArtifacts = deleteArtifacts;
    }

    @Nonnull
    @Override
    public String getUniqueKey() {
        return buildId.toString();
    }

    public BuildRun getBuildId() {
        return buildId;
    }

    public boolean isDeleteArtifacts() {
        return deleteArtifacts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BuildRetentionWorkItem that = (BuildRetentionWorkItem) o;
        return deleteArtifacts == that.deleteArtifacts &&
                Objects.equals(buildId, that.buildId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildId, deleteArtifacts);
    }
}
