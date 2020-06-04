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

package org.artifactory.addon.docker;

import java.util.Map;

/**
 * @author Dan Feldman
 */
public class DockerLabel {

    private String key;
    private String value;

    public DockerLabel(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public DockerLabel(Map.Entry<String, String> dockerV2InfoLabelEntry) {
        this.key = dockerV2InfoLabelEntry.getKey();
        this.value = dockerV2InfoLabelEntry.getValue();
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
