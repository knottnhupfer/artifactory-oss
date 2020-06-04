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
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dan Feldman
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AqlUISearchModel implements RestModel {

    //both
    protected String id;

    //out
    protected String displayName;
    protected String fullName;
    protected boolean visibleByDefault;
    protected AqlComparatorEnum[] allowedComparators;

    //in
    protected AqlComparatorEnum comparator;
    protected List<String> values;

    public AqlUISearchModel() {}

    //Used for Serialization
    public AqlUISearchModel(String id, String displayName, String fullName, boolean visibleByDefault, AqlComparatorEnum[] allowedComparators) {
        this.id = id;
        this.displayName = displayName;
        this.fullName = fullName;
        this.visibleByDefault = visibleByDefault;
        this.allowedComparators = allowedComparators;
    }

    @JsonCreator //Used for Deserialization
    public AqlUISearchModel(@JsonProperty("id") String id, @JsonProperty("comparator") AqlComparatorEnum comparator,
            @JsonProperty("values") List<String> values) {
        this.id = id;
        this.values = values;
        if (values.stream().filter(value -> value.contains("*") || value.contains("?")).findAny().isPresent()) {
            this.comparator = AqlComparatorEnum.matches;
        } else if (comparator != null) {
            this.comparator = comparator;
        } else {
            this.comparator = AqlComparatorEnum.equals;
        }
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFullName() {
        return fullName;
    }

    public AqlComparatorEnum[] getAllowedComparators() {
        return allowedComparators;
    }

    @JsonIgnore
    public AqlComparatorEnum getComparator() {
        return comparator;
    }

    @JsonIgnore
    public List<String> getValues() {
        return values;
    }

    public boolean isVisibleByDefault() {
        return visibleByDefault;
    }

    public void setVisibleByDefault(boolean visibleByDefault) {
        this.visibleByDefault = visibleByDefault;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AqlUISearchModel aqlUISearchModel = (AqlUISearchModel) o;

        if (id != null ? !id.equals(aqlUISearchModel.id) : aqlUISearchModel.id != null) {
            return false;
        }

        if (displayName != null ? !displayName.equals(aqlUISearchModel.displayName) : aqlUISearchModel.displayName != null) {
            return false;
        }

        if (fullName != null ? !fullName.equals(aqlUISearchModel.fullName) : aqlUISearchModel.fullName != null) {
            return false;
        }

        if (visibleByDefault != aqlUISearchModel.visibleByDefault) {
            return false;
        }

        if (allowedComparators != null ? !Arrays.equals(allowedComparators, aqlUISearchModel.allowedComparators) : aqlUISearchModel.allowedComparators != null)
            return false;

        if (comparator != null ? !comparator.equals(aqlUISearchModel.comparator) : aqlUISearchModel.comparator != null) {
            return false;
        }

        return values != null ? values.equals(aqlUISearchModel.values) : aqlUISearchModel.values == null;
    }
}
