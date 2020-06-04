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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import org.artifactory.security.AceInfo;

/**
 * Shown in effective permission tab, user edit modal, group edit modal, tree on a single artifact
 *
 * @author Chen Keinan
 */
public class EffectivePermission {

    private String principal;
    private boolean delete;
    private boolean deploy;
    private boolean annotate;
    private boolean read;
    private Boolean managed;
    private boolean managedXrayMeta;
    private boolean managedXrayWatches;
    private Integer mask;

    public EffectivePermission() {
    }

    public EffectivePermission(AceInfo aceInfo) {
        principal = aceInfo.getPrincipal();
        delete = aceInfo.canDelete();
        deploy = aceInfo.canDeploy();
        annotate = aceInfo.canAnnotate();
        read = aceInfo.canRead();
        managed = aceInfo.canManage();
        managedXrayMeta = aceInfo.canManagedXrayMeta();
        managedXrayWatches = aceInfo.canManagedXrayWatches();
        mask = aceInfo.getMask();
    }

    public void aggregatePermissions(EffectivePermission otherPermission) {
        setRead(isRead() || otherPermission.isRead());
        setAnnotate(isAnnotate() || otherPermission.isAnnotate());
        setDeploy(isDeploy() || otherPermission.isDeploy());
        setDelete(isDelete() || otherPermission.isDelete());
        setManagedXrayMeta(isManagedXrayMeta() || otherPermission.isManagedXrayMeta());
        setManagedXrayWatches(isManagedXrayWatches() || otherPermission.isManagedXrayWatches());

    }

    public void aggregatePermissions(AceInfo aceInfo) {
        setRead(isRead() || aceInfo.canRead());
        setAnnotate(isAnnotate() || aceInfo.canAnnotate());
        setDeploy(isDeploy() || aceInfo.canDeploy());
        setDelete(isDelete() || aceInfo.canDelete());
        setManagedXrayMeta(isManagedXrayMeta() || aceInfo.canManagedXrayMeta());
        setManagedXrayWatches(isManagedXrayWatches() || aceInfo.canManagedXrayWatches());
    }

    public void setAdmin() {
        setRead(true);
        setAnnotate(true);
        setDeploy(true);
        setDelete(true);
        setManagedXrayMeta(true);
        setManagedXrayWatches(true);
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isDeploy() {
        return deploy;
    }

    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
    }

    public boolean isAnnotate() {
        return annotate;
    }

    public void setAnnotate(boolean annotate) {
        this.annotate = annotate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Boolean isManaged() {
        return managed;
    }

    public void setManaged(Boolean managed) {
        this.managed = managed;
    }

    public Integer getMask() {
        return mask;
    }

    public void setMask(Integer mask) {
        this.mask = mask;
    }

    public boolean isManagedXrayMeta() { return managedXrayMeta; }

    public void setManagedXrayMeta( boolean managedXrayMeta) { this.managedXrayMeta = managedXrayMeta; }

    public boolean isManagedXrayWatches() { return managedXrayWatches; }

    public void setManagedXrayWatches(boolean managedXrayWatches) { this.managedXrayWatches = managedXrayWatches; }
}
