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

package org.artifactory.sapi.interceptor;

import org.artifactory.common.MutableStatusHolder;
import org.artifactory.interceptor.Interceptor;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.sapi.interceptor.context.InterceptorMoveCopyContext;

/**
 * @author yoav
 */
public interface StorageInterceptor extends Interceptor {

    void beforeCreate(VfsItem fsItem, MutableStatusHolder statusHolder);

    void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder, InterceptorCreateContext ctx);

    void assertDeletePathAllowed(RepoPath repoPath, MutableStatusHolder statusHolder);

    void assertDeleteRepoAllowed(String repoKey, MutableStatusHolder statusHolder);

    void beforeDelete(VfsItem fsItem, MutableStatusHolder statusHolder, boolean moved);

    void afterDelete(VfsItem fsItem, MutableStatusHolder statusHolder, DeleteContext ctx);

    boolean isCopyOrMoveAllowed(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder statusHolder);

    void beforeMove(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder statusHolder, Properties properties);

    void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder, Properties properties, InterceptorMoveCopyContext ctx);

    void beforeCopy(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder statusHolder, Properties properties);

    void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder, Properties properties, InterceptorMoveCopyContext ctx);

    void beforePropertyCreate(VfsItem fsItem, MutableStatusHolder statusHolder, String name, String... values);

    void afterPropertyCreate(VfsItem fsItem, MutableStatusHolder statusHolder, String name, String... values);

    void beforePropertyDelete(VfsItem fsItem, MutableStatusHolder statusHolder, String name);

    void afterPropertyDelete(VfsItem fsItem, MutableStatusHolder statusHolder, String name);
}