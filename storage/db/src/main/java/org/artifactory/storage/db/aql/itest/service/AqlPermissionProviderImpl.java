/*
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

package org.artifactory.storage.db.aql.itest.service;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.repo.RepoPath;

/**
 * @author Gidi Shabat
 */
public class AqlPermissionProviderImpl implements AqlPermissionProvider {

    private AuthorizationService authorizationService;
    private AddonsManager addonsManager;

    public AuthorizationService getAuthorizationProvider() {
        if (authorizationService == null) {
            authorizationService = ContextHelper.get().getAuthorizationService();
        }
        return authorizationService;
    }

    public AddonsManager getAddonsManager() {
        if (addonsManager == null) {
            addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        }
        return addonsManager;
    }

    @Override
    public boolean canRead(RepoPath repoPath) {
        return getAuthorizationProvider().canRead(repoPath);
    }

    @Override
    public boolean isAdmin() {
        return getAuthorizationProvider().isAdmin();
    }

    @Override
    public boolean isOss() {
        return getAddonsManager() instanceof OssAddonsManager;
    }
}