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
export class AdminAdvancedSystemInfoController {
    constructor(SystemInfoDao) {
       // console.log("log: "+SystemInfoDao);
        this.systemInfoDao = SystemInfoDao.getInstance();
        let self=this;
        this.systemInfo;
        this.systemInfoJoined;
        this.systemInfoDao.get().$promise.then(function(data){
            self.getSystemInfoKeys(data);
        })
    }
    getSystemInfoKeys(data) {
        this.systemInfo = data.systemInfo;
        this.systemInfoJoined = JSON.stringify(data.systemInfo);
        //let headers = Object.keys(systemInfo);
        //let subTitles = [];
    }

    replaceNewLines(text) {
        return text.replace(/\n/g, "<br>");
    }
}