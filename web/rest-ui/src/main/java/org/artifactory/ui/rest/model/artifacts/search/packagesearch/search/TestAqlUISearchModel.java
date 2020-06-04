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

package org.artifactory.ui.rest.model.artifacts.search.packagesearch.search;

import org.artifactory.aql.model.AqlComparatorEnum;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

/**
 * This class is used only for test purposes
 * @author Inbar Tal
 */
public class TestAqlUISearchModel extends AqlUISearchModel {

    public TestAqlUISearchModel(String id, AqlComparatorEnum allowedComparators, List<String> values) {
        super(id, allowedComparators, values);
    }

    public TestAqlUISearchModel(String id, String displayName, String fullName, boolean visibleByDefault,
                                AqlComparatorEnum[] allowedComparators, List<String> values) {
        super(id, displayName, fullName, visibleByDefault, allowedComparators);
        this.values = values;
    }

    @Override
    @JsonIgnore(false)
    public List<String> getValues() {
        return values;
    }
}
