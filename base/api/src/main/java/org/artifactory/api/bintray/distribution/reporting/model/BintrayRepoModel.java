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

package org.artifactory.api.bintray.distribution.reporting.model;

import com.google.common.collect.Maps;
import com.jfrog.bintray.client.api.details.RepositoryDetails;
import org.artifactory.api.bintray.BintrayUploadInfo;

import java.util.Collection;
import java.util.Map;

/**
 * @author Dan Feldman
 */
public class BintrayRepoModel {

    public String repoName;
    public Boolean created;
    public String type;
    public String visibility;
    public Boolean premium;
    public Map<String, BintrayPackageModel> packages = Maps.newHashMap();

    public BintrayRepoModel(BintrayUploadInfo uploadInfo) {
        RepositoryDetails repoDetails = uploadInfo.getRepositoryDetails();
        this.repoName = repoDetails.getName();
        this.type = repoDetails.getType();
        this.premium = repoDetails.getPremium();
        this.visibility = repoDetails.getIsPrivate() ? "Private" : "Public";
        packages.put(uploadInfo.getPackageDetails().getName(), new BintrayPackageModel(uploadInfo));
    }

    public void merge(BintrayRepoModel bintrayRepoModel) {
        bintrayRepoModel.packages.values().forEach(this::addPackage);
    }

    private void addPackage(BintrayPackageModel bintrayPackage) {
        BintrayPackageModel existingPackage = packages.get(bintrayPackage.packageName);
        if (existingPackage == null) {
            packages.put(bintrayPackage.packageName, bintrayPackage);
        } else {
            existingPackage.merge(bintrayPackage);
        }
    }

    public String getRepoName() {
        return repoName;
    }

    public Boolean getCreated() {
        return created;
    }

    public String getType() {
        return type;
    }

    public String getVisibility() {
        return visibility;
    }

    public Boolean isPremium() {
        return premium;
    }

    public Collection<BintrayPackageModel> getPackages() {
        return packages.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BintrayRepoModel)) return false;
        BintrayRepoModel that = (BintrayRepoModel) o;
        return repoName != null ? repoName.equals(that.repoName) : that.repoName == null;
    }

    @Override
    public int hashCode() {
        return repoName != null ? repoName.hashCode() : 0;
    }
}
