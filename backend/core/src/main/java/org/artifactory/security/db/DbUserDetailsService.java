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

package org.artifactory.security.db;

import org.artifactory.security.SimpleUser;
import org.artifactory.security.UserInfo;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

/**
 * This class provides both the user details and the password salt. Configured in the security.xml.
 *
 * @author freds
 */
@Deprecated
@Repository("dbUserDetailsService")
public class DbUserDetailsService implements UserDetailsService {

    @Autowired
    private UserGroupStoreService userGroupStore;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userGroupStore.findUser(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("The user: '%s' does not exists!", username));
        }
        return new SimpleUser(user);
    }

    public Object getSalt(UserDetails user) {
        return ((SimpleUser) user).getSalt();
    }
}
