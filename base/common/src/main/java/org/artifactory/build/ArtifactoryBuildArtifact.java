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

package org.artifactory.build;

import org.artifactory.fs.FileInfo;
import org.jfrog.build.api.Artifact;

/**
 * @author Dan Feldman
 */
public class ArtifactoryBuildArtifact {

    private final FileInfo fileInfo;
    private final Artifact artifact;

    public ArtifactoryBuildArtifact(Artifact artifact, FileInfo fileInfo) {
        this.artifact = artifact;
        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArtifactoryBuildArtifact)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ArtifactoryBuildArtifact that = (ArtifactoryBuildArtifact) o;
        if (!artifact.equals(that.artifact)) {
            return false;
        }
        if (getFileInfo() != null && that.getFileInfo() != null) {
            return getFileInfo().isIdentical(that.getFileInfo());
        } else {
            return getFileInfo() != null || that.getFileInfo() != null;
        }
    }

    @Override
    public int hashCode() {
        int result = artifact != null ? artifact.hashCode() : 1;
        result = 31 * result + (getFileInfo() != null ? getFileInfo().hashCode() : 0);
        return result;
    }
}
