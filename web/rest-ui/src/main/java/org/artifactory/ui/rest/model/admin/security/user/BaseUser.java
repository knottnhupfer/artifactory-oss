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

package org.artifactory.ui.rest.model.admin.security.user;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.security.UserConfigurationImpl;
import org.artifactory.security.UserInfo;

import java.util.Set;

/**
 * @author Chen Keinan
 */
public class BaseUser extends UserConfigurationImpl implements RestModel {

    private boolean proWithoutLicense;
    private Boolean canDeploy;
    private Boolean canManage;
    private Boolean canCreateReleaseBundle;
    private Boolean isBuildBasicView;
    private String externalRealmLink;
    private Boolean anonAccessEnabled;
    private boolean existsInDB;
    private boolean hideUploads;
    private boolean requireProfileUnlock;
    private boolean requireProfilePassword;
    private Boolean locked;
    private Boolean credentialsExpired;
    private Integer currentPasswordValidFor;
    private Integer numberOfGroups;

    public BaseUser(){}

    public BaseUser(UserInfo user) {
        this.setLastLoggedInMillis(user.getLastLoginTimeMillis());
        this.setRealm(user.getRealm());
        this.setAdmin(user.isAdmin());
        this.setGroupAdmin(user.isGroupAdmin());
        this.setEmail(user.getEmail());
        this.setName(user.getUsername());
        this.setProfileUpdatable(user.isUpdatableProfile());
        this.setLocked(user.isLocked());
        this.setCredentialsExpired(user.isCredentialsExpired());
        this.setInternalPasswordDisabled(user.isPasswordDisabled());
    }

    public void setProWithoutLicense(boolean proWithoutLicense) {
        this.proWithoutLicense = proWithoutLicense;
    }

    public boolean isProWithoutLicense() {
        return proWithoutLicense;
    }

    public BaseUser (String userName, boolean admin){
        super.setAdmin(admin);
        super.setName(userName);
    }

    public Boolean isCanDeploy() {
        return canDeploy;
    }

    public void setCanDeploy(Boolean canDeploy) {
        this.canDeploy = canDeploy;
    }

    public Boolean isCanCreateReleaseBundle() {
        return canCreateReleaseBundle;
    }

    public void setCanCreateReleaseBundle(Boolean canCreateReleaseBundle) {
        this.canCreateReleaseBundle = canCreateReleaseBundle;
    }

    public Boolean isCanManage() {
        return canManage;
    }

    public void setCanManage(Boolean canManage) {
        this.canManage = canManage;
    }

    public Boolean getBuildBasicView() {
        return isBuildBasicView;
    }

    public void setBuildBasicView(Boolean buildBasicView) {
        isBuildBasicView = buildBasicView;
    }

    public String getExternalRealmLink() {
        return externalRealmLink;
    }

    public void setExternalRealmLink(String externalRealmLink) {
        this.externalRealmLink = externalRealmLink;
    }

    public Boolean getAnonAccessEnabled() {
        return anonAccessEnabled;
    }

    public void setAnonAccessEnabled(Boolean anonAccessEnabled) {
        this.anonAccessEnabled = anonAccessEnabled;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public boolean isExistsInDB() { return existsInDB; }

    public void setExistsInDB(boolean existsInDB) { this.existsInDB = existsInDB; }

    public void setHideUploads(boolean hideUploads) { this.hideUploads = hideUploads; }

    public boolean getHideUploads() { return this.hideUploads; }

    public boolean isRequireProfileUnlock() {
        return requireProfileUnlock;
    }

    public boolean isRequireProfilePassword() {
        return requireProfilePassword;
    }

    public void setRequireProfilePassword(boolean requireProfilePassword) {
        this.requireProfilePassword = requireProfilePassword;
    }

    public void setRequireProfileUnlock(boolean requireProfileUnlock) {
        this.requireProfileUnlock = requireProfileUnlock;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    public Boolean getCredentialsExpired() {
        return credentialsExpired;
    }

    public void setCredentialsExpired(Boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    /**
     * @return number of days till password should be changed
     */
    public Integer getCurrentPasswordValidFor() {
        return currentPasswordValidFor;
    }

    /**
     * @param currentPasswordValidFor number of days till password should be changed
     */
    public void setCurrentPasswordValidFor(Integer currentPasswordValidFor) {
        this.currentPasswordValidFor = currentPasswordValidFor;
    }

    public Integer getNumberOfGroups() {
        if (numberOfGroups == null) {
            Set<String> groups = getGroups();
            return groups == null ? 0 : groups.size();
        }
        return numberOfGroups;
    }

    public void setNumberOfGroups(Integer numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }

}
