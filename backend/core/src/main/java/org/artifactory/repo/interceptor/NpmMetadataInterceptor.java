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

package org.artifactory.repo.interceptor;

import com.google.common.collect.Sets;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.npm.NpmAddon;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.MetadataVerificationException;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.md.PropertiesInfo;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.StorageAggregationInterceptor;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.sapi.interceptor.context.InterceptorMoveCopyContext;
import org.artifactory.storage.fs.MutableVfsFile;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.InternalStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Shay Yaakov
 */
public class NpmMetadataInterceptor extends StorageInterceptorAdapter implements StorageAggregationInterceptor {

    private static final Logger log = LoggerFactory.getLogger(NpmMetadataInterceptor.class);
    private static final String NPM_DISTTAG = "npm.disttag";

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder, InterceptorCreateContext ctx) {
        if (shouldTakeActionForRemoteMetadata(fsItem)) {
            try {
                addonsManager.addonByType(NpmAddon.class).validateNpmPackageJson(fsItem, statusHolder);
            } catch (MetadataVerificationException e) {
                throw new RuntimeException(e);
            }
        }
        if (shouldTakeAction(fsItem)) {
            if (hasVirginProps(fsItem)) {
                MutableVfsFile file = (MutableVfsFile) fsItem;
                PropertiesInfo originalProperties = file.getOriginalProperties();
                Set<String> deletions = getBeforeDelta(originalProperties, fsItem.getProperties(), NPM_DISTTAG);
                if (CollectionUtils.notNullOrEmpty(deletions)) {
                    addonsManager.addonByType(NpmAddon.class).addNpmPackage(((FileInfo) fsItem.getInfo()), deletions);
                    return;
                }
            }
            addonsManager.addonByType(NpmAddon.class).addNpmPackage(((FileInfo) fsItem.getInfo()), new HashSet<>());
        }
    }

    @Override
    public void afterDelete(VfsItem fsItem, MutableStatusHolder statusHolder, DeleteContext ctx) {
        if (shouldTakeAction(fsItem)) {
            if (hasVirginProps(fsItem)) {
                MutableVfsFile file = (MutableVfsFile) fsItem;
                PropertiesInfo originalProperties = file.getOriginalProperties();
                Set<String> deletedTags = originalProperties.get(NPM_DISTTAG);
                if (CollectionUtils.notNullOrEmpty(deletedTags)) {
                    addonsManager.addonByType(NpmAddon.class)
                            .removeNpmPackage(((FileInfo) fsItem.getInfo()), deletedTags);
                    return;
                }
            }
            addonsManager.addonByType(NpmAddon.class).removeNpmPackage(((FileInfo) fsItem.getInfo()), new HashSet<>());
        }
    }

    @Override
    public void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties, InterceptorMoveCopyContext ctx) {
        if (shouldTakeAction(sourceItem)) {
            addonsManager.addonByType(NpmAddon.class).removeNpmPackage(((FileInfo) sourceItem.getInfo()), new HashSet<>());
        }

        if (shouldTakeAction(targetItem)) {
            addonsManager.addonByType(NpmAddon.class).handleAddAfterCommit(((FileInfo) targetItem.getInfo()));
        }
    }

    @Override
    public void beforePropertyCreate(VfsItem fsItem, MutableStatusHolder statusHolder, String key, String... values) {
        if (fsItem instanceof MutableVfsFile && !((MutableVfsFile) fsItem).isOriginallyNew()) {
            ((MutableVfsFile) fsItem).markOriginalProperties();
        }
    }

    @Override
    public void beforePropertyDelete(VfsItem fsItem, MutableStatusHolder statusHolder, String key) {
        if (fsItem instanceof MutableVfsFile && !((MutableVfsFile) fsItem).isOriginallyNew()) {
            ((MutableVfsFile) fsItem).markOriginalProperties();
        }
    }

    @Override
    public void afterPropertyCreate(VfsItem fsItem, MutableStatusHolder statusHolder, String key, String... values) {
        if (shouldTakeAction(fsItem) && NPM_DISTTAG.equals(key)) {
            log.debug("Detected {} property created on: {}", NPM_DISTTAG, fsItem.getRepoPath());
            if (hasVirginProps(fsItem)) {
                collectDeletedTagsAndCalculate(fsItem, key, values);
                return;
            }
            log.debug("Detected {} property created on: {} with no previous props, indexing package", NPM_DISTTAG, fsItem.getRepoPath());
            addonsManager.addonByType(NpmAddon.class).handleTags((FileInfo) fsItem.getInfo(), new HashSet<>());
        }
    }

    @Override
    public void beforeCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
        if (!shouldTakeAction(fsItem)) {
            return;
        }
        MutableVfsFile mutableFile = (MutableVfsFile) fsItem;
        if (!mutableFile.isOriginallyNew()) {
            mutableFile.markOriginalProperties();
        }
    }

    @Override
    public void beforeDelete(VfsItem fsItem, MutableStatusHolder statusHolder, boolean moved)  {
        // If its not an override the item will not have properties yet and we'll not go in here.
        if (!shouldTakeAction(fsItem)) {
            return;
        }
        MutableVfsFile mutableFile = (MutableVfsFile) fsItem;
        if (!mutableFile.isOriginallyNew()) {
            mutableFile.markOriginalProperties();
        }
    }

    private boolean shouldTakeActionForRemoteMetadata(VfsItem fsItem) {
        if (ConstantValues.npmRemoteMetadataContentValidation.getBoolean() && fsItem.isFile() && fsItem.getName().equals("package.json")) {
            String repoKey = InternalStringUtils.replaceLast(fsItem.getRepoKey(), RepoPath.REMOTE_CACHE_SUFFIX, "");
            RepoDescriptor repoDescriptor = repositoryService.remoteRepoDescriptorByKey(repoKey);
            return ((repoDescriptor != null) && repoDescriptor.getType().equals(RepoType.Npm));
        }
        return false;
    }

    /**
     * Delta that returns all *before properties* that are no longer in the *after properties* (deleted properties)
     */
    private Set<String> getBeforeDelta(@Nullable PropertiesInfo oldProps, PropertiesInfo currentProps,
            String key) {
        Set<String> delta = Sets.newHashSet();
        if (oldProps != null) {
            delta = getPropValues(oldProps, key).stream()
                    .filter(oldValue -> !getPropValues(currentProps, key).contains(oldValue))
                    .collect(Collectors.toSet());
        }
        return delta;
    }

    private Set<String> getPropValues(@Nullable PropertiesInfo props, String propKey) {
        return props != null ? Optional.ofNullable(props.get(propKey)).orElse(Sets.newHashSet()) : Sets.newHashSet();
    }

    @Override
    public void afterPropertyDelete(VfsItem fsItem, MutableStatusHolder statusHolder, String key) {
        if (shouldTakeAction(fsItem) && NPM_DISTTAG.equals(key)) {
            if (hasVirginProps(fsItem) && fsItem.getProperties().get(NPM_DISTTAG) != null) {
                collectDeletedTagsAndCalculate(fsItem, key, fsItem.getProperties().get(NPM_DISTTAG).toArray(new String[0]));
                return;
            }
            addonsManager.addonByType(NpmAddon.class).handleTags((FileInfo) fsItem.getInfo(), new HashSet<>());
        }
    }

    @Override
    public void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties, InterceptorMoveCopyContext ctx) {
        if (shouldTakeAction(targetItem)) {
            addonsManager.addonByType(NpmAddon.class).handleAddAfterCommit(((FileInfo) targetItem.getInfo()));
        }
    }

    @Override
    public void afterRepoImport(RepoPath rootRepoPath, int itemsCount, MutableStatusHolder status) {
        if (repositoryService.localRepoDescriptorByKey(rootRepoPath.getRepoKey()) != null) {
            addonsManager.addonByType(NpmAddon.class).reindexAsync(rootRepoPath);
        }
    }

    private boolean shouldTakeAction(VfsItem item) {
        if (item.isFile()) {
            RepoPath repoPath = item.getRepoPath();
            if (repoPath.getPath().endsWith(".tgz")) {
                String repoKey = repoPath.getRepoKey();
                RepoDescriptor repoDescriptor = repositoryService.localRepoDescriptorByKey(repoKey);
                return ((repoDescriptor != null) && repoDescriptor.getType().equals(RepoType.Npm));
            }
        }

        return false;
    }

    private boolean hasVirginProps(VfsItem fsItem) {
        if ((fsItem instanceof MutableVfsFile)) {
            MutableVfsFile file = (MutableVfsFile) fsItem;
            return !file.isOriginallyNew() && file.isOriginalPropertiesAvailable();
        }
        return false;
    }

    private void collectDeletedTagsAndCalculate(VfsItem fsItem, String key, String[] values) {
        MutableVfsFile file = (MutableVfsFile) fsItem;
        PropertiesInfo originalProperties = file.getOriginalProperties();
        Properties newProps = new PropertiesImpl();
        newProps.putAll(key, values);
        Set<String> deletions = getBeforeDelta(originalProperties, newProps, key);
        log.debug("Starting handleTags for package:{}, with deleted tags: {}", fsItem.getRepoPath(), Arrays.toString(deletions.toArray()));
        addonsManager.addonByType(NpmAddon.class).handleTags((FileInfo) fsItem.getInfo(), deletions);
    }
}
