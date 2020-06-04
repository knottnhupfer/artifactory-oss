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

package org.artifactory.storage.db.aql.sql.builder.links;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlTableFieldsEnum;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.List;

/**
 * The TableLink wraps the SqlTable object  and provides/contains the relations between the tables.
 * It is being used by the TableLinkBrowser to find the shortest route between to tables
 * @author Gidi Shabat
 */
public class TableLink {
    private final List<TableLinkRelation> relations = Lists.newArrayList();
    private final SqlTable table;

    public TableLink(SqlTableEnum tableEnum) {
        this.table = new SqlTable(tableEnum);
    }

    /**
     * Adds  link between to tables in both directions
     * @param fromField
     * @param toTable
     * @param toFiled
     */
    public void addLink(AqlTableFieldsEnum fromField, TableLink toTable, AqlTableFieldsEnum toFiled) {
        TableLinkRelation tableLinkRelation = new TableLinkRelation(this, fromField, toTable, toFiled);
        relations.add(tableLinkRelation);
        tableLinkRelation = new TableLinkRelation(toTable, toFiled, this, fromField);
        toTable.relations.add(tableLinkRelation);
    }

    public SqlTable getTable() {
        return table;
    }

    public SqlTableEnum getTableEnum() {
        return table.getTable();
    }

    public List<TableLinkRelation> getRelations() {
        return relations;
    }

    @Override
    public String toString() {
        return "TableLink{" +
                "table=" + table +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableLink tableLink = (TableLink) o;

        return table.equals(tableLink.table);
    }

    @Override
    public int hashCode() {
        return table.hashCode();
    }
}
