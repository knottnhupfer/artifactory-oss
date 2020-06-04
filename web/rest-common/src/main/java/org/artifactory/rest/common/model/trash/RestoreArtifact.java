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

package org.artifactory.rest.common.model.trash;


import org.artifactory.rest.common.model.artifact.BaseArtifact;

/**
 * @author Shay Yaakov
 */
public class RestoreArtifact extends BaseArtifact {

    private String targetRepoKey;
    private String targetPath;
    private Integer transactionSize;

    public RestoreArtifact() {
    }

    public RestoreArtifact(String name) {
        super(name);
    }

    public String getTargetRepoKey() {
        return targetRepoKey;
    }

    public void setTargetRepoKey(String targetRepoKey) {
        this.targetRepoKey = targetRepoKey;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public void setTransactionSize(Integer transactionSize) {
        this.transactionSize = transactionSize;
    }

    public Integer getTransactionSize() {
        return transactionSize;
    }
}
