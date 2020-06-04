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

package org.artifactory.ldap;

import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.security.ldap.NewFilterBasedLdapUserSearch;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author gidis
 */
public class LdapUserSearchesHelper {
    public static List<LdapUserSearch> getLdapUserSearches(ContextSource ctx, LdapSetting settings, boolean aol) {
        SearchPattern searchPattern = settings.getSearch();
        String[] searchBases;
        if (searchPattern.getSearchBase() == null) {
            searchBases = new String[]{""};
        } else {
            searchBases = searchPattern.getSearchBase().split(Pattern.quote("|"));
        }
        boolean useObjectInjectionProtection = isObjectInjectionProtection(settings, aol);
        ArrayList<LdapUserSearch> result = new ArrayList<>();
        for (String base : searchBases) {
            LdapUserSearch userSearch;
            String filter = searchPattern.getSearchFilter();
            BaseLdapPathContextSource baseLdapCtx = (BaseLdapPathContextSource) ctx;
            if (useObjectInjectionProtection) {
                NewFilterBasedLdapUserSearch search = new NewFilterBasedLdapUserSearch(base, filter, baseLdapCtx);
                search.setSearchSubtree(searchPattern.isSearchSubTree());
                userSearch = search;
            } else {
                FilterBasedLdapUserSearch search = new FilterBasedLdapUserSearch(base, filter, baseLdapCtx);
                search.setSearchSubtree(searchPattern.isSearchSubTree());
                userSearch = search;
            }
            result.add(userSearch);
        }
        return result;
    }

    public static boolean isObjectInjectionProtection(LdapSetting settings, boolean isAol) {
        if (isAol) {
            return true;
        }
        Boolean ldapPoisoningProtection = settings.getLdapPoisoningProtection();
        return ldapPoisoningProtection == null ? true : ldapPoisoningProtection;
    }
}
