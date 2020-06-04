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

package org.artifactory.storage.db.binstore.service;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.ClusterOperationsService;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.ha.interceptor.ClusterTopologyListener;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.storage.BinariesInfo;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.environment.BinaryStoreProperties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.resource.RepoResourceInfo;
import org.artifactory.security.access.AccessService;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.BinaryInsertRetryException;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.binstore.service.*;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.db.binstore.entity.BinaryEntity;
import org.artifactory.storage.db.binstore.exceptions.PruneException;
import org.artifactory.storage.db.binstore.util.BinaryServiceUtils;
import org.artifactory.storage.db.binstore.util.FilestorePruner;
import org.artifactory.storage.db.binstore.visitors.BinaryTreeElementScanner;
import org.artifactory.storage.db.binstore.visitors.EssentialBinaryTreeElementHandler;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.fs.service.ArchiveEntriesService;
import org.artifactory.storage.model.FileBinaryProviderInfo;
import org.artifactory.util.CollectionUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.access.client.AccessClient;
import org.jfrog.security.util.Pair;
import org.jfrog.storage.binstore.common.BinaryElementRequestImpl;
import org.jfrog.storage.binstore.common.BinaryStoreContextImpl;
import org.jfrog.storage.binstore.common.ChecksumInputStream;
import org.jfrog.storage.binstore.common.ReaderTrackingInputStream;
import org.jfrog.storage.binstore.exceptions.BinaryNotFoundException;
import org.jfrog.storage.binstore.exceptions.BinaryRejectedException;
import org.jfrog.storage.binstore.exceptions.BinaryStorageException;
import org.jfrog.storage.binstore.exceptions.SignedUrlException;
import org.jfrog.storage.binstore.ifc.*;
import org.jfrog.storage.binstore.ifc.model.*;
import org.jfrog.storage.binstore.ifc.provider.BinaryProvider;
import org.jfrog.storage.binstore.manager.BinaryProviderManagerImpl;
import org.jfrog.storage.binstore.providers.base.BinaryProviderBase;
import org.jfrog.storage.binstore.providers.base.CloudBinaryProvider;
import org.jfrog.storage.binstore.utils.Checksum;
import org.jfrog.storage.common.ConflictsGuard;
import org.jfrog.storage.common.LockingMapFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.artifactory.checksum.ChecksumInfo.TRUSTED_FILE_MARKER;
import static org.artifactory.storage.db.binstore.util.BinaryServiceUtils.*;
import static org.jfrog.storage.binstore.ifc.model.BinaryElementHeaders.*;

/**
 * The main binary store of Artifactory that delegates to the BinaryProvider chain.
 *
 * @author Yossi Shaul
 */
@Service
@Reloadable(beanClass = BinaryService.class,
        initAfter = {AccessService.class, ClusterOperationsService.class},
        listenOn = CentralConfigKey.none)
public class BinaryServiceImpl implements InternalBinaryService, ClusterTopologyListener {
    private static final Logger log = LoggerFactory.getLogger(BinaryServiceImpl.class);

    @Autowired
    BinariesDao binariesDao;

    @Autowired
    private ArchiveEntriesService archiveEntriesService;

    @Autowired
    private InternalDbService dbService;

    @Autowired
    private AccessService accessService;

    @Autowired
    private DeleteBlobInfosWrapper deleteBlobInfosWrapper;

    /**
     * Map of delete protected sha1 checksums to the number of protections (active readers + writer count for each
     * binary)
     */
    private ConcurrentMap<String, Pair<AtomicInteger, Long>> deleteProtectedBinaries;
    private List<GarbageCollectorListener> garbageCollectorListeners = new CopyOnWriteArrayList<>();
    private BinaryProviderManager binaryProviderManager;
    private BinaryProviderConfig defaultValues;
    private Lock lock = new ReentrantLock();
    private BinaryProvider binaryProvider;
    private boolean forceBinaryProviderOptimizationOnce = false;
    AtomicInteger gcIteration = new AtomicInteger(0);

    @PostConstruct
    public void initialize() {
        log.debug("Initializing the ConfigurableBinaryProviderManager");
        deleteProtectedBinaries = new MapMaker().makeMap();
        // Generate Default values
        ArtifactoryHome artifactoryHome = ArtifactoryHome.get();
        BinaryStoreProperties storeProperties = new BinaryStoreProperties(artifactoryHome.getDataDir().getPath(),
                artifactoryHome.getSecurityDir().getPath());
        HaNodeProperties haNodeProperties = artifactoryHome.getHaNodeProperties();
        if (haNodeProperties != null) {
            storeProperties.setClusterDataDir(haNodeProperties.getClusterDataDir());
        }
        defaultValues = storeProperties.toDefaultValues();
        // TODO: [by fsi] this ha enabled needs to be done at the binary store project level
        if (haNodeProperties != null) {
            defaultValues.setBinaryStoreContext(new BinaryStoreContextImpl(new ArtifactoryLockingMapFactoryProvider(),
                    haNodeProperties.getProperties(), this::exists, this::getAccessClient));
            defaultValues.addParam("serviceId", haNodeProperties.getServerId() + "-binary-store");
        } else {
            // The only difference here is that we don't init the HaNodeProperties
            defaultValues.setBinaryStoreContext(
                    new BinaryStoreContextImpl(new ArtifactoryLockingMapFactoryProvider(), null, this::exists,
                            this::getAccessClient));
        }

        // Set the binarystore.xml file location
        File haAwareEtcDir = artifactoryHome.getEtcDir();
        File userConfigFile = new File(haAwareEtcDir, "binarystore.xml");
        defaultValues.setBinaryStoreXmlPath(userConfigFile.getPath());
        // Finally create an instance of the binary provider manager
        binaryProviderManager = new BinaryProviderManagerImpl(defaultValues);
        // Get the root binary provide from the binary provider manager
        binaryProvider = binaryProviderManager.getFirstBinaryProvider();
    }

    @Override
    public BinaryProviderManager getBinaryProviderManager() {
        return binaryProviderManager;
    }

    @Override
    @PreDestroy
    public void destroy() {
        notifyGCListenersOnDestroy();
        binaryProviderManager.contextDestroyed();
    }

    @Override
    public void addGCListener(GarbageCollectorListener garbageCollectorListener) {
        garbageCollectorListeners.add(garbageCollectorListener);
    }

    @Override
    public void addExternalFileStore(File externalFileDir, ProviderConnectMode connectMode) {
        // The external binary provider works only if the file binary provider is not null
        if (getBinariesDir() == null) {
            return;
        }
        // Prepare parameters for the new External binary provider
        String mode = connectMode.propName;
        String externalDir = externalFileDir.getAbsolutePath();
        String fileStoreDir = defaultValues.getParam("fileStoreDir");
        File fileStoreFullPath = new File(new File(defaultValues.getParam("baseDataDir")), fileStoreDir);
        // create and initialize the external binary providers.
        binaryProviderManager.initializeExternalBinaryProvider(mode, externalDir, fileStoreFullPath.getAbsolutePath(),
                defaultValues);
    }

    @Override
    public void disconnectExternalFilestore(File externalDir, ProviderConnectMode disconnectMode,
            BasicStatusHolder statusHolder) {
        ExternalBinaryProviderHelper.disconnectFromFileStore(this, externalDir, disconnectMode, statusHolder,
                binaryProviderManager, binariesDao, defaultValues);
    }

    @Override
    public File getBinariesDir() {
        // Get binary providers info tree from the manager
        BinaryProvidersInfo binaryProvidersInfo = binaryProviderManager.getBinaryProvidersInfo();
        BinaryTreeElement<BinaryProviderInfo> treeElement = binaryProvidersInfo.rootTreeElement;
        // Collect all the file binary providers in list
        List<FileBinaryProviderInfo> providersInfos = Lists.newArrayList();
        collectFileBinaryProvidersDirsInternal(providersInfos, treeElement);
        // Get the First binary provider
        FileBinaryProviderInfo fileBinaryProviderInfo = providersInfos.size() > 0 ? providersInfos.get(0) : null;
        if (fileBinaryProviderInfo != null) {
            // We need the wrapper to avoid binary dir recalculation even if there is no file binary provider
            return fileBinaryProviderInfo.getFileStoreDir();
        }
        return null;
    }

    @Override
    public boolean isProviderConfigured(String type) {
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("Must set a binary provider type to look for");
        }
        // Get binary providers info tree from the manager
        List<BinaryTreeElement<BinaryProviderInfo>> providersInfos = getBinaryTreeElementsList();
        return providersInfos.stream().filter(Objects::nonNull).map(BinaryTreeElement::getData)
                .map(data -> data.getProperties().get("type")).anyMatch(type::equals);
    }

    private List<BinaryTreeElement<BinaryProviderInfo>> getBinaryTreeElementsList() {
        BinaryProvidersInfo binaryProvidersInfo = binaryProviderManager.getBinaryProvidersInfo();
        List<BinaryTreeElement<BinaryProviderInfo>> providersInfos = Lists.newArrayList();
        collectBinaryProviderInfo(providersInfos, binaryProvidersInfo.rootTreeElement);
        return providersInfos;
    }

    @Override
    public StorageInfo getStorageInfoSummary() {
        List<BinaryTreeElement<BinaryProviderInfo>> providersInfos = getBinaryTreeElementsList();
        // Quota is for the final NFS if exists, but if final is infinite quota should be for cache fs binary provider
        // Take the smallest space available from all the providers that are not cache fs
        StorageInfo cacheFsStorageInfo = null;
        StorageInfo smallestStorageInfo = null;
        for (BinaryTreeElement<BinaryProviderInfo> providerInfo : providersInfos) {
            if (providerInfo != null) {
                BinaryProviderInfo data = providerInfo.getData();
                StorageInfo storageInfo = data.getStorageInfo();
                String type = data.getProperties().get("type");
                if ("cache-fs".equals(type)) {
                    cacheFsStorageInfo = storageInfo;
                    // An EFS mount generates negative free space!
                    if (cacheFsStorageInfo.getFreeSpace() < 0L || cacheFsStorageInfo.getFreeSpaceInPercent() < 0L ||
                            cacheFsStorageInfo.getFreeSpaceInPercent() > 100L) {
                        // It's infinite
                        cacheFsStorageInfo = BinaryProviderBase.INFINITE_STORAGE_INFO;
                    }
                } else if (!BinaryProviderBase.EMPTY_STORAGE_INFO.equals(storageInfo)) {
                    if (smallestStorageInfo == null) {
                        smallestStorageInfo = storageInfo;
                    } else {
                        // An EFS mount generates negative free space!
                        if (storageInfo.getFreeSpaceInPercent() > 0L &&
                                smallestStorageInfo.getFreeSpaceInPercent() > storageInfo.getFreeSpaceInPercent()) {
                            // TODO: [by fsi] May be have the caller aware of many layers?
                            smallestStorageInfo = storageInfo;
                        }
                    }
                }
            }
        }
        if (smallestStorageInfo != null) {
            if (BinaryProviderBase.INFINITE_STORAGE_INFO.equals(smallestStorageInfo) ||
                    smallestStorageInfo.getFreeSpace() == Long.MAX_VALUE || smallestStorageInfo.getFreeSpace() == -1L) {
                // If there is a chae fs above the infinite storage use this to check quota
                if (cacheFsStorageInfo != null) {
                    return cacheFsStorageInfo;
                }
            }
            return smallestStorageInfo;
        }
        // No smallest found, if cache use it.
        if (cacheFsStorageInfo != null) {
            return cacheFsStorageInfo;
        }
        // No storage info relevant found, return unknown
        return BinaryProviderBase.UNKNOWN_STORAGE_INFO;
    }

    @Override
    @Nullable
    public BinaryInfo addBinaryRecord(String sha1, String sha2, String md5, long length) {
        try {
            //[sha2]: switch to sha2 when storage supports it
            BinaryEntity result = binariesDao.load(ChecksumType.sha1, sha1);
            if (result == null) {
                // It does not exists in the DB
                // Let's check if in bin provider
                BinaryElementRequestImpl request = new BinaryElementRequestImpl(sha1, sha2, md5, length);
                if (binaryProvider.exists(request).exists()) {
                    //info missing? pull binary from the store and recalculate checksums
                    sha2 = fillInMissingChecksumInfo(sha1, sha2);
                    //sanity!
                    if (isBlank(sha1) || isBlank(sha2) || isBlank(md5)) {
                        log.debug("Missing checksum information for binary with SHA1 '{}', SHA256 '{}', MD5 '{}'. " +
                                "It will not be added", sha1, sha2, md5);
                        return null;
                    }
                    // Good let's use it
                    return getTransactionalMe().insertRecordInDb(sha1, sha2, md5, length);
                }
                return null;
            }
            return convertToBinaryInfo(result);
        } catch (SQLException e) {
            throw new StorageException("Could not reserve entry '" + sha1 + "'", e);
        }
    }

    /**
     * Used by {@link #addBinaryRecord} to complete missing sha2 data for any action the tries writing binary records
     * using already-existing binaries (i.e. checksum deploy, import
     */
    private String fillInMissingChecksumInfo(String sha1, String sha2) {
        //when we switch checksum type in the filestore a check for all checksum types must
        //be added here... for now the only possible scenario is export from version < 5.5.0
        //and import into version >= 5.5.0 which means there's no sha2 metadata for the file
        if (isNotBlank(sha2)) {
            return sha2;
        }
        //For import scenarios it would have been better to pull checksums from the source file, but for
        //the sake of over-complexity i'm doing a pull from the filestore.
        BinaryElement element = createBinaryElement(sha1, null, null, -1);
        element.addHeader(BinaryElementHeaders.SKIP_CACHE, "true");
        Checksum sha2Checksum = new Checksum(ChecksumType.sha256.alg(), ChecksumType.sha256.length());
        try (InputStream in = getBinary(element);
             ChecksumInputStream calcStream = new ChecksumInputStream(in, sha2Checksum)) {
            //waste the stream to have it calculate the checksum
            IOUtils.copy(calcStream, new NullOutputStream());
            calcStream.close();
            sha2 = sha2Checksum.getChecksum();
        } catch (Exception e) {
            log.error("Can't calculate missing checksum information for binary with SHA1 '{}' : {}", sha1,
                    e.getMessage());
            log.debug("", e);
        }
        return sha2;
    }

    @Override
    @Nonnull
    public BinaryInfo addBinary(BinaryStream in) throws IOException, BinaryRejectedException {
        if (in.getStream() instanceof BinaryServiceInputStream) {
            throw new IllegalStateException(
                    "Cannot add binary from checksum deploy " + ((BinaryServiceInputStream) in).getBinaryInfo());
        }
        BinaryInfo binaryInfo;
        BinaryElement bi = binaryProvider.addStream(in);
        log.trace("Inserted binary {} to file store", bi.getSha1());
        // From here we managed to create a binary record on the binary provider
        // So, failing on the insert in DB (because saving the file took to long)
        // can be re-tried based on the sha1
        try {
            binaryInfo = getTransactionalMe().insertRecordInDb(bi.getSha1(), bi.getSha2(), bi.getMd5(), bi.getLength());
        } catch (BinaryInsertRetryException e) {
            if (log.isDebugEnabled()) {
                log.info("Retrying add binary with SHA1: '" + bi.getSha1()
                        + "' and SHA256: '" + bi.getSha2() + "after receiving exception: ", e);
            } else {
                log.info("Retrying add binary with SHA1: '" + bi.getSha1()
                        + "' and SHA256: '" + bi.getSha2() + "after receiving exception: " + e.getMessage());
            }
            binaryInfo = addBinaryRecord(bi.getSha1(), bi.getSha2(), bi.getMd5(), bi.getLength());
            if (binaryInfo == null) {
                throw new StorageException("Failed to add binary record with SHA1: '" + bi.getSha1()
                        + "' and SHA256: '" + bi.getSha2() + "' during retry", e);
            }
        }
        return binaryInfo;
    }

    @Override
    public BinaryProvidersInfo<Map<String, String>> getBinaryProvidersInfo() {
        return this.getBinaryProvidersInfo(true);
    }

    private BinaryProvidersInfo<Map<String, String>> getBinaryProvidersInfo(boolean useCache) {
        // Get binary providers info tree from the binary store manager
        BinaryProvidersInfo binaryProvidersInfo = binaryProviderManager.getBinaryProvidersInfo();
        BinaryTreeElement<BinaryProviderInfo> teeElement = binaryProvidersInfo.rootTreeElement;
        // Create sub tree that contains only essential elements (for the UI)
        BinaryTreeElementScanner<BinaryProviderInfo, Map<String, String>> scanner = new BinaryTreeElementScanner<>();
        EssentialBinaryTreeElementHandler handler = new EssentialBinaryTreeElementHandler();
        BinaryTreeElement<Map<String, String>> scan = scanner.scan(teeElement, handler);
        return new BinaryProvidersInfo<>(binaryProvidersInfo.template, scan);
    }

    @Override
    public InputStream getBinary(String sha1) {
        return new ReaderTrackingInputStream(binaryProvider.getStream(new BinaryElementRequestImpl(sha1)), sha1, this);
    }

    @Override
    public InputStream getBinary(BinaryInfo bi) {
        BinaryElementRequestImpl request = new BinaryElementRequestImpl(bi.getSha1(), bi.getSha2(), bi.getMd5(),
                bi.getLength());
        if (!binaryProvider.exists(request).exists()) {
            return null;
        }
        return new BinaryServiceInputStreamWrapper(bi, this);
    }

    @Override
    public InputStream getBinary(BinaryElement element) throws BinaryNotFoundException {
        //BE CAREFUL! users of this method currently supplies the sha1 value
        return new ReaderTrackingInputStream(binaryProvider.getStream(new BinaryElementRequestImpl(element)),
                element.getSha1(), this);
    }

    @Override
    public String getSignedUrl(RepoResourceInfo info, String userName) {
        CloudBinaryProvider cloudProvider = getCloudBinaryProvider();
        if (cloudProvider == null) {
            throw new SignedUrlException("Couldn't generate signed url for " + info.getRepoPath().getId() + ". " +
                    "There is no binary cloud provider configured. Check the logs for more details.");
        }
        String signedUrl = cloudProvider.getSignedUrl(new BinaryElementRequestImpl(info.getSha1(), getHeaderMapForRedirect(info, userName)));
        if (signedUrl == null) {
            throw new SignedUrlException("Couldn't generate signed url for " + info.getRepoPath().getId() + ". " +
                    "Make sure " + cloudProvider+ " binary providers is configured to support signed urls and check" +
                    "the logs for more details.");
        }
        log.trace("Generated signed url: {}", signedUrl);
        return signedUrl;
    }

    private static Map<String, String> getHeaderMapForRedirect(RepoResourceInfo resourceInfo, String userName) {
        RepoPath resourceRepoPath = resourceInfo.getRepoPath();
        Map<String, String> headers = Maps.newHashMap();
        if (resourceRepoPath != null) {
            headers.put(ARTIFACT_NAME.getHeaderName(), resourceRepoPath.getName());
            headers.put(CONTENT_TYPE.getHeaderName(),
                    NamingUtils.getMimeType(resourceInfo.getRepoPath().getPath()).getType());
        }
        if (StringUtils.isNotEmpty(userName)) {
            headers.put(USER_NAME.getHeaderName(), userName);
        }
        return headers;
    }

    @Override
    public boolean canCloudProviderGenerateRedirection(String sha1) {
        CloudBinaryProvider cloudProvider = getCloudBinaryProvider();
        if (cloudProvider == null) {
            return false;
        }
        return cloudProvider.canGenerateRedirection(new BinaryElementRequestImpl(sha1));
    }

    private CloudBinaryProvider getCloudBinaryProvider() {
        return (CloudBinaryProvider) binaryProviderManager.getNextCloudProvider();
    }

    @Override
    public BinaryInfo findBinary(ChecksumType checksumType, String checksum) {
        try {
            BinaryEntity result = binariesDao.load(checksumType, checksum);
            if (result != null) {
                return convertToBinaryInfo(result);
            }
        } catch (SQLException e) {
            throw new StorageException(
                    "Storage error loading checksum ( " + checksumType.name() + "): '" + checksum + "'", e);
        }
        return null;
    }

    @Nonnull
    @Override
    public Set<BinaryInfo> findBinaries(@Nullable Collection<String> checksums) {
        Set<BinaryInfo> results = Sets.newHashSet();
        if (checksums == null || checksums.isEmpty()) {
            return results;
        }
        try {
            for (ChecksumType checksumType : ChecksumType.BASE_CHECKSUM_TYPES) {
                Collection<String> validChecksums = extractValid(checksumType, checksums);
                if (!validChecksums.isEmpty()) {
                    binariesDao.search(checksumType, validChecksums).stream()
                            .map(this::convertToBinaryInfo)
                            .forEach(results::add);
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Could not search for checksums " + checksums, e);
        }
        return results;
    }

    @Override
    public GarbageCollectorInfo runFullGarbageCollect() {
        final GarbageCollectorInfo result = new GarbageCollectorInfo();
        notifyGCListenersOnStart(result);

        Collection<BinaryEntity> binsToDelete;
        try {
            BinariesInfo countAndSize = binariesDao.getCountAndTotalSize();
            result.initialCount = countAndSize.getBinariesCount();
            result.initialSize = countAndSize.getBinariesSize();
            binsToDelete = binariesDao.findPotentialDeletion();
        } catch (SQLException e) {
            throw new StorageException("Could not find potential Binaries to delete!", e);
        }
        result.stopScanTimestamp = System.currentTimeMillis();
        result.candidatesForDeletion = binsToDelete.size();
        if (result.candidatesForDeletion > 0) {
            log.info("Found {} candidates for deletion", result.candidatesForDeletion);
        }
        List<String> deletedBinaries = deleteBinaries(result, binsToDelete);
        deleteBlobInfos(deletedBinaries);
        if (result.checksumsCleaned.get() > 0) {
            result.archivePathsCleaned.set(getTransactionalMe().deleteUnusedArchivePaths());
            result.archiveNamesCleaned.set(getTransactionalMe().deleteUnusedArchiveNames());
        }

        result.gcEndTime = System.currentTimeMillis();

        try {
            BinariesInfo countAndSize = binariesDao.getCountAndTotalSize();
            result.printCollectionInfo(countAndSize.getBinariesSize());
        } catch (SQLException e) {
            log.error("Could not list files due to " + e.getMessage());
        }
        boolean success = binaryProviderManager.optimize(forceBinaryProviderOptimizationOnce);
        if (success) {
            forceBinaryProviderOptimizationOnce = false;
        }
        return result;
    }

    public void deleteBlobInfos(List<String> deletedBinaries) {
        if (CollectionUtils.notNullOrEmpty(deletedBinaries)) {
            deleteBlobInfosWrapper.deleteBlobInfosAsync(deletedBinaries);
        }
    }

    @Override
    public void startGarbageCollect() {
        log.info("Triggering Garbage Collection");
        int skipFullGcCount = ConstantValues.gcSkipFullGcBetweenMinorIterations.getInt();
        boolean isTwentyIteration = gcIteration.incrementAndGet() % skipFullGcCount == 0;
        try {
            TrashService trashService = ContextHelper.get().beanForType(TrashService.class);
            if (trashService.isTrashcanEnabled()) {
                if (isTwentyIteration) {
                    runAllGarbageCollectStrategies();
                } else {
                    runTrashAndBinariesGarbageCollect();
                }
            } else {
                runFullGarbageCollect();
            }
        } finally {
            if (isTwentyIteration) {
                gcIteration.set(0);
            }
        }
    }

    public void runAllGarbageCollectStrategies() {
        runTrashAndBinariesGarbageCollect();
        runFullGarbageCollect();
    }

    private void runTrashAndBinariesGarbageCollect() {
        BinariesGarbageCollectorService binariesGarbageCollectorService = ContextHelper.get()
                .beanForType(BinariesGarbageCollectorService.class);
        binariesGarbageCollectorService.startGCByStrategy(GarbageCollectorStrategy.TRASH_AND_BINARIES);
    }

    private List<String> deleteBinaries(GarbageCollectorInfo result, Collection<BinaryEntity> binsToDelete) {
        int failures = 0;
        List<String> deletedBinaries = Lists.newArrayList();
        for (BinaryEntity bd : binsToDelete) {
            log.trace("Candidate for deletion: {}", bd);
            try {
                boolean deleted = dbService.invokeInTransaction("BinaryCleaner#" + bd.getSha1(), new BinaryCleaner(bd, result));
                if (deleted) {
                    deletedBinaries.add(bd.getSha2());
                }
            } catch (Exception e) {
                failures++;
                String msg = "Caught Exception, trying to clean {} : {}";
                if (failures >= ConstantValues.gcFailCountThreshold.getInt()) {
                    // We're past the allowed fail threshold, fail gc.
                    log.error("{}. Aborting Garbage Collection Run. {}, {}", msg, bd.getSha1(), e.getMessage());
                    log.debug("", e);
                    break;
                } else {
                    log.debug(msg, bd.getSha1(), e.getMessage());
                }
            }
        }
        return deletedBinaries;
    }

    /**
     * Deletes binary row and all dependent rows from the database
     *
     * @param sha1ToDelete Checksum to delete
     * @return True if deleted. False if not found or error
     */
    private boolean deleteEntry(String sha1ToDelete) {
        boolean hadArchiveEntries;
        try {
            hadArchiveEntries = archiveEntriesService.deleteArchiveEntries(sha1ToDelete);
        } catch (Exception e) {
            log.error("Failed to delete archive entries for " + sha1ToDelete, e);
            return false;
        }
        try {
            boolean entryDeleted = binariesDao.deleteEntry(sha1ToDelete) == 1;
            if (!entryDeleted && hadArchiveEntries) {
                log.error("Binary entry " + sha1ToDelete + " had archive entries that are deleted," +
                        " but the binary line was not deleted! Re indexing of archive needed.");
            }
            return entryDeleted;
        } catch (SQLException e) {
            log.error("Could execute delete from binary store of " + sha1ToDelete, e);
        }
        return false;
    }

    @Override
    public int deleteUnusedArchivePaths() {
        try {
            log.debug("Deleting unused archive paths");
            return archiveEntriesService.deleteUnusedPathIds();
        } catch (StorageException e) {
            log.error("Failed to delete unique paths: {}", e.getMessage());
            log.debug("Failed to delete unique paths", e);
            return 0;
        }
    }

    @Override
    public int deleteUnusedArchiveNames() {
        try {
            log.debug("Deleting unused archive names");
            return archiveEntriesService.deleteUnusedNameIds();
        } catch (StorageException e) {
            log.error("Failed to delete unique archive names: {}", e.getMessage());
            log.debug("Failed to delete unique archive paths", e);
            return 0;
        }
    }

    @Override
    public int incrementNoDeleteLock(String sha1) {
        Pair<AtomicInteger, Long> pair = deleteProtectedBinaries
                .putIfAbsent(sha1, new Pair<>(new AtomicInteger(1), System.currentTimeMillis()));
        if (pair == null) {
            return 1;
        } else {
            pair.setSecond(System.currentTimeMillis());
            return pair.getFirst().incrementAndGet();
        }
    }

    @Override
    public void decrementNoDeleteLock(String sha1) {
        AtomicInteger usageCount = deleteProtectedBinaries.get(sha1).getFirst();
        if (usageCount != null) {
            usageCount.decrementAndGet();
        }
    }

    @Override
    public boolean updateSha2ForSha1(String targetSha1, String newSha2) throws SQLException {
        return binariesDao.insertSha2(targetSha1, newSha2);
    }

    @Override
    public Collection<BinaryInfo> findAllBinaries() {
        try {
            Collection<BinaryEntity> allBinaries = binariesDao.findAll();
            List<BinaryInfo> result = new ArrayList<>(allBinaries.size());
            result.addAll(allBinaries.stream().map(this::convertToBinaryInfo).collect(Collectors.toList()));
            return result;
        } catch (SQLException e) {
            throw new StorageException("Could not retrieve all binary entries", e);
        }
    }

    @Override
    @Nonnull
    public BinaryInfo insertRecordInDb(String sha1, String sha2, String md5, long length) throws StorageException {
        BinaryEntityWithValidation dataRecord = new BinaryEntityWithValidation(sha1, sha2, md5, length);
        if (!dataRecord.isValid()) {
            throw new StorageException("Cannot insert invalid binary record: " + dataRecord);
        }
        try {
            //[sha2]: validate by sha2 as well? can find same-sha1-different-sha2 rows like this and fail uploads
            boolean binaryExists = binariesDao.exists(ChecksumType.sha1, sha1);
            if (!binaryExists) {
                createDataRecord(dataRecord, sha1);
            }
            // Always reselect from DB before returning
            BinaryEntity binary = binariesDao.load(ChecksumType.sha1, sha1);
            if (binary == null) {
                throw new StorageException("Could not find just inserted binary record: " + dataRecord);
            }
            if (StringUtils.isBlank(binary.getSha2()) && StringUtils.isNotBlank(sha2)) {
                // This could happen in case the binary was deployed before we stored sha2 in Artifactory
                binariesDao.insertSha2(binary.getSha1(), sha2);
                binary = binariesDao.load(ChecksumType.sha1, sha1);
            }
            return convertToBinaryInfo(binary);
        } catch (SQLException e) {
            throw new StorageException("Failed to insert new binary record: " + e.getMessage(), e);
        }
    }

    /**
     * @return Number of binaries and total size stored in the binary store
     */
    @Override
    public BinariesInfo getBinariesInfo() {
        try {
            return binariesDao.getCountAndTotalSize();
        } catch (SQLException e) {
            throw new StorageException("Could not calculate total size due to " + e.getMessage(), e);
        }
    }

    @Override
    public long getStorageSize() {
        return getBinariesInfo().getBinariesSize();
    }

    @Override
    public void ping() {
        // Ping storage
        try {
            binaryProviderManager.ping();
        } catch (BinaryStorageException bse) {
            log.warn("Binary provider failed ping attempt: {}", bse.getMessage());
            log.debug("", bse);
            throw bse;
        }
        // Ping DB
        try {
            if (binariesDao.exists(ChecksumType.sha1, "does not exists")) {
                throw new StorageException("Select entry fails");
            }
        } catch (SQLException e) {
            throw new StorageException("Accessing Binary Store DB failed with " + e.getMessage(), e);
        }
    }

    @Override
    public void prune(BasicStatusHolder statusHolder) {
        boolean locked = lock.tryLock();
        if (locked) {
            try {
                FilestorePruner pruner =
                        new FilestorePruner((BinaryProviderBase) binaryProviderManager.getFirstBinaryProvider(),
                                this::isActivelyUsed, BinaryServiceUtils.binariesDaoSearch(binariesDao), statusHolder);
                pruner.prune();
            } finally {
                lock.unlock();
            }
        } else {
            throw new PruneException("The prune process is already running");
        }
    }

    /**
     * @param sha1 sha1 checksum of the binary to check
     * @return True if the given binary is currently used by a reader (e.g., open stream) or writer
     */
    @Override
    public boolean isActivelyUsed(String sha1) {
        Pair<AtomicInteger, Long> pair = deleteProtectedBinaries.get(sha1);
        return pair != null && pair.getFirst().get() > 0;
    }

    private Collection<String> extractValid(ChecksumType checksumType, Collection<String> checksums) {
        return checksums.stream()
                .filter(checksumType::isValid)
                .collect(Collectors.toSet());
    }

    private InternalBinaryService getTransactionalMe() {
        return ContextHelper.get().beanForType(InternalBinaryService.class);
    }

    private BinaryInfo convertToBinaryInfo(BinaryEntity bd) {
        return new BinaryInfoImpl(bd.getSha1(), bd.getSha2(), bd.getMd5(), bd.getLength());
    }

    private void notifyGCListenersOnStart(GarbageCollectorInfo info) {
        for (GarbageCollectorListener garbageCollectorListener : garbageCollectorListeners) {
            garbageCollectorListener.start(info);
        }
    }

    private void notifyGCListenersOnDestroy() {
        garbageCollectorListeners.forEach(GarbageCollectorListener::destroy);
    }

    @Override
    public List<String> getAndManageErrors() {
        List<String> errors = binaryProviderManager.getErrors();
        if (errors.size() > 0) {
            forceOptimizationOnce();
        }
        return errors;
    }

    @Override
    public void forceOptimizationOnce() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        HaCommonAddon haCommonAddon = addonsManager.addonByType(HaCommonAddon.class);
        if (haCommonAddon.isHaEnabled() && !haCommonAddon.isPrimary()) {
            haCommonAddon.forceOptimizationOnce();
        } else {
            forceBinaryProviderOptimizationOnce = true;
        }
    }

    @Override
    public BinaryElement createBinaryElement(String sha1, String sha2, String md5, long length) {
        return binaryProviderManager.createBinaryElement(sha1, sha2, md5, length);
    }

    @Override
    public BinaryStream createBinaryStream(InputStream in, @Nullable ChecksumsInfo info) {
        return binaryProviderManager.createBinaryStream(in, getExpectedChecksumHeaders(info));
    }

    /**
     * Incoming checksum info for new files can either be client checksum populated by the upload mechanism,
     * trusted if required by the uploader (i.e. ui uploads) where no checksum validation is done on the client side,
     * or empty if no such information was provided.
     * The {@param info} is used to indicate expected checksums for the Binarystore which will fail to save a binary
     * on validation error.
     *
     * {@see ConstantValues.failUploadOnChecksumValidationError} controls whether validation information is passed to
     * the Binarystore
     */
    @Nonnull
    private Map<String, String> getExpectedChecksumHeaders(@Nullable ChecksumsInfo info) {
        Map<String, String> headers = Maps.newHashMap();
        //This flag should only be set to false if I messed up and users need to skip validations in an emergency :(
        if (info != null && ConstantValues.failUploadOnChecksumValidationError.getBoolean()) {
            addExpectedChecksumHeader(CHECKSUM_SHA1.getHeaderName(), info.getChecksumInfo(ChecksumType.sha1), headers);
            addExpectedChecksumHeader(CHECKSUM_SHA256.getHeaderName(), info.getChecksumInfo(ChecksumType.sha256),
                    headers);
            addExpectedChecksumHeader(CHECKSUM_MD5.getHeaderName(), info.getChecksumInfo(ChecksumType.md5), headers);
        }
        return headers;
    }

    /**
     * Adds {@param headerName} to the headers map if the {@param checksumInfo} contains data,
     * and the original (client / expected) checksum is not {@see ChecksumInfo.TRUSTED_FILE_MARKER}, which cannot be used
     * for validating the actual checksum of a binary
     */
    private void addExpectedChecksumHeader(String headerName, @Nullable ChecksumInfo checksumInfo,
            Map<String, String> headers) {
        String expectedChecksum = checksumInfo == null ? null : checksumInfo.getOriginal();
        if (StringUtils.isNotBlank(expectedChecksum) && !TRUSTED_FILE_MARKER.equals(expectedChecksum)) {
            headers.put(headerName, expectedChecksum);
        }
    }

    private void createDataRecord(BinaryEntity dataRecord, String sha1) throws SQLException {
        // insert a new binary record to the db
        try {
            binariesDao.create(dataRecord);
        } catch (SQLException e) {
            if (isDuplicatedEntryException(e)) {
                log.debug("Simultaneous insert of binary {} detected, binary will be checked.", sha1, e);
                throw new BinaryInsertRetryException(convertToBinaryInfo(dataRecord), e);
            } else {
                throw e;
            }
        }
    }

    public boolean isFileExist(String sha1) {
        return binaryProvider.exists(new BinaryElementRequestImpl(sha1)).exists();
    }

    @Override
    public void onContextCreated() {
        binaryProviderManager.contextCreated();
    }

    @Override
    public void clusterTopologyChanged(List<ArtifactoryServer> activeNodes) {
        List<ClusterNode> active = activeNodes.stream()
                .filter(Objects::nonNull)
                .map(BinaryServiceUtils::clusterNodeFromArtServer)
                .collect(Collectors.toList());
        binaryProviderManager.clusterChanged(active);
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {

    }

    public boolean executeBinaryCleaner(GCCandidate gcCandidate, GarbageCollectorInfo result) {
        return dbService.invokeInTransaction("BinaryCleaner#" + gcCandidate.getSha1(),
                new BinaryCleaner(new BinaryEntity(gcCandidate), result));
    }

    @Override
    public boolean isCloudProviderConfigured(){
        return getCloudBinaryProvider() != null;
    }

    private boolean exists(String sha1) {
        try {
            return binariesDao.exists(ChecksumType.sha1, sha1);
        } catch (SQLException sql) {
            log.debug("Failed existence check of path " + sha1, sql);
            throw new BinaryStorageException("Failed existence check of checksum " + sha1 + ": " + sql.getMessage(),
                    sql, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private AccessClient getAccessClient() {
        return accessService.getAccessClient();
    }

    /**
     * Deletes a single binary from the database and filesystem if not in use.
     */
    class BinaryCleaner implements Callable<Boolean> {
        private final GarbageCollectorInfo result;
        private final BinaryEntity bd;

        BinaryCleaner(BinaryEntity bd, GarbageCollectorInfo result) {
            this.result = result;
            this.bd = bd;
        }

        @Override
        public Boolean call() throws Exception {
            boolean binaryDeleted = false;
            String sha1 = bd.getSha1();
            deleteProtectedBinaries.putIfAbsent(sha1, new Pair<>(new AtomicInteger(0), System.currentTimeMillis()));
            Pair<AtomicInteger, Long> pair = deleteProtectedBinaries.get(sha1);
            if (pair.getFirst().compareAndSet(0, -30)) {
                log.debug("Targeting '{}' for deletion as it not seems to be used", sha1);
                try {
                    if (deleteEntry(sha1)) {
                        log.trace("Deleted {} record from binaries table", sha1);
                        result.checksumsCleaned.incrementAndGet();
                        if (binaryProvider.delete(new BinaryElementRequestImpl(sha1))) {
                            binaryDeleted = true;
                            log.trace("Deleted {} binary", sha1);
                            result.binariesCleaned.incrementAndGet();
                            result.totalSizeCleaned.addAndGet(bd.getLength());
                        } else {
                            log.error("Could not delete binary '{}'", sha1);
                        }
                    } else {
                        log.debug("Deleting '{}' has failed", sha1);
                    }
                } finally {
                    // remove delete protection (even if delete was not successful)
                    deleteProtectedBinaries.remove(sha1);
                    log.debug("Cleaning '{}' from ref. counter", sha1);
                }
            } else {
                Long timestamp = pair.getSecond();
                log.debug("Binary {} has {} readers with last timestamp of {}", sha1, pair.getFirst().get(), timestamp);
                long trashTime = (System.currentTimeMillis() - timestamp) / 1000;
                if (trashTime > ConstantValues.gcReadersMaxTimeSecs.getLong()) {
                    log.debug("Binary {} has reached it's max read time, removing it from ref. counter", sha1);
                    deleteProtectedBinaries.remove(sha1);
                } else {
                    log.debug("Binary {} is being read! Not deleting.", sha1);
                }
            }
            return binaryDeleted;
        }
    }

    private class ArtifactoryLockingMapFactoryProvider implements LockingMapFactoryProvider {
        private ConflictsGuard conflictsGuard;

        @Override
        public ConflictsGuard getConflictsGuard() {
            if (conflictsGuard == null) {
                conflictsGuard = ContextHelper.get().beanForType(AddonsManager.class)
                        .addonByType(HaCommonAddon.class).getConflictsGuard("binarystore");
            }
            return conflictsGuard;
        }
    }
}
