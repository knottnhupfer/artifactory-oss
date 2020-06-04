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
export class LinksController {
    constructor(GoogleAnalytics, ArtifactoryFeatures) {
        this.features = ArtifactoryFeatures;
        this.GoogleAnalytics = GoogleAnalytics;

        this.links = [
            {
                linkText: 'User Guide',
                class: 'user-guide',
                url: 'https://service.jfrog.org/artifactory/home/userguide',
                svg: 'images/userguide.svg'
            },
            {
                linkText: 'Webinar Signup',
                class: 'webinar',
                url: 'https://service.jfrog.org/artifactory/home/webinars',
                svg: 'images/webinar.svg'
            },
            {
                linkText: 'Support Portal',
                class: 'support',
                url: 'https://service.jfrog.org/artifactory/home/supportportal',
                svg: 'images/support.svg'
            },
            {
                linkText: 'Stack Overflow',
                class: 'stackoverflow',
                url: 'https://service.jfrog.org/artifactory/home/stackoverflow',
                svg: 'images/stackoverflow.svg'
            },
            {
                linkText: 'Blog',
                class: 'blogs',
                url: 'https://service.jfrog.org/artifactory/home/blog',
                svg: 'images/blogs.svg'
            },
            {
                linkText: 'Rest API',
                class: 'rest-api',
                url: 'https://service.jfrog.org/artifactory/home/restapi',
                svg: 'images/rest_api.svg'
            }
        ];

        if (this.features.isJCR()) {
            const stackoverflowIndex = _.findIndex(this.links, i => {
                return i.class === 'stackoverflow';
            });
            this.links[stackoverflowIndex].url = 'https://stackoverflow.com/questions/tagged/jfrog-container-registry';
        }

    }

    linkClick(linkText) {
        this.GoogleAnalytics.trackEvent('Homepage','Knowledge Resources Link',linkText)
    }

}