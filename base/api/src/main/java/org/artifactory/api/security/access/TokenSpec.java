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

package org.artifactory.api.security.access;

import org.jfrog.access.common.ServiceId;
import org.jfrog.access.common.SubjectFQN;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Access token specification
 * @author Yinon Avraham
 */
public abstract class TokenSpec<T extends TokenSpec> {

    private List<String> scope;
    private List<String> audience;
    private Boolean refreshable;
    private Long expiresIn;

    /**
     * The scope the token provides
     * @param scope a list of scope tokens
     * @return this {@link TokenSpec}
     */
    public T scope(List<String> scope) {
        this.scope = scope;
        return self();
    }

    /**
     * The audience the token is targeted for
     * @param audience a list of service IDs
     * @return this {@link TokenSpec}
     */
    public T audience(List<String> audience) {
        this.audience = audience;
        return self();
    }

    /**
     * Whether the token should be created as refreshable
     * @param refreshable <code>true</code> for refreshable token (a refresh token will be created for the token),
     *                    <code>false</code> otherwise.
     * @return this {@link TokenSpec}
     */
    public T refreshable(Boolean refreshable) {
        this.refreshable = refreshable;
        return self();
    }

    /**
     * The time in seconds before the token is expired
     * @param expiresIn the time in seconds
     * @return this {@link TokenSpec}
     */
    public T expiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
        return self();
    }

    /**
     * Set the token as non-expirable
     * @return this {@link TokenSpec}
     */
    public T noExpiry() {
        this.expiresIn = 0L;
        return self();
    }

    private T self() {
        return (T) this;
    }

    /**
     * Create the subject for this token, based on the provided service ID
     * @param serviceId the service ID under which to create the subject
     * @return the subject's fully qualified name
     */
    public abstract SubjectFQN createSubject(ServiceId serviceId);

    /**
     * Get the scope the token provides
     * @return a list of scope tokens
     */
    @Nonnull
    public List<String> getScope() {
        return scope == null ? emptyList() : scope;
    }

    /**
     * Get the audience the token is targeted for
     * @return  a list of service IDs
     */
    @Nonnull
    public List<String> getAudience() {
        return audience == null ? emptyList() : audience;
    }

    /**
     * Get the actual refreshable value set in this specification
     * @see #isRefreshable()
     */
    @Nullable
    public Boolean getRefreshable() {
        return refreshable;
    }

    /**
     * Whether the token should be created as refreshable
     * @see #getRefreshable()
     */
    public boolean isRefreshable() {
        return Boolean.TRUE.equals(refreshable);
    }

    /**
     * Get the time in seconds before the token is expired
     */
    @Nullable
    public Long getExpiresIn() {
        return expiresIn;
    }

    protected static String requireNonBlank(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
