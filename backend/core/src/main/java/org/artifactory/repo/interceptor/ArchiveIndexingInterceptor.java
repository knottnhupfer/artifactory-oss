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

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.ArchiveIndexer;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.ImportInterceptor;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.util.RepoPathUtils;

/**
 * Interceptor which handles archive indexing calculation upon creation
 *
 * @author Noam Tenne
 */
public class ArchiveIndexingInterceptor extends StorageInterceptorAdapter implements ImportInterceptor {

    /**
     * If the newly created item is a file, this method will mark it up for content indexing.
     *  @param fsItem       Newly created item
     * @param statusHolder StatusHolder
     * @param ctx
     */
    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder, InterceptorCreateContext ctx) {
        markArchiveForIndexing(fsItem);
    }

    @Override
    public void afterImport(VfsItem fsItem, MutableStatusHolder statusHolder) {
        markArchiveForIndexing(fsItem);
    }

    private void markArchiveForIndexing(VfsItem fsItem) {
        if (shouldIndexItem(fsItem)) {
            ArchiveIndexer archiveIndexer = ContextHelper.get().beanForType(ArchiveIndexer.class);
            archiveIndexer.markArchiveForIndexing(fsItem.getRepoPath());
        }
    }

    private boolean shouldIndexItem(VfsItem fsItem) {
        if (!fsItem.isFile()) {
            return false;
        }

        if (RepoPathUtils.isTrash(fsItem.getRepoPath())) {
            return false;
        }

        MimeType mimeType = NamingUtils.getMimeType(fsItem.getName());
        boolean supportsIndexing = mimeType.isArchive() && mimeType.isIndex();

        if (!supportsIndexing) {
            return false;
        }

        // Skip indexing on virtual repo
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        return repositoryService.virtualRepoDescriptorByKey(fsItem.getRepoKey()) == null;
    }
}
