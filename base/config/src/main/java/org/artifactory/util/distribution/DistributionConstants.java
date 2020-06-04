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

package org.artifactory.util.distribution;

import java.util.regex.Pattern;

/**
 * @author Dan Feldman
 */
public abstract class DistributionConstants {

    //Default Bintray repo names
    public static final String DEFAULT_CONAN_REPO_NAME = "conan";
    public static final String DEFAULT_DEB_REPO_NAME = "deb";
    public static final String DEFAULT_OPKG_REPO_NAME = "opkg";
    public static final String DEFAULT_VAGRANT_REPO_NAME = "boxes";
    public static final String DEFAULT_MAVEN_REPO_NAME = "maven";
    public static final String DEFAULT_NUGET_REPO_NAME = "nuget";
    public static final String DEFAULT_DOCKER_REPO_NAME = "registry";
    public static final String DEFAULT_RPM_REPO_NAME = "rpm";
    public static final String DEFAULT_GENERIC_REPO_NAME = "generic";

    //Default tokens
    public static final String PATH_TOKEN = wrapToken("artifactPath");
    public static final String PRODUCT_NAME_TOKEN = wrapToken("productName");
    public static final String PACKAGE_NAME_TOKEN = wrapToken("packageName");
    public static final String PACKAGE_VERSION_TOKEN = wrapToken("packageVersion");
    public static final String ARCHITECTURE_TOKEN = wrapToken("architecture");
    public static final String DOCKER_IMAGE_TOKEN = wrapToken("dockerImage");
    public static final String DOCKER_TAG_TOKEN = wrapToken("dockerTag");
    public static final String MODULE_TOKEN = wrapToken("module");
    public static final String ORG_TOKEN = wrapToken("org");
    public static final String CHANNEL_TOKEN = wrapToken("channel");
    public static final String BASE_REV_TOKEN = wrapToken("baseRev");
    public static final String VCS_TAG_TOKEN = wrapToken("vcsTag");
    public static final String VCS_REPO_TOKEN = wrapToken("vcsRepo");
    //Used internally to piggy-back the product name on the prop rule token
    public static final String PRODUCT_NAME_DUMMY_PROP = "internal.descriptor.product.name";
    public static final String ARTIFACT_TYPE_OVERRIDE_PROP = "distribution.package.type";
    public static final String CHECKSUM_DEPLOY_HEADER = "X-Checksum-Deploy";
    public static final String CHECKSUM_HEADER = "X-Checksum";
    public static final String IN_TRANSIT_REPO_KEY = "_intransit";
    public static final String EDGE_UPLOADS_REPO_KEY = "artifactory-edge-uploads";

    public static final Pattern TOKEN_PATTERN = Pattern.compile("\\$\\{[a-zA-Z]+\\}");

    public static String wrapToken(String key) {
        return "${" + key + "}";
    }

    public static String stripTokenBrackets(String token) {
        return token.replaceFirst("\\[", "").replaceFirst("\\]", "");
    }
}
