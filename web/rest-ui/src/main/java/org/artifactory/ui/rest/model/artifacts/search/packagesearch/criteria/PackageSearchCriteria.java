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

package org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria;

import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.DebianNaming;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.result.*;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.aql.model.AqlPhysicalFieldEnum.itemActualSha1;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.result.DummyPackageSearchResultMerger.DUMMY_MERGER;

/**
 * Contains all available criteria specific to each package type the search supports
 *
 * @author Dan Feldman
 */
public enum PackageSearchCriteria {
    //Keep packages ordered alphabetically please!

    bowerName(() -> new PackageSearchCriterion(PackageSearchType.bower, "bower.name",
            new AqlUISearchModel("bowerName", "Name", "Bower Package Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("bower.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    bowerVersion(() -> new PackageSearchCriterion(PackageSearchType.bower, "bower.version",
            new AqlUISearchModel("bowerVersion", "Version", "Bower Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("bower.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    chefName(() -> new PackageSearchCriterion(PackageSearchType.chef, "chef.name",
            new AqlUISearchModel("chefName", "Name", "Chef Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("chef.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    chefVersion(() -> new PackageSearchCriterion(PackageSearchType.chef, "chef.version",
            new AqlUISearchModel("chefVersion", "Version", "Chef Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("chef.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    chefCategory(() -> new PackageSearchCriterion(PackageSearchType.chef, "chef.category",
            new AqlUISearchModel("chefCategory", "Category", "Chef Category", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("chef.category",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    chefPlatform(() -> new PackageSearchCriterion(PackageSearchType.chef, "chef.platform",
            new AqlUISearchModel("chefPlatform", "Platform", "Chef Platform", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("chef.platform",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    cocoapodsName(() -> new PackageSearchCriterion(PackageSearchType.cocoapods, "pods.name",
            new AqlUISearchModel("cocoapodsName", "Name", "CocoaPods Package Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("pods.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    cocoapodsVersion(() -> new PackageSearchCriterion(PackageSearchType.cocoapods, "pods.version",
            new AqlUISearchModel("cocoapodsVersion", "Version", "CocoaPods Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("pods.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    composerName(() -> new PackageSearchCriterion(PackageSearchType.composer, "composer.name",
            new AqlUISearchModel("composerName", "Name", "Composer Package Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("composer.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    composerVersion(() -> new PackageSearchCriterion(PackageSearchType.composer, "composer.version",
            new AqlUISearchModel("composerVersion", "Version", "Composer Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("composer.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    conanName(() -> new PackageSearchCriterion(PackageSearchType.conan, "conan.package.name",
            new AqlUISearchModel("conanName", "Name", "Conan Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conan.package.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    conanVersion(() -> new PackageSearchCriterion(PackageSearchType.conan, "conan.package.version",
            new AqlUISearchModel("conanVersion", "Version", "Conan Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conan.package.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    conanUser(() -> new PackageSearchCriterion(PackageSearchType.conan, "conan.package.user",
            new AqlUISearchModel("conanUser", "User", "Conan User", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conan.package.user",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    conanChannel(() -> new PackageSearchCriterion(PackageSearchType.conan, "conan.package.channel",
            new AqlUISearchModel("conanChannel", "Channel", "Conan Channel", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conan.package.channel",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    conanOs(() -> new PackageSearchCriterion(PackageSearchType.conan, "conan.settings.os",
            new AqlUISearchModel("conanOs", "OS", "Conan OS", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conan.settings.os",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    conanArch(() -> new PackageSearchCriterion(PackageSearchType.conan, "conan.settings.arch",
            new AqlUISearchModel("conanArch", "Architecture", "Conan Architecture", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conan.settings.arch",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    conanBuildType(() -> new PackageSearchCriterion(PackageSearchType.conan, "conan.settings.build_type",
            new AqlUISearchModel("conanBuildType", "Build Type", "Conan Build Type", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conan.settings.build_type",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    conanCompiler(() -> new PackageSearchCriterion(PackageSearchType.conan, "conan.settings.compiler",
            new AqlUISearchModel("conanCompiler", "Compiler", "Conan Compiler", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conan.settings.compiler",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    cranPackageName(() -> new PackageSearchCriterion(PackageSearchType.cran, "cran.name",
            new AqlUISearchModel("cranPackageName", "Name", "Cran Package Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("cran.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    cranPackageVersion(() -> new PackageSearchCriterion(PackageSearchType.cran, "cran.version",
            new AqlUISearchModel("cranPackageVersion", "Version", "Cran Package Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("cran.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    condaName(() -> new PackageSearchCriterion(PackageSearchType.conda, "conda.name",
            new AqlUISearchModel("condaName", "Name", "Conda Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conda.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    condaVersion(() -> new PackageSearchCriterion(PackageSearchType.conda, "conda.version",
            new AqlUISearchModel("condaVersion", "Version", "Conda Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conda.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    condaArch(() -> new PackageSearchCriterion(PackageSearchType.conda, "conda.arch",
            new AqlUISearchModel("condaArch", "Arch", "Conda Arch", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conda.arch",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    condaPlatform(() -> new PackageSearchCriterion(PackageSearchType.conda, "conda.platform",
            new AqlUISearchModel("condaPlatform", "Platform", "Conda Platform", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("conda.platform",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    debianName(() -> new PackageSearchCriterion(PackageSearchType.debian, "deb.name",
            new AqlUISearchModel("debianName", "Name", "Debian Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("deb.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),


    debianVersion(() -> new PackageSearchCriterion(PackageSearchType.debian, "deb.version",
            new AqlUISearchModel("debianVersion", "Version", "Debian Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("deb.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    debianPriority(() -> new PackageSearchCriterion(PackageSearchType.debian, "deb.priority",
            new AqlUISearchModel("debianPriority", "Priority", "Debian Priority", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("deb.priority",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    debianMaintainer(() -> new PackageSearchCriterion(PackageSearchType.debian, "deb.maintainer",
            new AqlUISearchModel("debianMaintainer", "Maintainer", "Debian Maintainer", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("deb.maintainer",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    debianDistribution(() -> new PackageSearchCriterion(PackageSearchType.debian, DebianNaming.DISTRIBUTION_PROP,
            new AqlUISearchModel("debianDistribution", "Distribution", "Debian Distribution", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy(DebianNaming.DISTRIBUTION_PROP,
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    debianComponent(() -> new PackageSearchCriterion(PackageSearchType.debian, DebianNaming.COMPONENT_PROP,
            new AqlUISearchModel("debianComponent", "Component", "Debian Component", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy(DebianNaming.COMPONENT_PROP,
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    debianArchitecture(() -> new PackageSearchCriterion(PackageSearchType.debian, DebianNaming.ARCHITECTURE_PROP,
            new AqlUISearchModel("debianArchitecture", "Architecture", "Debian Architecture", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy(DebianNaming.ARCHITECTURE_PROP,
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    dockerV1Image(() -> new PackageSearchCriterion(PackageSearchType.dockerV1, "path",
            new AqlUISearchModel("dockerV1Image", "Image", "Docker V1 Image", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.matches}),
            new AqlUIDockerV1ImageSearchStrategy(AqlPhysicalFieldEnum.itemPath,
                    new AqlDomainEnum[]{AqlDomainEnum.items}),
            new AqlUISearchDockerV1ResultManipulator())),

    dockerV1Tag(() -> new PackageSearchCriterion(PackageSearchType.dockerV1, "docker.tag.name",
            new AqlUISearchModel("dockerV1Tag", "Tag", "Docker V1 Tag", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("docker.tag.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    dockerV2Image(() -> new PackageSearchCriterion(PackageSearchType.dockerV2, "docker.repoName",
            new AqlUISearchModel("dockerV2Image", "Image", "Docker V2 Image", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIDockerV2ImageSearchStrategy("docker.repoName",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    dockerV2TagPath(() -> new PackageSearchCriterion(PackageSearchType.dockerV2, "docker.tag.path",
            new AqlUISearchModel("dockerV2TagPath", "TagPath", "Docker V2 Tag", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIDockerV2TagPathSearchStrategy(AqlPhysicalFieldEnum.itemPath,
                    new AqlDomainEnum[]{AqlDomainEnum.items}),
            new AqlUISearchDummyResultManipulator())),

    dockerV2Tag(() -> new PackageSearchCriterion(PackageSearchType.dockerV2, "docker.manifest",
            new AqlUISearchModel("dockerV2Tag", "Tag", "Docker V2 Tag", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIDockerV2ImageSearchStrategy("docker.manifest",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    //IMPORTANT: remove after migration-enforcing major --> don't remove the logic that puts the prop from docker without replacing this!
    dockerV2ImageDigest(() -> new PackageSearchCriterion(PackageSearchType.dockerV2, "sha256",
            new AqlUISearchModel("dockerV2ImageDigest", "Image Digest", "Docker V2 Image Digest", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIDockerV2ImageDigestSearchStrategy("sha256",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    gemName(() -> new PackageSearchCriterion(PackageSearchType.gems, "gem.name",
            new AqlUISearchModel("gemName", "Name", "Gem Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("gem.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    gemVersion(() -> new PackageSearchCriterion(PackageSearchType.gems, "gem.version",
            new AqlUISearchModel("gemVersion", "Version", "Gem Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("gem.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    chartName(() -> new PackageSearchCriterion(PackageSearchType.helm, "chart.name",
            new AqlUISearchModel("chartName", "Name", "Helm Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("chart.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    chartVersion(() -> new PackageSearchCriterion(PackageSearchType.helm, "chart.version",
            new AqlUISearchModel("chartVersion", "Version", "Helm Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("chart.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    chartAppVersion(() -> new PackageSearchCriterion(PackageSearchType.helm, "chart.appVersion",
            new AqlUISearchModel("chartAppVersion", "App Version", "Helm App Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("chart.appVersion",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    goName(() -> new PackageSearchCriterion(PackageSearchType.go, "go.name",
            new AqlUISearchModel("goName", "Name", "Go Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("go.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    goVersion(() -> new PackageSearchCriterion(PackageSearchType.go, "go.version",
            new AqlUISearchModel("goVersion", "Version", "Go Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("go.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    nugetPackageId(() -> new PackageSearchCriterion(PackageSearchType.nuget, "nuget.id",
            new AqlUISearchModel("nugetPackageId", "ID", "NuGet Package ID", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("nuget.id",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    nugetVersion(() -> new PackageSearchCriterion(PackageSearchType.nuget, "nuget.version",
            new AqlUISearchModel("nugetVersion", "Version", "NuGet Package Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("nuget.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

/*    nugetTags(PackageSearchType.nuget, "nuget.tags",
            new AqlUISearchModel("nugetTags", "Tags", "NuGet Tags",
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("nuget.tags",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator()),

    nugetDigest(PackageSearchType.nuget, "nuget.digest",
            new AqlUISearchModel("nugetDigest", "Digest", "NuGet Digest",
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("nuget.digest",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator()),*/


    npmName(() -> new PackageSearchCriterion(PackageSearchType.npm, "npm.name",
            new AqlUISearchModel("npmName", "Name", "Npm Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUINpmNameSearchStrategy("npm.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    npmVersion(() -> new PackageSearchCriterion(PackageSearchType.npm, "npm.version",
            new AqlUISearchModel("npmVersion", "Version", "Npm Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("npm.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    npmScope(() -> new PackageSearchCriterion(PackageSearchType.npm, "npm.name",
            new AqlUISearchModel("npmScope", "Scope", "Npm Scope", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUINpmScopeSearchStrategy("npm.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchNpmResultManipulator())),

    npmKeywords(() -> new PackageSearchCriterion(PackageSearchType.npm, "npm.keywords",
            new AqlUISearchModel("npmKeywords", "Keywords", "Npm Keywords", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUINpmKeywordsSearchStrategy("npm.keywords",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchNpmResultManipulator())),

    npmChecksum(() -> new PackageSearchCriterion(PackageSearchType.npm, "npm.checksum",
            new AqlUISearchModel("npmChecksum", "Checksum", "Npm Checksum", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUINpmChecksumSearchStrategy(itemActualSha1, new AqlDomainEnum[]{AqlDomainEnum.items}),
            new AqlUISearchNpmResultManipulator())),

    opkgName(() -> new PackageSearchCriterion(PackageSearchType.opkg, "opkg.name",
            new AqlUISearchModel("opkgName", "Name", "Opkg Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("opkg.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    opkgVersion(() -> new PackageSearchCriterion(PackageSearchType.opkg, "opkg.version",
            new AqlUISearchModel("opkgVersion", "Version", "Opkg Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("opkg.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    opkgArchitecture(() -> new PackageSearchCriterion(PackageSearchType.opkg, "opkg.architecture",
            new AqlUISearchModel("opkgArchitecture", "Architecture", "Opkg Architecture", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("opkg.architecture",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    opkgPriority(() -> new PackageSearchCriterion(PackageSearchType.opkg, "opkg.priority",
            new AqlUISearchModel("opkgPriority", "Priority", "Opkg Priority", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("opkg.priority",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    opkgMaintainer(() -> new PackageSearchCriterion(PackageSearchType.opkg, "opkg.maintainer",
            new AqlUISearchModel("opkgMaintainer", "Maintainer", "Opkg Maintainer", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("opkg.maintainer",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    pypiName(() -> new PackageSearchCriterion(PackageSearchType.pypi, "pypi.name",
            new AqlUISearchModel("pypiName", "Name", "PyPi Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("pypi.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    pypiVersion(() -> new PackageSearchCriterion(PackageSearchType.pypi, "pypi.version",
            new AqlUISearchModel("pypiVersion", "Version", "Pypi Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("pypi.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    puppetName(() -> new PackageSearchCriterion(PackageSearchType.puppet, "puppet.name",
            new AqlUISearchModel("puppetName", "Name", "Puppet Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("puppet.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    puppetVersion(() -> new PackageSearchCriterion(PackageSearchType.puppet, "puppet.version",
            new AqlUISearchModel("puppetVersion", "Version", "Puppet Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("puppet.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    rpmName(() -> new PackageSearchCriterion(PackageSearchType.rpm, "rpm.metadata.name",
            new AqlUISearchModel("rpmName", "Name", "RPM Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("rpm.metadata.name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    rpmVersion(() -> new PackageSearchCriterion(PackageSearchType.rpm, "rpm.metadata.version",
            new AqlUISearchModel("rpmVersion", "Version", "RPM Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("rpm.metadata.version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    rpmArchitecture(() -> new PackageSearchCriterion(PackageSearchType.rpm, "rpm.metadata.arch",
            new AqlUISearchModel("rpmArchitecture", "Architecture", "RPM Architecture", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("rpm.metadata.arch",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

   /* rpmRelease(PackageSearchType.rpm, "rpm.metadata.release",
            new AqlUISearchModel("rpmRelease", "Release", "RPM Release",
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("rpm.metadata.release",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator()),*/

    vagrantName(() -> new PackageSearchCriterion(PackageSearchType.vagrant, "box_name",
            new AqlUISearchModel("vagrantName", "Box Name", "Vagrant Box Name", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("box_name",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    vagrantVersion(() -> new PackageSearchCriterion(PackageSearchType.vagrant, "box_version",
            new AqlUISearchModel("vagrantVersion", "Box Version", "Vagrant Box Version", true,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("box_version",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator())),

    vagrantProvider(() -> new PackageSearchCriterion(PackageSearchType.vagrant, "box_provider",
            new AqlUISearchModel("vagrantProvider", "Box Provider", "Vagrant Box Provider", false,
                    new AqlComparatorEnum[]{AqlComparatorEnum.equals, AqlComparatorEnum.matches}),
            new AqlUIPropertySearchStrategy("box_provider",
                    new AqlDomainEnum[]{AqlDomainEnum.items, AqlDomainEnum.properties}),
            new AqlUISearchDummyResultManipulator()));

    private final PackageSearchCriteriaFactory criteriaFactory;

    public PackageSearchCriterion getCriterion() {
        return criteriaFactory.create();
    }

    PackageSearchCriteria(PackageSearchCriteriaFactory ruleFactory) {
        this.criteriaFactory = ruleFactory;
    }

    public static class PackageSearchCriterion {

        PackageSearchType type;
        String aqlName;
        AqlUISearchModel model;
        AqlUISearchStrategy strategy;
        AqlUISearchResultManipulator resultManipulator;

        PackageSearchCriterion(PackageSearchType type, String aqlName, AqlUISearchModel model,
                AqlUISearchStrategy strategy, AqlUISearchResultManipulator resultManipulator) {
            this.type = type;
            this.aqlName = aqlName;
            this.model = model;
            this.strategy = strategy;
            this.resultManipulator = resultManipulator;
        }

        public PackageSearchType getType() {
            return type;
        }

        public AqlUISearchModel getModel() {
            return model;
        }

        public AqlUISearchStrategy getStrategy() {
            return strategy;
        }

        public AqlUISearchResultManipulator getResultManipulator() {
            return resultManipulator;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PackageSearchCriterion packageSearchCriterion = (PackageSearchCriterion) o;
            if (type != null ? !type.equals(packageSearchCriterion.type) : packageSearchCriterion.type != null) {
                return false;
            }

            if (aqlName != null ? !aqlName.equals(packageSearchCriterion.aqlName) :
                    packageSearchCriterion.aqlName != null) {
                return false;
            }
            if (model != null ? !model.equals(packageSearchCriterion.model) : packageSearchCriterion.model != null) {
                return false;
            }
            if (strategy != null ? !strategy.equals(packageSearchCriterion.strategy) :
                    packageSearchCriterion.strategy != null) {
                return false;
            }
            return resultManipulator != null ? resultManipulator.equals(packageSearchCriterion.resultManipulator) :
                    packageSearchCriterion.resultManipulator == null;
        }
    }

    public static AqlUISearchStrategy getStrategyByFieldId(String id) {
        return valueOf(id).getCriterion().strategy;
    }

    public static AqlUISearchStrategy getStrategyByTypeAndElementType(PackageSearchType type, String elementType) {
        for (PackageSearchCriteria packageSearchCriteria : PackageSearchCriteria.values()) {
            PackageSearchCriterion criterion = packageSearchCriteria.getCriterion();
            if (criterion.type.equals(type) && criterion.model.getDisplayName().equals(elementType)) {
                return criterion.strategy;
            }
        }
        return null;
    }

    /**
     * Returns the criteria that matches the AQL field name or the property key that {@param aqlName} references
     */
    public static PackageSearchCriterion getCriterionByAqlFieldOrPropName(String aqlName) {
        return Stream.of(values())
                .map(PackageSearchCriteria::getCriterion)
                .filter(criterion -> criterion.aqlName.equalsIgnoreCase(aqlName))
                .findAny()
                .orElse(null);
    }

    public static List<PackageSearchCriterion> getCriteriaByPackage(String packageType) {
        return Stream.of(values())
                .map(PackageSearchCriteria::getCriterion)
                .filter(searchCriterion -> searchCriterion.type.equals(PackageSearchType.getById(packageType)))
                .collect(Collectors.toList());
    }

    public static List<PackageSearchCriterion> getCriteriaByPackage(PackageSearchType packageType) {
        return Stream.of(values())
                .map(PackageSearchCriteria::getCriterion)
                .filter(searchCriterion -> searchCriterion.type.equals(packageType))
                .collect(Collectors.toList());
    }

    public static List<AqlUISearchResultManipulator> getResultManipulatorsByPackage(PackageSearchType packageType) {
        return getCriteriaByPackage(packageType)
                .stream()
                .map(PackageSearchCriterion::getResultManipulator)
                .collect(Collectors.toList());
    }

    public static PackageSearchType getPackageTypeByFieldId(String fieldId) {
        try {
            return valueOf(fieldId).getCriterion().type;
        } catch (IllegalArgumentException iae) {
            //no such fieldId
        }
        return null;
    }

    public static List<AqlUISearchStrategy> getStartegiesByPackageSearchType(PackageSearchType type) {
        return getCriteriaByPackage(type).stream()
                .map(PackageSearchCriterion::getStrategy)
                .collect(Collectors.toList());
    }

    //Keep packages ordered alphabetically please!
    public enum PackageSearchType {
        bower(RepoType.Bower, true, "bower", true, DUMMY_MERGER),
        chef(RepoType.Chef, true, "chef", true, DUMMY_MERGER),
        cocoapods(RepoType.CocoaPods, true, "cocoapods", true, DUMMY_MERGER),
        composer(RepoType.Composer, true, "composer", true, DUMMY_MERGER),
        //downloadEnabled is false - no sense in downloading a conan file
        conan(RepoType.Conan, false, "conan", false, new ConanPackageSearchResultMerger()),
        cran(RepoType.CRAN, false, "cran", true, DUMMY_MERGER),
        conda(RepoType.Conda, false, "conda", true, DUMMY_MERGER),
        debian(RepoType.Debian, false, "deb", true, DUMMY_MERGER),
        //downloadEnabled is false - no sense in downloading a manifest.json or tag.json for docker images
        dockerV1(RepoType.Docker, true, "docker", false, DUMMY_MERGER),
        dockerV2(RepoType.Docker, true, "docker", false, DUMMY_MERGER),
        gavc(RepoType.Maven, true, "pom", true, DUMMY_MERGER),
        gems(RepoType.Gems, false, "ruby-gems", true, DUMMY_MERGER),
        go(RepoType.Go, false, "golang", true, DUMMY_MERGER),
        helm(RepoType.Helm, false, "helm", true, DUMMY_MERGER),
        nuget(RepoType.NuGet, true, "nuget", true, DUMMY_MERGER),
        npm(RepoType.Npm, true, "npm", true, DUMMY_MERGER),
        opkg(RepoType.Opkg, false, "opkg", true, DUMMY_MERGER),
        pypi(RepoType.Pypi, false, "pypi", true, DUMMY_MERGER),
        puppet(RepoType.Puppet, false, "puppet", true, DUMMY_MERGER),
        rpm(RepoType.YUM, true, "rpm", true, DUMMY_MERGER),
        vagrant(RepoType.Vagrant, false, "vagrant", true, DUMMY_MERGER);
        /*, all(""),*/ /*gitlfs(RepoType.GitLfs, false, "git-lfs"),*/

        final boolean remoteCachesProps;
        final RepoType repoType;
        final String icon;
        final boolean downloadEnabled;
        final PackageSearchResultMerger resultMerger;

        PackageSearchType(RepoType repoType, boolean remoteCachesProps, String icon, boolean downloadEnabled,
                PackageSearchResultMerger resultMerger) {
            this.repoType = repoType;
            this.remoteCachesProps = remoteCachesProps;
            this.icon = icon;
            this.downloadEnabled = downloadEnabled;
            this.resultMerger = resultMerger;
        }

        public static PackageSearchType getById(String id) {
            for (PackageSearchType type : values()) {
                if (type.name().equalsIgnoreCase(id)) {
                    return type;
                }
            }
            throw new UnsupportedOperationException("Unsupported package");
        }

        public String getDisplayName() {
            if (this.equals(dockerV1)) {
                return "Docker V1";
            } else if (this.equals(dockerV2)) {
                return "Docker V2";
            } else if (this.equals(rpm)) {
                return "RPM";
            } else if (this.equals(gavc)) {
                return "GAVC";
            } else if (this.equals(pypi)) {
                return "PyPI";
            }
            return repoType.name();
        }

        public boolean isRemoteCachesProps() {
            return remoteCachesProps;
        }

        public boolean isDownloadEnabled() {
            return downloadEnabled;
        }

        public String getId() {
            return this.name();
        }

        public RepoType getRepoType() {
            return repoType;
        }

        public String getIcon() {
            return icon;
        }

        public PackageSearchResultMerger getResultMerger() {
            return resultMerger;
        }
    }
}
