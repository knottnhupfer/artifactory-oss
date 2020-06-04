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
import {jfBuilds} from "./jf_builds";
import {jfXray} from "./jf_xray";
import {jfEffectivePermissions} from "./jf_effective_permissions";
import {jfWatchers} from "./jf_watchers";
import {jfGeneral} from "./jf_general";
import {jfProperties} from "./jf_properties";
import {jfViewSource} from "./jf_view_source";
import {jfPomView} from "./jf_pom_view";
import {jfXmlView} from "./jf_xml_view";
import {jfIvyView} from "./jf_ivy_view";
import {jfNuget} from "./jf_nuget";
import {jfComposer} from "./jf_composer";
import {jfPyPi} from "./jf_pypi";
import {jfHelm} from "./jf_helm";
import {jfGo} from "./jf_go";
import {jfCran} from "./jf_cran";
import {jfConda} from "./jf_conda";
import {jfPuppet} from "./jf_puppet";
import {jfBower} from "./jf_bower";
import {jfDocker} from "./jf_docker";
import {jfDockerAncestry} from "./jf_docker_ancestry";
import {jfDockerV2} from "./jf_docker_v2";
import {jfRubyGems} from "./jf_ruby_gems";
import {jfNpmInfo} from "./jf_npm_info";
import {jfRpm} from "./jf_rpm_info";
import {jfCocoapods} from "./jf_cocoapods";
import {jfConan} from './jf_conan';
import {jfConanPackage} from './jf_conan_package';
import {jfStashInfo} from "./jf_stash_info";
import {jfDebianInfo} from "./jf_debian_info";
import {jfOpkgInfo} from "./jf_opkg_info";
import {jfChefInfo} from "./jf_chef_info";

export default angular.module('infoTabs', [])
        .directive({
            'jfBuilds': jfBuilds,
            'jfXray': jfXray,
            'jfEffectivePermissions': jfEffectivePermissions,
            'jfWatchers': jfWatchers,
            'jfGeneral': jfGeneral,
            'jfProperties': jfProperties,
            'jfViewSource': jfViewSource,
            'jfPomView': jfPomView,
            'jfXmlView': jfXmlView,
            'jfIvyView': jfIvyView,
            'jfNuget': jfNuget,
            'jfComposer': jfComposer,
            'jfPyPi': jfPyPi,
            'jfHelm': jfHelm,
            'jfGo': jfGo,
            'jfCran': jfCran,
            'jfConda': jfConda,
            'jfPuppet': jfPuppet,
            'jfBower': jfBower,
            'jfConan': jfConan,
            'jfConanPackage': jfConanPackage,
            'jfDocker': jfDocker,
            'jfDockerAncestry': jfDockerAncestry,
            'jfDockerV2': jfDockerV2,
            'jfRubyGems': jfRubyGems,
            'jfNpmInfo': jfNpmInfo,
            'jfRpm': jfRpm,
            'jfCocoapods': jfCocoapods,
            'jfStashInfo': jfStashInfo,
            'jfDebianInfo': jfDebianInfo,
            'jfChefInfo': jfChefInfo,
            'jfOpkgInfo': jfOpkgInfo
        });