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

package org.artifactory.api.bintray.distribution.resolver;

import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.bintray.distribution.rule.DistributionRuleToken;
import org.artifactory.api.bintray.distribution.rule.DistributionRuleTokens;
import org.artifactory.common.StatusEntry;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.distribution.rule.DefaultDistributionRules;
import org.artifactory.descriptor.repo.distribution.rule.DistributionCoordinates;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.util.distribution.DistributionConstants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.artifactory.bintray.distribution.util.DistributionUtils.createTimingOutMatcher;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Dan Feldman
 */
@Test
public class DistributionCoordinatesResolverTest extends ArtifactoryHomeBoundTest {

    private Properties allProps;
    private RepoPath mavenPath;
    private RepoPath dummyPath;

    DistributionReporter dummyStatus;

    @BeforeClass
    public void setUp() {
        dummyStatus = new DistributionReporter(true);
        allProps = (Properties) InfoFactoryHolder.get().createProperties();

        mavenPath = RepoPathFactory.create("maven-repo", "org/artifactory/pro/jfrog-artifactory-oss/4.x-SNAPSHOT/jfrog-artifactory-oss-4.x-20160519.161218-3290.jar");
        dummyPath = RepoPathFactory.create("dummy-repo", "path/to/file.ext");

        //Properties
        allProps.put("bower.name", "angular-ui-codemirror");
        allProps.put("bower.version", "0.2.3");
        allProps.put("pods.name", "KosherCocoa");
        allProps.put("pods.version", "3.0.4");
        allProps.put("deb.name", "plankDb");
        allProps.put("deb.version", "4.2012");
        allProps.put("deb.distribution", "wheezy");
        allProps.put("deb.component", "main");
        allProps.put("deb.architecture", "amd64");
        allProps.put("docker.repoName", "busybox");
        allProps.put("docker.manifest", "1.0");
        allProps.put("npm.name", "uglify-js");
        allProps.put("npm.version", "2.6.2");
        allProps.put("nuget.id", "WebGrease");
        allProps.put("nuget.version", "1.5.2");
        allProps.put("opkg.name", "alpine");
        allProps.put("opkg.version", "2.20-2");
        allProps.put("opkg.architecture", "i386");
        allProps.put("rpm.metadata.name", "audacity");
        allProps.put("rpm.metadata.version", "1.3.12");
        allProps.put("rpm.metadata.architecture", "x86_64");
        allProps.put("box_name", "trusty-server");
        allProps.put("box_version", "5.5");
        allProps.put("box_provider", "ProviderDude");

        //Layout
        allProps.put("${org}", "org.artifactory.oss");
        allProps.put("${orgPath}", "org.artifactory.oss");
        allProps.put("${module}", "jfrog-artifactory-oss");
        allProps.put("${baseRev}", "4.x");
        allProps.put("${folderItegRev}", "SNAPSHOT");
        allProps.put("${fileItegRev}", "20160519.161218-3290");
        allProps.put("${classifier}", "sources");
        allProps.put("${type}", "xmls");
        allProps.put("${ext}", "jar");
        allProps.put("${scalaVersion}", "123.0");
        allProps.put("${sbtVersion}", "456.0");

        allProps.put(DistributionConstants.PRODUCT_NAME_DUMMY_PROP, "product!");
    }

    /**
     * An artifact with existing {@link BintrayService#BINTRAY_REPO} etc. properties
     * override the resolved coordinates and the properties given for this path are ignored
     */
    public void existingPropertiesOverrideCoordinates() {
        //Bintray coordinates override props
        allProps.put(BintrayService.BINTRAY_REPO, "bt-repo");
        allProps.put(BintrayService.BINTRAY_PACKAGE, "bt-pkg");
        allProps.put(BintrayService.BINTRAY_VERSION, "bt-ver");
        allProps.put(BintrayService.BINTRAY_PATH, "bt-path");
        allProps.put(DistributionConstants.ARTIFACT_TYPE_OVERRIDE_PROP, "maven");

        DistributionRule rule = new DistributionRule("testerRule", RepoType.Generic, "", "",
                new DistributionCoordinates("generic", "myPkg", "myVer", "thePath"));
        DistributionCoordinatesResolver resolver = new DistributionCoordinatesResolver(rule, dummyPath, allProps, null);
        resolver.resolve(dummyStatus);
        assertThat(resolver.getRepo()).isEqualTo("bt-repo");
        assertThat(resolver.getPkg()).isEqualTo("bt-pkg");
        assertThat(resolver.getVersion()).isEqualTo("bt-ver");
        assertThat(resolver.getPath()).isEqualTo("bt-path");

        allProps.removeAll(BintrayService.BINTRAY_REPO);
        allProps.removeAll(BintrayService.BINTRAY_PACKAGE);
        allProps.removeAll(BintrayService.BINTRAY_VERSION);
        allProps.removeAll(BintrayService.BINTRAY_PATH);
        allProps.removeAll(DistributionConstants.ARTIFACT_TYPE_OVERRIDE_PROP);
    }

    /**
     * Resolves path regex replacement tokens correctly
     */
    public void resolvePathRegexTokens() {
        Properties props = (Properties) InfoFactoryHolder.get().createProperties();
        String pathNoExt = "org/artifactory/pro/jfrog-artifactory-oss/4.x-SNAPSHOT/jfrog-artifactory-oss-4.x-20160519.161218-3290";

        DistributionRule regexFilteredRule = new DistributionRule("regex-rule", RepoType.Generic, "(.*)-repo", "(.*).jar",
                new DistributionCoordinates("${repo:1}-new", "pkg", "ver", "${path:1}.new"));
        DistributionCoordinatesResolver resolver = new DistributionCoordinatesResolver(regexFilteredRule, dummyPath, props, null);

        //mock dist service operation adding capture groups
        Matcher repoMatcher = createTimingOutMatcher("test-repo", Pattern.compile("(.*)-repo"));
        Matcher pathMatcher = createTimingOutMatcher(pathNoExt + ".jar", Pattern.compile("(.*).jar"));
        repoMatcher.matches();
        pathMatcher.matches();
        resolver.addCaptureGroups(DistributionRuleFilterType.repo, repoMatcher, dummyStatus);
        resolver.addCaptureGroups(DistributionRuleFilterType.path, pathMatcher, dummyStatus);
        resolver.resolve(dummyStatus);

        assertThat(resolver.getRepo()).isEqualTo("test-new");
        assertThat(resolver.getPkg()).isEqualTo("pkg");
        assertThat(resolver.getVersion()).isEqualTo("ver");
        assertThat(resolver.getPath()).isEqualTo(pathNoExt + ".new");
    }

    public void failsOnRemainingTokens() {
        Properties props = (Properties) InfoFactoryHolder.get().createProperties();
        DistributionRule regexFilteredRule = new DistributionRule("regex-rule", RepoType.Generic, "(.*)-repo", "(.*).jar",
                new DistributionCoordinates("${repo:1}-new", "pkg", "ver", "${path:1}.new"));
        DistributionCoordinatesResolver resolver = new DistributionCoordinatesResolver(regexFilteredRule, dummyPath, props, null);
        resolver.resolve(dummyStatus);
        assertThat(dummyStatus.getPathErrors().get("dummy-repo/path/to/file.ext").stream()
                .map(StatusEntry::getMessage)
                .collect(Collectors.toList()))
                .containsOnly("Coordinate Field repo in rule 'regex-rule' contains tokens that were not matched: " +
                        "${repo:1}-new for artifact dummy-repo/path/to/file.ext, failing this rule.",
                        "Coordinate Field path in rule 'regex-rule' contains tokens that were not matched: " +
                                "${path:1}.new for artifact dummy-repo/path/to/file.ext, failing this rule.");
    }

    /**
     * Resolves layout based tokens correctly
     */
    public void resolveDefaultMavenRule() {
        DistributionRule mavenDefault = DefaultDistributionRules.getRuleByName("Maven");
        DistributionCoordinatesResolver resolver = new DistributionCoordinatesResolver(mavenDefault, mavenPath,
                allProps, RepoLayoutUtils.MAVEN_2_DEFAULT);
        resolver.resolve(dummyStatus);

        //Check token values were resolved to expected layout tokens
        assertThat(resolverTokenValues(resolver)).contains("org.artifactory.oss", "jfrog-artifactory-oss", "4.x",
                "SNAPSHOT", "20160519.161218-3290", "jar");

        //Check actual coordinates resolved ok
        assertThat(resolver.getRepo()).isEqualTo("maven");
        assertThat(resolver.getPkg()).isEqualTo("jfrog-artifactory-oss");
        assertThat(resolver.getVersion()).isEqualTo("4.x");
        assertThat(resolver.getPath()).isEqualTo(mavenPath.getPath());
    }

    /**
     * Resolves property based tokens correctly
     */
    public void resolveDefaultDebianProductRule() {
        DistributionRule defaultDebian = DefaultDistributionRules.getRuleByName("DebianProduct");
        DistributionCoordinatesResolver resolver = new DistributionCoordinatesResolver(defaultDebian, dummyPath, allProps, null);
        resolver.tokens.add(DistributionRuleTokens.getProductNameToken()); //The service has logic to add this - so just mock it here
        resolver.resolve(dummyStatus);

        assertThat(resolverTokenValues(resolver)).contains("plankDb", "4.2012", "wheezy", "main", "amd64");
        assertThat(resolver.getRepo()).isEqualTo("product!-deb");
        assertThat(resolver.getPkg()).isEqualTo("plankDb");
        assertThat(resolver.getVersion()).isEqualTo("4.2012");
        assertThat(resolver.getPath()).isEqualTo(dummyPath.getPath());
    }

    private List<String> resolverTokenValues(DistributionCoordinatesResolver resolver) {
        return resolver.tokens.stream()
                .map(DistributionRuleToken::getValue)
                .collect(Collectors.toList());
    }
}
