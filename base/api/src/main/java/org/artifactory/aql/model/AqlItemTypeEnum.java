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
public enum AqlItemTypeEnum {
    folder("folder", 0), file("file", 1), any("any", -1);
    public String signature;
    public int type;

    AqlItemTypeEnum(String signature, int type) {
        this.signature = signature;
        this.type = type;
    }

    public static AqlItemTypeEnum fromTypes(int type) {
        for (AqlItemTypeEnum aqlItemTypeEnum : values()) {
            if (aqlItemTypeEnum.type == type) {
                return aqlItemTypeEnum;
            }
        }
        return null;
    }

    public static AqlItemTypeEnum fromSignature(String signature) {
        for (AqlItemTypeEnum aqlItemTypeEnum : values()) {
            if (aqlItemTypeEnum.signature.equals(signature)) {
                return aqlItemTypeEnum;
            }
        }
        return null;
    }
}
