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
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;

import static org.artifactory.aql.model.AqlPhysicalFieldEnum.*;

/**
 * @author Inbar Tal
 */
public class AqlUINpmChecksumSearchStrategy extends AqlUIFieldSearchStrategy {

    public AqlUINpmChecksumSearchStrategy(AqlPhysicalFieldEnum field, AqlDomainEnum[] subdomainPath) {
        super(field, subdomainPath);
    }

    @Override
    public AqlBase.OrClause toQuery() {
        AqlBase.OrClause query = AqlApiItem.or();
        values.forEach(value -> {
            if (value.length() == 32) {
                query.append(
                        new AqlBase.CriteriaClause(itemActualMd5, Lists.newArrayList(AqlDomainEnum.items), comparator, value));
            } else if (value.length() == 40) {
                query.append(
                        new AqlBase.CriteriaClause(itemActualSha1, Lists.newArrayList(AqlDomainEnum.items), comparator, value));
            } else {
                query.append(
                        new AqlBase.CriteriaClause(itemSha2, Lists.newArrayList(AqlDomainEnum.items), comparator, value));
            }
        });
        return query;
    }

    @Override
    public boolean includePropsInResult() {
        return true;
    }
}
