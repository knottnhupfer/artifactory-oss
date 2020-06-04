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

package org.artifactory.repo.service;

import com.google.common.collect.*;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.WebstartAddon;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.config.RepositoryImportSettingsImpl;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.maven.MavenMetadataService;
import org.artifactory.api.maven.MavenMetadataWorkItem;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.VersionUnit;
import org.artifactory.api.repo.ArchiveFileContent;
import org.artifactory.api.repo.Async;
import org.artifactory.api.repo.ResearchService;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.repo.storage.FolderSummeryInfo;
import org.artifactory.api.request.UploadService;
import org.artifactory.api.rest.constant.RepositoriesRestConstants;
import org.artifactory.api.rest.constant.RestConstants;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.api.search.VersionSearchResults;
import org.artifactory.api.search.deployable.VersionUnitSearchControls;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.storage.StorageQuotaInfo;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.common.StatusHolder;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.exception.CancelException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.*;
import org.artifactory.importexport.ImportIsDisabledException;
import org.artifactory.info.InfoWriter;
import org.artifactory.io.StringResourceStreamHandle;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.*;
import org.artifactory.repo.cleanup.FolderPruningService;
import org.artifactory.repo.count.ArtifactCountRetriever;
import org.artifactory.repo.db.DbLocalRepo;
import org.artifactory.repo.db.importexport.DbRepoExportSearchHandler;
import org.artifactory.repo.interceptor.StorageInterceptors;
import org.artifactory.repo.local.PathDeletionContext;
import org.artifactory.repo.local.ValidDeployPathContext;
import org.artifactory.repo.mbean.ManagedRepository;
import org.artifactory.repo.service.deploy.ArtifactoryDeployRequest;
import org.artifactory.repo.service.deploy.ArtifactoryDeployRequestBuilder;
import org.artifactory.repo.service.flexible.context.MoveCopyContext;
import org.artifactory.repo.service.flexible.interfaces.FlatMoveCopyService;
import org.artifactory.repo.service.mover.*;
import org.artifactory.repo.service.versioning.RepositoriesCache;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.request.InternalArtifactoryResponse;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.request.NullRequestContext;
import org.artifactory.request.RepoRequests;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.ResolvedResource;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.sapi.common.BaseSettings;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.schedule.*;
import org.artifactory.search.InternalSearchService;
import org.artifactory.security.*;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.LicenseEventListener;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.DBEntityNotFoundException;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.binstore.service.BinaryInfo;
import org.artifactory.storage.binstore.service.BinaryService;
import org.artifactory.storage.db.binstore.service.BinaryServiceInputStream;
import org.artifactory.storage.db.security.service.VersioningCacheImpl;
import org.artifactory.storage.fs.*;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.ItemMetaInfo;
import org.artifactory.storage.fs.service.MigrationFileService;
import org.artifactory.storage.fs.service.NodeMetaInfoService;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemTree;
import org.artifactory.storage.fs.tree.TreeBrowsingCriteria;
import org.artifactory.storage.fs.tree.TreeBrowsingCriteriaBuilder;
import org.artifactory.storage.jobs.StatsDelegatingServiceFlushJob;
import org.artifactory.storage.jobs.StatsPersistingServiceFlushJob;
import org.artifactory.storage.jobs.migration.pathchecksum.RepoPathChecksumCalculationWorkItem;
import org.artifactory.storage.jobs.migration.pathchecksum.RepoPathChecksumMigrationJob;
import org.artifactory.storage.jobs.migration.pathchecksum.RepoPathChecksumMigrationJobDelegate;
import org.artifactory.storage.jobs.migration.sha256.ChecksumCalculationWorkItem;
import org.artifactory.storage.jobs.migration.sha256.Sha256CalculationFatalException;
import org.artifactory.storage.jobs.migration.sha256.Sha256MigrationJob;
import org.artifactory.storage.jobs.migration.sha256.Sha256MigrationJobDelegate;
import org.artifactory.storage.service.StatsServiceImpl;
import org.artifactory.storage.spring.StorageContextHelper;
import org.artifactory.util.*;
import org.artifactory.util.distribution.DistributionConstants;
import org.artifactory.version.CompoundVersionDetails;
import org.codehaus.jackson.type.TypeReference;
import org.jfrog.common.StreamSupportUtils;
import org.jfrog.common.config.diff.DataDiff;
import org.jfrog.storage.binstore.exceptions.BinaryRejectedException;
import org.jfrog.storage.binstore.exceptions.BinaryStorageException;
import org.jfrog.storage.binstore.ifc.BinaryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.artifactory.api.security.SecurityService.USER_SYSTEM;
import static org.artifactory.descriptor.repo.SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME;
import static org.artifactory.util.distribution.DistributionConstants.EDGE_UPLOADS_REPO_KEY;

@Service
@Reloadable(beanClass = InternalRepositoryService.class,
        initAfter = {StorageInterceptors.class, InternalCentralConfigService.class, TaskService.class},
        listenOn = {
                CentralConfigKey.localRepositoriesMap,
                CentralConfigKey.virtualRepositoriesMap,
                CentralConfigKey.remoteRepositoriesMap,
                CentralConfigKey.distributionRepositoriesMap,
                CentralConfigKey.releaseBundlesRepositoriesMap,
                CentralConfigKey.propertySets,
                CentralConfigKey.offlineMode,
                CentralConfigKey.xrayConfig
        })
public class RepositoryServiceImpl implements InternalRepositoryService, LicenseEventListener {
    private static final Logger log = LoggerFactory.getLogger(RepositoryServiceImpl.class);

    private static final String REPOSITORIES_MBEAN_TYPE = "Repositories";
    public static final RepoPath IN_TRANSIT_REPO_PATH = RepoPathFactory.create(DistributionConstants.IN_TRANSIT_REPO_KEY, "");

    @Autowired
    private AclService aclService;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MavenMetadataService mavenMetadataService;

    @Autowired
    private InternalSearchService searchService;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private StatsServiceImpl statsService;

    @Autowired
    private FileService fileService;

    @Autowired
    private BinaryService binaryService;

    @Autowired
    private FolderPruningService pruneService;

    @Autowired
    private FlatMoveCopyService flatMoveCopyService;

    @Autowired
    private CachedThreadPoolTaskExecutor cachedThreadPoolTaskExecutor;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private BuildService buildService;

    @Autowired
    private ResearchService researchService;

    //Holds providers for specialized remote repos
    private Map<RepoType, HttpRepoFactory> remoteRepoProviders;

    private ArtifactCountRetriever artifactCountRetriever;

    private VersioningCacheImpl<RepositoriesCache> cache;

    private CloseableHttpClient client;


    private static InternalRepositoryService getTransactionalMe() {
        return InternalContextHelper.get().beanForType(InternalRepositoryService.class);
    }

    private void initClient() {
        ProxyDescriptor proxy = InternalContextHelper.get().getCentralConfig().getDescriptor().getDefaultProxy();
        artifactCountRetriever = new ArtifactCountRetriever(fileService, cachedThreadPoolTaskExecutor);
        HttpClientConfigurator configurator = new HttpClientConfigurator();
        configurator.socketTimeout(15000).connectionTimeout(15000).retry(0, false);
        client = configurator.proxy(proxy).build();
    }

    @Override
    public int countFiles(String repoKey, String fileName) {
        int result = -1;
        log.debug("Fetching number of images in repoKey:{} ", repoKey);
        long startTime = System.currentTimeMillis();
        try {
            result = fileService.getFileCount(repoKey, fileName);
        } catch (Exception e) {
            log.error("Error during fetching number of images of repository:{} due to:{}", repoKey, e.getMessage());
            log.error("", e);
        }
        log.debug("Finished fetching number of images from:{}, time took:{} millis", repoKey, System.currentTimeMillis() - startTime);
        return result;
    }


    @Override
    public void init() {
        remoteRepoProviders = InternalContextHelper.get().beansForType(HttpRepoFactory.class).values()
                .stream()
                .collect(Collectors.toMap(HttpRepoFactory::getRepoType, Function.identity()));
        if (cache == null) {
            long timeout = ConstantValues.repositoriesDirtyReadsTimeoutMillis.getLong();
            cache = new VersioningCacheImpl<>(timeout, new RepositoriesCacheLoader());
        }
        HttpUtils.resetArtifactoryUserAgent();

        try {
            //Dump info to the log
            new InfoWriter().writeInfo();
        } catch (Exception e) {
            log.warn("Failed dumping system info", e);
        }

        // register internal statistics flushing job
        TaskBase localStatsFlushTask = TaskUtils.createRepeatingTask(StatsPersistingServiceFlushJob.class,
                TimeUnit.SECONDS.toMillis(ConstantValues.statsFlushIntervalSecs.getLong()),
                TimeUnit.SECONDS.toMillis(ConstantValues.statsFlushIntervalSecs.getLong()));

        // register remote statistics flushing job
        TaskBase remoteFlushTask = TaskUtils.createRepeatingTask(StatsDelegatingServiceFlushJob.class,
                TimeUnit.SECONDS.toMillis(ConstantValues.statsRemoteFlushIntervalSecs.getLong()),
                TimeUnit.SECONDS.toMillis(ConstantValues.statsRemoteFlushIntervalSecs.getLong()));

        taskService.startTask(localStatsFlushTask, false);
        taskService.startTask(remoteFlushTask, false);
        if (!ConstantValues.disableGlobalRepoAccess.getBoolean()) {
            log.warn("The global virtual repository 'repo' has been deprecated, requests to '/repo' will be treated " +
                    "normally.");
        }
        initClient();
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        HttpUtils.resetArtifactoryUserAgent();
        deleteOrphanRepos(oldDescriptor);
        cleanAndPromote();
        checkAndCleanChangedVirtualPomCleanupPolicy(oldDescriptor);
    }

    private void cleanAndPromote() {
        cache.get().remoteRepositoriesMap.values().forEach(RemoteRepo::cleanupResources);
        cache.promoteVersion();
    }

    @Override
    @Async(authenticateAsSystem = true)
    public void onContextReady() {
        registerRepositoriesMBeans();
    }

    @Override
    public void onContextCreated() {
        startSha2CalculationJob();
        startRepoPathChecksumJob();
    }

    private void checkAndCleanChangedVirtualPomCleanupPolicy(CentralConfigDescriptor oldDescriptor) {
        Map<String, VirtualRepoDescriptor> oldVirtualDescriptors = oldDescriptor.getVirtualRepositoriesMap();
        List<VirtualRepoDescriptor> newVirtualDescriptors = getVirtualRepoDescriptors();
        for (VirtualRepoDescriptor newDescriptor : newVirtualDescriptors) {
            String repoKey = newDescriptor.getKey();
            VirtualRepoDescriptor oldVirtualDescriptor = oldVirtualDescriptors.get(repoKey);
            if (oldVirtualDescriptor != null && pomCleanUpPolicyChanged(newDescriptor, oldVirtualDescriptor)) {
                VirtualRepo virtualRepo = virtualRepositoryByKey(repoKey);
                if (virtualRepo != null) {
                    log.info("Pom Repository Reference Cleanup Policy changed in '{}', cleaning repository cache. ",
                            repoKey);
                    RepoPath rootPath = InternalRepoPathFactory.repoRootPath(repoKey);
                    virtualRepo.undeploy(new DeleteContext(rootPath));
                } else {
                    log.warn("Unable to cleanup poms from the non-existing virtual repository '{}'", repoKey);
                }
            }
        }
    }

    private boolean pomCleanUpPolicyChanged(VirtualRepoDescriptor newDescriptor, VirtualRepoDescriptor oldDescriptor) {
        PomCleanupPolicy newPolicy = newDescriptor.getPomRepositoryReferencesCleanupPolicy();
        PomCleanupPolicy oldPolicy = oldDescriptor.getPomRepositoryReferencesCleanupPolicy();
        return !newPolicy.equals(oldPolicy);
    }

    private void deleteOrphanRepos(CentralConfigDescriptor oldDescriptor) {
        CentralConfigDescriptor currentDescriptor = centralConfigService.getDescriptor();
        Set<String> newRepoKeys = getConfigRepoKeys(currentDescriptor);
        Set<String> oldRepoKeys = getConfigRepoKeys(oldDescriptor);
        for (String key : oldRepoKeys) {
            if (!newRepoKeys.contains(key)) {
                log.debug("Removing the no-longer-referenced repository {}", key);
                StatusHolder statusHolder = deleteOrphanRepo(key);
                if (statusHolder.isError()) {
                    log.warn("Error occurred during repo '{}' removal: {}", key, statusHolder.getStatusMsg());
                }
            }
        }
    }

    //TORE: [by YS] delete from the db directly - there's no need for permissions checks, events etc.
    private StatusHolder deleteOrphanRepo(String repoKey) {
        BasicStatusHolder status = new BasicStatusHolder();
        StoringRepo storingRepo = storingRepositoryByKey(repoKey);
        if (storingRepo == null) {
            status.warn("Repo not found for deletion: " + repoKey, log);
            return status;
        } else if (storingRepo instanceof ReleaseBundlesRepo) {
            deleteRepoFromAllRelatedReleaseBundleAcls(repoKey);
        } else {
            deleteRepoFromAllRelatedRepoAcls(repoKey);
        }
        MutableVfsFolder rootFolder = storingRepo.getMutableFolder(storingRepo.getRepoPath(""));
        if (rootFolder == null) {
            status.warn("Root folder not found for deletion: " + repoKey, log);
            return status;
        }
        rootFolder.deleteIncludingRoot();
        return status;
    }

    private void deleteRepoFromAllRelatedRepoAcls(String repoKey) {
        //Delete all acl references to the repository being deleted
        List<RepoAcl> acls = aclService.getAllRepoAcls();
        for (RepoAcl repoAcl : acls) {
            MutableRepoPermissionTarget permissionTarget = InfoFactoryHolder.get().copyRepoPermissionTarget
                    (repoAcl.getPermissionTarget());
            String cachedRepoKey = repoKey.concat(LocalCacheRepoDescriptor.PATH_SUFFIX); //for remote repos
            List<String> repoKeys = permissionTarget.getRepoKeys();
            if (repoKeys.remove(repoKey) || repoKeys.remove(cachedRepoKey)) {
                MutableAcl<RepoPermissionTarget> mutableAclInfo = InfoFactoryHolder.get().copyRepoAcl(repoAcl);
                permissionTarget.setRepoKeys(repoKeys);
                mutableAclInfo.setPermissionTarget(permissionTarget);
                updateAcl(mutableAclInfo);
            }
        }
    }

    private void deleteRepoFromAllRelatedReleaseBundleAcls(String repoKey) {
        List<ReleaseBundleAcl> releaseBundleAcls = aclService.getAllReleaseBundleAcls(false);
        for (ReleaseBundleAcl releaseBundleAcl : releaseBundleAcls) {
            MutableReleaseBundlePermissionTarget permissionTarget = InfoFactoryHolder.get()
                    .copyReleaseBundlePermissionTarget(releaseBundleAcl.getPermissionTarget());
            List<String> repoKeys = permissionTarget.getRepoKeys();
            if (repoKeys.remove(repoKey)) {
                MutableAcl<ReleaseBundlePermissionTarget> mutableAclInfo = InfoFactoryHolder.get()
                        .copyReleaseBundleAcl(releaseBundleAcl);
                permissionTarget.setRepoKeys(repoKeys);
                mutableAclInfo.setPermissionTarget(permissionTarget);
                updateAcl(mutableAclInfo);
            }
        }
    }

    private void updateAcl(Acl<? extends RepoPermissionTarget> acl) {
        //In case that the DB returns null (e.g. permission target not found - already deleted by another thread),
        //the repository deletion process will not be aborted
        try {
            aclService.updateAcl(acl);
        } catch (DBEntityNotFoundException e) {
            log.warn(e.getMessage());
            log.debug("Permission target or ACL was not found in the database", e);
        }
    }

    private Set<String> getConfigRepoKeys(CentralConfigDescriptor descriptor) {
        Set<String> repoKeys = new HashSet<>();
        repoKeys.addAll(descriptor.getLocalRepositoriesMap().keySet());
        repoKeys.addAll(descriptor.getRemoteRepositoriesMap().keySet());
        repoKeys.addAll(descriptor.getVirtualRepositoriesMap().keySet());
        repoKeys.addAll(descriptor.getDistributionRepositoriesMap().keySet());
        repoKeys.addAll(descriptor.getReleaseBundlesRepositoriesMap().keySet());
        return repoKeys;
    }

    @Override
    public void destroy() {
        List<Repo> repos = Lists.newArrayList();
        repos.addAll(getVirtualRepositories());
        repos.addAll(getLocalAndRemoteRepositories());
        for (Repo repo : repos) {
            try {
                repo.destroy();
            } catch (Exception e) {
                log.error("Error while destroying the repository '{}'.", repo, e);
            }
        }
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }


    private RepoLayout getSimpleLayout(CentralConfigDescriptor configDescriptor) {
        return configDescriptor.getRepoLayouts().stream()
                .filter(repoLayout -> "simple-default".equals(repoLayout.getName()))
                .findFirst().orElse(null);
    }

    @Override
    public List<ItemInfo> getChildrenDeeply(RepoPath path) {
        List<ItemInfo> result = Lists.newArrayList();
        if (path == null) {
            return result;
        }
        if (!hasChildren(path)) {
            return result;
        }
        List<ItemInfo> children = getChildren(path);
        for (ItemInfo child : children) {
            result.add(child);
            result.addAll(getChildrenDeeply(child.getRepoPath()));
        }
        return result;
    }

    @Override
    public ModuleInfo getItemModuleInfo(RepoPath repoPath) {
        Repo repo = assertRepoKey(repoPath);
        return repo.getItemModuleInfo(repoPath.getPath());
    }

    private Repo assertRepoKey(RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        Repo repo = repositoryByKey(repoKey);
        if (repo == null) {
            throw new IllegalArgumentException("Repository '" + repoKey + "' not found!");
        }
        return repo;
    }

    @Override
    public boolean mkdirs(RepoPath folderRepoPath) {
        StoringRepo storingRepo = storingRepositoryByKey(folderRepoPath.getRepoKey());
        if (!storingRepo.itemExists(folderRepoPath.getPath())) {
            MutableVfsFolder folder = storingRepo.createOrGetFolder(folderRepoPath);
            return folder.isNew();
        }
        return false;
    }

    @Override
    public boolean virtualItemExists(RepoPath repoPath) {
        VirtualRepo virtualRepo = virtualRepositoryByKey(repoPath.getRepoKey());
        if (virtualRepo == null) {
            throw new RepositoryRuntimeException("Repository " + repoPath.getRepoKey() + " does not exists!");
        }
        return virtualRepo.virtualItemExists(repoPath.getPath());
    }

    @Override
    @Nonnull
    public MutableVfsItem getMutableItem(RepoPath repoPath) {
        //TORE: [by YS] should be storing repo once interfaces refactoring is done
        LocalRepo localRepo = localOrCachedRepositoryByKey(repoPath.getRepoKey());
        if (localRepo != null) {
            MutableVfsItem mutableFsItem = localRepo.getMutableFsItem(repoPath);
            if (mutableFsItem != null) {
                return mutableFsItem;
            }
        }
        throw new ItemNotFoundRuntimeException(repoPath);
    }

    private MutableVfsFile getMutableFile(RepoPath repoPath) {
        MutableVfsItem mutableItem = getMutableItem(repoPath);
        if (!(mutableItem instanceof MutableVfsFile)) {
            throw new FileExpectedException(repoPath);
        }
        return (MutableVfsFile) mutableItem;
    }

    @Override
    @Nullable
    public StatsInfo getStatsInfo(RepoPath repoPath) {
        if (!authService.canRead(repoPath)) {
            AccessLogger.downloadDenied(repoPath);
            return null;
        }
        return statsService.getStats(repoPath);
    }

    @Override
    public long getArtifactCount(RepoPath repoPath) {
        return fileService.getFilesCount(repoPath);
    }

    @Override
    public FolderSummeryInfo getArtifactCountAndSize(@Nonnull RepoPath repoPath) {
        return fileService.getFilesCountAndSize(repoPath);
    }

    @Override
    public long getNodesCount(RepoPath repoPath) {
        return fileService.getNodesCount(repoPath);
    }

    @Override
    public List<FileInfo> searchFilesWithBadChecksum(ChecksumType type) {
        return fileService.searchFilesWithBadChecksum(type);
    }

    @Override
    @Nonnull
    public List<ItemInfo> getChildren(RepoPath repoPath) {
        TreeBrowsingCriteria criteria = new TreeBrowsingCriteriaBuilder()
                .sortAlphabetically().applySecurity().cacheChildren(false).build();
        ItemNode rootNode = new ItemTree(repoPath, criteria).getRootNode();
        if (rootNode != null) {
            return rootNode.getChildrenInfo();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getChildrenNames(RepoPath repoPath) {
        List<ItemInfo> childrenInfo = getChildren(repoPath);
        List<String> childrenNames = Lists.newArrayListWithCapacity(childrenInfo.size());
        for (ItemInfo itemInfo : childrenInfo) {
            childrenNames.add(itemInfo.getName());
        }
        return childrenNames;
    }

    @Override
    public boolean hasChildren(RepoPath repoPath) {
        return fileService.hasChildren(repoPath);
    }

    @Override
    public void saveFileInternal(RepoPath fileRepoPath, InputStream is) throws RepoRejectException, IOException {
        try {
            MutableFileInfo fileInfo = InfoFactoryHolder.get().createFileInfo(fileRepoPath);
            fileInfo.createTrustedChecksums();
            SaveResourceContext saveContext = new SaveResourceContext.Builder(new FileResource(fileInfo), is).build();
            StoringRepo storingRepo = storingRepositoryByKey(fileRepoPath.getRepoKey());
            if (storingRepo == null) {
                throw new IllegalArgumentException("Storing repo for '" + fileRepoPath + "' not found");
            }
            saveResource(storingRepo, saveContext);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public List<VirtualRepo> getVirtualRepositories() {
        return new ArrayList<>(cache.get().virtualRepositoriesMap.values());
    }

    @Override
    public List<LocalRepo> getLocalAndCachedRepositories() {
        RepositoriesCache repositoriesCache = cache.get();
        Collection<LocalRepo> localRepos = repositoriesCache.localRepositoriesMap.values();
        Collection<LocalCacheRepo> cacheRepos = repositoriesCache.localCacheRepositoriesMap.values();
        List<LocalRepo> repos = new ArrayList<>(localRepos);
        repos.addAll(cacheRepos);
        return repos;
    }

    @Override
    public List<RealRepo> getLocalAndRemoteRepositories() {
        List<RealRepo> repos = new ArrayList<>();
        RepositoriesCache repositoriesCache = cache.get();
        repos.addAll(repositoriesCache.localRepositoriesMap.values());
        repos.addAll(repositoriesCache.remoteRepositoriesMap.values());
        return repos;
    }

    @Override
    public List<LocalRepoDescriptor> getLocalAndCachedRepoDescriptors() {
        return getLocalAndCachedRepositories().stream()
                .map(localRepo -> (LocalRepoDescriptor) localRepo.getDescriptor()).collect(Collectors.toList());
    }

    @Override
    public List<RemoteRepoDescriptor> getRemoteRepoDescriptors() {
        return cache.get().remoteRepositoriesMap.values().stream()
                .map(remoteRepo -> (RemoteRepoDescriptor) remoteRepo.getDescriptor()).collect(Collectors.toList());
    }

    @Override
    public VirtualRepoDescriptor virtualRepoDescriptorByKey(String repoKey) {
        if (repoKey == null || repoKey.length() == 0) {
            return null;
        }
        VirtualRepo virtualRepo = cache.get().virtualRepositoriesMap.get(repoKey);
        return virtualRepo != null ? virtualRepo.getDescriptor() : null;
    }

    @Override
    public List<RepoDescriptor> getRepoDescriptorByPackageType(RepoType repoType) {
        if (repoType == null) {
            throw new ItemNotFoundRuntimeException("Can't get descriptor of null package type");

        }
        return cache.get().repoTypeRepositoriesMap.get(repoType)
                .stream()
                .map(repo -> (RepoDescriptor) repo.getDescriptor())
                .collect(Collectors.toList());
    }

    @Override
    public Map<RepoType, Integer> getRepoDescriptorByPackageTypeCount() {
        Map<RepoType, Integer> ans = new TreeMap<>(new RepoType.RepoNameComparator());
        StreamSupportUtils.mapEntriesStream(cache.get().repoTypeRepositoriesMap)
                .forEach(entry -> ans.putIfAbsent(entry.getKey(), entry.getValue().size()));
        return ans;
    }

    @Override
    public boolean isVirtualRepoExist(String repoKey) {
        return virtualRepoDescriptorByKey(repoKey) != null;
    }

    @Override
    public DistributionRepoDescriptor distributionRepoDescriptorByKey(String key) {
        if (key == null || key.length() == 0) {
            return null;
        }
        DistributionRepo distRepo = cache.get().distributionRepositoriesMap.get(key);
        return distRepo != null ? distRepo.getDescriptor() : null;
    }

    @Override
    public ReleaseBundlesRepoDescriptor releaseBundlesRepoDescriptorByKey(String key) {
        if (key == null || key.length() == 0) {
            return null;
        }
        ReleaseBundlesRepo rbRepo = cache.get().releaseBundlesRepoMap.get(key);
        return rbRepo != null ? rbRepo.getDescriptor() : null;
    }

    @Override
    @Nonnull
    public String getStringContent(FileInfo fileInfo) {
        return getStringContent(fileInfo.getRepoPath());
    }

    @Override
    @Nonnull
    public String getStringContent(RepoPath repoPath) {
        LocalRepo repo = localOrCachedRepositoryByKey(repoPath.getRepoKey());
        if (repo == null) {
            throw new IllegalArgumentException("Local repository for '" + repoPath + "' doesn't exist");
        }
        return repo.getTextFileContent(repoPath);
    }

    @Override
    public ResourceStreamHandle getResourceStreamHandle(RepoPath repoPath) {
        LocalRepo repo = localOrCachedRepositoryByKey(repoPath.getRepoKey());
        if (repo == null) {
            throw new IllegalArgumentException("Local repository for '" + repoPath + "' doesn't exist");
        }
        // Recreate the repo path for remote stream handle request
        if (repo.isCache() && !repo.getKey().equals(repoPath.getRepoKey())) {
            repoPath = InternalRepoPathFactory.cacheRepoPath(repoPath);
        }
        return repo.getFileContent(repoPath);
    }

    @Override
    public ArchiveFileContent getArchiveFileContent(RepoPath archivePath, String sourceEntryPath) throws IOException {
        LocalRepo repo = localOrCachedRepositoryByKey(archivePath.getRepoKey());
        return new ArchiveContentRetriever().getArchiveFileContent(repo, archivePath, sourceEntryPath);
    }

    @Override
    public ArchiveFileContent getGenericArchiveFileContent(RepoPath archivePath, String sourceEntryPath)
            throws IOException {
        LocalRepo repo = localOrCachedRepositoryByKey(archivePath.getRepoKey());
        return new ArchiveContentRetriever().getGenericArchiveFileContent(repo, archivePath, sourceEntryPath);
    }

    /**
     * Import all the repositories under the passed folder which matches local or cached repository declared in the
     * configuration. Having empty directory for each repository is allowed and not an error. Nothing will be imported
     * for those.
     */
    @Override
    public void importAll(ImportSettingsImpl settings) {
        RepositoryImportSettingsImpl repositoriesImportSettings =
                new RepositoryImportSettingsImpl(settings.getBaseDir(), settings);
        repositoriesImportSettings.setRepositories(getLocalAndCacheRepoKeys());
        repositoriesImportSettings.setRepositoriesToDelete(Collections.emptyList());
        repositoriesImportSettings.setFailIfEmpty(false);
        importRepositoriesFromSettings(repositoriesImportSettings);
    }

    /**
     * Import the artifacts under the folder passed directly in the repository named "repoKey". If no repository with
     * this repo key exists or if the folder passed is empty, the status will be set to error.
     */
    @Override
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void importRepo(String repoKey, ImportSettingsImpl settings) {
        RepositoryImportSettingsImpl singleRepoImportSettings =
                new RepositoryImportSettingsImpl(settings.getBaseDir(), settings);
        singleRepoImportSettings.setRepositories(Lists.newArrayList(repoKey));
        singleRepoImportSettings.setSingleRepoImport(true);
        importRepositoriesFromSettings(singleRepoImportSettings);
    }

    /**
     * This method will delete and import all the local and cached repositories listed in the (newly loaded) config
     * file. This action is resource intensive and is done in multiple transactions to avoid out of memory exceptions.
     */
    @Override
    public void importFrom(ImportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        File repoRootPath = getRepositoriesExportDir(settings.getBaseDir());
        if (!repoRootPath.exists() || !repoRootPath.isDirectory()) {
            if (settings.isFailIfEmpty()) {
                throw new IllegalArgumentException(
                        "Could not find content to restore. Verify your export includes content or check the 'exclude content' checkbox");
            } else {
                status.status("No repositories root to import at " + repoRootPath, log);
                return;
            }
        }
        List<String> repositoryKeysForDeletion = getLocalAndCacheRepoKeys();
        List<String> localRepoKeysToImport = getLocalReposToImport(settings, repositoryKeysForDeletion);
        if(settings.isExcludeBuildInfoRepo()) {
            filterOutBuildRepo(localRepoKeysToImport);
            filterOutBuildRepo(repositoryKeysForDeletion);
        }
        RepositoryImportSettingsImpl repositoriesImportSettings = new RepositoryImportSettingsImpl(repoRootPath, settings);
        repositoriesImportSettings.setRepositories(localRepoKeysToImport);
        repositoriesImportSettings.setRepositoriesToDelete(repositoryKeysForDeletion);
        repositoriesImportSettings.setFailIfEmpty(false);
        importRepositoriesFromSettings(repositoriesImportSettings);
    }

    private List<String> getLocalReposToImport(ImportSettings settings, List<String> repositoryKeysForDeletion) {
        List<String> localRepoKeysForImport = settings.getRepositories();
        if (localRepoKeysForImport.isEmpty()) {
            localRepoKeysForImport = new ArrayList<>(repositoryKeysForDeletion);
        }
        return localRepoKeysForImport;
    }

    private void filterOutBuildRepo(List<String> repoKeys) {
        repoKeys.removeIf(repoName -> repoName.equals(buildService.getBuildInfoRepoKey()));
    }

    private void importRepositoriesFromSettings(ImportSettingsImpl settings) {
        if (!ConstantValues.repositoryImportEnabled.getBoolean()) {
            throw ImportIsDisabledException.buildRepositoriesException();
        }

        String jobToken = "No Job";
        boolean taskCompletion = false;
        MutableStatusHolder status = settings.getStatusHolder();
        try {
            jobToken = createAndStartImportJob(settings);
            taskCompletion = taskService.waitForTaskCompletion(jobToken);
        } finally {
            if (!taskCompletion && !status.isError()) {
                // Add error of no completion
                status.error("The task " + jobToken + " did not complete correctly.", log);
            }
        }
    }

    private String createAndStartImportJob(ImportSettingsImpl settings) {
        TaskBase task = TaskUtils.createManualTask(ImportJob.class, 0L);
        task.addAttribute(RepositoryImportSettingsImpl.class.getName(), settings);
        return taskService.startTask(task, true);
    }

    @Override
    public void exportTo(ExportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        status.status("Exporting repositories...", log);
        if (TaskCallback.currentTaskToken() == null) {
            exportAsync(BaseSettings.FULL_SYSTEM, settings);
        } else {
            List<String> repoKeys = settings.getRepositories();
            for (String repoKey : repoKeys) {
                boolean stop = taskService.pauseOrBreak();
                if (stop) {
                    status.error("Export was stopped", log);
                    return;
                }
                exportRepo(repoKey, settings);
                if (status.isError() && settings.isFailFast()) {
                    return;
                }
            }

            if (settings.isIncremental()) {
                File repositoriesDir = getRepositoriesExportDir(settings.getBaseDir());
                cleanupIncrementalBackupDirectory(repositoriesDir, repoKeys);
            }
        }
    }

    @Override
    public void exportRepo(String repoKey, ExportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        if (TaskCallback.currentTaskToken() == null) {
            exportAsync(repoKey, settings);
        } else {
            //Check if we need to break/pause
            boolean stop = taskService.pauseOrBreak();
            if (stop) {
                status.error("Export was stopped on " + repoKey, log);
                return;
            }
            LocalRepo sourceRepo = localOrCachedRepositoryByKey(repoKey);
            if (sourceRepo == null) {
                status.error("Export cannot be done on non existing repository " + repoKey, log);
                return;
            }
            if (shouldExcludeBuildInfo(settings, sourceRepo.getDescriptor())) {
                status.debug(repoKey + " is excluded from the export since user opted to exclude builds.", log);
                return;
            }
            File targetDir = getRepoExportDir(settings.getBaseDir(), repoKey);
            ExportSettingsImpl repoSettings = new ExportSettingsImpl(targetDir, settings);
            sourceRepo.exportTo(repoSettings);
        }
    }

    /**
     * 1. This is migration time, and the user opted to *include* builds - they are output from db (the old way) and we
     * exclude the build-info repo to avoid duplication
     *
     * 2. The user sent the 'exclude builds' option in  {@param settings}, since you can't exclude specific repos in
     * system export we have to exclude it here like this.
     */
    private boolean shouldExcludeBuildInfo(ExportSettings settings, RepoDescriptor descriptor) {
        return (settings.isExcludeBuilds() || settings.isExcludeBuildInfoRepo())
                && RepoType.BuildInfo.equals(descriptor.getType());
    }

    private File getRepoExportDir(File exportDir, String repoKey) {
        return new File(getRepositoriesExportDir(exportDir), repoKey);
    }

    private File getRepositoriesExportDir(File exportDir) {
        // the directory under the base export dir that contains the exported repositories
        return new File(exportDir, "repositories");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MutableStatusHolder exportSearchResults(SavedSearchResults searchResults, ExportSettingsImpl baseSettings) {
        return new DbRepoExportSearchHandler(searchResults, baseSettings).export();
    }

    @Override
    @Nonnull
    public ItemInfo getItemInfo(RepoPath repoPath) {
        LocalRepo localRepo = getLocalRepository(repoPath);
        VfsItem item = localRepo.getImmutableFsItem(repoPath);
        if (item != null) {
            return item.getInfo();
        }
        throw new ItemNotFoundRuntimeException("Item " + repoPath + " does not exist");
    }

    @Override
    @Nonnull
    public FileInfo getFileInfo(RepoPath repoPath) {
        ItemInfo itemInfo = getItemInfo(repoPath);
        if (itemInfo instanceof FileInfo) {
            return (FileInfo) itemInfo;
        } else {
            throw new FileExpectedException(repoPath);
        }
    }

    @Override
    @Nonnull
    public FolderInfo getFolderInfo(RepoPath repoPath) {
        ItemInfo itemInfo = getItemInfo(repoPath);
        if (itemInfo instanceof FolderInfo) {
            return (FolderInfo) itemInfo;
        } else {
            throw new FolderExpectedException(repoPath);
        }
    }

    @Override
    public boolean exists(RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        LocalRepo localRepo = localOrCachedRepositoryByKey(repoKey);
        return localRepo != null && localRepo.itemExists(repoPath.getPath());
    }

    @Override
    public boolean existsBySha1(String sha1) {
        return fileService.existsBySha1(sha1);
    }

    @Override
    public ItemMetaInfo getItemMetaInfo(ItemInfo itemInfo) {
        return ContextHelper.get().beanForType(NodeMetaInfoService.class).getNodeMetaInfo(itemInfo);
    }

    @Override
    public MoveMultiStatusHolder moveWithoutMavenMetadata(RepoPath from, RepoPath to, boolean dryRun,
            boolean suppressLayouts, boolean failFast) {
        MoverConfigBuilder configBuilder = new MoverConfigBuilder(from, to).copy(false).dryRun(dryRun).
                executeMavenMetadataCalculation(false).atomic(true).suppressLayouts(suppressLayouts).failFast(failFast);
        return moveOrCopy(configBuilder.build());
    }

    @Override
    public ChecksumInfo setClientChecksum(LocalRepo repo, ChecksumType checksumType, RepoPath targetFileRepoPath,
            String checksum) {
        MutableVfsFile vfsFile = getMutableFile(targetFileRepoPath);
        if (!checksumType.isValid(checksum)) {
            log.warn("Uploading non valid original checksum for {}", vfsFile.getRepoPath());
        }
        vfsFile.setClientChecksum(checksumType, checksum);
        // fire replication event for the client checksum
        RepoPath checksumRepoPath = InternalRepoPathFactory.create(targetFileRepoPath.getRepoKey(),
                targetFileRepoPath.getPath() + checksumType.ext());
        addonsManager.addonByType(ReplicationAddon.class)
                .offerLocalReplicationDeploymentEvent(checksumRepoPath, vfsFile.isFile());

        return vfsFile.getInfo().getChecksumsInfo().getChecksumInfo(checksumType);
    }

    @Override
    public MoveMultiStatusHolder delete(Set<RepoPath> pathsToMove) {
        Set<RepoPath> pathsToMoveIncludingParents = aggregatePathsToMove(pathsToMove, "", false);
        log.debug("The following paths will be removed: {}", pathsToMoveIncludingParents);
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        for (RepoPath pathToDelete : pathsToMoveIncludingParents) {
            status.merge(undeploy(pathToDelete));
        }
        return status;
    }

    @Override
    public MoveMultiStatusHolder move(RepoPath from, RepoPath to, boolean dryRun, boolean suppressLayouts,
            boolean failFast) {
        MoverConfigBuilder configBuilder = new MoverConfigBuilder(from, to).copy(false).dryRun(dryRun).
                executeMavenMetadataCalculation(false).suppressLayouts(suppressLayouts).failFast(failFast).atomic(true);
        MoveMultiStatusHolder status = moveOrCopy(configBuilder.build());
        for (RepoPath folderPath : status.getCandidatesForMavenMetadataCalculation()) {
            mavenMetadataService.calculateMavenMetadataAsync(new MavenMetadataWorkItem(folderPath, false));
        }
        return status;
    }

    @Override
    public MoveMultiStatusHolder move(Set<RepoPath> pathsToMove, String targetLocalRepoKey, Properties properties,
            boolean dryRun, boolean failFast) {
        Set<RepoPath> pathsToMoveIncludingParents = aggregatePathsToMove(pathsToMove, targetLocalRepoKey, false);
        log.debug("The following paths will be moved: {}", pathsToMoveIncludingParents);
        // start moving each path separately, marking each folder or file's parent folder for metadata recalculation
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        RepoPathMover mover = getMoveRepoPathService();
        for (RepoPath pathToMove : pathsToMoveIncludingParents) {
            RepoPath targetRepoPath = InternalRepoPathFactory
                    .create(targetLocalRepoKey, pathToMove.getPath(), pathToMove.isFolder());
            log.debug("Moving path: {} to {}", pathToMove, targetRepoPath);
            mover.executeOperation(status, new MoverConfigBuilder(pathToMove, targetRepoPath).copy(false).dryRun(dryRun)
                    .executeMavenMetadataCalculation(false).pruneEmptyFolders(true).properties(properties)
                    .unixStyleBehavior(false).failFast(failFast).atomic(true).build());
        }
        for (RepoPath folderPath : status.getCandidatesForMavenMetadataCalculation()) {
            mavenMetadataService.calculateMavenMetadataAsync(new MavenMetadataWorkItem(folderPath, false));
        }
        return status;
    }

    @Override
    public MoveMultiStatusHolder move(Set<RepoPath> pathsToMove, RepoPath targetParent, Properties properties,
            boolean dryRun, boolean failFast) {
        log.debug("The following paths will be moved under path {}: {}", targetParent, pathsToMove);
        String targetRepo = targetParent.getRepoKey();
        String targetPath = targetParent.getPath();
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        RepoPathMover mover = getMoveRepoPathService();
        for (RepoPath source : pathsToMove) {
            RepoPath target = InternalRepoPathFactory.create(targetRepo, targetPath + "/" + source.getName());
            log.debug("Moving path: {} to {}", source, target);
            mover.executeOperation(status, new MoverConfigBuilder(source, target).copy(false).dryRun(dryRun)
                    .executeMavenMetadataCalculation(false).pruneEmptyFolders(true).properties(properties)
                    .unixStyleBehavior(false).failFast(failFast).atomic(true).build());
        }
        for (RepoPath folderPath : status.getCandidatesForMavenMetadataCalculation()) {
            mavenMetadataService.calculateMavenMetadataAsync(new MavenMetadataWorkItem(folderPath, false));
        }
        return status;
    }

    @Override
    public MoveMultiStatusHolder move(RepoPath repoPath, String targetKey, String targetPath, Properties addProps,
            List<String> removeProps, boolean dryRun, boolean failFast, int transactionSize) {
        // Create move context
        MoveCopyContext context = new MoveCopyContext(repoPath, targetKey, targetPath);
        context.setTransactionSize(transactionSize).
                setDryRun(dryRun).
                setFailFast(failFast).
                setAddProperties(addProps).
                setRemovProperties(removeProps).
                setCopy(false).
                setExecuteMavenMetadataCalculation(true).
                setPruneEmptyFolders(false).
                setUnixStyleBehavior(true).
                setSuppressLayouts(true);
        // Execute move
        return flatMoveCopyService.moveCopy(context);
    }

    @Override
    public MoveMultiStatusHolder copy(RepoPath repoPath, String targetKey, String targetPath, Properties addProps,
            List<String> removeProps, boolean dryRun, boolean failFast, int transactionSize) {
        // Create move context
        MoveCopyContext context = new MoveCopyContext(repoPath, targetKey, targetPath);
        context.setTransactionSize(transactionSize).
                setDryRun(dryRun).
                setFailFast(failFast).
                setAddProperties(addProps).
                setRemovProperties(removeProps).
                setCopy(true).
                setExecuteMavenMetadataCalculation(true).
                setPruneEmptyFolders(false).
                setUnixStyleBehavior(false).
                setSuppressLayouts(true);
        // Execute move/copy
        return flatMoveCopyService.moveCopy(context);
    }

    @Override
    public MoveMultiStatusHolder moveMultiTx(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun, boolean suppressLayouts,
            boolean failFast) {
        return move(fromRepoPath, targetRepoPath, dryRun, suppressLayouts, failFast, false, null, false);
    }

    @Override
    public MoveMultiStatusHolder move(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun, boolean suppressLayouts,
            boolean failFast, boolean atomic, Properties properties, boolean overrideProperties) {
        MoverConfigBuilder configBuilder = new MoverConfigBuilder(fromRepoPath, targetRepoPath).copy(false).dryRun(dryRun).
                executeMavenMetadataCalculation(false).suppressLayouts(suppressLayouts).failFast(failFast)
                .atomic(atomic).properties(properties).overrideProperties(overrideProperties);
        MoveMultiStatusHolder assertStatus = new MoveMultiStatusHolder();
        fireAssertDeletePathAllowedEvent(fromRepoPath, assertStatus);
        if (assertStatus.hasErrors()) {
            return assertStatus;
        }
        MoveMultiStatusHolder status = moveOrCopy(configBuilder.build());
        for (RepoPath folderPath : status.getCandidatesForMavenMetadataCalculation()) {
            mavenMetadataService.calculateMavenMetadataAsync(new MavenMetadataWorkItem(folderPath, false));
        }
        return status;
    }

    @Override
    public MoveMultiStatusHolder copyMultiTx(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun,
           boolean suppressLayouts, boolean failFast) {
        return copy(fromRepoPath, targetRepoPath, dryRun, suppressLayouts, failFast, false, null, false);
    }

    @Override
    public MoveMultiStatusHolder copy(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun,
            boolean suppressLayouts, boolean failFast) {
        return copy(fromRepoPath, targetRepoPath, dryRun, suppressLayouts, failFast, true, null, false);
    }

    @Override
    public MoveMultiStatusHolder copy(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun,
            boolean suppressLayouts, boolean failFast, boolean atomic, Properties properties, boolean overrideProperties) {
        return copy(fromRepoPath, targetRepoPath, dryRun, suppressLayouts, failFast, atomic, properties, overrideProperties, false);
    }

    @Override
    public MoveMultiStatusHolder copyToCache(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun,
            boolean suppressLayouts, boolean failFast, boolean atomic, Properties properties, boolean overrideProperties) {
        return copy(fromRepoPath, targetRepoPath, dryRun, suppressLayouts, failFast, atomic, properties, overrideProperties, true);
    }

    private MoveMultiStatusHolder copy(RepoPath fromRepoPath, RepoPath targetRepoPath, boolean dryRun, boolean suppressLayouts,
            boolean failFast, boolean atomic, Properties properties, boolean overrideProperties, boolean toCache) {
        MoverConfigBuilder configBuilder = new MoverConfigBuilder(fromRepoPath, targetRepoPath).copy(true)
                .dryRun(dryRun).executeMavenMetadataCalculation(false).suppressLayouts(suppressLayouts).failFast(failFast)
                .atomic(atomic).properties(properties).overrideProperties(overrideProperties).toCache(toCache);
        MoveMultiStatusHolder status = moveOrCopy(configBuilder.build());
        for (RepoPath folderPath : status.getCandidatesForMavenMetadataCalculation()) {
            mavenMetadataService.calculateMavenMetadataAsync(new MavenMetadataWorkItem(folderPath, false));
        }
        return status;
    }

    @Override
    public MoveMultiStatusHolder copy(Set<RepoPath> pathsToCopy, String targetLocalRepoKey,
            Properties properties, boolean dryRun, boolean failFast) {
        Set<RepoPath> pathsToCopyIncludingParents = aggregatePathsToMove(pathsToCopy, targetLocalRepoKey, true);

        log.debug("The following paths will be copied: {}", pathsToCopyIncludingParents);
        //Start copying each path separately, marking each folder or file's parent folder for metadata recalculation
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        RepoPathMover mover = getCopyRepoPathService();
        for (RepoPath pathToCopy : pathsToCopyIncludingParents) {
            RepoPath targetRepoPath = InternalRepoPathFactory
                    .create(targetLocalRepoKey, pathToCopy.getPath(), pathToCopy.isFolder());
            mover.executeOperation(status, new MoverConfigBuilder(pathToCopy, targetRepoPath).copy(true).dryRun(dryRun)
                    .executeMavenMetadataCalculation(false).pruneEmptyFolders(false).properties(properties)
                    .unixStyleBehavior(false).failFast(failFast).atomic(true).build());
        }

        for (RepoPath folderPath : status.getCandidatesForMavenMetadataCalculation()) {
            mavenMetadataService.calculateMavenMetadataAsync(new MavenMetadataWorkItem(folderPath, false));
        }

        return status;
    }

    @Override
    public MoveMultiStatusHolder copy(RepoPath fromRepoPath, RepoPath targetRepoPath){
        MoverConfigBuilder configBuilder = new MoverConfigBuilder(fromRepoPath, targetRepoPath).copy(true)
                .dryRun(false).executeMavenMetadataCalculation(false).suppressLayouts(true).failFast(true)
                .atomic(false).properties(null).overrideProperties(false).toCache(false).unixStyleBehavior(false);
        return moveOrCopy(configBuilder.build());
    }

    private MoveMultiStatusHolder moveOrCopy(MoverConfig config) {
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        // copy or move service
        if (config.isCopy()) {
            getCopyRepoPathService().executeOperation(status, config);
        } else {
            getMoveRepoPathService().executeOperation(status, config);
        }
        return status;
    }

    /**
     * Returns an instance of the Repo Path Mover
     *
     * @return RepoPathMover
     */
    private RepoPathMover getMoveRepoPathService() {
        return ContextHelper.get().beanForType(MoveRepoPathService.class);
    }

    /**
     * Returns an instance of the Repo Path Mover
     *
     * @return RepoPathMover
     */
    private RepoPathMover getCopyRepoPathService() {
        return ContextHelper.get().beanForType(CopyRepoPathService.class);
    }

    @Override
    public StatusHolder deploy(RepoPath repoPath, InputStream inputStream) {
        try {
            ArtifactoryDeployRequest request = new ArtifactoryDeployRequestBuilder(repoPath)
                    .inputStream(inputStream).build();
            InternalArtifactoryResponse response = new InternalArtifactoryResponse();
            uploadService.upload(request, response);
            return response.getStatusHolder();
        } catch (Exception e) {
            String msg = String.format("Cannot deploy to '{%s}'.", repoPath);
            log.debug(msg, e);
            throw new RepositoryRuntimeException(msg, e);
        }
    }

    @Override
    public FileInfo getVirtualFileInfo(RepoPath virtualRepoPath) {
        VirtualRepo virtualRepo = virtualRepositoryByKey(virtualRepoPath.getRepoKey());
        if (virtualRepo == null) {
            throw new IllegalArgumentException(virtualRepoPath.getRepoKey() + " is not a virtual repository.");
        }
        Set<LocalRepo> resolvedLocalRepos = virtualRepo.getResolvedLocalAndCachedRepos();
        for (LocalRepo resolvedLocalRepo : resolvedLocalRepos) {
            if (resolvedLocalRepo.itemExists(virtualRepoPath.getPath())) {
                return getFileInfo(resolvedLocalRepo.getRepoPath(virtualRepoPath.getPath()));
            }
        }

        throw new ItemNotFoundRuntimeException("Item " + virtualRepoPath + " does not exists");
    }

    @Override
    public ItemInfo getVirtualItemInfo(RepoPath virtualRepoPath) {
        VirtualRepo virtualRepo = virtualRepositoryByKey(virtualRepoPath.getRepoKey());
        if (virtualRepo == null) {
            throw new IllegalArgumentException(virtualRepoPath.getRepoKey() + " is not a virtual repository.");
        }
        Set<LocalRepo> resolvedLocalRepos = virtualRepo.getResolvedLocalAndCachedRepos();
        for (LocalRepo resolvedLocalRepo : resolvedLocalRepos) {
            if (resolvedLocalRepo.itemExists(virtualRepoPath.getPath())) {
                return getItemInfo(resolvedLocalRepo.getRepoPath(virtualRepoPath.getPath()));
            }
        }

        throw new ItemNotFoundRuntimeException("Item " + virtualRepoPath + " does not exists");
    }

    @Override
    public FolderInfo getVirtualFolderInfo(RepoPath virtualRepoPath) {
        VirtualRepo virtualRepo = virtualRepositoryByKey(virtualRepoPath.getRepoKey());
        if (virtualRepo == null) {
            throw new IllegalArgumentException(virtualRepoPath.getRepoKey() + " is not a virtual repository.");
        }
        Set<LocalRepo> resolvedLocalRepos = virtualRepo.getResolvedLocalAndCachedRepos();
        for (LocalRepo resolvedLocalRepo : resolvedLocalRepos) {
            if (resolvedLocalRepo.itemExists(virtualRepoPath.getPath())) {
                return getFolderInfo(resolvedLocalRepo.getRepoPath(virtualRepoPath.getPath()));
            }
        }

        throw new ItemNotFoundRuntimeException("Folder " + virtualRepoPath + " does not exists");
    }


    @Override
    public List<RepoDescriptor> getVirtualResolvedLocalAndCacheDescriptors(String virtualRepoKey) {
        VirtualRepo virtualRepo = virtualRepositoryByKey(virtualRepoKey);
        if (virtualRepo != null) {
            return virtualRepo.getResolvedLocalAndCachedRepos().stream()
                    .map((Function<LocalRepo, RepoDescriptor>) Repo::getDescriptor).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public BasicStatusHolder undeploy(RepoPath repoPath, boolean calcMavenMetadata) {
        return undeploy(repoPath, calcMavenMetadata, false);
    }

    @Override
    public BasicStatusHolder undeploy(RepoPath repoPath, boolean calcMavenMetadata, boolean pruneEmptyFolders) {
        return undeploy(repoPath, pruneEmptyFolders,
                new DeleteContext(repoPath).calculateMavenMetadata(calcMavenMetadata));
    }

    @Override
    public BasicStatusHolder undeploy(RepoPath repoPath, boolean pruneEmptyFolders, DeleteContext deleteContext) {
        String repoKey = repoPath.getRepoKey();
        StoringRepo storingRepo = storingRepositoryByKey(repoKey);
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        if (storingRepo == null) {
            statusHolder.error("Could find storing repository by key '" + repoKey + "'", log);
            return statusHolder;
        }
        PathDeletionContext pathDeletionContext = new PathDeletionContext.Builder(storingRepo, repoPath.getPath(),
                statusHolder).assertOverwrite(false).build();
        assertDelete(pathDeletionContext);
        if (!statusHolder.isError()) {
            try {
                storingRepo.undeploy(deleteContext);
            } catch (CancelException e) {
                statusHolder.error("Undeploy was canceled by user plugin", e.getErrorCode(), e, log);
            }
        }

        if (pruneEmptyFolders && !repoPath.isRoot()) {
            pruneService.prune(repoPath.getParent());
        }

        return statusHolder;
    }

    @Override
    public BasicStatusHolder undeployMultiTransaction(RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        StoringRepo storingRepo = storingRepositoryByKey(repoKey);
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        if (storingRepo == null) {
            statusHolder.error("Could not find storing repository by key '" + repoKey + "'", log);
            return statusHolder;
        }
        PathDeletionContext pathDeletionContext = new PathDeletionContext.Builder(storingRepo, repoPath.getPath(),
                statusHolder).assertOverwrite(false).build();
        assertDelete(pathDeletionContext);
        if (!statusHolder.isError()) {
            ItemInfo itemInfo = getItemInfo(repoPath);
            boolean shouldProtectPathDeletion = pathDeletionContext.getRepo().shouldProtectPathDeletion(pathDeletionContext);
            boolean deleted = undeploySingleItemTransactions(itemInfo.getRepoPath(), true, statusHolder, shouldProtectPathDeletion);
            if (!deleted && !statusHolder.isError()) {
                // We won't respond 404 even if hideUnauthorizedResources flag is on, because the user does have permission for repoPath itself
                statusHolder.error(String.format(
                        "Not enough permissions to delete/overwrite all artifacts under '%s' (user: '%s' needs DELETE permission).",
                        repoPath, authService.currentUsername()), HttpStatus.SC_FORBIDDEN, log);
            }
        }
        return statusHolder;
    }

    /**
     * Deletes a file or recursively deletes a folder. Uses a single transaction for each leaf item
     *
     * @param repoPath          - repo path
     * @param calcMavenMetadata - if true calculate meta data
     * @return true if the given repoPath was successfully deleted
     */
    private boolean undeploySingleItemTransactions(RepoPath repoPath, boolean calcMavenMetadata,
            BasicStatusHolder statusHolder, boolean shouldProtectPathDeletion) {
        boolean canDeleteMe = true;
        if (!repoPath.isFile()) {
            List<ItemInfo> children = fileService.loadChildren(repoPath);
            for (ItemInfo child : children) {
                boolean deleted = undeploySingleItemTransactions(child.getRepoPath(), false, statusHolder,
                        shouldProtectPathDeletion);
                if (!deleted) {
                    canDeleteMe = false;
                }
            }
        }
        // If allowed then delete file or empty folder
        return canDeleteMe && deleteSingleLeaf(repoPath, calcMavenMetadata, statusHolder, shouldProtectPathDeletion);
    }

    /**
     * Checks that leaf deletion is allowed and if so, deletes in a single transaction
     *
     * @param repoPath - file or empty folder (leaf) repo path
     * @return true if the given repoPath was successfully deleted
     */
    private boolean deleteSingleLeaf(RepoPath repoPath, boolean calcMavenMetadata, BasicStatusHolder statusHolder,
            boolean shouldProtectPathDeletion) {
        if (shouldProtectPathDeletion && !authService.canDelete(repoPath)) {
            AccessLogger.deleteDenied(repoPath);
            return false;
        }
        return getTransactionalMe().undeployInTx(repoPath, calcMavenMetadata, statusHolder);
    }

    @Override
    public boolean undeployInTx(RepoPath repoPath, boolean calcMavenMetadata,
            BasicStatusHolder statusHolder) {
        StoringRepo storingRepo = storingRepositoryByKey(repoPath.getRepoKey());
        try {
            storingRepo.undeploy(new DeleteContext(repoPath).calculateMavenMetadata(calcMavenMetadata));
            return true;
        } catch (CancelException e) {
            statusHolder.error("Undeploy was canceled by user plugin", e.getErrorCode(), e, log);
            return false;
        }
    }

    @Override
    public RepositoriesCache rebuildRepositoriesInTransaction() {
        RepositoriesCacheBuilder repositoriesCacheBuilder = new RepositoriesCacheBuilder();
        return repositoriesCacheBuilder.rebuildRepositories();
    }

    @Override
    public BasicStatusHolder undeploy(RepoPath repoPath) {
        return undeploy(repoPath, true);
    }

    @Override
    public StatusHolder undeployVersionUnits(Set<VersionUnit> versionUnits) {
        BasicStatusHolder status = new BasicStatusHolder();
        InternalRepositoryService transactionalMe = getTransactionalMe();

        Set<RepoPath> pathsForMavenMetadataCalculation = Sets.newHashSet();

        for (VersionUnit versionUnit : versionUnits) {
            Set<RepoPath> repoPaths = versionUnit.getRepoPaths();
            if (repoPaths.stream().filter(authService::canDelete).count() != repoPaths.size()) {
                status.warn(String.format(
                        "The user: '%s' doesn't have permission to delete one or more the paths associated with module '%s', it will not be removed.",
                        authService.currentUsername(), versionUnit.getModuleInfo().getPrettyModuleId()), log);
                continue;
            }
            for (RepoPath repoPath : repoPaths) {
                BasicStatusHolder holder = transactionalMe.undeploy(repoPath, false, true);
                status.merge(holder);
                if (NamingUtils.isPom(repoPath.getPath())) {
                    // We need to re-calculate the artifact id folder (which is the grandparent of the pom file)
                    RepoPath grandparentFolder = RepoPathUtils.getAncestor(repoPath, 2);
                    if (grandparentFolder != null) {
                        pathsForMavenMetadataCalculation.add(grandparentFolder);
                    }
                }
            }
        }
        // Check to make sure of existence, might have been removed through the iterations of the version units

        if (!ConstantValues.mvnMetadataCalculationSkipDeleteEvent.getBoolean()) {
            pathsForMavenMetadataCalculation.stream()
                    .filter(this::exists)
                    .forEach(path -> mavenMetadataService
                            .calculateMavenMetadataAsync(new MavenMetadataWorkItem(path, false)));
        }
        return status;
    }

    @Override
    public int zap(RepoPath repoPath) {
        int zappedItems = 0;
        LocalRepo localRepo = getLocalRepository(repoPath);
        if (localRepo.isCache()) {
            LocalCacheRepo cache = (LocalCacheRepo) localRepo;
            zappedItems = cache.zap(repoPath);
        } else {
            log.warn("Got a zap request on a non-local-cache node '" + repoPath + "'.");
        }
        return zappedItems;
    }

    @Override
    public Set<String> getAllRepoKeys() {
        return cache.get().allRepoKeysCache;
    }

    @Override
    public Map<Character, List<String>> getAllRepoKeysByFirstCharMap() {
        return cache.get().reposByFirstCharMap;
    }

    @Override
    public List<RealRepoDescriptor> getLocalAndRemoteRepoDescriptors() {
        return getLocalAndRemoteRepositories()
                .stream()
                .map(repo -> (RealRepoDescriptor) repo.getDescriptor())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAnonAccessEnabled() {
        return authService.isAnonAccessEnabled();
    }

    @Override
    public Repo repositoryByKey(String key) {
        Repo repo = null;
        RepositoriesCache repositoriesCache = cache.get();
        if (repositoriesCache.localRepositoriesMap.containsKey(key)) {
            repo = (repositoriesCache.localRepositoriesMap.get(key));
        } else if (repositoriesCache.localCacheRepositoriesMap.containsKey(key)) {
            repo = repositoriesCache.localCacheRepositoriesMap.get(key);
        } else if (repositoriesCache.remoteRepositoriesMap.containsKey(key)) {
            repo = repositoriesCache.remoteRepositoriesMap.get(key);
        } else if (repositoriesCache.virtualRepositoriesMap.containsKey(key)) {
            repo = repositoriesCache.virtualRepositoriesMap.get(key);
        } else if (repositoriesCache.distributionRepositoriesMap.containsKey(key)) {
            repo = repositoriesCache.distributionRepositoriesMap.get(key);
        } else if (repositoriesCache.releaseBundlesRepoMap.containsKey(key)) {
            repo = repositoriesCache.releaseBundlesRepoMap.get(key);
        } else if (repositoriesCache.trashcan != null && key.equals(repositoriesCache.trashcan.getKey())) {
            repo = repositoriesCache.trashcan;
        } else if (repositoriesCache.supportBundlesRepo != null &&
                key.equals(repositoriesCache.supportBundlesRepo.getKey())) {
            repo = repositoriesCache.supportBundlesRepo;
        }
        return repo;
    }

    @Override
    public LocalRepo localRepositoryByKey(String key) {
        RepositoriesCache repositoriesCache = cache.get();
        if (RepoPathUtils.isTrash(key)) {
            return repositoriesCache.trashcan;
        }
        if (SUPPORT_BUNDLE_REPO_NAME.equals(key)) {
            return repositoriesCache.supportBundlesRepo;
        }
        return repositoriesCache.localRepositoriesMap.get(key);
    }

    @Override
    public RemoteRepo remoteRepositoryByKey(String key) {
        return cache.get().remoteRepositoriesMap.get(key);
    }

    @Override
    public VirtualRepo virtualRepositoryByKey(String key) {
        return cache.get().virtualRepositoriesMap.get(key);
    }

    @Override
    public DistributionRepo distributionRepoByKey(String key) {
        return cache.get().distributionRepositoriesMap.get(key);
    }

    @Override
    public ReleaseBundlesRepo releaseBundleRepositoryByKey(String key) {
        return cache.get().releaseBundlesRepoMap.get(key);
    }

    @Override
    @Nullable
    public LocalRepo localOrCachedRepositoryByKey(String key) {
        LocalRepo localRepo = localRepositoryByKey(key);
        if (localRepo == null) {
            RemoteRepo remoteRepo = remoteRepositoryByRemoteOrCacheKey(key);
            if (remoteRepo != null && remoteRepo.isStoreArtifactsLocally()) {
                localRepo = remoteRepo.getLocalCacheRepo();
            }
        }
        if (localRepo == null) {
            localRepo = distributionRepoByKey(key);
        }
        if (localRepo == null) {
            localRepo = releaseBundleRepositoryByKey(key);
        }
        return localRepo;
    }

    private RemoteRepo remoteRepositoryByRemoteOrCacheKey(String key) {
        RemoteRepo remoteRepo = remoteRepositoryByKey(key);
        if (remoteRepo == null) {
            //Try to get cached repositories
            int idx = key.lastIndexOf(RepoPath.REMOTE_CACHE_SUFFIX);
            //Get the cache either by <remote-repo-name> or by <remote-repo-name>-cache
            if (idx > 1 && idx + RepoPath.REMOTE_CACHE_SUFFIX.length() == key.length()) {
                remoteRepo = remoteRepositoryByKey(key.substring(0, idx));
            }
        }

        return remoteRepo;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <R extends Repo> RepoRepoPath<R> getRepoRepoPath(RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        if (RepoPathUtils.isTrash(repoKey)) {
            return new RepoRepoPath<>((R) cache.get().trashcan, repoPath);
        }
        R repo = (R) repositoryByKey(repoKey);
        if (repo == null) {
            throw new IllegalArgumentException("Repository '" + repoKey + "' not found!");
        }
        return new RepoRepoPath<>(repo, repoPath);
    }

    @Override
    public StoringRepo storingRepositoryByKey(String key) {
        LocalRepo localRepo = localOrCachedRepositoryByKey(key);
        if (localRepo != null) {
            return localRepo;
        }
        VirtualRepo virtualRepo = virtualRepositoryByKey(key);
        if (virtualRepo != null) {
            return virtualRepo;
        }
        return releaseBundleRepositoryByKey(key);
    }

    @Override
    public boolean isWriteLocked(RepoPath repoPath) {
        StoringRepo storingRepo = storingRepositoryByKey(repoPath.getRepoKey());
        return storingRepo != null && storingRepo.isWriteLocked(repoPath);
    }

    @Override
    public List<ItemInfo> getOrphanItems(RepoPath repoPath) {
        return fileService.getOrphanItems(repoPath);
    }

    @Override
    public void reloadConfigurationLazy() {
        cleanAndPromote();
    }

    @Override
    public List<LocalRepoDescriptor> getLocalRepoDescriptors() {
        return cache.get().localRepositoriesMap.values().stream()
                .map(localRepo -> (LocalRepoDescriptor) localRepo.getDescriptor())
                .filter(localRepo -> !localRepo.getKey().equals(buildService.getBuildInfoRepoKey()))
                .collect(Collectors.toList());
    }

    @Override
    public List<LocalRepoDescriptor> getLocalRepoDescriptorsIncludingBuildInfo() {
        return cache.get().localRepositoriesMap.values().stream()
                .map(localRepo -> (LocalRepoDescriptor) localRepo.getDescriptor())
                .collect(Collectors.toList());
    }

    @Override
    public List<LocalCacheRepoDescriptor> getCachedRepoDescriptors() {
        return cache.get().localCacheRepositoriesMap.values().stream()
                .map(Repo::getDescriptor)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistributionRepoDescriptor> getDistributionRepoDescriptors() {
        return cache.get().distributionRepositoriesMap.values().stream()
                .map(Repo::getDescriptor)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReleaseBundlesRepoDescriptor> getReleaseBundlesRepoDescriptors() {
        return cache.get().releaseBundlesRepoMap.values().stream()
                .map(RepoBase::getDescriptor)
                .collect(Collectors.toList());
    }

    @Override
    public  LocalRepoDescriptor getSupportBundlesRepoDescriptors() {
        return (LocalRepoDescriptor) cache.get().supportBundlesRepo.getDescriptor();
    }

    @Override
    public RepoDescriptor repoDescriptorByKey(String key) {
        Repo repo = repositoryByKey(key);
        return repo != null ? repo.getDescriptor() : null;
    }

    @Override
    public LocalRepoDescriptor localRepoDescriptorByKey(String key) {
        LocalRepo localRepo = localRepositoryByKey(key);
        return localRepo != null ? (LocalRepoDescriptor) localRepo.getDescriptor() : null;
    }

    @Override
    public LocalRepoDescriptor localOrCachedRepoDescriptorByKey(String key) {
        LocalRepo localRepo = localOrCachedRepositoryByKey(key);
        return localRepo != null ? (LocalRepoDescriptor) localRepo.getDescriptor() : null;
    }

    @Override
    public LocalRepoDescriptor localCachedOrDistributionRepoDescriptorByKey(String repoKey) {
        return Optional.ofNullable(localOrCachedRepoDescriptorByKey(repoKey))
                .orElse(distributionRepoDescriptorByKey(repoKey));
    }

    @Override
    public RemoteRepoDescriptor remoteRepoDescriptorByKey(String key) {
        RemoteRepo remoteRepo = remoteRepositoryByKey(key);
        return remoteRepo != null ? (RemoteRepoDescriptor) remoteRepo.getDescriptor() : null;
    }

    @Override
    public List<VirtualRepoDescriptor> getVirtualRepoDescriptors() {
        return getVirtualRepositories().stream()
                .map(RepoBase::getDescriptor)
                .collect(Collectors.toList());
    }

    @Override
    public void assertValidDeployPathAndPermissions(ValidDeployPathContext validDeployPathContext) throws RepoRejectException {
        LocalRepo repo = validDeployPathContext.getRepo();
        RepoPath repoPath = validDeployPathContext.getRepoPath();
        String path = repoPath.getPath();
        if (!repo.getKey().equals(repoPath.getRepoKey())) {
            // the repo path should point to the given repo (e.g, in case the repo path points to the remote repo)
            repoPath = InternalRepoPathFactory.create(repo.getKey(), path, repoPath.isFolder());
        }
        BasicStatusHolder status = repo.assertValidPath(repoPath, false);
        if (!status.isError()) {
            // if it is metadata, assert annotate privileges. Maven metadata is treated as regular file
            // (needs deploy permissions).
            if (NamingUtils.isMetadata(path)) {
                if (!authService.canAnnotate(repoPath)) {
                    String msg = String.format("The user: '%s' is not permitted to annotate '%s' on '%s'.",
                            authService.currentUsername(), path, repoPath);
                    status.error(msg, HttpStatus.SC_FORBIDDEN, log);
                    AccessLogger.annotateDenied(repoPath);
                }
            } else {
                //Assert deploy privileges
                boolean canDeploy = authService.canDeploy(repoPath);
                if (!canDeploy) {
                    String msg = String.format("The user: '%s' is not permitted to deploy '%s' into '%s'.",
                            authService.currentUsername(), path, repoPath);
                    status.error(msg, HttpStatus.SC_FORBIDDEN, log);
                    AccessLogger.deployDenied(repoPath);
                }
            }
            if (!status.isError()) {
                PathDeletionContext pathDeletionContext = new PathDeletionContext.Builder(repo, path, status)
                        .assertOverwrite(true).requestSha1(validDeployPathContext.getRequestSha1())
                        .requestSha2(validDeployPathContext.getRequestSha2())
                        .forceExpiryCheck(validDeployPathContext.isForceExpiryCheck()).build();
                assertDelete(pathDeletionContext);
            }

            if (!status.isError()) {
                // Assert that we don't exceed the user configured maximum storage size
                assertStorageQuota(status, validDeployPathContext.getContentLength());
            }
        }
        if (status.isError()) {
            if (status.getException() != null) {
                Throwable throwable = status.getException();
                if (throwable instanceof RepoRejectException) {
                    throw (RepoRejectException) throwable;
                }
                throw new RepoRejectException(throwable);
            }
            throw new RepoRejectException(status.getStatusMsg(), status.getStatusCode());
        }
    }

    private void assertStorageQuota(MutableStatusHolder statusHolder, long contentLength) {
        StorageQuotaInfo info = storageService.getStorageQuotaInfo(contentLength);
        if (info == null) {
            return;
        }

        if (info.isLimitReached()) {
            // Note: don't display the disk usage in the status holder - this message is written back to the user
            statusHolder.error(
                    "Datastore disk usage is too high. Contact your Artifactory administrator to add additional " +
                            "storage space or change the disk quota limits.", HttpStatus.SC_REQUEST_TOO_LONG, log
            );

            log.error(info.getErrorMessage());
        } else if (info.isWarningLimitReached()) {
            log.warn(info.getWarningMessage());
        }
    }

    @Override
    public <T extends RemoteRepoDescriptor> ResourceStreamHandle downloadAndSave(InternalRequestContext requestContext,
            RemoteRepo<T> remoteRepo, RepoResource res) throws IOException, RepoRejectException {
        return remoteRepo.downloadAndSave(requestContext, res);
    }

    @Override
    public RepoResource unexpireIfExists(LocalRepo localCacheRepo, String path) {
        RepoResource resource = internalUnexpireIfExists(localCacheRepo, path);
        if (resource == null) {
            return new UnfoundRepoResource(InternalRepoPathFactory.create(localCacheRepo.getKey(), path),
                    "Object is not in cache");
        }
        return resource;
    }

    @Override
    public ResourceStreamHandle unexpireAndRetrieveIfExists(InternalRequestContext requestContext,
            LocalRepo localCacheRepo, String path) throws IOException, RepoRejectException {
        RepoResource resource = internalUnexpireIfExists(localCacheRepo, path);
        if (resource != null && resource.isFound()) {
            return localCacheRepo.getResourceStreamHandle(requestContext, resource);
        }
        return null;
    }

    @Override
    public ResourceStreamHandle getResourceStreamHandle(InternalRequestContext requestContext, Repo repo,
            RepoResource res) throws IOException, RepoRejectException, BinaryRejectedException {
        if (res instanceof ResolvedResource) {
            RepoRequests.logToContext("The requested resource is already resolved - using a string resource handle");
            // resource already contains the content - just extract it and return a string resource handle
            String content = ((ResolvedResource) res).getContent();
            return new StringResourceStreamHandle(content);
        } else {
            RepoRequests.logToContext("The requested resource isn't pre-resolved");
            RepoPath repoPath = res.getRepoPath();
            if (repo.isReal()) {
                RepoRequests.logToContext("Target repository isn't virtual - verifying that downloading is allowed");
                //Permissions apply only to real repos
                StatusHolder holder = ((RealRepo) repo).checkDownloadIsAllowed(repoPath);
                if (!holder.isError()) {
                    holder = ((RealRepo) repo).checkDownloadIsNotBlocked(repoPath);
                }
                if (holder.isError()) {
                    RepoRequests.logToContext("Download isn't allowed - received status {} and message '%s'",
                            holder.getStatusCode(), holder.getStatusMsg());
                    throw new RepoRejectException(holder.getStatusMsg(), holder.getStatusCode());
                }
            }
            return repo.getResourceStreamHandle(requestContext, res);
        }
    }

    @Override
    public RepoResource saveResource(StoringRepo repo, SaveResourceContext saveContext)
            throws IOException, RepoRejectException, BinaryRejectedException {
        // save binary early without opening DB transaction (except in full db mode)
        SaveResourceContext newSaveContext;
        try {
            BinaryInfo binaryInfo;
            InputStream in = saveContext.getInputStream();
            if (in instanceof BinaryServiceInputStream) {
                // input stream is from existing binary
                binaryInfo = ((BinaryServiceInputStream) in).getBinaryInfo();
            } else {
                BinaryStream binaryStream = binaryService
                        .createBinaryStream(in, getExpectedChecksums(repo, saveContext));
                binaryInfo = binaryService.addBinary(binaryStream);
            }
            newSaveContext = new SaveResourceContext.Builder(saveContext).binaryInfo(binaryInfo).build();
        } catch (IOException | BinaryStorageException e) {
            saveContext.setException(e);    // signal error
            log.debug("", e);
            throw e;
        }
        return getTransactionalMe().saveResourceInTransaction(repo, newSaveContext);
    }

    private ChecksumsInfo getExpectedChecksums(StoringRepo repo, SaveResourceContext saveContext) {
        //Expected checksums are passed on the RepoResource, use them only if the checksum policy requires it.
        return repo.getChecksumPolicy().shouldVerifyBadClientChecksum()
                ? saveContext.getRepoResource().getInfo().getChecksumsInfo() : null;
    }

    @Override
    public RepoResource saveResourceInTransaction(StoringRepo repo, SaveResourceContext saveContext)
            throws IOException, RepoRejectException {
        return repo.saveResource(saveContext);
    }

    @Override
    public VersionSearchResults getVersionUnitsUnder(RepoPath repoPath) {
        VersionUnitSearchControls controls = new VersionUnitSearchControls(repoPath);
        return searchService.searchVersionUnits(controls);
    }

    @Override
    public long getArtifactCount() {
        return artifactCountRetriever.getCount();
    }

    @Override
    public List<VirtualRepoDescriptor> getVirtualReposContainingRepo(RepoDescriptor repoDescriptor) {
        RepoDescriptor descriptor = repoDescriptor;
        if (repoDescriptor instanceof LocalCacheRepoDescriptor) {
            //VirtualRepoResolver does not directly support local cache repos, so if the items descriptor is a cache,
            //We extract the caches remote repo, and use it instead
            descriptor = ((LocalCacheRepoDescriptor) repoDescriptor).getRemoteRepo();
        }

        List<VirtualRepoDescriptor> reposToDisplay = new ArrayList<>();
        List<VirtualRepoDescriptor> virtualRepos = getVirtualRepoDescriptors();
        for (VirtualRepoDescriptor virtualRepo : virtualRepos) {
            VirtualRepoResolver resolver = new VirtualRepoResolver(virtualRepo);
            if (resolver.contains(descriptor)) {
                reposToDisplay.add(virtualRepo);
            }
        }
        return reposToDisplay;
    }

    @Override
    public List<VirtualRepoDescriptor> getVirtualReposContainingRepo(RepoDescriptor repoDescriptor, int depth) {
        RepoDescriptor descriptor = repoDescriptor;
        if (repoDescriptor instanceof LocalCacheRepoDescriptor) {
            //VirtualRepoResolver does not directly support local cache repos, so if the items descriptor is a cache,
            //We extract the caches remote repo, and use it instead
            descriptor = ((LocalCacheRepoDescriptor) repoDescriptor).getRemoteRepo();
        }
        List<VirtualRepoDescriptor> reposToDisplay = new ArrayList<>();
        List<VirtualRepoDescriptor> virtualRepos = getVirtualRepoDescriptors();
        for (VirtualRepoDescriptor virtualRepo : virtualRepos) {
            VirtualRepoResolver resolver = new VirtualRepoResolver(virtualRepo,depth);
            if (resolver.contains(descriptor)) {
                reposToDisplay.add(virtualRepo);
            }
        }
        return reposToDisplay;
    }

    /**
     * Returns a list of local (non-cache) repo descriptors that the current user is permitted to deploy to.
     *
     * @return List of deploy-permitted local repos
     */
    @Override
    public List<LocalRepoDescriptor> getDeployableRepoDescriptors() {
        // if the user is an admin user, simply return all the deployable descriptors without checking specific
        // permission targets.
        if (authService.isAdmin()) {
            return getLocalRepoDescriptors();
        }
        List<RepoPermissionTarget> permissionTargets = aclService.getRepoPermissionTargets(ArtifactoryPermission.DEPLOY);
        Set<LocalRepoDescriptor> permittedDescriptors = Sets.newHashSet();
        Map<String, LocalRepoDescriptor> descriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        for (RepoPermissionTarget repoPermissionTargetInfo : permissionTargets) {
            List<String> repoKeys = repoPermissionTargetInfo.getRepoKeys();
            if (repoKeys.contains(PermissionTarget.ANY_REPO) || repoKeys.contains(PermissionTarget.ANY_LOCAL_REPO)) {
                return handleAnyPermission();
            }
            for (String repoKey : repoKeys) {
                LocalRepoDescriptor permittedDescriptor = descriptorMap.get(repoKey);
                if (permittedDescriptor != null) {
                    permittedDescriptors.add(permittedDescriptor);
                }
            }
        }
        addBuildInfoRepoIfNeeded(permittedDescriptors);
        return Lists.newArrayList(permittedDescriptors);
    }

    /**
     * If the user has ANY or ANY_LOCAL then they can deploy anywhere (apart from the build info repo which is a special
     * case handled here)
     */
    private List<LocalRepoDescriptor> handleAnyPermission() {
        // return the list of all local repositories
        List<LocalRepoDescriptor> localRepoDescriptors = getLocalRepoDescriptors();
        localRepoDescriptors.removeIf(repo -> RepoType.BuildInfo.equals(repo.getType())
                && !authService.hasBuildPermission(ArtifactoryPermission.DEPLOY));
        return localRepoDescriptors;
    }

    /**
     * Adds the Build Info repo to the list of deployable repos if user has deploy permission on it.
     */
    private void addBuildInfoRepoIfNeeded(Set<LocalRepoDescriptor> permittedDescriptors) {
        LocalRepoDescriptor buildInfoRepo = localRepoDescriptorByKey(buildService.getBuildInfoRepoKey());
        if (authService.hasBuildPermission(ArtifactoryPermission.DEPLOY)) {
            permittedDescriptors.add(buildInfoRepo);
        }
    }

    @Override
    public boolean isLocalOrCachedRepoPathAccepted(RepoPath repoPath) {
        LocalRepo repo = getLocalOrCachedRepository(repoPath);
        return repo == null || repo.accepts(repoPath);
    }

    @Override
    public boolean isRepoPathAccepted(RepoPath repoPath) {
        Repo repo = repositoryByKey(repoPath.getRepoKey());
        return !(repo instanceof RepoBase) || ((RepoBase) repo).accepts(repoPath);
    }

    @Override
    public boolean isRepoPathVisible(RepoPath repoPath) {
        return (repoPath != null) && authService.canRead(repoPath) &&
                (isLocalOrCachedRepoPathAccepted(repoPath) || authService.canAnnotate(repoPath));
    }

    @Override
    public boolean isLocalOrCachedRepoPathAcceptedOrCanAnotate(RepoPath repoPath) {
        return (repoPath != null) &&
                (isLocalOrCachedRepoPathAccepted(repoPath) || authService.canAnnotate(repoPath));
    }

    @Override
    public boolean isRepoPathHandled(RepoPath repoPath) {
        String path = repoPath.getPath();
        if (repoPath.isRoot()) {
            return true;
        }
        LocalRepo repo = getLocalOrCachedRepository(repoPath);
        return repo.handlesReleaseSnapshot(path);
    }

    @Override
    public List<RemoteRepoDescriptor> getSharedRemoteRepoConfigs(String remoteUrl, Map<String, String> headersMap) {

        List<RemoteRepoDescriptor> remoteRepos = Lists.newArrayList();
        List<RepoDetails> remoteReposDetails = getSharedRemoteRepoDetails(remoteUrl, headersMap);
        boolean hasDefaultProxy = centralConfigService.defaultProxyDefined();
        ProxyDescriptor defaultProxy = centralConfigService.getMutableDescriptor().getDefaultProxy();
        for (RepoDetails remoteRepoDetails : remoteReposDetails) {
            String configurationUrl = remoteRepoDetails.getConfiguration();
            if (org.apache.commons.lang.StringUtils.isNotBlank(configurationUrl)) {
                RemoteRepoDescriptor remoteRepoConfig = getSharedRemoteRepoConfig(configurationUrl, headersMap);
                if (remoteRepoConfig != null) {
                    if (hasDefaultProxy && defaultProxy != null) {
                        ((HttpRepoDescriptor) remoteRepoConfig).setProxy(defaultProxy);
                    }
                    RepoLayout repoLayout = remoteRepoConfig.getRepoLayout();

                    //If there is no contained layout or if it doesn't exist locally, just add the default
                    if ((repoLayout == null) ||
                            centralConfigService.getDescriptor().getRepoLayout(repoLayout.getName()) == null) {
                        remoteRepoConfig.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
                    }

                    RepoLayout remoteRepoLayout = remoteRepoConfig.getRemoteRepoLayout();
                    //If there is contained layout doesn't exist locally, remove it
                    if ((remoteRepoLayout != null) &&
                            centralConfigService.getDescriptor().getRepoLayout(remoteRepoLayout.getName()) == null) {
                        remoteRepoConfig.setRemoteRepoLayout(null);
                    }

                    remoteRepos.add(remoteRepoConfig);
                }
            }
        }

        return remoteRepos;
    }

    @Override
    public Tree<ZipEntryInfo> zipEntriesToTree(RepoPath zipPath) throws IOException {
        LocalRepo localRepo = getLocalOrCachedRepository(zipPath);
        VfsFile file = localRepo.getImmutableFile(zipPath);
        ZipInputStream zin = null;
        try {
            Tree<ZipEntryInfo> tree;
            zin = new ZipInputStream(file.getStream());
            ZipEntry zipEntry;
            tree = InfoFactoryHolder.get().createZipEntriesTree();
            try {
                while ((zipEntry = zin.getNextEntry()) != null) {
                    tree.insert(InfoFactoryHolder.get().createZipEntry(zipEntry));
                }
                // IllegalArgumentException is being thrown from: java.util.zip.ZipInputStream.getUTF8String on a
                // bad archive
            } catch (IllegalArgumentException e) {
                throw new IOException(
                        "An error occurred while reading entries from zip file: " + file.getRepoPath());
            }
            return tree;
        } finally {
            IOUtils.closeQuietly(zin);
        }
    }

    @Override
    public ArchiveInputStream archiveInputStream(RepoPath zipPath) throws IOException {
        LocalRepo localRepo = getLocalOrCachedRepository(zipPath);
        VfsFile file = localRepo.getImmutableFile(zipPath);
        String zipSuffix = zipPath.getPath().toLowerCase();
        return ZipUtils.returnArchiveInputStream(file.getStream(), zipSuffix);
    }

    @Override
    public ItemInfo getLastModified(RepoPath pathToSearch) {
        if (pathToSearch == null) {
            throw new IllegalArgumentException("Repo path cannot be null.");
        }
        if (!exists(pathToSearch)) {
            throw new ItemNotFoundRuntimeException("Could not find item: " + pathToSearch.getId());
        }

        return collectLastModified(pathToSearch);
    }

    private ItemInfo collectLastModified(RepoPath pathToSearch) {
        TreeBrowsingCriteria criteria = new TreeBrowsingCriteriaBuilder().applySecurity().build();
        ItemTree itemTree = new ItemTree(pathToSearch, criteria);
        LinkedList<ItemNode> fringe = Lists.newLinkedList();
        fringe.add(itemTree.getRootNode());
        ItemInfo lastModified = null;
        while (!fringe.isEmpty()) {
            ItemNode last = fringe.removeLast();
            if (last.hasChildren()) {
                fringe.addAll(last.getChildren());
            }
            if (!last.isFolder()) {
                if (lastModified == null || last.getItemInfo().getLastModified() > lastModified.getLastModified()) {
                    lastModified = last.getItemInfo();
                }
            }
        }
        return lastModified;
    }

    @Override
    public void touch(RepoPath repoPath) {
        if (repoPath == null) {
            throw new IllegalArgumentException("Repo path cannot be null.");
        }
        LocalRepo localOrCachedRepository = getLocalOrCachedRepository(repoPath);
        if (localOrCachedRepository == null) {
            throw new IllegalArgumentException(repoPath + " is not local or cache repository path");
        }
        MutableVfsItem mutableFsItem = localOrCachedRepository.getMutableFsItem(repoPath);
        if (mutableFsItem == null) {
            throw new ItemNotFoundRuntimeException("Could not find item: " + repoPath.getId());
        }
        mutableFsItem.setModified(System.currentTimeMillis());
    }

    @Override
    public void fixChecksums(RepoPath fileRepoPath) {
        MutableVfsFile mutableFile = getMutableFile(fileRepoPath);
        FileInfo fileInfo = mutableFile.getInfo();
        ChecksumsInfo checksumsInfo = fileInfo.getChecksumsInfo();
        for (ChecksumInfo checksumInfo : checksumsInfo.getChecksums()) {
            if (!checksumInfo.checksumsMatch()) {
                mutableFile.setClientChecksum(checksumInfo.getType(), ChecksumInfo.TRUSTED_FILE_MARKER);
            }
        }
    }

    private void exportAsync(@Nonnull String repoKey, ExportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        TaskBase task = TaskUtils.createManualTask(ExportJob.class, 0L, "Export Repository - " + repoKey);
        task.addAttribute(Task.REPO_KEY, repoKey);
        task.addAttribute(ExportSettingsImpl.class.getName(), settings);
        taskService.startTask(task, true);
        boolean completed = taskService.waitForTaskCompletion(task.getToken());
        if (!completed) {
            if (!status.isError()) {
                // Add Error of no completion
                status.error("The task " + task + " did not complete correctly", log);
            }
        }
    }

    @Override
    @Nonnull
    public LocalRepo getLocalRepository(RepoPath repoPath) {
        if (RepoPathUtils.isTrash(repoPath)) {
            return cache.get().trashcan;
        }
        String repoKey = repoPath.getRepoKey();
        LocalRepo localRepo = localOrCachedRepositoryByKey(repoKey);
        if (localRepo == null) {
            localRepo = distributionRepoByKey(repoKey);
        }
        if (localRepo == null) {
            localRepo = releaseBundleRepositoryByKey(repoKey);
        }
        if (localRepo == null) {
            throw new IllegalArgumentException("Repository '" + repoKey + "' is not a local repository");
        }
        return localRepo;
    }

    private List<String> getLocalAndCacheRepoKeys() {
        List<String> result = new ArrayList<>();
        for (LocalRepoDescriptor localRepoDescriptor : getLocalAndCachedRepoDescriptors()) {
            result.add(localRepoDescriptor.getKey());
        }
        result.addAll(cache.get().distributionRepositoriesMap.keySet());
        result.addAll(cache.get().releaseBundlesRepoMap.keySet());
        return result;
    }


    private RepoResource internalUnexpireIfExists(LocalRepo repo, String path) {
        // Need to release the read lock first
        RepoPath repoPath = InternalRepoPathFactory.create(repo.getKey(), path);
        RepoPath fsItemRepoPath = NamingUtils.getLockingTargetRepoPath(repoPath);
        // Write lock auto upgrade supported LockingHelper.releaseReadLock(fsItemRepoPath);
        MutableVfsItem fsItem = repo.getMutableFsItem(fsItemRepoPath);
        if (fsItem != null) {
            log.debug("{}: falling back to using cache entry for resource info at '{}'.", this, path);
            //Reset the resource age so it is kept being cached
            fsItem.setUpdated(System.currentTimeMillis());
            return repo.getInfo(new NullRequestContext(repoPath));
        }
        return null;
    }

    private void assertDelete(PathDeletionContext pathDeletionContext) {
        StoringRepo repo = pathDeletionContext.getRepo();
        String path = pathDeletionContext.getPath();
        BasicStatusHolder status = pathDeletionContext.getStatus();
        // RTFACT-14918: In case the delete request is to the root of the repository, only admin will succeed.
        if (StringUtils.isBlank(path) && !authService.isAdmin()) {
            status.error(String.format(
                    "Not enough permissions to delete content of repository '%s' (by user: '%s'). This requires admin privileges.",
                    repo, authService.currentUsername()), HttpStatus.SC_FORBIDDEN, log);
            return;
        }
        RepoPath repoPath = InternalRepoPathFactory.create(repo.getKey(), path);
        //Check that has delete rights to replace an exiting item
        if (repo.shouldProtectPathDeletion(pathDeletionContext)) {
            if (!authService.canDelete(repoPath)) {
                AccessLogger.deleteDenied(repoPath);
                if (centralConfigService.getDescriptor().getSecurity().isHideUnauthorizedResources()) {
                    status.error("Could not locate artifact '" + repoPath + "'.", HttpStatus.SC_NOT_FOUND, log);
                } else {
                    status.error(String.format(
                            "Not enough permissions to delete/overwrite artifact '%s' (user: '%s' needs DELETE permission).",
                            repoPath, authService.currentUsername()), HttpStatus.SC_FORBIDDEN,
                            log);
                }
            }
        }

        //For deletion (as opposed to overwrite), check that path actually exists
        if (!pathDeletionContext.isAssertOverwrite() && !repo.itemExists(repoPath.getPath())) {
            status.error("Could not locate artifact '" + repoPath + "' (Nothing to delete).",
                    HttpStatus.SC_NOT_FOUND, log);
        }

        fireAssertDeletePathAllowedEvent(repoPath, status);
    }

    private void fireAssertDeletePathAllowedEvent(RepoPath repoPath, BasicStatusHolder status) {
        StorageInterceptors interceptors = StorageContextHelper.get().beanForType(StorageInterceptors.class);
        interceptors.assertDeletePathAllowed(repoPath, status);
    }

    /**
     * Remove export folders of repositories that are not part of the current backup included repositories
     * this cleanup is needed in incremental backup when a repository is excluded from the backup or removed
     */
    private void cleanupIncrementalBackupDirectory(File targetDir, List<String> reposToBackup) {
        if (!targetDir.exists()) {
            log.debug("Repositories backup directory doesn't exist: {}", targetDir.getAbsolutePath());
            return; // nothing to clean
        }
        File[] childFiles = targetDir.listFiles();
        if (childFiles == null) {
            return;
        }
        for (File childFile : childFiles) {
            String fileName = childFile.getName();
            if (fileName.endsWith(METADATA_FOLDER)) {
                continue;  // skip metadata folders, will delete them with the actual folder if needed
            }
            boolean includedInBackup = false;
            for (String repoKey : reposToBackup) {
                if (fileName.equals(repoKey)) {
                    includedInBackup = true;
                    break;
                }
            }
            if (!includedInBackup) {
                log.info("Deleting {} from the incremental backup dir since it is not part " +
                        "of the backup included repositories", childFile.getAbsolutePath());
                boolean deleted = FileUtils.deleteQuietly(childFile);
                if (!deleted) {
                    log.warn("Failed to delete {}", childFile.getAbsolutePath());
                }
                // now delete the metadata folder of the repository is it exists
                File metadataFolder = new File(childFile.getParentFile(), childFile.getName() + METADATA_FOLDER);
                if (metadataFolder.exists()) {
                    deleted = FileUtils.deleteQuietly(metadataFolder);
                    if (!deleted) {
                        log.warn("Failed to delete metadata folder {}", metadataFolder.getAbsolutePath());
                    }
                }
            }
        }
    }

    private LocalRepo getLocalOrCachedRepository(RepoPath repoPath) {
        return localOrCachedRepositoryByKey(repoPath.getRepoKey());
    }

    /**
     * check if repo exist already in case a new repo is created
     *
     * @return true if exist in cache
     */
    @Override
    public boolean isRepoExistInCache(RepoPath repoPath) {
        return getLocalOrCachedRepository(repoPath) != null;
    }

    // ----- RepoPath migration logic ----- \\

    @Override
    public void updateRepoPathChecksum(RepoPathChecksumCalculationWorkItem workItem) {
        RepoPathChecksumMigrationJobDelegate delegate = workItem.getDelegate();
        RepoPath repoPath = workItem.getRepoPath();
        //do each file in a separate (sync) transaction, we don't want long locks while the conversion runs]
        try {
            delegate.incrementCurrentBatchCount();
            log.debug("Updating repoPath checksum for path {}", repoPath);
            ((MigrationFileService) fileService).updateRepoPathChecksum(repoPath);
            delegate.incrementTotalDone();
        } catch (Exception e) {
            delegate.log().error("Failed to update repoPath checksum for path " + repoPath.toPath(), e);
        }
    }

    // ----- RepoPath migration logic ----- \\

    // ----- SHA256 migration logic ----- \\

    @Override
    public void updateSha2(ChecksumCalculationWorkItem workItem) {
        String sha2 = null;
        Sha256MigrationJobDelegate delegate = workItem.getDelegate();
        String sha1 = workItem.getSha1();
        try {
            try {
                sha2 = delegate.getOrCalculateSha2(sha1);
            } catch (Sha256CalculationFatalException e) {
                //Exception downstream are already logged, just need to mark bad paths.
                delegate.handleExceptionDuringMigration(workItem.getPaths(), e);
            }
            if (StringUtils.isNotBlank(sha2)) {
                for (RepoPath path : workItem.getPaths()) {
                    //do each file in a separate (sync) transaction, we don't want long locks while the conversion runs]
                    try {
                        delegate.incrementCurrentBatchCount();
                        delegate.log().debug("Updating sha256 value '{}' for path {}", sha2, path.toPath());
                        getTransactionalMe().updateSha2ForPath(path, sha2);
                        delegate.incrementTotalDone();
                    } catch (Exception e) {
                        delegate.handleExceptionDuringMigration(Lists.newArrayList(path), e);
                        delegate.log()
                                .error("Failed to update sha256 value '" + sha2 + "' on path " + path.toPath(), e);
                    }
                }
            }
        } finally {
            delegate.markTaskAsFinished(sha1);
        }
    }

    @Override
    public void updateSha2ForPath(RepoPath path, String sha2) {
        MutableVfsFile vfsFile = getMutableFileForMigration(path);
        //Once acquiring a write lock, double-check no one updated sha2 in the meantime (i.e. virtual cache)
        if (vfsFile != null && StringUtils.isBlank(vfsFile.getSha2())) {
            ((Sha256ConvertableMutableVfsFile) vfsFile).setSha2(sha2);
        }
    }

    private MutableVfsFile getMutableFileForMigration(RepoPath path) {
        String repoKey = path.getRepoKey();
        //Trashcan is in local map
        VfsItemFactory storingRepo = localOrCachedRepositoryByKey(path.getRepoKey());
        if (storingRepo == null) {
            storingRepo = virtualRepositoryByKey(path.getRepoKey());
        }
        if (storingRepo == null) {
            throw new ItemNotFoundRuntimeException("Repository '" + repoKey + "' not local, cache or virtual");
        }
        return storingRepo.getMutableFile(path);
    }

    // ----- SHA256 migration logic ----- \\

    /**
     * Aggregates and unifies the given paths by parent
     *
     * @param pathsToMove        Paths to be moved\copied
     * @param targetLocalRepoKey Key of target local repo
     * @return Set of aggregated paths to move
     */
    private Set<RepoPath> aggregatePathsToMove(Set<RepoPath> pathsToMove, String targetLocalRepoKey, boolean copy) {
        // aggregate paths by parent repo path
        Multimap<RepoPath, RepoPath> pathsByParent = HashMultimap.create();
        for (RepoPath pathToMove : pathsToMove) {
            if (!pathToMove.getRepoKey().equals(targetLocalRepoKey)) {
                pathsByParent.put(pathToMove.getParent(), pathToMove);
            }
        }

        // now for each parent check if all its files are moved, and if they do, we will move
        // the parent folder and its children instead of just the children
        Set<RepoPath> pathsToMoveIncludingParents = new HashSet<>();
        for (RepoPath parentPath : pathsByParent.keySet()) {
            Collection<RepoPath> children = pathsByParent.get(parentPath);
            if (parentPath.isRoot()) {
                // parent is the repository itself and cannot be moved, just add the children
                pathsToMoveIncludingParents.addAll(children);
            } else {
                // if the parent children count equals to the number of files to be moved, move the folder instead
                LocalRepo repository = getLocalRepository(parentPath);
                VfsFolder folder =
                        copy ? repository.getImmutableFolder(parentPath) : repository.getMutableFolder(parentPath);
                // get all the folder children using write lock
                List<VfsItem> folderChildren = folder.getImmutableChildren();
                if (folderChildren.size() == children.size()) {
                    pathsToMoveIncludingParents.add(parentPath);
                } else {
                    pathsToMoveIncludingParents.addAll(children);
                }
            }
        }
        return pathsToMoveIncludingParents;
    }

    private String adjustRefererValue(Map<String, String> headersMap, String headerVal) {
        //Append the artifactory uagent to the referer
        if (headerVal == null) {
            //Fallback to host
            headerVal = headersMap.get("HOST");
            if (headerVal == null) {
                //Fallback to unknown
                headerVal = "UNKNOWN";
            }
        }
        if (!headerVal.startsWith("http")) {
            headerVal = "http://" + headerVal;
        }
        try {
            URL uri = new URL(headerVal);
            //Only use the uri up to the path part
            headerVal = uri.getProtocol() + "://" + uri.getAuthority();
        } catch (MalformedURLException e) {
            //Nothing
        }
        headerVal += "/" + HttpUtils.getArtifactoryUserAgent();
        return headerVal;
    }

    /**
     * Returns a list of shared repository details
     *
     * @param remoteUrl  URL of remote Artifactory instance
     * @param headersMap Map of headers to set for client
     * @return List of shared repository details
     */
    private List<RepoDetails> getSharedRemoteRepoDetails(String remoteUrl, Map<String, String> headersMap) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(remoteUrl);
        if (!remoteUrl.endsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append(RestConstants.PATH_API).append("/").append(RepositoriesRestConstants.PATH_ROOT).
                append("?").append(RepositoriesRestConstants.PARAM_REPO_TYPE).append("=").
                append(RepoDetailsType.REMOTE.name());

        try (CloseableHttpResponse response = executeGetMethod(urlBuilder.toString(), headersMap)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return JacksonReader.streamAsValueTypeReference(response.getEntity().getContent(),
                        new TypeReference<List<RepoDetails>>() {
                        }
                );
            } else {
                return Lists.newArrayList();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the shared remote repository descriptor from the given configuration URL
     *
     * @param configUrl  URL of repository configuration
     * @param headersMap Map of headers to set for client
     * @return RemoteRepoDescriptor
     */
    private RemoteRepoDescriptor getSharedRemoteRepoConfig(String configUrl, Map<String, String> headersMap) {
        try (CloseableHttpResponse response = executeGetMethod(configUrl, headersMap)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return JacksonReader.streamAsValueTypeReference(response.getEntity().getContent(),
                        new TypeReference<HttpRepoDescriptor>() {
                        }
                );
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes an HTTP GET method
     *
     * @param url        URL to query
     * @param headersMap Map of headers to set for client
     * @return The http response
     */
    private CloseableHttpResponse executeGetMethod(String url, Map<String, String> headersMap) throws IOException {
        HttpGet getMethod = new HttpGet(url);
        setHeader(getMethod, headersMap, HttpHeaders.USER_AGENT);
        setHeader(getMethod, headersMap, HttpHeaders.REFERER);
        return client.execute(getMethod);
    }

    /**
     * Sets the HTTP headers for the given method
     *
     * @param getMethod  Get method that should be set with the headers
     * @param headersMap Map of headers to set
     * @param headerKey  Key of header to set
     */
    private void setHeader(HttpGet getMethod, Map<String, String> headersMap, String headerKey) {
        String headerVal = headersMap.get(headerKey.toUpperCase());
        if (HttpHeaders.REFERER.equalsIgnoreCase(headerKey)) {
            headerVal = adjustRefererValue(headersMap, headerVal);
        }
        if (headerVal != null) {
            getMethod.setHeader(headerKey, headerVal);
        }
    }

    private void registerRepositoriesMBeans() {
        MBeanRegistrationService registrationService = ContextHelper.get().beanForType(MBeanRegistrationService.class);
        registrationService.unregisterAll(REPOSITORIES_MBEAN_TYPE);
        for (LocalRepoDescriptor descriptor : getLocalAndCachedRepoDescriptors()) {
            registrationService.register(new ManagedRepository(descriptor), REPOSITORIES_MBEAN_TYPE,
                    descriptor.getKey());
        }
    }

    private void startSha2CalculationJob() {
        TaskBase sha2Job = TaskUtils.createManualTask(Sha256MigrationJob.class, 0L);
        taskService.startTask(sha2Job, false);
    }

    private void startRepoPathChecksumJob() {
        TaskBase pathChecksumJob = TaskUtils.createManualTask(RepoPathChecksumMigrationJob.class, 0L);
        taskService.startTask(pathChecksumJob, false);
    }

    private class RepositoriesCacheBuilder {

        // a cache of all the repository keys
        private Set<String> allRepoKeysCache;
        private LocalRepo trashcan;
        private LocalRepo supportBundles;
        private Map<String, LocalRepo> localRepositoriesMap = Maps.newLinkedHashMap();
        private Map<String, RemoteRepo> remoteRepositoriesMap = Maps.newLinkedHashMap();
        private Map<String, LocalCacheRepo> localCacheRepositoriesMap = Maps.newLinkedHashMap();
        private Map<String, VirtualRepo> virtualRepositoriesMap = Maps.newLinkedHashMap();
        private Map<String, DistributionRepo> distributionRepositoriesMap = Maps.newLinkedHashMap();
        private Map<String, ReleaseBundlesRepo> releaseBundleRepositoriesMap = Maps.newLinkedHashMap();
        private Map<RepoType, Collection<Repo>> repoTypeRepositoriesMap = Maps.newTreeMap(new RepoType.RepoNameComparator());
        private GenericHttpRepoFactory genericRemoteRepoFactory = new GenericHttpRepoFactory();
        private Map<Character, List<String>> reposByFirstCharMap;

        RepositoriesCache rebuildRepositories() {
            //Create the repository objects from the descriptor
            CentralConfigDescriptor centralConfig = centralConfigService.getDescriptor();
            InternalRepositoryService transactionalMe = getTransactionalMe();

            //Trashcan
            rebuildTrashcan(centralConfig, transactionalMe);

            //SupportBundles
            rebuildSupportBundles(centralConfig, transactionalMe);

            //Locals
            rebuildLocalRepositories(centralConfig, transactionalMe);

            // Remotes and caches
            rebuildRemoteAndCacheRepositories(centralConfig);

            // Virtuals
            rebuildVirtualRepositories(centralConfig, transactionalMe);

            //Distribution
            rebuildDistributionRepositories(centralConfig, transactionalMe);

            // Release Bundle
            rebuildReleaseBundleRepositories(centralConfig, transactionalMe);

            initAllRepoKeysCache();

            initRepoTypeRepositoriesMap(centralConfig);

            return new RepositoriesCache(releaseBundleRepositoriesMap, distributionRepositoriesMap,
                    virtualRepositoriesMap, localCacheRepositoriesMap, remoteRepositoriesMap, localRepositoriesMap,
                    trashcan, allRepoKeysCache, supportBundles, repoTypeRepositoriesMap, reposByFirstCharMap);
        }

        private void initRepoTypeRepositoriesMap(
                CentralConfigDescriptor centralConfig) {
            Multimap<RepoType, Repo> sortedSetMultimap = TreeMultimap.create(new RepoType.RepoNameComparator(),
                    (repo1, repo2) -> repo1.getKey().compareTo(repo2.getKey()));

            StreamSupportUtils.stream(Lists.newArrayList(Iterables.concat(localRepositoriesMap.values(),
                    distributionRepositoriesMap.values(),
                    localCacheRepositoriesMap.values(),
                    remoteRepositoriesMap.values(),
                    virtualRepositoriesMap.values()))
            ).forEach(repo -> sortedSetMultimap.put(repo.getDescriptor().getType(), repo));
            repoTypeRepositoriesMap = sortedSetMultimap.asMap();
        }

        private void rebuildTrashcan(CentralConfigDescriptor centralConfig, InternalRepositoryService transactionalMe) {
            DbLocalRepo oldTrashcanRepo = null;
            if (trashcan != null) {
                oldTrashcanRepo = (DbLocalRepo<TrashRepoDescriptor>) trashcan;
            }
            TrashRepoDescriptor trashRepoDescriptor = new TrashRepoDescriptor(TrashService.TRASH_KEY,
                    getSimpleLayout(centralConfig));
            trashcan = new DbLocalRepo<>(trashRepoDescriptor, transactionalMe, oldTrashcanRepo);
            trashcan.init();
        }

        private void rebuildSupportBundles(CentralConfigDescriptor centralConfig,
                InternalRepositoryService transactionalMe) {
            DbLocalRepo oldSupportBundles = null;
            if (supportBundles != null) {
                oldSupportBundles = (DbLocalRepo<SupportBundleRepoDescriptor>) supportBundles;
            }
            SupportBundleRepoDescriptor supportBundleDesc = new SupportBundleRepoDescriptor(
                    SUPPORT_BUNDLE_REPO_NAME,
                    getSimpleLayout(centralConfig));
            supportBundles = new DbLocalRepo<>(supportBundleDesc, transactionalMe, oldSupportBundles);
            supportBundles.init();
        }

        private void rebuildLocalRepositories(CentralConfigDescriptor centralConfig,
                InternalRepositoryService transactionalMe) {
            Map<String, LocalRepo> newLocalRepositoriesMap = Maps.newLinkedHashMap();
            for (LocalRepoDescriptor repoDescriptor : centralConfig.getLocalRepositoriesMap().values()) {
                DbLocalRepo<LocalRepoDescriptor> oldLocalRepo = null;
                String key = repoDescriptor.getKey();
                if (!localRepositoriesMap.isEmpty()) {
                    LocalRepo oldRepo = localRepositoriesMap.get(key);
                    if (oldRepo != null) {
                        if (!(oldRepo instanceof DbLocalRepo)) {
                            log.error("Reloading configuration did not find local repository " + key);
                        } else {
                            //noinspection unchecked
                            oldLocalRepo = (DbLocalRepo<LocalRepoDescriptor>) oldRepo;
                        }
                    } else {
                        // This could be a new repo that is in the newly saved config but not in the global map yet.
                        // Only if we do not find it there as well then it is an error
                        LocalRepoDescriptor newLocalRepo = centralConfig.getLocalRepositoriesMap().get(key);
                        if (newLocalRepo == null) {
                            log.error("Reloading configuration did not find local repository {}", key);
                        }
                    }
                }
                LocalRepo repo = new DbLocalRepo<>(repoDescriptor, transactionalMe, oldLocalRepo);
                try {
                    repo.init();
                } catch (Exception e) {
                    log.error("Failed to initialize local repository '{}'. Repository will be blacked-out",
                            repo.getKey(),
                            e);
                    ((LocalRepoDescriptor) repo.getDescriptor()).setBlackedOut(true);
                }
                newLocalRepositoriesMap.put(repo.getKey(), repo);
            }
            localRepositoriesMap = newLocalRepositoriesMap;
        }

        private void rebuildRemoteAndCacheRepositories(CentralConfigDescriptor config) {
            // stop remote repo online monitors
            Map<String, LocalCacheRepo> newMap = Maps.newLinkedHashMap();
            Map<String, RemoteRepo> newRemoteRepositoriesMap = Maps.newLinkedHashMap();
            for (RemoteRepoDescriptor repoDescriptor : config.getRemoteRepositoriesMap().values()) {
                RemoteRepo remoteRepo = createRemoteRepo((HttpRepoDescriptor) repoDescriptor, config.isOfflineMode(),
                        remoteRepositoriesMap.get(repoDescriptor.getKey()));
                try {
                    remoteRepo.init();
                } catch (Exception e) {
                    log.error("Failed to initialize remote repository '" + remoteRepo.getKey() + "'. " +
                            "Repository will be blacked-out!", e);
                    ((HttpRepoDescriptor) remoteRepo.getDescriptor()).setBlackedOut(true);
                }
                newRemoteRepositoriesMap.put(remoteRepo.getKey(), remoteRepo);
                if (remoteRepo.isStoreArtifactsLocally()) {
                    LocalCacheRepo localCacheRepo = remoteRepo.getLocalCacheRepo();
                    if (localCacheRepo != null) {
                        newMap.put(localCacheRepo.getKey(), localCacheRepo);
                    }
                }
            }
            localCacheRepositoriesMap = newMap;
            remoteRepositoriesMap = newRemoteRepositoriesMap;
        }

        private void rebuildVirtualRepositories(CentralConfigDescriptor centralConfig,
                InternalRepositoryService transactionalMe) {
            // Build the virtual repo cache in new map and once the map is read, replace it atomically
            Map<String, VirtualRepo> newMap = Maps.newLinkedHashMap();
            Map<String, VirtualRepoDescriptor> virtualRepoDescriptorMap = centralConfig.getVirtualRepositoriesMap();
            WebstartAddon webstartAddon = addonsManager.addonByType(WebstartAddon.class);
            for (VirtualRepoDescriptor repoDescriptor : virtualRepoDescriptorMap.values()) {
                VirtualRepo repo = webstartAddon.createVirtualRepo(transactionalMe, repoDescriptor);
                newMap.put(repo.getKey(), repo);
            }
            for (VirtualRepo virtualRepo : newMap.values()) {
                virtualRepo.updateRepos(newMap, this::repositoryByKey);
            }

            // 2. call the init method only after all virtual repos exist
            for (VirtualRepo virtualRepo : newMap.values()) {
                virtualRepo.init();
            }
            virtualRepositoriesMap = newMap;
        }

        public Repo repositoryByKey(String key) {
            Repo repo = null;
            if (localRepositoriesMap.containsKey(key)) {
                repo = (localRepositoriesMap.get(key));
            } else if (localCacheRepositoriesMap.containsKey(key)) {
                repo = localCacheRepositoriesMap.get(key);
            } else if (remoteRepositoriesMap.containsKey(key)) {
                repo = remoteRepositoriesMap.get(key);
            } else if (virtualRepositoriesMap.containsKey(key)) {
                repo = virtualRepositoriesMap.get(key);
            } else if (distributionRepositoriesMap.containsKey(key)) {
                repo = distributionRepositoriesMap.get(key);
            } else if (releaseBundleRepositoriesMap.containsKey(key)) {
                repo = distributionRepositoriesMap.get(key);
            } else if (supportBundles != null && key.equals(supportBundles.getKey())) {
                repo = supportBundles;
            } else if (trashcan != null && key.equals(trashcan.getKey())) {
                repo = trashcan;
            }
            return repo;
        }

        private void rebuildDistributionRepositories(CentralConfigDescriptor config,
                InternalRepositoryService transactionalMe) {
            Map<String, DistributionRepoDescriptor> distRepoDescriptorMap = config.getDistributionRepositoriesMap();
            Map<String, DistributionRepo> newDistRepositoriesMap = Maps.newLinkedHashMap();
            for (DistributionRepoDescriptor repoDescriptor : distRepoDescriptorMap.values()) {
                DistributionRepo oldDistRepo = null;
                String key = repoDescriptor.getKey();
                if (!distributionRepositoriesMap.isEmpty()) {
                    DistributionRepo oldRepo = distributionRepositoriesMap.get(key);
                    if (oldRepo != null) {
                        //noinspection unchecked
                        oldDistRepo = oldRepo;
                    } else {
                        // This could be a new repo that is in the newly saved config but not in the global map yet.
                        // Only if we do not find it there as well then it is an error
                        DistributionRepoDescriptor newDistRepo = config.getDistributionRepositoriesMap().get(key);
                        if (newDistRepo == null) {
                            log.error("Reloading configuration did not find local repository {}", key);
                        }
                    }
                }
                DistributionRepo repo = new DistributionRepo(repoDescriptor, transactionalMe, oldDistRepo);
                try {
                    repo.init();
                } catch (Exception e) {
                    log.error("Failed to initialize local repository '{}'. Repository will be blacked-out",
                            repo.getKey(), e);
                    repo.getDescriptor().setBlackedOut(true);
                }
                newDistRepositoriesMap.put(repo.getKey(), repo);
            }
            distributionRepositoriesMap.clear();
            distributionRepositoriesMap.putAll(newDistRepositoriesMap);
        }

        private void rebuildReleaseBundleRepositories(CentralConfigDescriptor centralConfig,
                InternalRepositoryService transactionalMe) {
            Map<String, ReleaseBundlesRepo> newReleaeBundlesRepositoriesMap = Maps.newLinkedHashMap();
            for (ReleaseBundlesRepoDescriptor repoDescriptor : centralConfig.getReleaseBundlesRepositoriesMap().values()) {
                DbLocalRepo<ReleaseBundlesRepoDescriptor> oldRepo = null;
                String key = repoDescriptor.getKey();
                if (!releaseBundleRepositoriesMap.isEmpty()) {
                    ReleaseBundlesRepo oldReleaseRepo = releaseBundleRepositoriesMap.get(key);
                    if (oldReleaseRepo != null) {
                        //noinspection unchecked
                        oldRepo = oldReleaseRepo;
                    } else {
                        // This could be a new repo that is in the newly saved config but not in the global map yet.
                        // Only if we do not find it there as well then it is an error
                        LocalRepoDescriptor newLocalRepo = centralConfig.getDistributionRepositoriesMap().get(key);
                        if (newLocalRepo == null) {
                            log.error("Reloading configuration did not find release bundle repository {}", key);
                        }
                    }
                }
                ReleaseBundlesRepo repo = new ReleaseBundlesRepo(repoDescriptor, transactionalMe, oldRepo);
                try {
                    repo.init();
                } catch (Exception e) {
                    log.error("Failed to initialize release bundle repository '{}'. Repository will be blacked-out",
                            repo.getKey(), e);
                    repo.getDescriptor().setBlackedOut(true);
                }
                newReleaeBundlesRepositoriesMap.put(repo.getKey(), repo);
            }
            releaseBundleRepositoriesMap = newReleaeBundlesRepositoriesMap;
        }

        private void initAllRepoKeysCache() {
            Set<String> newKeys = new HashSet<>();
            newKeys.addAll(localRepositoriesMap.keySet());
            newKeys.addAll(remoteRepositoriesMap.keySet());
            newKeys.addAll(localCacheRepositoriesMap.keySet());
            newKeys.addAll(virtualRepositoriesMap.keySet());
            newKeys.addAll(distributionRepositoriesMap.keySet());
            newKeys.addAll(releaseBundleRepositoriesMap.keySet());
            newKeys.add(supportBundles.getKey());
            allRepoKeysCache = newKeys;

            reposByFirstCharMap = StreamSupportUtils.stream(allRepoKeysCache)
                    .filter(repo -> !StringUtils.equals(supportBundles.getKey(), repo))
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.groupingBy(repoKey -> repoKey.toLowerCase().charAt(0),
                            TreeMap::new, Collectors.toList()));
        }

        private HttpRepo createRemoteRepo(HttpRepoDescriptor descriptor, boolean offlineMode, RemoteRepo oldRemote) {
            return remoteRepoProviders.getOrDefault(descriptor.getType(), genericRemoteRepoFactory)
                    .build(descriptor, offlineMode, oldRemote);
        }

        private class GenericHttpRepoFactory implements HttpRepoFactory {

            @Override
            public HttpRepo build(HttpRepoDescriptor repoDescriptor, boolean offlineMode, RemoteRepo oldRemoteRepo) {
                return new HttpRepo(repoDescriptor, getTransactionalMe(), addonsManager, researchService, offlineMode, oldRemoteRepo);
            }

            @Override
            public RepoType getRepoType() {
                return RepoType.Generic;
            }
        }
    }

    private class RepositoriesCacheLoader implements Callable<RepositoriesCache> {

        @Override
        public RepositoriesCache call() throws Exception {
            InternalRepositoryService transactionalMe = getTransactionalMe();
            return transactionalMe.rebuildRepositoriesInTransaction();
        }
    }

    @Override
    public List<String> getAllLocalRepoKeys() {
        List<String> repoKeys = Lists.newArrayList();
        this.getLocalAndCachedRepoDescriptors().forEach(descriptor -> repoKeys.add(descriptor.getKey()));
        this.getDistributionRepoDescriptors().forEach(descriptor -> repoKeys.add(descriptor.getKey()));
        return repoKeys;
    }

    @Override
    public String getDefaultDeploymentRepoKey(String localOrVirtualRepoKey) {
        if (StringUtils.isBlank(localOrVirtualRepoKey)) {
            return null;
        }
        LocalRepo localRepo = localRepositoryByKey(localOrVirtualRepoKey);
        if (nonNull(localRepo)) {
            return localOrVirtualRepoKey;
        }
        VirtualRepo virtualRepo = virtualRepositoryByKey(localOrVirtualRepoKey);
        if (isNull(virtualRepo)) {
            throw new DoesNotExistException("Unable to find repo " + localOrVirtualRepoKey);
        }
        LocalRepoDescriptor defaultDeploymentRepo = virtualRepo.getDescriptor().getDefaultDeploymentRepo();
        if (isNull(defaultDeploymentRepo)) {
            throw new DoesNotExistException(
                    "Unable to find default deployment repo for virtual repo " + localOrVirtualRepoKey);
        }
        return defaultDeploymentRepo.getKey();
    }

    @Override
    public BasicStatusHolder removeRepository(String repoKey) {
        BasicStatusHolder status = new BasicStatusHolder();

        StorageInterceptors interceptors = StorageContextHelper.get().beanForType(StorageInterceptors.class);
        interceptors.assertDeleteRepoAllowed(repoKey, status);
        if (status.hasErrors()) {
            return status;
        }

        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        configDescriptor.setRepoBlackedOut(repoKey, true);
        centralConfigService.saveEditedDescriptorAndReload(configDescriptor);

        StoringRepo storingRepo = storingRepositoryByKey(repoKey);
        if (storingRepo != null && storingRepo.isLocal()) {
            RepoPath repoPath = InternalRepoPathFactory.create(storingRepo.getKey());
            undeployMultiTransaction(repoPath);
        }
        //remove the repo from config descriptor and than remove all file in single transaction
        //usually no file be remain after first delete with the multiTransaction deletion
        configDescriptor = centralConfigService.getMutableDescriptor();
        configDescriptor.removeRepository(repoKey);
        centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
        return status;
    }

    @Override
    public void onLicenseLoaded() {
        createEdgeUploadsRepoIfNeeded();
    }

    /**
     * For Edges, create a local generic repo named EDGE_UPLOADS_REPO_KEY, to which uploads will be allowed.
     */
    private void createEdgeUploadsRepoIfNeeded() {
        if (!addonsManager.isEdgeLicensed()) {
            return;
        }
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        LocalRepoDescriptor edgeUploadsRepo = mutableDescriptor.getLocalRepositoriesMap().get(EDGE_UPLOADS_REPO_KEY);
        if (edgeUploadsRepo != null && RepoType.Generic.equals(edgeUploadsRepo.getType())) {
            log.trace("Repository '" + EDGE_UPLOADS_REPO_KEY + "' already exists");
            return;
        }
        if (mutableDescriptor.isRepositoryExists(EDGE_UPLOADS_REPO_KEY)) {
            log.warn("Failed to create repository '" + EDGE_UPLOADS_REPO_KEY + "' because a repository with that key already exists");
            return;
        }

        EdgeUtils.addEdgeUploadsRepo(mutableDescriptor);

        try {
            if (USER_SYSTEM.equals(authService.currentUsername()) && AuthenticationHelper.getAuthentication() == null) {
                // This can happen when Artifactory starts with a licence file
                securityService.doAsSystem(() -> {
                    centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
                });
            } else {
                centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
            }
            log.info("Successfully added repository '" + EDGE_UPLOADS_REPO_KEY + "'");
        } catch (Exception e) {
            log.error("Failed to create repository '" + EDGE_UPLOADS_REPO_KEY + "'", e);
        }
    }
}
