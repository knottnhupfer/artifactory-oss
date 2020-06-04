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

package org.artifactory.api.build;

import org.artifactory.common.StatusEntry;

import java.util.Comparator;

/**
 * @author Alexei Vainshtein
 */
public class StatusEntryComparators {

    public static Comparator<StatusEntry> sortByStatusCodeImportency() {
        return new BasicStatusHolderByStatusCodeComparator();
    }
    /**
     * Compares builds based on the status code from status holders.
     */
    private static class BasicStatusHolderByStatusCodeComparator implements Comparator<StatusEntry> {

        @Override
        public int compare(StatusEntry statusEntry1, StatusEntry statusEntry2) {
            return Integer.compare(statusEntry1.getStatusCode(), statusEntry2.getStatusCode());
        }
    }
}
