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

package org.artifactory.ui.rest.model.admin.configuration.repository.local;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.BasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.xray.XrayRepoConfigModel;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_INCLUDES_PATTERN;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
public class LocalBasicRepositoryConfigModel implements BasicRepositoryConfigModel {

    private String publicDescription;
    private String internalDescription;
    protected String includesPattern = DEFAULT_INCLUDES_PATTERN;
    protected String excludesPattern;
    protected String layout;
    private XrayRepoConfigModel xrayConfig;

    public XrayRepoConfigModel getXrayConfig() {
        return xrayConfig;
    }

    public void setXrayConfig(XrayRepoConfigModel xrayConfig) {
        this.xrayConfig = xrayConfig;
    }

    @Override
    public String getPublicDescription() {
        return publicDescription;
    }

    @Override
    public void setPublicDescription(String publicDescription) {
        this.publicDescription = publicDescription;
    }

    @Override
    public String getInternalDescription() {
        return internalDescription;
    }

    @Override
    public void setInternalDescription(String internalDescription) {
        this.internalDescription = internalDescription;
    }

    @Override
    public String getIncludesPattern() {
        return includesPattern;
    }

    @Override
    public void setIncludesPattern(String includesPattern) {
        this.includesPattern = includesPattern;
    }

    @Override
    public String getExcludesPattern() {
        return excludesPattern;
    }

    @Override
    public void setExcludesPattern(String excludesPattern) {
        this.excludesPattern = excludesPattern;
    }

    @Override
    public String getLayout() {
        return layout;
    }

    @Override
    public void setLayout(String layout) {
        this.layout = layout;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
