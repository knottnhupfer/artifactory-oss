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

package org.artifactory.api.component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.MimeType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;

/**
 * @author Gal Ben Ami
 */
@Data
@JsonSerialize
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComponentDetails {

    private static ComponentDetails DEFAULT_COMPONENT_DETAILS = ComponentDetails.builder().build();

    private static final ObjectMapper mapper = new ObjectMapper();

    private String name;
    private String version;
    private RepoType componentType;
    private String extension;
    @Builder.Default
    private String mimeType = MimeType.def.getType();

    public static ComponentDetails fromJson(String jsonString) {
        try {
            return mapper.readValue(jsonString, ComponentDetails.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String toJson(ComponentDetails componentDetails) {
        try {
            return mapper.writeValueAsString(componentDetails);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static ComponentDetails getDefaultComponentDetails() {
        return DEFAULT_COMPONENT_DETAILS;
    }
}
