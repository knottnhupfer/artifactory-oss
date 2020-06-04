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

package org.artifactory.repo.interceptor;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.composer.ComposerAddon;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.sapi.interceptor.context.InterceptorMoveCopyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Intercept specific Composer storage events
 *
 * @author Shay Bagants
 */
public class ComposerInterceptor extends StorageInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(ComposerInterceptor.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder, InterceptorCreateContext ctx) {
        if (shouldTakeAction(fsItem)) {
            log.debug("Creation of artifact '{}' is a potential for package re-index", fsItem.getPath());
            addonsManager.addonByType(ComposerAddon.class).handlePackageDeployment(fsItem.getRepoPath());
        }
    }

    @Override
    public void afterDelete(VfsItem fsItem, MutableStatusHolder statusHolder, DeleteContext ctx) {
        if (shouldTakeAction(fsItem)) {
            log.debug("Deletion of artifact '{}' is a potential for package re-index", fsItem.getPath());
            addonsManager.addonByType(ComposerAddon.class).handlePackageDeletion(fsItem.getRepoPath());
        }
    }

    @Override
    public void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder, Properties properties, InterceptorMoveCopyContext ctx) {
        if (shouldTakeAction(sourceItem)) {
            log.trace("Deleting artifact '{}' as a part of move operation", sourceItem.getPath());
            addonsManager.addonByType(ComposerAddon.class).handlePackageDeletion(sourceItem.getRepoPath());
        }

        if (shouldTakeAction(targetItem)) {
            log.trace("Creating artifact '{}' as a part of move operation", targetItem.getPath());
            addonsManager.addonByType(ComposerAddon.class).handlePackageDeployment(targetItem.getRepoPath());
        }
    }

    @Override
    public void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder, Properties properties, InterceptorMoveCopyContext ctx) {
        if (shouldTakeAction(targetItem)) {
            log.trace("Creating artifact '{}' as a part of copy operation", targetItem.getPath());
            addonsManager.addonByType(ComposerAddon.class).handlePackageDeployment(targetItem.getRepoPath());
        }
    }

    /**
     * Some packages has no 'version' attribute in their composer.json file. In this case, best practice is to to deploy
     * the package with the 'composer.version=x.y.z' matrix param, but in some cases, users might forget and add the
     * property manually after the deployment. This method triggers the package index calculation for a new
     * 'composer.version' property. To avoid loop, we are checking if we should re-tag the artifact with property during
     * the index process or not.
     */
    @Override
    public void afterPropertyCreate(VfsItem fsItem, MutableStatusHolder statusHolder, String key, String... values) {
        if (shouldTakeAction(fsItem) && key.equals("composer.version")) {
            addonsManager.addonByType(ComposerAddon.class).handlePackageDeployment(fsItem.getRepoPath());
        }
    }

    private boolean shouldTakeAction(VfsItem item) {
        log.debug("Checking if '{}' is a potential for indexing", item.getPath());
        if (item.isFile()) {
            RepoPath repoPath = item.getRepoPath();
            RepoDescriptor repoDescriptor = repositoryService.localRepoDescriptorByKey(repoPath.getRepoKey());
            if (((repoDescriptor != null) && repoDescriptor.getType().equals(RepoType.Composer))) {
                return addonsManager.addonByType(ComposerAddon.class).isComposerSupportedExtension(repoPath.getName());
            }
        }
        return false;
    }
}