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

package org.artifactory.rest.resource.system;

import com.google.common.collect.Maps;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.constant.ArtifactRestConstants;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.conversion.version.v213.V213ConversionFailFunction;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.version.converter.DBSqlConverter;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.storage.fs.service.PropertiesService;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.*;

/**
 * @author Shay Bagants
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN, HaRestConstants.ROLE_HA})
public class PsqlResource {
    private static final Logger log = LoggerFactory.getLogger(PsqlResource.class);

    private PropertiesService propertiesService;
    private ConfigsService configsService;

    PsqlResource(PropertiesService propertiesService, ConfigsService configsService) {
        this.propertiesService = propertiesService;
        this.configsService = configsService;
    }

    @GET
    @Path("properties/listInvalidProperties")
    @Produces("application/json")
    public Response listInvalidProperties() {
        Map<String, List<String>> allLongProps = getAllLongProps();
        PostgresqlPropTrimAndIndexStatusModel responseModel = new PostgresqlPropTrimAndIndexStatusModel();
        responseModel.setProperties(allLongProps);
        return Response.ok().entity(responseModel).build();
    }

    @POST
    @Path("properties/fix")
    @Produces("application/json")
    public Response trimValuesAndFixIndex(@QueryParam(ArtifactRestConstants.PARAM_DRY_RUN) int dryRun) {
        if (!isPostgresqlDb()) {
            PostgresqlPropTrimAndIndexStatusModel responseModel = new PostgresqlPropTrimAndIndexStatusModel(null,
                    "Resource is available for PostgreSQL database only", null);
            responseModel.setError("Resource is available for PostgreSQL database only");
            return Response.status(BAD_REQUEST.getStatusCode()).entity(responseModel).build();
        }
        Map<String, List<String>> longProps = getAllLongProps();
        Map<String, List<String>> expectedTrimmedProps = Maps.newHashMap();
        // populate the expectedTrimmedProps map like the properties were trimmedtrimmed
        simulateTrimPropValues(longProps, expectedTrimmedProps);
        return handleAndBuildResponse(dryRun, expectedTrimmedProps);
    }

    /**
     * Handle psql property values fix request. on dryrun, simulate how it should be, otherwise, trimming property
     * values to the max allowed value length and fixing the node_props indexes. Both applies for Postgresql only.
     */
    private Response handleAndBuildResponse(@QueryParam(ArtifactRestConstants.PARAM_DRY_RUN) int dryRun,
            Map<String, List<String>> expectedTrimmedProps) {
        if (dryRun == 0) {
            int trimmedPropValues = propertiesService.trimPropertyValuesToAllowedLength();
            boolean indexFixed = fixIndex();
            PostgresqlPropTrimAndIndexStatusModel responseModel = new PostgresqlPropTrimAndIndexStatusModel();
            responseModel.setIndexFixed(indexFixed);
            if (!indexFixed) {
                String errorMsg = "Failed to fix index on 'node_props' table. " + (isPostgresqlDb() ?
                        "See artifactory.log for further details" : "This resource is intended for PostgreSQL only");
                responseModel.setError(errorMsg);
            }
            if (trimmedPropValues > 0) {
                responseModel.setProperties(expectedTrimmedProps);
            } else {
                log.info("No properties should be trimmed");
            }
            return Response.status(indexFixed ? OK.getStatusCode() : INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(responseModel).build();
        }
        PostgresqlPropTrimAndIndexStatusModel responseModel = new PostgresqlPropTrimAndIndexStatusModel();
        responseModel.setProperties(expectedTrimmedProps);
        responseModel.setIndexFixed(false);
        return Response.ok().entity(responseModel).build();
    }

    boolean fixIndex() {
        if (isPostgresqlDb()) {
            try {
                DBSqlConverter converter = new DBSqlConverter("v213_node_props_index");
                DbType dbType = getDbType();
                converter.convert(jdbcHelper(), dbType);
                if (configsService.hasConfig(V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER)) {
                    configsService.deleteConfig(V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER);
                }
                return true;
            } catch (Exception e) {
                log.error("Failed to perform conversion. ", e);
            }
        }
        return false;
    }

    private boolean isPostgresqlDb() {
        return getDbType() == DbType.POSTGRESQL;
    }

    private DbType getDbType() {
        return ContextHelper.get().beanForType(ArtifactoryDbProperties.class).getDbType();
    }

    private JdbcHelper jdbcHelper() {
        return ContextHelper.get().beanForType(JdbcHelper.class);
    }

    private String trimPropIfNeeded(String val) {
        int maxAllowedPropSize = getMaxAllowedPropSize();
        return val.length() > maxAllowedPropSize ? val.substring(0, getMaxAllowedPropSize()) : val;
    }

    private Map<String, List<String>> getAllLongProps() {
        return propertiesService.getAllPropsLongerThan(getMaxAllowedPropSize());
    }

    private void simulateTrimPropValues(Map<String, List<String>> longProps,
            Map<String, List<String>> expectedTrimmedProps) {
        longProps.forEach((key, valuesList) -> {
            List<String> trimmedValued = valuesList.stream()
                    .map(this::trimPropIfNeeded)
                    .collect(Collectors.toList());
            expectedTrimmedProps.put(key, trimmedValued);
        });
    }

    private int getMaxAllowedPropSize() {
        return ConstantValues.dbPostgresPropertyValueMaxSize.getInt();
    }
}
