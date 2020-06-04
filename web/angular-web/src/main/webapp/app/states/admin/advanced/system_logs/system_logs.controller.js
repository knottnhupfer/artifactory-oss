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
import API from '../../../../constants/api.constants';

export class AdminAdvancedSystemLogsController {
    constructor($scope, SystemLogsDao, $interval, $window, $timeout) {

        this.logsDao = SystemLogsDao;
        this.$interval = $interval;
        this.$window = $window;
        this.$timeout = $timeout;

        this.intervalPromise = null;
        this.timeoutSpinner = null;
        this.timeCount = 5;

        this._getInitialData();

        $scope.$on('$destroy', ()=> {
            this.stopTimeout();
            this.stopInterval();
        });
    }

    _getInitialData() {
        this.logsDao.getLogs().$promise.then((data)=> {
            this.refreshRateSecs = data.refreshRateSecs;
            this.logs = _.map(data.logs, (logName)=>{return {logName:logName}});
            this.selectedLog = this.logs[0].logName;
            this.data = {fileSize: 0};
            this._getLogData();
        });
    }

    _getLogData() {
        this.stopInterval();

        this.logsDao.getLogData({id: this.selectedLog, fileSize: this.data.fileSize, $no_spinner: true}).$promise.then((data)=> {
            this.stopTimeout();

            if (this.data.fileSize === 0) {
                this.$timeout(()=> {
                    var textarea = document.getElementById('textarea');
                    textarea.scrollTop = textarea.scrollHeight;
                });
            }

            if (data.fileSize)
                this.data = data;

            this.timeCount = this.refreshRateSecs;
            if (!this.intervalPromise && !this.paused)
                this.startInterval();
        });

        this.timeoutSpinner = this.$timeout(() => {
            this.timeCount--;
        }, 400);
    }

    download() {
        this.$window.open(`${API.API_URL}/systemlogs/downloadFile?id=`+this.selectedLog, '_blank');
    }


    onChangeLog() {
        this.stopInterval();
        this.data = {fileSize: 0};
        this._getLogData();
    }

    startInterval() {
        this.intervalPromise = this.$interval(()=> {
            if (this.timeCount == 0)
                this._getLogData();
            else
                this.timeCount--;
        }, 1000);
    }

    stopInterval() {
        if (this.intervalPromise) {
            this.$interval.cancel(this.intervalPromise);
            this.intervalPromise = null;
        }
    }

    stopTimeout() {
        if (this.timeoutSpinner) {
            this.$timeout.cancel(this.timeoutSpinner);
            this.timeoutSpinner = null;
        }
    }

    togglePause() {
        this.paused = !this.paused;
        if (this.paused) {
            this.stopInterval();
            this.stopTimeout();
        }
        else {
            this.startInterval();
        }
    }
    getPauseLinkText() {
        return this.paused ? 'Resume' : 'Pause';
    }
}
