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
import EVENTS from "../../constants/artifacts_events.constants";

class jfSpinnerController {

    constructor($timeout, $scope, $state, JFrogEventBus, $element) {
        this.$scope = $scope;
        this.$state = $state;
        this.$timeout = $timeout;
        this.$element = $element;
        this.show = false;
        this.count = 0;
        this.JFrogEventBus = JFrogEventBus;
        this.intervalPromise = null;
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.SHOW_SPINNER, (domain) => {
            this.showSpinner(domain);
        });
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.HIDE_SPINNER, () => {
            this.hideSpinner()
        });
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.CANCEL_SPINNER, () => {
            this.count--;
            if (this.count<0) this.count = 0;
        });
    }

    showSpinner(domain) {
        if ((!domain && this.domain === 'body' && this.$state.current.name === 'login') ||
                (!domain && this.domain === 'content' && this.$state.current.name !== 'login') ||
                (this.domain === domain)) {

            this.count++;
            this.show = true;
            this.lastShowTime = (new Date()).getTime();
        }
    }

    hideSpinner() {

        let doHide = () => {
            this.count--;
            if (this.count<0) this.count = 0;
            if (this.count === 0) {
                this.show = false;
            }
        }

        if (!this.lastShowTime) doHide();
        else {
            let timeOn = (new Date()).getTime() - this.lastShowTime;
            if (timeOn > 600) doHide();
            else {
                let addTime = 600 - timeOn;
                this.$timeout(()=>{
                    doHide();
                },addTime);
            }
        }

    }

    isModalOpen() {
        return ($('.modal').length > 0) ? true : false;
    }

    getBrowser(){
        let userAgent = navigator.userAgent, temp,
            browserData = userAgent.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
        if(/trident/i.test(browserData[1])){
            temp=  /\brv[ :]+(\d+)/g.exec(userAgent) || [];
            return 'IE '+(temp[1] || '');
        }
        if(browserData[1]=== 'Chrome'){
            temp = userAgent.match(/\b(OPR|Edge)\/(\d+)/);
            if(temp!= null) {
                return temp.slice(1).join(' ').replace('OPR', 'Opera');
            }
        }
        browserData = browserData[2]? [browserData[1], browserData[2]]: [navigator.appName, navigator.appVersion, '-?'];
        if((temp= userAgent.match(/version\/(\d+)/i))!= null) browserData.splice(1, 1, temp[1]);
        return browserData.join(' ');
    }

}

export function jfSpinner() {

    return {
        restrict: 'E',
        scope: {
            domain: '@'
        },
        controller: jfSpinnerController,
        controllerAs: 'jfSpinner',
        templateUrl: 'directives/jf_spinner/jf_spinner.html',
        bindToController: true
    };
}
