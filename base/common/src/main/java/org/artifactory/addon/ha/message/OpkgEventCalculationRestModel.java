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

import org.artifactory.addon.opkg.OpkgCalculationEvent;

/**
 * Model for propagation, similar to OpkgCalculationEvent, but should only be used for propagation and should not be
 * used as a part of the actual indexing process
 *
 * @author Shay Bagants
 */
public class OpkgEventCalculationRestModel extends DpkgCalculationEventRestModel {

    private String path;

    public OpkgEventCalculationRestModel() {
        super();
    }

    public OpkgEventCalculationRestModel(String repoKey, String passphrase, long timestamp, boolean isIndexEntireRepo) {
        super(repoKey, passphrase, timestamp, isIndexEntireRepo);
    }

    public OpkgEventCalculationRestModel(OpkgCalculationEvent event) {
        super(event.getRepoKey(), event.getPassphrase(), event.getTimestamp(), event.isIndexEntireRepo());
        this.path = event.getPath();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
