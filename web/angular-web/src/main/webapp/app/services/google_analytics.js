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
export class GoogleAnalytics {
    constructor($timeout,$interval, $location, ArtifactoryState, FooterDao) {

        this.$location = $location;
        this.$interval = $interval;
        this.$timeout = $timeout;
        this.ArtifactoryState = ArtifactoryState;
        this.footerDao = FooterDao;


        this.footerDao.get().then(() => {
            this.allowGA = (this.footerDao.getInfo().isAol && !this.footerDao.getInfo().isDedicatedAol) || _.includes(this.footerDao.getInfo().buildNumber, 'SNAPSHOT');
        });

        this.artifactsPageCounter = '';
    }


    _setUpGA() {
        if (this.allowGA) {

            // setup timeout settings
            this.GA = {
                active: true
            };

             let uaCode = (this.footerDao.getInfo().gaAccount) ? 'UA-87840116-2' : 'UA-87840116-1';

            (function(i,s,o,g,r,a,m) {i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
                    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;if (m) m.parentNode.insertBefore(a,m);
            })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

            window.ga('create', uaCode, 'auto');
            window.ga('set', 'dataSource', 'Artifactory UI');

            this.ArtifactoryState.setState('gaTrackPage', () => {
                let url = this.$location.$$absUrl;
                if (url.slice(-2) == '//') return;

                // ignore entry to tree without path (user will redirect to first result in tree)
                if (url.match(/(#.+)/) && url.match(/(#.+)/)[1] == '#/artifacts/browse/tree/General/') return;


                // * * * * * * Calculating time inside artifacts page * * * * * * //

                if (url.match(/#\/artifacts/)) {    // if in artifacts page
                    let currentTime = Date.now();

                    if (this.artifactsPageCounter != '') {
                        this.timeOnArtifactsPage = currentTime - this.artifactsPageCounter;
                    }
                    this.artifactsPageCounter = currentTime;

                } else {    // if not artifacts page
                    if (this.artifactsPageCounter) {
                        this.timeOnArtifactsPage = Date.now() - this.artifactsPageCounter;  // calculate time in case of leaving artifacts page
                        delete this.artifactsPageCounter;
                    }
                }

                // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //


                // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                //
                // Send pageview without the full path for artifacts browser page (tree and simple)
                //
                // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

                if (url.match(/\/browse\/tree\/|browse\/simple/)) {
                    // If url location is in tree browse or simple browse ignore the path
                    // from the url set to GA. leave only the relevant part of the URL.
                    let match = url.match(/#.*(tree|simple)\/.*?\//)
                    if (match) this._sendPageView(match[0])

                } else if (url.match(/(#.+)/)) {
                    this._sendPageView(url.match(/(#.+)/)[1])
                }
            })

            this.ArtifactoryState.getState('gaTrackPage')();
        }
    }

    _createRandomId() {

        let text = "";
        let possible = "abcdefghijklmnopqrstuvwxyz0123456789";

        for( var i=0; i < 10; i++ )
            text += possible.charAt(Math.floor(Math.random() * possible.length));

        return text;

    }

    _generateDimensions() {

        let randomId = this._createRandomId();
        let date = new Date();

        let timestamp = Math.floor(date.getTime()/1000);

        let sessionId = "S" + randomId + "-" + timestamp;
        let interactionId = "I" + randomId + "-" + timestamp;

        let dimensions = {
            timestamp: timestamp,
            sessionId: sessionId,
            interactionId: interactionId
        }

        return dimensions;

    }


    _sendPageView(pageUrl, hitType = 'pageview') {

        let dimensions = this._generateDimensions();

        window.ga('set', {
            page: pageUrl
        });


        if (this.timeOnArtifactsPage) {
            window.ga('send', {
                hitType: hitType,
                metric1: this.timeOnArtifactsPage,
                dimension4: dimensions.timestamp,
                dimension5: dimensions.interactionId,
                dimension6: dimensions.sessionId
            });
            delete this.timeOnArtifactsPage;
            return;
        }

        window.ga('send', {
            hitType: hitType,
            dimension4: dimensions.timestamp,
            dimension5: dimensions.interactionId,
            dimension6: dimensions.sessionId
        });

    }

    trackEvent(eventCategory, eventAction, eventLabel = 'undefined', eventValue = null, dimension1 = 'undefined', dimension2 = 'undefined', dimension3 = 'undefined') {

        //this._generateDimensions();

        if (this.allowGA && window.ga) {
            // Track google analytics event
            // ga('send', 'event', [eventCategory], [eventAction], [eventLabel], [eventValue], [fieldsObject]);
            // More on this here: https://developers.google.com/analytics/devguides/collection/analyticsjs/events

            let dimensions = this._generateDimensions();

            window.ga('send', {
                hitType: 'event',
                eventCategory: eventCategory,
                eventAction: eventAction,
                eventLabel: eventLabel,
                eventValue: eventValue,
                dimension1: dimension1,
                dimension2: dimension2,
                dimension3: dimension3,
                dimension4: dimensions.timestamp,
                dimension5: dimensions.interactionId,
                dimension6: dimensions.sessionId
            });
        }
    }
}