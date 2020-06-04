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

/**
 * Created by Yinon Avraham.
 */
public class UserTokenSpec extends TokenSpec<UserTokenSpec> {

    private static final int MAX_USERNAME_LENGTH = 58;

    private final String username;

    private UserTokenSpec(@Nullable String username) {
        this.username = username;//TODO [YA] requireNonBlank(username, "username is required"); currently can't require because the spec is used also for refresh where the username is not required
    }

    /**
     * Create a new user token specification.
     * @param username the username
     * @return a new empty user token specification
     */
    @Nonnull
    public static UserTokenSpec create(@Nullable String username) {
        return new UserTokenSpec(username);
    }

    /**
     * Get the username the token is for
     */
    @Nullable
    public String getUsername() {
        return username;
    }

    @Override
    public SubjectFQN createSubject(ServiceId serviceId) {
        validateUsername();
        return new SubjectFQN(serviceId, SubjectFQN.USERS_NAME_PART, username);
    }

    private void validateUsername() {
        requireNonBlank(username, "username is required");
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                    "username length exceeds maximum length of " + MAX_USERNAME_LENGTH + " characters");
        }
    }

    public static boolean isUserToken(TokenInfo tokenInfo) {
        return isUserTokenSubject(tokenInfo.getSubject());
    }

    // TODO: [NS] Move uses of these methods to not static after figuring out where to implement them
    public static boolean isUserTokenSubject(String subject) {
        return SubjectFQN.isUserTokenSubject(subject);
    }

    @Nonnull
    public static String extractUsername(String subject) {
        return SubjectFQN.extractUsername(subject);
    }
}
