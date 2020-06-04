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

package org.artifactory.aql;

import com.google.common.collect.Sets;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.*;
import org.artifactory.repo.RepoPath;

import java.util.Date;
import java.util.Set;
import java.util.function.Function;

import static org.artifactory.checksum.ChecksumInfo.TRUSTED_FILE_MARKER;

/**
 * Converter from Aql entities to other data objects.
 *
 * @author Yossi Shaul
 */
public abstract class AqlConverts {

    public static final Function<AqlItem, FolderInfo> toFolderInfo = aqlItem -> {
        RepoPath repoPath = AqlUtils.fromAql((AqlBaseFullRowImpl) aqlItem);
        MutableFolderInfo folderInfo = InfoFactoryHolder.get().createFolderInfo(repoPath);
        folderInfo.setCreated(aqlItem.getCreated().getTime());
        folderInfo.setLastUpdated(aqlItem.getUpdated().getTime());
        folderInfo.setCreatedBy(aqlItem.getCreatedBy());
        Date modified = aqlItem.getModified();
        if (modified != null) {
            folderInfo.setLastModified(modified.getTime());
        }
        folderInfo.setModifiedBy(aqlItem.getModifiedBy());
        return folderInfo;
    };

    public static final Function<AqlItem, FileInfo> toFileInfo = aqlItem -> {
        RepoPath repoPath = AqlUtils.fromAql((AqlBaseFullRowImpl) aqlItem);
        Long nodeId = aqlItem.getNodeId();
        MutableFileInfo fileInfo;
        //node id might be null if query did not originate in items domain and did not specifically include it.
        if (nodeId != null) {
            fileInfo = InfoFactoryHolder.get().createFileInfo(repoPath, nodeId);
        } else {
            fileInfo = InfoFactoryHolder.get().createFileInfo(repoPath);
        }
        fileInfo.setSize(aqlItem.getSize());
        fileInfo.setCreated(aqlItem.getCreated().getTime());
        fileInfo.setLastUpdated(aqlItem.getUpdated().getTime());
        fileInfo.setCreatedBy(aqlItem.getCreatedBy());
        Date modified = aqlItem.getModified();
        if (modified != null) {
            fileInfo.setLastModified(modified.getTime());
        }
        fileInfo.setModifiedBy(aqlItem.getModifiedBy());
        Set<ChecksumInfo> checksums = Sets.newHashSet();
        checksums.add(new ChecksumInfo(ChecksumType.md5, aqlItem.getOriginalMd5(), aqlItem.getActualMd5()));
        checksums.add(new ChecksumInfo(ChecksumType.sha1, aqlItem.getOriginalSha1(), aqlItem.getActualSha1()));
        checksums.add(new ChecksumInfo(ChecksumType.sha256, TRUSTED_FILE_MARKER, aqlItem.getSha2()));
        fileInfo.setChecksums(checksums);
        return fileInfo;
    };

    public static final Function<AqlItem, String> toItemName = AqlItem::getName;

    public static final Function<AqlItem, FileInfo> toMinimalFileInfo = aqlItem -> {
        RepoPath repoPath = AqlUtils.fromAql((AqlBaseFullRowImpl) aqlItem);
        return InfoFactoryHolder.get().createFileInfo(repoPath);
    };

    public static final Function<AqlItem, ItemInfo> toItemInfo = aqlItem -> {
        if (AqlItemTypeEnum.folder == aqlItem.getType()) {
            return toFolderInfo.apply(aqlItem);
        } else {
            return toFileInfo.apply(aqlItem);
        }
    };

    public static final Function<AqlItem, RepoPath> toRepoPath =
            aqlItem -> AqlUtils.fromAql((AqlBaseFullRowImpl) aqlItem);
}
