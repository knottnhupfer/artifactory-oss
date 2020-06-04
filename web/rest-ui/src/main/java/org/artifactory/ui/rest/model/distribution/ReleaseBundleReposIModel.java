package org.artifactory.ui.rest.model.distribution;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Inbar Tal
 */
@NoArgsConstructor
@Data
public class ReleaseBundleReposIModel {
    private List<String> repositories = Lists.newArrayList();
}
