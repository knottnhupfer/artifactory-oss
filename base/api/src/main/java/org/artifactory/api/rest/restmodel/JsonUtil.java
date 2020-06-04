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

package org.artifactory.api.rest.restmodel;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Chen Keinan
 */
public class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * json to String exclude null data
     *
     * @param model - model data to String
     * @return - model data with json format
     */
    public static String jsonToString(Object model) {
        //        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.disableDefaultTyping();
        String jasonString = null;
        try {
            jasonString = mapper.writeValueAsString(model);
        } catch (IOException e) {
            log.error(e.toString());
        }
        return jasonString;
    }


    /**
     * map input stream to model
     *
     * @param data      - input stream
     * @param valueType - class type
     * @param <T>       -
     * @return
     * @throws IOException
     */
    public static <T> T mapDataToModel(InputStream data, Class<T> valueType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        T model = mapper.readValue(data, valueType);
        return model;
    }
}
