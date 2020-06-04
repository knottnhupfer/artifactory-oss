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
import DICTIONARY from './../../constants/artifact_general.constant';

class jfRubyGemsController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.artifactoryGridFactory = JFrogGridFactory;
        this.artifactViewsDao = ArtifactViewsDao;
        this.JFrogEventBus = JFrogEventBus;
        this.DICTIONARY = DICTIONARY.rubyGems;
        this.gemsRubyGridOptions = {};
        this.$scope = $scope;
    }

    $onInit() {
        this._initRubyGems();
    }

    getRepoPath() {
        return this.currentNode.data.repoKey + "/" + this.currentNode.data.path;
    }

    _initRubyGems() {
        this._createGrid();
        this._registerEvents();
        this._getRubyGemsData();
    }
    _getColumns() {
        return [
            {
                name: 'Name',
                displayName: 'Name',
                field:'name'
            },
            {
                name: 'Version',
                displayName: 'Version',
                field:'version'
            },
            {
                name: 'Type',
                displayName: 'Type',
                field:'type'
            }
        ]
    }

    _getRubyGemsData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "gems",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    //console.log(data);
                    this.gemsRubyData = data;
                    this.gemsRubyGridOptions.setGridData(this.gemsRubyData.gemsDependencies);
                })
    }

    _createGrid() {
        if (!this.gridFrameworkAssembliesOptions || !Object.keys(this.gridFrameworkAssembliesOptions).length) {
            this.gemsRubyGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                    .setColumns(this._getColumns())
                    .setRowTemplate('default');
        }
        else{
            this._getRubyGemsData();
        }
    }

    _registerEvents() {
        let self = this;
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self._getRubyGemsData();
            }
        });
    }
}
export function jfRubyGems() {
    return {
        restrict: 'EA',
        controller: jfRubyGemsController,
        controllerAs: 'jfRubyGems',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_ruby_gems.html'
    }
}