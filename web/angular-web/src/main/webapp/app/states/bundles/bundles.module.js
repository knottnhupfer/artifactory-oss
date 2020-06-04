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
import {BundlesListController} from "../bundles/bundles_list.controller";
import {BundlePageController} from "../bundles/bundle_page.controller";

function bundlesConfig ($stateProvider) {

    $stateProvider
            .state('bundles', {
                url: '/bundles',
                templateUrl: 'states/bundles/bundles.html',
                parent: 'app-layout',
            })
            .state('bundles.list', {
                url: '/{tab}',
                templateUrl: 'states/bundles/bundles_list.html',
                controller: 'BundlesListController as BundlesList'
            })
            .state('bundles.bundle_page', {
                url: '/{type}/{bundleName}/{version}',
                templateUrl: 'states/bundles/bundle_page.html',
                controller: 'BundlePageController as BundlePage'
            })
}

export default angular.module('bundles', [])
        .config(bundlesConfig)
        .controller('BundlesListController', BundlesListController)
        .controller('BundlePageController', BundlePageController)