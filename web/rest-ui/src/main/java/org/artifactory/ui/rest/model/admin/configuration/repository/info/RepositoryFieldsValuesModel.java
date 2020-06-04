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

package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.bintray.distribution.rule.DistributionRuleToken;
import org.artifactory.api.bintray.distribution.rule.DistributionRuleTokens;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.rule.DefaultDistributionRules;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.util.distribution.DistributionConstants;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.artifactory.util.distribution.DistributionConstants.PATH_TOKEN;

/**
 * @author Aviad Shikloshi
 */
public class RepositoryFieldsValuesModel {

    private List<String> repositoryLayouts;
    private List<String> packageTypes;
    private List<String> webStartKeyPairs;
    private List<String> availableLocalRepos;
    private List<String> availableRemoteRepos;
    private List<String> availableVirtualRepos;
    private List<String> proxies;
    private String defaultProxy;
    private Map<RepoType, List<String>> distributionTokensByType;
    private Map<String, List<String>> distributionTokensByLayout;
    private List<DistributionRule> distributionDefaultRules;
    private List<DistributionRule> distributionDefaultProductRules;

    public RepositoryFieldsValuesModel(CentralConfigDescriptor descriptor, RepositoryService repositoryService) {
        repositoryLayouts = descriptor.getRepoLayouts().stream()
                .map(RepoLayout::getName)
                .collect(Collectors.toList());

        packageTypes = Lists.newArrayList(RepoType.values()).stream()
                .map(RepoType::name)
                .collect(Collectors.toList());

        webStartKeyPairs = ContextHelper.get().beanForType(AddonsManager.class).addonByType(ArtifactWebstartAddon.class)
                .getKeyPairNames();

        availableLocalRepos = repositoryService.getLocalRepoDescriptors().stream()
                .map(LocalRepoDescriptor::getKey)
                .collect(Collectors.toList());

        availableRemoteRepos = repositoryService.getRemoteRepoDescriptors().stream()
                .map(RemoteRepoDescriptor::getKey)
                .collect(Collectors.toList());

        availableVirtualRepos = repositoryService.getVirtualRepoDescriptors()
                .stream()
                .map(VirtualRepoDescriptor::getKey)
                .collect(Collectors.toList());

        CentralConfigDescriptor centralDescriptor = ContextHelper.get().beanForType(CentralConfigService.class)
                .getDescriptor();

        proxies = Lists.newArrayList(centralDescriptor.getProxies().stream()
                .map(ProxyDescriptor::getKey)
                .collect(Collectors.toList()));

        ProxyDescriptor proxy = descriptor.getDefaultProxy();
        if (proxy != null) {
            defaultProxy = proxy.getKey();
        }

        distributionTokensByType = Maps.newHashMap();
        for (RepoType type : RepoType.values()) {
            List<String> models = DistributionRuleTokens.tokensByType(type, null)
                    .stream()
                    .map(DistributionRuleToken::getToken)
                    .collect(Collectors.toList());
            models.add(DistributionRuleTokens.getProductNameToken().getToken());
            distributionTokensByType.put(type, models);
        }
        distributionTokensByLayout = populateLayoutTokens(centralDescriptor);
        distributionDefaultRules = DefaultDistributionRules.getDefaultRules();
        distributionDefaultProductRules = DefaultDistributionRules.getDefaultProductRules();
    }

    private Map<String, List<String>> populateLayoutTokens(CentralConfigDescriptor centralDescriptor) {
        Map<String, List<String>> layoutTokens = Maps.newHashMap();
        centralDescriptor.getRepoLayouts().forEach(layout -> layoutTokens.put(layout.getName(), tokensForLayout(layout)));
        return layoutTokens;
    }

    private List<String> tokensForLayout(RepoLayout layout) {
        List<String> layoutTokens = RepoLayoutUtils.getLayoutTokens(layout)
                .stream()
                .map(s -> DistributionConstants.wrapToken(s.replace("[", "").replace("]", "")))
                .collect(Collectors.toList());
        layoutTokens.add(PATH_TOKEN);
        return layoutTokens;
    }

    public List<String> getRepositoryLayouts() {
        return repositoryLayouts;
    }

    public List<String> getPackageTypes() {
        return packageTypes;
    }

    public List<String> getAvailableLocalRepos() {
        return availableLocalRepos;
    }

    public List<String> getAvailableRemoteRepos() {
        return availableRemoteRepos;
    }

    public List<String> getAvailableVirtualRepos() {
        return availableVirtualRepos;
    }

    public List<String> getWebStartKeyPairs() {
        return webStartKeyPairs;
    }

    public List<String> getProxies() {
        return proxies;
    }

    public String getDefaultProxy() {
        return defaultProxy;
    }

    public Map<RepoType, List<String>> getDistributionTokensByType() {
        return distributionTokensByType;
    }

    public Map<String, List<String>> getDistributionTokensByLayout() {
        return distributionTokensByLayout;
    }

    public List<DistributionRule> getDistributionDefaultRules() {
        return distributionDefaultRules;
    }

    public List<DistributionRule> getDistributionDefaultProductRules() {
        return distributionDefaultProductRules;
    }

}
