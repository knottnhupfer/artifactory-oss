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

import org.artifactory.aql.AqlException;
import org.artifactory.aql.AqlParserException;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Gidi Shabat
 */
public class AqlValue implements AqlVariable {

    private AqlVariableTypeEnum valueType;
    private String value;

    public AqlValue(AqlVariableTypeEnum valueType, String value) {
        this.valueType = valueType;
        this.value = value;
    }

    public Object toObject() throws AqlException {
        Object result = value;
        if (value == null) {
            return null;
        }
        if (AqlVariableTypeEnum.string == valueType) {
            result = value;
        }
        if (AqlVariableTypeEnum.date == valueType) {
            try {
                result = Long.parseLong(value);
            } catch (Exception e1) {
                try {
                    result = ISODateTimeFormat.dateOptionalTimeParser().parseMillis(value);
                } catch (Exception e2) {
                    throw new AqlParserException(
                            String.format("Invalid Date format: %s, AQL expect ISODateTimeFormat or long number", value), e2);
                }
            }
        }
        if (AqlVariableTypeEnum.longInt == valueType) {
            try {
                result = Long.valueOf(value);
            } catch (NumberFormatException e) {
                throw new AqlException("AQL Expect long value but found:" + value.toString() + "\n");
            }
        }
        if (AqlVariableTypeEnum.itemType == valueType) {
            AqlItemTypeEnum aqlItemTypeEnum = AqlItemTypeEnum.fromSignature(value);
            if(aqlItemTypeEnum !=null){
                result = aqlItemTypeEnum.type;
            }else{
                throw new AqlException(String.format("Invalid file type: %s, valid types are : %s, %s, %s", value,
                        AqlItemTypeEnum.file.signature, AqlItemTypeEnum.folder.signature,
                        AqlItemTypeEnum.any.signature));
            }
        }
        if (AqlVariableTypeEnum.integer == valueType) {
            try {
                result = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                throw new AqlException("AQL Expect integer value but found:" + value.toString() + "\n");
            }
        }
        return result;

    }
}
