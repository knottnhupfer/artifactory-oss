package org.artifactory.rest.resource.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Uriah Levy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class MetadataServerReindexModel {
    private List<String> paths;
}