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
import ConfigDescriptor from "./config_descriptor/config_descriptor.module";
import Maintenance from "./maintenance/maintenance.module";
import SecurityDescriptor from "./security_descriptor/security_descriptor.module";
import StorageSummary from "./storage_summary/storage_summary.module";
import SystemInfo from "./system_info/system_info.module";
import SystemLogs from "./system_logs/system_logs.module";
import SupportPage from "./support_page/support_page.module";
import LogAnalytics from "./log_analytics/log_analytics.module";
import {AdminAdvancedController} from "./advanced.controller";

function advancedConfig($stateProvider) {
    $stateProvider
            .state('admin.advanced', {
                url: '/advanced',
                template: '<ui-view></ui-view>',
                controller: 'AdminAdvancedController as AdminAdvanced'
            })
}

export default angular.module('admin.advanced', [
    ConfigDescriptor.name,
    Maintenance.name,
    SecurityDescriptor.name,
    StorageSummary.name,
    SystemInfo.name,
    SystemLogs.name,
    SupportPage.name,
    LogAnalytics.name
])
        .config(advancedConfig)
        .controller('AdminAdvancedController', AdminAdvancedController);

