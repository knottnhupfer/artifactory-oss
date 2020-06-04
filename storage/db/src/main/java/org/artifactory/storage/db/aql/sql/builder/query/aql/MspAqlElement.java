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

package org.artifactory.storage.db.aql.sql.builder.query.aql;

/**
 * Represent propery criteria which is actually two criterias:
 * "_property_key" "$equals" "<some_key>" "and" "_property_value" "$equals" "<some_value>"
 *
 * @author Gidi Shabat
 */
public class MspAqlElement implements AqlQueryElement {
    private int tableId;

    public MspAqlElement(int tableId) {
        this.tableId = tableId;
    }

    public int getTableId() {
        return tableId;
    }

    @Override
    public boolean isOperator() {
        return false;
    }
}
