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

package org.artifactory.repo.service.flexible.tools;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.model.AqlSortTypeEnum;
import org.artifactory.common.StatusEntry;
import org.artifactory.exception.CancelException;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.*;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.service.flexible.MoveCopyItemInfo;
import org.artifactory.repo.service.flexible.context.MoveCopyContext;
import org.artifactory.repo.service.flexible.listeners.MoveCopyListeners;
import org.artifactory.repo.service.flexible.validators.MoveCopyValidator;
import org.artifactory.repo.service.trash.prune.FullPathInfo;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.util.RepoPathUtils;
import org.jfrog.client.util.PathUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.domain.sensitive.AqlApiItem.*;

/**
 * The class is simple state less helper that help to simplify the move/copy code in the move/copy service
 *
 * @author gidis
 */
public class MoveCopyHelper {

    /**
     * returns true if need to stop the move/copy process
     */
    public static boolean isErrorAndFailFast(boolean failFast, MoveMultiStatusHolder finalStatus) {
        return (finalStatus.hasWarnings() || finalStatus.hasErrors()) && failFast;
    }

    /**
     * returns true if need to stop the move/copy process
     */
    public static boolean isError(MoveMultiStatusHolder finalStatus) {
        return (finalStatus.hasWarnings() || finalStatus.hasErrors());
    }

    /**
     * notify all listeners before item move/copy
     */
    public static void notifyAfterMoveCopy(MoveCopyItemInfo element, MoveCopyContext context, MoveMultiStatusHolder status,
                                           List<MoveCopyListeners> moveCopyListeners) {
        for (MoveCopyListeners moveCopyListener : moveCopyListeners) {
            if (moveCopyListener.isInterested(element, context)) {
                moveCopyListener.notifyAfterMoveCopy(element, status, context);
                if (isErrorAndFailFast(context.isFailFast(), status)) {
                    break;
                }
            }
        }
    }

    /**
     * notify all listeners before item move/copy
     */
    public static void notifyBeforeMoveCopy(MoveCopyItemInfo element, MoveCopyContext context, MoveMultiStatusHolder status, List<MoveCopyListeners> moveCopyListeners) {
        for (MoveCopyListeners moveCopyListener : moveCopyListeners) {
            if (moveCopyListener.isInterested(element, context)) {
                moveCopyListener.notifyBeforeMoveCopy(element, status, context);
                if (isErrorAndFailFast(context.isFailFast(), status)) {
                    break;
                }
            }
        }
    }

    /**
     * The method validates single move/copy of file.
     * It returns:
     * True without errors/warning in staus holder to copy item.
     * True with errors/warning to skip move/copy of an item  and failFast=true to  stop all processes if failFast=true
     * False without errors/warning to skip move/copy of an item if failFast=false and stop all processes if failFast=true
     * False without errors to continue the move/copy
     */
    public static boolean validate(MoveCopyItemInfo element, MoveCopyContext context, MoveMultiStatusHolder status,
                                   List<MoveCopyValidator> moveCopyValidators) {
        for (MoveCopyValidator moveCopyValidator : moveCopyValidators) {
            if (moveCopyValidator.isInterested(element, context)) {
                boolean valid = moveCopyValidator.validate(element, status, context);
                if (!valid) {
                    return false;
                }
                if (isErrorAndFailFast(context.isFailFast(), status)) {
                    return true;
                }
            }
        }
        return true;
    }

    public static MoveMultiStatusHolder doExecuteMoveCopyOnBulk(List<MoveCopyItemInfo> items, MoveCopyContext context,
            SingleArtifactMoveCopier copier, List<MoveCopyListeners> moveCopyListeners, List<MoveCopyValidator> validators) {
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        for (MoveCopyItemInfo element : items) {
            // Notify before move/copy
            StatusEntry lastError = status.getLastError();
            boolean valid = validate(element, context, status, validators);
            if (!valid) {
                continue;
            }
            if (shouldStopProcess(status, context, lastError)) {
                return status;
            }
            // Notify before move/copy
            lastError = status.getLastError();
            notifyBeforeMoveCopy(element, context, status, moveCopyListeners);
            if (shouldStopProcess(status, context, lastError)) {
                return status;
            }
            // Do move copy
            copier.executeOperation(element, context, status);
            // Task is not finished still need to delete folders so don't update the status
            if (isErrorAndFailFast(context.isFailFast(), status)) {
                return status;
            }
            // Notify on file successful move (notify on Folder move/copy at the end )
            if (element.getSourceItem().isFile()) {
                notifyAfterMoveCopy(element, context, status, moveCopyListeners);
            }
        }
        return status;
    }

    public static boolean shouldStopProcess(MoveMultiStatusHolder status, MoveCopyContext context, StatusEntry lastError) {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        CancelException cancelException = status.getCancelException(lastError);
        return cancelException != null || ((status.hasWarnings() || status.hasErrors()) && context.isFailFast());
    }

    /**
     * Returns true if the repoPath represent trash repo path
     */
    private static boolean isTrash(RepoPath pathToMove) {
        return RepoPathUtils.isTrash(pathToMove);
    }

    /**
     * The method create AQL query that will return all sub folders and files of single root item sorted by dept
     */
    public static AqlApiItem createQuery(RepoPath repoPath, AqlSortTypeEnum order) {
        RepoPath parent = repoPath.getParent();
        AqlBase.AndClause<AqlApiItem> and = AqlApiItem.and();
        AqlApiItem query = createWithEmptyResults().filter(and);
        and.append(AqlApiItem.repo().equal(repoPath.getRepoKey()));
        if (parent == null) {
            // repoPath is Root
            and.append(AqlApiItem.path().matches("*"));
        } else if (parent.isRoot()) {
            // repoPath parent is Root
            AqlBase.OrClause<AqlApiItem> or = AqlApiItem.or();
            AqlBase.AndClause<AqlApiItem> and2 = AqlApiItem.and();
            and2.append(AqlApiItem.name().equal(repoPath.getName()));
            and2.append(AqlApiItem.path().equal("."));
            or.append(and2);
            or.append(AqlApiItem.path().equal(repoPath.getName()));
            or.append(AqlApiItem.path().matches(repoPath.getName() + "/*"));
            and.append(or);
        } else {
            // All other repoPats which are not root and parent is not root
            AqlBase.OrClause<AqlApiItem> or = AqlApiItem.or();
            AqlBase.AndClause<AqlApiItem> and2 = AqlApiItem.and();
            and2.append(AqlApiItem.name().equal(repoPath.getName()));
            and2.append(AqlApiItem.path().equal(parent.getPath()));
            or.append(and2);
            or.append(AqlApiItem.path().equal(repoPath.getPath()));
            or.append(AqlApiItem.path().matches(repoPath.getPath() + "/*"));
            and.append(or);
        }
        and.append(AqlApiItem.type().equal(AqlItemTypeEnum.any.signature));
        query.include(repo(), path(), name()).addSortElement(depth());
        if (AqlSortTypeEnum.asc == order) {
            query.asc();
        } else {
            query.desc();
        }
        return query;
    }

    /**
     * The method transform list to AQL result into list of SingleItemMoveCopyInfo elements
     */
    public static List<MoveCopyItemInfo> getBulkElementInfos(MoveCopyContext context, List<FullPathInfo> items,
                                                             InternalRepositoryService repositoryService) {
        return items.stream().map(item -> createBulkElementInfo(context, item, repositoryService))
                .collect(Collectors.toList());
    }

    /**
     * Helper method that collects all the information needed to move/copy single item
     * into single object: SingleItemMoveCopyInfo
     * The methods calculates the final target of the move/copy
     */
    private static MoveCopyItemInfo createBulkElementInfo(MoveCopyContext context, FullPathInfo fullPathInfo,
                                                          InternalRepositoryService repositoryService) {
        String path = ".".equals(fullPathInfo.getPath()) ? "" : fullPathInfo.getPath();
        String name = ".".equals(fullPathInfo.getName()) ? "" : fullPathInfo.getName();
        RepoPath sourceRepoPath = RepoPathFactory.create(fullPathInfo.getRepo(), path + "/" + name);

        // Calculate the final target repo path
        String contextTargetPath = context.getTargetPath();
        String baseTargetPath = StringUtils.isBlank(contextTargetPath) ? "/" : contextTargetPath;
        // Slash at the means that the user have explicitly ask to restore into dir
        boolean folder = baseTargetPath.endsWith("/");
        // Clean slashes to avoid duplicate slashes
        baseTargetPath = PathUtils.trimSlashes(baseTargetPath).toString();
        String diffPath;
        if (folder) {
            diffPath = getDiffPath(context.getSourceRepoPath(), sourceRepoPath, true);
        } else {
            diffPath = getDiffPath(context.getSourceRepoPath(), sourceRepoPath, false);
        }
        // Create target path
        String targetPath = baseTargetPath + "/" + diffPath;
        // Make sure to remove duplicate slashes
        targetPath = PathUtils.removeDuplicateSlashes(targetPath);
        String targetRepo = StringUtils.isNotBlank(context.getTargetKey()) ? context.getTargetKey() : sourceRepoPath.getRepoKey();
        RepoPath targetRepoPath = InternalRepoPathFactory.create(targetRepo, targetPath);
        // Create move copy info
        RepoRepoPath<LocalRepo> sourceRrp = repositoryService.getRepoRepoPath(sourceRepoPath);
        VfsItem sourceItem = sourceRrp.getRepo().getImmutableFsItem(sourceRepoPath);
        RepoRepoPath<LocalRepo> targetRrp = repositoryService.getRepoRepoPath(targetRepoPath);
        targetRrp = adjustTargetPathIfTargetExists(sourceItem, targetRrp, context);
        VfsItem targetItem = targetRrp.getRepo().getImmutableFsItem(targetRrp.getRepoPath());
        FileInfo targetOriginalFileInfo = null;
        if (targetItem != null && targetItem.isFile()) {
            VfsFile file = (VfsFile) targetItem;
            targetOriginalFileInfo = file.getInfo();
        }
        long dept = fullPathInfo.getDept();
        return new MoveCopyItemInfo(sourceRepoPath, targetRepoPath, sourceItem, targetItem, sourceRrp, targetRrp, dept, targetOriginalFileInfo);
    }

    /**
     * In case of trash can the method removes the repo key from the path
     */
    private static RepoPath cleanRepoKeyFromPath(RepoPath pathToMove) {
        String ancestor = PathUtils.stripFirstPathElement(pathToMove.getPath());
        String repoKey = PathUtils.getFirstPathElement(pathToMove.getPath());
        return RepoPathFactory.create(repoKey, ancestor);
    }

    /**
     * The method resolves the relative sub path of the source in relation to the root folder of the copy
     */
    private static String getDiffPath(RepoPath rootSource, RepoPath currentSource, boolean skipName) {
        if (rootSource == null) {
            return currentSource.getPath();
        }
        if (isTrash(rootSource)) {
            rootSource = cleanRepoKeyFromPath(rootSource);
        }
        if (isTrash(currentSource)) {
            currentSource = cleanRepoKeyFromPath(currentSource);
        }
        String[] rootSourceElements = PathUtils.getPathElements(rootSource.getPath());
        String[] currentSourceElements = PathUtils.getPathElements(currentSource.getPath());
        int maxIndex = Math.min(rootSourceElements.length, currentSourceElements.length);
        int index = 0;
        String commonPath = "";
        int skip=skipName?1:0;
        while (index < maxIndex-skip && rootSourceElements[index].equals(currentSourceElements[index])) {
            commonPath = StringUtils.isBlank(commonPath) ? rootSourceElements[index] : commonPath + "/" + rootSourceElements[index];
            index++;
        }
        return PathUtils.trimSlashes(currentSource.getPath().replaceFirst(commonPath, "")).toString();
    }

    /**
     * if the target is a directory and it exists we move/copy the source UNDER the target directory instead of
     * replacing it - this is the default unix filesystem behavior.
     */
    private static RepoRepoPath<LocalRepo> adjustTargetPathIfTargetExists(VfsItem sourceItem, RepoRepoPath<LocalRepo> targetRrp, MoveCopyContext context) {
        if (context.isUnixStyleBehavior()) {
            VfsItem targetFsItem = targetRrp.getRepo().getImmutableFsItem(targetRrp.getRepoPath());
            if (targetFsItem != null && targetFsItem.isFolder() && sourceItem.isFile()) {
                String adjustedPath = targetRrp.getRepoPath().getPath() + "/" + sourceItem.getName();
                targetRrp = new RepoRepoPath<>(targetRrp.getRepo(),
                        InternalRepoPathFactory.create(targetRrp.getRepoPath().getRepoKey(), adjustedPath, true));
            }
        }
        return targetRrp;
    }
}

