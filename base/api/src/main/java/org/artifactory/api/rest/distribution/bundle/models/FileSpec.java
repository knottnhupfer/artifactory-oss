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

package org.artifactory.api.rest.distribution.bundle.models;

import com.google.common.base.Joiner;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jfrog.common.ArgUtils;

import java.util.List;

/**
 * @author Tomer Mayost
 */
@Data
@NoArgsConstructor
public class FileSpec {

    @JsonProperty(value = "source_path")
    String sourcePath;
    List<ArtifactProperty> props;
    @JsonProperty(value = "target_artifactory_url")
    String targetArtifactoryUrl;
    @JsonProperty(value = "target_path")
    String targetPath;
    @JsonProperty(value = "release_bundle")
    boolean releaseBundle;
    @JsonIgnore
    String internalTmpPath;

    public void setInternalTmpPath(String transactionPath) {
        this.internalTmpPath = Joiner.on("/")
                .join(transactionPath, targetPath);
    }

    @Override
    public String toString() {
        return "FileSpec{" +
                "sourcePath='" + sourcePath + '\'' +
                ", props=" + props +
                ", targetArtifactoryUrl='" + targetArtifactoryUrl + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", releaseBundle=" + releaseBundle +
                ", internalTmpPath='" + internalTmpPath + '\'' +
                '}';
    }

    public void validate(){
       sourcePath = ArgUtils.requireNonBlank(sourcePath,"source_path must not be blank");
       targetPath = ArgUtils.requireNonBlank(targetPath,"target_path must not be blank");
       targetArtifactoryUrl = ArgUtils.requireNonBlank(targetArtifactoryUrl,"target_artifactory_url must not be blank");
    }
}
