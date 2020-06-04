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

import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.jfrog.access.token.JwtAccessToken.SCOPE_TOKEN_PATTERN;
import static org.jfrog.access.token.JwtAccessToken.SCOPE_TOKEN_REGEX;

/**
 * Utility for handling member-of-groups scope token
 *
 * @author Yinon Avraham.
 */
public class MemberOfGroupsScopeToken {

    private static final String PREFIX = "member-of-groups:";
    private static final String GROUP_DELIMITER = ",";
    public static final Pattern SCOPE_MEMBER_OF_GROUPS_PATTERN = Pattern.compile(
            //member-of-groups:g1,g2,g3  (at least one group)
            PREFIX + SCOPE_TOKEN_REGEX + "(" + GROUP_DELIMITER + SCOPE_TOKEN_REGEX + ")*");

    /**
     * Check whether a scope token is a valid member-of-groups scope token
     * @param scopeToken the scope token to parse
     */
    public static boolean accepts(String scopeToken) {
        return scopeToken != null && SCOPE_MEMBER_OF_GROUPS_PATTERN.matcher(scopeToken).matches();
    }

    /**
     * Parse a scope token as member-of-groups
     * @param scopeToken the scope token to parse
     * @return the parsed instance
     */
    @Nonnull
    public static MemberOfGroupsScopeToken parse(String scopeToken) {
        if (!accepts(scopeToken)) {
            throw new IllegalArgumentException("Not a valid member-of-groups scope token:" + scopeToken);
        }
        scopeToken = scopeToken.replace("\"","");
        String[] groupNames = scopeToken.substring(PREFIX.length()).split(GROUP_DELIMITER);
        Set<String> groupNameSet = Sets.newLinkedHashSet(Arrays.asList(groupNames)); //predictable order
        return new MemberOfGroupsScopeToken(groupNameSet);
    }

    private final Set<String> groupNames = Sets.newLinkedHashSet(); //predictable order

    public MemberOfGroupsScopeToken(@Nonnull Set<String> groupNames) {
        assertValidGroupNames(groupNames);
        this.groupNames.addAll(groupNames);
    }

    private void assertValidGroupNames(@Nonnull Set<String> groupNames) {
        if (requireNonNull(groupNames, "group names are required").isEmpty()) {
            throw new IllegalArgumentException("at least one group name is required");
        }
        groupNames.forEach(groupName -> {
            if (!SCOPE_TOKEN_PATTERN.matcher(groupName).matches()) {
                throw new IllegalArgumentException("Group name is not a valid scope token: " + groupName);
            }
        });
    }

    /**
     * Get the group names in this scope token
     */
    @Nonnull
    public Set<String> getGroupNames() {
        return Collections.unmodifiableSet(groupNames);
    }

    /**
     * Get the formatted scope token
     */
    @Nonnull
    public String getScopeToken() {
        return PREFIX + String.join(GROUP_DELIMITER, groupNames);
    }

    @Override
    public String toString() {
        return getScopeToken();
    }
}
