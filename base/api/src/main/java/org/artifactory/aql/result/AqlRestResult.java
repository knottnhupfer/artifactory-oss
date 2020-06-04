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

package org.artifactory.aql.result;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author Gidi Shabat
 */
public abstract class AqlRestResult implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(AqlRestResult.class);

    private AqlPermissionProvider permissionProvider;

    AqlRestResult(AqlPermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    protected boolean canRead(AqlDomainEnum domain, ResultSet resultSet) {
        if (permissionProvider.isAdmin()) {
            return true;
        } else {
            if (AqlDomainEnum.items == domain) {
                try {
                    String itemRepo = resultSet.getString(AqlPhysicalFieldEnum.itemRepo.name());
                    String itemPath = resultSet.getString(AqlPhysicalFieldEnum.itemPath.name());
                    String itemName = resultSet.getString(AqlPhysicalFieldEnum.itemName.name());
                    RepoPath repoPath = AqlUtils.fromAql(itemRepo, itemPath, itemName);
                    return permissionProvider.canRead(repoPath);
                } catch (Exception e) {
                    logMinimalFieldError(e);
                }
            }
            return false;
        }
    }

    protected boolean canRead(AqlDomainEnum domain, String repo, String path, String name) {
        if (permissionProvider.isAdmin()) {
            return true;
        } else {
            if (AqlDomainEnum.items == domain) {
                try {
                    RepoPath repoPath = AqlUtils.fromAql(repo, path, name);
                    return permissionProvider.canRead(repoPath);
                } catch (Exception e) {
                    logMinimalFieldError(e);
                }
            }
            return false;
        }
    }

    public abstract byte[] read();

    private void logMinimalFieldError(Exception e) {
        log.error("AQL minimal field expectation error: repo, path and name", e);
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonPropertyOrder(value = {"itemRepo", "itemPath", "itemName", "itemType", "itemSize", "itemCreated", "itemCreatedBy", "itemModified", "itemModifiedBy", "itemUpdated", "itemDepth"}, alphabetic = true)
    public static class Row {
        @JsonIgnore
        public Map<String, Row> subDomains;
        @JsonIgnore
        public AqlDomainEnum domain;
        @JsonIgnore
        public Long statId;
        @JsonIgnore
        public Long itemId;
        @JsonIgnore
        public Long propertyId;
        @JsonIgnore
        public Long propertyItemId;
        @JsonIgnore
        public Long buildId;
        @JsonIgnore
        public Long buildPropertyId;
        @JsonIgnore
        public Long buildArtifactId;
        @JsonIgnore
        public Long modulePropertyId;
        @JsonIgnore
        public Long moduleId;
        @JsonIgnore
        public Long buildDependencyId;
        @JsonIgnore
        public Long archiveEntryPathId;
        @JsonIgnore
        public Long statRemoteId;
        // release artifacts
        @JsonIgnore
        public Long releaseBundleFileId;
        @JsonIgnore
        public Long releaseBundleFileNodeId;
        @JsonIgnore
        public Long releaseBundleFileBundleId;


        @JsonProperty("repo")
        public String itemRepo;
        @JsonProperty("path")
        public String itemPath;
        @JsonProperty("name")
        public String itemName;
        @JsonProperty("size")
        public Long itemSize;
        @JsonProperty("depth")
        public Integer itemDepth;
        @JsonProperty("modified")
        public String itemModified;
        @JsonProperty("created")
        public String itemCreated;
        @JsonProperty("updated")
        public String itemUpdated;
        @JsonProperty("created_by")
        public String itemCreatedBy;
        @JsonProperty("modified_by")
        public String itemModifiedBy;
        @JsonProperty("type")
        public AqlItemTypeEnum itemType;
        @JsonProperty("virtual_repos")
        public String[] itemVirtualRepos;
        @JsonProperty("original_md5")
        public String itemOriginalMd5;
        @JsonProperty("actual_md5")
        public String itemActualMd5;
        @JsonProperty("original_sha1")
        public String itemOriginalSha1;
        @JsonProperty("actual_sha1")
        public String itemActualSha1;
        @JsonProperty("sha256")
        public String itemSha2;
        @JsonProperty("repo_path_checksum")
        public String repoPathChecksum;
        @JsonProperty("downloaded")
        public String statDownloaded;
        @JsonProperty("downloads")
        public Integer statDownloads;
        @JsonProperty("downloaded_by")
        public String statDownloadedBy;
        @JsonProperty("remote_downloaded_by")
        public String statRemoteDownloadedBy;
        @JsonProperty("remote_downloads")
        public Integer statRemoteDownloads;
        @JsonProperty("remote_downloaded")
        public String statRemoteDownloaded;
        @JsonProperty("remote_path")
        public String statRemotePath;
        @JsonProperty("remote_origin")
        public String statRemoteOrigin;
        @JsonProperty("key")
        public String propertyKey;
        @JsonProperty("value")
        public String propertyValue;
        @JsonProperty("entry.name")
        public String archiveEntryName;
        @JsonProperty("entry.path")
        public String archiveEntryPath;
        @JsonProperty("module.name")
        public String moduleName;
        @JsonProperty("module.property.key")
        public String modulePropertyKey;
        @JsonProperty("module.property.value")
        public String modulePropertyValue;
        @JsonProperty("dependency.name")
        public String buildDependencyName;
        @JsonProperty("dependency.scope")
        public String buildDependencyScope;
        @JsonProperty("dependency.type")
        public String buildDependencyType;
        @JsonProperty("dependency.sha1")
        public String buildDependencySha1;
        @JsonProperty("dependency.md5")
        public String buildDependencyMd5;
        @JsonProperty("artifact.name")
        public String buildArtifactName;
        @JsonProperty("artifact.type")
        public String buildArtifactType;
        @JsonProperty("artifact.sha1")
        public String buildArtifactSha1;
        @JsonProperty("artifact.md5")
        public String buildArtifactMd5;
        @JsonProperty("build.property.key")
        public String buildPropertyKey;
        @JsonProperty("build.property.value")
        public String buildPropertyValue;
        @JsonProperty("build.url")
        public String buildUrl;
        @JsonProperty("build.name")
        public String buildName;
        @JsonProperty("build.number")
        public String buildNumber;
        @JsonProperty("build.started")
        public String buildStarted;
        @JsonProperty("build.created")
        public String buildCreated;
        @JsonProperty("build.created_by")
        public String buildCreatedBy;
        @JsonProperty("build.modified")
        public String buildModified;
        @JsonProperty("build.modified_by")
        public String buildModifiedBy;

        @JsonProperty("build.promotion.created")
        public String buildPromotionCreated;
        @JsonProperty("build.promotion.created_by")
        public String buildPromotionCreatedBy;
        @JsonProperty("build.promotion.user")
        public String buildPromotionUserName;
        @JsonProperty("build.promotion.status")
        public String buildPromotionStatus;
        @JsonProperty("build.promotion.repo")
        public String buildPromotionRepo;
        @JsonProperty("build.promotion.comment")
        public String buildPromotionComment;
        // Release bundle
        @JsonProperty("release.name")
        public String releaseBundleName;
        @JsonProperty("release.version")
        public String releaseBundleVersion;
        @JsonProperty("release.status")
        public String releaseBundleStatus;
        @JsonProperty("release.created")
        public String releaseBundleCreated;
        @JsonProperty("release.signature")
        public String releaseBundleSignature;
        @JsonProperty("release.type")
        public String releaseBundleType;
        // Release bundle files
        @JsonProperty("release_artifact.repo_path")
        public String releaseBundleFileRepoPath;

        @JsonProperty("items")
        public List<Row> items;
        @JsonProperty("properties")
        public List<Row> properties;
        @JsonProperty("stats")
        public List<Row> statistics;
        @JsonProperty("archives")
        public List<Row> archives;
        @JsonProperty("entries")
        public List<Row> entries;
        @JsonProperty("artifacts")
        public List<Row> artifacts;
        @JsonProperty("dependencies")
        public List<Row> dependencies;
        @JsonProperty("modules")
        public List<Row> modules;
        @JsonProperty("module.properties")
        public List<Row> moduleProperties;
        @JsonProperty("builds")
        public List<Row> builds;
        @JsonProperty("build.properties")
        public List<Row> buildProperties;
        @JsonProperty("build.promotions")
        public List<Row> buildPromotions;
        @JsonProperty("releaseBundles")
        public List<Row> releaseBundles;
        @JsonProperty("releaseBundleFiles")
        public List<Row> releaseBundleFiles;


        public Row(AqlDomainEnum domain) {
            this.domain = domain;
        }

        public void put(String fieldName, Object value) {
            try {
                Field declaredField = getClass().getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                declaredField.set(this, value);
            } catch (Exception e) {
                log.error("Failed to fill Aql result {}: with value: {}", fieldName, value);
                log.debug("Failed to fill Aql result {}: with value: {}", fieldName, value, e);
            }
        }

        public void merge(Row row) {
            merge(row, this);
        }

        private boolean merge(Row source, Row target) {
            boolean containsData = mapFields(source, target);
            boolean childContainsData = false;
            if (source.subDomains != null) {
                for (String id : source.subDomains.keySet()) {
                    Row sourceSubRow = source.subDomains.get(id);
                    if (target.subDomains == null) {
                        target.subDomains = Maps.newHashMap();
                    }
                    Row targetSubRow = target.subDomains.get(id);
                    if (targetSubRow == null) {
                        targetSubRow = new Row(sourceSubRow.getDomain());
                        target.subDomains.put(id, targetSubRow);
                    }
                    childContainsData = merge(sourceSubRow, targetSubRow);
                    if (!childContainsData) {
                        target.subDomains.remove(id);
                    }
                }
            }
            return containsData || childContainsData;
        }

        private boolean mapFields(Row source, Row target) {
            boolean containsData = false;
            try {
                Field[] declaredFields = source.getClass().getFields();
                for (Field declaredField : declaredFields) {
                    if (!declaredField.getName().equals("subDomains") && !declaredField.getName().equals("domain")) {
                        Object value = declaredField.get(source);
                        //Special behaviour for archive domain, since archive domain is built by two tables and doesn't have real key
                        containsData = true;
                        declaredField.set(target, value);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new AqlException("failed to map result fields", e);
            }
            return containsData;
        }

        public Row build() {
            if (subDomains != null) {
                for (Row row : subDomains.values()) {
                    row.build();
                    AqlDomainEnum domainEnum = row.getDomain();
                    switch (domainEnum) {
                        case items: {
                            if (this.items == null) {
                                this.items = Lists.newArrayList();
                            }
                            this.items.add(row);
                            break;
                        }
                        case properties: {
                            if (this.properties == null) {
                                this.properties = Lists.newArrayList();
                            }
                            this.properties.add(row);
                            break;
                        }
                        case statistics: {
                            if (this.statistics == null) {
                                this.statistics = Lists.newArrayList();
                            }
                            this.statistics.add(row);
                            break;
                        }
                        case archives: {
                            if (this.archives == null) {
                                this.archives = Lists.newArrayList();
                            }
                            this.archives.add(row);
                            break;
                        }
                        case entries: {
                            if (this.entries == null) {
                                this.entries = Lists.newArrayList();
                            }
                            this.entries.add(row);
                            break;
                        }
                        case artifacts: {
                            if (this.artifacts == null) {
                                this.artifacts = Lists.newArrayList();
                            }
                            this.artifacts.add(row);
                            break;
                        }
                        case dependencies: {
                            if (this.dependencies == null) {
                                this.dependencies = Lists.newArrayList();
                            }
                            this.dependencies.add(row);
                            break;
                        }
                        case modules: {
                            if (this.modules == null) {
                                this.modules = Lists.newArrayList();
                            }
                            this.modules.add(row);
                            break;
                        }
                        case moduleProperties: {
                            if (this.moduleProperties == null) {
                                this.moduleProperties = Lists.newArrayList();
                            }
                            this.moduleProperties.add(row);
                            break;
                        }
                        case builds: {
                            if (this.builds == null) {
                                this.builds = Lists.newArrayList();
                            }
                            this.builds.add(row);
                            break;
                        }
                        case buildProperties: {
                            if (this.buildProperties == null) {
                                this.buildProperties = Lists.newArrayList();
                            }
                            this.buildProperties.add(row);
                            break;
                        }
                        case buildPromotions: {
                            if (this.buildPromotions == null) {
                                this.buildPromotions = Lists.newArrayList();
                            }
                            this.buildPromotions.add(row);
                            break;
                        }
                        case releaseBundles: {
                            if (this.releaseBundles == null) {
                                this.releaseBundles = Lists.newArrayList();
                            }
                            this.releaseBundles.add(row);
                            break;
                        }
                        case releaseBundleFiles: {
                            if (this.releaseBundleFiles == null) {
                                this.releaseBundleFiles = Lists.newArrayList();
                            }
                            this.releaseBundleFiles.add(row);
                            break;
                        }

                    }
                }
            }
            return this;
        }

        public AqlDomainEnum getDomain() {
            return domain;
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonPropertyOrder(value = {"start", "end", "total"}, alphabetic = true)
    protected class Range {

        @JsonProperty("start_pos")
        protected Long start;
        @JsonProperty("end_pos")
        protected Long end;
        @JsonProperty("total")
        protected Long total;
        @JsonProperty("limit")
        protected Long limited;

        public Range(long start, long end, long limited) {
            this.start = start;
            this.end = end;
            this.limited = Long.MAX_VALUE == limited ? null : limited;
        }

        public Range(long start, long end, long total, long limited) {
            this.start = start;
            this.end = end;
            this.total = total;
            this.limited = Long.MAX_VALUE == limited ? null : limited;
        }
    }
}
