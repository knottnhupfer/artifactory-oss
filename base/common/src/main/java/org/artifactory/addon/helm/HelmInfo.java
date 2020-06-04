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

package org.artifactory.addon.helm;

import lombok.Data;

import java.util.List;

/**
 * @author nadavy
 */
@Data
public class HelmInfo {

    private String name;
    private String version;
    private String appVersion;
    private String created;
    private String description;
    private List<String> keywords;
    private List<String> maintainers;
    private List<String> sources;
    private Boolean deprecated;

    HelmInfo(String name, String version, String appVersion, String created, String description,
            List<String> keywords, List<String> maintainers, List<String> sources, Boolean deprecated) {
        this.name = name;
        this.version = version;
        this.appVersion = appVersion;
        this.created = created;
        this.description = description;
        this.keywords = keywords;
        this.maintainers = maintainers;
        this.sources = sources;
        this.deprecated = deprecated;
    }
}

