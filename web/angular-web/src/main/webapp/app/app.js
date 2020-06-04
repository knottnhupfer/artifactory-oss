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
import Models from './models/models.module';
import ValidationConstants from './constants/validation.constants.js';

import EVENTS from './constants/artifacts_events.constants';

if (angular.version.full !== '1.7.2') console.log("%cWrong AngularJS version!","color: #ff0000;");

// For debugging:
window._inject = function(injectable) {
    return angular.element(document.body).injector().get(injectable);
}
if (!String.prototype.endsWith) {
    String.prototype.endsWith = function (str) {
        return this.substr(this.length - str.length,str.length)===str;
    }
}
if (!String.prototype.startsWith) {
    String.prototype.startsWith = function (str) {
        return this.substr(0, str.length)===str;
    }
}

/**
 * providers configurations
 * @param $urlRouterProvider
 */
function appConfig($stateProvider, $locationProvider, $urlRouterProvider, ngClipProvider, $httpProvider, JFrogUILibConfigProvider) {

    JFrogUILibConfigProvider.setConfig({
        customValidationMessages: ValidationConstants,
        customModalTemplatesPath: 'modal_templates',
        customEventsDefinition: EVENTS,
        webworkersPath: location.pathname
    });

    $locationProvider.hashPrefix('');

    $urlRouterProvider.otherwise(function($injector,$location) {
        if ($location.path() === '' || $location.path() === '/') return '/home';
        else return '/404';
    });
    ngClipProvider.setPath("css/ZeroClipboard.swf");
    $httpProvider.interceptors.push('artifactorySpinnerInterceptor');
    $httpProvider.interceptors.push('artifactoryMessageInterceptor');
    $httpProvider.interceptors.push('artifactorySessionInterceptor');
    $httpProvider.interceptors.push('artifactoryServerErrorInterceptor');
    $httpProvider.interceptors.push('artifactoryDebugInterceptor');
}

function appRun($httpBackend, $rootScope, ArtifactoryFeatures, $timeout, $animate, ArtifactoryHttpClient, RESOURCE, ArtifactoryState, JFrogNotifications) {

    $httpBackend.whenPOST(/.*/).passThrough();
    $httpBackend.whenPUT(/.*/).passThrough();
    $httpBackend.whenGET(/.*/).passThrough();
    $httpBackend.whenDELETE(/.*/).passThrough();
    $httpBackend.whenPATCH(/.*/).passThrough();
    $httpBackend.whenHEAD(/.*/).passThrough();
    defineCodeMirrorMimes();
    defineCodeMirrorLinkOverlay();
    defineCodeMirrorAqlMode();

    $timeout(()=>{
        if (ArtifactoryFeatures.isOss()) {
            installHiringDevsHook();
        }
    },5000);

    ArtifactoryHttpClient.get(RESOURCE.TREE_BROWSER + '/repoOrder').then(repoOrder => {
        ArtifactoryState.setState('repoOrder', repoOrder.data);
    });

    let msgs = ["Lorem ipsum dolor sit amet, consectetur adipisicing elit. Cupiditate, vel.","Lorem ipsum dolor sit amet.","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Error incidunt necessitatibus nemo suscipit, voluptate aliquam culpa adipisci! Doloremque pariatur commodi debitis vitae, aut quisquam rem vero nam atque veniam excepturi.","Lorem ipsum dolor sit amet, consectetur adipisicing.","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ipsum magnam in debitis dolore, ipsam voluptatem sed minus quisquam!","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quia, repellendus, provident.","Lorem ipsum dolor sit amet, consectetur adipisicing elit.","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium?","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Saepe non consequuntur earum quam eum ut laboriosam, nam incidunt.","Lorem ipsum dolor sit amet, consectetur adipisicing elit."];

    window._notification = function(n = 1) {
        for (var i=0;i<n;i++) {
            JFrogNotifications.create({ info: msgs[Math.floor(Math.random() * 10) + 0], timeout: 60000 });
        }
        $rootScope.$apply();

    };


    tempFixForAnimateParamsReversal($animate);
    logNgAnimations($animate);
}

angular.module('artifactory.ui', [
    'jfrog.ui.essentials',
    'jfrog.native.ui',
    'color.picker',
    'ui.layout',
    'ngMockE2E',
    'selectize',
    'angular-capitalize-filter',
    'monospaced.mousewheel',

    // Application modules
    'artifactory.templates',
    'artifactory.services',
    'artifactory.directives',
    'artifactory.dao',
    'artifactory.ui_components',
    'artifactory.states',
    'artifactory.filters',
    Models.name
])
    .config(appConfig)
    .run(appRun);

function aliasMime(newMime, existingMime) {
    CodeMirror.defineMIME(newMime, CodeMirror.mimeModes[existingMime]);
}
function defineCodeMirrorMimes() {
    aliasMime('text/x-java-source', 'text/x-java');
    aliasMime('pom', 'text/xml');

    /* Example definition of a simple mode that understands a subset of
     * JavaScript:
     */

}


function defineCodeMirrorAqlMode() {
    CodeMirror.defineMode("aql", function () {
        var urlRegex = /^https?:\/\/[a-zA-Z]+(\.)?(:[0-9]+)?.+?(?=\s|$|"|'|>|<)/;

        let inApiKey = false;
        return {
            token: function (stream, state) {

                if (stream.match(/(?:curl|\-\H|\-\X|\-d|POST)\b/)) {
                    return "external-command";
                }
                else if (stream.match(/(?:X\-Api\-Key)\b/)) {
                    inApiKey=true;
                    return "header-tag";
                }
                else if (stream.match("'")) {
                    inApiKey = false;
                    return null;
                }
                else if (stream.match(/(?:find|include|limit|sort|offset)\b/)) {
                    return "aql-keyword";
                }
                else if (stream.match(/(?:\$and|\$or|\$ne|\$gt|\$gte|\$lt|\$lte|\$rf|\$msp|\$match|\$nmatch|\$eq|\$asc|\$desc)\b/)) {
                    return "aql-operators";
                }
                else if (stream.match(/(?:items|builds|entries)\b/)) {
                    return "aql-domain";
                }
                else if (stream.match(/[\{\}\[\]\(\)]+/)) {
                    return "aql-brackets";
                }
                else if (stream.match(urlRegex)) {
                    return "api-url";
                }
                else {
                    let ret = null;
                    if (inApiKey && !stream.match(':')) {
                        ret = "api-key";
                    }
                    stream.next();
                    return ret;
                }
            }
        };

    });
}

function defineCodeMirrorLinkOverlay() {
    var urlRegex = /^https?:\/\/[a-zA-Z]+(\.)?(:[0-9]+)?.+?(?=\s|$|"|'|>|<)/;
    CodeMirror.defineMode("links", function (config, parserConfig) {
        var linkOverlay = {
            token: function (stream, state) {
                if (stream.match(urlRegex)) {
                    return "link";
                }
                while (stream.next() != null && !stream.match(urlRegex, false)) {
                }
                return null;
            }
        };

        return CodeMirror.overlayMode(CodeMirror.getMode(config, config.mimeType || "text/xml"), linkOverlay);
    });
}




function installHiringDevsHook() {
    window.u = {
        r: {
            reading: function() {
                window.never={
                    mind: function() {
                        window.location.href="https://www.jfrog.com/about/open-positions/";
                    }
                };
                setTimeout(function() {
                    delete window.never;
                },500);
                return false;
            }
        }
    };
    console.log('%cif (u.r.reading(this) && u.can(code) && u.r.looking4.a.job) {\n    u.may(b.come.a(new JFrog("Star Developer")));\n}\nelse {\n    never.mind();\n}\n// Run this code snippet to find out more about CAREERS & OPPORTUNITIES @ JFrog', "font: 12px sans-serif; color: #43a047;");
}


function tempFixForAnimateParamsReversal($animate) {
    let origFunc = $animate.enabled.bind($animate);
    $animate.enabled = function() {
        if (typeof arguments[0] === 'boolean' && typeof arguments[1] === 'object') {
            let temp = arguments[0];
            arguments[0] = arguments[1];
            arguments[1] = temp;
        }
        return origFunc.apply($animate, arguments);
    }
}

function logNgAnimations($animate) {
    if (localStorage._logNgAnimations) {
        setInterval(()=>{
            let enters = $('.ng-enter').get();
            let leaves = $('.ng-leave').get();

            if (enters.length || leaves.length) {

                let all = enters.concat(leaves);

                all.forEach((elem) => {
                    if ($animate.enabled(elem)) {
                        console.log('ngAnimating: ',elem);
                    }
                });
            }
        },100);
    }
}


