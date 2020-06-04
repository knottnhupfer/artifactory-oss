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

package org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria;

import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy.AqlUIFieldSearchStrategy;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy.AqlUISearchStrategy;

/**
 * Contains all available field criteria that is globally supported for every package
 *
 * @author Dan Feldman
 */
public enum FieldSearchCriteria {

    repo(new AqlUISearchModel("repo", "Repository", "Repository", false, new AqlComparatorEnum[]{AqlComparatorEnum.equals}),
            new AqlUIFieldSearchStrategy(AqlPhysicalFieldEnum.itemRepo, new AqlDomainEnum[]{AqlDomainEnum.items}));

    AqlUISearchModel model;
    AqlUISearchStrategy strategy;

    FieldSearchCriteria(AqlUISearchModel model, AqlUISearchStrategy strategy) {
        this.model = model;
        this.strategy = strategy;
    }


    public AqlUISearchModel getModel() {
        return model;
    }

    public AqlUISearchStrategy getStrategy() {
        return strategy;
    }

    public static AqlUISearchStrategy getStrategyByFieldId(String id) {
        return valueOf(id).strategy;
    }
}