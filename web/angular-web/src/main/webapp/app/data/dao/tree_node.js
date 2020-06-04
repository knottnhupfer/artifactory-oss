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
const ARCHIVE_MARKER = '!';

export function TreeNodeFactory($q, $injector, RESOURCE, ArtifactoryHttpClient, ArtifactWatchesDao, ArtifactActionsDao,
        NativeBrowser, ArtifactXrayDao,ArtifactoryFeatures) {
    return function (data) {
        return new TreeNode(data, $q, $injector, RESOURCE, ArtifactoryHttpClient, ArtifactWatchesDao,
                ArtifactActionsDao, NativeBrowser, ArtifactXrayDao,ArtifactoryFeatures)
    }
}

class TreeNode {
    constructor(data, $q, $injector, RESOURCE, ArtifactoryHttpClient, ArtifactWatchesDao, ArtifactActionsDao,
            NativeBrowser, ArtifactXrayDao, ArtifactoryFeatures) {
        this.$q = $q;
        this.treeBrowserDao = $injector.get('TreeBrowserDao');
        this.RESOURCE = RESOURCE;
        this.artifactoryHttpClient = ArtifactoryHttpClient;
        this.artifactWatchesDao = ArtifactWatchesDao;
        this.artifactActionsDao = ArtifactActionsDao;
        this.nativeBrowser = NativeBrowser;
        this.ArtifactXrayDao = ArtifactXrayDao;
        this.features = ArtifactoryFeatures

        // Wrap the data
        angular.extend(this, data);

        if (this.children) {
            this.hasChild = true;
        }
        this._initIconType();
        this._initFullpath();

        this.className = 'TreeNode';

        this.updateFavoriteState();
    }

    updateFavoriteState() {
        let isFav = false;
        if (!this.isRepo()) {
            isFav = false;
        } else if (localStorage.favoritesRepos) {
            let favRepos = JSON.parse(localStorage.favoritesRepos);
            if (_.contains(favRepos.favoritesRepos, this.repoKey)) {
                isFav = true;
            }
        }
        this.isFav = isFav;
    }

    _initIconType() {
        if (!this.icon && this.compacted && this.isLocalFolder()) {
            this.iconType = 'compactedFolder';
        } else if (this.isTrashcan()) {
            this.iconType = 'trashcan';
        } else if (this.isRepoInTrashcan()) {
            this.iconType = 'localRepository';
        } else if (this.isBuildsInfoRepo()) {
            this.iconType = 'buildInfoRepository';
        } else if (this.isSupportBundleRepo()) {
            this.iconType = 'supportBundleRepository';
        } else if (this.isRepo()) {
            switch (this.repoType) {
                case 'virtual':
                    this.iconType = 'virtualRepository';
                    break;
                case 'distribution':
                    this.iconType = 'distributionRepository';
                    break;
                case 'releaseBundles':
                    this.iconType = 'distributionRepository';
                    break;
                case 'remote':
                    this.iconType = 'remoteRepository';
                    break;
                case 'cached':
                    this.iconType = 'cachedRepository';
                    break;
                default:
                    this.iconType = 'localRepository';
            }
        } else {
            this.iconType = this.mimeType || this.icon || this.type;
        }

    }

    _initFullpath() {
        if (this.path == '') {
            this.fullpath = this.repoKey;
        } else {
            let path = this.path;
            if (this.archivePath) {
                path = path.replace(this.archivePath, this.archivePath + ARCHIVE_MARKER);
            }
            this.fullpath = this.repoKey + "/" + path;
        }
    }

    getRoot() {
        return this.parent ? this.parent.getRoot() : this;
    }

    findNodeByPath(path, startIndex, includeArchives = true) {
        if (startIndex === path.length) {
            return this;
        }
        if (this.isArchive() && !includeArchives) {
            return this;
        }
        // Find child:
        return this.getChildren(true, null, path[startIndex]).then((children) => {
            children = children.data || children;
            while (startIndex != path.length) {
                startIndex++;
                let partialPath = path.slice(0, startIndex).join('/');
                // TODO: remove second condition after Chen fixes server. Currently sometimes server returns path that ends with '/'
                let child = _.findWhere(children, {path: partialPath}) || _.findWhere(children,
                        {path: partialPath + '/'});
                if (child) {
                    return child;
                }
            }
        })
                .then((child) => {
                    // Recursively look for rest of path:
                    if (child) {
                        return child.findNodeByPath(path, startIndex, includeArchives);
                    } else {
                        return this;
                    }
                }).catch(() => this);
    }

    isLocal() {
        return this.local;
    }

    isFile() {
        return this.type == 'file' || this.type == 'virtualRemoteFile';
    }

    isLocalFolder() {
        return this.type == 'folder';
    }

    isFolder() {
        return this.isLocalFolder() || this.type === 'virtualRemoteFolder';
    }

    isRepo() {
        return this.type === 'repository' || this.type === 'virtualRemoteRepository';
    }

    isFavorite() {
        return this.isFav;
    }

    isBuildsInfoRepo() {
        return this.isRepo() && this.repoPkgType === 'BuildInfo';
    }

    isSupportBundleRepo() {
        return this.isRepo() && this.repoPkgType === 'Support';
    }

    isInSupportBundle() {
        return !this.isRepo() && this.repoType === 'supportBundles';
    }

    isTrashcan() {
        return this.isRepo() && this.repoType === 'trash';
    }

    isInTrashcan() {
        return !this.isRepo() && this.repoType === 'trash';
    }

    isRepoInTrashcan() {
        return this.isInTrashcan() && !_.contains(this.path, '/');
    }

    isArchive() {
        return (typeof this.fileType !== 'undefined' && this.fileType === 'archive');
    }

    isInsideArchive() {
        return this.archivePath;
    }

    // If tabs or actions don't exist already - fetch them from the server
    load(_self = this) {
        let newPath = (this.type === "folder" && this.path.substr(-1) != "/") ? this.path + "/" : this.path;
        if (this.tabs || this.actions) {
            return this.$q.when(this);
        } else {
            let data = {
                type: this.type,
                repoPath: this.repoKey + "/" + newPath,
                repoType: this.repoType || 'local',
                repoPkgType: this.repoPkgType,
                cached: this.cached
            };
            return this.artifactoryHttpClient.post(this.RESOURCE.TREE_BROWSER + "/tabsAndActions", data)
                    .then((response) => {
                        this.tabs = response.data.tabs;
                        this.actions = response.data.actions;

                        let xrayTab = _.find(this.tabs, (tab) => {
                            return tab.name === 'Xray';
                        });

                        if (xrayTab) {

                            this.ArtifactXrayDao.isArtifactBlockedAsync(
                                    {repoKey: this.repoKey, path: response.data.repoPath.path})
                                    .$promise.then((response) => {
                            }, (err) => {
                                xrayTab.class = "marked";
                            })
                        }
                        if (this.features.isJCRDemo() && this.path.indexOf("manifest.json") > -1) {
                            this.tabs.push({name:"Xray",feature: "xray"})
                        }

                        let removeIndex = this.actions.indexOf(_.findWhere(this.actions, {name: "NativeBrowser"}));
                        if (removeIndex !== -1) {
                            this.actions.splice(removeIndex, 1);
                        }

                        let index = this.actions.indexOf(_.findWhere(this.actions, {name: "Watch"}));
                        if (index === -1) {
                            index = this.actions.indexOf(_.findWhere(this.actions, {name: "Unwatch"}));
                        }
                        if (index === -1) {
                            index = this.actions.indexOf(_.findWhere(this.actions, {name: "Move"}));
                        }
                        if (index === -1) {
                            index = this.actions.indexOf(_.findWhere(this.actions, {name: "Refresh"}));
                        }
                        if (this.nativeBrowser.isAllowed(this)) {
                            this.actions.splice(index + 1, 0, {
                                icon: "icon-simple-browser",
                                name: "NativeBrowser",
                                title: "Native Browser"
                            });
                        }

                        let distAction = _.findWhere(this.actions, {name: 'Distribute'});
                        if (distAction && this.repoType === 'distribution') {
                            distAction.name = 'Redistribute';
                        }


                        return this;
                    });
        }

    }

    getDownloadPath() {
        if (!this.downloadPathPromise) {
            let data = {
                repoKey: this.repoKey,
                path: encodeURIComponent(this.downloadPath || this.path).replace(/%2F/g, '/')
            };
            this.downloadPathPromise = this.artifactActionsDao.perform({action: 'download'}, data).$promise
                    .then((response) => {
                        this.actualDownloadPath = response.data.path;
                        if (response.data.xrayValidation) {
                            this.xrayShouldValidate = response.data.xrayValidation;
                        }
                    });
        }
        return this.downloadPathPromise;
    }

    refreshWatchActions() {
        // Can't watch archive / remote files
        if (!this.isInsideArchive() && this.isLocal() && !this.isTrashcan() && !this.isInTrashcan() &&
                !this.isSupportBundleRepo() && !this.isInSupportBundle()) {
            return this.artifactWatchesDao.status({repoKey: this.repoKey, path: this.path})
                    .$promise.then((action) => {
                        //                    console.log(action);
                        let previousAction = this._getWatchAction();
                        if (action && action.name) {
                            // Replace the previous action with the new one
                            if (previousAction) {
                                previousAction.name = action.name;
                            }// Or add the new one if didn't exist before
                            else {
                                this.actions.push(action);
                            }
                        } else {
                            // Remove the previous action if there is no new action
                            if (previousAction) {
                                _.remove(this.actions, previousAction);
                            }
                        }
                        return this;
                    });
        } else {
            return this.$q.when(this);
        }
    }

    _getWatchAction() {
        return _.find(this.actions, (action) => {
            return _.contains(['Watch', 'Unwatch'], action.name);
        });
    }


    invalidateChildren() {
        this.children = null;
    }

    invalidateParent() {
        if (this.parent) {
            this.parent.invalidateChildren();
        } else {
            this.treeBrowserDao.invalidateRoots();
        }
    }

    // Get the children of this node and cache the result
    // or return the cached promise
    getChildren(force = false, continueState, mustInclude) {
        // Server errors if requesting children of file
        // (simple browser always requests getChildren of current node)
        if (this.isFile() && !this.isArchive()) {
            return this.$q.when(null);
        }

        if (!this.children || (force && !this.isInsideArchive()) || this.isTrashcan()) {
            // Load children from server, and cache them
            this.children = this.treeBrowserDao._loadChildren({
                type: "junction",
                repoType: this.repoType,
                repoKey: this.repoKey,
                path: this.path,
                text: this.text,
                trashcan: this.isTrashcan()
            }, this, continueState, {}, null, mustInclude);
        }
        return this.children;
    }
}
