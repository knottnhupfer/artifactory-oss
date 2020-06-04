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

import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;

/**
 * @author ortalh
 */
public class AqlUIDockerV2TagPathSearchStrategy extends AqlUIFieldSearchStrategy {

    public AqlUIDockerV2TagPathSearchStrategy(AqlPhysicalFieldEnum field, AqlDomainEnum[] subdomainPath) {
        super(field, subdomainPath);
    }

    @Override
    public AqlBase.OrClause toQuery() {
        AqlBase.OrClause query = AqlApiItem.or();
        AqlBase.AndClause and = AqlApiItem.and();
        and.append(new AqlBase.CriteriaClause(AqlPhysicalFieldEnum.itemName, Lists.newArrayList(AqlDomainEnum.items),
                AqlComparatorEnum.equals, MANIFEST_FILENAME));
        values.forEach(value -> and.append(
                new AqlBase.CriteriaClause(field, Lists.newArrayList(AqlDomainEnum.items), comparator, value)));

        query.append(and);
        return query;
    }
}
