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

import java.util.Map;

/**
 * @author nadavy
 */
public class SourceReleaseBundleRequest {

    private String signedJwsBundle;
    private String storingRepo;
    private Map<String, String> artifactMapping;

    public SourceReleaseBundleRequest() {
    }

    public SourceReleaseBundleRequest(String signedJwsBundle, String storingRepo,
            Map<String, String> artifactMapping) {
        this.signedJwsBundle = signedJwsBundle;
        this.storingRepo = storingRepo;
        this.artifactMapping = artifactMapping;
    }

    public String getSignedJwsBundle() {
        return signedJwsBundle;
    }

    public void setSignedJwsBundle(String signedJwsBundle) {
        this.signedJwsBundle = signedJwsBundle;
    }

    public String getStoringRepo() {
        return storingRepo;
    }

    public void setStoringRepo(String storingRepo) {
        this.storingRepo = storingRepo;
    }

    public Map<String, String> getArtifactMapping() {
        return artifactMapping;
    }

    public void setArtifactMapping(Map<String, String> artifactMapping) {
        this.artifactMapping = artifactMapping;
    }

    @Override
    public String toString() {
        return "SourceReleaseBundleRequest{" +
                "signedJwsBundle='" + signedJwsBundle + '\'' +
                ", storingRepo='" + storingRepo + '\'' +
                ", artifactMapping=" + artifactMapping +
                '}';
    }
}
