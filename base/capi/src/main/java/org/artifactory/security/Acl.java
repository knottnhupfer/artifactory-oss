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

package org.artifactory.security;

import org.artifactory.common.Info;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author Yuval Reches
 */
public interface Acl<T extends PermissionTarget> extends Info {

    T getPermissionTarget();

    Set<AceInfo> getAces();

    /**
     * Unsafe (mutable) set of aces; can usually be used when the ace info is only displayable (a.k.a doesn't determine
     * access control, for example in UI services). Can also be used when the aces need to be modified. More efficient
     * than {@link this#getAces()} in terms of memory usage, since it doesn't copy the set of aces into an immutable set.
     * @return the mutable set of aces
     */
    Set<MutableAceInfo> getMutableAces();

    String getUpdatedBy();

    long getLastUpdated();

    /**
     * @return The unique key of the acl record in Access db.
     *         null in case the acl is in pre-creation state in Artifactory (before sending the acl to Access)
     */
    @Nullable
    String getAccessIdentifier();
}
