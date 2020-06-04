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

package org.artifactory.addon.build;

import org.artifactory.addon.Addon;
import org.artifactory.api.build.model.diff.BuildsDiffBaseFileModel;
import org.artifactory.api.rest.build.diff.BuildsDiff;
import org.artifactory.build.BuildRun;
import org.jfrog.build.api.Build;

import java.util.List;

/**
 * Reason for this addon is the Build JSON and Build diff in OSS version --> they are forbidden
 *
 * @author Chen Keinan
 */
public interface ArtifactBuildAddon extends Addon {

    /**
     * Used for Build JSON in the UI's Artifact Builds tab
     */
    default BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted) {
        return null;
    }

    default BuildsDiff getBuildsDiff(Build firstBuild, Build secondBuild, String baseStorageInfoUri) {
        return null;
    }

    default List<BuildsDiffBaseFileModel> compareArtifacts(Build build, Build secondBuild) {
        return null;
    }

    default List<BuildsDiffBaseFileModel> compareDependencies(Build build, Build secondBuild) {
        return null;
    }

}
