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

package org.artifactory.addon.pypi;

import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * @author Yoav Luft
 */
public interface PypiAddon extends Addon {

    String PROPERTIES_PREFIX = "pypi";
    String NAME_PROPERTY = PROPERTIES_PREFIX + ".name";
    String VERSION_PROPERTY = PROPERTIES_PREFIX + ".version";

    default void reindex(LocalRepoDescriptor descriptor, boolean async) {
    }

    default PypiPkgMetadata getPypiMetadata(RepoPath packagePath) {
        return null;
    }

    default boolean isPypiFile(RepoPath repoPath) {
        return false;
    }

}
