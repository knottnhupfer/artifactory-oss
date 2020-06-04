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


import lombok.Builder;
import org.artifactory.descriptor.repo.RepoType;

/**
 * An object to hold minimal details for repository provisioning
 *
 * @author Noam Y. Tenne
 */
@Builder
public class RepoDetails {

    private String key;
    private String description;
    private RepoDetailsType type;
    private String url;
    private String configuration;
    private RepoType packageType;

    /**
     * Default Constructor
     */
    public RepoDetails() {
    }

    public RepoDetails(String key, String description, RepoDetailsType type, String url, String configuration, RepoType packageType) {
        this.key = key;
        this.description = description;
        this.type = type;
        this.url = url;
        this.configuration = configuration;
        this.packageType = packageType;
    }

    /**
     * Returns the key of the repository
     *
     * @return Repository key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of the repository
     *
     * @param key Repository key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the description of the repository
     *
     * @return Repository description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the repository
     *
     * @param description Repository description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the type of the repository
     *
     * @return Repository type
     */
    public RepoDetailsType getType() {
        return type;
    }

    /**
     * Sets the type of the repository
     *
     * @param type Repository type
     */
    public void setType(RepoDetailsType type) {
        this.type = type;
    }

    /**
     * Returns the URL of the repository
     *
     * @return Repository URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the repository
     *
     * @param url Repository URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the configuration URL of the repository
     *
     * @return Repository configuration URL
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration URL of the repository
     *
     * @param configuration Repository configuration URL
     */
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the package type of the repository
     *
     * @return Package type of the repository
     */
    public RepoType getPackageType() {
        return packageType;
    }
    /**
     * Sets the package type of the repository
     *
     * @param packageType Repository Package type
     */
    public void setPackageType(RepoType packageType) {this.packageType = packageType; }
}