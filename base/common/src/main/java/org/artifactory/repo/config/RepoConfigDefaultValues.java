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

package org.artifactory.repo.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.go.GoGitProvider;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.descriptor.repo.vcs.VcsType;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.artifactory.common.ConstantValues.debianDefaultArchitectures;

/**
 * @author Dan Feldman
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class RepoConfigDefaultValues {

    //local basic
    public static final String DEFAULT_INCLUDES_PATTERN = "**/*";
    public static final String DEFAULT_REPO_LAYOUT = "simple-default";

    //local advanced
    public static final boolean DEFAULT_BLACKED_OUT = false;
    public static final boolean DEFAULT_ALLOW_CONTENT_BROWSING = false;

    //remote basic
    public static final boolean DEFAULT_OFFLINE = false;
    public static final ContentSynchronisation DEFAULT_DELEGATION_CONTEXT = null;

    //remote advanced
    public static final boolean DEFAULT_HARD_FAIL = false;
    public static final boolean DEFAULT_STORE_ARTIFACTS_LOCALLY = true;
    public static final boolean DEFAULT_SYNC_PROPERTIES = false;
    public static final boolean DEFAULT_SHARE_CONFIG = false;
    public static final boolean DEFAULT_BLOCK_MISMATCHING_MIME_TYPES = true;
    public static final boolean DEFAULT_BYPASS_HEAD_REQUESTS = false;

    public static final boolean DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE = true;
    public static final boolean DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE = false;

    //network
    public static final int DEFAULT_SOCKET_TIMEOUT = 15000;
    public static final boolean DEFAULT_LENIENENT_HOST_AUTH = false;
    public static final boolean DEFAULT_COOKIE_MANAGEMENT = false;

    //cache
    public static final int DEFAULT_KEEP_UNUSED_ARTIFACTS = 0;
    public static final long DEFAULT_RETRIEVAL_CACHE_PERIOD = 7200;
    public static final long DEFAULT_ASSUMED_OFFLINE = 300;
    public static final long DEFAULT_MISSED_RETRIEVAL_PERIOD = 1800;

    //replication
    public static final boolean DEFAULT_LOCAL_REPLICATION_ENABLED = true;
    public static final boolean DEFAULT_REMOTE_REPLICATION_ENABLED = false;
    public static final boolean DEFAULT_EVENT_REPLICATION = false;
    public static final boolean DEFAULT_REPLICATION_SYNC_DELETES = false;
    public static final boolean DEFAULT_REPLICATION_SYNC_STATISTICS = false;
    public static final boolean DEFAULT_REPLICATION_CHECK_BINARY_EXISTENCE_IN_FILESTORE = false;

    //virtual
    public static final boolean DEFAULT_VIRTUAL_CAN_RETRIEVE_FROM_REMOTE = false;
    public static final long DEFAULT_VIRTUAL_RETRIEVAL_CACHE_PERIOD = 7200;

    //distribution
    public static final boolean DEFAULT_GPG_SIGN = false;
    public static final boolean DEFAULT_NEW_BINTRAY_REPO_PRIVATE = true;
    public static final boolean DEFAULT_NEW_BINTRAY_REPO_PREMIUM = true;

    //xray
    public static boolean DEFAULT_XRAY_INDEX = false;

    //download redirect
    public static boolean DEFAULT_DOWNLOAD_REDIRECT = false;

    //bower
    public static final String DEFAULT_BOWER_REGISTRY = "https://registry.bower.io";

    //CocoaPods
    public static final String DEFAULT_PODS_SPECS_REPO = "https://github.com/CocoaPods/Specs";

    //Composer
    public static final String DEFAULT_COMPOSER_REGISTRY = "https://packagist.org";

    //debian
    public static final boolean DEFAULT_DEB_TRIVIAL_LAYOUT = false;
    public static final String  DEFAULT_DEB_ARCHITECTURES = debianDefaultArchitectures.getString();

    //docker
    public static final DockerApiVersion DEFAULT_DOCKER_API_VER = DockerApiVersion.V2;
    public static final boolean DEFAULT_TOKEN_AUTH = true;
    public static final int DEFAULT_MAX_UNIQUE_TAGS = 0;
    public static final boolean DEFAULT_DOCKER_BLOCK_PUSHING_SCHEMA1 = true;
    public static final boolean DEFAULT_DOCKER_VIRTUAL_RESOLVE_TAGS_BY_TIMESTAMP = false;

    //maven / gradle / ivy / sbt
    public static final int DEFAULT_MAX_UNIQUE_SNAPSHOTS = 0;
    public static final boolean DEFAULT_HANDLE_RELEASES = true;
    public static final boolean DEFAULT_HANDLE_SNAPSHOTS = true;
    public static final boolean DEFAULT_SUPPRESS_POM_CHECKS = true;
    public static final boolean DEFAULT_SUPPRESS_POM_CHECKS_MAVEN = false;
    public static final SnapshotVersionBehavior DEFAULT_SNAPSHOT_BEHAVIOR = SnapshotVersionBehavior.UNIQUE;
    public static final LocalRepoChecksumPolicyType DEFAULT_CHECKSUM_POLICY = LocalRepoChecksumPolicyType.CLIENT;
    public static final boolean DEFAULT_EAGERLY_FETCH_JARS = false;
    public static final boolean DEFAULT_EAGERLY_FETCH_SOURCES = false;
    public static final ChecksumPolicyType DEFAULT_REMOTE_CHECKSUM_POLICY = ChecksumPolicyType.GEN_IF_ABSENT;
    public static final boolean DEFAULT_REJECT_INVALID_JARS = false;
    public static final PomCleanupPolicy DEFAULT_POM_CLEANUP_POLICY = PomCleanupPolicy.discard_active_reference;
    public static final boolean DEFAULT_FORCE_MAVEN_AUTH = false;

    //nuget
    public static final String DEFAULT_NUGET_FEED_PATH = "api/v2";
    public static final String DEFAULT_NUGET_DOWNLOAD_PATH = "api/v2/package";
    public static final boolean DEFAULT_FORCE_NUGET_AUTH = false;
    public static final String DEFAULT_NUGET_V3_URL = "https://api.nuget.org/v3/index.json";

    // PyPI
    public static final String DEFAULT_PYPI_REGISTRY = "https://pypi.org";
    public static final String DEFAULT_PYPI_SUFFIX = "simple";

    //vcs
    public static final VcsType DEFAULT_VCS_TYPE = VcsType.GIT;
    public static final VcsGitProvider DEFAULT_GIT_PROVIDER = VcsGitProvider.GITHUB;
    public static final VcsGitConfiguration DEFAULT_VCS_GIT_CONFIG = new VcsGitConfiguration();

    //yum
    public static final int DEFAULT_YUM_METADATA_DEPTH = 0;
    public static final String DEFAULT_YUM_GROUPFILE_NAME = "groups.xml";
    public static final boolean DEFAULT_YUM_AUTO_CALCULATE = true;
    public static final boolean DEFAULT_ENABLE_FILELIST_INDEXING = false;

    //go
    public static final List<String> DEFAULT_GO_METADATA_URLS =
            ImmutableList.of("**/" + GoGitProvider.Github.getPrefix() + "**",
                            "**/" + GoGitProvider.GoGoogleSource.getPrefix() + "**",
                            "**/" + GoGitProvider.Gopkgin.getPrefix() + "**",
                            "**/" + GoGitProvider.Golang.getPrefix() + "**",
                            "**/" + GoGitProvider.K8s.getPrefix() + "**");

    // example repository key
    public static final String EXAMPLE_REPO_KEY = "example-repo-local";

    public static List<String> getDefaultDebianPackagesFileFormats() {
        return Lists.newArrayList("bz2");
    }

    public static final Set<String> DEFAULT_URLS_LIST = Arrays.stream(DefaultUrl.values())
            .map(DefaultUrl::getUrl)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());

    // default remote registry urls
    public enum DefaultUrl {
        BOWER(RepoType.Bower, Constants.GITHUB_URL),
        COCOAPODS(RepoType.CocoaPods, Constants.GITHUB_URL),
        COMPOSER(RepoType.Composer, Constants.GITHUB_URL),
        CHEF(RepoType.Chef, "https://supermarket.chef.io"),
        CONDA(RepoType.Conda, "https://repo.anaconda.com/pkgs/main"),
        CONAN(RepoType.Conan, "https://conan.bintray.com"),
        CRAN(RepoType.CRAN, "https://cran.r-project.org/"),
        DEBIAN(RepoType.Debian, "http://archive.ubuntu.com/ubuntu/"),
        DOCKER(RepoType.Docker, "https://registry-1.docker.io/"),
        GEMS(RepoType.Gems, "https://rubygems.org/"),
        GENERIC(RepoType.Generic, StringUtils.EMPTY),
        GITLFS(RepoType.GitLfs, StringUtils.EMPTY),
        GO(RepoType.Go, "https://gocenter.io/"),
        GRADLE(RepoType.Gradle, Constants.JCENTER_URL),
        HELM(RepoType.Helm, "https://storage.googleapis.com/kubernetes-charts"),
        IVY(RepoType.Ivy, Constants.JCENTER_URL),
        MAVEN(RepoType.Maven, Constants.JCENTER_URL),
        NPM(RepoType.Npm, "https://registry.npmjs.org"),
        NUGET(RepoType.NuGet, "https://www.nuget.org/"),
        OPKG(RepoType.Opkg, "https://downloads.openwrt.org/chaos_calmer/15.05.1/"),
        P2(RepoType.P2, Constants.JCENTER_URL),
        PUPPET(RepoType.Puppet, "https://forgeapi.puppetlabs.com/"),
        PYPI(RepoType.Pypi, "https://files.pythonhosted.org"),
        RPM(RepoType.YUM, "http://mirror.centos.org/centos/"),
        SBT(RepoType.SBT, Constants.JCENTER_URL),
        VAGRANT(RepoType.Vagrant, StringUtils.EMPTY),
        VCS(RepoType.VCS, Constants.GITHUB_URL);

        private RepoType type;
        private String url;

        DefaultUrl(RepoType type, String url) {
            this.type = type;
            this.url = url;
        }

        RepoType getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        public static String urlByType(RepoType type) {
            return Arrays.stream(values())
                    .filter(defaultUrl -> defaultUrl.type.equals(type))
                    .findAny()
                    .map(defaultUrl -> defaultUrl.url)
                    .orElse(StringUtils.EMPTY);
        }

        private static class Constants {
            static final String GITHUB_URL = "https://github.com/";
            static final String JCENTER_URL = "https://jcenter.bintray.com";
        }
    }


}