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

package org.artifactory.addon.debian.index;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.artifactory.addon.dpkgcommon.DpkgCalculationEvent;
import org.artifactory.fs.FileInfo;

import javax.annotation.Nullable;

import static org.artifactory.addon.debian.index.DebianIndexEventType.ALL;
import static org.artifactory.addon.debian.index.DebianIndexEventType.FORCED;

/**
 * @author Gidi Shabat
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class DebianCalculationEvent extends DpkgCalculationEvent {

    String distribution;
    String component;
    String architecture;
    FileInfo artifact;
    DebianIndexEventType eventType;

    public DebianCalculationEvent(String repoKey, @Nullable String passphrase) {
        super(repoKey, passphrase);
        this.distribution = "";
        this.component = null;
        this.architecture = null;
        //ok to have null artifact here, used only by reindex of trivial repo
        this.artifact = null;
        this.eventType = ALL;
    }

    /**
     * Constructor used by the interceptor, no passphrase is passed on deployments
     */
    public DebianCalculationEvent(String repoKey, String distribution, String component, String architecture,
            @Nullable FileInfo artifact, DebianIndexEventType eventType) {
        super(repoKey, null); //The service inserts passphrase directly to the indexer
        this.distribution = distribution;
        this.component = component;
        this.architecture = architecture;
        this.artifact = artifact;
        this.eventType = eventType;
    }

    /**
     * Constructor for auto layout entire-repo events where rest calls may pass the passphrase
     */
    public DebianCalculationEvent(String repoKey, String distribution, String component, String architecture, @Nullable String passphrase) {
        super(repoKey, passphrase);
        this.distribution = distribution;
        this.component = component;
        this.architecture = architecture;
        this.artifact = null;
        this.eventType = DebianIndexEventType.ALL;
    }

    public static DebianCalculationEvent forced(String repoKey, String distribution, String component, String architecture) {
        return new DebianCalculationEvent(repoKey, distribution, component, architecture, null, FORCED);
    }

    public String coordinates() {
        return distribution + "/" + component + "/" + architecture;
    }
}
