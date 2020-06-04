package org.artifactory.ui.rest.model.common;

import java.util.Set;

/**
 * UI model for Effective Permission tab (extended for Repo and Build)
 *
 * @author Yuval Reches
 */
public class PermissionTargetInfo {
    private String permissionName;
    private Set<String> groups;
    private Set<String> users;

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }
}
