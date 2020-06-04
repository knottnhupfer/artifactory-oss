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

package org.artifactory.ui.rest.model.admin.configuration.repository.distribution.rule;

import org.artifactory.api.bintray.distribution.rule.DistributionRuleTokens;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.jfrog.common.config.diff.DiffIgnore;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
public class DistributionRuleModel extends DistributionRule implements RestModel {

    private String packageType;
    @DiffIgnore
    private List<DistributionRuleTokenModel> availableTokens;
    //Both of these are used by the UI only when testing paths - backend does not pass them.
    private String testPath;
    private String productName;

    public DistributionRuleModel() {

    }

    public DistributionRuleModel(DistributionRule distRule) {
        this.setName(distRule.getName());
        this.packageType = distRule.getType().name();
        this.setRepoFilter(distRule.getRepoFilter());
        this.setPathFilter(distRule.getPathFilter());
        this.setDistributionCoordinates(new DistributionCoordinatesModel(distRule.getDistributionCoordinates()));
        this.availableTokens = DistributionRuleTokens.tokensByType(distRule.getType(), null)
                .stream()
                .map(DistributionRuleTokenModel::new)
                .collect(Collectors.toList());
        this.availableTokens.add(new DistributionRuleTokenModel(DistributionRuleTokens.getProductNameToken()));
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public List<DistributionRuleTokenModel> getAvailableTokens() {
        return availableTokens;
    }

    public void setAvailableTokens(List<DistributionRuleTokenModel> availableTokens) {
        this.availableTokens = availableTokens;
    }

    public String getTestPath() {
        return testPath;
    }

    public void setTestPath(String testPath) {
        this.testPath = testPath;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
