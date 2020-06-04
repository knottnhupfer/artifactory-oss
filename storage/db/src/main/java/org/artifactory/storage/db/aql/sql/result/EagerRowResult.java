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

package org.artifactory.storage.db.aql.sql.result;

import com.google.common.collect.Maps;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.rows.RowResult;

import java.util.Collections;
import java.util.Map;

/**
 * @author Yinon Avraham
 */
public class EagerRowResult implements RowResult {

    private final Map<DomainSensitiveField, Object> fieldValues = Maps.newHashMap();

    @Override
    public void put(DomainSensitiveField field, Object value) {
        fieldValues.put(field, value);
    }

    @Override
    public Object get(DomainSensitiveField field) {
        return fieldValues.get(field);
    }

    public Map<DomainSensitiveField, Object> toMap() {
        return Collections.unmodifiableMap(fieldValues);
    }
}
