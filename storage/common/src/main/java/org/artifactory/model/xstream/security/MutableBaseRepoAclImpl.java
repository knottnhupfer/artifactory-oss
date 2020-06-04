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

import com.google.common.collect.ImmutableSet;
import org.artifactory.security.AceInfo;
import org.artifactory.security.MutableAceInfo;
import org.artifactory.security.MutableBaseRepoAcl;
import org.artifactory.security.RepoPermissionTarget;
import org.artifactory.util.CollectionUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yoav Landman
 */
public abstract class MutableBaseRepoAclImpl<T extends RepoPermissionTarget> implements MutableBaseRepoAcl<T> {

    private Set<AceImpl> aces = new HashSet<>();
    private String updatedBy;
    private long lastUpdated;
    private String accessIdentifier;

    MutableBaseRepoAclImpl() {
    }

    MutableBaseRepoAclImpl(long lastUpdated, String updatedBy, String accessIdentifier, Set<AceInfo> aces) {
        this(aces, updatedBy, lastUpdated);
        this.accessIdentifier = accessIdentifier;
    }

    MutableBaseRepoAclImpl(Set<AceInfo> aces, String updatedBy, long lastUpdated) {
        if (CollectionUtils.notNullOrEmpty(aces)) {
            for (AceInfo ace : aces) {
                this.aces.add(new AceImpl(ace));
            }
        }
        this.updatedBy = updatedBy;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public abstract T getPermissionTarget();

    @Override
    @SuppressWarnings({"unchecked"})
    public Set<AceInfo> getAces() {
        return ImmutableSet.<AceInfo>copyOf((Set) aces);
    }

    @Override
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public Set<MutableAceInfo> getMutableAces() {
        return (Set<MutableAceInfo>) (Set) aces;
    }

    @Override
    public void setAces(Set<AceInfo> aces) {
        this.aces.clear();
        if (aces != null) {
            for (AceInfo ace : aces) {
                this.aces.add(new AceImpl(ace));
            }
        }
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }

    @Nullable
    @Override
    public String getAccessIdentifier() {
        return accessIdentifier;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MutableBaseRepoAclImpl)) {
            return false;
        }

        MutableBaseRepoAclImpl info = (MutableBaseRepoAclImpl) o;

        return !(getPermissionTarget() != null ? !getPermissionTarget().equals(info.getPermissionTarget()) :
                info.getPermissionTarget() != null);
    }

    @Override
    public int hashCode() {
        return (getPermissionTarget() != null ? getPermissionTarget().hashCode() : 0);
    }

}