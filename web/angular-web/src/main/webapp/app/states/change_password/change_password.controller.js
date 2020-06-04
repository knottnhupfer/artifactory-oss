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
export class ChangePasswordController {

    constructor(UserDao, $state, $stateParams) {
        this.$state = $state;
        this.userDao = UserDao.getInstance();
        this.fields = {};
        this.username = $stateParams.username;
    }

    passwordsMatch() {
        return this.fields.newPassword === this.fields.retypeNewPassword;
    }

    change() {
        this.userDao.changePassword({},{
            userName: this.username,
            oldPassword: this.fields.oldPassword,
            newPassword1: this.fields.newPassword,
            newPassword2: this.fields.retypeNewPassword
        }).$promise.then((res)=>{
            if (res.status === 200) {
                this.$state.go('login');
            }

        })
    }
}
