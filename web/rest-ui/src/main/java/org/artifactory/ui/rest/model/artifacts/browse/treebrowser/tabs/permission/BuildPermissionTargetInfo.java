package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * UI model for specific build Effective Permission tab, "Permission Target" inner tab
 *
 * @author Yuval Reches
 */
@Getter
@Setter
@NoArgsConstructor
public class BuildPermissionTargetInfo extends RepoPermissionTargetInfo {

    private List<String> includePatterns = Lists.newArrayList();
    private List<String> excludePatterns = Lists.newArrayList();

}