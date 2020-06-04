package org.artifactory.ui.rest.model.admin.security.permissions.build;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Used in specific permission page, to get up-to-date list of builds that the patterns match
 *
 * @author Yuval Reches
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildPermissionPatternsUIModel extends BaseModel {

    private List<String> includePatterns = Lists.newArrayList();
    private List<String> excludePatterns = Lists.newArrayList();

}
