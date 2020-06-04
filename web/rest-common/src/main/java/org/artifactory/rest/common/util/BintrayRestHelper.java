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

package org.artifactory.rest.common.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.jackson.JacksonFactory;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.StatusEntry;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.repo.RepoPathFactory;
import org.codehaus.jackson.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.List;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;

/**
 * Helper class containing shared functions for bintray rest endpoints in Artifactory
 *
 * @author Dan Feldman
 */
public class BintrayRestHelper {
    private static final Logger log = LoggerFactory.getLogger(BintrayRestHelper.class);

    private static final String ERR_FILED_NAME = "Errors";
    private static final String WARN_FILED_NAME = "Warnings";

    public static boolean isPushToBintrayAllowed(@Nullable BasicStatusHolder status, @Nullable String distRepoKey) {
        boolean userCanDeploy = userCanDeploy(distRepoKey);
        if (!validUserForOperation() || !userCanDeploy) {
            UserGroupService userGroupService = ContextHelper.get().beanForType(UserGroupService.class);
            if (status != null) {
                status.error(String.format(
                        "Invalid user for operation - you do not have the required permission, user: '%s'",
                        userGroupService.currentUser().getUsername()), SC_FORBIDDEN, log);
            }
            return false;
        }
        if (ConstantValues.bintrayUIHideUploads.getBoolean()) {
            if (status != null) {
                status.error("Your system administrator has disabled uploads to Bintray", SC_FORBIDDEN, log);
            }
            return false;
        }
        CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);
        boolean offlineMode = centralConfigService.getDescriptor().isOfflineMode();
        if (offlineMode) {
            if (status != null) {
                status.error("Artifactory is in global offline mode", SC_FORBIDDEN, log);
            }
            return false;
        }
        return true;
    }

    /**
     * Used by the UI builds pane
     */
    public static boolean userCanDistributeBuild() {
        AuthorizationService authService = ContextHelper.get().beanForType(AuthorizationService.class);
        boolean canDeployToDist = ContextHelper.get().beanForType(RepositoryService.class)
                .getDistributionRepoDescriptors().stream()
                .map(RepoDescriptor::getKey)
                .filter(repoKey -> authService.canDeploy(RepoPathFactory.create(repoKey, ".")))
                .findAny()
                .isPresent();
        return canDeployToDist && validUserForOperation();
    }

    private static boolean validUserForOperation() {
        AuthorizationService authorizationService = ContextHelper.get().beanForType(AuthorizationService.class);
        UserGroupService userGroupService = ContextHelper.get().beanForType(UserGroupService.class);
        boolean userExists = !userGroupService.currentUser().isTransientUser();
        boolean anonymousUser = authorizationService.isAnonymous();
        return userExists && !anonymousUser;
    }

    private static boolean userCanDeploy(@Nullable String distRepoKey) {
        boolean userCanDeploy;
        AuthorizationService authService = ContextHelper.get().beanForType(AuthorizationService.class);
        if (StringUtils.isNotBlank(distRepoKey)) {
            userCanDeploy = authService.canDeploy(RepoPathFactory.create(distRepoKey, ""));
        } else {
            userCanDeploy = authService.canDeployToLocalRepository();
        }
        return userCanDeploy;
    }

    public static Response createAggregatedResponse(final BasicStatusHolder status, final String performedOn,
            boolean async) {
        StreamingOutput streamingOutput = outputStream -> {
            JsonGenerator jsonGenerator = JacksonFactory.createJsonGenerator(outputStream);
            jsonGenerator.writeStartObject();
            if (status.hasErrors()) {
                writeEntries(jsonGenerator, ERR_FILED_NAME, status.getErrors());
                if (status.hasWarnings()) {
                    writeEntries(jsonGenerator, WARN_FILED_NAME, status.getWarnings());
                }
            } else {
                String msg = "Pushing " + performedOn + " to Bintray " + (async ? "Scheduled to run " : "finished ")
                        + ((status.hasWarnings() ? "with warnings, view the log for details" : "successfully."));
                jsonGenerator.writeStringField("message", msg);
            }
            jsonGenerator.writeEndObject();
            IOUtils.closeQuietly(jsonGenerator);
        };
        int statusCode = HttpStatus.SC_OK;
        if (status.hasErrors()) {
            statusCode = status.getErrors().size() > 1 ? HttpStatus.SC_CONFLICT : status.getStatusCode();
        }
        return Response.status(statusCode).entity(streamingOutput).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    private static void writeEntries(JsonGenerator jsonGenerator, String fieldName,
            List<StatusEntry> entries) throws IOException {
        jsonGenerator.writeArrayFieldStart(fieldName);
        for (StatusEntry entry : entries) {
            jsonGenerator.writeStartObject();
            if (entry.getStatusCode() != 0 && entry.getStatusCode() != HttpStatus.SC_BAD_REQUEST) {
                jsonGenerator.writeNumberField("status", entry.getStatusCode());
            }
            jsonGenerator.writeStringField("message", entry.getMessage());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
    }
}
