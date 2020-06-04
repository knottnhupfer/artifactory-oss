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

package org.artifactory.ui.rest.model.artifacts.search.packagesearch.result;

import com.google.common.collect.HashMultimap;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.result.rows.FullRow;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for all 'Package Search' results (that can actually be any result) - basic fields are members based on
 * the domain, any extra included fields (i.e. AqlApiItem.create().filter(...).include()) are inserted in the map.
 *
 * @author Dan Feldman
 */
public interface AqlUISearchResult {

    @JsonIgnore
    AqlDomainEnum getDomain();

    //Returns a Jackson serializable map representing the extra fields multimap.
    Map<String, Collection<String>> getExtraFields();

    //Used by AqlUISearchResultManipulator to change or add values to the extra fields multimap.
    HashMultimap<String, String> getExtraFieldsMap();

    AqlUISearchResult aggregateRow(FullRow row);

}
