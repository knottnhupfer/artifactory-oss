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

package org.artifactory.addon.opkg;


import org.artifactory.addon.dpkgcommon.DpkgCalculationEvent;

import javax.annotation.Nullable;

/**
 * @author Noam Y. Tenne
 * @author Dan Feldman
 */
public class OpkgCalculationEvent extends DpkgCalculationEvent implements Comparable<OpkgCalculationEvent> {

    private final String path;
    private final boolean isIndexEntireRepo;
    protected long timestamp;

    OpkgCalculationEvent(String repoKey, String path, @Nullable String passphrase, boolean isIndexEntireRepo) {
        super(repoKey, passphrase);
        this.isIndexEntireRepo = isIndexEntireRepo;
        this.timestamp = System.currentTimeMillis();
        this.path = path;
    }

    public OpkgCalculationEvent(String repoKey, String passphrase, long timestamp, boolean isIndexEntireRepo, String path) {
        super(repoKey, passphrase);
        this.isIndexEntireRepo = isIndexEntireRepo;
        this.timestamp = timestamp;
        this.path = path;
    }

    static OpkgCalculationEvent indexEntireRepoEvent(String repoKey, @Nullable String passphrase) {
        return new OpkgCalculationEvent(repoKey, "", passphrase, true);
    }

    public String getPath() {
        return path;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isIndexEntireRepo() {
        return isIndexEntireRepo;
    }

    @Override
    public int compareTo(OpkgCalculationEvent o) {
        int i = repoKey.compareTo(o.repoKey);
        if (i != 0) {
            return i;
        }
        i = Boolean.compare(isIndexEntireRepo, o.isIndexEntireRepo);
        if(i != 0) {
            return i;
        }
        if (path != null) {
            i = path.compareTo(o.path);
            if (i != 0) {
                return i;
            }
        }
        return i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpkgCalculationEvent)) {
            return false;
        }
        OpkgCalculationEvent that = (OpkgCalculationEvent) o;
        if(!super.equals(o)) {
            return false;
        }
        if (isIndexEntireRepo != that.isIndexEntireRepo) {
            return false;
        }
        return !(path != null ? !path.equals(that.getPath()) : that.getPath() != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (isIndexEntireRepo ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OpkgCalculationEvent{" +
                "repoKey='" + repoKey + '\'' +
                "path='" + path + '\'' +
                "timestamp='" + timestamp + '\'' +
                '}';
    }
}
