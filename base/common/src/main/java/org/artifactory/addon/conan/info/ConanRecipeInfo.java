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

package org.artifactory.addon.conan.info;

/**
 * @author Yinon Avraham
 */
public class ConanRecipeInfo {

    private String name;
    private String version;
    private String user;
    private String channel;
    private String reference;
    private String author;
    private String url;
    private String license;

    private ConanRecipeInfo() { }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getUser() {
        return user;
    }

    public String getChannel() {
        return channel;
    }

    public String getReference() {
        return reference;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public String getLicense() {
        return license;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConanRecipeInfo recipeInfo = new ConanRecipeInfo();

        private Builder() {}

        public Builder name(String name) {
            recipeInfo.name = name;
            return this;
        }

        public Builder version(String version) {
            recipeInfo.version = version;
            return this;
        }

        public Builder user(String user) {
            recipeInfo.user = user;
            return this;
        }

        public Builder channel(String channel) {
            recipeInfo.channel = channel;
            return this;
        }

        public Builder reference(String reference) {
            recipeInfo.reference = reference;
            return this;
        }

        public Builder author(String author) {
            recipeInfo.author = author;
            return this;
        }

        public Builder url(String url) {
            recipeInfo.url = url;
            return this;
        }

        public Builder license(String license) {
            recipeInfo.license = license;
            return this;
        }

        public ConanRecipeInfo create() {
            ConanRecipeInfo result = recipeInfo;
            recipeInfo = null; // ensure this builder can no longer modify the instance
            return result;
        }
    }
}
