package org.artifactory.ui.rest.model.distribution;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Inbar Tal
 */
@Data
@NoArgsConstructor
public class ReleaseBundlePermissionIModel {
    private List<String> repositories = Lists.newArrayList();
    private List<String> includePatterns = Lists.newArrayList();
    private List<String> excludePatterns = Lists.newArrayList();
}


