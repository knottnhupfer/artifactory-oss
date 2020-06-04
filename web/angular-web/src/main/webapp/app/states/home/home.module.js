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
import {HomeController} from "./home.controller";

function homeConfig ($stateProvider) {

    $stateProvider
            .state('home', {
                url: '/home',
                parent: 'app-layout',
                templateUrl: 'states/home/home.html',
                controller: 'HomeController as Home',
                onExit: (TreeBrowserDao) => {
                    TreeBrowserDao.invalidateRoots();
                },
            })
}

export default angular.module('home', [])
        .config(homeConfig)
        .controller('HomeController', HomeController)