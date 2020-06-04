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

package org.artifactory.ui.rest.model.admin.configuration.layouts;

import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Lior Hasson
 */
public class LayoutGridModel extends BaseModel {
    private String name;
    private String artifactPathPattern;
    private LayoutActionsModel layoutActions;

    public LayoutGridModel(RepoLayout repoLayout) {
        this.name = repoLayout.getName();
        this.artifactPathPattern = repoLayout.getArtifactPathPattern();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtifactPathPattern() {
        return artifactPathPattern;
    }

    public void setArtifactPathPattern(String artifactPathPattern) {
        this.artifactPathPattern = artifactPathPattern;
    }

    public LayoutActionsModel getLayoutActions() {
        return layoutActions;
    }

    public void setLayoutActions(LayoutActionsModel layoutActions) {
        this.layoutActions = layoutActions;
    }
}
