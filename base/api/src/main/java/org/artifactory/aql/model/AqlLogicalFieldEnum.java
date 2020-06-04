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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.artifactory.aql.model.AqlDomainEnum.items;

/**
 * @author Yinon Avraham
 */
public enum AqlLogicalFieldEnum implements AqlFieldEnum {

    itemVirtualRepos("virtual_repos", items);

    private final String signature;
    private final AqlDomainEnum domain;

    AqlLogicalFieldEnum(String signature, AqlDomainEnum domain) {
        this.signature = signature;
        this.domain = domain;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return domain;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public <T> T doSwitch(AqlFieldEnumSwitch<T> fieldEnumSwitch) {
        return fieldEnumSwitch.caseOf(this);
    }

    private static final AqlLogicalFieldEnum[] EMPTY_ARRAY = new AqlLogicalFieldEnum[0];
    private static final Map<AqlDomainEnum, AqlLogicalFieldEnum[]> FIELDS_BY_DOMAIN;

    static {
        //Collect and map fields by domain
        Map<AqlDomainEnum, List<AqlLogicalFieldEnum>> map = Maps.newHashMap();
        for (AqlLogicalFieldEnum field : values()) {
            List<AqlLogicalFieldEnum> fields = map.get(field.domain);
            if (fields == null) {
                fields = Lists.newArrayList();
                map.put(field.domain, fields);
            }
            fields.add(field);
        }
        //Convert map value to array (immutable, no need to convert to array afterwards, etc.)
        Map<AqlDomainEnum, AqlLogicalFieldEnum[]> mapOfArrays = Maps.newHashMap();
        for (Map.Entry<AqlDomainEnum, List<AqlLogicalFieldEnum>> entry : map.entrySet()) {
            List<AqlLogicalFieldEnum> fields = entry.getValue();
            mapOfArrays.put(entry.getKey(), fields.toArray(new AqlLogicalFieldEnum[fields.size()]));
        }
        FIELDS_BY_DOMAIN = Collections.unmodifiableMap(mapOfArrays);
    }

    static AqlLogicalFieldEnum[] getFieldsByDomain(AqlDomainEnum domain) {
        AqlLogicalFieldEnum[] fields = FIELDS_BY_DOMAIN.get(domain);
        if (fields == null) {
            return EMPTY_ARRAY;
        }
        return fields;
    }
}
