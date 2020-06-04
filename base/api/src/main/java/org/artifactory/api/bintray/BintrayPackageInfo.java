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

package org.artifactory.api.bintray;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Bintray package info
 *
 * @author Gidi Shabat
 */
public class BintrayPackageInfo implements Serializable {
    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "repo")
    private String repo;
    @JsonProperty(value = "owner")
    private String owner;
    @JsonProperty(value = "desc")
    private String desc;
    @JsonProperty(value = "desc_url")
    private String desc_url;
    //@JsonProperty(value = "labels")
    //private String labels;
    //@JsonProperty(value = "attribute_names")
    //private String attribute_names;
    @JsonProperty(value = "rating")
    private String rating;
    @JsonProperty(value = "rating_count")
    private String rating_count;
    @JsonProperty(value = "followers_count")
    private String followers_count;
    @JsonProperty(value = "created")
    private String created;
    //@JsonProperty(value = "versions")
    //private String versions;
    @JsonProperty(value = "latest_version")
    private String latest_version;
    @JsonProperty(value = "updated")
    private String updated;
    @JsonProperty(value = "linked_to_repo")
    private String linked_to_repo;


    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getDesc_url() {
        return desc_url;
    }

    public String getRepo() {
        return repo;
    }

    //public String getVersions() {
    //    return versions;
    //}

    public String getLatest_version() {
        return latest_version;
    }

    public String getUpdated() {
        return updated;
    }

    public String getRating_count() {
        return rating_count;
    }

    public String getCreated() {
        return created;
    }

    public String getFollowers_count() {
        return followers_count;
    }

    public String getRating() {
        return rating;
    }

    //public String getAttribute_names() {
    //    return attribute_names;
    //}

    //public String getLabels() {
    //    return labels;
    //}

    public String getLinked_to_repo() {
        return linked_to_repo;
    }
}
