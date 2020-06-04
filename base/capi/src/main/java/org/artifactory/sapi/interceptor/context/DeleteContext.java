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

package org.artifactory.sapi.interceptor.context;

import org.artifactory.repo.RepoPath;

/**
 * A context to pass when performing item deletion.
 *
 * @author Yossi Shaul
 */
public class DeleteContext {

    private final RepoPath repoPath;
    private boolean calculateMavenMetadata;
    private boolean triggeredByMove;
    private boolean avoidBuildDeleteInterceptor;

    public DeleteContext(RepoPath repoPath) {
        this.repoPath = repoPath;
    }

    /**
     * @return The original repo path send to delete.
     */
    public RepoPath getRepoPath() {
        return repoPath;
    }

    public boolean isCalculateMavenMetadata() {
        return calculateMavenMetadata;
    }

    public DeleteContext calculateMavenMetadata() {
        return calculateMavenMetadata(true);
    }

    public DeleteContext calculateMavenMetadata(boolean calculateMavenMetadata) {
        this.calculateMavenMetadata = calculateMavenMetadata;
        return this;
    }

    public boolean isTriggeredByMove() {
        return triggeredByMove;
    }

    public DeleteContext triggeredByMove() {
        triggeredByMove = true;
        return this;
    }

    public boolean isAvoidBuildDeleteInterceptor() {
        return avoidBuildDeleteInterceptor;
    }

    public DeleteContext avoidBuildDeleteInterceptor() {
        avoidBuildDeleteInterceptor = true;
        return this;
    }
}
