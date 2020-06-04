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

package org.artifactory.security.providermgr;

import org.springframework.security.authentication.AuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

/**
 * Model object for saving bearer tokens
 *
 * @author Chen Keinan
 */
public abstract class ArtifactoryCacheKey {
    private String user;
    protected String basicauth;
    private String repoKey;
    protected AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
    protected HttpServletRequest servletRequest;

    public ArtifactoryCacheKey(String user, String basicauth, String repoKey,
            AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource,
            HttpServletRequest servletRequest) {
        this.user = user;
        this.basicauth = basicauth;
        this.repoKey = repoKey;
        this.authenticationDetailsSource = authenticationDetailsSource;
        this.servletRequest = servletRequest;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBasicauth() {
        return basicauth;
    }

    public void setBasicauth(String basicauth) {
        this.basicauth = basicauth;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArtifactoryCacheKey that = (ArtifactoryCacheKey) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        if (basicauth != null ? !basicauth.equals(that.basicauth) : that.basicauth != null) return false;
        return !(repoKey != null ? !repoKey.equals(that.repoKey) : that.repoKey != null);

    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (basicauth != null ? basicauth.hashCode() : 0);
        result = 31 * result + (repoKey != null ? repoKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TokenCacheKey{" +
                "scope='" + user + '\'' +
                ", realm='" + basicauth + '\'' +
                ", repoKey='" + repoKey + '\'' +
                '}';
    }

    public abstract ProviderMgr getProviderMgr();
}
