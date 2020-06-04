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
import EVENTS from '../constants/artifacts_events.constants';

export function artifactorySpinnerInterceptor($injector, $timeout, $q, JFrogEventBus) {

    let SPINNER_TIMEOUT = 500; //milis
    let serial = 0;
    let timeouts = {};
    let pendings = [];
    let canceled = [];
    let inDelay = [];

    JFrogEventBus.register(EVENTS.CANCEL_SPINNER, () => {
        if (pendings.length) {
        JFrogEventBus.dispatch(EVENTS.HIDE_SPINNER);
            canceled = canceled.concat(pendings);
            pendings = [];
//            console.log('canceled: ', canceled);
        }
    });


    function request(req) {

        req.headers['Request-Agent'] = 'artifactoryUI';

        if ((!req.params || !req.params.$no_spinner) && !_.contains(req.url,'$no_spinner=true') && req.url.startsWith('../ui/') ) {

            let domain = req.params ? req.params.$spinner_domain : undefined;

            req.headers.serial = serial;

            pendings.push(serial);

            inDelay.push(serial);
            timeouts[serial] = $timeout(()=> {
                let canceledIndex = canceled.indexOf(req.headers.serial);

                if (canceledIndex < 0) {
                    JFrogEventBus.dispatch(EVENTS.SHOW_SPINNER, domain);
                }
                else {
                    canceled.splice(canceledIndex,1);
                }

                let inDelayIndex = inDelay.indexOf(req.headers.serial);
                if (inDelayIndex >= 0) inDelay.splice(inDelayIndex,1);

//                console.log('inDelay',inDelay);
            }, SPINNER_TIMEOUT);

            serial++;

        }

        return req;
    }

    function response(res) {
        if (handleResponse(res)) return res;
        else return $q.defer().promise;
    }

    function responseError(res) {
        if (handleResponse(res)) return $q.reject(res);
        else return $q.defer().promise;
    }

    function handleResponse(res) {
        let s = res.config.headers.serial;

        let pendingIndex = pendings.indexOf(s);
        if (pendingIndex >= 0) {
            pendings.splice(pendingIndex,1);
        }

        let inDelayIndex = inDelay.indexOf(s);

        let canceledIndex = canceled.indexOf(s);
        if (canceledIndex >= 0) {
//            console.log('canceled',res);
            if (inDelayIndex < 0) canceled.splice(canceledIndex,1);
            return false;
        }
        else {
            if (timeouts[s]) {
                if (inDelayIndex >= 0) inDelay.splice(inDelayIndex,1);
                else {
                    JFrogEventBus.dispatch(EVENTS.HIDE_SPINNER);
                }
                $timeout.cancel(timeouts[s]);
                delete timeouts[s];
            }

            return  true;
        }

    }

    return {
        response: response,
        request: request,
        responseError: responseError
    };
}
