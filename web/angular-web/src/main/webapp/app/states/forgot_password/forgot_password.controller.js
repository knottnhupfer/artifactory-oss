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
const EMAIL_SENT_MESSAGE = "Reset password email was sent. \nDidn't receive it? Contact your system administrator.";

export class ForgotPasswordController {

    constructor($state, User, JFrogNotifications, JFrogEventBus) {
        this.user = {};
        this.UserService = User;
        this.$state = $state;
        this.artifactoryNotifications = JFrogNotifications;
        this.JFrogEventBus = JFrogEventBus;
        this.forgotPasswordForm = null;
        this.message = '';
        this.EVENTS = JFrogEventBus.getEventsDefinition();
    }

    forgot() {
        let self = this;

        this.JFrogEventBus.dispatch(this.EVENTS.FORM_SUBMITTED);
        if (this.forgotPasswordForm.$valid) {
            this.pending = true;
            this.UserService.forgotPassword(this.user).then(success, error)
        } else {
            form.user.$dirty = true;
        }

        function success(result) {
            self.pending = false;
            self.$state.go('login');
            self.artifactoryNotifications.create({info: EMAIL_SENT_MESSAGE});
        }

        function error(errors) {
            self.pending = false;
            self.$state.go('login');
        }
    }
}