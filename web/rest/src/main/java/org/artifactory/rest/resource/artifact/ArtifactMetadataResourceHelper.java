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
package org.artifactory.rest.resource.artifact;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.PropertiesAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.property.Property;
import org.artifactory.fs.StatsInfo;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.model.xstream.fs.StatsImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.exception.AuthorizationRestException;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.storage.fs.service.StatsService;
import org.jfrog.common.JsonMergeException;
import org.jfrog.common.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import static org.jfrog.common.JsonUtils.jsonMerge;

/**
 * @author dudim
 */
@Component
public class ArtifactMetadataResourceHelper {

    private AddonsManager addonsManager;

    private StatsService statsService;

    private AuthorizationService authorizationService;

    private static final Logger log = LoggerFactory.getLogger(ArtifactMetadataResourceHelper.class);

    @Autowired
    public ArtifactMetadataResourceHelper(AddonsManager addonsManager,
            @Qualifier("statsServiceImpl") StatsService statsService,
            AuthorizationService authorizationService) {
        this.addonsManager = addonsManager;
        this.statsService = statsService;
        this.authorizationService = authorizationService;
    }

    void validateUserHasAnnotateAuthorization(RepoPath repoPath) {
        if (!authorizationService.canAnnotate(repoPath)) {
            throw new AuthorizationRestException(String.format(
                    "Request for '%s' is forbidden for user: '%s', You must have annotate permission on this path",
                    repoPath, authorizationService.currentUsername()));
        }
    }

    boolean handleStatistics(JsonNode requestedStats, RepoPath repoPath) {
        StatsInfo packageStats = statsService.getStats(repoPath);
        if (packageStats == null) {
            packageStats = getDefaultStatForPackage(repoPath);
        }
        StatsInfo modifiedStats = jsonMerge(packageStats, requestedStats.toString(), StatsImpl.class, null);
        return statsService.setStats(repoPath, modifiedStats);
    }

    private StatsImpl getDefaultStatForPackage(RepoPath repoPath) {
        StatsImpl statsImpl = new StatsImpl();
        statsImpl.setRepoPath(repoPath.getPath());
        return statsImpl;
    }

    @SuppressWarnings("unchecked")
    boolean handleProperties(JsonNode requestedProps, RepoPath repoPath, String recursiveProperties,
            String atomicProperties) {
        PropertiesImpl properties = (PropertiesImpl) propertiesAddon().getProperties(repoPath);
        Map<String, Object> modifiedPropertiesMap;
        try {
            modifiedPropertiesMap = jsonMerge(convertToMap(properties.entries()), requestedProps.toString(), Map.class,
                    null);
        } catch (JsonMergeException e) {
            log.debug("Error merging properties due: {}", e.getMessage(), e);
            throw new BadRequestException(e.getMessage());
        }
        propertiesAddon().deletePathProperties(repoPath.toPath(), recursiveProperties,
                convertPropertiesToStringList(properties));

        if (!modifiedPropertiesMap.isEmpty()) {
            Response response = propertiesAddon()
                    .savePathProperties(repoPath.toPath(), recursiveProperties,
                            convertToPropertyMap(modifiedPropertiesMap), atomicProperties);
            return response.getStatus() == Response.Status.NO_CONTENT.getStatusCode();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<Property, List<String>> convertToPropertyMap(Map<String, Object> modifiedPropertiesMap) {
        return modifiedPropertiesMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> new Property(entry.getKey()), entry -> {
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        return Lists.newArrayList(value.toString());
                    } else if (value instanceof List) {
                        return (ArrayList) value;
                    } else {
                        throw new BadRequestException(
                                "Value:" + value.toString() + " is not valid, please use string or array of string");
                    }
                }));
    }

    private Map<String, List<String>> convertToMap(Set<Map.Entry<String, String>> properties) {
        Map<String, List<String>> propertyMap = new HashMap<>();
        properties.forEach(entry -> {
            propertyMap.putIfAbsent(entry.getKey(), Lists.newArrayList());
            propertyMap.get(entry.getKey()).add(entry.getValue());
        });

        return propertyMap;
    }

    StringList convertPropertiesToStringList(Properties properties) {
        String packagePropertiesKeys = Arrays.toString(properties.keys().toArray());
        packagePropertiesKeys = packagePropertiesKeys.substring(1, packagePropertiesKeys.length() - 1);
        return new StringList(packagePropertiesKeys);
    }

    PropertiesAddon propertiesAddon() {
        return addonsManager.addonByType(PropertiesAddon.class);
    }
}
