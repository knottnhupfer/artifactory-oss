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

package org.artifactory.addon;

import static org.artifactory.addon.AddonTypeConstants.*;

/**
 * @author Yoav Aharoni
 */
public enum AddonType {
    //
    //PLEASE MAKE SURE THESE DETAILS ARE CONSISTENT WITH THE ONES IN THE PROPERTY FILES
    //
    AOL("aol", "Artifactory Online", -1, new String[]{ALL}, LIC_PRO, "aol"),
    BUILD("build", "Build Integration", 100, new String[]{ALL, FEATURES}, LIC_PRO, "Build+Integration"),
    XRAY("xray", "JFrog Xray", 500, new String[]{ALL, FEATURES, ECOSYSTEM}, LIC_PRO, "Welcome+to+JFrog+Xray"),
    MULTIPUSH("multipush", "Multipush Replication", 100, new String[]{ALL, ENTERPRISE}, LIC_ENT, "Repository+Replication#RepositoryReplication-Multi-pushReplication"),
    LICENSES("license", "License Control", 200, new String[]{ALL, FEATURES}, LIC_PRO, "License+Control"),
    REST("rest", "Advanced REST", 300, new String[]{ALL, FEATURES}, LIC_PRO, "Artifactory+REST+API"),
    LDAP("ldap", "LDAP Groups", 400, new String[]{ALL, FEATURES}, LIC_PRO, "Ldap+Groups"),
    REPLICATION("replication", "Repository Replication", 500, new String[]{ALL, FEATURES}, LIC_PRO, "Repository+Replication"),
    PROPERTIES("properties", "Properties", 600, new String[]{ALL, FEATURES}, LIC_PRO, "Properties"),
    SEARCH("search", "Smart Searches", 700, new String[]{ALL, FEATURES}, LIC_PRO, "Smart+Searches"),
    PLUGINS("plugins", "User Plugins", 800, new String[]{ALL, FEATURES}, LIC_PRO, "User+Plugins"),
    YUM("rpm", "RPM", 900, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "RPM+Repositories"),
    P2("p2", "P2", 1000, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "P2+Repositories"),
    NUGET("nuget", "NuGet", 1100, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Nuget+Repositories"),
    LAYOUTS("layouts", "Repository Layouts", 1200, new String[]{ALL, FEATURES}, LIC_PRO, "Repository+Layouts"),
    FILTERED_RESOURCES("filtered-resources", "Filtered Resources", 1300, new String[]{ALL, FEATURES}, LIC_PRO, "Filtered+Resources"),
    SSO("sso", "Crowd & SSO", 1400, new String[]{ALL}, LIC_PRO, "Atlassian+Crowd+and+JIRA+Integration"),
    SSH("ssh", "SSH", 500, new String[]{ALL, FEATURES}, LIC_OSS, "SSH+Integration"),
    WATCH("watch", "Watches", 1500, new String[]{ALL, FEATURES}, LIC_PRO, "Watches"),
    WEBSTART("webstart", "Jar Signing", 1600, new String[]{ALL, FEATURES}, LIC_PRO,"WebStart+and+Jar+Signing"),
    GEMS("gems", "RubyGems", 1100, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "RubyGems+Repositories") ,
    NPM("npm", "npm", 860, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "NPM+Registry"),
    BOWER("bower", "Bower", 870, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Bower+Repositories"),
    COCOAPODS("cocoapods", "CocoaPods", 880, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "CocoaPods+Repositories"),
    CONAN("conan", "Conan", 890, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Conan+Repositories"),
    DEBIAN("debian", "Debian", 900, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Debian+Repositories"),
    DISTRIBUTION("distribution", "JFrog Bintray Distribution", 930, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "Distribution+Repository"),
    OPKG("opkg", "Opkg", 700, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Opkg+Repositories"),
    PYPI("pypi", "PyPI", 970, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "PyPI+Repositories"),
    PUPPET("puppet", "Puppet", 990, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Puppet+Repositories"),
    DOCKER("docker", "Docker", 910, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Docker+Repositories"),
    VAGRANT("vagrant", "Vagrant", 915, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Vagrant+Repositories"),
    VCS("vcs", "VCS", 920, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "VCS+Repositories"),
    GITLFS("git-lfs", "Git LFS", 930, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Git+LFS+Repositories"),
    HA("ha", "High Availability", 2000, new String[]{ALL, ENTERPRISE}, LIC_ENT, "Artifactory+High+Availability"),
    S3("s3fileStore", "S3 Object Store", 2000, new String[]{ALL, ENTERPRISE}, LIC_ENT, "S3+Object+Storage"),
    GCS("gcs", "Google Cloud Storage", 2000, new String[]{ALL, ENTERPRISE}, LIC_ENT, "Google+Cloud+Storage"),
    HDFS("hdfsFileStore", "HDFS", 2020, new String[]{ALL, ENTERPRISE}, LIC_ENT, "HDFS+Storage" ),
    SHARDING("sharding", "Sharding", 2020, new String[]{ALL, ENTERPRISE}, LIC_ENT, "Filestore+Sharding" ),
    AQL("aql", "AQL", 2000, new String[]{ALL, FEATURES}, LIC_OSS, "Artifactory+Query+Language"),
    MAVEN_PLUGIN("maven", "Maven Plugin", 2000, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "Maven+Artifactory+Plugin"),
    GRADLE_PLUGIN("gradle", "Gradle Plugin", 2000, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "Gradle+Artifactory+Plugin"),
    JENKINS_PLUGIN("jenkins", "Jenkins Plugin", 2000, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "Jenkins+(Hudson)+Artifactory+Plug-in"),
    BAMBOO_PLUGIN("bamboo", "Bamboo Plugin", 2000, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "Bamboo+Artifactory+Plug-in"),
    TC_PLUGIN("teamcity", "TeamCity Plugin", 2000, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "TeamCity+Artifactory+Plug-in"),
    MSBUILD_PLUGIN("msbuild", "MSBuild/TFS Plugin", 2000, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "MSBuild+Artifactory+Plugin"),
    BINTRAY_INTEGRATION("bintray-integration", "Bintray Integration", 2000, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "Bintray+Integration"),
    JFROG_CLI("jfrog-cli", "JFrog CLI", 930, new String[]{ALL, ECOSYSTEM}, "jfrog-cli","Welcome+to+JFrog+CLI"),
    SMART_REPO("smart-repo", "Smart Remote Repo", 2000, new String[]{ALL, FEATURES}, LIC_PRO, "Smart+Remote+Repositories"),
    SUMOLOGIC("sumo-logic", "Sumo Logic", 2000, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "Log+Analytics"),
    OAUTH("oauth", "OAuth", 2000, new String[]{ALL, FEATURES}, LIC_PRO, "OAuth+Integration"),
    SBT("sbt", "SBT", 2000, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_OSS, "SBT+Repositories"),
    IVY("ivy", "Ivy Plugin", 2000, new String[]{ALL, ECOSYSTEM}, LIC_OSS, "Working+with+Ivy"),
    COMPOSER("composer", "PHP Composer", 2100, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "PHP+Composer+Repositories"),
    CHEF("chef", "Chef Cookbook", 2200, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Chef+Supermarket"),
    HELM("helm", "Helm", 900, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Helm+Charts+Repositories"),
    GO("go", "Go", 900, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Go+Repositories"),
    CRAN("cran", "CRAN", 900, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Cran+Repositories"),
    CONDA("conda", "Conda", 910, new String[]{ALL, PACKAGE_MANAGEMENT}, LIC_PRO, "Conda+Repositories");

    private String addonName;
    private String addonDisplayName;
    private int displayOrdinal;
    private String[] categories;
    private String type;
    private String configureUrlSuffix;

    AddonType(String addonName, String addonDisplayName, int displayOrdinal, String[] categories, String type, String configureUrlSuffix) {
        this.addonName = addonName;
        this.addonDisplayName = addonDisplayName;
        this.displayOrdinal = displayOrdinal;
        this.categories = categories;
        this.type = type;
        this.configureUrlSuffix = configureUrlSuffix;
    }

    public String getAddonDisplayName() {
        return addonDisplayName;
    }

    public String getAddonName() {
        return addonName;
    }

    public int getDisplayOrdinal() {
        return displayOrdinal;
    }

    public String[] getCategories() {
        return categories;
    }

    public String getType() {
        return type;
    }

    public String getConfigureUrlSuffix() {
        return configureUrlSuffix;
    }
}