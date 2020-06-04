package org.artifactory.metadata.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.md.Properties;
import org.artifactory.metadata.service.provider.AbstractMetadataProvider;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.event.service.metadata.mapper.MetadataEntityMapper;
import org.artifactory.storage.db.event.service.metadata.model.MutableMetadataEntityBOM;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.PropertiesService;
import org.artifactory.storage.fs.service.StatsService;
import org.jfrog.metadata.client.model.*;
import org.jfrog.metadata.client.model.event.MetadataEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A facade for creating {@link MetadataEntity}'s
 *
 * @author Uriah Levy
 */
@Component
public class MetadataEntityFacade {
    private static final Logger log = LoggerFactory.getLogger(MetadataEntityFacade.class);
    private FileService fileService;
    private PropertiesService propertiesService;
    private RepositoryService repositoryService;
    private MetadataEntityMapper metadataEntityMapper;
    private Map<RepoType, AbstractMetadataProvider> metadataProvidersMap;
    private MetadataEventService metadataEventService;

    @Autowired
    public MetadataEntityFacade(List<AbstractMetadataProvider> metadataProviders, FileService fileService,
            PropertiesService propertiesService, RepositoryService repositoryService,
            MetadataEntityMapper metadataEntityMapper) {
        this.metadataProvidersMap = metadataProviders.stream()
                .collect(Collectors.toMap(AbstractMetadataProvider::getRepoType,
                        Function.identity()));
        this.fileService = fileService;
        this.propertiesService = propertiesService;
        this.repositoryService = repositoryService;
        this.metadataEntityMapper = metadataEntityMapper;
    }

    void setMetadataService(MetadataEventServiceImpl metadataEventService) {
        this.metadataEventService = metadataEventService;
    }

    Optional<Pair<MetadataEntity, MetadataEventEntity>> createMetadataEntity(PrioritizedNodeEvent event) {
        if (isDeleteEvent(event.getType())) {
            Optional<MetadataEntity> pathDeletedEntity = createPathDeletedEntity(event.getRepoPath());
            return pathDeletedEntity.map(entity -> Pair.of(entity, MetadataEventEntity.PATH_DELETED));
        } else {
            Optional<MetadataPackage> mdsPackageEntity = createMdsPackageEntity(event.getRepoPath());
            return mdsPackageEntity.map(entity -> Pair.of(entity, MetadataEventEntity.PACKAGE_CREATED));
        }
    }

    Optional<MetadataPackage> createMdsPackageEntity(RepoPath repoPath) {
        Optional<ItemInfo> itemInfo = loadItem(repoPath);
        return itemInfo.map(this::createMdsPackage);
    }

    MetadataStatistics getMdsItemStatistics(Map<RepoPath, Long> pathToDownloadCount) {
        List<MetadataItemStatistics> itemStatistics = pathToDownloadCount.entrySet()
                .stream()
                .filter(e -> isLeadArtifact(e.getKey()))
                .filter(e -> e.getValue() > 0)
                .map(e -> new MetadataItemStatistics(e.getKey().toPath(), e.getValue()))
                .collect(Collectors.toList());
        return new MetadataStatistics(itemStatistics);
    }

    boolean isLeadArtifact(RepoPath repoPath) {
        if (!repoPath.isFile() || repoPath.isRoot()) { // <- root is a file :(
            return false;
        }
        Optional<AbstractMetadataProvider> metadataProvider = metadataProviderForItem(repoPath.getRepoKey());
        return metadataProvider.map(provider -> provider.isLeadArtifact(repoPath)).orElse(false);
    }

    Optional<Set<String>> getSupportedExtensionsByRepoType(RepoType repoType) {
        AbstractMetadataProvider metadataProvider = metadataProvidersMap.get(repoType);
        return Optional.ofNullable(metadataProvider).map(AbstractMetadataProvider::getLeadFileExtensions);
    }

    private Optional<AbstractMetadataProvider> metadataProviderForItem(String repoKey) {
        Optional<LocalRepoDescriptor> repoDescriptor = getLocalOrCacheRepoDescriptor(repoKey);
        if (repoDescriptor.isPresent()) {
            return Optional.ofNullable(metadataProvidersMap.get(repoDescriptor.get().getType()));
        }
        log.debug("Metadata provider not found for repo '{}'", repoKey);
        return Optional.empty();
    }

    Optional<LocalRepoDescriptor> getLocalOrCacheRepoDescriptor(String repoKey) {
        LocalRepoDescriptor repoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        if (repoDescriptor == null) {
            log.debug("Unable to associate Metadata event with a local or cache repository.");
            return Optional.empty();
        }
        return Optional.of(repoDescriptor);
    }

    @Nonnull
    private StatsService statsService() {
        return ContextHelper.get().beanForType(StatsService.class);
    }

    private boolean isDeleteEvent(EventType type) {
        return type.equals(EventType.delete);
    }

    private Optional<MetadataEntity> createPathDeletedEntity(RepoPath repoPath) {
        // The version repo is the deleted metadata entity
        return Optional.of(new MetadataVersionRepo(repoPath.getRepoKey(), repoPath.getPath()));
    }

    private Optional<ItemInfo> loadItem(RepoPath repoPath) {
        try {
            return Optional.of(fileService.loadItem(repoPath));
        } catch (RuntimeException e) {
            log.debug("Unable to find item with path '{}'. Metadata event handling will be skipped.",
                    repoPath.toPath());
            return Optional.empty();
        }
    }

    private MetadataPackage createMdsPackage(ItemInfo item) {
        Properties properties = propertiesService.getProperties(item);
        Optional<MutableMetadataEntityBOM> mutableMetadataBOM = createMutableMetadataEntityBOM(item, properties, getStatsIfNeeded(item));
        return mutableMetadataBOM
                .map(mutableMetadataEntityBOM -> metadataBomToMdsPackage(mutableMetadataEntityBOM, item.getRepoPath()))
                .orElse(null);
    }

    private StatsInfo getStatsIfNeeded(ItemInfo item) {
        if (metadataEventService.isMigrating()) {
            return statsService().getStats((FileInfo) item);
        }
        return null;
    }

    private Optional<MutableMetadataEntityBOM> createMutableMetadataEntityBOM(ItemInfo itemInfo,
            Properties properties, @Nullable StatsInfo stats) {
        Optional<AbstractMetadataProvider> metadataProvider = metadataProviderForItem(itemInfo.getRepoKey());
        if (metadataProvider.isPresent()) {
            MutableMetadataEntityBOM metadataBom = new MutableMetadataEntityBOM();
            metadataProvider.get().supplement(metadataBom, (FileInfo) itemInfo, properties, stats);
            return Optional.of(metadataBom);
        }
        return Optional.empty();
    }

    private MetadataPackage metadataBomToMdsPackage(MutableMetadataEntityBOM mutableMetadataBOM,
            RepoPath repoPath) {
        MetadataPackage metadataPackage = metadataEntityMapper.metadataBomToMdsPackage(mutableMetadataBOM);
        addVersionFromBom(mutableMetadataBOM, metadataPackage);
        addFilesFromBom(mutableMetadataBOM, metadataPackage);
        if (!validPackageEvent(metadataPackage, repoPath)) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring invalid metadata event for path '{}'", repoPath.toPath());
            }
            return null;
        }
        return metadataPackage;
    }

    private void addFilesFromBom(MutableMetadataEntityBOM mutableMetadataBOM, MetadataPackage metadataPackage) {
        if (metadataPackage.getVersions().get(0).getFiles() != null) {
            metadataPackage.getVersions().get(0).getFiles().addAll(mdsFilesFromBomArtifacts(mutableMetadataBOM));
        }
    }

    private void addVersionFromBom(MutableMetadataEntityBOM mutableMetadataBOM, MetadataPackage metadataPackage) {
        List<MetadataVersion> versions = metadataPackage.getVersions();
        if (versions != null) {
            versions.add(metadataEntityMapper.metadataBomToMdsVersion(mutableMetadataBOM));
        }
    }

    private boolean validPackageEvent(MetadataPackage metadataPackage, RepoPath repoPath) {
        if (StringUtils.isBlank(metadataPackage.getPkgid())) {
            if (log.isDebugEnabled()) {
                log.debug("pkgid is empty for {}", repoPath.toPath());
            }
            return false;
        }
        if (metadataPackage.getVersions().isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Versions array is empty for {}", repoPath.toPath());
            }
            return false;
        }
        if (StringUtils.isBlank(metadataPackage.getVersions().get(0).getName())) {
            if (log.isDebugEnabled()) {
                log.debug("Version name is empty for {}", repoPath.toPath());
            }
            return false;
        }
        return true;
    }

    private List<MetadataFile> mdsFilesFromBomArtifacts(MutableMetadataEntityBOM mutableMetadataBOM) {
        return mutableMetadataBOM.getArtifactMetadata().stream()
                .map(artifact -> metadataEntityMapper.metadataBomToMdsFile(artifact))
                .collect(Collectors.toList());
    }
}
