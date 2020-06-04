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

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gidi Shabat
 */
public class ArtifactoryStandardUrlResolver {

    //Keep this alphabetical please!
    private static final String[] templates = {"bower", "chef", /*coco*/"pods", "composer", "conan", "conda", "cran", "deb", "docker",
            "gems", "go", "helm", /*git-*/"lfs", "npm", "nuget", "puppet", "pypi", "vagrant", "vcs", "yum"};
    private static Pattern remoteRepoUrlPattern = Pattern.compile("(.+)/([^/]+)");

    private final Matcher remoteRepoUrlMatcher;
    private String url;

    public ArtifactoryStandardUrlResolver(String url) {
        this.url = url;
        remoteRepoUrlMatcher = remoteRepoUrlPattern.matcher(url);
        if (!remoteRepoUrlMatcher.find()) {
            throwInvalidUrlForm(url);
        }
    }

    public String getBaseUrl() {
        String baseUrl = remoteRepoUrlMatcher.group(1);
        baseUrl = peelRestApi(baseUrl);
        if (StringUtils.isBlank(baseUrl)) {
            throwInvalidUrlForm(url);
        }
        return baseUrl;
    }

    public String getRepoKey() {
        String repoKey = remoteRepoUrlMatcher.group(2);
        if (StringUtils.isBlank(repoKey)) {
            throwInvalidUrlForm(url);
        }
        return repoKey;
    }

    private static String peelRestApi(String url) {
        for (String template : templates) {
            if (url.endsWith("/api/" + template)) {
                url = StringUtils.removeEnd(url, "/api/" + template);
            }
        }
        return url;
    }

    private void throwInvalidUrlForm(String remoteUrl) {
        throw new IllegalArgumentException("The URL form of '" + remoteUrl + "' is unsupported.");
    }
}
