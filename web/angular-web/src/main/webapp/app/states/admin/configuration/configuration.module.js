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
import Licenses from "./licenses/licenses.module";
import Mail from "./mail/mail.module";
import Xray from "./xray/xray.module";
import Proxies from "./proxies/proxies.module";
import ReverseProxies from "./reverse_proxies/reverse_proxies.module";
import RegisterPro from "./register_pro/register_pro.module";
import Bintray from "./bintray/bintray.module";
import General from "./general/general.module";
import PropertySets from "./property_sets/property_sets.module";
import HighAvailability from "./ha/ha.module";
import {AdminConfigurationController} from "./configuration.controller";

/**
 * configuration and state definition
 * @param $stateProvider
 */
function configurationConfig($stateProvider) {

    $stateProvider
            .state('admin.configuration', {
                url: '/configuration',
                template: '<ui-view></ui-view>',
                controller: 'AdminConfigurationController as AdminConfiguration'
            })
}

/**
 * Module definition
 */
export default angular.module('admin.configuration', [
    Licenses.name,
    Mail.name,
    Xray.name,
    Proxies.name,
    ReverseProxies.name,
    RegisterPro.name,
    Bintray.name,
    General.name,
    PropertySets.name,
    HighAvailability.name

])
        .config(configurationConfig)
        .controller('AdminConfigurationController', AdminConfigurationController);