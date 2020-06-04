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

import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.jfrog.client.util.PathUtils;

import java.util.List;
import java.util.Map;

/**
 * Denotes an available token for a distribution rule that resolves to the Artifact's path.
 * Can optionally use {@param pathElement} to indicate the value should be populated by the path element in that
 * position after running the path through {@link PathUtils#getPathElements}.
 *
 * @author Dan Feldman
 */
public class DistributionRulePathToken extends DistributionRuleToken {

    private int pathElement;

    DistributionRulePathToken(String token) {
        this.token = token;
        this.pathElement = 0;
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
        populateValueInternal(path);
    }

    @Override
    public void populateValue(RepoPath path, Map<String, List<String>> pathProperties) throws Exception {
        populateValueInternal(path);
    }

    private void populateValueInternal(RepoPath path) {
        if (pathElement > 0) {
            String[] elements = PathUtils.getPathElements(path.getPath());
            if (elements.length < pathElement) {
                throw new IllegalArgumentException("Token " + token + " is unable to resolve a value from path " +
                        path + " as it's trying to match a path location " + pathElement + " that doesn't exist.");
            } else {
                value = elements[pathElement - 1];
            }
        } else {
            value = path.getPath();
        }
    }
}
