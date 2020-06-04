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

import org.artifactory.aql.model.AqlActionEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.result.AqlRestResult.Row;

/**
 * The find action actually does nothing, every aql query is first and foremost a find query - so it simply returns the
 * current row to the caller.
 *
 * @author Dan Feldman
 */
public class AqlFindAction implements AqlAction {

    @Override
    public Row doAction(Row row) throws AqlActionException {
        // Nothing to do for the find action.
        return row;
    }

    @Override
    public String getName() {
        return AqlActionEnum.find.name;
    }


    @Override
    public boolean supportsDomain(AqlDomainEnum domain) {
        // find is supported on all domains
        return true;
    }

    @Override
    public void setDryRun(boolean dryRun) {
        //nop
    }

    @Override
    public boolean isDryRun() {
        return false;
    }
}
