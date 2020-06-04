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

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestSpecialFields;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Chen Keinan
 */
public class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper BASE_OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper OBJECT_MAPPER_NO_DEFAULT_MAPPING = new ObjectMapper() {{
        setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        disableDefaultTyping();
    }};
    private static final ObjectMapper OBJECT_MAPPER_NO_SPECIAL_EXCLUDED = new ObjectMapper() {{
        setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        setFilters(new SimpleFilterProvider()
                .addFilter("exclude fields",
                        SimpleBeanPropertyFilter.serializeAllExcept(new HashSet<>(0))));
    }};

    private static final Map<Class, ObjectMapper> SPECIAL_OBJECT_MAPPERS = new HashMap<>();

    private static <T extends RestModel> ObjectMapper getObjectMapper(Class<T> restModelClass) {
        ObjectMapper result = SPECIAL_OBJECT_MAPPERS.get(restModelClass);
        if (result == null) {
            synchronized (SPECIAL_OBJECT_MAPPERS) {
                result = new ObjectMapper() {{
                    IgnoreSpecialFields ignoreSpecialFields = restModelClass.getAnnotation(IgnoreSpecialFields.class);
                    setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
                    setFilters(new SimpleFilterProvider()
                            .addFilter("exclude fields",
                                    SimpleBeanPropertyFilter.serializeAllExcept((ignoreSpecialFields.value()))));
                }};
                SPECIAL_OBJECT_MAPPERS.put(restModelClass, result);
            }
        }
        return result;
    }

    /**
     * json to String exclude null data
     *
     * @param model - model data to String
     * @return - model data with json format
     */
    public static String jsonToString(RestModel model) {
        String jasonString = null;
        try {
            jasonString = OBJECT_MAPPER_NO_DEFAULT_MAPPING.writeValueAsString(model);
        } catch (IOException e) {
            // TODO: [by fsi] Why ignoring this?
            log.error("JSON write of " + model.getClass() + " due to: " + e.getMessage(), e);
        }
        return jasonString;
    }

    /**
     * jsonToString exclude null data end edit fields
     *
     * @param model - model data to String
     * @return - model data with json format
     */
    public static String jsonToStringIgnoreSpecialFields(RestModel model) {
        if (model instanceof RestSpecialFields) {
            if (!((RestSpecialFields) model).ignoreSpecialFields()) {
                try {
                    return OBJECT_MAPPER_NO_SPECIAL_EXCLUDED.writeValueAsString(model);
                } catch (IOException e) {
                    // TODO: [by fsi] Why ignoring this?
                    log.error("JSON write of " + model.getClass() + " due to: " + e.getMessage(), e);
                    return null;
                }
            }
        }
        try {
            return getObjectMapper(model.getClass()).writeValueAsString(model);
        } catch (IOException e) {
            // TODO: [by fsi] Why ignoring this?
            log.error("JSON write of " + model.getClass() + " due to: " + e.getMessage(), e);
            return null;
        }
    }

    public static <T extends RestModel> T mapDataToModel(String data, Class<T> valueType) throws IOException {
        return BASE_OBJECT_MAPPER.readValue(data, valueType);
    }
}
