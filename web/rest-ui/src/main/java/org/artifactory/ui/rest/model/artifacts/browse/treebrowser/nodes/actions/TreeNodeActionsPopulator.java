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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.actions;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.watch.ArtifactWatchAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.utils.RegExUtils;
import org.jfrog.client.util.PathUtils;
import org.jfrog.security.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Builds an actions list to be added to a tree node
 *
 * @author Shay Yaakov
 */
@Component
public class TreeNodeActionsPopulator {

    private RepositoryService repoService;
    private AuthorizationService authService;
    private CentralConfigService centralConfig;
    private BuildService buildService;
    private AddonsManager addonsManager;

    @Autowired
    public TreeNodeActionsPopulator(RepositoryService repoService, AuthorizationService authService,
            CentralConfigService centralConfig, BuildService buildService, AddonsManager addonsManager) {
        this.repoService = repoService;
        this.authService = authService;
        this.centralConfig = centralConfig;
        this.buildService = buildService;
        this.addonsManager = addonsManager;
    }

    public void populateForRepository(TabsAndActions tabsAndActions) {
        List<TabOrAction> actions = Lists.newArrayList();
        RepoPath repoPath = tabsAndActions.getRepoPath();
        String repoType = tabsAndActions.getRepoType();
        if ("remote".equals(repoType) || "virtual".equals(repoType)) {
            actions.add(refresh());
            actions.add(virtualZapCaches(tabsAndActions.getRepoType()));
            actions.add(remoteVirtualReindex(tabsAndActions.getRepoPkgType(), tabsAndActions.getRepoType()));
            actions.add(favorites());
        } else if ("distribution".equals(repoType) || "releaseBundles".equals(repoType)) {
            actions.add(refresh());
            actions.add(watch(repoPath));
            actions.add(deleteContent(repoPath));
            actions.add(favorites());
        } else if ("supportBundles".equals(repoType)) {
            actions.add(refresh());
            actions.add(downloadFolder(repoPath));
            actions.add(deleteContent(repoPath));
        } else if ("trash".equals(repoType)) {
            actions.add(refresh());
            actions.add(searchTrash());
            actions.add(emptyTrash());
        } else {
            actions.add(downloadFolder(repoPath));
            actions.add(refresh());
            actions.add(copyContent(repoPath));
            actions.add(moveContent(repoPath));
            actions.add(watch(repoPath));
            actions.add(zap(repoPath));
            actions.add(reindex(repoPath, tabsAndActions.getRepoPkgType(), tabsAndActions.getRepoType()));
            actions.add(deleteVersions(repoPath));
            actions.add(deleteContent(repoPath));
            actions.add(favorites());
        }
        tabsAndActions.setActions(actions.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public void populateForFolder(TabsAndActions tabsAndActions, boolean edgeLicensed) {
        List<TabOrAction> actions = Lists.newArrayList();
        RepoPath repoPath = tabsAndActions.getRepoPath();
        String repoType = tabsAndActions.getRepoType();
        if ("remote".equals(repoType) || "virtual".equals(repoType)) {
            actions.add(refresh());
        } else if ("distribution".equals(repoType)) {
            actions.add(refresh());
            actions.add(watch(repoPath));
            actions.add(distribute(edgeLicensed));
            actions.add(delete(repoPath));
        } else if ("releaseBundles".equals(repoType)) {
            actions.add(refresh());
            actions.add(watch(repoPath));
            actions.add(downloadFolder(repoPath));
        } else if ("supportBundles".equals(repoType)) {
            actions.add(refresh());
            actions.add(downloadFolder(repoPath));
            actions.add(delete(repoPath));
        } else if ("trash".equals(repoType)) {
            actions.add(refresh());
            actions.add(restore());
            actions.add(deletePermanently());
        } else {
            actions.add(downloadFolder(repoPath));
            actions.add(refresh());
            actions.add(copy(repoPath));
            actions.add(move(repoPath));
            actions.add(watch(repoPath));
            actions.add(distribute(edgeLicensed));
            actions.add(zap(repoPath));
            actions.add(deleteVersions(repoPath));
            actions.add(delete(repoPath));
        }
        tabsAndActions.setActions(actions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public void populateForFile(TabsAndActions tabsAndActions, boolean edgeLicensed) {
        List<TabOrAction> actions = Lists.newArrayList();
        RepoPath repoPath = tabsAndActions.getRepoPath();
        String repoType = tabsAndActions.getRepoType();
        boolean isCached = tabsAndActions.getCached();
        if ("remote".equals(repoType)) {
            actions.add(download());
        } else if ("virtual".equals(repoType)) {
            if (!isCached) {
                actions.add(download());
                tabsAndActions.setActions(actions.stream().filter(Objects::nonNull).collect(Collectors.toList()));
                return;
            }
            repoPath = repoService.getVirtualFileInfo(repoPath).getRepoPath();
            RepoPath finalRepoPath = repoPath;
            if (repoService.getLocalAndCachedRepoDescriptors().stream()
                    .anyMatch(e -> e.getKey().equals(finalRepoPath.getRepoKey()))) {
                actions.add(download());
            }

        } else if ("distribution".equals(repoType)) {
            actions.add(watch(repoPath));
            actions.add(distribute(edgeLicensed));
            actions.add(delete(repoPath));
            actions.add(download());
        } else if ("releaseBundles".equals(repoType)) {
            actions.add(watch(repoPath));
            actions.add(download());
        }else if ("supportBundles".equals(repoType)) {
            actions.add(download());
            actions.add(view(repoPath));
            actions.add(delete(repoPath));
        } else if ("trash".equals(repoType)) {
            actions.add(restore());
            actions.add(deletePermanently());
        } else {
            actions.add(download());
            actions.add(distribute(edgeLicensed));
            actions.add(view(repoPath));
            actions.add(copy(repoPath));
            actions.add(move(repoPath));
            actions.add(watch(repoPath));
            actions.add(delete(repoPath));
        }
        tabsAndActions.setActions(actions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    TabOrAction downloadFolder(RepoPath repoPath) {
        FolderDownloadConfigDescriptor folderDownloadConfig = centralConfig.getDescriptor().getFolderDownloadConfig();
        if (authService.canRead(repoPath) && folderDownloadConfig.isEnabled() &&
                (!authService.isAnonymous() || folderDownloadConfig.isEnabledForAnonymous())) {
            return new TabOrAction("DownloadFolder");
        }
        return null;
    }

    private TabOrAction refresh() {
        return new TabOrAction("Refresh");
    }

    private TabOrAction favorites() {
            return new TabOrAction("Favorites");
    }

    private TabOrAction searchTrash() {
        return new TabOrAction("SearchTrash");
    }

    private TabOrAction emptyTrash() {
        return new TabOrAction("EmptyTrash");
    }

    private TabOrAction copyContent(RepoPath repoPath) {
        if (authService.canRead(repoPath) && authService.canDeployToLocalRepository()) {
            return new TabOrAction("CopyContent");
        }
        return null;
    }

    private TabOrAction moveContent(RepoPath repoPath) {
        if (authService.canDelete(repoPath) && authService.canDeployToLocalRepository()) {
            return new TabOrAction("MoveContent");
        }
        return null;
    }

    private TabOrAction watch(RepoPath repoPath) {
        if (userCanWatch(repoPath)) {
            if (isUserWatchingRepoPath(repoPath)) {
                return new TabOrAction("Unwatch");
            } else {
                return new TabOrAction("Watch");
            }
        }
        return null;
    }

    private TabOrAction zap(RepoPath repoPath) {
        LocalRepoDescriptor localRepoDescriptor = localOrCachedRepoDescriptor(repoPath);
        if (authService.canManage(repoPath) && localRepoDescriptor != null && localRepoDescriptor.isCache()) {
            return new TabOrAction("Zap");
        }
        return null;
    }

    private TabOrAction reindex(RepoPath repoPath, RepoType repoPkgType, String repoType) {
        Matcher matcher = RegExUtils.LOCAL_REPO_REINDEX_PATTERN.matcher(repoPkgType.name());
        boolean foundMatch = matcher.matches();
        if (foundMatch && authService.canManage(repoPath) && "local".equals(repoType)) {
            return new TabOrAction("RecalculateIndex");
        }
        if (RepoType.Debian.equals(repoPkgType) && "cached".equals(repoType) && authService.canManage(repoPath)) {
            return new TabOrAction("CalculateDebianCoordinates");
        }
        return null;
    }

    private TabOrAction deleteVersions(RepoPath repoPath) {
        LocalRepoDescriptor localRepoDescriptor = localOrCachedRepoDescriptor(repoPath);
        if ((authService.canManage(repoPath) || authService.canDelete(repoPath))
                && authService.canRead(repoPath) && localRepoDescriptor != null && localRepoDescriptor.isLocal()) {
            return new TabOrAction("DeleteVersions");
        }
        return null;
    }

    private TabOrAction deleteContent(RepoPath repoPath) {
        if (authService.canDelete(repoPath)) {
            return new TabOrAction("DeleteContent");
        }
        return null;
    }

    private TabOrAction copy(RepoPath repoPath) {
        if (!NamingUtils.isSystem(repoPath.getPath())
                && !buildService.getBuildInfoRepoKey().equals(repoPath.getRepoKey())
                && authService.canRead(repoPath)
                && authService.canDeployToLocalRepository()) {
            return new TabOrAction("Copy");
        }
        return null;
    }

    private TabOrAction move(RepoPath repoPath) {
        if (!NamingUtils.isSystem(repoPath.getPath())
                && !buildService.getBuildInfoRepoKey().equals(repoPath.getRepoKey())
                && authService.canDelete(repoPath)
                && authService.canDeployToLocalRepository()) {
            return new TabOrAction("Move");
        }
        return null;
    }

    private TabOrAction distribute(boolean edgeLicensed) {
        // since RTFACT-13636, always visible in menu, except in Edge nodes
        if (!edgeLicensed) {
            return new TabOrAction("Distribute");
        }
        return null;
    }

    private TabOrAction delete(RepoPath repoPath) {
        if (authService.canDelete(repoPath)) {
            return new TabOrAction("Delete");
        }
        return null;
    }

    private TabOrAction deletePermanently() {
        if (authService.isAdmin()) {
            return new TabOrAction("DeletePermanently");
        }
        return null;
    }

    private TabOrAction download() {
        return new TabOrAction("Download");
    }

    private TabOrAction view(RepoPath repoPath) {
        String path = repoPath.getPath();
        // PACKAGES is CRAN metadata index. it doesn't have an extension, so can't be added to the mimetypes
        if (NamingUtils.isViewable(path) || "class".equals(PathUtils.getExtension(path)) || path.endsWith("/PACKAGES")) {
            return new TabOrAction("View");
        }
        return null;
    }

    private TabOrAction virtualZapCaches(String repoType) {
        if (authService.isAdmin()) {
            if ("virtual".equals(repoType)) {
                return new TabOrAction("ZapCaches");
            }
        }
        return null;
    }

    private TabOrAction remoteVirtualReindex(RepoType repoPkgType, String repoType) {
        if (authService.isAdmin()) {
            if (RegExUtils.REMOTE_REPO_REINDEX_PATTERN.matcher(repoPkgType.name()).matches() && "remote".equals(repoType)) {
                return new TabOrAction("RecalculateIndex");
            } else {
                Matcher matcher = RegExUtils.VIRTUAL_REPO_REINDEX_PATTERN.matcher(repoPkgType.name());
                boolean foundMatch = matcher.matches();
                if (foundMatch && "virtual".equals(repoType)) {
                    return new TabOrAction("RecalculateIndex");
                }
            }
        }
        return null;
    }

    private TabOrAction restore() {
        if (authService.isAdmin()) {
            return new TabOrAction("Restore");
        }
        return null;
    }

    private boolean isUserWatchingRepoPath(RepoPath repoPath) {
        ArtifactWatchAddon watchAddon = addonsManager.addonByType(ArtifactWatchAddon.class);
        return watchAddon.isUserWatchingRepo(repoPath, authService.currentUsername());
    }

    private boolean userCanWatch(RepoPath repoPath) {
        boolean isAddonSupported = addonsManager.isAddonSupported(AddonType.WATCH);
        return authService.canRead(repoPath) && isAddonSupported && !authService.isAnonymous()
                && !authService.isTransientUser() && !isThisBranchHasWatchAlready(repoPath);
    }

    private boolean isThisBranchHasWatchAlready(RepoPath repoPath) {
        ArtifactWatchAddon watchAddon = addonsManager.addonByType(ArtifactWatchAddon.class);
        Pair<RepoPath, WatchersInfo> nearestWatch = watchAddon.getNearestWatchDefinition(repoPath, authService.currentUsername());
        return nearestWatch != null && !(nearestWatch.getFirst().getPath().equals(repoPath.getPath()));
    }

    private LocalRepoDescriptor localOrCachedRepoDescriptor(RepoPath repoPath) {
        return repoService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
    }
}
