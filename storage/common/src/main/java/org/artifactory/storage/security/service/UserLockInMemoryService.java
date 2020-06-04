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

package org.artifactory.storage.security.service;

import javax.annotation.Nonnull;

/**
 * @author Noam Shemesh
 */
public interface UserLockInMemoryService {

    /**
     * Calculates next login if user previously
     * failed to login due to incorrect credentials
     * and now have to wait before trying to login again
     *
     * @param loginIdentifier - Can be user/access token or api key
     * @return login delay or -1 if user login should
     * not be delayed (e.g last login was successful)
     */
    long getNextLoginTime(String loginIdentifier);

    /**
     * Calculates next login if user previously
     * failed to login due to incorrect credentials
     * and now have to wait before trying to login again
     *
     * @param incorrectLoginAttempts
     * @param lastAccessTimeMillis
     * @return login delay or -1 if user login should
     * not be delayed (e.g last login was successful)
     */
    long getNextLoginTime(int incorrectLoginAttempts, long lastAccessTimeMillis);

    /**
     * Checks whether given user is locked
     * <p>
     * note: this method using caching in sake
     * of DB load preventing
     *
     * @param loginIdentifier - Can be user/access token or api key
     * @return boolean
     */
    boolean isUserLocked(String loginIdentifier);

    /**
     * Locks user upon incorrect login attempt
     *
     * @param loginIdentifier - Can be user/access token or api key
     */
    void lockUser(@Nonnull String loginIdentifier);

    /**
     * Unlocks locked out user
     *
     * @param userName
     */
    void unlockUser(@Nonnull String userName);

    /**
     * Unlocks all locked out users
     */
    void unlockAllUsers();

    /**
     * Updates user access details
     *
     * @param loginIdentifier - Can be user/access token or api key
     * @param accessTimeMillis
     */
    void updateUserAccess(String loginIdentifier, boolean userLockPolicyEnabled, long accessTimeMillis);

    /**
     * Registers incorrect login attempt
     *
     * @param loginIdentifier - Can be user/access token or api key
     */
    void registerIncorrectLoginAttempt(@Nonnull String loginIdentifier);

    /**
     * Resets logon failures
     *
     * @param loginIdentifier - Can be user/access token or api key
     */
    void resetIncorrectLoginAttempts(@Nonnull String loginIdentifier);

    /**
     * @param loginIdentifier - Can be user/access token or api key
     * @return incorrect login attempts for user
     */
    int getIncorrectLoginAttempts(@Nonnull String loginIdentifier);

}