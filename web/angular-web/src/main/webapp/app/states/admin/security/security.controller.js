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
export class AdminSecurityController {
  constructor ($modal) {
   this.userData=this.getUserGridData();
      this.modal= $modal;
  }

    getUserGridData(){

        return[
            {"User Name": "System Info", "Realm": "admin.advanced.system_info", "Admin":true,"Last Login":"12/12/2015","Extrenal Realm Status":"bla bla"},
            {"User Name": "System Info", "Realm": "admin.advanced.system_info", "Admin":true,"Last Login":"12/12/2015","Extrenal Realm Status":"bla bla"},
            {"User Name": "System Info", "Realm": "admin.advanced.system_info", "Admin":true,"Last Login":"12/12/2015","Extrenal Realm Status":"bla bla"},
            {"User Name": "System Info", "Realm": "admin.advanced.system_info", "Admin":true,"Last Login":"12/12/2015","Extrenal Realm Status":"bla bla"}

        ]
    }
    addUser() {
    this.modal.open({template:"<div>test test</div>"})
        this.userData.push({});

    }
}