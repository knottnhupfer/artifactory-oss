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

import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;

import java.util.List;

/**
 * @author Dan Feldman
 */
public interface AqlUISearchStrategy {

    /**
     * Sets the values to search for in the chosen field
     */
    AqlUISearchStrategy values(List<String> values);

    /**
     * Sets the values to search for in the chosen field
     */
    AqlUISearchStrategy values(String... values);

    /**
     * Sets the comparator to search with in the chosen field
     */
    AqlUISearchStrategy comparator(AqlComparatorEnum comparator);

    /**
     * Returns the field being searched
     */
    AqlPhysicalFieldEnum getSearchField();

    /**
     * Returns the property key being searched for all property based strategies.
     */
    String getSearchKey();

    /**
     * Constructs a ready-to-go or clause based on the chosen strategy and requested criteria.
     */
    AqlBase.OrClause toQuery();

    boolean includePropsInResult();
}
