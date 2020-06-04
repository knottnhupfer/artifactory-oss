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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.BrowsableItemCriteria;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive.ArchiveEntriesTree;
import org.artifactory.ui.utils.BrowseUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Collections;
import java.util.List;

import static org.artifactory.repo.RepoDetailsType.*;


/**
 * Junction node represents an action of which the user wishes to view an item children.
 * The user clicks the little arrow on the tree and the junction node request
 * gets populated with the children names (without their data to save network bandwidth)
 *
 * @author Chen  Keinan
 * @author Omri Ziv
 */
@JsonTypeName("junction")
public class JunctionNode implements RestTreeNode {

    @JsonIgnore
    private final RepositoryBrowsingService repositoryBrowsingService;
    @JsonIgnore
    private final RepositoryService repoService;
    @JsonIgnore
    private final NodeRepoTypeHelper nodeRepoTypeHelper;

    private String repoKey;
    private String path;
    private String repoType;
    private ItemInfo fileInfo = null;
    private RepoType repoPkgType = null;

    public JunctionNode() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        repositoryBrowsingService = ContextHelper.get().beanForType(RepositoryBrowsingService.class);
        repoService = ContextHelper.get().beanForType(RepositoryService.class);
        this.nodeRepoTypeHelper = new NodeRepoTypeHelper(addonsManager, repoService);
    }

    @Override
    public ContinueResult<? extends RestModel> fetchItemTypeData(boolean isCompact) {
        if (isArchiveExpendRequest()) {
            // get all archive children
            return new ArchiveEntriesTree().buildChildren(repoKey, path);
        } else {
            // get repository or folder children 1st depth
            getRepoPkgType();
            List<? extends RestModel> children = getChildren(isCompact);
            return new ContinueResult<>(null, children);
        }
    }

    @Override
    public boolean isArchiveExpendRequest() {
        return isLocalOrCached() && !isFolder() && isArchive();
    }

    private boolean isLocalOrCached() {
        return LOCAL_REPO.equals(repoType) || CACHED_REPO.equals(repoType) || DISTRIBUTION_REPO.equals(repoType) ||
                RELEASE_BUNDLE_REPO.equals(repoType) || SUPPORT_BUNDLES_REPO.equals(repoType);
    }

    private void getRepoPkgType() {
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        RepoDescriptor repoDescriptor = repositoryService.repoDescriptorByKey(repoKey);
        repoPkgType = repoDescriptor.getType();
    }

    private List<? extends RestModel> getChildren(boolean isCompact) {
        List<INode> children;
        switch (repoType) {
            case "remote": {
                RepoPath remoteRepoPath = InternalRepoPathFactory.create(getRepoKey(), getPath(), true);
                BrowsableItemCriteria criteria = getBrowsableItemCriteria(remoteRepoPath);
                List<BaseBrowsableItem> remoteChildren = repositoryBrowsingService.getRemoteRepoBrowsableChildren(criteria);
                //RTFACT-9746
                remoteChildren = BrowseUtils.filterChecksums(remoteChildren);
                children = Lists.newArrayListWithExpectedSize(remoteChildren.size());
                Collections.sort(remoteChildren);
                populateRemoteData(children, remoteChildren, "remote");
                break;
            }
            case "virtual": {
                RepoPath virtualRepoPath = InternalRepoPathFactory.create(getRepoKey(), getPath(), true);
                BrowsableItemCriteria criteria = getBrowsableItemCriteria(virtualRepoPath);
                List<BaseBrowsableItem> virtualChildren = repositoryBrowsingService.getVirtualRepoBrowsableChildren(criteria);
                //RTFACT-9746
                virtualChildren = BrowseUtils.filterChecksums(virtualChildren);
                children = Lists.newArrayListWithExpectedSize(virtualChildren.size());
                Collections.sort(virtualChildren);
                populateRemoteData(children, virtualChildren, "virtual");
                break;
            }
            default: {
                RepoPath repositoryPath = InternalRepoPathFactory.create(getRepoKey(), getPath());
                List<ItemInfo> items = repoService.getChildren(repositoryPath);
                //RTFACT-9746
                items = BrowseUtils.filterItemInfoChecksums(items);
                children = Lists.newArrayListWithExpectedSize(items.size());
                populateChildData(children, repoService, items, isCompact);
                break;
            }
        }
        return children;
}



    /**
     * get remote and virtual browsable item criteria
     *
     * @param repositoryPath - repository path
     * @return browsable item criteria
     */
    private BrowsableItemCriteria getBrowsableItemCriteria(RepoPath repositoryPath) {
        return new BrowsableItemCriteria.Builder(repositoryPath).includeChecksums(false).build();
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * populate File or Folder Data
     * @param children - list of child's
     * @param repoService - repository service
     * @param items - nodes items ( File or Folder)
     */
    private void populateChildData(List<INode> children, RepositoryService repoService, List<ItemInfo> items, boolean isCompact) {
        for (ItemInfo pathItem : items) {
            RepoPath repoPath = pathItem.getRepoPath();
            if (!repoService.isLocalOrCachedRepoPathAcceptedOrCanAnotate(repoPath)) {
                continue;
            }
            children.add(getChildItem(pathItem, pathItem.getRelPath(),repoPath,isCompact));
        }
    }

    /**
     * populate File or Folder Data
     * @param children - list of child's
     * @param items - nodes items ( File or Folder)
     */
    private void populateRemoteData(List<INode> children, List<BaseBrowsableItem> items, String repoType) {
        for (BaseBrowsableItem pathItem : items) {
            children.add(getRemoteChildItem(pathItem, repoType));
        }
    }

    /**
     * Returns a new child  node item
     *
     * @param pathItem       The path to the child content
     * @param relativePath   The relative path to the child itself
     * @return File or folder node
     */
    private INode getChildItem(ItemInfo pathItem, String relativePath, RepoPath repoPath, boolean isCompact) {
        INode child;
        if (pathItem.isFolder()) {
            child = new FolderNode(((FolderInfo) pathItem), pathItem.getName(), repoType);
            if  (isCompact) {
                // compact child folder
                compactFolder(child);
           }
        } else {
            MimeType mimeType = NamingUtils.getMimeType(relativePath);
            child = new FileNode(repoPath, pathItem.getName(), repoType, repoPkgType, mimeType.isArchive());
        }
        return child;
    }

    /**
     * Returns a new child  node item
     *
     * @param pathItem       The path to the child content
     * @return File or folder node
     */
    protected INode getRemoteChildItem(BaseBrowsableItem pathItem, String repoType) {
        INode child;
        if (pathItem.isFolder()) {
            child = createRemoteOrVirtualFolderNode(pathItem, repoType);
        } else {
            MimeType mimeType = NamingUtils.getMimeType(pathItem.getRelativePath());
            child = new VirtualRemoteFileNode(pathItem, pathItem.getName(), repoType, repoPkgType, mimeType.isArchive());
        }
        return child;
    }

    /**
     * create remote or virtual folder node
     *
     * @param pathItem       - path item
     * @param repoType       - repo type
     * @return - tree node
     */
    private INode createRemoteOrVirtualFolderNode(BaseBrowsableItem pathItem, String repoType) {
        INode child;
        String repoKey = pathItem.getRepoKey();
        RepoPath repositoryPath = pathItem.getRepoPath();
        if (repoKey.endsWith("-cache")) {
            repoKey = repoKey.replace("-cache", "");
            repositoryPath = InternalRepoPathFactory.create(repoKey, pathItem.getRepoPath().getPath());
        }
        child = new VirtualRemoteFolderNode(repositoryPath, pathItem, pathItem.getName(), repoType);
        return child;
    }

    /**
     * compact 1sr folder child  by looking for empty child folders
     * @param child - 1st folder child
     */
    private void compactFolder(INode child) {
        FolderNode folder = (FolderNode) child;
        StringBuilder nameBuilder = new StringBuilder(folder.getText());
        FolderNode next = folder.fetchNextChild();
        // look for empty folders
        while (next != null){
            if (shouldNotBeCompacted(folder.getRepoPath())) {
                break;
            }
            folder.updateRepoPath(next.getRepoPath());
            folder.setCompacted(true);
            // update compact name
            nameBuilder.append('/').append(next.getText());
            next = next.fetchNextChild();
        }
        folder.setText(nameBuilder.toString());
    }

    private boolean shouldNotBeCompacted(RepoPath repoPath) {
        return nodeRepoTypeHelper.isConanReferenceFolder(repoPath);
    }

    private boolean isArchive() {
        return NamingUtils.getMimeType(getFileInfo().getRelPath()).isArchive();
    }

    private boolean isFolder() {
        return getFileInfo().isFolder();
    }

    private ItemInfo getFileInfo() {
        if (fileInfo == null) {
            RepoPath repositoryPath = InternalRepoPathFactory.create(getRepoKey(), getPath());
            fileInfo = repoService.getItemInfo(repositoryPath);
        }
        return fileInfo;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
