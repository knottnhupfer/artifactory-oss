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

package org.artifactory.storage.db.aql.itest.service.decorator;

import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.model.AqlRepoProvider;

/**
 * @author Yinon Avraham
 */
public class AqlQueryDecoratorContext {

    private final AqlRepoProvider repoProvider;
    private final AqlPermissionProvider permissionProvider;

    public AqlQueryDecoratorContext(AqlRepoProvider repoProvider, AqlPermissionProvider permissionProvider) {
        this.repoProvider = repoProvider;
        this.permissionProvider = permissionProvider;
    }

    public AqlRepoProvider getRepoProvider() {
        return repoProvider;
    }

    public AqlPermissionProvider getPermissionProvider() {
        return permissionProvider;
    }
}
