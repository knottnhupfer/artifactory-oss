package org.artifactory.security.permissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

import static org.artifactory.security.PermissionTarget.ANY_PATH;

/**
 * @author Omri Ziv
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class BasePermissionTargetModel {

    // TODO - Properties representations not supported the same on Jersey REST And iTest.

    @org.codehaus.jackson.annotate.JsonProperty("include-patterns")
    @com.fasterxml.jackson.annotation.JsonProperty("include-patterns")
    private List<String> includePatterns = Collections.singletonList(ANY_PATH);
    @org.codehaus.jackson.annotate.JsonProperty("exclude-patterns")
    @com.fasterxml.jackson.annotation.JsonProperty("exclude-patterns")
    private List<String> excludePatterns = Collections.emptyList();
    private List<String> repositories;

}
