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

package org.artifactory.aql.result.rows;

import com.google.common.collect.Sets;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;

import java.util.Date;
import java.util.Set;

import static org.artifactory.checksum.ChecksumInfo.TRUSTED_FILE_MARKER;

/**
 * Item row that's fully mappable to a {@link org.artifactory.fs.FileInfo}
 *
 * @author Dan Feldman
 */
@Data
public class FileInfoItemRow implements RowResult {

    protected long itemId;
    protected String repo;
    protected String path;
    protected String name;
    protected long size;

    protected long modified;
    protected long created;
    protected long updated;

    protected String createdBy;
    protected String modifiedBy;

    protected String originalMd5;
    protected String actualMd5;
    protected String originalSha1;
    protected String actualSha1;
    protected String sha2;

    protected String propKey;
    protected String propVal;

    @Override
    public void put(DomainSensitiveField field, Object value) {
        if (field.getField() == AqlPhysicalFieldEnum.itemId) {
            itemId = (Long) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemRepo) {
            repo = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemPath) {
            path = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemName) {
            name = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemSize) {
            size = (long) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemModified) {
            modified = ((Date) value).getTime();
        } else if (field.getField() == AqlPhysicalFieldEnum.itemCreated) {
            created = ((Date) value).getTime();
        } else if (field.getField() == AqlPhysicalFieldEnum.itemUpdated) {
            updated = ((Date) value).getTime();
        } else if (field.getField() == AqlPhysicalFieldEnum.itemCreatedBy) {
            createdBy = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemModifiedBy) {
            modifiedBy = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemOriginalMd5) {
            originalMd5 = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemActualMd5) {
            actualMd5 = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemOriginalSha1) {
            originalSha1 = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemActualSha1) {
            actualSha1 = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.itemSha2) {
            sha2 = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.propertyKey) {
            propKey = (String) value;
        } else if (field.getField() == AqlPhysicalFieldEnum.propertyValue) {
            propVal = (String) value;
        }
    }

    @Override
    public Object get(DomainSensitiveField field) {
        return null;
    }

    public RepoPath getRepoPath() {
        if (StringUtils.equals(path, ".")) {
            return RepoPathFactory.create(repo, name);
        } else {
            return RepoPathFactory.create(repo, path + "/" + name);
        }
    }

    public FileInfo toFileInfo() {
        MutableFileInfo fileInfo = InfoFactoryHolder.get().createFileInfo(getRepoPath(), itemId);
        fileInfo.setSize(size);
        fileInfo.setLastModified(modified);
        fileInfo.setCreated(created);
        fileInfo.setLastUpdated(updated);
        fileInfo.setCreatedBy(createdBy);
        fileInfo.setModifiedBy(modifiedBy);
        Set<ChecksumInfo> checksums = Sets.newHashSet();
        checksums.add(new ChecksumInfo(ChecksumType.md5, originalMd5, actualMd5));
        checksums.add(new ChecksumInfo(ChecksumType.sha1, originalSha1, actualSha1));
        if (StringUtils.isNotBlank(sha2)) {
            checksums.add(new ChecksumInfo(ChecksumType.sha256, TRUSTED_FILE_MARKER, sha2));
        }
        fileInfo.setChecksums(checksums);
        return fileInfo;
    }
}
