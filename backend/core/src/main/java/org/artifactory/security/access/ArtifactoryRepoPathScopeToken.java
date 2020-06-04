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

package org.artifactory.security.access;

import org.artifactory.io.checksum.ChecksumUtils;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scope token for allowing read permissions on a specific repo path
 *
 * @author Nadav Yogev
 */
public class ArtifactoryRepoPathScopeToken {

    static final String PATH_CHECKSUM_PREFIX = "path.checksum";
    private static final String PATH_PREFIX = "(?<prefix>path|" + PATH_CHECKSUM_PREFIX + "):";
    private static final String REPO_PATHS_PATTERN = "(?<repoPath>[\\w\\d/.-]+)";
    static final String READ_PERMISSION = "read";
    private static final String REPO_PERMISSIONS = ":(?<permissions>"+READ_PERMISSION+")";
    // Example: - path:repo/path/artifact:read
    //          - path.checksum:f781a2...:read (in case the path is too long)
    static final Pattern SCOPE_ARTIFACTORY_REPO_PATH_PATTERN = Pattern
            .compile(PATH_PREFIX + REPO_PATHS_PATTERN + REPO_PERMISSIONS);

    /**
     * Check whether a scope token is a valid artifactory repo path scope token
     *
     * @param scopeToken the scope token to parse
     */
    public static boolean accepts(String scopeToken) {
        return scopeToken != null && SCOPE_ARTIFACTORY_REPO_PATH_PATTERN.matcher(scopeToken).matches();
    }

    public static ArtifactoryRepoPathScopeToken parse(String scopeToken) {
        if (!accepts(scopeToken)) {
            throw new IllegalArgumentException("Not a valid artifactory repo path scope token:" + scopeToken);
        }
        Matcher matcher = SCOPE_ARTIFACTORY_REPO_PATH_PATTERN.matcher(scopeToken);
        if (matcher.find()) {
            boolean checksumPath = PATH_CHECKSUM_PREFIX.equals(matcher.group("prefix"));
            String repoPath = matcher.group("repoPath");
            String repoPathChecksum = !checksumPath ? ChecksumUtils.calculateSha1(new ByteArrayInputStream(repoPath.getBytes())) : repoPath;
            String permissions = matcher.group("permissions");
            return new ArtifactoryRepoPathScopeToken(repoPathChecksum, permissions, checksumPath);
        }
        throw new IllegalArgumentException("Not a valid artifactory repo path scope token:" + scopeToken);
    }


    private final String repoPathChecksum;
    private final String permissions;
    private final boolean checksumPath;

    private ArtifactoryRepoPathScopeToken(String repoPathChecksum, String permissions, boolean checksumPath) {
        this.repoPathChecksum = repoPathChecksum;
        this.permissions = permissions;
        this.checksumPath = checksumPath;
    }

    /**
     * Get the group names in this scope token
     */
    @Nonnull
    String getRepoPathChecksum() {
        return repoPathChecksum;
    }

    @Nonnull
    public String getPermissions() {
        return permissions;
    }

    boolean isChecksumPath() {
        return checksumPath;
    }

    /**
     * Get the formatted scope token
     */
    @Nonnull
    public String getScopeToken() {
        return PATH_PREFIX + repoPathChecksum + ":" + permissions;
    }

    @Override
    public String toString() {
        return getScopeToken();
    }

    public static String getPath(String scopeToken) {
        Matcher matcher = SCOPE_ARTIFACTORY_REPO_PATH_PATTERN.matcher(scopeToken);
        if (matcher.find()) {
            return matcher.group("repoPath");
        }
        return null;
    }

    public static String getPermissions(String scopeToken) {
        return parse(scopeToken).getPermissions();
    }
}
