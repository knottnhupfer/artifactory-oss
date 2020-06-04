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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

/**
 * @author Yinon Avraham.
 */
public class MemberOfGroupsScopeTokenTest {

    @Test(dataProvider = "provideValidScopeTokenAndGroupNames")
    public void testParse(String scopeToken, Set<String> groupNames) throws Exception {
        assertEquals(MemberOfGroupsScopeToken.parse(scopeToken).getGroupNames(), groupNames);
    }

    @Test(dataProvider = "provideValidScopeTokenAndGroupNames")
    public void testGetScopeToken(String scopeToken, Set<String> groupNames) throws Exception {
        assertEquals(MemberOfGroupsScopeToken.parse(scopeToken).getScopeToken(), scopeToken);
        assertEquals(new MemberOfGroupsScopeToken(groupNames).getScopeToken(), scopeToken);
        assertEquals(new MemberOfGroupsScopeToken(groupNames).toString(), scopeToken);
    }

    @DataProvider
    public static Object[][] provideValidScopeTokenAndGroupNames() {
        return new Object[][]{
                { "member-of-groups:g1", Sets.newLinkedHashSet(asList("g1")) },
                { "member-of-groups:g1,g2", Sets.newLinkedHashSet(asList("g1", "g2")) },
                { "member-of-groups:g1,g2,g3", Sets.newLinkedHashSet(asList("g1", "g2", "g3")) },
                { "member-of-groups:readers", Sets.newLinkedHashSet(asList("readers")) },
                { "member-of-groups:readers,deployers", Sets.newLinkedHashSet(asList("readers", "deployers")) },
                { "member-of-groups:*", Sets.newLinkedHashSet(asList("*")) }
        };
    }

    @Test(dataProvider = "provideScopeTokens")
    public void testAccepts(String scopeToken, boolean expected) throws Exception {
        assertEquals(MemberOfGroupsScopeToken.accepts(scopeToken), expected);
    }

    @DataProvider
    public static Object[][] provideScopeTokens() {
        return new Object[][]{
                { "member-of-groups:g1", true },
                { "member-of-groups:g1,g2", true },
                { "member-of-groups:g1,g2,g3", true },
                { "member-of-groups:readers", true },
                { "member-of-groups:readers,deployers", true },
                { "member-of-groups:*", true },
                { "member-of-groups:g1,g2\\", false },
                { "member-of-groups:g1,\\g2", false },
                { "member-of-groups:g1\\,g2", false },
                { "member-of-groups:\\g1,g2", false },
                { "member-of-groups:\"g1,g2 with space\"", true },
                { "member-of-groups:\"g1 with space,g2", true },
                { "member-of-groups :g1,g2", false },
                { "member-of-group:g1,g2", false },
                { "\\member-of-groups:g1,g2", false },
                { "foo:g1", false },
                { "", false },
                { null, false },
        };
    }
}