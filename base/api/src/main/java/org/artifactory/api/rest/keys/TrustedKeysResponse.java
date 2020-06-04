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

package org.artifactory.api.rest.keys;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author Rotem Kfir
 */
@ToString
public class TrustedKeysResponse {

    @JsonProperty
    private final List<TrustedKeyResponse> keys = Lists.newArrayList();

    @Nonnull
    public List<TrustedKeyResponse> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    public TrustedKeysResponse keys(@Nullable List<TrustedKeyResponse> keys) {
        this.keys.clear();
        if (keys != null) {
            this.keys.addAll(keys);
        }
        return this;
    }
}
