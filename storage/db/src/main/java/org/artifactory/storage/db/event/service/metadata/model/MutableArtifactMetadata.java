package org.artifactory.storage.db.event.service.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Uriah Levy
 */
@Data
@AllArgsConstructor
public class MutableArtifactMetadata {
    private String artifactName;
    private String artifactMimeType;
    private long artifactLength;
    private String md5;
    private String sha1;
    private String sha256;
    private boolean lead;
}
