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
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jfrog.client.util.PathUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Denotes an available token for a distribution rule that resolves to a property set on the Artifact.
 *
 * @author Dan Feldman
 */
public class DistributionRulePropertyToken extends DistributionRuleToken {

    @JsonIgnore
    private String propertyKey;

    public DistributionRulePropertyToken(String token, String key) {
        this.token = token;
        this.propertyKey = key;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getValue() {
        return value;
    }

    @JsonIgnore
    public String getPropertyKey() {
        return propertyKey;
    }

    @Override
    public void populateValue(RepoPath path, Properties pathProperties) throws Exception {
        Set<String> values = pathProperties.get(propertyKey);
        if (CollectionUtils.isEmpty(values)) {
            throw new Exception("No value was resolved for property token " + token + " on artifact " + path.toPath()
                    + ", which is resolved from property: " + propertyKey);
        }
        value = PathUtils.collectionToDelimitedString(values);
    }

    @Override
    public void populateValue(RepoPath path, Map<String, List<String>> pathProperties) throws Exception {
        List<String> values = pathProperties.get(propertyKey);
        if (CollectionUtils.isEmpty(values)) {
            throw new Exception("No value was resolved for property token " + token + " on artifact " + path.toPath()
                    + ", which is resolved from property: " + propertyKey);
        }
        value = PathUtils.collectionToDelimitedString(values);
    }
}
