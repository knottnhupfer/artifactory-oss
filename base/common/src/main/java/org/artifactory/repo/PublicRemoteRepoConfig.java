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

package org.artifactory.repo;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Remote repository configuration class that contains information that should be available to non-admin user with
 * partial permission on the repository.
 *
 * @author Shay Bagants
 */
public class PublicRemoteRepoConfig implements CommonRepoConfig {

    private String key;
    private String type;
    private String packageType;
    private String description;
    private String url;

    public PublicRemoteRepoConfig(String key, String type, String packageType, String description, String url) {
        this.key = key;
        this.type = type;
        this.packageType = packageType;
        this.description = description;
        this.url = url;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @JsonProperty(RepositoryConfiguration.TYPE_KEY)
    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getPackageType() {
        return packageType;
    }

    @Override
    public String getKey() {
        return key;
    }

    public String getURL() {
        return url;
    }
}
