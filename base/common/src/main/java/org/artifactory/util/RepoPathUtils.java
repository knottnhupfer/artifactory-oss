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

package org.artifactory.util;

import org.apache.commons.lang.StringUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.trash.TrashService;
import org.jfrog.client.util.PathUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Utility class for {@link org.artifactory.repo.RepoPath}.
 *
 * @author Yossi Shaul
 */
public abstract class RepoPathUtils {

    /**
     * @param degree The degree of the ancestor (1 - parent, 2 - grandparent, etc)
     * @return Returns the n-th ancestor of this repo path. Null if doesn't exist.
     */
    @Nullable
    public static RepoPath getAncestor(RepoPath repoPath, int degree) {
        RepoPath result = repoPath.getParent();   // first ancestor
        for (int i = degree - 1; i > 0 && result != null; i--) {
            result = result.getParent();
        }
        return result;
    }

    public static boolean isAncestorOf(@Nonnull RepoPath ancestorRepoPath, @Nonnull RepoPath otherRepoPath) {
        if (otherRepoPath.getRepoKey().equals(ancestorRepoPath.getRepoKey())) {
            RepoPath otherPathParent = otherRepoPath.getParent();
            if (otherPathParent != null) {
                String ancestorPath = ancestorRepoPath.getPath();
                String parentPath = otherPathParent.getPath();
                if (StringUtils.isBlank(ancestorPath)) {
                    return true;
                }
                if (StringUtils.isNotBlank(parentPath)) {
                    parentPath = PathUtils.trimTrailingSlashes(parentPath);
                    ancestorPath = PathUtils.trimTrailingSlashes(ancestorPath);
                    if (StringUtils.isNotBlank(parentPath) && StringUtils.isNotBlank(ancestorPath) && parentPath.equals(ancestorPath)) {
                        return true;
                    } else {
                        return isAncestorOf(ancestorRepoPath, otherPathParent);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates repo path representing the root repository path (i.e. path is empty).
     *
     * @param repoKey The repository key
     * @return Repository root repo path
     */
    public static RepoPath repoRootPath(String repoKey) {
        return RepoPathFactory.create(repoKey, "");
    }

    /**
     * @param repoPath The repo path to check
     * @return True if the repo path represents a path in the trash can
     */
    public static boolean isTrash(RepoPath repoPath) {
        return repoPath != null && isTrash(repoPath.getRepoKey());
    }

    /**
     * @param repoKey The repo key to check
     * @return True if the repo key represents the repo key of the trash can
     */
    public static boolean isTrash(String repoKey) {
        return TrashService.TRASH_KEY.equals(repoKey);
    }

    /**
     * @return a new repo path pointing to the same path of the input path that represents a folder path
     * (name ends with slash). Same repo path is returned if already a folder repo path.
     */
    public static RepoPath toFolderRepoPath(@Nonnull RepoPath repoPath) {
        Objects.requireNonNull(repoPath, "RepoPath cannot be null");
        if (repoPath.isFolder()) {
            return repoPath;
        }
        return RepoPathFactory.create(repoPath.getRepoKey(), repoPath.getPath() + "/");
    }

    /**
     * @return a new repo path pointing to the same path of the input path that represents a file path
     * (name doesn't end with slash). Same repo path is returned if already a file repo path.
     */
    public static RepoPath toFileRepoPath(@Nonnull RepoPath repoPath) {
        Objects.requireNonNull(repoPath, "RepoPath cannot be null");
        if (repoPath.isFile()) {
            return repoPath;
        }
        return RepoPathFactory.create(repoPath.getRepoKey(), repoPath.getPath());
    }
}
