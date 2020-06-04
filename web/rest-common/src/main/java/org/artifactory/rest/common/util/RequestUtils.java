package org.artifactory.rest.common.util;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author haims
 */
public class RequestUtils {

    public static HashMap<String, List<String>> getFormParameters(ArtifactoryRestRequest request) {
        Map<String, String[]> parameterMap = request.getServletRequest().getParameterMap();
        HashMap<String, List<String>> formParameters = new HashMap<>();
        for (Map.Entry<String, String[]> x: parameterMap.entrySet()) {
            formParameters.put(x.getKey(), Arrays.asList(x.getValue()));
        }
        return formParameters;
    }
}
