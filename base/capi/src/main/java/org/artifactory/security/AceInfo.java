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

import java.util.Set;

/**
 * Date: 8/2/11
 * Time: 10:23 AM
 *
 * @author Fred Simon
 */
public interface AceInfo extends Info {
    String getPrincipal();

    boolean isGroup();

    int getMask();

    boolean canManage();

    boolean canDelete();

    boolean canDeploy();

    boolean canAnnotate();

    boolean canRead();

    boolean canManagedXrayMeta();

    boolean canManagedXrayWatches();

    Set<String> getPermissionsAsString();

    Set<String> getPermissionsDisplayNames();

    Set<String> getPermissionsUiNames();
}
