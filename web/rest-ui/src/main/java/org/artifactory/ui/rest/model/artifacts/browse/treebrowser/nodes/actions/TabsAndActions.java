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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.actions;

import com.google.common.collect.Lists;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.List;

/**
 * @author Shay Yaakov
 */
public class TabsAndActions implements RestModel {

    private String type;
    private RepoPath repoPath;
    private String repoType;
    private RepoType repoPkgType;
    private boolean cached;
    private List<TabOrAction> tabs = Lists.newArrayList();
    private List<TabOrAction> actions = Lists.newArrayList();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String rpp) {
        this.repoPath = RepoPathFactory.create(rpp);
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public RepoType getRepoPkgType() {
        return repoPkgType;
    }

    public void setRepoPkgType(RepoType repoPkgType) {
        this.repoPkgType = repoPkgType;
    }

    public boolean getCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public List<TabOrAction> getTabs() {
        return tabs;
    }

    public void setTabs(List<TabOrAction> tabs) {
        this.tabs = tabs;
    }

    public List<TabOrAction> getActions() {
        return actions;
    }

    public void setActions(List<TabOrAction> actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
