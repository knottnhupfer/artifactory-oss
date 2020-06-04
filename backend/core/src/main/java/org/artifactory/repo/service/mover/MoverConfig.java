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

package org.artifactory.repo.service.mover;

import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

/**
 * Repo path mover configuration object
 *
 * @author Noam Y. Tenne
 */
public class MoverConfig {
    private final RepoPath fromRepoPath;
    private final RepoPath targetLocalRepoPath;
    private final boolean copy;
    private final boolean dryRun;
    private final boolean executeMavenMetadataCalculation;
    private final boolean pruneEmptyFolders;
    private final Properties properties;
    private boolean overrideProperties;
    private final boolean suppressLayouts;
    private final boolean failFast;
    private final boolean unixStyleBehavior;
    private final boolean atomic;
    private final boolean toCache;

    /**
     * Main constructor
     *
     * @param fromRepoPath       Source repo path
     * @param copy               Is a copy being performed
     * @param dryRun             Is the current run a dry one (no items actually moved)
     * @param executeMavenMetadataCalculation
     *                           Should immediately execute metadata calculation, or schedule
     * @param pruneEmptyFolders  If should prune empty folders after move
     * @param overrideProperties True for overriding existing properties, false for adding to existing properties
     * @param suppressLayouts    True if path translation across different layouts should be suppressed.
     * @param failFast           Flag to indicate whether the operation should fail upon encountering an error.
     * @param unixStyleBehavior  Use Unix-style behavior when dealing with existing nested folders
     */
    public MoverConfig(RepoPath fromRepoPath, RepoPath targetLocalRepoPath, boolean copy,
            boolean dryRun, boolean executeMavenMetadataCalculation, boolean pruneEmptyFolders, Properties properties,
                       boolean overrideProperties, boolean suppressLayouts, boolean failFast, boolean unixStyleBehavior,
                       boolean atomic, boolean toCache) {
        this.fromRepoPath = fromRepoPath;
        this.targetLocalRepoPath = targetLocalRepoPath;
        this.copy = copy;
        this.dryRun = dryRun;
        this.executeMavenMetadataCalculation = executeMavenMetadataCalculation;
        this.pruneEmptyFolders = pruneEmptyFolders;
        this.properties = properties;
        this.overrideProperties = overrideProperties;
        this.suppressLayouts = suppressLayouts;
        this.failFast = failFast;
        this.unixStyleBehavior = unixStyleBehavior;
        this.atomic = atomic;
        this.toCache = toCache;
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * Returns the repo path of the source
     *
     * @return Source repo path
     */
    public RepoPath getFromRepoPath() {
        return fromRepoPath;
    }

    /**
     * @return Returns the target repo path (if null, the target path should be in targetLocalRepoKey)
     */
    public RepoPath getTargetLocalRepoPath() {
        return targetLocalRepoPath;
    }

    /**
     * Indicates if a copy is being performed
     *
     * @return True if performing a copy, false if not
     */
    public boolean isCopy() {
        return copy;
    }

    /**
     * Indicates if the current run is a dry one (no items actually moved)
     *
     * @return True if run is dry, false if not
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * Indicates if the metadata should be calculated immediately or scheduled
     *
     * @return True if metadata should be calculated immediately, false if not
     */
    public boolean isExecuteMavenMetadataCalculation() {
        return executeMavenMetadataCalculation;
    }

    /**
     * Indicates if search results are being moved (will perform empty dir cleanup)
     *
     * @return True if search results are being moved, false if not
     */
    public boolean isPruneEmptyFolders() {
        return pruneEmptyFolders;
    }

    /**
     * Indicates whether path translation across different layouts should be suppressed.
     *
     * @return True if path translation should be suppressed
     */
    public boolean isSuppressLayouts() {
        return suppressLayouts;
    }

    /**
     * Indicates whether the operation should fail upon encountering an error.
     *
     * @return True if the operation should fail on the first error
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * Indicates whether to deal with existing nested folders like unix when copying or moving:
     * if a source folder being copied already exists in the target, another folder with the same name
     * will be created under the target to avoid overwriting the target.
     * i.e. when copying source path org/jfrog/1 to target path org/jfrog/1 , the result will be org/jfrog/1/1
     *
     * @return True if Unix-style behavior should be used when dealing with existing nested folders.
     */
    public boolean isUnixStyleBehavior() {
        return unixStyleBehavior;
    }

    public boolean isAtomic() {
        return atomic;
    }

    public boolean isOverrideProperties() {
        return overrideProperties;
    }

    /**
     * Indicates whether the operation should fail when the target is a cache repository.
     *
     * @return True if the operation should NOT fail when the target is a cache repository
     */
    public boolean isToCache() {
        return toCache;
    }
}
