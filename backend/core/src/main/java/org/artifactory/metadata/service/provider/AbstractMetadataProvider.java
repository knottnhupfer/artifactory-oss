package org.artifactory.metadata.service.provider;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.storage.db.event.service.metadata.model.MutableArtifactMetadata;
import org.artifactory.storage.db.event.service.metadata.model.MutableMetadataEntityBOM;
import org.jfrog.metadata.client.model.MetadataVersionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 * Copies data into the {@link MutableMetadataEntityBOM}, either from the {@link Properties}, or some other data source
 */
public abstract class AbstractMetadataProvider implements MetadataProvider {
    private static final Logger log = LoggerFactory.getLogger(AbstractMetadataProvider.class);

    static final String COLON = ":";
    static final String ARTIFACTORY_LICENSES = "artifactory.licenses";
    private static final String PACKAGE_ID_SEPARATOR = COLON + "//";
    private static final int MAX_PROPERTY_SIZE = 255;

    private RepoType type;
    private TypeSpecificMetadataTemplate typeSpecificMetadataTemplate;

    AbstractMetadataProvider(RepoType type) {
        this.type = type;
    }

    /**
     * Copy data from the {@link Properties} into the metadata BOM
     *
     * @param metadataBOM - the {@link MutableMetadataEntityBOM} that will hold the metadata
     * @param fileInfo    - the {@link FileInfo} object that containing file related metadata
     * @param properties  - the {@link Properties} key-value store containing package metadata
     */
    @Override
    public void supplement(MutableMetadataEntityBOM metadataBOM, FileInfo fileInfo, Properties properties,
            @Nullable StatsInfo stats) {
        allResolvers()
                .forEach(resolver -> resolver.apply(metadataBOM, properties, fileInfo, stats));
    }

    @Override
    public Set<String> getLeadFileExtensions() {
        return typeSpecificMetadataTemplate.getLeadFileExtensions();
    }

    @Override
    public RepoType getRepoType() {
        return type;
    }

    public abstract boolean isLeadArtifact(RepoPath repoPath);

    protected abstract Optional<AqlApiItem> buildVersionFilesQuery(@Nonnull RepoPath repoPath);

    protected abstract String getType();

    void setTypeSpecificMetadataTemplate(TypeSpecificMetadataTemplate typeSpecificMetadataTemplate) {
        this.typeSpecificMetadataTemplate = typeSpecificMetadataTemplate;
    }

    Optional<ModuleInfo> getModuleInfo(RepoPath repoPath,
            @Nonnull InternalRepositoryService repositoryService) {
        Repo repo = repositoryService.repositoryByKey(repoPath.getRepoKey());
        if (repo != null) {
            try {
                ModuleInfo artifactModuleInfo = repo.getArtifactModuleInfo(repoPath.getPath());
                if (artifactModuleInfo.isValid()) {
                    return Optional.of(artifactModuleInfo);
                }
            } catch (Exception e) {
                log.error("Unable to extract Module Info from path '{}'", repoPath);
                log.debug("", e);
            }
        }
        return Optional.empty();
    }

    String getPkgIdSuffixFromLayout(ModuleInfo moduleInfo, RepoPath repoPath) {
        if (moduleInfo.isValid()) {
            return moduleInfo.getOrganization().replaceAll("/", ".") + COLON + moduleInfo.getModule();
        }
        log.debug("Module Info invalid for path {}", repoPath);
        return null;
    }

    MetadataResolver userPropsResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo statsInfo) -> properties
                .keySet()
                .stream()
                .filter(property -> !typeSpecificMetadataTemplate.getExcludedUserProperties().contains(property))
                .filter(property -> !property.equals(typeSpecificMetadataTemplate.getLicenseKey()))
                .filter(property -> !property.startsWith(ARTIFACTORY_LICENSES))
                .forEach(key -> bom.getUserProperties().put(key, properties.get(key)));
    }

    MetadataResolver tagsResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo statsInfo) -> typeSpecificMetadataTemplate
                .getTagKey().ifPresent(tagKey -> {
                    String rawTagsValue = valueForKey(properties, tagKey);
                    if (StringUtils.isNotBlank(rawTagsValue)) {
                        Set<String> tags = setByTemplateFunction(rawTagsValue,
                                typeSpecificMetadataTemplate.getTagValueResolver());
                        bom.setTags(tags);
                    }
                });
    }

    MetadataResolver qualifiersResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo statsInfo) -> typeSpecificMetadataTemplate
                .getQualifierKeysToMdsKeys()
                .keySet()
                .forEach(qualifierKey -> {
                    String rawValue = valueForKey(properties, qualifierKey);
                    if (StringUtils.isNotBlank(rawValue)) {
                        String value = valueByTemplateFunction(rawValue,
                                typeSpecificMetadataTemplate.getQualifierValueResolver());
                        if (StringUtils.isNotBlank(value)) {
                            bom.getQualifiers()
                                    .put(typeSpecificMetadataTemplate.getQualifierKeysToMdsKeys().get(qualifierKey),
                                            value);
                        }
                    }
                });
    }

    MetadataResolver pkgIdResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo statsInfo) -> {
            String pkgidSuffix = typeSpecificMetadataTemplate.getPkgidSuffix(fileInfo.getRepoPath(), properties);
            if (StringUtils.isNotBlank(pkgidSuffix)) {
                bom.setPkgid(trimToMaxSize(getRepoType().getPkgidPrefix() + PACKAGE_ID_SEPARATOR + pkgidSuffix));
            } else {
                log.debug("No pkgid resolved for path '{}'", fileInfo.getRepoPath());
            }
        };
    }

    MetadataResolver packageDescriptionResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo statsInfo) -> {
            Optional<String> descriptionKey = typeSpecificMetadataTemplate.getDescriptionKey();
            if (descriptionKey.isPresent()) {
                String description = valueForKey(properties, descriptionKey.get());
                if (StringUtils.isNotBlank(description)) {
                    bom.setDescription(description);
                }
            }
        };
    }

    MetadataResolver versionResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo statsInfo) -> {
            if (typeSpecificMetadataTemplate.isPropertyBased()) {
                typeSpecificMetadataTemplate.getVersionKey().ifPresent(nameKey -> {
                    String version = valueForKey(properties, nameKey);
                    if (StringUtils.isNotBlank(version)) {
                        bom.setVersion(version);
                    }
                });
            } else {
                bom.setVersion(typeSpecificMetadataTemplate.getVersion(fileInfo.getRepoPath()));
            }
        };
    }

    String valueForKey(Properties properties, String key) {
        String value = properties.getFirst(key);
        if (StringUtils.isBlank(value)) {
            if (log.isDebugEnabled()) {
                log.debug("Artifact Metadata provider has no metadata for property '{}'", key);
            }
            return "";
        }
        return trimValueIfNeeded(key, value);
    }

    String trimValueIfNeeded(@Nonnull String key, @Nonnull String value) {
        if (shouldTrimByKey(key)) {
            return trimToMaxSize(value);
        }
        return value;
    }

    MetadataResolver nameResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo statsInfo) -> {
            if (typeSpecificMetadataTemplate.isPropertyBased()) {
                typeSpecificMetadataTemplate.getNameKey().ifPresent(nameKey -> {
                    String name = valueForKey(properties, nameKey);
                    if (StringUtils.isNotBlank(name)) {
                        bom.setName(name);
                    }
                });
            } else {
                String name = typeSpecificMetadataTemplate.getName(fileInfo.getRepoPath());
                if (StringUtils.isNotBlank(name)) {
                    bom.setName(name);
                }
            }
        };
    }

    private boolean shouldTrimByKey(@Nonnull String key) {
        return typeSpecificMetadataTemplate.getDescriptionKey().map(descKey -> !descKey.equals(key)).orElse(true) &&
                typeSpecificMetadataTemplate.getTagKey().map(tagKey -> !tagKey.equals(key)).orElse(true);
    }

    private String trimToMaxSize(@Nonnull String value) {
        return value.length() > MAX_PROPERTY_SIZE ? value.substring(0, MAX_PROPERTY_SIZE) : value;
    }

    private MetadataResolver typeResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo stats) -> bom
                .setPackageType(getType().toLowerCase());
    }

    private MetadataResolver licenseResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo statsInfo) -> {
            Set<String> licenses = collectionForKey(properties, typeSpecificMetadataTemplate.getLicenseKey());
            if (CollectionUtils.isNotEmpty(licenses)) {
                bom.setLicenses(new ArrayList<>(licenses));
            }
        };
    }

    private MetadataResolver fileInfoResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo leadArtifactFileInfo, StatsInfo stats) -> {
            // Artifact metadata
            setArtifactMetadata(bom, leadArtifactFileInfo);
            // Version creation date
            bom.setCreated(leadArtifactFileInfo.getCreated());
            // Version Repo
            setVersionRepo(bom, leadArtifactFileInfo, stats);
        };
    }

    private MetadataResolver statsResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo stats) -> {
            if (stats != null) {
                bom.setDownloadCount(stats.getDownloadCount());
            }
        };
    }

    private MetadataResolver issuesResolver() {
        return (MutableMetadataEntityBOM bom, Properties properties, FileInfo fileInfo, StatsInfo stats) -> {
            Optional<String> issuesKey = typeSpecificMetadataTemplate.getIssuesKey();
            if (issuesKey.isPresent()) {
                String issuesUrl = valueForKey(properties, issuesKey.get());
                if (StringUtils.isNotBlank(issuesUrl)) {
                    bom.setIssuesUrl(issuesUrl);
                }
            }
        };
    }

    private Set<MetadataResolver> allResolvers() {
        return ImmutableSet.of(
                nameResolver(),
                versionResolver(),
                licenseResolver(),
                pkgIdResolver(),
                userPropsResolver(),
                qualifiersResolver(),
                tagsResolver(),
                packageDescriptionResolver(),
                fileInfoResolver(),
                typeResolver(),
                issuesResolver(),
                statsResolver());
    }

    private void setVersionRepo(MutableMetadataEntityBOM bom, FileInfo leadArtifactFileInfo,
            StatsInfo stats) {
        RepositoryService repositoryService = ContextHelper.get().beanForType(RepositoryService.class);
        boolean isLocal =
                !repositoryService.localOrCachedRepoDescriptorByKey(leadArtifactFileInfo.getRepoKey()).isCache()
                        && repositoryService.localOrCachedRepoDescriptorByKey(leadArtifactFileInfo.getRepoKey())
                        .isLocal();
        String baseUrl = ContextHelper.get().beanForType(CentralConfigService.class).getDescriptor()
                .getUrlBase();
        String repoUrl = null;
        if (!StringUtils.isBlank(baseUrl)) {
            repoUrl = baseUrl + "/" + leadArtifactFileInfo.getRepoPath().toPath();
        } else {
            log.debug("The Artifactory Base URL is not set. Repository URL not populated.");
        }
        MetadataVersionRepo metadataVersionRepo = new MetadataVersionRepo(leadArtifactFileInfo.getRepoKey(),
                repoUrl, isLocal ? "local" : "remote", leadArtifactFileInfo.getRepoPath().getPath(),
                stats != null ? stats.getDownloadCount() : null);
        bom.setRepos(Collections.singletonList(metadataVersionRepo));
    }

    private void setArtifactMetadata(MutableMetadataEntityBOM bom, FileInfo leadArtifactFileInfo) {
        List<MutableArtifactMetadata> versionFiles = findVersionFiles(leadArtifactFileInfo);
        MutableArtifactMetadata leadArtifact = new MutableArtifactMetadata(
                leadArtifactFileInfo.getName(), leadArtifactFileInfo.getMimeType(), leadArtifactFileInfo.getSize(),
                leadArtifactFileInfo.getMd5(), leadArtifactFileInfo.getSha1(), leadArtifactFileInfo.getSha2(),
                true);
        versionFiles.add(leadArtifact);
        bom.setArtifactMetadata(versionFiles);
    }

    private List<MutableArtifactMetadata> findVersionFiles(FileInfo leadArtifactFileInfo) {
        Optional<AqlApiItem> versionFilesQuery = buildVersionFilesQuery(leadArtifactFileInfo.getRepoPath());
        if (versionFilesQuery.isPresent()) {
            AqlService aqlService = ContextHelper.get().beanForType(AqlService.class);
            AqlEagerResult<AqlItem> aqlResult = aqlService.executeQueryEager(versionFilesQuery.get());
            if (!aqlResult.getResults().isEmpty()) {
                return aqlResult.getResults().stream()
                        .filter(item -> !item.getName().equals(leadArtifactFileInfo.getName()))
                        .map(item -> new MutableArtifactMetadata(item.getName(), null, item.getSize(),
                                item.getActualMd5(), item.getActualSha1(), item.getSha2(), false))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private Set<String> collectionForKey(Properties properties, String key) {
        Set<String> collection = properties.get(key);
        if (CollectionUtils.isEmpty(collection)) {
            if (log.isDebugEnabled()) {
                log.debug("Artifact Metadata provider has no metadata for property '{}'", key);
            }
            return Collections.emptySet();
        }
        return collection;
    }

    private String valueByTemplateFunction(String value, Function<String, String> function) {
        if (StringUtils.isBlank(value)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping extraction of value by function from an empty value");
            }
            return "";
        }
        return function.apply(value);
    }

    private Set<String> setByTemplateFunction(String value, Function<String, Set<String>> function) {
        if (StringUtils.isBlank(value)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping extraction of value by function from an empty value");
            }
            return new HashSet<>();
        }
        return function.apply(value);
    }
}
