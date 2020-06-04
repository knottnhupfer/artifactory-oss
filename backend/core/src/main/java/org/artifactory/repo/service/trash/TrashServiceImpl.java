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

package org.artifactory.repo.service.trash;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.StatusHolder;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.service.trash.prune.TrashcanPruner;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.binstore.service.BinaryService;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.storage.db.binstore.service.garbage.TrashUtil;
import org.artifactory.storage.db.fs.model.DbFsItemProvider;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.fs.session.StorageSessionHolder;
import org.artifactory.util.RepoPathUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Shay Yaakov
 */
@Reloadable(beanClass = TrashService.class, initAfter = {InternalCentralConfigService.class, BinaryService.class},
        listenOn = CentralConfigKey.none)
public class TrashServiceImpl implements TrashService {
    private static final Logger log = LoggerFactory.getLogger(TrashServiceImpl.class);

    @Autowired
    private InternalRepositoryService repoService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private InternalBinaryService binaryStore;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private org.artifactory.storage.fs.service.PropertiesService storagePropsService;

    @Autowired
    private NonTrashableItems nonTrashableItems;

    @Override
    public void init() {
        binaryStore.addGCListener(new TrashcanPruner());
    }

    @Override
    public void copyToTrash(RepoPath repoPath) {
        if (RepoPathUtils.isTrash(repoPath)) {
            return;
        }

        LocalRepoDescriptor localRepoDescriptor = repoService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        if (localRepoDescriptor == null || localRepoDescriptor.isCache()) {
            return;
        }

        if (!configService.getDescriptor().getTrashcanConfig().isEnabled()) {
            return;
        }

        if (nonTrashableItems.skipTrash(localRepoDescriptor, repoPath.getPath())) {
            return;
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication originalAuthentication = securityContext.getAuthentication();
        try {
            trashAsSystem(repoPath, originalAuthentication.getName());
        } finally {
            securityContext.setAuthentication(originalAuthentication);
        }
    }

    private void trashAsSystem(RepoPath repoPath, String deletedBy) {
        securityService.authenticateAsSystem();
        RepoPath trashPath = RepoPathFactory.create(TRASH_KEY, repoPath.getRepoKey() + "/" + repoPath.getPath());
        if (repoPath.isFile()) {
            undeployFileWithSameNameInTrash(trashPath);
            log.debug("Trashing item '{}'", repoPath);
            Properties properties = buildDeletedItemProperties(repoPath, deletedBy);
            repoService.copy(repoPath, trashPath, false, true, true);
            propertiesService.setProperties(trashPath, properties, true);
        } else {
            log.debug("Overriding folder properties on trashed item '{}'", trashPath);
            if (repoService.exists(trashPath)) {
                propertiesService.setProperties(trashPath, buildDeletedItemProperties(repoPath, deletedBy), true);
            }
        }
    }

    /**
     * TrashPath is the future path of the deleted file. If any of the folders in the path exist in trash as a file
     * it will be deleted from trash and replaced with the folder hierarchy. This in order to preserve the trash can
     * policy to save the last thing deleted.
     */
    private void undeployFileWithSameNameInTrash(RepoPath trashPath) {
        List<RepoPath> ancestors = DbFsItemProvider.getAncestors(trashPath);
        //we want to iterate the path from the beginning
        Collections.reverse(ancestors);
        for (RepoPath ancestor : ancestors) {
            try {
                repoService.getFileInfo(ancestor);
                repoService.undeploy(ancestor);
                StorageSession storageSession = StorageSessionHolder.getSession();
                if (storageSession != null) {
                    storageSession.save();
                }
            } catch (ItemNotFoundRuntimeException e) {
                return;
            } catch (FileExpectedException e) {
                //continue to check next folder in path.
            }
        }
    }

    @Override
    public MoveMultiStatusHolder restoreBulk(RepoPath repoPath, String restoreRepo, String restorePath,
            int transactionSize) {
        final MoveMultiStatusHolder finalStatus = new MoveMultiStatusHolder();
        if (repoService.localRepositoryByKey(restoreRepo) == null) {
            finalStatus.warn("Restore repo '" + restoreRepo + "' doesn't exist", log);
            return finalStatus;
        }
        // First mark properties to delete
        List<String> propertiesToRemove = Lists.newArrayList();
        propertiesToRemove.add(PROP_TRASH_TIME);
        propertiesToRemove.add(PROP_DELETED_BY);
        propertiesToRemove.add(TRASH_KEY);
        propertiesToRemove.add(PROP_ORIGIN_REPO_TYPE);
        propertiesToRemove.add(PROP_ORIGIN_PATH);
        MoveMultiStatusHolder status = repoService.move(repoPath, restoreRepo, restorePath, null, propertiesToRemove,
                false, true, transactionSize);
        finalStatus.merge(status);
        return finalStatus;
    }

    @Override
    public MoveMultiStatusHolder restoreBulk(RepoPath repoPath, String restoreRepo, String restorePath) {
        int transactionSize = ConstantValues.moveCopyDefaultTransactionSize.getInt();
        return restoreBulk(repoPath, restoreRepo, restorePath, transactionSize);

    }


    @Override
    public MoveMultiStatusHolder restore(RepoPath repoPath, String restoreRepo, String restorePath) {
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        if (repoService.localRepositoryByKey(restoreRepo) == null) {
            status.warn("Restore repo '" + restoreRepo + "' doesn't exist", log);
            return status;
        }

        // First delete the unnecessary properties from the items in the trash
        propertiesService.deletePropertyRecursively(repoPath, PROP_TRASH_TIME, false);
        propertiesService.deletePropertyRecursively(repoPath, PROP_DELETED_BY, false);

        RepoPath restoreRepoPath = RepoPathFactory.create(restoreRepo, restorePath);
        final MoveMultiStatusHolder finalStatus = new MoveMultiStatusHolder();
        if (rootRepoRestore(repoPath)) {
            repoService.getChildren(repoPath).stream()
                    .map(ItemInfo::getRepoPath)
                    .forEach(child -> {
                        MoveMultiStatusHolder moveMultiStatusHolder = restorePath(child, restoreRepoPath);
                        finalStatus.merge(moveMultiStatusHolder);
                    });
        } else {
            finalStatus.merge(restorePath(repoPath, restoreRepoPath));
        }

        return finalStatus;
    }

    private MoveMultiStatusHolder restorePath(RepoPath repoPath, RepoPath restoreRepoPath) {
        return repoService.moveMultiTx(repoPath, restoreRepoPath, false, true, true);
    }

    private boolean rootRepoRestore(RepoPath repoPath) {
        RepoPath parent = repoPath.getParent();
        return parent != null && StringUtils.isBlank(parent.getPath());
    }

    @Override
    public StatusHolder empty() {
        return repoService.undeployMultiTransaction(InternalRepoPathFactory.repoRootPath(TRASH_KEY));
    }

    private Properties buildDeletedItemProperties(RepoPath repoPath, String deletedBy) {
        Properties properties = propertiesService.getProperties(repoPath);
        String repoKey = repoPath.getRepoKey();
        properties.replaceValues(PROP_TRASH_TIME, Lists.newArrayList(String.valueOf(System.currentTimeMillis())));
        properties.replaceValues(PROP_DELETED_BY, Lists.newArrayList(deletedBy));
        properties.replaceValues(PROP_ORIGIN_REPO, Lists.newArrayList(repoKey));
        properties.replaceValues(PROP_ORIGIN_REPO_TYPE,
                Lists.newArrayList(repoService.repoDescriptorByKey(repoKey).getType().getType()));
        properties.replaceValues(PROP_ORIGIN_PATH, Lists.newArrayList(repoPath.getPath()));
        return properties;
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {

    }

    @Override
    public List<GCCandidate> getGCCandidatesFromTrash() {
        if (configService.getDescriptor().getTrashcanConfig().isEnabled()) {
            String validFrom = getTrashValidFrom();
            if (StringUtils.isNotBlank(validFrom) && Long.valueOf(validFrom) > 0) {
                return storagePropsService.getGCCandidatesFromTrash(validFrom);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public boolean isTrashcanEnabled() {
        return configService.getDescriptor().getTrashcanConfig().isEnabled();
    }

    @Override
    public void undeployFromTrash(GCCandidate gcCandidate) {
        if (gcCandidate.getRepoPath().isFile() && !gcCandidate.getRepoPath().isRoot()) {
            if (!TrashUtil.isTrashItem(gcCandidate)) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping non-trash item {}", gcCandidate.getRepoPath().toPath());
                }
                return;
            }
            long itemDeleteStartTime = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("Deleting item: {}", gcCandidate);
            }
            repoService.undeploy(gcCandidate.getRepoPath(), false, false);
            if (log.isDebugEnabled()) {
                log.debug("Deleting item: {} took {} millis", gcCandidate.getRepoPath(),
                        System.currentTimeMillis() - itemDeleteStartTime);
            }
        }
    }

    private String getTrashValidFrom() {
        int retentionPeriodDays = configService.getDescriptor().getTrashcanConfig().getRetentionPeriodDays();

        long valueFrom = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionPeriodDays);
        validateRetentionPeriodTimestamp(valueFrom);
        return String.valueOf(valueFrom);
    }

    @Override
    public void validateRetentionPeriodTimestamp(long retentionDaysTimestamp) {
        long minAllowedTimestamp = 1044106642176L; //2003, January 1
        if (retentionDaysTimestamp < minAllowedTimestamp) {
            throw new IllegalArgumentException("Illegal retention date. Retention period goes back too far.");
        }
    }
}
