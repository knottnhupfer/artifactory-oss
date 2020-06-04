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

package org.artifactory.rest.common.model.distribution;

import org.apache.http.HttpStatus;
import org.artifactory.api.bintray.distribution.Distribution;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayRepoModel;
import org.artifactory.api.jackson.JacksonFactory;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.RestResponse;
import org.codehaus.jackson.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Json format that goes out to both REST and UI is the same - this builder is used to construct it.
 *
 * @author Dan Feldman
 */
public class DistributionResponseBuilder {
    private static final Logger log = LoggerFactory.getLogger(DistributionResponseBuilder.class);

    private DistributionResponseBuilder() {

    }

    public static int getResponseCode(DistributionReporter status) {
        int statusCode = HttpStatus.SC_OK;
        if (status.hasErrors()) {
            statusCode = status.getPathErrors().size() > 1 ? HttpStatus.SC_CONFLICT : status.getStatusCode();
        }
        return statusCode;
    }

    public static String writeResponseBody(DistributionReporter status, String performedOn, boolean async, boolean isDryRun) throws IOException {
        try (StringWriter writer = new StringWriter();
             JsonGenerator jsonGenerator = JacksonFactory.createJsonGenerator(writer)) {
            jsonGenerator.writeStartObject();
            String msg = getGeneralMessage(performedOn, async, isDryRun, status);
            jsonGenerator.writeStringField("message", msg);
            writeProduct(status, jsonGenerator);
            writeDistributedRepos(status, jsonGenerator);
            writeStatusByPaths(status, jsonGenerator);
            writeGeneralStatus(status, jsonGenerator);
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();
            writer.flush();
            return writer.toString();
        }
    }

    private static void writeProduct(DistributionReporter status, JsonGenerator jsonGenerator) throws IOException {
        if (status.getRegisteredProduct() != null) {
            jsonGenerator.writeFieldName("product");
            jsonGenerator.writeObject(status.getRegisteredProduct());
        }
    }

    private static void writeDistributedRepos(DistributionReporter status, JsonGenerator jsonGenerator) throws IOException {
        if (!status.getRegisteredRepos().isEmpty()) {
            jsonGenerator.writeArrayFieldStart("distributed");
            for (BintrayRepoModel repo : status.getRegisteredRepos()) {
                jsonGenerator.writeObject(repo);
            }
            jsonGenerator.writeEndArray();
        }
    }

    private static void writeStatusByPaths(DistributionReporter status, JsonGenerator jsonGenerator) throws IOException {
        if (!status.getPathErrors().isEmpty() || !status.getPathWarnings().isEmpty()) {
            jsonGenerator.writeArrayFieldStart("messagesByPath");
            for (String path : getAllPaths(status)) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("path", path);
                writeEntriesArray(jsonGenerator, "errors", status.getPathErrors().get(path));
                writeEntriesArray(jsonGenerator, "warnings", status.getPathWarnings().get(path));
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }
    }

    /**
     * Paths that have warnings might not have had errors and vise-versa, we want to output all paths
     */
    private static Collection<String> getAllPaths(DistributionReporter status) {
        return Stream.concat(status.getPathErrors().keys().stream(), status.getPathWarnings().keys().stream())
                .collect(Collectors.toSet());
    }

    private static void writeGeneralStatus(DistributionReporter status, JsonGenerator jsonGenerator) throws IOException {
        writeEntriesArray(jsonGenerator, "errors", status.getGeneralErrors().get(DistributionReporter.GENERAL_MSG));
        writeEntriesArray(jsonGenerator, "warnings", status.getGeneralWarnings().get(DistributionReporter.GENERAL_MSG));
    }

    /**
     * Used to write error\warn entries for a specific path or general ones
     */
    private static void writeEntriesArray(JsonGenerator jsonGenerator, String fieldName, Collection<StatusEntry> entries) throws IOException {
        if (!entries.isEmpty()) {
            jsonGenerator.writeArrayFieldStart(fieldName);
            for (StatusEntry entry : entries) {
                jsonGenerator.writeStartObject();
                if (entry.getStatusCode() != 0) {
                    jsonGenerator.writeNumberField("code", entry.getStatusCode());
                }
                jsonGenerator.writeStringField("message", entry.getMessage());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }
    }

    public static void doResponse(RestResponse response, String performedOn, Distribution distribution, DistributionReporter status) {
        if (distribution.isAsync()) {
            String msg = DistributionResponseBuilder.getGeneralMessage(performedOn, distribution.isAsync(),
                    distribution.isDryRun(), status);
            if (status.hasErrors()) {
                response.error(msg).responseCode(HttpStatus.SC_BAD_REQUEST);
            } else {
                response.info(msg);
            }
        } else {
            try {
                response.iModel(DistributionResponseBuilder.writeResponseBody(status, performedOn,
                        distribution.isAsync(), distribution.isDryRun()));
            } catch (IOException ioe) {
                String err = "Error writing response - check the logs for details: ";
                response.error(err + ioe.getMessage());
                log.error(err, ioe);
            }
        }
    }

    private static String getGeneralMessage(String performedOn, boolean async, boolean isDryRun, DistributionReporter status) {
        String msg = isDryRun ? "Dry run for distribution of " : "Distribution of " + performedOn;
        if (status.hasErrors()) {
            msg += " encountered errors: " + status.getLastError().getMessage();
        } else {
            msg += (async ? " Scheduled to run " : " finished ") + (status.hasWarnings() ? "with warnings." : "successfully.");
        }
        return msg;
    }
}
