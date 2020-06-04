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

package org.artifactory.ui.utils;

import java.util.regex.Pattern;

/**
 * @author Chen Keinan
 */
public class RegExUtils {

    public static final Pattern LOCAL_REPO_REINDEX_PATTERN = Pattern.compile("Gems|Npm|Bower|CocoaPods|NuGet|Debian|Opkg|YUM|Pypi|Composer|Chef|Puppet|Helm|CRAN|Conda|Conan");
    public static final Pattern REMOTE_REPO_REINDEX_PATTERN = Pattern.compile("Bower|NuGet");
    public static final Pattern VIRTUAL_REPO_REINDEX_PATTERN = Pattern.compile("Gems|Npm|Bower|NuGet|Debian");

}
