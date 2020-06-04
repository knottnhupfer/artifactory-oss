package org.artifactory.api.rest.distribution.bundle.models;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class ReleaseBundleSearchModel {
    private List<String> versions = Lists.newArrayList();

}
