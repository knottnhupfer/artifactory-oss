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

package org.artifactory.api.repo;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.config.ImportableExportable;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.VersionUnit;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.repo.storage.FolderSummeryInfo;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.api.search.VersionSearchResults;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.common.StatusHolder;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.fs.*;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.Lock;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.util.Tree;
import org.jfrog.access.proto.generated.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: freds Date: Jul 21, 2008 Time: 8:07:50 PM
 */
public interface RepositoryService extends ImportableExportable {

    String METADATA_FOLDER = ".artifactory-metadata";

    List<LocalRepoDescriptor> getLocalRepoDescriptors();

    List<LocalRepoDescriptor> getLocalRepoDescriptorsIncludingBuildInfo();

    List<LocalCacheRepoDescriptor> getCachedRepoDescriptors();

    List<VirtualRepoDescriptor> getVirtualRepoDescriptors();

    List<DistributionRepoDescriptor> getDistributionRepoDescriptors();

    List<ReleaseBundlesRepoDescriptor> getReleaseBundlesRepoDescriptors();

    LocalRepoDescriptor getSupportBundlesRepoDescriptors();

    /**
     * @return Gets a list of local and remote repositories, no caches
     */
    List<RealRepoDescriptor> getLocalAndRemoteRepoDescriptors();

    List<LocalRepoDescriptor> getLocalAndCachedRepoDescriptors();

    List<RemoteRepoDescriptor> getRemoteRepoDescriptors();

    RepoDescriptor repoDescriptorByKey(String key);

    /**
     * Gets a local or cache repository by key.
     *
     * @param repoKey The key for a cache can either be the remote repository one or the cache one(ends with "-cache")
     */
    LocalRepoDescriptor localOrCachedRepoDescriptorByKey(String repoKey);

    LocalRepoDescriptor localCachedOrDistributionRepoDescriptorByKey(String repoKey);

    LocalRepoDescriptor localRepoDescriptorByKey(String key);

    RemoteRepoDescriptor remoteRepoDescriptorByKey(String repoKey);

    VirtualRepoDescriptor virtualRepoDescriptorByKey(String repoKey);

    boolean isVirtualRepoExist(String repoKey);

    DistributionRepoDescriptor distributionRepoDescriptorByKey(String key);

    ReleaseBundlesRepoDescriptor releaseBundlesRepoDescriptorByKey(String key);

    /**
     * Internal - get the raw content directly
     */
    @Nonnull
    String getStringContent(FileInfo fileInfo);

    /**
     * Internal - get the raw content directly. Returns empty string if content not found.
     */
    @Nonnull
    String getStringContent(RepoPath repoPath);

    /**
     * Internal - get the raw content directly
     * NOTICE! this method skips all possible checks (permissions incl./excl. xray etc.) -- only call this if you
     * REALLY know what you're doing!
     *
     * @return The ResourceStreamHandle for an existing file or a NullResourceStreamHandle for a non-exiting file
     */
    ResourceStreamHandle getResourceStreamHandle(RepoPath repoPath);

    /**
     * @param archivePath     Repository path of the archive file
     * @param sourceEntryPath File path inside the archive
     * @return The source entry details (including content if found)
     * @throws IOException On failure reading to archive or the sources file (will not fail if not found)
     */
    ArchiveFileContent getArchiveFileContent(RepoPath archivePath, String sourceEntryPath) throws IOException;

    /**
     * Import all the repositories under the passed folder which matches local or cached repository declared in the
     * configuration. Having empty directory for each repository is allowed and not an error. Nothing will be imported
     * for those.
     */
    void importAll(ImportSettingsImpl settings);

    /**
     * Import the artifacts under the folder passed directly in the repository named "repoKey". If no repository with
     * this repo key exists or if the folder passed is empty, the status will be set to error.
     */
    void importRepo(String repoKey, ImportSettingsImpl settings);

    /**
     * @param repoPath Repository path of the item (Repo must be local(or cache otherwise IllegalArgumentException) )
     * @return Folder or file info. Throws exception if the path doesn't exist.
     */
    @Nonnull
    ItemInfo getItemInfo(RepoPath repoPath) throws ItemNotFoundRuntimeException;

    /**
     * @param repoPath Repository path of the file (Repo must be local(or cache otherwise IllegalArgumentException) )
     * @return The file info. Throws exception if the path doesn't exist or it doesn't point to a file.
     */
    @Nonnull
    FileInfo getFileInfo(RepoPath repoPath) throws ItemNotFoundRuntimeException, FileExpectedException;

    /**
     * @param repoPath Repository path of the folder (Repo must be local(or cache otherwise IllegalArgumentException) )
     * @return The folder info. Throws exception if the path doesn't exist or it doesn't point to a folder.
     */
    @Nonnull
    FolderInfo getFolderInfo(RepoPath repoPath) throws ItemNotFoundRuntimeException, FolderExpectedException;

    /**
     * Deletes the file or folder at the give repo path. Deletion is recursive if the repo path points to folder.
     *
     * @param repoPath Repository path to a file or folder
     * @return Status of the deletion
     */
    @Request(aggregateEventsByTimeWindow = true)
    @Lock
    BasicStatusHolder undeploy(RepoPath repoPath);

    @Request(aggregateEventsByTimeWindow = true)
    @Lock
    BasicStatusHolder undeploy(RepoPath repoPath, boolean calcMavenMetadata);

    @Request(aggregateEventsByTimeWindow = true)
    @Lock
    BasicStatusHolder undeploy(RepoPath repoPath, boolean calcMavenMetadata, boolean pruneEmptyFolders);

    @Request(aggregateEventsByTimeWindow = true)
    @Lock
    BasicStatusHolder undeploy(RepoPath repoPath, boolean pruneEmptyFolders, DeleteContext deleteContext);

    @Request(aggregateEventsByTimeWindow = true)
    StatusHolder undeployMultiTransaction(RepoPath repoPath);

    @Request(aggregateEventsByTimeWindow = true)
    StatusHolder undeployVersionUnits(Set<VersionUnit> versionUnits);

    /**
     * Moves repository path (pointing to a folder) to another absolute target. The move will only move paths the user
     * has permissions to move and paths that are accepted by the target repository. Maven metadata will be recalculated
     * for both the source and target folders.
     *
     * @param targetPath      The target local non-cached repository to move the path to.
     * @param dryRun          If true the method will just report the expected result but will not move any file
     * @param suppressLayouts If true, path translation across different layouts should be suppressed.
     * @param failFast        If true, the operation should fail upon encountering an error.
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    @Lock
    MoveMultiStatusHolder move(RepoPath fromRepoPath, RepoPath targetPath, boolean dryRun, boolean suppressLayouts, boolean failFast);

    /**
     * Moves set of paths to another local repository.
     * <p/>
     * This method will only move paths the user has permissions to move and paths that are accepted by the target
     * repository.
     * <p/>
     * Maven metadata will be recalculated for both the source and target folders after all items has been moved. If a
     * path already belongs to the target repository it will be skipped.
     * <p/>
     * This move method does not use Unix-style handling of existing nested folders, meaning that folder content might
     * be overwritten (i.e. when moving source path org/jfrog/1 to target path org/jfrog/1 the contents of source 1/
     * are moved into target 1/  - unlike normal unix behavior where such an operation will create org/jfrog/1/1)
     *
     * @param pathsToMove   Paths to move, each pointing to file or folder.
     * @param targetRepoKey Key of the target local non-cached repository to move the path to.
     * @param properties    Properties to attach on the target paths
     * @param dryRun        If true the method will just report the expected result but will not move any file  @return
     * @param failFast      True if the operation should abort upon the first occurring warning or error
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    @Lock
    MoveMultiStatusHolder move(Set<RepoPath> pathsToMove, String targetRepoKey, Properties properties, boolean dryRun, boolean failFast);

    /**
     * Same as above only that all {@param pathsToMove} are moved under {@param targetPath} with their name only.
     * For example:  source -> repo1/a/b/file.ext, repo1/c/d/file2.ext  target: repo2/x/y will result in:
     * repo2/x/y/file.ext and repo2/x/y/file2.ext
     *
     * This method is meant specifically to accept files only! sending folders will have undefined behavior.
     * Take care not to move same-name files or the transaction will blow up.
     */
    @Lock
    MoveMultiStatusHolder move(Set<RepoPath> pathsToMove, RepoPath targetPath, Properties properties, boolean dryRun, boolean failFast);


    MoveMultiStatusHolder move(RepoPath repoPath, String targetLocalRepoKey, String baseTargetPath,
            Properties addProps, List<String> removeProps, boolean dryRun, boolean failFast, int transactionSize);

    MoveMultiStatusHolder copy(RepoPath repoPath, String targetLocalRepoKey, String baseTargetPath,
            Properties addProps, List<String> removeProps, boolean dryRun, boolean failFast, int transactionSize);

    /**
     * Copies repository path to another absolute path. The copy will only copy paths the user has permissions to read
     * and paths that are accepted by the target repository. Maven metadata will be recalculated for both the source and
     * target folders. Metadata is also copied.
     *
     * @param fromRepoPath    Repository path to copy. This path must represent a folder in a local repository.
     * @param targetRepoPath  Path of the target local non-cached repository to copy the path to.
     * @param dryRun          If true the method will just report the expected result but will not copy any file
     * @param suppressLayouts If true, path translation across different layouts should be suppressed.
     * @param failFast        If true, the operation should fail upon encountering an error.
     * @param atomic          If true, copy will be done in a single transaction, else in multi transaction.
     * @param properties      properties to set
     * @param overrideProperties If true then properties given will override existing properties, else properties will be added to existing.
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    MoveMultiStatusHolder copy(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun, boolean suppressLayouts,
            boolean failFast, boolean atomic, Properties properties, boolean overrideProperties);

    /**
     * Copies repository path to another absolute path. Allows copying to a cache repository. The copy will only copy paths the user has permissions to read
     * and paths that are accepted by the target repository. Maven metadata will be recalculated for both the source and
     * target folders. Metadata is also copied.
     *
     * @param fromRepoPath    Repository path to copy. This path must represent a folder in a local repository.
     * @param targetRepoPath  Path of the target local non-cached repository to copy the path to.
     * @param dryRun          If true the method will just report the expected result but will not copy any file
     * @param suppressLayouts If true, path translation across different layouts should be suppressed.
     * @param failFast        If true, the operation should fail upon encountering an error.
     * @param atomic          If true, copy will be done in a single transaction, else in multi transaction.
     * @param properties      properties to set
     * @param overrideProperties If true then properties given will override existing properties, else properties will be added to existing.
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    MoveMultiStatusHolder copyToCache(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun, boolean suppressLayouts,
            boolean failFast, boolean atomic, Properties properties, boolean overrideProperties);

    /**
     * Copies repository path to another absolute path in multi tx. The copy will only copy paths the user has permissions to read
     * and paths that are accepted by the target repository. Maven metadata will be recalculated for both the source and
     * target folders. Metadata is also copied.
     *
     * @param fromRepoPath    Repository path to copy. This path must represent a folder in a local repository.
     * @param targetRepoPath  Path of the target local non-cached repository to copy the path to.
     * @param dryRun          If true the method will just report the expected result but will not copy any file
     * @param suppressLayouts If true, path translation across different layouts should be suppressed.
     * @param failFast        If true, the operation should fail upon encountering an error.
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    MoveMultiStatusHolder copyMultiTx(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun,
            boolean suppressLayouts, boolean failFast);

    /**
     * Copies Docker Repositories from one to another
     *
     * @param fromRepoPath      Repository path to copy. This path must represent a folder in a local repository.
     * @param targetRepoPath    Path of the target local non-cached repository to copy the path to.
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    MoveMultiStatusHolder copy(RepoPath fromRepoPath, RepoPath targetRepoPath);

    /**
     * Move repository path to another absolute path in multi tx. The move will only move paths the user has permissions to read
     * and paths that are accepted by the target repository. Maven metadata will be recalculated for both the source and
     * target folders. Metadata is also moved.
     *
     * @param fromRepoPath    Repository path to move. This path must represent a folder in a local repository.
     * @param targetRepoPath  Path of the target local non-cached repository to move the path to.
     * @param dryRun          If true the method will just report the expected result but will not copy any file
     * @param suppressLayouts If true, path translation across different layouts should be suppressed.
     * @param failFast        If true, the operation should fail upon encountering an error.
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    MoveMultiStatusHolder moveMultiTx(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun,
            boolean suppressLayouts, boolean failFast);

    /**
     * Move repository path to another absolute path in multi tx. The move will only move paths the user has permissions to read
     * and paths that are accepted by the target repository. Maven metadata will be recalculated for both the source and
     * target folders. Metadata is also moved.
     *
     * @param fromRepoPath    Repository path to move. This path must represent a folder in a local repository.
     * @param targetRepoPath  Path of the target local non-cached repository to move the path to.
     * @param dryRun          If true the method will just report the expected result but will not copy any file
     * @param suppressLayouts If true, path translation across different layouts should be suppressed.
     * @param failFast        If true, the operation should fail upon encountering an error.
     * @param atomic          If true, copy will be done in a single transaction, else in multi transaction.
     * @param properties      properties to set
     * @param overrideProperties If true then properties given will override existing properties, else properties will be added to existing.
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    MoveMultiStatusHolder move(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun, boolean suppressLayouts,
            boolean failFast, boolean atomic, Properties properties, boolean overrideProperties);
    /**
     * Copies repository path to another absolute path in single tx may case performance issues. The copy will only copy paths the user has permissions to read
     * and paths that are accepted by the target repository. Maven metadata will be recalculated for both the source and
     * target folders. Metadata is also copied.
     *
     * @param fromRepoPath    Repository path to copy. This path must represent a folder in a local repository.
     * @param targetRepoPath  Path of the target local non-cached repository to copy the path to.
     * @param dryRun          If true the method will just report the expected result but will not copy any file
     * @param suppressLayouts If true, path translation across different layouts should be suppressed.
     * @param failFast        If true, the operation should fail upon encountering an error.
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    @Lock
    MoveMultiStatusHolder copy(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun, boolean suppressLayouts,
            boolean failFast);

    /**
     * Copies a set of paths to another local repository in single tx mode in order to prevent long db locking.
     * <p/>
     * This method will only copy paths the user has permissions to move and paths that are accepted by the target
     * repository.
     * <p/>
     * Maven metadata will be recalculated for both the source and target folders after all items has been copied. If a
     * path already belongs to the target repository it will be skipped.
     * <p/>
     * This copy method does not use Unix-style handling of existing nested folders, meaning that folder content might
     * be overwritten (i.e. when copying source path org/jfrog/1 to target path org/jfrog/1 the contents of source 1/
     * are copied into target 1/  - unlike normal unix behavior where such an operation will create org/jfrog/1/1)
     *
     * @param pathsToCopy        Paths to copy, each pointing to file or folder.
     * @param targetLocalRepoKey Key of the target local non-cached repository to move the path to.
     * @param properties
     * @param dryRun             If true the method will just report the expected result but will not copy any file
     * @param failFast           True if the operation should abort upon the first occurring warning or error
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    @Lock
    MoveMultiStatusHolder copy(Set<RepoPath> pathsToCopy, String targetLocalRepoKey,
            Properties properties, boolean dryRun, boolean failFast);

    /**
     * delete all artifacts with the given paths
     * @param pathsToMove
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    @Lock
    MoveMultiStatusHolder delete(Set<RepoPath> pathsToMove);

    /**
     * Expire expirable resources (folders, snapshot artifacts, maven metadata, etc.)
     *
     * @param repoPath Cache repository path of a folder of file to zap. If it is a folder the zap is recursively
     *                 applied.
     * @return A count of the items affected by the zap
     */
    @Lock
    int zap(RepoPath repoPath);

    Set<String> getAllRepoKeys();

    Map<Character, List<String>> getAllRepoKeysByFirstCharMap();

    /**
     * Return true if the given {@code repoPath} exists as a local or local cache repository. Throws an exception if not found
     *
     * @param repoPath A repo path in the repository
     * @return Local/cache repository matching the repo path repo key
     *
     * @throws IllegalArgumentException if repoPath not found
     */
    boolean exists(RepoPath repoPath);

    boolean existsBySha1(String sha1);

    /**
     * Returns a list of children {@link org.artifactory.fs.ItemInfo} of the given repo path that can be read by the
     * current user.
     * An empty list is returned if the path doesn't exist of is not pointing to a folder.
     *
     * @param repoPath The repo path to list children
     * @return Returns a list of children {@link org.artifactory.fs.ItemInfo} of the given repo path or empty list.
     */
    @Nonnull
    List<ItemInfo> getChildren(RepoPath repoPath);

    @Lock
    List<ItemInfo> getChildrenDeeply(RepoPath path);

    List<String> getChildrenNames(RepoPath repoPath);

    boolean hasChildren(RepoPath repoPath);

    void exportRepo(String repoKey, ExportSettings settings);

    ArchiveFileContent getGenericArchiveFileContent(RepoPath archivePath, String sourceEntryPath) throws IOException;


    /**
     * Export the selected search result into a target directory
     *
     * @param searchResults The search results to export
     * @param baseSettings
     * @return The status of the procedure
     */
    MutableStatusHolder exportSearchResults(SavedSearchResults searchResults, ExportSettingsImpl baseSettings);

    /**
     * Returns all the version units under a certain path.
     *
     * @param repoPath The repository path (might be repository root with no sub-path)
     * @return ItemSearchResults containing version units under a certain path
     */
    VersionSearchResults getVersionUnitsUnder(RepoPath repoPath);

    /**
     * @return the number of artifacts currently being served, including virtual repo cached files
     */
    long getArtifactCount();

    /**
     * Returns a list of local repo descriptors that the user is permitted to deploy on
     *
     * @return List<LocalRepoDescriptor> - List of deploy-permitted local repos
     */
    List<LocalRepoDescriptor> getDeployableRepoDescriptors();

    /**
     * Checks if the specified repoPath is handled by the snapshot(integration)/release policy of the repoPath's
     * repository.
     */
    boolean isRepoPathHandled(RepoPath repoPath);

    /**
     * Checks if the specified repoPath () is accepted by the include/exclude rules of the repoPath's repository.
     *
     * @param repoPath The repo path to check
     * @return True if the repository accepts the given path
     */
    boolean isLocalOrCachedRepoPathAccepted(RepoPath repoPath);

    /**
     * Checks if the specified repoPath () is accepted by the include/exclude rules of the repoPath's repository.
     * or current user can annotate
     * @param repoPath The repo path to check
     * @return True if the repository accepts the given path
     */
    boolean isLocalOrCachedRepoPathAcceptedOrCanAnotate(RepoPath repoPath);
    /**
     * Checks if the specified repoPath is accepted by the include/exclude rules of the specified repository.
     *
     * @param repoPath The repo path to check
     * @return True if the repository accepts the given path
     */
    boolean isRepoPathAccepted(RepoPath repoPath);

    /**
     * Indicates whether the repo path is visible, permission and repo-acceptance-wise
     *
     * @param repoPath Repo path to check
     * @return True if the the current user can read the path and the path is accepted by the repo. When the path is not
     * accepted, the method will return true if the user has annotate permissions or higher
     */
    boolean isRepoPathVisible(RepoPath repoPath);

    /**
     * @return List of virtual repositories that include the repository in their list.
     */
    List<VirtualRepoDescriptor> getVirtualReposContainingRepo(RepoDescriptor repoDescriptor);

    /**
     * @return List of virtual repositories that include the repository in their list according to specified depth.
     */
    List<VirtualRepoDescriptor> getVirtualReposContainingRepo(RepoDescriptor repoDescriptor,int depth);

    /**
     * Indicates if the given virtual repo path exists
     *
     * @param repoPath Virtual repo path
     * @return True if repo path exists, false if not
     */
    boolean virtualItemExists(RepoPath repoPath);

    /**
     * Returns the shared remote repository list from the given Artifactory instance URL
     *
     * @param remoteUrl  URL of remote Artifactory instance
     * @param headersMap Header-map to add to the request
     * @return List of shared remote repositories
     */
    List<RemoteRepoDescriptor> getSharedRemoteRepoConfigs(String remoteUrl, Map<String, String> headersMap);

    /**
     * @param zipPath Path to a zip like file
     * @return Tree representation of the entries in the zip.
     * @throws IOException On error retrieving or parsing the zip file
     */
    Tree<ZipEntryInfo> zipEntriesToTree(RepoPath zipPath) throws IOException;


    ArchiveInputStream archiveInputStream(RepoPath zipPath) throws IOException;

    /**
     * Returns the latest modified item of the given file or folder (recursively)
     *
     * @param pathToSearch Repo path to search in
     * @return Latest modified item
     */
    ItemInfo getLastModified(RepoPath pathToSearch);

    @Lock
    void touch(RepoPath repoPath);

    /**
     * Fixes ant inconsistencies with the files checksums.
     *
     * @param fileRepoPath Repository path of the file
     */
    @Lock
    void fixChecksums(RepoPath fileRepoPath);

    ModuleInfo getItemModuleInfo(RepoPath repoPath);

    /**
     * Creates a folder in the specified path if such folder not already exist. Fails if the repo path points to a file.
     *
     * @param folderRepoPath Repo path to create the folder in
     * @return True if created successfully, false otherwise
     *
     * @throws FolderExpectedException if a file node already exist in this path
     */
    @Lock
    boolean mkdirs(RepoPath folderRepoPath);

    StatusHolder deploy(RepoPath repoPath, InputStream inputStream);

    /**
     * Returns the first resolved local file info from a virtual repo.
     *
     * @param virtualRepoPath Repo path of virtual file
     * @return Local file info
     */
    FileInfo getVirtualFileInfo(RepoPath virtualRepoPath);

    /**
     * Returns the first resolved local item info from a virtual repo.
     *
     * @param virtualRepoPath Repo path of virtual item
     * @return Local item info
     */
    ItemInfo getVirtualItemInfo(RepoPath virtualRepoPath);

    FolderInfo getVirtualFolderInfo(RepoPath virtualRepoPath);

    /**
     * Returns a list of the local and cache aggregated repositories (recursively) under the given virtual
     * repository key, or an empty list in case of non-existing virtual repository
     *
     * @param virtualRepoKey The virtual repository key
     * @return A list of the local and cache aggregates repositories under the given virtual repository key
     */
    List<RepoDescriptor> getVirtualResolvedLocalAndCacheDescriptors(String virtualRepoKey);

    /**
     * Returns the files count under the specified repo path (repository or folder).
     *
     * @param repoPath Repository path of a root repo or a folder
     * @return Files count under the specified repo path
     */
    long getArtifactCount(RepoPath repoPath);

    /**
     * Returns the files count and total size under specified repo path (repository or folder)
     *
     * @return folder summery with size and count
     */
    FolderSummeryInfo getArtifactCountAndSize(@Nonnull RepoPath repoPath);


    /**
     * Returns the files and folder (nodes)count under the specified repo path (repository or folder).
     *
     * @param repoPath Repository path of a root repo or a folder
     * @return files and folder count under the specified repo path
     */
    long getNodesCount(RepoPath repoPath);

    /**
     * Search for all files with bad checksums of the given checksum type (SHA-1 or MD5)
     *
     * @param type The checksum type to search for, we support SHA-1 or MD5
     */
    List<FileInfo> searchFilesWithBadChecksum(ChecksumType type);

    @Nullable
    StatsInfo getStatsInfo(RepoPath repoPath);

    boolean isWriteLocked(RepoPath repoPath);

    List<ItemInfo> getOrphanItems(RepoPath repoPath);

    void reloadConfigurationLazy();

    /**
     * Verifies remove repository is allowed and removes it from the config descriptor
     */
    BasicStatusHolder removeRepository(String repoKey);

    List<RepoDescriptor> getRepoDescriptorByPackageType(RepoType repoType);

    Map<RepoType, Integer> getRepoDescriptorByPackageTypeCount();

}
