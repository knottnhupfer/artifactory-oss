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
import ApiConstants from "../constants/api.constants";
import {ArtifactoryCookies} from "./artifactory_cookies";
import {ArtifactoryHttpClient} from "./artifactory_http_client";
import {ArtifactoryStorage} from "./artifactory_storage";
import {ArtifactoryXmlParser} from "./artifactory_xml_parser";
import KeyboardShortcutsModalService from './keyboard_shortcuts_service/keyboard_shortcuts_service';
import {UserFactory} from "./user";
import {ArtifactoryState} from "./artifactory_state";
import {artifactorySessionInterceptor} from "./artifactory_session_interceptor";
import {artifactoryDebugInterceptor} from "./artifactory_debug_interceptor";
import {artifactorySpinnerInterceptor} from "./artifactory_spinner_interceptor";
import {artifactoryMessageInterceptor} from "./artifactory_message_interceptor";
import {artifactoryServerErrorInterceptor} from "./artifactory_server_error_interceptor";
import {ArtifactoryModelSaverFactory} from "./artifactory_model_saver";
import {ArtifactoryFeatures} from "./artifactory_features";
import {GoogleAnalytics} from "./google_analytics";
import {NativeBrowser} from "./native_browser";
import {ArtifactActions} from "./artifact_actions";
import {SetMeUpModal} from "./set_me_up_modal";
import {ArtifactoryDeployModal} from "./artifactory_deploy_modal";
import {PushToBintrayModal} from "./push_to_bintray_modal.js";
import {parseUrl} from "./parse_url";
import {recursiveDirective} from "./recursive_directive";
import {ArtifactorySidebarDriver} from "./artifactory_sidebar_driver";
import {OnBoardingWizard} from "./onboarding_wizard";
import {SaveArtifactoryHaLicenses} from './save_artifactory_ha_licenses';

//import {artifactoryIFrameDownload}              from './artifactory_iframe_download';

angular.module('artifactory.services', ['ui.router', 'artifactory.ui_components', 'toaster'])
        .constant('RESOURCE', ApiConstants)
        .service('ArtifactoryCookies', ArtifactoryCookies)
        .service('ArtifactoryHttpClient', ArtifactoryHttpClient)
        .service('ArtifactoryStorage', ArtifactoryStorage)
        .service('ArtifactoryXmlParser', ArtifactoryXmlParser)
        .service('User', UserFactory)
        .service('ArtifactoryState', ArtifactoryState)
        //        .factory('artifactoryIFrameDownload', artifactoryIFrameDownload)
        .factory('artifactorySessionInterceptor', artifactorySessionInterceptor)
        .factory('artifactoryDebugInterceptor', artifactoryDebugInterceptor)
        .factory('artifactoryMessageInterceptor', artifactoryMessageInterceptor)
        .factory('artifactoryServerErrorInterceptor', artifactoryServerErrorInterceptor)
        .factory('artifactorySpinnerInterceptor', artifactorySpinnerInterceptor)
        .service('NativeBrowser', NativeBrowser)
        .service('ArtifactoryFeatures', ArtifactoryFeatures)
        .service('GoogleAnalytics', GoogleAnalytics)
        .service('ArtifactActions', ArtifactActions)
        .service('SetMeUpModal', SetMeUpModal)
        .factory('parseUrl', parseUrl)
        .factory('recursiveDirective', recursiveDirective)
        .factory('ArtifactoryModelSaver', ArtifactoryModelSaverFactory)
        .service('ArtifactoryDeployModal', ArtifactoryDeployModal)
        .service('PushToBintrayModal', PushToBintrayModal)
        .service('ArtifactorySidebarDriver', ArtifactorySidebarDriver)
        .service('OnBoardingWizard', OnBoardingWizard)
        .service('KeyboardShortcutsModalService', KeyboardShortcutsModalService)
        .service('SaveArtifactoryHaLicenses', SaveArtifactoryHaLicenses);