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

package org.artifactory.descriptor.repo;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.descriptor.repo.vcs.VcsUrlBuilder;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yoav Luft
 */
@XmlType(name = "VcsGitType", propOrder = {"provider", "downloadUrl"})
@GenerateDiffFunction
public class VcsGitConfiguration implements Descriptor {

    @XmlElement(name = "provider")
    private VcsGitProvider provider = VcsGitProvider.GITHUB;

    @XmlElement(name = "downloadUrl")
    private String downloadUrl;

    public VcsGitConfiguration() {
    }

    public VcsGitProvider getProvider() {
        return provider;
    }

    public void setProvider(VcsGitProvider provider) {
        this.provider = provider;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * Constructs ResourceDownloadUrl
     *
     * @param user git user
     * @param repository git repository
     * @param file a file to download
     * @param version content branch/tag
     *
     * @return ResourceDownloadUrl
     */
    public String buildResourceDownloadUrl(String user, String repository, String file, String version) {
        return VcsUrlBuilder.resourceDownloadUrl(provider, user, repository, file, version);
    }

    /**
     * Constructs ResourceDownloadUrl
     *
     * @param user git user
     * @param repository git repository
     * @param version content branch/tag
     *
     * @return ResourceDownloadUrl
     */
    public String buildReleaseDownloadUrl(String user, String repository, String version, String ballType) {
        return VcsUrlBuilder.releaseDownloadUrl(provider, user, repository, version, ballType);
    }

    /**
     * Constructs RepositoryDownloadUrl
     *
     * @param gitOrg git user
     * @param gitRepo git repository
     * @param version content branch/tag
     * @param fileExt file ext
     *
     * @return RepositoryDownloadUrl
     */
    public String buildRepositoryDownloadUrl(String gitOrg, String gitRepo, String version, String fileExt) {
        String url = StringUtils.isNotBlank(downloadUrl) ? downloadUrl : provider.getRepositoryDownloadUrl();
        return VcsUrlBuilder.repositoryDownloadUrl(url, gitOrg, gitRepo, version, fileExt);
    }
}
