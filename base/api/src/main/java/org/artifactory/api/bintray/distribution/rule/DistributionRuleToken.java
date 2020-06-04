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

import java.util.List;
import java.util.Map;

/**
 * Denotes an available token for a distribution rule that resolves to a property set on the Artifact.
 *
 * @author Dan Feldman
 */
public abstract class DistributionRuleToken {

    protected String token;
    protected String value;

    public abstract String getToken();

    public abstract String getValue();

    /**
     * Populates the value of this token if it's actual key matches a key in the properties, or the path.
     * the {@param Properties} are used to pass any key-value pair that tokens might use such as actual properties
     * set on an Artifact or layout tokens and their actual value
     */
    public abstract void populateValue(RepoPath path, Properties pathProperties) throws Exception;

    /**
     * Populates the value of this token if it's actual key matches a key in the properties, or the path.
     * the {@param pathProperties} are used to pass any key-value pair that tokens might use such as actual properties
     * set on an Artifact or layout tokens and their actual value
     */
    public abstract void populateValue(RepoPath path, Map<String, List<String>> pathProperties) throws Exception;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DistributionRuleToken)) return false;
        DistributionRuleToken that = (DistributionRuleToken) o;
        return getToken() != null ? getToken().equals(that.getToken()) : that.getToken() == null;
    }

    @Override
    public int hashCode() {
        return getToken() != null ? getToken().hashCode() : 0;
    }
}
