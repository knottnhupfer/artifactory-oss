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
import PackagesNativeController from "./native.controller";

export const MODULE_PACKAGE_NATIVE = 'packages';

function NativeConfig($stateProvider) {
    $stateProvider
            .state({
                name: 'packagesNative',
                url: '/packages/{subRouterPath:JFrogSubRouterPath}',
                controller: 'PackagesNativeController',
                controllerAs: '$ctrl',
                template: require('./native.view.html'),
                parent: 'app-layout',
            });
}

angular.module(MODULE_PACKAGE_NATIVE, [])
        .config(NativeConfig)
        .controller('PackagesNativeController', PackagesNativeController);
