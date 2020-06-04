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

package org.artifactory.addon.docker.rest;

import java.util.Map;

/**
 * Model object for saving bearer tokens
 *
 * @author Shay Yaakov
 */
public class DockerTokenCacheKey {
    private final String scope;
    private final String realm;
    private final String service;
    private final String repoKey;

    public DockerTokenCacheKey(Map<String, String> challengeParams, String repoKey) {
        this.repoKey = repoKey;
        this.scope = challengeParams.get("scope");
        this.realm = challengeParams.get("realm");
        this.service = challengeParams.get("service");
    }

    public String getScope() {
        return scope;
    }

    public String getRealm() {
        return realm;
    }

    public String getService() {
        return service;
    }

    public String getRepoKey() {
        return repoKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerTokenCacheKey that = (DockerTokenCacheKey) o;

        if (scope != null ? !scope.equals(that.scope) : that.scope != null) return false;
        if (realm != null ? !realm.equals(that.realm) : that.realm != null) return false;
        if (service != null ? !service.equals(that.service) : that.service != null) return false;
        return !(repoKey != null ? !repoKey.equals(that.repoKey) : that.repoKey != null);

    }

    @Override
    public int hashCode() {
        int result = scope != null ? scope.hashCode() : 0;
        result = 31 * result + (realm != null ? realm.hashCode() : 0);
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (repoKey != null ? repoKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TokenCacheKey{" +
                "scope='" + scope + '\'' +
                ", realm='" + realm + '\'' +
                ", service='" + service + '\'' +
                ", repoKey='" + repoKey + '\'' +
                '}';
    }
}
