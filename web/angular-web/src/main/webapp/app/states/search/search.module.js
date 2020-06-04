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
import {SearchStateController} from './search.controller';
import {searchQueryMaker} from './search_query_maker';
import {packageSearch} from './package_search';


function searchConfig($stateProvider) {
    $stateProvider

        .state('search', {
            url: '/search/{searchType}/{query}',
            parent: 'app-layout',
            templateUrl: 'states/search/search.html',
            controller: 'SearchStateController as SearchController',
            params: {oauthError: null, fromHome: false},
        })
}

export default angular.module('search', [])
    .config(searchConfig)
    .directive('searchQueryMaker', searchQueryMaker)
    .directive('packageSearch', packageSearch)
    .controller('SearchStateController', SearchStateController);

