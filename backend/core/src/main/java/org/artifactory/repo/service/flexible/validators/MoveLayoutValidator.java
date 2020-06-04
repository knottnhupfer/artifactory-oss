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

package org.artifactory.repo.service.flexible.validators;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.service.flexible.MoveCopyItemInfo;
import org.artifactory.repo.service.flexible.context.MoveCopyContext;

/**
 * @author gidis
 */
public class MoveLayoutValidator implements MoveCopyValidator {
    private final LayoutsCoreAddon layoutsCoreAddon;

    public MoveLayoutValidator(AddonsManager addonsManager) {
        layoutsCoreAddon = addonsManager.addonByType(LayoutsCoreAddon.class);
    }

    @Override
    public boolean validate(MoveCopyItemInfo element, MoveMultiStatusHolder status, MoveCopyContext context) {
        // Check if cross layout move/copy
        LocalRepo sourceRepo = element.getSourceRrp().getRepo();
        LocalRepo targetRepo = element.getTargetRrp().getRepo();
        RepoLayout sourceLayout = sourceRepo.getDescriptor().getRepoLayout();
        RepoLayout targetLayout = targetRepo.getDescriptor().getRepoLayout();
        if (!layoutsCoreAddon.canCrossLayouts(sourceLayout, targetLayout)) {
            throw new IllegalArgumentException(String.format("Can't execute cross layout move, layouts (source %s and target %s)" +
                    "are not equals", sourceLayout, targetLayout));
        }
        return true;
    }

    @Override
    public boolean isInterested(MoveCopyItemInfo itemInfo, MoveCopyContext context) {
        return ! context.isSuppressLayouts();
    }
}
