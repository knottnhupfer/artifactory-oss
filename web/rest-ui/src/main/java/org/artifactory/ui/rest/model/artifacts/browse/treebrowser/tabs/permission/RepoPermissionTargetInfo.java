package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import org.artifactory.ui.rest.model.common.PermissionTargetInfo;

import java.util.List;

/**
 * UI model for repo Effective Permission tab
 *
 * @author nadavy
 */
public class RepoPermissionTargetInfo extends PermissionTargetInfo {

    private List<String> repoKeys;

    public RepoPermissionTargetInfo() {
    }

    public List<String> getRepoKeys() {
        return repoKeys;
    }

    public void setRepoKeys(List<String> repoKeys) {
        this.repoKeys = repoKeys;
    }

}