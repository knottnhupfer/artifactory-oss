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

package org.artifactory.repo.cache.expirable;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.MavenNaming;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author Noam Y. Tenne
 */
@Component
public class MavenIndexChecker extends IndexChecker {

    @Override
    public boolean isExpirable(RepoType repoType, String repoKey, @Nonnull String path) {
        return repoType != null && repoType.isMavenGroup() && MavenNaming.isIndex(path);
    }

    @Override
    public boolean isLocalGenerated(@Nonnull RepoType repoType, @Nonnull String repoKey, @Nonnull String path) {
        return repoType.isMavenGroup() && MavenNaming.isIndex(path);
    }
}
