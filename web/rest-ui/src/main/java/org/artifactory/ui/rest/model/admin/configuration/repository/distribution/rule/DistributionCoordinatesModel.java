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

import org.artifactory.api.bintray.distribution.rule.DistributionRuleToken;
import org.artifactory.descriptor.repo.distribution.rule.DistributionCoordinates;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.jfrog.common.config.diff.DiffIgnore;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dan Feldman
 */
public class DistributionCoordinatesModel extends DistributionCoordinates implements RestModel {

    //Only used by test rule service
    @DiffIgnore
    public Set<DistributionRuleToken> tokens = new HashSet<>();

    public DistributionCoordinatesModel() {

    }

    public DistributionCoordinatesModel(DistributionCoordinates distributionCoordinates) {
        super(distributionCoordinates);
    }

    public Set<DistributionRuleToken> getTokens() {
        return tokens;
    }

    public void setTokens(Set<DistributionRuleToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
