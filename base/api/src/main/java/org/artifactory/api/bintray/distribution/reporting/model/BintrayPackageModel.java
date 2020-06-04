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
import com.google.common.collect.Sets;
import com.jfrog.bintray.client.api.details.PackageDetails;
import org.artifactory.api.bintray.BintrayUploadInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Dan Feldman
 */
public class BintrayPackageModel {

    public String packageName;
    public Boolean created;
    public Set<String> licenses = Sets.newHashSet();
    public Map<String, BintrayVersionModel> versions = Maps.newHashMap();

    public BintrayPackageModel() {

    }

    public BintrayPackageModel(BintrayUploadInfo uploadInfo) {
        PackageDetails pkgDetails = uploadInfo.getPackageDetails();
        this.packageName = pkgDetails.getName();
        if (pkgDetails.getLicenses() != null) {
            this.licenses.addAll(pkgDetails.getLicenses());
        }
        versions.put(uploadInfo.getVersionDetails().getName(), new BintrayVersionModel(uploadInfo));
    }

    public void merge(BintrayPackageModel pkg) {
        licenses.addAll(pkg.licenses);
        pkg.versions.values().forEach(this::addVersion);
    }

    private void addVersion(BintrayVersionModel bintrayVersion) {
        BintrayVersionModel existingVersion = versions.get(bintrayVersion.versionName);
        if (existingVersion == null) {
            versions.put(bintrayVersion.versionName, bintrayVersion);
        } else {
            existingVersion.merge(bintrayVersion);
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public Boolean getCreated() {
        return created;
    }

    public Set<String> getLicenses() {
        return licenses;
    }

    public Collection<BintrayVersionModel> getVersions() {
        return versions.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BintrayPackageModel)) return false;
        BintrayPackageModel that = (BintrayPackageModel) o;
        return packageName != null ? packageName.equals(that.packageName) : that.packageName == null;
    }

    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }
}
