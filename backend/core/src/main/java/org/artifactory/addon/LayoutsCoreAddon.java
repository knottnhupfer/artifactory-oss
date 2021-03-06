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

package org.artifactory.addon;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.service.mover.MoverConfig;
import org.artifactory.sapi.fs.VfsItem;

import javax.annotation.Nullable;

/**
 * The core interface of the repository layouts
 *
 * @author Noam Y. Tenne
 */
public interface LayoutsCoreAddon extends Addon {

    /**
     * Asserts that central config to be saved adheres to the limitations
     *
     * @param newDescriptor Descriptor to check
     */
    void assertLayoutConfigurationsBeforeSave(CentralConfigDescriptor newDescriptor);

    default boolean canCrossLayouts(RepoLayout source, RepoLayout target) {
        return false;
    }

    default void performCrossLayoutMoveOrCopy(MoveMultiStatusHolder status, MoverConfig moverConfig, LocalRepo sourceRepo,
                                              LocalRepo targetLocalRepo, VfsItem fsItemToMove) {
        throw new UnsupportedOperationException(
                "Cross layout move or copy operations require the Repository Layouts addon.");
    }

    default String translateArtifactPath(RepoLayout sourceRepoLayout, RepoLayout targetRepoLayout, String path) {
        return translateArtifactPath(sourceRepoLayout, targetRepoLayout, path, null);
    }

    default String translateArtifactPath(RepoLayout sourceRepoLayout, RepoLayout targetRepoLayout, String path,
                                         @Nullable BasicStatusHolder multiStatusHolder) {
        return path;
    }
}