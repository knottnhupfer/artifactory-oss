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

package org.artifactory.storage.db.aql.sql.builder.query.sql.type;

import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DomainSensitiveTable {
    private SqlTable table;
    private List<SqlTableEnum> tables;

    public DomainSensitiveTable(SqlTable table, List<SqlTableEnum> tables) {
        this.tables = tables;
        this.table = table;
    }

    public List<SqlTableEnum> getTables() {
        return tables;
    }

    public SqlTable getTable() {
        return table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainSensitiveTable that = (DomainSensitiveTable) o;

        if (table != null ? !table.equals(that.table) : that.table != null) {
            return false;
        }
        return tables != null ? tables.equals(that.tables) : that.tables == null;
    }

    @Override
    public int hashCode() {
        int result = table != null ? table.hashCode() : 0;
        result = 31 * result + (tables != null ? tables.hashCode() : 0);
        return result;
    }
}
