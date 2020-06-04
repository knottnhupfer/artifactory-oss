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

package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.result.rows.AqlBuildProperty;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiBuildProperty extends AqlBase<AqlApiBuildProperty, AqlBuildProperty> {

    public AqlApiBuildProperty() {
        super(AqlBuildProperty.class, true);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildProperty> key() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildProperties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildPropertyKey, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildProperty> value() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildProperties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildPropertyValue, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains<AqlApiBuildProperty> build() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildProperties, AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains(subDomains);
    }

    public static AqlBase.PropertyCriteriaClause<AqlApiProperty> property(String key, AqlComparatorEnum comparator,
            String value) {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildProperties);
        return new AqlBase.PropertyCriteriaClause(key, comparator, value, subDomains);
    }
    public static AqlApiBuildProperty create() {
        return new AqlApiBuildProperty();
    }
}