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

package org.artifactory.aql.action;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.model.AqlActionEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.result.AqlRestResult.Row;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.common.StatusHolder;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.artifactory.aql.action.AqlActionException.Reason.ACTION_FAILED;
import static org.artifactory.aql.action.AqlActionException.Reason.UNSUPPORTED_FOR_DOMAIN;

/**
 * Deletes the item denoted by a given row by resolving it's path and undeploying it using the {@link RepositoryService}
 *
 * @author Dan Feldman
 */
public class AqlDeleteItemAction implements AqlAction {
    private static final Logger log = LoggerFactory.getLogger(AqlDeleteItemAction.class);

    private boolean dryRun = true;

    @Override
    public Row doAction(Row row) throws AqlActionException {
        //Should also be verified in query construction phase but just in case.
        if (!AqlDomainEnum.items.equals(row.getDomain())) {
            String msg = "Skipping delete action for row, only items domain is supported - row has domain: " + row.getDomain();
            log.debug(msg);
            throw new AqlActionException(msg, UNSUPPORTED_FOR_DOMAIN);
        }
        doIfNeeded(row);
        return row;
    }

    private void doIfNeeded(Row row) throws AqlActionException {
        RepoPath itemPath = AqlUtils.fromAql(row);
        if (dryRun) {
            if (!getAuthService().canDelete(itemPath)) {
                throw new AqlActionException("User does not have permission to delete item at '"
                        + itemPath.toPath() + "'.", ACTION_FAILED);
            }
        } else {
            // Permissions are verified by repoService.
            StatusHolder status = getRepoService().undeployMultiTransaction(itemPath);
            if (status.isError()) {
                throw new AqlActionException(status.getLastError().getMessage(), ACTION_FAILED);
            }
        }
    }

    @Override
    public String getName() {
        return AqlActionEnum.deleteItem.name;
    }

    @Override
    public boolean supportsDomain(AqlDomainEnum domain) {
        return AqlDomainEnum.items.equals(domain);
    }

    @Override
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public boolean isDryRun() {
        return dryRun;
    }

    private RepositoryService getRepoService() {
        return ContextHelper.get().beanForType(RepositoryService.class);
    }

    private AuthorizationService getAuthService() {
        return ContextHelper.get().beanForType(AuthorizationService.class);
    }
}
