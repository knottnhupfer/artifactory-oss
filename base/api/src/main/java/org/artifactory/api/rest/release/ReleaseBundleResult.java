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

package org.artifactory.api.rest.release;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Model represent the release bundle result. For example:
 * <pre>
 *     {@code
 *     {
 *      "results" : [ {
 *          "urn" : "jf-artifactory@28189424-1f38-43bd-82e3-d89511e2ce40/example-repo-local/builds.sql",
 *          "sha256" : "f7d5c289b8401ec5c30147f0286ae52880687008adb83a8eb0e9453ff2573f37",
 *          "properties" : {
 *              "someKey2" : [ "somevalue" ],
 *              "someKey" : [ "value1", "value2" ]
 *          },
 *          "signature" : "willBeReadySoon",
 *          "pkg_type" : "willBeReadySoon",
 *          "pkg_name" : "builds.sql",
 *          "pkg_version" : "3.0.0"
 *          }, {
 *          "urn" : "jf-artifactory@28189424-1f38-43bd-82e3-d89511e2ce40/example-repo-local/file.txt",
 *          "sha256" : "abd57727297a6f93fa406c6aadae0e5ef7bf960c20953d14c72ddfe7f2ddbce0",
 *          "properties" : {
 *              "someKey2" : [ "somevalue" ],
 *              "someKey" : [ "value1", "value2" ]
 *          },
 *          "signature" : "willBeReadySoon",
 *          "pkg_type" : "willBeReadySoon",
 *          "pkg_name" : "file.txt",
 *          "pkg_version" : "3.0.0",
 *          "size" : "100" // in Bytes
 *      } ]
 *  }
 * </pre>
 *
 * @author Shay Bagants
 */
public class ReleaseBundleResult {

    private List<ReleaseBundleItem> results = Lists.newArrayList();

    public List<ReleaseBundleItem> getResults() {
        return results;
    }

    public void setResults(List<ReleaseBundleItem> results) {
        this.results = results;
    }

    public static class ReleaseBundleItem {
        private String urn;
        private String sha256;
        private Map<String, List<String>> properties = new HashMap<>();
        private String signature;
        @JsonProperty("pkg_type")
        private String pkgType;
        @JsonProperty("pkg_name")
        private String pkgName;
        @JsonProperty("pkg_version")
        private String pkgVersion;
        private long size; // in Bytes
        @JsonProperty("xray_scan_info")
        private ReleaseBundleItemXrayInfo xrayScanInfo;

        public ReleaseBundleItem(String urn, String sha256, Map<String, List<String>> properties, String signature,
                String pkgType, String pkgName, String pkgVersion, long size) {
            this.urn = urn;
            this.sha256 = sha256;
            this.properties = properties;
            this.signature = signature;
            this.pkgType = pkgType;
            this.pkgName = pkgName;
            this.pkgVersion = pkgVersion;
            this.size = size;
        }

        public ReleaseBundleItem(String urn, String pkgType, long size) {
            this.urn = urn;
            this.pkgType = pkgType;
            this.size = size;
        }

        public ReleaseBundleItem() {
        }

        public String getUrn() {
            return urn;
        }

        public void setUrn(String urn) {
            this.urn = urn;
        }

        public String getSha256() {
            return sha256;
        }

        public void setSha256(String sha256) {
            this.sha256 = sha256;
        }

        public Map<String, List<String>> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, List<String>> properties) {
            this.properties = properties;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getPkgType() {
            return pkgType;
        }

        public void setPkgType(String pkgType) {
            this.pkgType = pkgType;
        }

        public String getPkgName() {
            return pkgName;
        }

        public void setPkgName(String pkgName) {
            this.pkgName = pkgName;
        }

        public String getPkgVersion() {
            return pkgVersion;
        }

        public void setPkgVersion(String pkgVersion) {
            this.pkgVersion = pkgVersion;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public ReleaseBundleItemXrayInfo getXrayScanInfo() {
            return xrayScanInfo;
        }

        public void setXrayScanInfo(ReleaseBundleItemXrayInfo xrayScanInfo) {
            this.xrayScanInfo = xrayScanInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ReleaseBundleItem that = (ReleaseBundleItem) o;
            return Objects.equals(urn, that.urn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(urn);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseBundleItemXrayInfo {

        private boolean blocked;

        @JsonProperty("blocked_reason")
        @Nullable
        private String blockedReason;
    }
}
