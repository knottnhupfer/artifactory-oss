package org.artifactory.api.rest.distribution.bundle.models;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Inbar Tal
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BundlesNamesResponse {
    private List<String> bundles = Lists.newArrayList();
}
