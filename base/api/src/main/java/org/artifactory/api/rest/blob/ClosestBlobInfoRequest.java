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

package org.artifactory.api.rest.blob;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.MimeType;
import org.codehaus.jackson.annotate.JsonProperty;

import static org.jfrog.common.ArgUtils.requireNonBlank;

/**
 * @author Rotem Kfir
 */
@ToString
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ClosestBlobInfoRequest {

    @JsonProperty
    private String checksum; // sha256

    @JsonProperty("component_type")
    private RepoType componentType;

    @JsonProperty("component_name")
    private String componentName;

    @JsonProperty("component_version")
    private String componentVersion;

    @JsonProperty("component_mimetype")
    private String componentMimetype;

    @JsonProperty("artifact_name")
    private String artifactName;

    private ClosestBlobInfoRequest(String checksum, String mimeType, RepoType componentType, String componentName, String componentVersion,
            String artifactName) {
        this.checksum = requireNonBlank(checksum, "Checksum is required");
        this.componentType = componentType;
        this.componentName = componentName;
        this.componentVersion = componentVersion;
        this.artifactName = requireNonBlank(artifactName, "Artifact name is required");
        this.componentMimetype = mimeType == null || mimeType.isEmpty() ? MimeType.def.getType() : mimeType;
    }

    public void validate() {
        requireNonBlank(checksum, "Checksum is required");
        requireNonBlank(artifactName, "Artifact name is required");
        requireNonBlank(componentMimetype, "Component Mime Type is required");
    }

    public String getChecksum() {
        return checksum;
    }

    public RepoType getComponentType() {
        return componentType;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public String getComponentMimetype() { return componentMimetype; }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String checksum;
        private RepoType componentType;
        private String componentName;
        private String componentVersion;
        private String componentMimetype;
        private String artifactName;

        public Builder() {
            //For serialization purposes
        }

        public Builder setChecksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder setComponentType(RepoType componentType) {
            this.componentType = componentType;
            return this;
        }

        public Builder setComponentName(String componentName) {
            this.componentName = componentName;
            return this;
        }

        public Builder setComponentVersion(String componentVersion) {
            this.componentVersion = componentVersion;
            return this;
        }

        public Builder setArtifactName(String artifactName) {
            this.artifactName = artifactName;
            return this;
        }

        public Builder setComponentMimetype(String mimeType){
            this.componentMimetype = mimeType;
            return this;
        }

        public ClosestBlobInfoRequest build() {
            return new ClosestBlobInfoRequest(checksum, componentMimetype, componentType, componentName, componentVersion, artifactName);
        }
    }
}
