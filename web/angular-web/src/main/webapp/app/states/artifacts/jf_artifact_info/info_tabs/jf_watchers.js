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
import EVENTS from '../../../../constants/artifacts_events.constants';

class jfWatchersController {

    constructor($scope, $state, ArtifactWatchesDao, JFrogGridFactory, JFrogEventBus, $q,
                ArtifactoryStorage) {
        this.$scope = $scope;
        this.$state = $state;
        this.watchersGridOption = {};
        this.artifactWatchesDao = ArtifactWatchesDao;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryStorage = ArtifactoryStorage;
        this.$q = $q;
    }

    $onInit() {
        this._createGrid();
        this._getWatchesData();
        this._registerEvents();
    }

    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.currentNode = node;
            this._getWatchesData();
        });
        this.JFrogEventBus.registerOnScope(this.$scope, [EVENTS.ACTION_WATCH, EVENTS.ACTION_UNWATCH], () => {
            this._getWatchesData();
        });
    }

    _deleteWatches(watches) {
        let data = watches.map((watch) => {
            let selectedWachers = {
                name: watch.watcherName,
                repoKey: watch.watchConfigureOn.split(':')[0],
                path: watch.watchConfigureOn.split(':')[1]
            }
            return selectedWachers;
        });
        let json = {watches: data};

        return this.artifactWatchesDao.delete(json).$promise
                .then(() => {
                this.JFrogEventBus.dispatch(EVENTS.ACTION_UNWATCH, this.currentNode);
                    this._getWatchesData();
                });
    }

    _createGrid() {
        let batchActions = [{
            callback: (watches) => this._deleteWatches(watches),
            visibleWhen: () => this.currentNode && this.currentNode.data && this.currentNode.data.getRoot().repoType !== 'virtual',
            name: "Delete",
            icon: 'clear'
        }];

        this.watchersGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getColumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setBatchActions(batchActions)
                .setButtons(this._getActions());

    }

    _getColumns() {
        return [
            {
                name: "Watcher Name",
                displayName: "Watcher Name",
                field: "watcherName",
                width: '20%'
            },
            {
                name: "Watching Since",
                displayName: "Watching Since",
                field: "watchingSince",
                width: '30%'
            },
            {
                name: "Watch Configured On",
                displayName: "Watch Configured On",
                field: 'watchConfigureOn',
                width: '50%'
            }

        ]
    }

    showInTree(row) {
        let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
        let repoKey = row.watchConfigureOn.split(':')[0];
        let path = row.watchConfigureOn.split(':')[1];
        let artifactPath = repoKey + "/" + (path);
        let archivePath = '';
        this.$state.go('artifacts.browsers.path', {
            "tab": "General",
            "browser": browser,
            "artifact": artifactPath
        });
    }

    _getActions() {
        return [{
            icon: 'icon icon-show-in-tree',
            tooltip: 'Show In Tree',
            callback: row => this.showInTree(row)
        },
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (watch) => this._deleteWatches([watch]),
                visibleWhen: () => this.currentNode && this.currentNode.data && this.currentNode.data.getRoot().repoType !== 'virtual',
            }];
    }

    _getWatchesData() {
        let self = this;
        this.artifactWatchesDao.query({
            path: self.currentNode.data.path,
            repoKey: self.currentNode.data.repoKey
        }).$promise.then((watchers) => {
                    this.watchers = watchers;
                    this.watchersGridOption.setGridData(watchers);
                });
    }

}
export function jfWatchers() {
    return {
        restrict: 'EA',
        scope: {
            currentNode: '='
        },
        controller: jfWatchersController,
        controllerAs: 'jfWatchers',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_watchers.html'
    }
}