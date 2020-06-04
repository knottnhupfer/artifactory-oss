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

package org.artifactory.api.bintray;

import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.details.RepositoryDetails;
import com.jfrog.bintray.client.api.details.VersionDetails;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jfrog.build.api.release.BintrayUploadInfoOverride;

import java.util.*;

/**
 * Container class for the Bintray client's PackageDetails and VersionDetails to allow easy deserialization from
 * Artifactory's own json format which also includes the file and property filters.
 *
 * @author Dan Feldman
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BintrayUploadInfo {

    @JsonProperty("repo")
    RepositoryDetails repositoryDetails = new RepositoryDetails();
    @JsonProperty("package")
    PackageDetails packageDetails = new PackageDetails();
    @JsonProperty("version")
    VersionDetails versionDetails = new VersionDetails();
    @JsonProperty(value = "applyToRepoFiles")
    private List<String> artifactRelativePaths;                 //Relative paths in same repo
    @JsonProperty(value = "applyToFiles")
    private List<String> artifactPaths;                         //Fully qualified paths, from any repo
    @JsonProperty(value = "applyToProps")
    private Set<Map<String, Collection<String>>> filterProps;   //Properties to filter artifacts by
    @JsonProperty
    private Boolean publish;

    public BintrayUploadInfo() {

    }

    @JsonIgnore
    public BintrayUploadInfo(BintrayUploadInfoOverride override) {
        this.packageDetails = new PackageDetails(override.packageName);
        this.packageDetails.setSubject(override.subject);
        this.packageDetails.setRepo(override.repoName);
        this.packageDetails.setLicenses(override.licenses);
        this.versionDetails = new VersionDetails(override.versionName);
        this.packageDetails.setVcsUrl(override.vcsUrl);
    }

    public RepositoryDetails getRepositoryDetails() {
        return repositoryDetails;
    }

    public void setRepositoryDetails(RepositoryDetails repositoryDetails) {
        this.repositoryDetails = repositoryDetails;
    }

    public PackageDetails getPackageDetails() {
        return packageDetails;
    }

    public void setPackageDetails(PackageDetails packageDetails) {
        this.packageDetails = packageDetails;
    }

    public VersionDetails getVersionDetails() {
        return versionDetails;
    }

    public void setVersionDetails(VersionDetails versionDetails) {
        this.versionDetails = versionDetails;
    }

    public List<String> getArtifactRelativePaths() {
        return artifactRelativePaths;
    }

    public void setArtifactRelativePaths(List<String> artifactRelativePaths) {
        this.artifactRelativePaths = artifactRelativePaths;
    }

    public List<String> getArtifactPaths() {
        return artifactPaths;
    }

    public void setArtifactPaths(List<String> artifactPaths) {
        this.artifactPaths = artifactPaths;
    }

    public Set<Map<String, Collection<String>>> getFilterProps() {
        return filterProps;
    }

    public void setFilterProps(Set<Map<String, Collection<String>>> filterProps) {
        this.filterProps = filterProps;
    }

    public Boolean isPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    @Override
    @JsonIgnore
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BintrayUploadInfo)) {
            return false;
        }

        BintrayUploadInfo that = (BintrayUploadInfo) o;

        if (repositoryDetails != null) {
            if (that.repositoryDetails != null) {
                if (!Optional.ofNullable(repositoryDetails.getName()).orElse("")
                        .equals(that.repositoryDetails.getName()) ||
                        !Optional.ofNullable(repositoryDetails.getOwner()).orElse("")
                                .equals(that.repositoryDetails.getOwner()) ||
                        !Optional.ofNullable(repositoryDetails.getType()).orElse("")
                                .equals(that.repositoryDetails.getType())) {
                    return false;
                }
            } else {
                return false;
            }
        } else if (that.repositoryDetails != null) {
            return false;
        }

        if (packageDetails != null) {
            if (that.packageDetails != null) {
                if (!Optional.ofNullable(packageDetails.getName()).orElse("").equals(that.packageDetails.getName()) ||
                        !Optional.ofNullable(packageDetails.getRepo()).orElse("")
                                .equals(that.packageDetails.getRepo()) ||
                        !Optional.ofNullable(packageDetails.getOwner()).orElse("")
                                .equals(that.packageDetails.getOwner()) ||
                        !Optional.ofNullable(packageDetails.getSubject()).orElse("")
                                .equals(that.packageDetails.getSubject())) {
                    return false;
                }
            } else {
                return false;
            }
        } else if (that.packageDetails != null) {
            return false;
        }

        if (versionDetails != null) {
            if (that.versionDetails != null) {
                return Optional.ofNullable(versionDetails.getName()).orElse("").equals(that.versionDetails.getName()) &&
                        Optional.ofNullable(versionDetails.getOwner()).orElse("")
                                .equals(that.versionDetails.getOwner()) &&
                        Optional.ofNullable(versionDetails.getPkg()).orElse("").equals(that.versionDetails.getPkg()) &&
                        Optional.ofNullable(versionDetails.getRepo()).orElse("").equals(that.versionDetails.getRepo());
            } else {
                return false;
            }
        } else {
            return that.versionDetails == null;
        }
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        int result = 0;
        if (repositoryDetails != null) {
            result = 31 * result + (repositoryDetails.getName() != null ? repositoryDetails.getName().hashCode() : 0);
            result = 31 * result + (repositoryDetails.getOwner() != null ? repositoryDetails.getOwner().hashCode() : 0);
            result = 31 * result + (repositoryDetails.getType() != null ? repositoryDetails.getType().hashCode() : 0);
        }
        if (packageDetails != null) {
            result = 31 * result + (packageDetails.getName() != null ? packageDetails.getName().hashCode() : 0);
            result = 31 * result + (packageDetails.getRepo() != null ? packageDetails.getRepo().hashCode() : 0);
            result = 31 * result + (packageDetails.getOwner() != null ? packageDetails.getOwner().hashCode() : 0);
            result = 31 * result + (packageDetails.getSubject() != null ? packageDetails.getSubject().hashCode() : 0);
        }
        if (versionDetails != null) {
            result = 31 * result + (versionDetails.getName() != null ? versionDetails.getName().hashCode() : 0);
            result = 31 * result + (versionDetails.getOwner() != null ? versionDetails.getOwner().hashCode() : 0);
            result = 31 * result + (versionDetails.getPkg() != null ? versionDetails.getPkg().hashCode() : 0);
            result = 31 * result + (versionDetails.getRepo() != null ? versionDetails.getRepo().hashCode() : 0);
        }
        return result;
    }

    @Override
    @JsonIgnore
    public String toString() {
        String toString = "";
        if (repositoryDetails != null) {
            toString += repositoryDetails.getOwner() == null ? "" : repositoryDetails.getOwner() + "/";
            toString += repositoryDetails.getName() == null ? "" : repositoryDetails.getName() + "/";
        }
        if (packageDetails != null) {
            toString += packageDetails.getName() == null ? "" : packageDetails.getName() + "/";
        }
        if (versionDetails != null) {
            toString += versionDetails.getName() == null ? "" : versionDetails.getName();
        }
        return toString;
    }
}