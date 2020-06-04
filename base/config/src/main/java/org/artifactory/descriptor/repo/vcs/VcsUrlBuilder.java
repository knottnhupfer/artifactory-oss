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

package org.artifactory.descriptor.repo.vcs;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * @author Michael Pasternak
 */
public class VcsUrlBuilder {

    /**
     * Formats single resource download URL based on
     * VcsGitProvider url definition
     *
     * @param provider
     * @param user
     * @param repository
     * @param file
     * @param branch
     *
     * @return formatted url
     */
    public static String resourceDownloadUrl(VcsGitProvider provider, String user, String repository, String file, String branch) {
        if(!StringUtils.isBlank(provider.getResourceDownloadUrl())) {
            String[] values = new String[] {user, repository, file, branch};
            return MessageFormat.format(provider.getResourceDownloadUrl(), values);
        }
        return null;
    }

    /**
     * Formats single resource download URL based on
     * VcsGitProvider url definition
     *
     * @param provider
     * @param user
     * @param repository
     * @param release
     *
     * @return formatted url
     */
    public static String releaseDownloadUrl(VcsGitProvider provider, String user, String repository, String release, String ballType) {
        if (!StringUtils.isBlank(provider.getReleaseDownloadUrl())) {
            String[] values = new String[]{user, repository, ballType, release};
            return MessageFormat.format(provider.getReleaseDownloadUrl(), values);
        }
        return null;
    }

    /**
     * Formats repository download URL based on
     * VcsGitProvider url definition
     *
     * @param urlTemplate
     * @param gitOrg
     * @param gitRepo
     * @param version
     * @param fileExt
     *
     * @return formatted url
     */
    public static String repositoryDownloadUrl(String urlTemplate, String gitOrg, String gitRepo, String version, String fileExt) {
        if(!StringUtils.isBlank(urlTemplate)) {
            String[] values = new String[] {gitOrg, gitRepo, version, fileExt};
            return MessageFormat.format(urlTemplate, values);
        }
        return null;
    }
}
