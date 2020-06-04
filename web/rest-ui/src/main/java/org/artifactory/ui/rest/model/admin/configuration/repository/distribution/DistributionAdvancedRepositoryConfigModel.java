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

import com.google.common.collect.Lists;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.DownloadRedirectRepoConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_GPG_SIGN;

/**
 * @author Dan Feldman
 */
public class DistributionAdvancedRepositoryConfigModel extends LocalAdvancedRepositoryConfigModel {

    private List<DistributionRule> distributionRules = Lists.newArrayList();
    private String proxy;
    private boolean gpgSign = DEFAULT_GPG_SIGN;
    private String gpgPassPhrase;
    private Set<String> whiteListedProperties = new HashSet<>();

    public List<DistributionRule> getDistributionRules() {
        return distributionRules;
    }

    public void setDistributionRules(List<DistributionRule> distributionRules) {
        this.distributionRules = distributionRules;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Set<String> getWhiteListedProperties() {
        return whiteListedProperties;
    }

    public void setWhiteListedProperties(Set<String> whiteListedProperties) {
        this.whiteListedProperties = whiteListedProperties;
    }

    public boolean isGpgSign() {
        return gpgSign;
    }

    public void setGpgSign(boolean gpgSign) {
        this.gpgSign = gpgSign;
    }

    public String getGpgPassPhrase() {
        return gpgPassPhrase;
    }

    public void setGpgPassPhrase(String gpgPassPhrase) {
        this.gpgPassPhrase = gpgPassPhrase;
    }

    @Override
    public DownloadRedirectRepoConfigModel getDownloadRedirectConfig() {
        return null;
    }

    @Override
    public void setDownloadRedirectConfig(DownloadRedirectRepoConfigModel downloadRedirectRepoConfig) {
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
