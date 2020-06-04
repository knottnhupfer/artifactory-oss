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
import SpecialValues from './special_values';

class rtfactStorageViewerController {
    constructor($scope,$interval,BinaryProvidersInfoDao) {

        this.$scope = $scope;
        this.$interval = $interval;
        this.binaryProvidersInfoDao = BinaryProvidersInfoDao.getInstance();

    }

    $onInit() {
        if (!this.data) {
            this._getData();
        }
        else {
            this.data = this._transformData(this.data);
        }
    }

    _getData() {
        this.binaryProvidersInfoDao.get().$promise.then((data)=>{
            this.data = this._transformData(data);

            //debug aid
            if (localStorage._debugStorageViewer === 'true') this._installDebugHooks();
        })
    }

    _installDebugHooks() {
        window.setData = ((totals) => {
            this._setTestData(totals);
            this.$scope.$apply();
        }).bind(this);
        window.randomData = (doApply = true) => {
            let totals = [];
            let n = Math.round(Math.random()*15) + 1;
            for (let i = 0; i<n; i++) {
                let val;
                if (Math.random() < .4) val = 'infinite';
                else if (Math.random() < .2) val = 'unsupported';
                else val = Math.round(Math.random()*9999999999999);
                if (Math.random() < .2) val = val + '*';
                totals.push(val);
            }
            this._setTestData(totals);
            if (doApply) this.$scope.$apply();
        }
        window.runRandomTest = () => {
            this.$interval(()=>{
                randomData(false);
            },100)
        }
    }

    //debug aid
    _setTestData(totalSizes) {

//        let template = this.template = this.template ? this.template : this.data.subElements[0].subElements[0];
        let template = {
            "data": {
                "baseDataDir": "/home/danny/workspace-4.2/artifactory/devenv/.artifactory/data",
                "period": "1",
                "usageSpace": "211439489024",
                "freeSpace": "23654612992",
                "totalSpace": "235094102016",
                "fileStoreDir": "shard-fs-1",
                "type": "state-aware",
                "binariesDir": "/home/danny/workspace-4.2/artifactory/devenv/.artifactory/data/shard-fs-1",
                "tempDir": "_pre",
                "usageSpaceInPercent": "89",
                "id": "shard-fs-3",
                "freeSpaceInPercent": "11",
                "essential": "true"
            },
            "caches": [

            ]
        };

        let subs = [];

        for (let i in totalSizes) {
            let ts = totalSizes[i];
            let unsupported = false;
            if (_.isString(ts) && ts.endsWith('*')) {
                ts = ts.substr(0, ts.length - 1);
                unsupported = true;
            }
            let newStorage = angular.copy(template);
            newStorage.data.id = 'test-fs-'+i;
            newStorage.data.totalSpace = ts + '';

            if (unsupported) newStorage.data.usageSpace = SpecialValues.UNSUPPORTED_VALUE;
            else newStorage.data.usageSpace = _.isNumber(ts) ? Math.round(Math.random()*ts) + '' : Math.round(100000000000*Math.random()) + '';

            subs.push(newStorage);
        }

        if (window.storageRef) {
            window.storageRef.data.type = 'sharding';
            window.storageRef.data.id = 'mock-shard';
            window.storageRef.subElements = subs;
//            window.storageRef.data.quotaErrorLimit = 100 * Math.random();
        }
        else {
            window.storageRef = this.data;
            this.data.data.type='sharding';
            this.data.data.id='mock-shard';
            let cacheMock = angular.copy(template);
            cacheMock.data.id = 'cache-mock';
            this.data.caches = [cacheMock];
            this.data.subElements = subs;
        }



/*
        if (this.debugInterval) {
            this.$interval.cancel(this.debugInterval);
            this.debugInterval = null;
        }

        this.debugInterval = this.$interval(()=>{
            subs.forEach((sub)=>{
                if (sub.data.usageSpace !== SpecialValues.UNSUPPORTED_VALUE) {
                    sub.data.usageSpace = Math.round(parseFloat(sub.data.usageSpace) + (10000000000*Math.random()-5000000000)) + '';
                    if (!SpecialValues.isSpecialValue(sub.data.totalSpace) &&  parseFloat(sub.data.usageSpace) > parseFloat(sub.data.totalSpace)) sub.data.usageSpace = sub.data.totalSpace;
                    if (parseFloat(sub.data.usageSpace) < 0) sub.data.usageSpace = '0';
                    if (SpecialValues.isSpecialValue(sub.data.totalSpace)) delete sub.displayWidth;
                }
            })
        },100)
*/

    }

    _transformData(root) {
        let transformed = {};

        let isEmpty = (element) => {
            if (element.data) return false;
            if (element.subBinaryTreeElements && element.subBinaryTreeElements.length) return false;
            if (element.nextBinaryTreeElement && !isEmpty(element.nextBinaryTreeElement)) return false;

            return true;
        };

        let caches = [];
        let current = root;
        while (current) {
            let currentObj = {data: current.data};
            if (current.subBinaryTreeElements && current.subBinaryTreeElements.length) {
                currentObj.subElements = current.subBinaryTreeElements;
                for (let i = 0; i < currentObj.subElements.length; i++) {
                    currentObj.subElements[i] = this._transformData(currentObj.subElements[i])
                }
            }
            if (currentObj.data && current.nextBinaryTreeElement && !isEmpty(current.nextBinaryTreeElement)) {
                caches.push(currentObj);
                current = current.nextBinaryTreeElement;
            }
            else if (!currentObj.data && current.nextBinaryTreeElement && !isEmpty(current.nextBinaryTreeElement)) {
                current = current.nextBinaryTreeElement;
            }
            else {
                transformed = currentObj;
                transformed.caches = caches;
                current = null;
            }
        }
        return transformed;
    }

}

export function rtfactStorageViewer() {
    return {
        restrict: 'E',
        scope: {
            data: '=?'
        },
        controller: rtfactStorageViewerController,
        controllerAs: 'StorageViewer',
        templateUrl: 'directives/rtfact_storage_viewer/rtfact_storage_viewer.html',
        bindToController: true
    };
}
