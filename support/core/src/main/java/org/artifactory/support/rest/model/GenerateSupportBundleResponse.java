package org.artifactory.support.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.jfrog.support.rest.model.SupportBundleConfig.Artifactory;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateSupportBundleResponse {

    private String id;
    private Artifactory artifactory;
}
