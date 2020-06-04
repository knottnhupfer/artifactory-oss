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

package org.artifactory.ui.utils;

import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.fs.ItemInfo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yoav Luft
 * @author Yossi Shaul
 */
public class BrowseUtils {

    /**
     * @return A filtered list without the checksum items (md5, sha1 abd sha256).
     */
    public static List<BaseBrowsableItem> filterChecksums(Collection<BaseBrowsableItem> items) {
        List<BaseBrowsableItem> filtered = items.stream().filter(i -> i != null && i.getName() != null &&
                !i.getName().endsWith(".sha1") && !i.getName().endsWith(".md5") && !i.getName().endsWith(".sha256"))
                .collect(Collectors.toList());
        return filtered;
    }

    public static List<ItemInfo> filterItemInfoChecksums(Collection<ItemInfo> items) {
        List<ItemInfo> filtered = items.stream().filter(i -> i != null && i.getName() != null &&
                !i.getName().endsWith(".sha1") && !i.getName().endsWith(".md5") && !i.getName().endsWith(".sha256"))
                .collect(Collectors.toList());
        return filtered;
    }
}
