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

package org.artifactory.addon.npm;

import org.artifactory.addon.Addon;
import org.artifactory.api.repo.Async;
import org.artifactory.api.repo.exception.MetadataVerificationException;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author Shay Yaakov
 */
public interface NpmAddon extends Addon {

    default void addNpmPackage(FileInfo info, Set<String> deletedTags) {
        //noop
    }

    @Async(delayUntilAfterCommit = true)
    default void handleAddAfterCommit(FileInfo info) {
        //noop
    }

    default void removeNpmPackage(FileInfo info,  Set<String> deletedTags) {
        //noop
    }

    @Async
    default void reindexAsync(RepoPath repoPath) {
        //noop
    }

    @Nullable
    default NpmMetadataInfo getNpmMetaDataInfo(RepoPath repoPath) {
        return null;
    }

    default void validateNpmPackageJson(VfsItem fsItem, MutableStatusHolder statusHolder) throws MetadataVerificationException {
        //noop
    }

    default String getReadme(String repoPath) {
        return "";
    }

    default void handleTags(FileInfo info, Set<String> deletedTagsProps) {
        //noop
    }
}