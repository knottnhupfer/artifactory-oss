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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.jfrog.client.util.PathUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dan Feldman
 */
public class AqlUIFieldSearchStrategy implements AqlUISearchStrategy {

    protected AqlPhysicalFieldEnum field;
    protected List<String> values;
    protected List<AqlDomainEnum> subdomainPath;
    protected AqlComparatorEnum comparator;

    public AqlUIFieldSearchStrategy(AqlPhysicalFieldEnum field, AqlDomainEnum[] subdomainPath) {
        this.field = field;
        this.subdomainPath = Stream.of(subdomainPath).collect(Collectors.toList());
    }

    @Override
    public AqlUIFieldSearchStrategy values(List<String> values) {
        this.values = values;
        return this;
    }

    @Override
    public AqlUIFieldSearchStrategy values(String... values) {
        this.values = Stream.of(values).collect(Collectors.toList());
        return this;
    }

    @Override
    public AqlUIFieldSearchStrategy comparator(AqlComparatorEnum comparator) {
        this.comparator = comparator;
        return this;
    }

    @Override
    public AqlPhysicalFieldEnum getSearchField() {
        return field;
    }

    @Override
    public String getSearchKey() {
        return "";
    }

    @Override
    public AqlBase.OrClause toQuery() {
        AqlBase.OrClause query = AqlApiItem.or();
        values.stream().forEach(value -> query.append(
                new AqlBase.CriteriaClause(field, Lists.newArrayList(AqlDomainEnum.items), comparator, value)));
        return query;
    }

    @Override
    public boolean includePropsInResult() {
        return false;
    }

    @Override
    public String toString() {
        return "AqlUIFieldSearchStrategy{" +
                "field: " + field +
                ", values: " + PathUtils.collectionToDelimitedString(values) +
                ", comparator: " + comparator +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AqlUISearchStrategy)) {
            return false;
        }

        AqlUIFieldSearchStrategy aqlUISearchStrategy = (AqlUIFieldSearchStrategy) o;

        if (field != null ? !field.equals(aqlUISearchStrategy.field) : aqlUISearchStrategy.field != null) {
            return false;
        }

        if (values != null ? !values.equals(aqlUISearchStrategy.values) : aqlUISearchStrategy.values != null) {
            return false;
        }

        if (subdomainPath != null ? !subdomainPath.equals(aqlUISearchStrategy.subdomainPath) :
                aqlUISearchStrategy.subdomainPath != null) {
            return false;
        }

        return comparator != null ? comparator.equals(aqlUISearchStrategy.comparator) :
                aqlUISearchStrategy.comparator == null;
    }
}
