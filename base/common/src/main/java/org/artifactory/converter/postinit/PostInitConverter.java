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

package org.artifactory.converter.postinit;

import org.artifactory.converter.ConverterWithPreconditionAdapter;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.artifactory.version.VersionComparator;

/**
 * @author nadav yogev
 * @author Dan Feldman
 */
public abstract class PostInitConverter implements ConverterWithPreconditionAdapter {

    private final VersionComparator comparator;

    public PostInitConverter(ArtifactoryVersion from, ArtifactoryVersion until) {
        this.comparator = new VersionComparator(from, until);
    }

    /**
     * The default shouldConvert logic is a bit special as we usually want post init conversion to only run if upgrading
     * from an impacted version.
     * For instance, the docker conversion fixes a bug that was introduced in 4.11.0 and solved in 4.12.1 so conversion
     * should run only when upgrading from versions that are between these 2.
     * (meaning that if an upgrade is 4.9 -> 4.13 conversion will not run in this case)
     *
     * Still this method may be overloaded by the implementing class to allow more flexibility over when to run.
     */
    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        return source.getVersion().after(comparator.getFrom())
                && source.getVersion().before(comparator.getUntil());
    }
}
