package org.artifactory.addon.xray;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArtifactXrayDownloadStatus {

    public static final ArtifactXrayDownloadStatus VALID = new ArtifactXrayDownloadStatus(false, null);

    /** Xray sets this on artifacts that should be blocked - depending on it's own watch for this repo\path */
    private boolean blocked;
    private String blockReason;
}
