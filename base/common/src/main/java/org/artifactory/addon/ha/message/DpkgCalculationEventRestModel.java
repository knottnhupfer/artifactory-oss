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

package org.artifactory.addon.ha.message;

/**
 * Model for propagation, similar to DpkgCalculationEvent, but should only be used for propagation and should not
 * be passed to the actual index
 *
 * @author Shay Bagants
 */
public abstract class DpkgCalculationEventRestModel {

    private String repoKey;
    private String passphrase = null;
    private long timestamp;
    private boolean isIndexEntireRepo;

    public DpkgCalculationEventRestModel() {
    }

    public DpkgCalculationEventRestModel(String repoKey, String passphrase, long timestamp, boolean isIndexEntireRepo) {
        this.repoKey = repoKey;
        this.passphrase = passphrase;
        this.timestamp = timestamp;
        this.isIndexEntireRepo = isIndexEntireRepo;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
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

    public void setIndexEntireRepo(boolean indexEntireRepo) {
        isIndexEntireRepo = indexEntireRepo;
    }
}
