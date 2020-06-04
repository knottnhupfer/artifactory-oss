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

package org.artifactory.repo.service.flexible;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.model.AqlSortTypeEnum;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.result.rows.AqlLazyObjectResultStreamer;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.repo.interceptor.StorageInterceptors;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.service.flexible.context.MoveCopyContext;
import org.artifactory.repo.service.flexible.interfaces.FlatMoveCopyService;
import org.artifactory.repo.service.flexible.interfaces.FlatMoveCopyServiceInternal;
import org.artifactory.repo.service.flexible.listeners.MoveCopyListeners;
import org.artifactory.repo.service.flexible.listeners.ReplicationMoveCopyListener;
import org.artifactory.repo.service.flexible.listeners.StorageMoveCopyListener;
import org.artifactory.repo.service.flexible.tools.SingleArtifactMoveCopier;
import org.artifactory.repo.service.flexible.validators.*;
import org.artifactory.repo.service.trash.prune.FullPathInfo;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.storage.fs.MutableVfsFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.List;

import static org.artifactory.repo.service.flexible.tools.MoveCopyHelper.*;

/**
 * @author gidis
 */
@Service
public class FlatMoveCopyServiceImpl implements FlatMoveCopyService, FlatMoveCopyServiceInternal {
    protected static final Logger log = LoggerFactory.getLogger(FlatMoveCopyServiceImpl.class);

    @Autowired
    protected InternalRepositoryService repositoryService;
    @Autowired
    private AqlService aqlService;
    @Autowired
    private AddonsManager addonsManager;
    @Autowired
    private AuthorizationService authService;
    @Autowired
    protected StorageInterceptors storageInterceptors;

    protected List<MoveCopyValidator> moveCopyValidators;
    protected List<MoveCopyListeners> moveCopyListeners;
    protected SingleArtifactMoveCopier copier;

    @PostConstruct
    public void init() {
        // Register validators
        moveCopyValidators = Lists.newArrayList();
        moveCopyListeners = Lists.newArrayList();
        moveCopyValidators.add(new BasicMoveCopyValidator());
        moveCopyValidators.add(new MoveLayoutValidator(addonsManager));
        moveCopyValidators.add(new AuthorizationMoveCopyValidator(authService));
        moveCopyValidators.add(new BasicMoveCopyValidator());
        moveCopyValidators.add(new AuthorizationMoveValidator(authService));
        moveCopyValidators.add(new IncludeExcludeMoveCopyValidator());
        moveCopyValidators.add(new MavenMoveCopyValidator());
        // Register listeners
        moveCopyListeners.add(new StorageMoveCopyListener(storageInterceptors));
        ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
        moveCopyListeners.add(new ReplicationMoveCopyListener(repositoryService, replicationAddon));
        // Init single file copier
        copier = new SingleArtifactMoveCopier(repositoryService);
    }

    /**
     * Copy all files that are under a single path to some target repo/path
     */
    @Override
    public MoveMultiStatusHolder moveCopy(MoveCopyContext context) {
        // Create multi status holder that will aggregate all the move results.
        final MoveMultiStatusHolder finalStatus = new MoveMultiStatusHolder();
        try {
            long start = System.currentTimeMillis();
            // Don't output to the logger if executing in dry run
            finalStatus.setActivateLogging(!context.isDryRun());
            // Create AQL query that return al items to move/copy sorted by dept asc
            AqlApiItem moveCopyQuery = createQuery(context.getSourceRepoPath(), AqlSortTypeEnum.asc);
            // Execute Aql query (stream based) and run the function
            executeMoveCopyTasksInBulks(context, moveCopyQuery, finalStatus);
            // If error occurred the dont delete files and folders
            if (isErrorAndFailFast(context.isFailFast(), finalStatus)) {
                return finalStatus;
            }
            // Create root dir copy info
            RepoRepoPath<LocalRepo> sourceRrp = repositoryService.getRepoRepoPath(context.getSourceRepoPath());
            if (isError(finalStatus)) {
                return finalStatus;
            }
            List<MoveCopyItemInfo> foldersToDelete = context.getFoldersToDelete();
            if (!foldersToDelete.isEmpty()) {
                FlatMoveCopyServiceInternal txMe = ContextHelper.get().beanForType(FlatMoveCopyServiceInternal.class);
                txMe.executeDeleteRootDir(sourceRrp, context, finalStatus);
            }
            long end = System.currentTimeMillis();
            log.trace("Total move execution time is", (end - start));
        } catch (Exception e) {
            String msg = "Failed to execute  " + (context.isCopy() ? "copy" : "move") + "out items list to move";
            log.error(msg, e);
        }
        return finalStatus;
    }

    /**
     * Creates bulk of move/copy tasks (query results) each bulk will be executed in single transaction
     */
    private void executeMoveCopyTasksInBulks(MoveCopyContext context, AqlApiItem query, MoveMultiStatusHolder status) throws Exception {
        List<FullPathInfo> items = Lists.newArrayList();
        FullPathInfo row;
        try (AqlLazyResult<AqlItem> result = aqlService.executeQueryLazy(query)) {
            AqlLazyObjectResultStreamer<FullPathInfo> streamer = new AqlLazyObjectResultStreamer<>(result,
                    FullPathInfo.class);
            while ((row = streamer.getRow()) != null) {
                if (isRoot(row)) {
                    continue;
                }
                items.add(row);
                if (items.size() >= context.getTransactionSize()) {
                    executeMoveCopyOnBulkItems(context, items, status);
                    // Stop move/copy process in case of warning/error and failFast
                    items.clear();
                    if (isErrorAndFailFast(context.isFailFast(), status)) {
                        break;
                    }
                }
            }
            if (items.size() > 0) {
                executeMoveCopyOnBulkItems(context, items, status);
            }
        } catch (SQLException e) {
            status.error("Failed to query items list to move", 501, e, log);
            throw e;
        } catch (Exception e) {
            String msg = "Failed to execute  " + (context.isCopy() ? "copy" : "move") + "out items list to move";
            status.error(msg, 501, e, log);
            throw e;
        }
    }

    /**
     * processing a chunk of elements (items) as sub method of executeMoveCopyTasksInBulks
     */
    private void executeMoveCopyOnBulkItems(MoveCopyContext context, List<FullPathInfo> items, MoveMultiStatusHolder status) {
        FlatMoveCopyServiceInternal txMe = ContextHelper.get().beanForType(FlatMoveCopyServiceInternal.class);
        // Convert db result to BulkElementInfo;
        List<MoveCopyItemInfo> elements = getBulkElementInfos(context, items, repositoryService);
        MoveMultiStatusHolder statusHolder = txMe.executeMoveCopyOnBulk(elements, context);
        status.merge(statusHolder);
    }

    private boolean isRoot(FullPathInfo row) {
        return ".".equals(row.getPath()) && ".".equals(row.getName());
    }

    /**
     * The method execute the root directory and its children (folders and items) in our case it is only folders since
     * the files where already deleted during the move file
     */
    @Override
    public void executeDeleteRootDir(RepoRepoPath<LocalRepo> rrp, MoveCopyContext context, MoveMultiStatusHolder status) {
        MoveMultiStatusHolder statusHolder = new MoveMultiStatusHolder();
        try {
            LocalRepo repo = rrp.getRepo();
            MutableVfsFolder mutableFolder = repo.getMutableFolder(rrp.getRepoPath());
            if (mutableFolder != null) {
                mutableFolder.delete(new DeleteContext(mutableFolder.getRepoPath()).triggeredByMove());
            } else {
                statusHolder.error(String.format("About to delete %s but folder doesn't exist ", rrp.getRepoPath()), log);
            }
            List<MoveCopyItemInfo> foldersToDelete = context.getFoldersToDelete();
            for (int i = foldersToDelete.size() - 1; i >= 0; i--) {
                MoveCopyItemInfo moveCopyItemInfo = foldersToDelete.get(i);
                notifyAfterMoveCopy(moveCopyItemInfo, context, status, moveCopyListeners);
                if (isErrorAndFailFast(context.isFailFast(), status)) {
                    return;
                }
            }
        } catch (Exception e) {
            statusHolder.error("Failed do delete folder %s during move/copy ", e, log);
        }
    }

    /**
     * The method execute bulk move/copy inside single transaction
     */
    @Override
    public MoveMultiStatusHolder executeMoveCopyOnBulk(List<MoveCopyItemInfo> items, MoveCopyContext context) {
        return doExecuteMoveCopyOnBulk(items, context, copier, moveCopyListeners, moveCopyValidators);
    }
}


