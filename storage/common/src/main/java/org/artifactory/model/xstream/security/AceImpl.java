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

package org.artifactory.model.xstream.security;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.artifactory.security.AceInfo;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.security.MutableAceInfo;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Fred Simon
 */
@XStreamAlias("ace")
public class AceImpl implements MutableAceInfo {
    private String principal;
    private boolean group;
    private int mask;

    public AceImpl() {
    }

    public AceImpl(String principal, boolean group, int mask) {
        this.principal = principal;
        this.group = group;
        this.mask = mask;
    }

    public AceImpl(AceInfo copy) {
        this(copy.getPrincipal(), copy.isGroup(), copy.getMask());
    }

    @Override
    public String getPrincipal() {
        return principal;
    }

    @Override
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    @Override
    public boolean isGroup() {
        return group;
    }

    @Override
    public void setGroup(boolean group) {
        this.group = group;
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public void setMask(int mask) {
        this.mask = mask;
    }

    @Override
    public boolean canManage() {
        return (getMask() & ArtifactoryPermission.MANAGE.getMask()) > 0;
    }

    @Override
    public void setManage(boolean manage) {
        if (manage) {
            setMask(getMask() | ArtifactoryPermission.MANAGE.getMask());
        } else {
            setMask(getMask() & ~ArtifactoryPermission.MANAGE.getMask());
        }
    }

    @Override
    public boolean canDelete() {
        return (getMask() & ArtifactoryPermission.DELETE.getMask()) > 0;
    }

    @Override
    public void setDelete(boolean delete) {
        if (delete) {
            setMask(getMask() | ArtifactoryPermission.DELETE.getMask());
        } else {
            setMask(getMask() & ~ArtifactoryPermission.DELETE.getMask());
        }
    }

    @Override
    public boolean canDeploy() {
        return (getMask() & ArtifactoryPermission.DEPLOY.getMask()) > 0;
    }

    @Override
    public void setDeploy(boolean deploy) {
        if (deploy) {
            setMask(getMask() | ArtifactoryPermission.DEPLOY.getMask());
        } else {
            setMask(getMask() & ~ArtifactoryPermission.DEPLOY.getMask());
        }
    }

    @Override
    public boolean canAnnotate() {
        return (getMask() & ArtifactoryPermission.ANNOTATE.getMask()) > 0;
    }

    @Override
    public void setAnnotate(boolean annotate) {
        if (annotate) {
            setMask(getMask() | ArtifactoryPermission.ANNOTATE.getMask());
        } else {
            setMask(getMask() & ~ArtifactoryPermission.ANNOTATE.getMask());
        }
    }

    @Override
    public boolean canRead() {
        return (getMask() & ArtifactoryPermission.READ.getMask()) > 0;
    }

    @Override
    public void setRead(boolean read) {
        if (read) {
            setMask(getMask() | ArtifactoryPermission.READ.getMask());
        } else {
            setMask(getMask() & ~ArtifactoryPermission.READ.getMask());
        }
    }

    @Override
    public void setManagedXrayMeta(boolean xrayMeta) {
        if (xrayMeta) {
            setMask(getMask() | ArtifactoryPermission.MANAGED_XRAY_META.getMask());
        }
        else {
            setMask(getMask() & ~ArtifactoryPermission.MANAGED_XRAY_META.getMask());
        }
    }

    @Override
    public void setManagedXrayWatches(boolean xrayWatches) {
        if (xrayWatches) {
            setMask(getMask() | ArtifactoryPermission.MANAGED_XRAY_WATCHES.getMask());
        }
        else {
            setMask(getMask() & ~ArtifactoryPermission.MANAGED_XRAY_WATCHES.getMask());
        }
    }

    @Override
    public boolean canManagedXrayMeta() {
        return (getMask() & ArtifactoryPermission.MANAGED_XRAY_META.getMask()) > 0;
    }

    @Override
    public boolean canManagedXrayWatches() {
        return (getMask() & ArtifactoryPermission.MANAGED_XRAY_WATCHES.getMask()) > 0;
    }

    @Override
    public Set<String> getPermissionsAsString() {
        return Arrays.stream(ArtifactoryPermission.values())
                .filter(permission -> (getMask() & permission.getMask()) > 0)
                .map(ArtifactoryPermission::getString)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getPermissionsDisplayNames() {
        return Arrays.stream(ArtifactoryPermission.values())
                .filter(permission -> (getMask() & permission.getMask()) > 0)
                .map(ArtifactoryPermission::getDisplayName)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getPermissionsUiNames() {
        return Arrays.stream(ArtifactoryPermission.values())
                .filter(permission -> (getMask() & permission.getMask()) > 0)
                .map(ArtifactoryPermission::getUiName)
                .collect(Collectors.toSet());
    }

    @Override
    public void setPermissionsFromStrings(Set<String> permissionStrings) {
        for (ArtifactoryPermission artifactoryPermission : ArtifactoryPermission.values()) {
            if (permissionStrings.contains(artifactoryPermission.getString())) {
                setMask(getMask() | artifactoryPermission.getMask());
            } else {
                setMask(getMask() & ~artifactoryPermission.getMask());
            }
        }
    }

    @Override
    public void setPermissionsFromDisplayNames(Set<String> permissionDisplayNames) throws IllegalArgumentException {
        validatePermissions(permissionDisplayNames);
        for (ArtifactoryPermission artifactoryPermission : ArtifactoryPermission.values()) {
            if (permissionDisplayNames.contains(artifactoryPermission.getDisplayName())) {
                setMask(getMask() | artifactoryPermission.getMask());
            } else {
                setMask(getMask() & ~artifactoryPermission.getMask());
            }
        }
    }

    /**
     * Iterating the user request and validating all actions are of valid type.
     * @throws IllegalArgumentException in case one of the actions provided is invalid
     */
    private void validatePermissions(Set<String> permissionDisplayNames) throws IllegalArgumentException {
        permissionDisplayNames.forEach(permission -> {
            try {
                ArtifactoryPermission.fromDisplayName(permission);
            } catch (IllegalArgumentException e) {
                // Fallback to internal name
                ArtifactoryPermission.fromString(permission);
            }
        });
    }

    @Override
    public void setPermissionsFromUiNames(Set<String> permissionUiNames) {
        for (ArtifactoryPermission artifactoryPermission : ArtifactoryPermission.values()) {
            if (permissionUiNames.contains(artifactoryPermission.getUiName())) {
                setMask(getMask() | artifactoryPermission.getMask());
            } else {
                setMask(getMask() & ~artifactoryPermission.getMask());
            }
        }
    }

    @Override
    public String toString() {
        return "AceImpl{" +
                "principal='" + principal + '\'' +
                ", group=" + group +
                ", mask=" + mask +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AceImpl aceInfo = (AceImpl) o;
        return group == aceInfo.group &&
                !(principal != null ? !principal.equals(aceInfo.principal) :
                        aceInfo.principal != null);
    }

    @Override
    public int hashCode() {
        int result;
        result = (principal != null ? principal.hashCode() : 0);
        result = 31 * result + (group ? 1 : 0);
        return result;
    }
}