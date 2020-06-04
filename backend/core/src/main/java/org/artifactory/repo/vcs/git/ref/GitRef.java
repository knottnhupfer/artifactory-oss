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

package org.artifactory.repo.vcs.git.ref;

/**
 * @author Shay Yaakov
 */
public class GitRef {
    public String name;
    public String commitId;
    public boolean isBranch;

    public GitRef(String name, String commitId, boolean isBranch) {
        this.name = name;
        this.commitId = commitId;
        this.isBranch = isBranch;
    }

    public String filename(String gitRepo, String ext) {
        String branchTag = isBranch ? this.name  + "-" + commitId : this.name;
        return gitRepo + "-" + branchTag + "." + ext;
    }
}
