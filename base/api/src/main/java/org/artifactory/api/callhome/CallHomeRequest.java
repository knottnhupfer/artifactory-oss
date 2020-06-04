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

package org.artifactory.api.callhome;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.rest.subscription.Subscription;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * POJO used when calling home
 *
 * @author Shay Yaakov
 */
public class CallHomeRequest {

    public String product = "JFrog-Artifactory";

    public String repository = "artifactory";

    @JsonProperty(value = "package")
    public String packageName = "jfrog-artifactory-generic";

    public String version;

    @JsonProperty(value = "artifactory_license_type")
    public String licenseType;

    @JsonProperty(value = "artifactory_license_oem")
    public String licenseOEM;

    @JsonProperty(value = "artifactory_license_expiration")
    public String licenseExpiration;

    @JsonProperty(value = "db_type")
    public String dbType;

    @JsonProperty(value = "db_version")
    public String dbVersion;

    public String dist = "unknown";

    public String setup;

    @JsonProperty(value = "nunmber_of_nodes")
    public Integer numberOfNodes;

    @JsonProperty(value = "account_name")
    public String accountName;

    public Environment environment = new Environment();

    public Subscription subscription;

    @JsonProperty(value = "feature_groups")
    private List<FeatureGroup> featureGroups;


    public CallHomeRequest() {
        //For serialization proposes
    }

    /**
     * Adds in
     *
     * @param callHomeFeature
     */
    public void addCallHomeFeature(FeatureGroup callHomeFeature) {
        if (featureGroups == null)
            featureGroups = Lists.newLinkedList();
        if (callHomeFeature != null)
            featureGroups.add(callHomeFeature);
    }

    public void setDist(String artdist) {
        if (StringUtils.isBlank(artdist)) {
            return;
        }

        this.dist = artdist;
        String repoSuffix = StringUtils.equals(this.licenseType, "oss") ? "" : "-pro";
        String pkgSuffix = StringUtils.equals(this.licenseType, "oss") ? "-oss" : "-pro";
        switch (artdist) {
            case "docker":
                this.repository = "registry";
                this.packageName = "artifactory:artifactory" + pkgSuffix;
                break;
            case "zip":
                this.repository = "artifactory" + repoSuffix;
                this.packageName = "jfrog-artifactory" + pkgSuffix + "-zip";
                break;
            case "rpm":
                this.repository = "artifactory" + repoSuffix + "-rpms";
                this.packageName = "jfrog-artifactory" + pkgSuffix + "-rpm";
                break;
            case "deb":
                this.repository = "artifactory" + repoSuffix + "-debs";
                this.packageName = "jfrog-artifactory" + pkgSuffix + "-deb";
                break;
        }
    }

    public static class Environment {
        @JsonProperty(value = "runtime_id")
        public String hostId;

        @JsonProperty(value = "service_id")
        public String serviceId;

        @JsonProperty(value = "user_id")
        public String licenseHash;
        public Attributes attributes = new Attributes();

        public static class Attributes {
            @JsonProperty(value = "os_name")
            public String osName;
            @JsonProperty(value = "os_arch")
            public String osArch;
            @JsonProperty(value = "java_version")
            public String javaVersion;
        }
    }
}
