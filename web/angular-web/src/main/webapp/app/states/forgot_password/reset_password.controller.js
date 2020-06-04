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
const PASSWORD_CHANGED_MESSAGE = 'Password changed successfully';

export class ResetPasswordController {

    constructor($stateParams, User, $state, JFrogNotifications, JFrogEventBus, $timeout) {
        this.$stateParams = $stateParams;
        this.userService = User;
        this.$state = $state;
        this.key = $stateParams.key;
        this.artifactoryNotifications = JFrogNotifications;
        this.JFrogEventBus = JFrogEventBus;
        this.ResetPasswordForm = null;
        this.$timeout = $timeout;
        this.user = {};
        this.EVENTS = JFrogEventBus.getEventsDefinition();
    }

    resetPassword() {
        var self = this;

        this.JFrogEventBus.dispatch(this.EVENTS.FORM_SUBMITTED);

        if (this.ResetPasswordForm.$valid) {
            this.userService.validateKey(this.key).then(success, error);
        }

        function success(response) {
            if (response.data.user) {
                self.user.user = response.data.user;
                self.userService.resetPassword(self.key, self.user).then(function (response) {
                    self.artifactoryNotifications.create(response.data);
                    self.$state.go('login');
                });
            }
        }

        function error(errors) {
            if (errors.data.error) {
                self.artifactoryNotifications.create({error: errors.data.error});
            }
        }
    }

    checkMatchingPasswords() {
        this.$timeout(() => {
            if (this.ResetPasswordForm.password.$valid && this.ResetPasswordForm.repeatPassword.$valid) {
            this.JFrogEventBus.dispatch(this.EVENTS.FORM_CLEAR_FIELD_VALIDATION);
            }
        });
    }
}