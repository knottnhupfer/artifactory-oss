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

package org.artifactory.storage.db.search.model;

import org.artifactory.sapi.search.VfsBoolType;

/**
 * Date: 8/6/11
 * Time: 12:51 PM
 *
 * @author Fred Simon
 */
public abstract class BaseVfsQueryCriterion {
    VfsBoolType nextBool;

    public BaseVfsQueryCriterion() {
        this.nextBool = VfsBoolType.AND;
    }

    protected abstract VfsBoolType fill(DbSqlQueryBuilder query);

    public abstract boolean isValid();

    public abstract boolean hasPropertyFilter();

    public abstract boolean hasStatisticFilter();
}
