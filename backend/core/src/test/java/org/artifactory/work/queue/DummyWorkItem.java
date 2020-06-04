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

package org.artifactory.work.queue;

import org.artifactory.api.repo.WorkItem;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Shay Bagants
 */
public class DummyWorkItem extends WorkItem {

    private boolean processed = false;
    private String uniqueKey;

    public DummyWorkItem(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public void process() {
        processed = true;
    }

    public boolean isProcessed() {
        return processed;
    }

    @Nonnull
    @Override
    public String getUniqueKey() {
        return uniqueKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DummyWorkItem)) {
            return false;
        }
        DummyWorkItem that = (DummyWorkItem) o;
        return processed == that.processed &&
                Objects.equals(uniqueKey, that.uniqueKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processed, uniqueKey);
    }
}
