package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Inbar Tal
 */
@Getter
@Setter
@NoArgsConstructor
public class ReleaseBundlePermissionTargetInfo extends RepoPermissionTargetInfo {

    private List<String> includePatterns = Lists.newArrayList();
    private List<String> excludePatterns = Lists.newArrayList();
}
