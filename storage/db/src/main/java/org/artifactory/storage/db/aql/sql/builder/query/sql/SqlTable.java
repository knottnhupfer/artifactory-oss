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

package org.artifactory.storage.db.aql.sql.builder.query.sql;

import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

/**
 * The class represent table in the query.
 * It actually contains the table name and alias name in the query
 *
 * @author Gidi Shabat
 */
public class SqlTable {
    public static final int MINIMAL_DYNAMIC_TABLE_ID = 100;
    private final SqlTableEnum table;
    private final int id;

    /**
     * Static table instances
     */
    public SqlTable(SqlTableEnum table) {
        this.table = table;
        this.id = -1;
    }

    public SqlTable(SqlTableEnum table, int id) {
        this.table = table;
        this.id = id;
    }
    public String getTableName() {
        return table.name();

    }

    public SqlTableEnum getTable() {
        return table;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SqlTable table1 = (SqlTable) o;

        if (id != table1.id) {
            return false;
        }
        return table == table1.table;
    }

    @Override
    public String toString() {
        return "Table{" +
                "table=" + table +
                '}';
    }

    @Override
    public int hashCode() {
        int result = table != null ? table.hashCode() : 0;
        result = 31 * result + id;
        return result;
    }

    public String getAlias() {
        return getAliasDeclaration() + ".";
    }

    public String getAliasDeclaration() {
        if (id < 0) {
            return table.alias;
        } else {
            return table.alias + id;
        }
    }
}