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

package org.artifactory.mime;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * @author Gidi Shabat
 */
public class DebianNaming {

    public static final String RELEASE = "Release";
    public static final String INRELEASE = "InRelease";
    public static final String RELEASE_GPG = "Release.gpg";
    public static final String PACKAGES = "Packages";
    public static final String PACKAGES_GZ = "Packages.gz";
    public static final String PACKAGES_BZ2 = "Packages.bz2";
    public static final String PACKAGES_XZ = "Packages.xz";
    public static final String PACKAGES_LZMA = "Packages.lzma";

    public static final String SOURCES = "Sources";
    public static final String SOURCES_BZ2 = "Sources.bz2";
    public static final String SOURCES_GZ = "Sources.gz";
    public static final String CONTENTS_PREFIX = "Contents";
    public static final String DISTS_PATH = "dists";

    public static final String DISTRIBUTION_PROP = "deb.distribution";
    public static final String COMPONENT_PROP = "deb.component";
    public static final String ARCHITECTURE_PROP = "deb.architecture";
    public static final String packageType = "deb.type";

    public static final String DEBIAN_NAME = "deb.name";
    public static final String DEBIAN_VERSION = "deb.version";
    public static final String DEBIAN_MAINTAINER = "deb.maintainer";
    public static final String DEBIAN_PRIORITY = "deb.priority";
    public static final String DEBIAN_SECTION = "deb.section";
    public static final String DEBIAN_WEBSITE = "deb.website";

    public static boolean isIndexFile(@Nonnull String fileName) {
        return isPackagesIndex(fileName) || isReleaseIndex(fileName) || isContentIndex(fileName);
    }

    public static boolean isSupportedIndex(@Nonnull String fileName) {
        return isReleaseIndex(fileName) || isPackagesIndex(fileName);
    }

    public static boolean isExpirable(@Nonnull String fileName) {
        return isTranslationIndex(fileName) || isSourcesIndex(fileName) ||
                isReleaseIndex(fileName) || isSigningFile(fileName) || isPackagesIndex(fileName) ||
                isContentIndex(fileName);
    }

    public static boolean isReleaseIndex(@Nonnull String fileName) {
        return fileName.equalsIgnoreCase(RELEASE) || fileName.equalsIgnoreCase(INRELEASE);
    }

    public static boolean isPackagesIndex(@Nonnull String fileName) {
        return Stream.of(PACKAGES, PACKAGES_GZ, PACKAGES_BZ2, PACKAGES_XZ, PACKAGES_LZMA).anyMatch(
                fileName::equalsIgnoreCase);
    }

    public static boolean isSourcesIndex(@Nonnull String fileName) {
        return fileName.equalsIgnoreCase(SOURCES) || fileName.equalsIgnoreCase(SOURCES_GZ) || fileName.equalsIgnoreCase(SOURCES_BZ2);
    }

    public static boolean isTranslationIndex(@Nonnull String fileName) {
        return fileName.startsWith("Translation-") || fileName.startsWith("translation-");
    }

    public static boolean isSigningFile(@Nonnull String fileName) {
        return fileName.equalsIgnoreCase(RELEASE_GPG);
    }

    public static boolean isInRelease(@Nonnull String fileName) {
        return fileName.equalsIgnoreCase(INRELEASE);
    }

    //Contents or Contents-<arch>.<gz\bz\bz2>
    public static boolean isContentIndex(@Nonnull String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.startsWith(CONTENTS_PREFIX.toLowerCase() + "-");
    }
}
