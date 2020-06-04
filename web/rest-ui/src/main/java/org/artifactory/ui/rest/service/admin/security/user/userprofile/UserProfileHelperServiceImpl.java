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

package org.artifactory.ui.rest.service.admin.security.user.userprofile;

import org.artifactory.api.security.UserGroupService;
import org.artifactory.security.InternalUsernamePasswordAuthenticationToken;
import org.artifactory.security.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * @author Tamir Hadad
 */
@Service
public class UserProfileHelperServiceImpl implements UserProfileHelperService {

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * load current logged user info
     *
     * @return - user info data
     */
    @Override
    public UserInfo loadUserInfo() {
        // load the user directly from the database. the instance returned from currentUser() might not
        // be with the latest changes
        return userGroupService.findUser(userGroupService.currentUser().getUsername());
    }

    /**
     * authenticate current logged user against entered password
     *
     * @param userInfo               - logged user info
     * @param enteredCurrentPassword - entered password
     * @return - if true user is authenticated
     */
    @Override
    public boolean authenticate(UserInfo userInfo, String enteredCurrentPassword) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new InternalUsernamePasswordAuthenticationToken(userInfo.getUsername(),
                            enteredCurrentPassword));
            return (authentication != null) && authentication.isAuthenticated();
        } catch (AuthenticationException e) {
            return false;
        }
    }
}
