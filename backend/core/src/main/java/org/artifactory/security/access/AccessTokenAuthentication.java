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

package org.artifactory.security.access;

import org.jfrog.access.token.JwtAccessToken;
import org.jfrog.access.token.JwtAccessTokenImpl;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author Yinon Avraham
 */
public class AccessTokenAuthentication extends AbstractAuthenticationToken {

    private final JwtAccessToken accessToken;
    //TODO [YA] remove the principal once supporting access token authorization. Then the principal is actually the token.
    private final Object principal;

    /**
     * Creates a token with the supplied array of authorities.
     * @param tokenValue  the access token represented by this authentication.
     * @param principal   the principal represented by this authentication.
     * @param authorities the collection of <code>GrantedAuthority</code>s for the
     * @throws IllegalArgumentException if the given token value is not in the expected format.
     */
    public AccessTokenAuthentication(@Nonnull String tokenValue, @Nullable Object principal,
            @Nullable Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.accessToken = JwtAccessTokenImpl.parseTokenValue(tokenValue);
        this.principal = principal;
    }

    /**
     * Creates a token with the supplied array of authorities.
     * @param accessToken the access token represented by this authentication.
     * @param principal   the principal represented by this authentication.
     * @param authorities the collection of <code>GrantedAuthority</code>s for the
     */
    public AccessTokenAuthentication(@Nonnull JwtAccessToken accessToken, @Nullable Object principal,
            @Nullable Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.accessToken = accessToken;
        this.principal = principal;
    }

    @Nonnull
    public JwtAccessToken getAccessToken() {
        return accessToken;
    }

    @Override
    public Object getCredentials() {
        return accessToken.getTokenValue();
    }

    @Nullable
    @Override
    public Object getPrincipal() {
        return principal;
    }
}
