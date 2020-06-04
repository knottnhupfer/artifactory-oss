package org.artifactory.api.rest.distribution.bundle.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.bundle.BundleTransactionStatus;
import org.artifactory.bundle.BundleType;

/**
 * Lightweight type of org.artifactory.storage.db.bundle.model.DBArtifactsBundle.
 *
 * @author Inbar Tal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactsBundleModel {
    long id;
    String name;
    String version;
    BundleTransactionStatus status;
    BundleType type;
}
