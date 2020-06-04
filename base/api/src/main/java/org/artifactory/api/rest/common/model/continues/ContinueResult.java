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
package org.artifactory.api.rest.common.model.continues;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Omri Ziv
 */
@Data
@AllArgsConstructor
@JsonSerialize
public class ContinueResult<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String continueState;
    List<T> data;

    public ContinueResult(int continueState, List<T> data) {
        this.continueState = continueState + "";
        this.data = data;
    }

    public ContinueResult() {
        this.data = new ArrayList<>();
    }

    public void merge(ContinueResult<T> other) {
        this.continueState = other.getContinueState();
        if (data == null) {
            data = new ArrayList<>();
        }
        data.addAll(other.getData());
    }

}
