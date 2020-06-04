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

package org.artifactory.descriptor.repo;

import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Comparator;

/**
 * @author Shay Yaakov
 */
@XmlEnum(value = String.class)
@GenerateDiffFunction(typeMethod = "getType")
public enum RepoType {
    @XmlEnumValue("maven")Maven("maven", "Maven", "gav"),
    @XmlEnumValue("gradle")Gradle("gradle", "Gradle"),
    @XmlEnumValue("ivy")Ivy("ivy", "Ivy"),
    @XmlEnumValue("sbt")SBT("sbt", "SBT"),
    @XmlEnumValue("nuget")NuGet("nuget", "NuGet"),
    @XmlEnumValue("gems")Gems("gems", "Gems", "rubygems"),
    @XmlEnumValue("npm")Npm("npm", "Npm"),
    @XmlEnumValue("bower")Bower("bower", "Bower"),
    @XmlEnumValue("debian")Debian("debian", "Debian", "deb"),
    @XmlEnumValue("pypi")Pypi("pypi", "Pypi"),
    @XmlEnumValue("puppet")Puppet("puppet", "Puppet"),
    @XmlEnumValue("docker")Docker("docker", "Docker"),
    @XmlEnumValue("vagrant")Vagrant("vagrant", "Vagrant"),
    @XmlEnumValue("gitlfs")GitLfs("gitlfs", "GitLfs"),
    @XmlEnumValue("yum")YUM("yum", "RPM", "rpm"),
    @XmlEnumValue("vcs")VCS("vcs", "VCS"),
    @XmlEnumValue("p2")P2("p2", "P2"),
    @XmlEnumValue("generic")Generic("generic", "Generic"),
    @XmlEnumValue("opkg")Opkg("opkg", "Opkg"),
    @XmlEnumValue("cocoapods")CocoaPods("cocoapods", "CocoaPods"),
    @XmlEnumValue("conan")Conan("conan", "Conan"),
    @XmlEnumValue("distribution")Distribution("distribution", "Distribution"),
    @XmlEnumValue("releasebundles")ReleaseBundles("releasebundles", "ReleaseBundles"),
    @XmlEnumValue("buildinfo")BuildInfo("buildinfo", "BuildInfo"),
    @XmlEnumValue("chef")Chef("chef", "Chef"),
    @XmlEnumValue("composer")Composer("composer", "Composer"),
    @XmlEnumValue("helm")Helm("helm", "Helm"),
    @XmlEnumValue("go")Go("go", "Go"),
    @XmlEnumValue("cran") CRAN("cran", "CRAN"),
    @XmlEnumValue("conda")Conda("conda", "Conda"),
    @XmlEnumValue("supportbundles")Support("support", "Support");

    private final String type;
    private final String displayName;
    private String pkgidPrefix;

    RepoType(String type, String displayName) {
        this.type = type;
        this.displayName = displayName;
        this.pkgidPrefix = type;
    }

    RepoType(String type, String displayName, String pkgidPrefix) {
        this.type = type;
        this.displayName = displayName;
        this.pkgidPrefix = pkgidPrefix;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Used by REST methods to properly display package types to the users
    public String getEffectiveType() {
        if (this == YUM) {
            return "rpm";
        }
        return type;
    }

    public boolean isMavenGroup() {
        return this == Maven || this == Ivy || this == Gradle || this == P2 || this == SBT;
    }

    public String getPkgidPrefix() {
        if (!pkgidPrefix.equals(type)) {
            return pkgidPrefix;
        }
        return type;
    }

    public boolean isVcsGroup() {
        return this == VCS || this == Composer || this == CocoaPods || this == Bower;
    }
    public static RepoType fromType(String type) {
        type = normalizePackageType(type);
        for (RepoType repoType : values()) {
            if (type.equalsIgnoreCase(repoType.type)) {
                return repoType;
            }
        }
        return Generic;
    }
    // Used to convert users requests (REST \ UI) to backend type

    private static String normalizePackageType(String packageType) {
        packageType = packageType.toLowerCase();
        if (packageType.equals("rpm")) {
            return "yum";
        }
        return packageType;
    }

    public static class RepoNameComparator implements Comparator<RepoType> {

        @Override
        public int compare(RepoType o1, RepoType o2) {
            return o1.name().compareTo(o2.name());
        }
    }
}
