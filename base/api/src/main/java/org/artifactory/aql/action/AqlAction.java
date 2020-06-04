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

import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.result.AqlRestResult.Row;

import javax.annotation.Nullable;

/**
 * Denotes an action that can be performed as part of an AQL query.
 * Each action is backed by it's own keyword (for the parser) and api.
 * An action is performed on each row that's returned by the AQL query it was given with.
 *
 * @author Dan Feldman
 */
public interface AqlAction {

    /**
     * Performs the required action on the given {@param row}.
     * @return {@param row} if the action was performed successfully.
     * @throws AqlActionException if action failed to execute on row.
     */
    @Nullable
    Row doAction(Row row) throws AqlActionException;

    /**
     * @return This action's name
     */
    String getName();

    /**
     * @return true if {@param domain} is supported by this action
     */
    boolean supportsDomain(AqlDomainEnum domain);

    /**
     * Sets the dry run flag of this action.
     */
    void setDryRun(boolean dryRun);

    /**
     * @return true if this action should be performed in dry run mode.
     */
    boolean isDryRun();

}
