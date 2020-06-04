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

import org.artifactory.aql.model.AqlTableFieldsEnum;

/**
 * The TableLinkRelation represent link between two tables
 *
 * @author Gidi Shabat
 */
public class TableLinkRelation {
    private TableLink fromTable;
    private AqlTableFieldsEnum fromField;
    private TableLink toTable;
    private AqlTableFieldsEnum toFiled;

    public TableLinkRelation(TableLink fromTable, AqlTableFieldsEnum fromField, TableLink toTable,
            AqlTableFieldsEnum toFiled) {
        this.fromTable = fromTable;
        this.fromField = fromField;
        this.toTable = toTable;
        this.toFiled = toFiled;
    }

    public TableLink getFromTable() {
        return fromTable;
    }

    public AqlTableFieldsEnum getFromField() {
        return fromField;
    }

    public TableLink getToTable() {
        return toTable;
    }

    @Override
    public String toString() {
        return "TableLinkRelation{" +
                "fromTable=" + fromTable +
                ", toTable=" + toTable +
                '}';
    }

    public AqlTableFieldsEnum getToFiled() {
        return toFiled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableLinkRelation that = (TableLinkRelation) o;

        if (fromField != that.fromField) {
            return false;
        }
        if (fromTable != null ? !fromTable.equals(that.fromTable) : that.fromTable != null) {
            return false;
        }
        if (toFiled != that.toFiled) {
            return false;
        }
        return toTable != null ? toTable.equals(that.toTable) : that.toTable == null;
    }

    @Override
    public int hashCode() {
        int result = fromTable != null ? fromTable.hashCode() : 0;
        result = 31 * result + (fromField != null ? fromField.hashCode() : 0);
        result = 31 * result + (toTable != null ? toTable.hashCode() : 0);
        result = 31 * result + (toFiled != null ? toFiled.hashCode() : 0);
        return result;
    }
}
