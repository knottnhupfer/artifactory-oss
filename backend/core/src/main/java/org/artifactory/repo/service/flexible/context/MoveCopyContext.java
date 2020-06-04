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

package org.artifactory.repo.service.flexible.context;

import com.google.common.collect.Lists;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.flexible.MoveCopyItemInfo;

import java.util.List;

/**
 * @author gidis
 */
public class MoveCopyContext {
    private boolean executeMavenMetadataCalculation;
    private RepoPath sourceRepoPath;
    private String targetKey;
    private String targetPath;
    private Properties addProps;
    private List<String> removeProps;
    private boolean dryRun;
    private boolean failFast;
    private int transactionSize;
    private boolean copy;
    private boolean pruneEmptyFolders;
    private boolean unixStyleBehavior;
    private boolean suppressLayouts;
    private List<MoveCopyItemInfo> foldersToDelete = Lists.newArrayList();

    public RepoPath getSourceRepoPath() {
        return sourceRepoPath;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public Properties getAddProps() {
        return addProps;
    }

    public List<String> getRemoveProps() {
        return removeProps;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public int getTransactionSize() {
        return transactionSize;
    }

    public boolean isCopy() {
        return copy;
    }

    public boolean isExecuteMavenMetadataCalculation() {
        return executeMavenMetadataCalculation;
    }

    public boolean isPruneEmptyFolders() {
        return pruneEmptyFolders;
    }

    public boolean isUnixStyleBehavior() {
        return unixStyleBehavior;
    }

    public boolean isSuppressLayouts() {
        return suppressLayouts;
    }

    public MoveCopyContext(RepoPath sourceRepoPath, String targetKey, String targetPath) {
        this.sourceRepoPath = sourceRepoPath;
        this.targetKey = targetKey;
        this.targetPath = targetPath;
    }

    public MoveCopyContext setCopy(boolean copy) {
        this.copy = copy;
        return this;
    }

    public MoveCopyContext setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public MoveCopyContext setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    public MoveCopyContext setExecuteMavenMetadataCalculation(boolean executeMavenMetadataCalculation) {
        this.executeMavenMetadataCalculation = executeMavenMetadataCalculation;
        return this;
    }

    public MoveCopyContext setPruneEmptyFolders(boolean pruneEmptyFolders) {
        this.pruneEmptyFolders = pruneEmptyFolders;
        return this;
    }

    public MoveCopyContext setRemovProperties(List<String> removeProperties) {
        this.removeProps = removeProperties;
        return this;
    }

    public MoveCopyContext setAddProperties(Properties addProperties) {
        this.addProps = addProperties;
        return this;
    }

    public MoveCopyContext setUnixStyleBehavior(boolean unixStyleBehavior) {
        this.unixStyleBehavior = unixStyleBehavior;
        return this;
    }

    public MoveCopyContext setSuppressLayouts(boolean suppressLayouts) {
        this.suppressLayouts = suppressLayouts;
        return this;
    }

    public MoveCopyContext setTransactionSize(int transactionSize) {
        this.transactionSize = transactionSize;
        return this;
    }

    public List<MoveCopyItemInfo> getFoldersToDelete() {
        return foldersToDelete;
    }
}
