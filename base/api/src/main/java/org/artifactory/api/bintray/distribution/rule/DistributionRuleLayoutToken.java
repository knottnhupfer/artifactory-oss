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

package org.artifactory.api.bintray.distribution.rule;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

import java.util.List;
import java.util.Map;

/**
 * Denotes an available token for a distribution rule that resolves to section of the layout this artifact belongs to
 * (i.e. the layout of the repo storing this artifact)
 *
 * @author Dan Feldman
 */
public class DistributionRuleLayoutToken extends DistributionRuleToken {

    public DistributionRuleLayoutToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void populateValue(RepoPath path, Properties pathProperties) throws Exception {
        value = pathProperties.getFirst(token);
        if (StringUtils.isBlank(value)) {
            throw new Exception("No value was resolved for layout token " + token + " on artifact " + path.toPath());
        }
    }

    @Override
    public void populateValue(RepoPath path, Map<String, List<String>> pathProperties) throws Exception {
        List<String> values = pathProperties.get(token);
        if (CollectionUtils.isEmpty(values)) {
            throw new Exception("No value was resolved for layout token " + token + " on artifact " + path.toPath());
        }
        value = values.get(0);
        if (StringUtils.isBlank(value)) {
            throw new Exception("No value was resolved for layout token " + token + " on artifact " + path.toPath());
        }
    }
}
