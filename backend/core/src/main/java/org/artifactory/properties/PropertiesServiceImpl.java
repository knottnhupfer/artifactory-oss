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

package org.artifactory.properties;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.properties.LockableAddProperties;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.exception.CancelException;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.io.checksum.ChecksumUtils;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.StoringRepo;
import org.artifactory.repo.interceptor.StorageInterceptors;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.security.AccessLogger;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.fs.MutableVfsFile;
import org.artifactory.storage.fs.MutableVfsFolder;
import org.artifactory.storage.fs.MutableVfsItem;
import org.artifactory.storage.fs.lock.LockingHelper;
import org.artifactory.storage.fs.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the properties service.
 *
 * @author Yossi Shaul
 */
@Service
public class PropertiesServiceImpl implements PropertiesService, LockableAddProperties {
    private static final Logger log = LoggerFactory.getLogger(PropertiesServiceImpl.class);

    @Autowired
    private InternalRepositoryService repoService;

    @Autowired
    private org.artifactory.storage.fs.service.PropertiesService dbPropertiesService;

    @Autowired
    private StorageInterceptors interceptors;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public boolean hasProperties(RepoPath repoPath) {
        MutableVfsItem mutableSessionItem = LockingHelper.getIfWriteLockedByMe(repoPath);
        if (mutableSessionItem != null) {
            return mutableSessionItem.getProperties().isEmpty();
        } else {
            return dbPropertiesService.hasProperties(repoPath);
        }
    }

    @Override
    @Nonnull
    public Properties getProperties(RepoPath repoPath) {
        return getPropertiesInternal(repoPath, null);
    }

    @Override
    @Nonnull
    public Properties getProperties(ItemInfo itemInfo) {
        return getPropertiesInternal(itemInfo.getRepoPath(), itemInfo);
    }

    @Nonnull
    @Override
    public Map<Long, Set<String>> getProperties(List<Long> nodeIdList, String propKey) {
        return dbPropertiesService.getProperties(nodeIdList, propKey);
    }

    // if we have item info it is better to use it because we can avoid a db call.
    private Properties getPropertiesInternal(@Nonnull RepoPath repoPath, @Nullable ItemInfo itemInfo) {
        if (!authService.canRead(repoPath)) {
            AccessLogger.downloadDenied(repoPath);
            return new PropertiesImpl();
        }
        MutableVfsItem mutableItem = LockingHelper.getIfWriteLockedByMe(repoPath);
        if (mutableItem != null) {
            return mutableItem.getProperties();
        } else {
            if (itemInfo != null) {
                return dbPropertiesService.getProperties(itemInfo);
            } else {
                return dbPropertiesService.getProperties(repoPath);
            }
        }
    }

    @Override
    public Map<RepoPath, Properties> getProperties(Set<RepoPath> repoPaths, String... mandatoryKeys) {
        Map<RepoPath, Properties> map = Maps.newHashMap();
        for (RepoPath path : repoPaths) {
            Properties properties = getProperties(path);
            boolean containsAllKeys = true;
            for (String mandatoryKey : mandatoryKeys) {
                if (!properties.containsKey(mandatoryKey)) {
                    containsAllKeys = false;
                    break;
                }
            }
            if (containsAllKeys) {
                map.put(path, properties);
            }
        }
        return map;
    }

    @Override
    public Map<Long, Properties> getAllProperties(String repoKey, String propKey, List<String> propValues) {
        return dbPropertiesService.getAllProperties(repoKey, propKey, propValues);
    }

    @Override
    public boolean setProperties(RepoPath repoPath, Properties properties, boolean isInternalProperties) {
        if (isInternalProperties) {
            return setMetadataProperties(repoPath, properties);
        } else {
            return setArtifactProperties(repoPath, properties);
        }
    }

    @Override
    public boolean removePropertyValues(RepoPath repoPath, String propKey, Set<String> valuesToRemove, boolean isInternalProperties) {
        MutableVfsItem mutableItem = repoService.getMutableItem(repoPath);
        Properties modifiedProperties = new PropertiesImpl(mutableItem.getProperties());
        Set<String> propValues = modifiedProperties.get(propKey);
        if (propValues != null) {
            propValues.removeAll(valuesToRemove);
        }
        if (isInternalProperties) {
            return setMetadataProperties(repoPath, modifiedProperties);
        } else {
            return setArtifactProperties(repoPath, modifiedProperties);
        }
    }

    @Override
    public boolean addProperties(RepoPath repoPath, String propKey, Set<String> valuesToAdd, boolean isInternalProperties) {
        MutableVfsItem mutableItem = repoService.getMutableItem(repoPath);
        Properties modifiedProperties = new PropertiesImpl(mutableItem.getProperties());
        modifiedProperties.putAll(propKey, valuesToAdd);
        if (isInternalProperties) {
            return setMetadataProperties(repoPath, modifiedProperties);
        } else {
            return setArtifactProperties(repoPath, modifiedProperties);
        }
    }

    boolean setMetadataProperties(RepoPath repoPath, Properties properties) {
        if (!assertCanAnnotate(repoPath)) {
            return false;
        }
        try {
            MutableVfsItem mutableItem = repoService.getMutableItem(repoPath);
            mutableItem.setProperties(properties);
            ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
            replicationAddon.offerLocalReplicationPropertiesChangeEvent(repoPath, mutableItem.isFile());
        } catch (ItemNotFoundRuntimeException e) {
            log.error("Cannot set properties for {}: Item not found.", repoPath);
            return false;
        }
        return true;
    }

    boolean setArtifactProperties(RepoPath repoPath, Properties properties) {
        if (!assertCanAnnotate(repoPath)) {
            return false;
        }
        try {
            MutableVfsItem mutableItem = repoService.getMutableItem(repoPath);
            Properties modifiedProperties = filterPreExistingProperties(properties, mutableItem);
            BasicStatusHolder statusHolder = new BasicStatusHolder();
            goThroughBeforeCreateProperties(repoPath, mutableItem, modifiedProperties, statusHolder);
            mutableItem.setProperties(properties);
            goThroughAfterCreateProperties(mutableItem, modifiedProperties, statusHolder);
        } catch (ItemNotFoundRuntimeException e) {
            log.error("Cannot set properties for {}: Item not found.", repoPath);
            return false;
        }
        return true;
    }

    private Properties filterPreExistingProperties(Properties properties, MutableVfsItem mutableItem) {
        Properties current = mutableItem.getProperties();
        Properties modifiedProperties = new PropertiesImpl(properties);
        //This is safe because it is a set of map entries, thus each key can be present only once
        current.entries().forEach(entry -> {
            String propKey = entry.getKey();
            String propNewValue = properties.getFirst(entry.getKey());
            if (properties.containsKey(propKey) && propNewValue != null && propNewValue.equals(entry.getValue())) {
                modifiedProperties.removeAll(entry.getKey());
            }
        });
        return modifiedProperties;
    }

    void goThroughBeforeCreateProperties(RepoPath repoPath, MutableVfsItem mutableItem,
            Properties modifiedProperties, BasicStatusHolder statusHolder) {
        modifiedProperties.entries().forEach(propKey -> {
            interceptors.beforePropertyCreate(mutableItem, statusHolder, propKey.getKey(), propKey.getValue());
            CancelException cancelException = statusHolder.getCancelException();
            if (cancelException != null) {
                LockingHelper.removeLockEntry(mutableItem.getRepoPath());
                AccessLogger.propertyAddedDenied(repoPath, "Property key " + propKey.getKey() + " Canceled");
                throw cancelException;
            }
        });
    }

    void goThroughAfterCreateProperties(MutableVfsItem mutableItem, Properties modifiedProperties,
            BasicStatusHolder statusHolder) {
        modifiedProperties.entries().forEach(propKey ->
                interceptors.afterPropertyCreate(mutableItem, statusHolder, propKey.getKey(), propKey.getValue()));
    }

    private boolean assertCanAnnotate(RepoPath repoPath) {
        if (!authService.canAnnotate(repoPath)) {
            AccessLogger.annotateDenied(repoPath);
            log.error("Cannot set '{}' on '{}': lacking annotate permissions.", "Properties", repoPath.getId());
            return false;
        }
        return true;
    }

    @Override
    public Properties updateProperties(RepoPath repoPath, Properties updateProperties) {
        MutableVfsItem mutableItem;
        try {
            mutableItem = repoService.getMutableItem(repoPath);
            Properties current = mutableItem.getProperties();
            // remove key if null value.
            Set<String> allKeysUpdated = updateProperties.entries().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
            Set<String> keyToRemove = updateProperties.entries().stream().filter(it -> it.getValue() == null).map(Map.Entry::getKey).collect(Collectors.toSet());

            allKeysUpdated.forEach(current::removeAll);
            // update
            current.putAll(updateProperties);
            // remove
            keyToRemove.forEach(current::removeAll);
            mutableItem.setProperties(current);

            return mutableItem.getProperties();
        } catch (ItemNotFoundRuntimeException e) {
            log.error("Cannot add properties for {}: Item not found.", repoPath);
            return null;
        }
    }

    @Override
    public void addProperty(RepoPath repoPath, @Nullable PropertySet propertySet, Property property, String... values) {
        addProperty(repoPath, propertySet, property, false, values);
    }

    @Override
    public void addProperty(RepoPath repoPath, @Nullable PropertySet propertySet, Property property,
                            boolean updateAccessLogger, String... values) {
        if (values == null || values.length == 0) {
            return;
        }

        MutableVfsItem mutableItem;
        try {
            mutableItem = repoService.getMutableItem(repoPath);
        } catch (ItemNotFoundRuntimeException e) {
            log.error("Cannot add properties for {}: Item not found.", repoPath);
            AccessLogger.propertyAddedDenied(repoPath, "Property key " + property + " Item not found");
            return;
        }

        BasicStatusHolder statusHolder = new BasicStatusHolder();
        interceptors.beforePropertyCreate(mutableItem, statusHolder, property.getName(), values);
        CancelException cancelException = statusHolder.getCancelException();
        if (cancelException != null) {
            LockingHelper.removeLockEntry(mutableItem.getRepoPath());
            AccessLogger.propertyAddedDenied(repoPath, "Property key " + property + " Canceled");
            throw cancelException;
        }

        Properties properties = mutableItem.getProperties();

        //Build the xml name of the property
        String xmlPropertyName = getXmlPropertyName(propertySet, property);
        boolean exist = properties.containsKey(xmlPropertyName);
        if (!property.isMultipleChoice()) {
            // If the added property is a single selection, remove any existing values belonging to it, before adding
            // the new one that will replace it
            properties.removeAll(xmlPropertyName);
        }

        properties.putAll(xmlPropertyName, Arrays.asList(values));
        mutableItem.setProperties(properties);
        if (updateAccessLogger) {
            if (exist) {
                AccessLogger.propertyUpdated(repoPath, "Property key " + xmlPropertyName);
            } else {
                AccessLogger.propertyAdded(repoPath, "Property key " + xmlPropertyName);
            }
        }
        interceptors.afterPropertyCreate(mutableItem, statusHolder, property.getName(), values);

    }

    @Override
    public void editProperty(RepoPath repoPath, @Nullable PropertySet propertySet, Property property,
                             boolean updateAccessLogger, String... values) {
        String propertyName = property.getName();
        if (propertySet != null && StringUtils.isNotBlank(propertySet.getName())) {
            propertyName = propertySet.getName() + "." + propertyName;
        }
        // Only update if property actually exists for path.
        if (deleteProperty(repoPath, propertyName, false)) {
            addProperty(repoPath, propertySet, property, false, values);
            if (updateAccessLogger) {
                AccessLogger.propertyUpdated(repoPath, "Property key " + propertyName);
            }
        }
    }

    @Override
    public void addPropertyRecursively(RepoPath repoPath, @Nullable PropertySet propertySet, Property property,
                                       boolean updateAccessLogger, String... values) {
        StoringRepo storingRepo = repoService.storingRepositoryByKey(repoPath.getRepoKey());
        MutableVfsItem fsItem = storingRepo.getMutableFsItem(repoPath);
        if (fsItem == null) {
            log.warn("No item found at {}. Property not added.", repoPath);
            return;
        }
        addPropertyRecursivelyInternal(fsItem, propertySet, property, updateAccessLogger, values);
    }

    @Override
    public void addPropertyRecursively(RepoPath repoPath, @Nullable PropertySet propertySet, Property property,
                                       String... values) {
        addPropertyRecursively(repoPath, propertySet, property, false, values);
    }

    @Override
    public void addPropertyRecursivelyMultiple(RepoPath repoPath, @Nullable PropertySet propertySet,
                                               Map<Property, List<String>> propertyMapFromRequests, boolean updateAccessLogger) {
        ItemInfo itemInfo = repoService.getItemInfo(repoPath);
        // add property in multi transaction
        addPropertyMultiTransaction(itemInfo, propertySet, propertyMapFromRequests, updateAccessLogger);
    }

    @Override
    @Deprecated
    public void addPropertySha256RecursivelyMultiple(RepoPath repoPath) {
        ItemInfo itemInfo = repoService.getItemInfo(repoPath);
        // add property in multi transaction
        addSha256PropertyMultiTransaction(itemInfo);
    }


    /**
     * add property in multi transaction mode
     *
     * @param itemInfo                - repo path
     * @param propertySet             - property set
     * @param propertyMapFromRequests - properties map from request
     */
    private void addPropertyMultiTransaction(ItemInfo itemInfo, @Nullable PropertySet propertySet,
                                             Map<Property, List<String>> propertyMapFromRequests, boolean updateAccessLogger) {
        // add current property
        log.debug("start tx and add property to artifact:{}", itemInfo.getRepoPath());
        transactionalMe().addPropertyInternalMultiple(itemInfo.getRepoPath(), propertySet, propertyMapFromRequests
                , updateAccessLogger);
        if (itemInfo.isFolder()) {
            // add to children recursively
            FileService fileService = ContextHelper.get().beanForType(FileService.class);
            List<ItemInfo> children = fileService.loadChildren(itemInfo.getRepoPath());
            for (ItemInfo child : children) {
                addPropertyMultiTransaction(child, propertySet, propertyMapFromRequests, updateAccessLogger);
            }
        }
    }

    /**
     * add property in multi transaction mode
     *
     * @param itemInfo - repo path
     */
    private void addSha256PropertyMultiTransaction(ItemInfo itemInfo) {
        if (itemInfo.isFolder()) {
            // add to children recursively
            FileService fileService = ContextHelper.get().beanForType(FileService.class);
            List<ItemInfo> children = fileService.loadChildren(itemInfo.getRepoPath());
            for (ItemInfo child : children) {
                addSha256PropertyMultiTransaction(child);
            }
        } else {
            // add current property
            log.debug("start tx and add property to artifact:{}", itemInfo.getRepoPath());
            transactionalMe().addSha256PropertyInternalMultiple((FileInfo) itemInfo);
        }
    }

    /**
     * @deprecated Sha2 now available by default after passing the required conversion
     * remove after migration-enforcing major
     */
    @Override
    @Deprecated
    public void addSha256PropertyInternalMultiple(FileInfo fileInfo) {
        MutableVfsFile mutableVfsItem = (MutableVfsFile) repoService.getMutableItem(fileInfo.getRepoPath());
        //sha2 may have already been calculated by migration
        String sha2 = fileInfo.getSha2();
        if (StringUtils.isBlank(sha2)) {
            sha2 = ChecksumUtils.getChecksum(mutableVfsItem.getStream(), ChecksumType.sha256);
        }
        if (!StringUtils.isEmpty(sha2)) {
            log.debug("adding sha256 property with value {}", sha2);
            Property property = new Property("sha256");
            addProperty(mutableVfsItem.getRepoPath(), null, property, false, sha2);
            BasicStatusHolder statusHolder = new BasicStatusHolder();
            interceptors.afterPropertyDelete(mutableVfsItem, statusHolder, property.getName());
        }
    }

    @Override
    public void addPropertyInternalMultiple(RepoPath repoPath, @Nullable PropertySet propertySet,
                                            Map<Property, List<String>> propertyMapFromRequest, boolean updateAccessLogger) {
        MutableVfsItem mutableVfsItem = repoService.getMutableItem(repoPath);
        // add single property
        for (Map.Entry<Property, List<String>> propertyStringEntry : propertyMapFromRequest.entrySet()) {
            List<String> value = propertyStringEntry.getValue();
            String[] values = new String[value.size()];
            value.toArray(values);
            Property property = propertyStringEntry.getKey();
            addProperty(mutableVfsItem.getRepoPath(), propertySet, property, updateAccessLogger, values);
            BasicStatusHolder statusHolder = new BasicStatusHolder();
            interceptors.afterPropertyDelete(mutableVfsItem, statusHolder, property.getName());
        }
    }

    private void addPropertyRecursivelyInternal(MutableVfsItem fsItem, PropertySet propertySet, Property property,
                                                boolean updateAccessLogger, String[] values) {
        // add property to the current path
        addProperty(fsItem.getRepoPath(), propertySet, property, updateAccessLogger, values);

        if (fsItem.isFolder()) {
            // add to children recursively
            List<MutableVfsItem> children = ((MutableVfsFolder) fsItem).getMutableChildren();
            for (MutableVfsItem child : children) {
                addPropertyRecursivelyInternal(child, propertySet, property, updateAccessLogger, values);
            }
        }
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        interceptors.afterPropertyDelete(fsItem, statusHolder, property.getName());
    }


    private LockableAddProperties transactionalMe() {
        return InternalContextHelper.get().beanForType(LockableAddProperties.class);
    }

    @Override
    public boolean deleteProperty(RepoPath repoPath, String property) {
        return deleteProperty(repoPath, property, false);
    }

    @Override
    public boolean deleteProperty(RepoPath repoPath, String property, boolean updateAccessLogger) {
        MutableVfsItem mutableItem = repoService.getMutableItem(repoPath);
        if (mutableItem == null) {
            log.error("Cannot change properties for {}: Item not found.", repoPath);
            AccessLogger.propertyDeletedDenied(repoPath, "Property key " + property + " Item not found");
            return false;
        }

        BasicStatusHolder statusHolder = new BasicStatusHolder();
        interceptors.beforePropertyDelete(mutableItem, statusHolder, property);
        CancelException cancelException = statusHolder.getCancelException();
        if (cancelException != null) {
            LockingHelper.removeLockEntry(mutableItem.getRepoPath());
            AccessLogger.propertyDeletedDenied(repoPath, "Property key " + property + " Cancelled");
            throw cancelException;
        }

        Properties properties = mutableItem.getProperties();
        boolean exist = properties.containsKey(property);
        properties.removeAll(property);
        mutableItem.setProperties(properties);
        if (updateAccessLogger) {
            if (exist) {
                AccessLogger.propertyDeleted(repoPath, "Property key " + property);
            }
        }
        interceptors.afterPropertyDelete(mutableItem, statusHolder, property);
        return exist;
    }

    @Override
    public void deletePropertyRecursively(RepoPath repoPath, String property, boolean updateAccessLogger) {
        StoringRepo storingRepo = repoService.storingRepositoryByKey(repoPath.getRepoKey());
        MutableVfsItem fsItem = storingRepo.getMutableFsItem(repoPath);
        if (fsItem == null) {
            log.warn("No item found at {}. Property not added.", repoPath);
            return;
        }
        deletePropertyRecursivelyInternal(fsItem, property, updateAccessLogger);
    }

    @Override
    public void deletePropertyRecursively(RepoPath repoPath, String property) {
        deletePropertyRecursively(repoPath, property, false);
    }

    private void deletePropertyRecursivelyInternal(MutableVfsItem fsItem, String property, boolean updateAccessLogger) {
        // add property to the current path
        deleteProperty(fsItem.getRepoPath(), property, updateAccessLogger);

        if (fsItem.isFolder()) {
            // delete from children recursively
            List<MutableVfsItem> children = ((MutableVfsFolder) fsItem).getMutableChildren();
            for (MutableVfsItem child : children) {
                deletePropertyRecursivelyInternal(child, property, updateAccessLogger);
            }
        }
    }

    @Override
    public boolean removeProperties(RepoPath repoPath) {
        return setProperties(repoPath, new PropertiesImpl(), true);
    }

    /**
     * Builds an xml name of the property set and the property in the pattern of PropertSetName.PropertyName
     *
     * @param propertySet Property set to use - can be null
     * @param property    Property to use
     * @return Xml property name
     */
    private String getXmlPropertyName(@Nullable PropertySet propertySet, Property property) {
        String xmlPropertyName = "";
        if (propertySet != null) {
            String setName = propertySet.getName();
            if (StringUtils.isNotBlank(setName)) {
                xmlPropertyName += setName + ".";
            }
        }
        return xmlPropertyName + property.getName();
    }
}
