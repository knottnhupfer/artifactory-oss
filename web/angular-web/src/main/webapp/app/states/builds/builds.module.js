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
import {AllBuildsController} from './all_builds.controller';
import {BuildsPageController} from './builds_page/builds_page.controller';
import buildTabs from './build_tabs/build_tabs.module';


function buildsConfig($stateProvider) {
    $stateProvider
            .state('builds', {
                url: '/builds',
                parent: 'app-layout',
                abstract: true,
                templateUrl: 'states/builds/builds.html'
            })
            .state('builds.all', {
                url: '/',
                templateUrl: 'states/builds/all_builds.html',
                controller: 'AllBuildsController as AllBuilds'
            })
            .state('builds.build_page', {
                url: '/:buildName/:buildNumber/:startTime/:tab/:moduleID',

                params: {
                    startTime: {
                        value: null,
                        squash: true
                    },
                    tab: {
                        value: 'published',
                        squash: true
                    },
                    moduleID: {
                        value: null,
                        squash: true
                    }
                },
                templateUrl: 'states/builds/builds_page/builds_page.html',
                controller: 'BuildsPageController as BuildsPage'
            })
}

export default angular.module('builds', ['buildTabs'])
        .config(buildsConfig)
        .controller('AllBuildsController', AllBuildsController)
        .controller('BuildsPageController', BuildsPageController)
