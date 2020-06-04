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

package org.artifactory.factory.xstream;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.factory.common.AbstractInfoFactory;
import org.artifactory.fs.*;
import org.artifactory.md.MetadataInfo;
import org.artifactory.md.MutableMetadataInfo;
import org.artifactory.md.MutablePropertiesInfo;
import org.artifactory.md.PropertiesInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.*;
import org.artifactory.model.xstream.security.*;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.resource.MutableRepoResourceInfo;
import org.artifactory.resource.RepoResourceInfo;
import org.artifactory.security.*;
import org.artifactory.util.Tree;

import javax.annotation.Nullable;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * Date: 8/1/11
 * Time: 10:01 PM
 *
 * @author Fred Simon
 */
@SuppressWarnings({"UnusedDeclaration"})
public class XStreamInfoFactory extends AbstractInfoFactory {

    private XStream securityXStream;
    private XStream fileSystemXStream;

    @Override
    public RepoPath createRepoPathFromId(String repoPathId) {
        return InternalRepoPathFactory.createRepoPath(repoPathId);
    }

    @Override
    public RepoPath createRepoPath(String repoKey, String path) {
        return new RepoPathImpl(repoKey, path);
    }

    @Override
    public RepoPath createRepoPath(RepoPath parent, String relPath) {
        return new RepoPathImpl(parent, relPath);
    }

    @Override
    public RepoPath createRepoPath(String repoKey, String path, boolean folder) {
        return new RepoPathImpl(repoKey, path, folder);
    }

    @Override
    public MutableRepoResourceInfo copyRepoResource(RepoResourceInfo repoResourceInfo) {
        if (repoResourceInfo == null) {
            return null;
        }
        if (repoResourceInfo instanceof InternalFileInfo) {
            return new FileInfoImpl((InternalFileInfo) repoResourceInfo);
        } else if (repoResourceInfo instanceof MetadataInfo) {
            return new MetadataInfoImpl((MetadataInfo) repoResourceInfo);
        } else if (repoResourceInfo instanceof ZipEntryResourceInfo) {
            throw new IllegalArgumentException(
                    "Cannot copy " + repoResourceInfo + " since zip entries cannot be modified!");
        } else {
            throw new IllegalArgumentException(
                    "Cannot copy " + repoResourceInfo + " of class " + repoResourceInfo.getClass().getName());
        }
    }

    @Override
    public MutableItemInfo copyItemInfo(ItemInfo itemInfo) {
        if (itemInfo == null) {
            return null;
        }
        if (itemInfo instanceof InternalFileInfo) {
            return new FileInfoImpl((InternalFileInfo) itemInfo);
        } else if (itemInfo instanceof InternalFolderInfo) {
            return new FolderInfoImpl((InternalFolderInfo) itemInfo);
        } else if (itemInfo instanceof ZipEntryResourceInfo) {
            throw new IllegalArgumentException(
                    "Cannot copy " + itemInfo + " since zip entries cannot be modified!");
        } else if (itemInfo instanceof MutableItemInfo) {
            return (MutableItemInfo) itemInfo;
        } else {
            throw new IllegalArgumentException(
                    "Cannot copy " + itemInfo + " of class " + itemInfo.getClass().getName());
        }
    }

    @Override
    public MutableFileInfo createFileInfo(RepoPath repoPath) {
        return new FileInfoImpl(repoPath);
    }

    @Override
    public MutableFileInfo createFileInfo(RepoPath repoPath, long id) {
        return new FileInfoImpl(repoPath, id);
    }

    @Override
    public MutableFileInfo copyFileInfo(FileInfo fileInfo) {
        return new FileInfoImpl((InternalFileInfo) fileInfo);
    }

    @Override
    public MutableFolderInfo createFolderInfo(RepoPath repoPath) {
        return new FolderInfoImpl(repoPath);
    }

    @Override
    public MutableFolderInfo copyFolderInfo(FolderInfo folderInfo) {
        return new FolderInfoImpl((InternalFolderInfo) folderInfo);
    }

    @Override
    public MutablePropertiesInfo createProperties() {
        return new PropertiesImpl();
    }

    @Override
    public MutablePropertiesInfo copyProperties(PropertiesInfo copy) {
        return new PropertiesImpl(copy);
    }

    @Override
    public MutableRepoPermissionTarget createRepoPermissionTarget() {
        return new RepoPermissionTargetImpl();
    }

    @Override
    public MutableBuildPermissionTarget createBuildPermissionTarget() {
        return new BuildPermissionTargetImpl();
    }

    @Override
    public MutableReleaseBundlePermissionTarget createReleaseBundlePermissionTarget() {
        return new ReleaseBundlePermissionTargetImpl();
    }

    @Override
    public MutableRepoPermissionTarget copyRepoPermissionTarget(RepoPermissionTarget copy) {
        return new RepoPermissionTargetImpl(copy);
    }

    @Override
    public MutableReleaseBundlePermissionTarget copyReleaseBundlePermissionTarget(ReleaseBundlePermissionTarget copy) {
        return new ReleaseBundlePermissionTargetImpl(copy);
    }

    @Override
    public MutableUserInfo createUser() {
        return new UserImpl();
    }

    @Override
    public MutableUserInfo copyUser(UserInfo copy) {
        return new UserImpl(copy);
    }

    @Override
    public UserGroupInfo createUserGroup(String groupName) {
        return new UserGroupImpl(groupName);
    }

    @Override
    public UserGroupInfo createUserGroup(String groupName, String realm) {
        return new UserGroupImpl(groupName, realm);
    }

    @Override
    public MutableGroupInfo createGroup() {
        return new GroupImpl();
    }

    @Override
    public MutableGroupInfo copyGroup(GroupInfo copy) {
        return new GroupImpl(copy);
    }

    @Override
    public MutableRepoAcl createRepoAcl() {
        return new MutableRepoAclImpl();
    }

    @Override
    public MutableRepoAcl copyRepoAcl(RepoAcl copy) {
        return new MutableRepoAclImpl(copy);
    }

    @Override
    public MutableBuildAcl copyBuildAcl(BuildAcl copy) {
        return new MutableBuildAclImpl(copy);
    }

    @Override
    public MutableReleaseBundleAcl copyReleaseBundleAcl(ReleaseBundleAcl copy) {
        return new MutableReleaseBundleAclImpl(copy);
    }

    @Override
    public MutableAceInfo createAce() {
        return new AceImpl();
    }

    @Override
    public MutableAceInfo copyAce(AceInfo copy) {
        return new AceImpl(copy);
    }

    @Override
    public SecurityInfo createSecurityInfo(List<UserInfo> users, List<GroupInfo> groups, List<RepoAcl> repoAcls,
            @Nullable List<BuildAcl> buildAcls, List<ReleaseBundleAcl> releaseBundleAcls) {
        return new SecurityDataImpl(users, groups, repoAcls, buildAcls, releaseBundleAcls);
    }

    @Override
    public XStream getSecurityXStream() {
        if (securityXStream == null) {
            securityXStream = XStreamFactory.create(SecurityDataImpl.class,
                    RepoPermissionTargetImpl.class,
                    BuildPermissionTargetImpl.class,
                    MutableRepoAclImpl.class,
                    MutableBuildAclImpl.class,
                    AceImpl.class,
                    UserImpl.class,
                    UserProperty.class,
                    UserGroupImpl.class,
                    GroupImpl.class
            );
        }
        return securityXStream;
    }

    @Override
    public XStream getFileSystemXStream() {
        if (fileSystemXStream == null) {
            fileSystemXStream = XStreamFactory.create(FileInfoImpl.class,
                    FileAdditionalInfo.class,
                    FolderInfoImpl.class,
                    FolderAdditionalInfo.class,
                    StatsImpl.class,
                    PropertiesImpl.class,
                    WatchersImpl.class,
                    WatcherImpl.class);
        }
        return fileSystemXStream;
    }

    @Override
    public MutableStatsInfo createStats() {
        return new StatsImpl();
    }

    @Override
    public MutableStatsInfo copyStats(StatsInfo copy) {
        return new StatsImpl(copy);
    }

    @Override
    public MutableWatchersInfo createWatchers() {
        return new WatchersImpl();
    }

    @Override
    public MutableWatcherInfo createWatcher(String watcherUsername, long watchCreationDate) {
        return new WatcherImpl(watcherUsername, watchCreationDate);
    }

    @Override
    public MetadataEntryInfo createMetadataEntry(String metadataName, String xmlContent) {
        return new MetadataEntry(metadataName, xmlContent);
    }

    @Override
    public MutableWatchersInfo copyWatchers(WatchersInfo copy) {
        return new WatchersImpl(copy);
    }

    @Override
    public Tree<ZipEntryInfo> createZipEntriesTree() {
        return new ZipEntriesTree();
    }

    @Override
    public ZipEntryInfo createZipEntry(ZipEntry... zipEntries) {
        return new ZipEntryImpl(zipEntries);
    }

    @Override
    public ZipEntryInfo createArchiveEntry(ArchiveEntry... archiveEntries) {
        return new ArchiveEntryImpl(archiveEntries);
    }

    @Override
    public ZipEntryResourceInfo createZipEntryResource(FileInfo info, ZipEntryInfo zipEntryInfo, long actualSize,
            ChecksumsInfo checksumsInfo) {
        return new ZipEntryResourceImpl(info, zipEntryInfo, actualSize, checksumsInfo);
    }

    @Override
    public MutableMetadataInfo createMetadata(RepoPath repoPath) {
        return new MetadataInfoImpl(repoPath);
    }
}
