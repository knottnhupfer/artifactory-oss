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

package org.artifactory.addon.filteredresources;

import org.apache.commons.io.IOUtils;
import org.artifactory.addon.Addon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.fs.ZipEntryRepoResource;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.Request;
import org.artifactory.resource.ResourceStreamHandle;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Filtered resources core functionality interface
 *
 * @author Noam Y. Tenne
 */
public interface FilteredResourcesAddon extends Addon {

    /**
     * Indicates whether this file is marked as a resource that can be filtered
     *
     * @param repoPath Item to check
     * @return True if the file is marked as a filtered resource
     */
    default boolean isFilteredResourceFile(RepoPath repoPath) {
        return false;
    }

    /**
     * Indicates whether this file is marked as a resource that can be filtered based on the file properties given as
     * parameter.
     *
     * @param repoPath Item to check
     * @param props    The item current properties
     * @return True if the file is marked as a filtered resource
     */
    default boolean isFilteredResourceFile(RepoPath repoPath, Properties props) {
        return false;
    }

    /**
     * Returns the file's content after filtering it. Always auto closes the input stream
     *
     * @param request         File request
     * @param fileInfo        File info
     * @param fileInputStream File content input stream. Always auto closed
     * @return Resource of filtered file content
     */
    RepoResource getFilteredResource(Request request, FileInfo fileInfo, InputStream fileInputStream);

    default String filterResource(Request request, Properties contextProperties, Reader reader) throws Exception {
        try {
            return IOUtils.toString(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    default String filterResource(Reader reader, Object model) throws Exception {
        try {
            return IOUtils.toString(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Controls the filtered state of a resource
     *
     * @param repoPath Target item repo path
     * @param filtered True if the item should be marked as filter
     */
    default void toggleResourceFilterState(RepoPath repoPath, boolean filtered) {
    }

    /**
     * Returns resource representing a file inside zip. The method will fail if the input stream doesn't represent valid
     * zip stream.
     *
     * @param request The client request
     * @param info    The zip file info
     * @param stream  The file input stream
     * @return The resource info contained in the zip file. Including size and checksums
     */
    RepoResource getZipResource(Request request, FileInfo info, InputStream stream);

    /**
     * Returns stream handle to the entry inside the zip
     *
     * @param resource The zip resource
     * @param stream   Input stream of a zip
     * @return Stream handle to the entry inside the zip
     * @throws IOException on failure to open the zip stream of to locate the entry
     */
    default ResourceStreamHandle getZipResourceHandle(ZipEntryRepoResource resource, InputStream stream) throws IOException {
        throw new UnsupportedOperationException(
                "Direct resource download from zip requires the Filtered resources add-on.");
    }


    default String getGeneratedSettingsUsernameTemplate() {
        return "${security.getCurrentUsername()}";
    }

    default String getGeneratedSettingsUserCredentialsTemplate(boolean escape) {
        AuthorizationService authorizationService = ContextHelper.get().getAuthorizationService();

        if (authorizationService.isAnonymous() || authorizationService.isTransientUser()) {
            return "";
        }

        StringBuilder credentialsTemplateBuilder = new StringBuilder("${security.get");
        if (escape) {
            credentialsTemplateBuilder.append("Escaped");
        }
        return credentialsTemplateBuilder.append("EncryptedPassword()!\"*** Insert encrypted password here ***\"}")
                .toString();
    }

}
