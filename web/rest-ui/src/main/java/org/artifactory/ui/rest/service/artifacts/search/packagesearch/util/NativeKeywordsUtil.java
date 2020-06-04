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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.util;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeConstants.EMPTY_KEYWORD;

/**
 * @author Inbar Tal
 */
public class NativeKeywordsUtil {

    public static Set<String> getKeywordsAsSet(String keywords) {
        if (isEmpty(keywords) || EMPTY_KEYWORD.equals(keywords)) {
            return Sets.newHashSet();
        }
        String fixedKeywords = keywords.replaceAll("[\\[\\]\"]", "");
        return Arrays.stream(fixedKeywords.split(","))
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .collect(Collectors.toSet());
    }
}
