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
export class QuickSearchController {
    constructor($state, ArtifactoryFeatures, GoogleAnalytics) {
        this.GoogleAnalytics = GoogleAnalytics;
        this.$state = $state;
        this.features = ArtifactoryFeatures;

        this.links = [
            {
                title: 'Package\nSearch',
                search: 'package'
            },
            {
                title: 'Archive\nSearch',
                search: 'archive'
            },
            {
                title: 'Property\nSearch',
                search: 'property'
            },
            {
                title: 'Checksum\nSearch',
                search: 'checksum'
            },
            {
                title: 'JCenter\nSearch',
                search: 'remote'
            }
        ]

        if (this.features.isEdgeNode() || this.features.isConanCE() || this.features.isJCR()) {
            this.links = _.filter(this.links, (link) => {
                return link.search != 'remote';
            });
        }
    }

    search() {
        if (!this.query) return;

        let query = {
            "search": "quick",
            "query": this.query
        }
        this.$state.go('search',{searchType: 'quick', query: btoa(JSON.stringify(query)), fromHome: true});
    }

    gotoSearch(searchType) {
        this.GoogleAnalytics.trackEvent('Homepage' , 'Quick Search link' , searchType);
        this.$state.go('search',{searchType: searchType});
    }

}
