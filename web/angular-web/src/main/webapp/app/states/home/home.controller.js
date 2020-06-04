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
import {ArtifactCountController} from '../../home_widgets/artifacts_count_widget/artifacts_count.controller';
import {VersionController} from '../../home_widgets/version_widget/version.controller';
import {UpdatesController} from '../../home_widgets/updates_widget/updates.controller';
import {QuickSearchController} from '../../home_widgets/quick_search/quick_search.controller';
import {AddonsController} from '../../home_widgets/addons_widget/addons.controller';
import {LinksController} from '../../home_widgets/links_widget/links.controller';
import {MostDownloadedController} from '../../home_widgets/most_downloaded_widget/most_downloaded';
import {LastDeployedController} from '../../home_widgets/last_deployed_widget/last_deployed';
import {SetMeUpWidgetController} from '../../home_widgets/set_me_up_widget/set-me-up.controller';

import EVENTS from '../../constants/artifacts_events.constants';

export class HomeController {
    constructor($timeout,$scope, User, ArtifactoryState, ArtifactoryFeatures, HomePageDao, JFrogEventBus, GoogleAnalytics) {
        this.$timeout = $timeout;
        this.$scope = $scope;
        this.homePageDao = HomePageDao;
        this.user = User.getCurrent();
        this.features = ArtifactoryFeatures;
        this.ArtifactoryState = ArtifactoryState;
        this.GoogleAnalytics = GoogleAnalytics;
        this.widgetsOptions = {
            padding: 15,
            minHeight: 490
        };
        let url ="https://jfrog.com/artifactory/eula/";
        this.homeTitle = 'Artifactory is happily serving';
        if (this.features.isConanCE()) {
            url = "https://jfrog.com/aritfactory-community-edition-c-c-eula/"
        }
        else if (this.features.isJCR()) {
            // todo:JCR: set proper eula link
            this.homeTitle = 'JFrog Container Registry is happily serving';
            url = "https://jfrog.com/aritfactory-community-edition-c-c-eula/"
        }
        this.footerText = `Other than JFrog's trademarks, marks and logos, all other trademarks displayed in this application are owned by their respective holders. JFrog is not sponsored by, endorsed by or affiliated with the holders of these trademarks. More info here - <a target=_blank" href="https://jfrog.com/artifactory/cloud-eula/" class="jf-link">Terms of Use</a>,<a target=_blank" class="jf-link" href="${url}">EULA</a>.`;

        this.defineWidgets();
        this.createLayout();

        JFrogEventBus.register(EVENTS.REFRESH_PAGE_CONTENT,()=>{
            this.getData();
        });

        this.getData();
        this.isAol = () => {return this.features.isAol()};
        this.jfNewsIsOn = false; // Turns jf_news on / off
    }

    getData() {
        this.homepageData = {};
        this.homePageDao.get({widgetName:'artifactCount'}).$promise.then((data)=> {
            _.extend(this.homepageData, data.widgetData);
            this.homePageDao.get({widgetName:'info'}).$promise.then((data)=> {
                _.extend(this.homepageData, data.widgetData);
            });
        });

    }

    defineWidgets() {
        this.widgets = {
            artifactCount: {
                name: 'Artifacts Count',
                id: 'artifacts-count',
                templateUrl: 'home_widgets/artifacts_count_widget/artifacts_count.html',
                controller: ArtifactCountController,
                scroll: false,
                showSpinner: true,
            },
            version: {
                name: 'Version',
                id: 'version',
                templateUrl: 'home_widgets/version_widget/version.html',
                controller: VersionController,
                scroll: false,
                showSpinner: true,
            },
            updates: {
                name: 'Updates',
                id: 'updates',
                templateUrl: 'home_widgets/updates_widget/updates.html',
                controller: UpdatesController,
                scroll: true,
                showSpinner: true
            },
            quickSearch: {
                name: 'Quick Search',
                id: 'quick-search',
                templateUrl: 'home_widgets/quick_search/quick_search.html',
                controller: QuickSearchController,
                scroll: false,
                showSpinner: false
            },
            addons: {
                name: 'Addons',
                id: 'addons',
                templateUrl: 'home_widgets/addons_widget/addons.html',
                controller: AddonsController,
                scroll: false,
                showSpinner: true,
            },
            usefulLinks: {
                name: 'Useful Links',
                templateUrl: 'home_widgets/links_widget/links.html',
                controller: LinksController,
                scroll: false,
                showSpinner: false
            },
            lastDeployed: {
                name: 'Last Deployed',
                id: 'last-deployed',
                templateUrl: 'home_widgets/last_deployed_widget/last_deployed.html',
                controller: LastDeployedController,
                scroll: true,
                showSpinner: true
            },
            mostDownloaded: {
                name: 'Most Downloaded',
                id: 'most-downloaded',
                templateUrl: 'home_widgets/most_downloaded_widget/most_downloaded.html',
                controller: MostDownloadedController,
                scroll: true,
                showSpinner: true
            },
            setMeUp: {
                name: 'Set me up',
                id: 'set-me-up',
                templateUrl: 'home_widgets/set_me_up_widget/set-me-up.html',
                controller: SetMeUpWidgetController,
                scroll: true,
                showSpinner: true
            },
        }
    }

    createLayout() {
        this.widgetsLayout = {
            main: {
                rows: [
                    {
                        size: '73%',
                        cells: ['100% #top']
                    },
                    {
                        size: '27%',
                        cells: ['100% #bottom']
                    }
                ]
            },
            top: {
                main: {
                    rows: [
                        {
                            size:'100%',
                            cells: ['33% #topLeft', '34% #topCenter', '33% #topRight']
                        }
                    ]
                },
                topLeft: {
                    columns: [
                        {
                            size: '100%',
                            cells: ['40% @quickSearch', '60% @usefulLinks']
                        }
                    ]
                },
                topCenter: {
                    main: {
                        columns: [
                            {
                                size: '100%',
                                cells: ['100% @setMeUp']
                            }
                        ]
                    }
                },
                topRight: {
                    columns: [
                        {
                            size: '100%',
                            cells: ['50% @lastDeployed', '50% @mostDownloaded']
                        }
                    ]
                }
            },
            bottom: {
                columns: [
                    {
                        size:'100%',
                        cells: ['100% @addons']
                    }
                ]
            }
        }

        if (this.features.isEdgeNode()) this.widgetsLayout.top.topRight.columns[0].cells = ['100% @mostDownloaded'];
    }

}
