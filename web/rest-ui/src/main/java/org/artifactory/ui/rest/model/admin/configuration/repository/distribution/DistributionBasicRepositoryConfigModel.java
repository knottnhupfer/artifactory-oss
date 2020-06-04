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

package org.artifactory.ui.rest.model.admin.configuration.repository.distribution;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.xray.XrayRepoConfigModel;

import java.util.HashSet;
import java.util.Set;

import static org.artifactory.repo.config.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 */
public class DistributionBasicRepositoryConfigModel extends LocalBasicRepositoryConfigModel {

    protected String layout = DEFAULT_REPO_LAYOUT;
    private String productName;
    private boolean defaultNewRepoPrivate = DEFAULT_NEW_BINTRAY_REPO_PRIVATE;
    private boolean defaultNewRepoPremium = DEFAULT_NEW_BINTRAY_REPO_PREMIUM;
    private Set<String> defaultLicenses = new HashSet<>();
    private String defaultVcsUrl;

    @Override
    public String getLayout() {
        return layout;
    }

    @Override
    public void setLayout(String layout) {
        //Always generic
    }

    @Override
    public XrayRepoConfigModel getXrayConfig() {
        return null;
    }

    @Override
    public void setXrayConfig(XrayRepoConfigModel xrayConfig) {

    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public boolean getDefaultNewRepoPrivate() {
        return defaultNewRepoPrivate;
    }

    public void setDefaultNewRepoPrivate(boolean defaultNewRepoPrivate) {
        this.defaultNewRepoPrivate = defaultNewRepoPrivate;
    }

    public boolean getDefaultNewRepoPremium() {
        return defaultNewRepoPremium;
    }

    public void setDefaultNewRepoPremium(boolean defaultNewRepoPremium) {
        this.defaultNewRepoPremium = defaultNewRepoPremium;
    }

    public Set<String> getDefaultLicenses() {
        return defaultLicenses;
    }

    public void setDefaultLicenses(Set<String> defaultLicenses) {
        this.defaultLicenses = defaultLicenses;
    }

    public String getDefaultVcsUrl() {
        return defaultVcsUrl;
    }

    public void setDefaultVcsUrl(String defaultVcsUrl) {
        this.defaultVcsUrl = defaultVcsUrl;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
