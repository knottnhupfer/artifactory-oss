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

package org.artifactory.metrics.providers.features;

import org.artifactory.api.callhome.FeatureGroup;

/**
 * This is an interface that should be implemented by any class that represent a feature
 * (e.g. repositories feature, security feature etc..)
 *
 * @author Shay Bagants
 */
public interface CallHomeFeature {

    FeatureGroup getFeature();

    /**
     * Optionally clears any transient data kept in memory that can be discarded after the execution of a call home cycle.
     */
    default void clearData() {

    }

}
