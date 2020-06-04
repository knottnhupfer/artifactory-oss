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

package org.artifactory.descriptor.repo.distribution.rule;

import org.artifactory.descriptor.repo.RepoType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.artifactory.util.distribution.DistributionConstants.*;

/**
 * Forgive me for going over the right margin, this was getting unreadable.
 *
 * @author Dan Feldman
 */
public enum DefaultDistributionRules {

    Bower(() -> new DistributionRule("Bower-default", RepoType.Bower, "", "",
            new DistributionCoordinates(DEFAULT_GENERIC_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    BowerProduct(() -> new DistributionRule("Bower-product-default", RepoType.Bower, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_GENERIC_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    CocoaPods(() -> new DistributionRule("CocoaPods-default", RepoType.CocoaPods, "", "",
            new DistributionCoordinates(DEFAULT_GENERIC_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    CocoaPodsProduct(() -> new DistributionRule("CocoaPods-product-default", RepoType.CocoaPods, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_GENERIC_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    Conan(() -> new DistributionRule("Conan-default", RepoType.Conan, "", "",
            new DistributionCoordinates(DEFAULT_CONAN_REPO_NAME, MODULE_TOKEN + ":" + ORG_TOKEN, BASE_REV_TOKEN + ":" + CHANNEL_TOKEN, PATH_TOKEN))),

    ConanProduct(() -> new DistributionRule("Conan-product-default", RepoType.Conan, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_CONAN_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    Debian(() -> new DistributionRule("Debian-default", RepoType.Debian, "", "",
            new DistributionCoordinates(DEFAULT_DEB_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    DebianProduct(() -> new DistributionRule("Debian-product-default", RepoType.Debian, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_DEB_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    Docker(() -> new DistributionRule("Docker-default", RepoType.Docker, "", "",
            new DistributionCoordinates(DEFAULT_DOCKER_REPO_NAME, DOCKER_IMAGE_TOKEN, DOCKER_TAG_TOKEN, PATH_TOKEN))),

    DockerProduct(() -> new DistributionRule("Docker-product-default", RepoType.Docker, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_DOCKER_REPO_NAME, DOCKER_IMAGE_TOKEN, DOCKER_TAG_TOKEN, PATH_TOKEN))),

    Gradle(() -> new DistributionRule("Gradle-default", RepoType.Gradle, "", "",
            new DistributionCoordinates(DEFAULT_MAVEN_REPO_NAME, MODULE_TOKEN, BASE_REV_TOKEN, PATH_TOKEN))),

    GradleProduct(() -> new DistributionRule("Gradle-product-default", RepoType.Gradle, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_MAVEN_REPO_NAME, MODULE_TOKEN, BASE_REV_TOKEN, PATH_TOKEN))),

    Ivy(() -> new DistributionRule("Ivy-default", RepoType.Ivy, "", "",
            new DistributionCoordinates(DEFAULT_MAVEN_REPO_NAME, MODULE_TOKEN, BASE_REV_TOKEN, PATH_TOKEN))),

    IvyProduct(() -> new DistributionRule("Ivy-product-default", RepoType.Ivy, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_MAVEN_REPO_NAME, MODULE_TOKEN, BASE_REV_TOKEN, PATH_TOKEN))),

    Maven(() -> new DistributionRule("Maven-default", RepoType.Maven, "", "",
            new DistributionCoordinates(DEFAULT_MAVEN_REPO_NAME, MODULE_TOKEN, BASE_REV_TOKEN, PATH_TOKEN))),

    MavenProduct(() -> new DistributionRule("Maven-product-default", RepoType.Maven, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_MAVEN_REPO_NAME, MODULE_TOKEN, BASE_REV_TOKEN, PATH_TOKEN))),

    Npm(() -> new DistributionRule("Npm-default", RepoType.Npm, "", "",
            new DistributionCoordinates(DEFAULT_GENERIC_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    NpmProduct(() -> new DistributionRule("Npm-product-default", RepoType.Npm, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_GENERIC_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    NuGet(() -> new DistributionRule("NuGet-default", RepoType.NuGet, "", "",
            new DistributionCoordinates(DEFAULT_NUGET_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    NuGetProduct(() -> new DistributionRule("NuGet-product-default", RepoType.NuGet, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_NUGET_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    Opkg(() -> new DistributionRule("Opkg-default", RepoType.Opkg, "", "",
            new DistributionCoordinates(DEFAULT_OPKG_REPO_NAME, PACKAGE_NAME_TOKEN + "-" + ARCHITECTURE_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    OpkgProduct(() -> new DistributionRule("Opkg-product-default", RepoType.Opkg, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_OPKG_REPO_NAME, PACKAGE_NAME_TOKEN + "-" + ARCHITECTURE_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    Rpm(() -> new DistributionRule("Rpm-default", RepoType.YUM, "", "",
            new DistributionCoordinates(DEFAULT_RPM_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    RpmProduct(() -> new DistributionRule("Rpm-product-default", RepoType.YUM, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_RPM_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    Sbt(() -> new DistributionRule("Sbt-default", RepoType.SBT, "", "",
            new DistributionCoordinates(DEFAULT_MAVEN_REPO_NAME, MODULE_TOKEN, BASE_REV_TOKEN, PATH_TOKEN))),

    SbtProduct(() -> new DistributionRule("Sbt-product-default", RepoType.SBT, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_MAVEN_REPO_NAME, MODULE_TOKEN, BASE_REV_TOKEN, PATH_TOKEN))),

    Vagrant(() -> new DistributionRule("Vagrant-default", RepoType.Vagrant, "", "",
            new DistributionCoordinates(DEFAULT_VAGRANT_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN))),

    VagrantProduct(() -> new DistributionRule("Vagrant-product-default", RepoType.Vagrant, "", "",
            new DistributionCoordinates(PRODUCT_NAME_TOKEN + "-" + DEFAULT_VAGRANT_REPO_NAME, PACKAGE_NAME_TOKEN, PACKAGE_VERSION_TOKEN, PATH_TOKEN)));

    private final DistributionRuleFactory ruleFactory;

    DistributionRule getRule() {
        return ruleFactory.create();
    }

    DefaultDistributionRules(DistributionRuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    public static List<DistributionRule> getDefaultRules() {
        return collectRules(defaultRule -> !defaultRule.name().endsWith("Product"));
    }

    public static List<DistributionRule> getDefaultProductRules() {
        return collectRules(defaultRule -> defaultRule.name().endsWith("Product"));
    }

    public static DistributionRule getRuleByName(String ruleName) {
        return collectRules(defaultRule -> defaultRule.name().equalsIgnoreCase(ruleName))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public static List<DistributionRule> collectRules(Predicate<DefaultDistributionRules> predicate) {
        return Arrays.stream(values())
                .filter(predicate)
                .map(DefaultDistributionRules::getRule)
                .collect(Collectors.toList());
    }
}
