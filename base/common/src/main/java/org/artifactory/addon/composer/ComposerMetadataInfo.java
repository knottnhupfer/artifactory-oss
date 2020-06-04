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

package org.artifactory.addon.composer;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * @author Shay Bagants
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ComposerMetadataInfo {

    @JsonProperty("composerGeneralInfo")
    private ComposerInfo composerInfo;
    @JsonProperty("composerDependencies")
    private List<ComposerDependency> dependencies;
    private String description;

    public ComposerInfo getComposerInfo() {
        return composerInfo;
    }

    public void setComposerInfo(ComposerInfo composerInfo) {
        this.composerInfo = composerInfo;
    }

    public List<ComposerDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<ComposerDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
