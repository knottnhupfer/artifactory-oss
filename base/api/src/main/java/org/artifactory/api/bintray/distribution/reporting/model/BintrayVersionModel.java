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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.jfrog.bintray.client.api.details.VersionDetails;
import org.artifactory.api.bintray.BintrayUploadInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Dan Feldman
 */
public class BintrayVersionModel {

    public String versionName;
    public Boolean created;
    public Multimap<String, String> attributes = HashMultimap.create();
    public Set<String> paths = Sets.newHashSet();

    public BintrayVersionModel() {

    }

    public BintrayVersionModel(BintrayUploadInfo uploadInfo) {
        VersionDetails versionDetails = uploadInfo.getVersionDetails();
        this.versionName = versionDetails.getName();
        if (versionDetails.getAttributes() != null) {
            versionDetails.getAttributes().forEach(attribute -> attributes.putAll(attribute.getName(), attribute.getValues()));
        }
    }

    public void merge(BintrayVersionModel version) {
        attributes.putAll(version.attributes);
        paths.addAll(version.paths);
    }

    public String getVersionName() {
        return versionName;
    }

    public Boolean getCreated() {
        return created;
    }

    public Map<String, Collection<String>> getAttributes() {
        if (attributes.isEmpty()) {
            return null;
        } else {
            return attributes.asMap();
        }
    }

    public Set<String> getPaths() {
        return paths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BintrayVersionModel)) return false;
        BintrayVersionModel that = (BintrayVersionModel) o;
        return versionName != null ? versionName.equals(that.versionName) : that.versionName == null;
    }

    @Override
    public int hashCode() {
        return versionName != null ? versionName.hashCode() : 0;
    }
}
