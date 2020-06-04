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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties("repoPath")
public abstract class BaseNode implements INode {

    protected transient RepoPath repoPath;
    private String repoKey;
    private String path;
    private String text;
    private String repoType;
    private boolean hasChild = false;
    private boolean local = true;
    protected RepoType repoPkgType;
    private String icon;

    public BaseNode(RepoPath repoPath) {
        this.repoPath = repoPath;
        this.repoKey = repoPath.getRepoKey();
        this.path = repoPath.getPath();
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public void updateRepoPath(RepoPath repoPath) {
        this.repoPath = repoPath;
        if (repoPath != null) {
            this.repoKey = repoPath.getRepoKey();
            this.path = repoPath.getPath();
        }
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isTrash() {
        return "trash".equals(repoType);
    }

    public boolean isDistribution() {
        return "distribution".equals(repoType);
    }

    public RepoType getRepoPkgType() {
        return repoPkgType;
    }

    public void setRepoPkgType(RepoType repoPkgType) {
        this.repoPkgType = repoPkgType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseNode)) {
            return false;
        }
        BaseNode base = (BaseNode) o;
        return repoPath.equals(base.repoPath);
    }

    @Override
    public int hashCode() {
        return repoPath.hashCode();
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
