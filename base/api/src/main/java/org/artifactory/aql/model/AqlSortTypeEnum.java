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

package org.artifactory.aql.model;

/**
 * @author Gidi Shabat
 */
public enum AqlSortTypeEnum {
    desc("$desc", "DESC"), asc("$asc", "ASC");
    private String aqlName;
    private String sqlName;

    AqlSortTypeEnum(String aqlName, String sqlName) {
        this.aqlName = aqlName;
        this.sqlName = sqlName;
    }

    public String getAqlName() {
        return aqlName;
    }

    public String getSqlName() {
        return sqlName;
    }

    public static AqlSortTypeEnum fromAql(String aql) {
        for (AqlSortTypeEnum sortTypeEnum : values()) {
            if (sortTypeEnum.aqlName.equals(aql)) {
                return sortTypeEnum;
            }
        }
        throw new IllegalStateException("Couldn't find enum with the corresponding aql name");
    }
}
