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

package org.artifactory.api.bintray.distribution.rule;

import com.google.common.collect.Lists;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.DebianNaming;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.util.distribution.DistributionConstants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.util.distribution.DistributionConstants.*;

/**
 * Tokens that are available to each rule based on its {@link RepoType}
 *
 * @author Dan Feldman
 */
public enum DistributionRuleTokens {

    BOWER_NAME(RepoType.Bower, () -> new DistributionRulePropertyToken(Keys.PACKAGE_NAME.key, "bower.name")),
    BOWER_VERSION(RepoType.Bower, () -> new DistributionRulePropertyToken(Keys.PACKAGE_VERSION.key, "bower.version")),
    COCOAPODS_NAME(RepoType.CocoaPods, () -> new DistributionRulePropertyToken(Keys.PACKAGE_NAME.key, "pods.name")),
    COCOAPODS_VERSION(RepoType.CocoaPods, () -> new DistributionRulePropertyToken(Keys.PACKAGE_VERSION.key, "pods.version")),
    CONAN_USER(RepoType.Conan, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_ORG.key)),
    CONAN_NAME(RepoType.Conan, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_MODULE.key)),
    CONAN_VERSION(RepoType.Conan, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_BASE_REVISION.key)),
    CONAN_CHANNEL(RepoType.Conan, () -> new DistributionRuleLayoutToken(Keys.CONAN_CHANNEL.key)),
    DEBIAN_NAME(RepoType.Debian, () -> new DistributionRulePropertyToken(Keys.PACKAGE_NAME.key, DebianNaming.DEBIAN_NAME)),
    DEBIAN_VERSION(RepoType.Debian, () -> new DistributionRulePropertyToken(Keys.PACKAGE_VERSION.key, DebianNaming.DEBIAN_VERSION)),
    DEBIAN_DISTRIBUTION(RepoType.Debian, () -> new DistributionRulePropertyToken(Keys.DEB_DISTRIBUTION.key, DebianNaming.DISTRIBUTION_PROP)),
    DEBIAN_COMPONENT(RepoType.Debian, () -> new DistributionRulePropertyToken(Keys.DEB_COMPONENT.key, DebianNaming.COMPONENT_PROP)),
    DEBIAN_ARCHITECTURE(RepoType.Debian, () -> new DistributionRulePropertyToken(Keys.ARCHITECTURE.key, DebianNaming.ARCHITECTURE_PROP)),
    DOCKER_IMAGE(RepoType.Docker, () -> new DistributionRulePropertyToken(Keys.DOCKER_IMAGE.key, "docker.repoName")),
    DOCKER_TAG(RepoType.Docker, () -> new DistributionRulePropertyToken(Keys.DOCKER_TAG.key, "docker.manifest")),
    GRADLE_ORG(RepoType.Gradle, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_ORG.key)),
    GRADLE_MODULE(RepoType.Gradle, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_MODULE.key)),
    GRADLE_BASE_REV(RepoType.Gradle, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_BASE_REVISION.key)),
    GRADLE_CLASSIFIER(RepoType.Gradle, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_CLASSIFIER.key)),
    GRADLE_EXTENSION(RepoType.Gradle, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_EXTENSION.key)),
    IVY_ORG(RepoType.Ivy, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_ORG.key)),
    IVY_MODULE(RepoType.Ivy, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_MODULE.key)),
    IVY_BASE_REVISION(RepoType.Ivy, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_BASE_REVISION.key)),
    IVY_TYPE(RepoType.Ivy, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_TYPE.key)),
    IVY_CLASSIFIER(RepoType.Ivy, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_CLASSIFIER.key)),
    IVY_EXTENSION(RepoType.Ivy, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_EXTENSION.key)),
    MAVEN_ORG(RepoType.Maven, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_ORG_PATH.key)),
    MAVEN_MODULE(RepoType.Maven, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_MODULE.key)),
    MAVEN_BASE_REVISION(RepoType.Maven, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_BASE_REVISION.key)),
    MAVEN_CLASSIFIER(RepoType.Maven, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_CLASSIFIER.key)),
    MAVEN_EXTENSION(RepoType.Maven, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_EXTENSION.key)),
    NPM_NAME(RepoType.Npm, () -> new DistributionRulePropertyToken(Keys.PACKAGE_NAME.key, "npm.name")),
    NPM_VERSION(RepoType.Npm, () -> new DistributionRulePropertyToken(Keys.PACKAGE_VERSION.key, "npm.version")),
    NUGET_NAME(RepoType.NuGet, () -> new DistributionRulePropertyToken(Keys.PACKAGE_NAME.key, "nuget.id")),
    NUGET_VERSION(RepoType.NuGet, () -> new DistributionRulePropertyToken(Keys.PACKAGE_VERSION.key, "nuget.version")),
    OPKG_NAME(RepoType.Opkg, () -> new DistributionRulePropertyToken(Keys.PACKAGE_NAME.key, "opkg.name")),
    OPKG_VERSION(RepoType.Opkg, () -> new DistributionRulePropertyToken(Keys.PACKAGE_VERSION.key, "opkg.version")),
    OPKG_ARCHITECTURE(RepoType.Opkg, () -> new DistributionRulePropertyToken(Keys.ARCHITECTURE.key, "opkg.architecture")),
    RPM_NAME(RepoType.YUM, () -> new DistributionRulePropertyToken(Keys.PACKAGE_NAME.key, "rpm.metadata.name")),
    RPM_VERSION(RepoType.YUM, () -> new DistributionRulePropertyToken(Keys.PACKAGE_VERSION.key, "rpm.metadata.version")),
    RPM_ARCHITECTURE(RepoType.YUM, () -> new DistributionRulePropertyToken(Keys.ARCHITECTURE.key, "rpm.metadata.arch")),
    SBT_ORG(RepoType.SBT, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_ORG.key)),
    SBT_MODULE(RepoType.SBT, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_MODULE.key)),
    SBT_SCALA_VERSION(RepoType.SBT, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_SCALA_VERSION.key)),
    SBT_SBT_VERSION(RepoType.SBT, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_SBT_VERSION.key)),
    SBT_BASE_REVISION(RepoType.SBT, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_BASE_REVISION.key)),
    SBT_TYPE(RepoType.SBT, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_TYPE.key)),
    SBT_CLASSIFIER(RepoType.SBT, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_CLASSIFIER.key)),
    SBT_EXTENSION(RepoType.SBT, () -> new DistributionRuleLayoutToken(Keys.LAYOUT_EXTENSION.key)),
    VAGRANT_NAME(RepoType.Vagrant, () -> new DistributionRulePropertyToken(Keys.PACKAGE_NAME.key, "box_name")),
    VAGRANT_VERSION(RepoType.Vagrant, () -> new DistributionRulePropertyToken(Keys.PACKAGE_VERSION.key, "box_version")),
    VAGRANT_PROVIDER(RepoType.Vagrant, () -> new DistributionRulePropertyToken(Keys.VAGRANT_PROVIDER.key, "box_provider"));

    final RepoType type;
    final DistributionRuleTokenFactory tokenFactory;

    DistributionRuleTokens(RepoType type, DistributionRuleTokenFactory tokenFactory) {
        this.type = type;
        this.tokenFactory = tokenFactory;
    }

    public DistributionRuleToken getToken() {
        return tokenFactory.create();
    }

    DistributionRuleTokenFactory getTokenFactory() {
        return tokenFactory;
    }

    public static final DistributionRuleTokenFactory PRODUCT_NAME_TOKEN_FACTORY = () -> new DistributionRulePropertyToken(PRODUCT_NAME_TOKEN, PRODUCT_NAME_DUMMY_PROP);

    public static DistributionRuleToken getProductNameToken() {
        return PRODUCT_NAME_TOKEN_FACTORY.create();
    }

    /**
     * @return all tokens available to the given {@param type}, with the default path and product name tokens.
     */
    public static Set<DistributionRuleToken> tokensByType(RepoType type, @Nullable RepoLayout layout) {
        Set<DistributionRuleToken> allTokens = new HashSet<>();
        if (type == null) {
            return allTokens;
        }
        allTokens.add(new DistributionRulePathToken(PATH_TOKEN));
        allTokens.addAll(Stream.of(Types.values())
                .filter(types -> type.equals(types.repoType))
                .findAny()
                .orElse(Types.DUMMY)
                .tokenFactories.stream()
                .map(DistributionRuleTokenFactory::create)
                .collect(Collectors.toSet()));
        if ((type.isMavenGroup() || type.equals(RepoType.Generic)) && layout != null) {
            //Only add layout tokens if rule type is Maven-y or for generic rules.
            allTokens.addAll(getResolverCompatibleLayoutTokens(layout));
        }
        return allTokens;
    }

    private static List<DistributionRuleTokenFactory> tokensByPackageType(RepoType type) {
        return Stream.of(values())
                .filter(tokenKey -> tokenKey.type.equals(type))
                .map(DistributionRuleTokens::getTokenFactory)
                .collect(Collectors.toList());
    }

    /**
     * Performance wise, this is not the best place for getting layout tokens as we already cache them in
     * DistributionService::addLayoutTokens but it makes it quite unreadable to have actual tokens added in the
     * same place that also resolves them.
     * If this is identified as a performance bottleneck return this logic to where it was in commit d806bf3
     */
    private static List<DistributionRuleLayoutToken> getResolverCompatibleLayoutTokens(RepoLayout layout) {
        return RepoLayoutUtils.getLayoutTokens(layout).stream()
                .map(DistributionConstants::stripTokenBrackets)
                .map(DistributionConstants::wrapToken)
                .map(DistributionRuleLayoutToken::new)
                .collect(Collectors.toList());
    }

    public enum Types {
        BOWER(RepoType.Bower, tokensByPackageType(RepoType.Bower)),
        COCOAPODS(RepoType.CocoaPods, tokensByPackageType(RepoType.CocoaPods)),
        CONAN(RepoType.Conan, tokensByPackageType(RepoType.Conan)),
        DEBIAN(RepoType.Debian, tokensByPackageType(RepoType.Debian)),
        DOCKER(RepoType.Docker, tokensByPackageType(RepoType.Docker)),
        GRADLE(RepoType.Gradle, tokensByPackageType(RepoType.Gradle)),
        IVY(RepoType.Ivy, tokensByPackageType(RepoType.Ivy)),
        MAVEN(RepoType.Maven, tokensByPackageType(RepoType.Maven)),
        NPM(RepoType.Npm, tokensByPackageType(RepoType.Npm)),
        NUGET(RepoType.NuGet, tokensByPackageType(RepoType.NuGet)),
        OPKG(RepoType.Opkg, tokensByPackageType(RepoType.Opkg)),
        RPM(RepoType.YUM, tokensByPackageType(RepoType.YUM)),
        SBT(RepoType.SBT, tokensByPackageType(RepoType.SBT)),
        VAGRANT(RepoType.Vagrant, tokensByPackageType(RepoType.Vagrant)),
        DUMMY(null, new ArrayList<>());

        final RepoType repoType;
        final List<DistributionRuleTokenFactory> tokenFactories;

        Types(RepoType repoType, List<DistributionRuleTokenFactory> tokenFactories) {
            this.repoType = repoType;
            this.tokenFactories = Collections.unmodifiableList(Lists.newArrayList(tokenFactories));
        }
    }

    public enum Keys {
        PACKAGE_NAME(PACKAGE_NAME_TOKEN),
        PACKAGE_VERSION(PACKAGE_VERSION_TOKEN),
        DEB_DISTRIBUTION(wrapToken("distribution")),
        DEB_COMPONENT(wrapToken("component")),
        CONAN_CHANNEL(wrapToken("channel")),
        ARCHITECTURE(ARCHITECTURE_TOKEN),
        VAGRANT_PROVIDER(wrapToken("boxProvider")),
        LAYOUT_ORG(wrapToken("org")),
        LAYOUT_ORG_PATH(wrapToken("orgPath")),
        LAYOUT_MODULE(MODULE_TOKEN),
        LAYOUT_BASE_REVISION(BASE_REV_TOKEN),
        LAYOUT_FOLDER_REVISION(wrapToken("folderItegRev")),
        LAYOUT_FILE_REVISION(wrapToken("fileItegRev")),
        LAYOUT_CLASSIFIER(wrapToken("classifier")),
        LAYOUT_TYPE(wrapToken("type")),
        LAYOUT_EXTENSION(wrapToken("ext")),
        LAYOUT_SCALA_VERSION(wrapToken("scalaVersion")),
        LAYOUT_SBT_VERSION(wrapToken("sbtVersion")),
        SCOPE(wrapToken("scope")),
        DOCKER_IMAGE(DOCKER_IMAGE_TOKEN),
        DOCKER_TAG(DOCKER_TAG_TOKEN);

        public final String key;

        Keys(String key) {
            this.key = key;
        }
    }
}
