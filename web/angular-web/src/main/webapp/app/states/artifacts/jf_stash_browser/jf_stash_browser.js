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
import JFCommonBrowser from "../jf_common_browser/jf_common_browser";
import types from '../constants/artifact_browser_icons.constant';

export function jfStashBrowser() {
    return {
        scope: {
            startCompact: '='
        },
        restrict: 'E',
        controller: JFStashBrowserController,
        controllerAs: 'jfStashBrowser',
        templateUrl: 'states/artifacts/jf_stash_browser/jf_stash_browser.html',
        bindToController: true
    }
}

class JFStashBrowserController extends JFCommonBrowser {
    constructor(JFTreeApi, $timeout, $injector, JFrogEventBus, $element, $scope, $state, $stateParams,
                $q, ArtifactoryState, ArtifactActions, StashResultsDao, User, JFrogUIUtils) {

        super(JFTreeApi, $scope, ArtifactActions, ArtifactoryState, JFrogUIUtils);

        this.rootID = '____root_node____';

        this.type="stash";
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.$q = $q;
        this.user = User;
        this.TreeNode = $injector.get('TreeNode');
        this.JFrogEventBus = JFrogEventBus;
        this.EVENTS = JFrogEventBus.getEventsDefinition();
        this.JFrogUIUtils  = JFrogUIUtils;
        this.stashResultsDao = StashResultsDao;
        this.types = types;


        this.whenTreeDataLoaded = $q.defer();

        this.$element = $element;

        this.filteredActions = ['Copy', 'Move', 'Watch', 'Unwatch', 'UploadToBintray', 'Distribute', 'Refresh', 'DeleteVersions', 'DownloadFolder', 'Zap', 'ZapCaches', 'IgnoreAlert', 'UnignoreAlert'];

        this.discardedCount = 0;


    }

    $onInit() {
        this.compactMode = this.startCompact || false;
        this._registerEvents();
        this._setUpTree();
    }

    _setUpTree() {

        let driver = {
            uniqueId: node => node.id,
            nodeById: this.nodeByPathGetter.bind(this),
            text: node => node.text,
            childrenChecker: this.childrenChecker.bind(this),
            children: this.childrenGetter.bind(this),
            parent: this.parentGetter.bind(this),
            contextMenuItems: this._getContextMenuItems.bind(this)
        }

        this.treeApi
                .setNodeTemplate(node => this.getNodeTemplate(node))
                .setDataDriver(driver)
                .on('item.selected', item => {
                    item.data.load().then(() => {
                        this.JFrogEventBus.dispatch(this.EVENTS.TREE_NODE_SELECT, item)
                    });
                })
                .on('keydown', e => {
                    this.JFrogEventBus.dispatch(this.EVENTS.ACTIVATE_TREE_SEARCH, e.key);
                })
                .on('ready', this.onReady.bind(this))


        this.treeApi.createViewPane()
                .setItemHeight('30px')
                .setItemsPerPage('auto');


        this.treeApi.freeze();
    }

    getNodeTemplate(node) {
        return `<div>
                    <i class="tree-node-icon ${this.types[node.type] ? this.types[node.type].icon : this.types['default'].icon}"></i>
                    <span class="node-text" style="margin-left: 5px">${node.text}</span>
                </div>`
    }


    onReady() {
        let root = _.find(this.stashData, {id: this.rootID});
        let currentPath = this.$stateParams.artifact ? this.$stateParams.artifact.substring(this.$stateParams.artifact.indexOf('/')+1).split(' ').join('') : null;
        let nodeToOpen = this.nodeByPathGetter(currentPath);

        if (!nodeToOpen) {
            this.treeApi.openNode(root);
            this.treeApi.unFreeze();
            this.treeApi.selectFirst();
        }
        else {
            this.treeApi.openDeepNode(nodeToOpen).then(() => this.treeApi.unFreeze())
        }
        this.treeApi.focus();

    }


    fetchStashData() {
        let defer = this.$q.defer();
        this.stashResultsDao.get({name: 'stash'}).$promise.then((data) => {
            this.stashData = this._transformStashDataToTree(data);
            if (this.compactMode) {
                this.stashData = this._compactTree(this.stashData);
            }
            this.whenTreeDataLoaded.resolve();
            defer.resolve()
        });

        return defer.promise;

    }


    nodeByPathGetter(path) {
        let node = _.find(this.stashData, {id: path});
        return node;
    }

    childrenChecker(parent) {
        return !!_.filter(this.stashData, {parent: parent.id}).length;
    }

    childrenGetter(parent) {
        let defer = this.$q.defer();
        let childrenGetterInner = () => {
            if (!parent) {
                defer.resolve(_.filter(this.stashData, {id: this.rootID}));
            }
            else {
                let children = _.filter(this.stashData, {parent: parent.id})
                defer.resolve(children);
            }
        }
        if (!this.stashData) {
            this.fetchStashData().then(childrenGetterInner);
        }
        else childrenGetterInner();

        return defer.promise;
    }

    parentGetter(node) {
        let parentId = node.parent;
        let parent = this.nodeByPathGetter(parentId);
        return parent;
    }

    _transformStashDataToTree(stashData) {

        let treeData = [];

        let pushToTree = (treeItemData) => {
            if (!_.findWhere(treeData,{id:treeItemData.id})) {
                treeData.push(treeItemData);
            }
        };

        this.rootNode = this._createRootNode(stashData);
        pushToTree(this.rootNode);

        stashData.forEach((result,index)=>{
            result.path = result.relativePath;
            result.text = result.name;
            result.type = 'file';

            let dirArray = (result.relativePath).split('/');
            dirArray.pop();

            let resultNode = {
                id: result.relativePath.split(' ').join(''),
                text: this.JFrogUIUtils.getSafeHtml(result.name),
                parent: dirArray.join('/').split(' ').join('') || this.rootID,
                type: result.mimeType,
                data: this._filterActions(new this.TreeNode(result))
            };

            // replace the node icon type to the package type if necessary
            let type = (typeof result.fileType != 'undefined' ? result.fileType : result.type);
            if(this._iconsShouldBeReplacedWithRepoPkgTypeIcon(type,result.repoPkgType,result.fullpath)){
                resultNode.type = resultNode.data.iconType = result.repoPkgType.toLocaleLowerCase();
            }

            pushToTree(resultNode);

            for (let i = dirArray.length-1; i>=0; i--) {
                let up = _.clone(dirArray);
                up.pop();

                let folderNode = {
                    id: dirArray.join('/').split(' ').join(''),
                    text: dirArray[i],
                    parent: up.join('/').split(' ').join('') || this.rootID,
                    type: 'folder',
                    data: this._filterActions(new this.TreeNode({
                        repoKey: result.repoKey,
                        path: dirArray.join('/'),
                        text: dirArray[i],
                        type: 'folder'
                    }))
                };

                pushToTree(folderNode);
                dirArray.pop();
            }

        });

        return treeData;
    }

    _createRootNode(stashData) {
        let THIS = this;
        let node;
        node = {
            id: this.rootID,
            parent: '#',
            text: 'Stashed Search Results',
            type: 'stash',
            data: {
                text: 'Stashed Search Results',
                iconType: 'stash',
                load: function() {

                    THIS.$stateParams.artifact = '';
                    THIS.$stateParams.tab = 'StashInfo';


                    this.tabs = [{name: "StashInfo"}];
                    this.actions = stashData.length ? [
                        {title: "Copy Stash to Repository", name: "CopyStash", icon: "icon-copy"},
                        {title: "Move Stash to Repository", name: "MoveStash", icon: "icon-move"},
                        {title: "Discard Search Results", name: "DiscardStash", icon: "icon-delete-content"}
                    ] : [];
                    if (!THIS.user.currentUser.getCanDeploy()) {
                        this.actions.shift();
                        this.actions.shift();
                    }
                    this.info = {
                        artifactCount: Math.max(stashData.length - THIS.discardedCount, 0)
                    };
                    return THIS.$q.when(this);
                },
                getDownloadPath: () => {return this.$q.when(this);},
                refreshWatchActions: () => {return this.$q.when(this);},
                isRepo: () => {return false;}
            }

        }
        return node;
    }

    _filterActions(treeNode) {
        let origLoad = treeNode.load.bind(treeNode);
        treeNode.load = () => {
            return origLoad().then(()=> {

                treeNode.actions = _.filter(treeNode.actions, (action)=> {
                    return this.filteredActions.indexOf(action.name) === -1;
                });

                let deleteAction = _.findWhere(treeNode.actions, {name: "Delete"});
                if (deleteAction) treeNode.actions.splice(treeNode.actions.indexOf(deleteAction), 1);


                if (!_.findWhere(treeNode.actions, {name: "ShowInTree"})) {
                    treeNode.actions.push({
                        title: "Show In Tree",
                        name: "ShowInTree",
                        icon: "icon-show-in-tree"
                    });
                }

                if (!_.findWhere(treeNode.actions, {name: "DiscardFromStash"})) {
                    treeNode.actions.push({
                        title: "Discard from Stash",
                        name: "DiscardFromStash",
                        icon: "icon-delete-content"
                    });
                }

                if (deleteAction) treeNode.actions.push(deleteAction);

            });
        };

        return treeNode;
    }

    _compactTree(stashData) {

        let compactedData = [].concat(stashData);

        let getChildren = (parent) => {
            return _.filter(compactedData, {parent: parent.id});
        }

        let nodeById = (id) => {
            return _.find(compactedData, {id});
        }

        let remove = (node) => {
            _.remove(compactedData, n => n === node);
        }

        let recursiveCompact;
        recursiveCompact = (node) => {
            if (node.type !== 'folder') {
                node.data = nodeById(node.id).data;
                return;
            }

            let children = getChildren(node);
            if (children.length === 1 && children[0].type === 'folder') {
                node.text += '/' + children[0].text;
                node.data = nodeById(children[0].id).data;
                node.id = children[0].id;
                remove(children[0]);
                recursiveCompact(node);
            }
            else if (children.length > 1) {
                children.forEach((child) => {
                    recursiveCompact(child)
                });
            }
        };

        getChildren(nodeById(this.rootID)).forEach(node => recursiveCompact(node));

        return compactedData;
    }

    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_SEARCH_CHANGE, text => this._searchTree(text)
        )
        ;
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_SEARCH_CANCEL, text => this._clear_search()
        )
        ;
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_SEARCH_KEYDOWN, e => {
            this.treeApi.handleKeyEvent(e);
            this._searchTreeKeyDown(e.keyCode)
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_DELETE, (node) => {
            node.alreadyDeleted = true;
            this.artifactActions.perform({name: 'DiscardFromStash'}, node);
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_MOVE_STASH, (options) => {
            this.exitStashState(options);
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_COPY_STASH, (options) => {
            this.exitStashState(options);
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_DISCARD_STASH, () => {
            delete this.stashData;
            this.fetchStashData().then(() => {
                this.treeApi.refreshTree().then(() => {
                    let selected = this.treeApi.getSelectedNode();
                    selected.data.load().then(() => {
                        this.JFrogEventBus.dispatch(this.EVENTS.TREE_NODE_SELECT, selected)
                    });
                })
            })
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_DISCARD_FROM_STASH, (node) => {
            this._discardFromStash(node);
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_REFRESH_STASH, () => {
            delete this.stashData;
            this.fetchStashData().then(() => {
                this.treeApi.refreshTree().then(() => {
                    let root = _.find(this.stashData, {id: this.rootID});
                    this.treeApi.openNode(root);
                })
            })
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_EXIT_STASH, (node) => {
            if (node) this.treeApi.selectNode(this.nodeByPathGetter(node.id));
            this.$timeout(()=>this.exitStashState());
        });

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_NODE_OPEN, path => {
            this._openTreeNode(path)
        });

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_COMPACT, (compact) => this._toggleCompactFolders(compact)
        )
        ;

        // URL changed (like back button / forward button / someone input a URL)
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TABS_URL_CHANGED, (stateParams) => {
            let path = stateParams.artifact ? stateParams.artifact.substring(stateParams.artifact.indexOf('/')+1) : this.rootID;
            if (stateParams.tab === 'StashInfo' && this.$state.params.tab !== 'StashInfo') path = this.rootID;
            this.whenTreeDataLoaded.promise.then(() => {

                let selectedNode = this.treeApi.getSelectedNode();
                if (selectedNode && selectedNode.fullpath === stateParams.artifact) return;

                let pathNode = this.nodeByPathGetter(path);
                if (pathNode) this.treeApi.openDeepNode(pathNode);
            })
        });
    }

    _countArtifacts(node) {
        let defer = this.$q.defer();
        if (node.type !== 'folder') {
            defer.resolve(1);
        }
        else {
            let artifacts = 0;
            this.childrenGetter(node).then(children => {
                let pendings = children.length;
                children.forEach(child => {
                    this._countArtifacts(child).then(childCount => {
                        artifacts += childCount;
                        pendings--;
                        if (!pendings) {
                            defer.resolve(artifacts);
                        }
                    })
                })
            })
        }
        return defer.promise;
    }

    _discardFromStash(node) {
        this._countArtifacts(node).then(artifactsDiscarded => {
            this.discardedCount += artifactsDiscarded;
            let selected = this.treeApi.getSelectedNode();
            selected.data.load().then(() => {
                this.JFrogEventBus.dispatch(this.EVENTS.TREE_NODE_SELECT, selected)
            });
        })


        let deletePoint = node;
        let parent = this.parentGetter(node);
        while (parent.$childrenCache.length === 1 && parent.id !== this.rootID) {
            deletePoint = parent;
            parent = this.parentGetter(parent);
        }

        this.treeApi.selectNode(parent);
        this.treeApi.deleteNode(deletePoint);

    }


    exitStashState(options) {
        this.$scope.$destroy();
        let artifact = options && options.target ? options.target.targetRepoKey || '/' : this.$stateParams.artifact || '';
        this.$state.go('artifacts.browsers.path', {tab: 'General', artifact: artifact, browser: 'tree'});
        this.$timeout(()=> {
            this.JFrogEventBus.dispatch(this.EVENTS.TREE_REFRESH);
            if (options) {
                this.JFrogEventBus.dispatch(this.EVENTS.ACTION_COPY, {node: options.node, target: options.target});
            }
        })
    }

}