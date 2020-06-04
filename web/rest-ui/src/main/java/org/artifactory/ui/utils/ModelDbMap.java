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

package org.artifactory.ui.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Chen Keinan
 */
public class ModelDbMap {

    public static  Map<String,String> getBuildsMap(){
         Map<String,String> propBuildsFieldMap = new HashMap<>();
        propBuildsFieldMap.put("buildName", "build_name");
        propBuildsFieldMap.put("lastBuildTime", "build_time  ");
            propBuildsFieldMap.put("buildNumber","build_number");
            propBuildsFieldMap.put("status","status");
        return propBuildsFieldMap;
    }

    public static  Map<String,String> getModuleMap(){
        Map<String,String> propBuildsFieldMap = new HashMap<>();
        propBuildsFieldMap.put("moduleId","module_name_id");
        propBuildsFieldMap.put("numOfArtifacts","num_of_art");
        propBuildsFieldMap.put("numOfDependencies","num_of_dep");
         return propBuildsFieldMap;
    }

    public static Map<String, String> getBuildProps() {
        Map<String, String> propBuildsFieldMap = new HashMap<>();
        propBuildsFieldMap.put("key", "propsKey");
        propBuildsFieldMap.put("value", "propsValue");
        return propBuildsFieldMap;
    }
}
