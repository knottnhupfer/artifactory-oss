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

import org.jfrog.common.config.diff.DiffIgnore;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Map;

/**
 * Enum of the VCS Git repository type.
 *
 * @author Shay Yaakov
 */
@XmlEnum(String.class)
@GenerateDiffFunction(typeMethod = "getType")
public enum VcsGitProvider {

    @XmlEnumValue("github")
    GITHUB("github", new VcsGitProviderConfiguration()),

    @XmlEnumValue("bitbucket")
    BITBUCKET("bitbucket", new VcsBitbucketProviderConfiguration()),

    @XmlEnumValue("stash")
    STASH("stash", new VcsStashProviderConfiguration()),

    @XmlEnumValue("oldstash")
    OLDSTASH("oldstash", new VcsOldStashProviderConfiguration()),

    @XmlEnumValue("artifactory")
    ARTIFACTORY("artifactory", new VcsArtifactoryProviderConfiguration()),

    @XmlEnumValue("custom")
    CUSTOM("custom", new VcsCustomProviderConfiguration());

    VcsGitProvider(String type, VcsProviderConfiguration configuration) {
        this.type = type;
        this.configuration = configuration;
    }

    private final String type;

    @DiffIgnore
    private final VcsProviderConfiguration configuration;

    public String getPrettyText() {
        return configuration.getPrettyText();
    }

    public String getRefsPrefix() {
        return configuration.getRefsPrefix();
    }

    public String getRepositoryDownloadUrl() {
        return configuration.getRepositoryDownloadUrl();
    }

    public String getResourceDownloadUrl() {
        return configuration.getResourceDownloadUrl();
    }

    public String getReleaseDownloadUrl() {
        return configuration.getReleaseDownloadUrl();
    }

    public Map<String, String> getHeaders() {
        return configuration.getHeaders();
    }

    public String getType() {
        return type;
    }
}