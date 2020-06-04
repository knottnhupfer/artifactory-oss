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

import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;

/**
 * @author Dan Feldman
 */
public class AqlUIDockerV2ImageDigestSearchStrategy extends AqlUIPropertySearchStrategy {

    public AqlUIDockerV2ImageDigestSearchStrategy(String key, AqlDomainEnum[] subdomainPath) {
        super(key, subdomainPath);
    }

    @Override
    public AqlBase.OrClause toQuery() {
        AqlBase.OrClause query = AqlApiItem.or();
        values.stream().forEach(value -> {
            AqlBase.AndClause and = AqlApiItem.and();
            //The image digest
            and.append(new AqlBase.PropertyCriteriaClause(key, comparator, value, subdomainPath));
            //Don't return other sha256 tagged artifacts - only point to the tag's manifest.json which must have a
            // value in the docker.repoName and docker.manifest props (to make sure it's v2)
            and.append(
                    new AqlBase.PropertyCriteriaClause("docker.repoName", AqlComparatorEnum.matches, "*", subdomainPath));
            and.append(
                    new AqlBase.PropertyCriteriaClause("docker.manifest", AqlComparatorEnum.matches, "*", subdomainPath));
            query.append(and);
        });
        return query;
    }

    @Override
    public boolean includePropsInResult() {
        return true;
    }
}