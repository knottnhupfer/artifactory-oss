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
/**
 * returns a function that accept some custom info
 * and returns the interceptor object.
 * intent to be injected and use in DAO's
 *
 * @returns {Function}
 */
export function artifactoryNotificationsInterceptor($q, JFrogNotifications) {

    /**
     * accept an additional info that can be used
     * in the returned interceptor object
     *
     * @returns {{response: Function, responseError: Function}}
     */
    return {
        response: function (res) {
            if (res.data && !(res.config && res.config.params && res.config.params.$suppress_toaster)) {
                if (!res.data.url) {
                    if (res.data.info || res.data.warn) {
                        if ((res.data.info && res.data.info.indexOf('<a') !== -1 && res.data.info.indexOf('</a>') !== -1) ||
                            (res.data.warn && res.data.warn.indexOf('<a') !== -1 && res.data.warn.indexOf('</a>') !== -1)) {
                            JFrogNotifications.create(res.data, true);
                        }
                        else {
                            JFrogNotifications.create(res.data);
                        }
                    } else if (res.data.feedbackMsg) {
                        JFrogNotifications.create(res.data.feedbackMsg);
                    }
                }
            }
            return res;
        },
        responseError: function (res) {
            // Response error as array:
            if (res.data && res.data.errors && res.data.errors.length && !(res.config && res.config.params && res.config.params.$suppress_toaster)) {
                try {
                    JFrogNotifications.create(JSON.parse(res.data.errors[0].message));
                }
                catch (e) { // message is not a json object but a simple string
                    JFrogNotifications.create({error: res.data.errors[0].message});
                }
            }
            // Response error as single object:
            else if (res.data && (res.data.error || res.data.warn) && !(res.config && res.config.params && res.config.params.$suppress_toaster)) {
                JFrogNotifications.create(res.data);
            }
            return $q.reject(res);
        }
    }
}