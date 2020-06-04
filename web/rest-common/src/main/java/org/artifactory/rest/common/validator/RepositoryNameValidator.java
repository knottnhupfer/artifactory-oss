package org.artifactory.rest.common.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.repo.SupportBundleRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.repo.ReleaseBundlesRepositoryConfigurationImpl;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.util.UiRequestUtils;
import org.jdom2.Verifier;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

public class RepositoryNameValidator {

    private static final int REPO_KEY_MAX_LENGTH = 64;
    private static final List<Character> forbiddenChars = Lists
            .newArrayList('/', '\\', ':', '|', '?', '*', '"', '<', '>');

    public static void validateRepoName(String repoKey, String repoType, CentralConfigService centralConfig) throws RepoConfigException {
        if (isBlank(repoKey)) {
            throw new RepoConfigException("Repository key cannot be empty", SC_BAD_REQUEST);
        }
        //Local repos limited at 64, remote repos append '-cache' to cache repos which is 6 chars.
        int maxLength = "remote".equals(repoType) ? (REPO_KEY_MAX_LENGTH - 6) : REPO_KEY_MAX_LENGTH;
        if (StringUtils.length(repoKey) > maxLength) {
            throw new RepoConfigException("Repository key exceeds maximum length (" + maxLength + ") chars.",
                    SC_BAD_REQUEST);
        }
        if (UiRequestUtils.isReservedName(repoKey)) {
            throw new RepoConfigException("Repository key '" + repoKey + "' is a reserved name", SC_BAD_REQUEST);
        }
        if (repoKey.equals(".") || repoKey.equals("..") || repoKey.equals("&")) {
            throw new RepoConfigException("Invalid Repository key", SC_BAD_REQUEST);
        }
        if(repoKey.chars().anyMatch(chr -> forbiddenChars.contains((char) chr))) {
            throw new RepoConfigException("Illegal Repository key : '/,\\,:,|,?,<,>,*,\"' is not allowed",
                    SC_BAD_REQUEST);
        }
        String error = Verifier.checkXMLName(repoKey);
        if (StringUtils.isNotBlank(error)) {
            throw new RepoConfigException("Repository key contains illegal character", SC_BAD_REQUEST);
        }

        if (!centralConfig.getMutableDescriptor().isKeyAvailable(repoKey)) {
            throw new RepoConfigException("Case insensitive repository key already exists", SC_BAD_REQUEST);
        }

        if (SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME.equals(repoKey)) {
            throw new RepoConfigException(SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME + " is a reserved repository key", SC_BAD_REQUEST);
        }

        if (ReleaseBundlesRepoDescriptor.RELEASE_BUNDLE_DEFAULT_REPO.equals(repoKey) && !ReleaseBundlesRepositoryConfigurationImpl.TYPE.equals(repoType)) {
            throw new RepoConfigException(ReleaseBundlesRepoDescriptor.RELEASE_BUNDLE_DEFAULT_REPO + " is a reserved repository key", SC_BAD_REQUEST);
        }
    }
}
