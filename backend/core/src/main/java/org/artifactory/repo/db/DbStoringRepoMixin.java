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

package org.artifactory.repo.db;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.PropertiesAddon;
import org.artifactory.addon.RestCoreAddon;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.StatusHolder;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.exception.CancelException;
import org.artifactory.exception.SQLIntegrityException;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.fs.ZipEntryRepoResource;
import org.artifactory.io.SimpleResourceStreamHandle;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.io.checksum.policy.ChecksumPolicyException;
import org.artifactory.maven.MavenModelUtils;
import org.artifactory.md.Properties;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.model.xstream.fs.StatsImpl;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.SaveResourceContext;
import org.artifactory.repo.cache.expirable.CacheExpiry;
import org.artifactory.repo.interceptor.StorageInterceptors;
import org.artifactory.repo.local.LocalNonCacheOverridables;
import org.artifactory.repo.local.PathDeletionContext;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.request.RepoRequests;
import org.artifactory.request.Request;
import org.artifactory.request.RequestContext;
import org.artifactory.resource.*;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.security.AccessLogger;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.binstore.service.BinaryInfo;
import org.artifactory.storage.fs.*;
import org.artifactory.storage.fs.lock.FsItemsVault;
import org.artifactory.storage.fs.lock.LockEntryId;
import org.artifactory.storage.fs.lock.LockingHelper;
import org.artifactory.storage.fs.repo.StoringRepo;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.StatsService;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.fs.session.StorageSessionHolder;
import org.artifactory.storage.fs.tree.VfsImmutableProvider;
import org.artifactory.storage.service.StatsServiceImpl;
import org.artifactory.util.ExceptionUtils;
import org.jfrog.storage.binstore.exceptions.BinaryStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Yossi Shaul
 */
public class DbStoringRepoMixin<T extends RepoBaseDescriptor> implements StoringRepo {
    private static final Logger log = LoggerFactory.getLogger(DbStoringRepoMixin.class);

    public static final String ETAG_PROP_KEY = PropertySet.ARTIFACTORY_RESERVED_PROP_SET + ".internal.etag";

    private static final String CONTENT_TYPE_PROP_KEY = PropertySet.ARTIFACTORY_RESERVED_PROP_SET + "." +
            PropertiesService.CONTENT_TYPE_PROPERTY_NAME;

    private final T descriptor;

    private AuthorizationService authorizationService;
    private StorageInterceptors interceptors;
    private AddonsManager addonsManager;
    private FileService fileService;
    private StatsService statsService;
    private CentralConfigService configService;

    private DbStoringRepoMixin oldStoringRepo;
    private FsItemsVault fileVault;
    private InternalRepositoryService repositoryService;
    private PropertiesService propertiesService;
    private VfsItemProviderFactory vfsItemProviderFactory;
    private FsItemsVault folderVault;

    public DbStoringRepoMixin(T descriptor, DbStoringRepoMixin oldStoringRepo) {
        this.descriptor = descriptor;
        this.oldStoringRepo = oldStoringRepo;
    }

    public void init() throws StorageException {
        ArtifactoryContext context = ContextHelper.get();

        authorizationService = context.beanForType(AuthorizationService.class);

        interceptors = context.beanForType(StorageInterceptors.class);
        addonsManager = context.beanForType(AddonsManager.class);
        fileService = context.beanForType(FileService.class);
        statsService = context.beanForType(StatsServiceImpl.class);
        configService = context.beanForType(CentralConfigService.class);
        repositoryService = context.beanForType(InternalRepositoryService.class);
        propertiesService = context.beanForType(PropertiesService.class);
        vfsItemProviderFactory = context.beanForType(VfsItemProviderFactory.class);
        if (oldStoringRepo != null) {
            fileVault = oldStoringRepo.fileVault;
            folderVault = oldStoringRepo.folderVault;
        } else {
            fileVault = addonsManager.addonByType(HaAddon.class).getFsFileItemVault();
            folderVault = addonsManager.addonByType(HaAddon.class).getFsFolderItemVault();
        }
        // Throw away after usage
        oldStoringRepo = null;

        //Create the repo node if it doesn't exist
        RepoPath rootRP = InternalRepoPathFactory.create(getKey(), "");
        if (!fileService.exists(rootRP)) {
            createOrGetFolder(rootRP);
        }
    }

    /**
     * Create the resource in the local repository
     */
    @SuppressWarnings("unchecked")
    public RepoResource saveResource(SaveResourceContext context) throws IOException, RepoRejectException {
        RepoResource res = context.getRepoResource();
        RepoPath repoPath = InternalRepoPathFactory.create(getKey(), res.getRepoPath().getPath());
        MutableVfsFile mutableFile = null;
        try {
            //Create the parent folder if it does not exist
            RepoPath parentPath = repoPath.getParent();
            if (parentPath == null) {
                throw new StorageException("Cannot save resource, no parent repo path exists");
            }

            // get or create the file. also creates any ancestors on the path to this item
            mutableFile = createOrGetFile(repoPath);

            invokeBeforeCreateInterceptors(mutableFile);

            setClientChecksums(res, mutableFile);

            /*
             * If the file isn't a non-unique snapshot and it already exists, create a defensive of the checksums
             * info for later comparison
             */
            boolean isNonUniqueSnapshot = MavenNaming.isNonUniqueSnapshot(repoPath.getId());
            String overriddenFileSha1 = null;
            if (!isNonUniqueSnapshot && !mutableFile.isNew()) {
                overriddenFileSha1 = mutableFile.getSha1();
                log.debug("Overriding {} with sha1: {}", mutableFile.getRepoPath(), overriddenFileSha1);
            }

            fillBinaryData(context, mutableFile);

            verifyChecksum(mutableFile);

            if (jarValidationRequired(repoPath)) {
                validateJar(mutableFile, repoPath);
            }

            long lastModified = res.getInfo().getLastModified();
            mutableFile.setModified(lastModified);
            mutableFile.setUpdated(System.currentTimeMillis());

            String userId = authorizationService.currentUsername();
            mutableFile.setModifiedBy(userId);
            if (mutableFile.isNew()) {
                mutableFile.setCreatedBy(userId);
            }

            // allow admin override of selected file details (i.e., override when replicating)
            overrideFileDetailsFromRequest(context, mutableFile);

            updateResourceWithActualBinaryData(res, mutableFile);

            clearOldFileData(mutableFile, isNonUniqueSnapshot, overriddenFileSha1);

            Properties properties = context.getProperties();
            if (properties != null) {
                saveProperties(context, mutableFile);
            }

            invokeAfterCreateInterceptors(mutableFile, res);
            AccessLogger.deployed(repoPath);
            return res;
        } catch (SQLIntegrityException e) {
            throw e;
        } catch (Exception e) {
            return handleSaveResourceException(context, res, mutableFile, e);
        }
    }

    private RepoResource handleSaveResourceException(SaveResourceContext context, RepoResource res,
            MutableVfsFile mutableFile, Exception e) throws RepoRejectException, IOException {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (NoTransactionException e1) {
            log.error("Cannot rollback!", e1);
        }
        if (mutableFile != null) {
            mutableFile.markError();
        }
        // throw back any CancelException
        if (e instanceof CancelException) {
            if (mutableFile != null) {
                LockingHelper.removeLockEntry(mutableFile.getRepoPath());
            }
            RepoRejectException repoRejectException = new RepoRejectException(e);
            context.setException(repoRejectException);
            throw repoRejectException;
        }

        // throw back any RepoRejectException (ChecksumPolicyException etc)
        Throwable rejectException = ExceptionUtils.getCauseOfType(e, RepoRejectException.class);
        if (rejectException != null) {
            context.setException(rejectException);
            throw (RepoRejectException) rejectException;
        }
        //Unwrap any IOException and throw it
        Throwable ioCause = ExceptionUtils.getCauseOfType(e, IOException.class);
        if (ioCause != null) {
            log.error("IO error while trying to save resource {}'': {}: {}", res.getRepoPath(),
                    ioCause.getClass().getName(), ioCause.getMessage());
            log.debug("IO error while trying to save resource {}'': {}",
                    res.getRepoPath(), ioCause.getMessage(), ioCause);
            context.setException(ioCause);
            throw (IOException) ioCause;
        }
        log.error("Couldn't save resource {}, reason:", res.getRepoPath(), e);
        context.setException(e);
        throw ExceptionUtils.toRuntimeException(e);
    }

    private void fillBinaryData(SaveResourceContext context, MutableVfsFile mutableFile) {
        BinaryInfo binaryInfo = context.getBinaryInfo();
        boolean usedExistingBinary = false;
        if (binaryInfo != null) {
            usedExistingBinary = mutableFile.tryUsingExistingBinary(binaryInfo.getSha1(), binaryInfo.getSha2(),
                    binaryInfo.getMd5(), binaryInfo.getLength());
            if (!usedExistingBinary) {
                log.debug("Tried to save with existing binary info but failed: '{}': {}",
                        mutableFile.getRepoPath().toPath(), binaryInfo);
            }
        }
        if (!usedExistingBinary) {
            InputStream in = context.getInputStream();
            mutableFile.fillBinaryData(in);
        } else {
            log.trace("Using existing binary info for '{}': {}", mutableFile.getRepoPath().toPath(), binaryInfo);
        }
    }

    private boolean jarValidationRequired(RepoPath repoPath) {
        if (!NamingUtils.isJarVariant(repoPath.getPath())) {
            return false;
        }
        RemoteRepoDescriptor remoteRepoDescriptor;
        T repoDescriptor = getDescriptor();
        if (repoDescriptor instanceof RemoteRepoDescriptor) {
            remoteRepoDescriptor = ((RemoteRepoDescriptor) repoDescriptor);
        } else if (repoDescriptor instanceof LocalCacheRepoDescriptor) {
            remoteRepoDescriptor = ((LocalCacheRepoDescriptor) repoDescriptor).getRemoteRepo();
        } else {
            return false;
        }
        return remoteRepoDescriptor.isRejectInvalidJars();
    }

    private void validateJar(MutableVfsFile mutableFile, RepoPath repoPath)
            throws RepoRejectException, BinaryStorageException {
        String pathId = repoPath.getId();
        InputStream jarStream = mutableFile.getStream();
        try {
            log.trace("Validating the content of '{}'.", pathId);
            JarInputStream jarInputStream = new JarInputStream(jarStream, true);
            JarEntry entry = jarInputStream.getNextJarEntry();

            if (entry == null) {
                if (jarInputStream.getManifest() != null) {
                    log.trace("Found manifest validating the content of '{}'.", pathId);
                    return;
                }
                throw new IllegalStateException("Could not find entries within the archive.");
            }
            log.trace("Finished validating the content of '{}'.", pathId);
        } catch (Exception e) {
            String message = String.format("Failed to validate the content of '%s': %s", pathId, e.getMessage());
            log.debug(message, e);
            throw new RepoRejectException(message, HttpStatus.SC_CONFLICT);
        } finally {
            IOUtils.closeQuietly(jarStream);
        }
    }

    private void saveProperties(SaveResourceContext context, MutableVfsFile mutableFile) throws RepoRejectException {
        Properties properties = context.getProperties();
        if ((properties != null) && !properties.isEmpty()) {
            BasicStatusHolder statusHolder = new BasicStatusHolder();
            for (String key : properties.keys()) {
                Set<String> values = properties.get(key);
                String[] vals = (values != null) ? values.toArray(new String[values.size()]) : new String[]{};
                interceptors.beforePropertyCreate(mutableFile, statusHolder, key, vals);
            }
            checkForCancelException(statusHolder);
            mutableFile.setProperties(properties);
            for (String key : properties.keys()) {
                Set<String> values = properties.get(key);
                String[] vals = (values != null) ? values.toArray(new String[values.size()]) : new String[]{};
                interceptors.afterPropertyCreate(mutableFile, statusHolder, key, vals);
            }
        }
    }

    private void verifyChecksum(MutableVfsFile mutableFile) throws ChecksumPolicyException {
        ChecksumPolicy policy = getChecksumPolicy();
        ChecksumsInfo checksumsInfo = mutableFile.getInfo().getChecksumsInfo();
        boolean passes = policy.verify(checksumsInfo.getChecksums());
        if (!passes) {
            throw new ChecksumPolicyException(policy, checksumsInfo, mutableFile.getRepoPath());
        }
    }

    private void invokeBeforeCreateInterceptors(MutableVfsFile mutableFile) throws RepoRejectException {
        if (mutableFile.getInfo().getRepoPath().isRoot()) {
            return;
        }
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        StorageInterceptors interceptors = ContextHelper.get().beanForType(StorageInterceptors.class);
        interceptors.beforeCreate(mutableFile, statusHolder);
        checkForCancelException(statusHolder);
    }

    private void invokeAfterCreateInterceptors(MutableVfsFile mutableFile, RepoResource res) throws RepoRejectException {
        //TODO: [by YS] leave it here until we decide how to implement events
        if (mutableFile.isNew()) {
            StorageSession storageSession = StorageSessionHolder.getSession();
            if (storageSession != null) {
                storageSession.save();
            }
        }

        BasicStatusHolder statusHolder = new BasicStatusHolder();
        InterceptorCreateContext ctx = new InterceptorCreateContext();
        interceptors.afterCreate(mutableFile, statusHolder, ctx);
        checkForCancelException(statusHolder);
        changePathIfNeeded(res, ctx);
    }

    /**
     * Checks if one of the interceptors changed the resource location during the creation.
     * In case not empty we change the response repo path on a repo resource.
     *
     * @param repoResource resource deployed
     * @param context holds the alternate path in case artifact was moved
     */
    private void changePathIfNeeded(RepoResource repoResource, InterceptorCreateContext context) {
        RepoPath alternateRepoPath = context.getAlternateRepoPath();
        if (alternateRepoPath != null) {
            repoResource.setResponseRepoPath(alternateRepoPath);
        }
    }

    private void checkForCancelException(StatusHolder status) throws RepoRejectException {
        if (status.getCancelException() != null) {
            throw status.getCancelException();
        }
    }

    /**
     * Return <b>locally</b> stored file resource. Unfound resource will be returned if the requested resource doesn't
     * exist locally.
     */
    public RepoResource getInfo(InternalRequestContext context) throws FileExpectedException {
        RestCoreAddon restCoreAddon = addonsManager.addonByType(RestCoreAddon.class);
        org.artifactory.repo.StoringRepo storingRepo = getOwningRepo();
        context = restCoreAddon.getDynamicVersionContext(storingRepo, context, false);

        String path = context.getResourcePath();
        RepoPath repoPath = InternalRepoPathFactory.create(getKey(), path);
        VfsFile vfsFile = getImmutableFile(repoPath);
        if (vfsFile == null) {
            RepoRequests.logToContext("Unable to find resource in %s", repoPath);
            return new UnfoundRepoResource(repoPath, "File not found.");
        }
        if (MavenNaming.isMavenMetadata(path)) {
            PropertiesAddon propertiesAddon = addonsManager.addonByType(PropertiesAddon.class);
            RepoResource mdResource = propertiesAddon.assembleDynamicMetadata(context, repoPath);
            if ((mdResource instanceof FileResource) && MavenNaming.isSnapshotMavenMetadata(path)) {
                mdResource = resolveMavenMetadataForCompatibility(((FileResource) mdResource), context);
            }
            return mdResource;
        }

        //Handle query-aware get
        Properties properties = propertiesService.getProperties(vfsFile.getInfo());
        Properties queryProperties = context.getProperties();
        boolean exactMatch = true;
        if (!queryProperties.isEmpty()) {
            RepoRequests.logToContext("Request includes query properties");
            Properties.MatchResult matchResult;
            matchResult = properties.matchQuery(queryProperties);
            if (matchResult == Properties.MatchResult.NO_MATCH) {
                exactMatch = false;
                RepoRequests.logToContext("Request query properties don't match those that annotate the artifact");
            } else if (matchResult == Properties.MatchResult.CONFLICT) {
                RepoRequests.logToContext("Request query properties conflict with those that annotate the " +
                        "artifact - returning unfound resource");
                return new UnfoundRepoResource(repoPath, "File '" + repoPath +
                        "' was found, but mandatory properties do not match.",
                        UnfoundRepoResourceReason.Reason.PROPERTY_MISMATCH);
            }
        }

        RepoResource localResource = getFilteredOrFileResource(vfsFile, context, properties, exactMatch);
        setMimeType(localResource, properties);
        setETag(localResource, properties);
        return localResource;
    }

    private void setMimeType(RepoResource localResource, Properties properties) {
        if (!localResource.isFound() || !(localResource instanceof FileResource) || (properties == null)) {
            return;
        }
        String customMimeType = properties.getFirst(CONTENT_TYPE_PROP_KEY);
        if (isNotBlank(customMimeType)) {
            ((FileResource) localResource).setMimeType(customMimeType);
        }
    }

    private void setETag(RepoResource localResource, Properties properties) {
        if (!localResource.isFound() || !(localResource instanceof FileResource) || (properties == null)) {
            return;
        }
        String etag = properties.getFirst(ETAG_PROP_KEY);
        if (isNotBlank(etag)) {
            ((FileResource) localResource).setETag(etag);
        }
    }

    @Override
    public ChecksumPolicy getChecksumPolicy() {
        return getOwningRepo().getChecksumPolicy();
    }

    private org.artifactory.repo.StoringRepo getOwningRepo() {
        return repositoryService.storingRepositoryByKey(descriptor.getKey());
    }

    private RepoResource resolveMavenMetadataForCompatibility(FileResource metadataResource,
            InternalRequestContext context) {
        if (ConstantValues.mvnMetadataVersion3Enabled.getBoolean() && !context.clientSupportsM3SnapshotVersions()) {
            RepoRequests.logToContext("Use of v3 Maven metadata is enabled, but the requesting client doesn't " +
                    "support it - checking if the response should be modified for compatibility");
            FileInfo info = metadataResource.getInfo();
            try {
                Metadata metadata = MavenModelUtils.toMavenMetadata(repositoryService.getStringContent(info));
                Versioning versioning = metadata.getVersioning();
                if (versioning != null) {
                    List<SnapshotVersion> snapshotVersions = versioning.getSnapshotVersions();
                    if ((snapshotVersions != null) && !snapshotVersions.isEmpty()) {
                        RepoRequests.logToContext("Found snapshot versions - modifying the response for compatibility");
                        versioning.setSnapshotVersions(null);
                        return new ResolvedResource(metadataResource, MavenModelUtils.mavenMetadataToString(metadata));
                    }
                }
            } catch (IOException e) {
                RepoPath repoPath = info.getRepoPath();
                log.error("An error occurred while filtering Maven metadata '{}' for compatibility: " +
                        "{}\nReturning original content.", repoPath, e.getMessage());
                RepoRequests.logToContext("An error occurred while filtering for compatibility. Returning the " +
                        "original content: %s", e.getMessage());
            }
        }
        //In case none of the above apply, or if we encountered an error, return the original resource
        return metadataResource;
    }

    @Override
    public boolean hasChildren(VfsFolder vfsFolder) {
        return fileService.hasChildren(vfsFolder.getRepoPath());
    }

    @Override
    public List<VfsItem> getImmutableChildren(VfsFolder folder) {
        List<ItemInfo> childrenInfo = fileService.loadChildren(folder.getRepoPath());
        List<VfsItem> mutableChildren = Lists.newArrayListWithCapacity(childrenInfo.size());
        for (ItemInfo childInfo : childrenInfo) {
            mutableChildren.add(getImmutableItem(childInfo.getRepoPath()));
        }
        return mutableChildren;
    }

    @Override
    public List<MutableVfsItem> getMutableChildren(MutableVfsFolder folder) {
        List<ItemInfo> childrenInfo = fileService.loadChildren(folder.getRepoPath());
        List<MutableVfsItem> mutableChildren = Lists.newArrayListWithCapacity(childrenInfo.size());
        for (ItemInfo childInfo : childrenInfo) {
            MutableVfsItem mutableItem;
            if (childInfo.isFolder()) {
                mutableItem = getMutableFolder(childInfo.getRepoPath());
            } else {
                mutableItem = getMutableFile(childInfo.getRepoPath());
            }
            if (mutableItem != null) {
                mutableChildren.add(mutableItem);
            } else {
                log.debug("Item deleted between first item loading and write lock: {}", childInfo.getRepoPath());
            }
        }
        return mutableChildren;
    }

    @Override
    public boolean itemExists(String relativePath) {
        return fileService.exists(new RepoPathImpl(getKey(), relativePath));
    }

    private RepoResource getFilteredOrFileResource(VfsFile vfsFile, RequestContext context,
            Properties properties, boolean exactMatch) {
        Request request = context.getRequest();
        if (request != null && descriptor.isReal()) {
            FilteredResourcesAddon filteredResourcesAddon = addonsManager.addonByType(FilteredResourcesAddon.class);
            if (filteredResourcesAddon.isFilteredResourceFile(vfsFile.getRepoPath(), properties)) {
                RepoRequests.logToContext("Resource is marked as filtered - sending it through the engine");
                InputStream stream = vfsFile.getStream();
                try {
                    return filteredResourcesAddon.getFilteredResource(request, vfsFile.getInfo(), stream);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
        }

        if (request != null && request.isZipResourceRequest()) {
            RepoRequests.logToContext("Resource is contained within an archiving - retrieving");
            InputStream stream = vfsFile.getStream();
            try {
                return addonsManager.addonByType(FilteredResourcesAddon.class).getZipResource(
                        request, vfsFile.getInfo(), stream);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }

        return new FileResource(vfsFile.getInfo(), exactMatch);
    }

    public T getDescriptor() {
        return descriptor;
    }

    private void overrideFileDetailsFromRequest(SaveResourceContext context, MutableVfsFile mutableFile) {
        // Only administrators can override the core attributed of a file. This is mainly used during replication
        // This is not open to non-admin to prevent faked data (e.g., createdBy)
        if (!authorizationService.isAdmin()) {
            return;
        }
        if (context.getCreated() > 0) {
            mutableFile.setCreated(context.getCreated());
        }

        if (isNotBlank(context.getCreateBy())) {
            mutableFile.setCreatedBy(context.getCreateBy());
        }

        if (isNotBlank(context.getModifiedBy())) {
            mutableFile.setModifiedBy(context.getModifiedBy());
        }
    }

    private void setClientChecksums(RepoResource res, MutableVfsFile mutableFile) {
        // set the file extension checksums (only needed if the file is currently being downloaded)
        ChecksumsInfo resourceChecksums = res.getInfo().getChecksumsInfo();
        ChecksumInfo sha1Info = resourceChecksums.getChecksumInfo(ChecksumType.sha1);
        mutableFile.setClientSha1(sha1Info != null ? sha1Info.getOriginalOrNoOrig() : null);
        ChecksumInfo sha2Info = resourceChecksums.getChecksumInfo(ChecksumType.sha256);
        mutableFile.setClientSha2(sha2Info != null ? sha2Info.getOriginalOrNoOrig() : null);
        ChecksumInfo md5Info = resourceChecksums.getChecksumInfo(ChecksumType.md5);
        mutableFile.setClientMd5(md5Info != null ? md5Info.getOriginalOrNoOrig() : null);
    }

    private void updateResourceWithActualBinaryData(RepoResource res, VfsFile vfsFile) {
        // update the resource with actual checksums and size (calculated in fillBinaryData) - RTFACT-3112
        if (res.getInfo() instanceof MutableRepoResourceInfo) {
            ChecksumsInfo newlyCalculatedChecksums = vfsFile.getInfo().getChecksumsInfo();
            MutableRepoResourceInfo info = (MutableRepoResourceInfo) res.getInfo();
            info.setChecksums(newlyCalculatedChecksums.getChecksums());
            info.setSize(vfsFile.length());
        }
    }

    private void clearOldFileData(MutableVfsFile mutableFile, boolean isNonUniqueSnapshot, String oldFileSha1) {
        // If the artifact is not a non-unique snapshot and already exists but with a different checksum, remove
        // all the existing metadata
        if (!isNonUniqueSnapshot && oldFileSha1 != null) {
            if (!oldFileSha1.equals(mutableFile.getSha1())) {
                mutableFile.setStats(new StatsImpl());
                mutableFile.setProperties(new PropertiesImpl());
            }
        }
    }

    MutableVfsItem getMutableItem(RepoPath repoPath) {
        VfsMutableItemProvider provider = vfsItemProviderFactory.createItemProvider(this, repoPath,
                fileVault, folderVault);
        return provider.getMutableFsItem();
    }

    VfsItem getImmutableItem(RepoPath repoPath) {
        VfsImmutableProvider provider = vfsItemProviderFactory.
                createImmutableItemProvider(this, repoPath);
        return provider.getImmutableFsItem();
    }

    public VfsFile getImmutableFile(RepoPath repoPath) {
        VfsImmutableProvider provider = vfsItemProviderFactory.createImmutableFileProvider(this, repoPath);
        return provider.getImmutableFile();
    }

    public MutableVfsFile getMutableFile(RepoPath repoPath) {
        VfsMutableFileProvider provider = vfsItemProviderFactory.createFileProvider(this, repoPath,
                fileVault, folderVault);
        return provider.getMutableFile();
    }

    public MutableVfsFile createOrGetFile(RepoPath repoPath) {
        VfsMutableFileProvider provider = vfsItemProviderFactory.createFileProvider(this, repoPath,
                fileVault, folderVault);
        return provider.getOrCreMutableFile();
    }

    @Override
    public VfsFolder getImmutableFolder(RepoPath repoPath) {
        VfsImmutableProvider provider = vfsItemProviderFactory.createImmutableFolderProvider(this, repoPath);
        return provider.getImmutableFolder();
    }

    public MutableVfsFolder getMutableFolder(RepoPath repoPath) {
        VfsMutableFolderProvider provider = vfsItemProviderFactory.createFolderProvider(this, repoPath,
                fileVault, folderVault);
        return provider.getMutableFolder();
    }

    public MutableVfsFolder createOrGetFolder(RepoPath repoPath) {
        VfsMutableFolderProvider provider = vfsItemProviderFactory.createFolderProvider(this, repoPath,
                fileVault, folderVault);
        return provider.getOrCreMutableFolder();
    }

    @Override
    public String getKey() {
        return descriptor.getKey();
    }

    @Override
    public boolean isWriteLocked(RepoPath repoPath) {
        // TODO: [by FSI] Invalid argument if not me repo key
        LockEntryId lock = fileVault.getLock(repoPath);
        return lock.getLock().isLocked();
    }

    public void undeploy(DeleteContext ctx) {
        MutableVfsItem item = getMutableItem(ctx.getRepoPath());
        if (item == null || item.isMarkedForDeletion()) {
            return;
        }
        item.delete(ctx);
    }

    public ResourceStreamHandle getResourceStreamHandle(RequestContext requestContext, RepoResource res)
            throws IOException {

        RepoPathImpl repoPath = new RepoPathImpl(getKey(), res.getRepoPath().getPath());
        RepoRequests.logToContext("Creating a resource handle from '%s'", repoPath);
        VfsFile file = getImmutableFile(repoPath);

        // If resource does not exist throw an IOException
        if (file == null) {
            RepoRequests.logToContext("Unable to find the resource - throwing exception");
            throw new FileNotFoundException(
                    "Could not get resource stream. Path '" + repoPath.getPath() + "' not found in " + getKey());
        }

        RepoRequests.logToContext("Identified requested resource as a file");
        ResourceStreamHandle handle;
        Request request = requestContext.getRequest();
        if (request != null && request.isZipResourceRequest() && (res instanceof ZipEntryRepoResource)) {
            RepoRequests.logToContext("Requested resource is contained within an archive - " +
                    "using specialized handle");
            handle = addonsManager.addonByType(FilteredResourcesAddon.class)
                    .getZipResourceHandle((ZipEntryRepoResource) res, file.getStream());
        } else {
            RepoRequests.logToContext("Requested resource is an ordinary artifact - " +
                    "using normal content handle with length '%s'", file.length());
            handle = new SimpleResourceStreamHandle(file.getStream(), file.length());
        }
        statsService.updateDownloadStatsIfNeeded(file.getRepoPath(), request, descriptor.isReal());
        return handle;
    }

    boolean shouldProtectPathDeletion(PathDeletionContext pathDeletionContext) {
        String path = pathDeletionContext.getPath();
        if (NamingUtils.isChecksum(path)) {
            //Never protect checksums
            return false;
        }

        if (pathDeletionContext.isAssertOverwrite()) {
            if (MavenNaming.isMavenMetadata(path)) {
                return false;
            }

            org.artifactory.repo.StoringRepo owningRepo = getOwningRepo();
            if (owningRepo.isCache()) {
                if (pathDeletionContext.isForceExpiryCheck()) {
                    return false;
                }
                if ((ContextHelper.get().beanForType(CacheExpiry.class).isExpirable(
                        owningRepo.getDescriptor().getType(), owningRepo.getKey(), path))) {
                    return false;
                }
            } else {
                if (ContextHelper.get().beanForType(LocalNonCacheOverridables.class)
                        .isOverridable(((LocalRepo) owningRepo), path)) {
                    return false;
                }
            }
            return isFileOverwrite(pathDeletionContext);
        }
        return true;
    }

    /**
     * Should protect path deletion for these conditions:
     * - If the item is a file (means that is also exists) and no incoming checksums in request, protect.
     * - If this is a deploy with checksum, and the request checksum matches the repoPath one, don't protect.
     * - If the item is not a file (it's either a folder or doesn't exist), don't protect.
     */
    boolean isFileOverwrite(PathDeletionContext pathDeletionContext) {
        String path = pathDeletionContext.getPath();
        String requestSha1 = pathDeletionContext.getRequestSha1();
        String requestSha2 = pathDeletionContext.getRequestSha2();
        RepoPath repoPath = InternalRepoPathFactory.create(getKey(), path);
        if (requestSha1 == null && requestSha2 == null) {
            return repositoryService.exists(repoPath);
        } else {
            try {
                FileInfo fileInfo = repositoryService.getFileInfo(repoPath);
                String existingSha1 = fileInfo.getSha1();
                String existingSha2 = fileInfo.getSha2();
                //If request sha1 or sha2 not equal to existing - this is an overwrite so we should protect
                return differentChecksums(requestSha1, existingSha1) || differentChecksums(requestSha2, existingSha2);
            } catch (ItemNotFoundRuntimeException e) {
                return false;
            }
        }
    }

    private boolean differentChecksums(String requestChecksum, String existingChecksum) {
        return requestChecksum != null && !Objects.equals(requestChecksum, existingChecksum);
    }
}

