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
import {jfPublishedModules} from "./jf_published_modules";
import {jfBuildInfoJson} from "./jf_build_info_json";
import {jfEnvironment} from "./jf_environment";
import {jfIssues} from "./jf_issues";
import {jfDiff} from "./jf_diff";
import {jfReleaseHistory} from "./jf_release_history";
import {jfLicenses} from "./jf_licenses";
import {jfEffectivePermission} from "./jf_effective_permission";

export default angular.module('buildTabs', [])
        .directive({
            'jfPublishedModules': jfPublishedModules,
            'jfBuildInfoJson': jfBuildInfoJson,
            'jfEnvironment': jfEnvironment,
            'jfIssues': jfIssues,
            'jfDiff': jfDiff,
            'jfReleaseHistory': jfReleaseHistory,
            'jfLicenses': jfLicenses,
            'jfBuildEffectivePermission': jfEffectivePermission
    });