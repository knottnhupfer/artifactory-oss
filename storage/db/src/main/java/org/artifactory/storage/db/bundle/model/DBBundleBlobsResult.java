package org.artifactory.storage.db.bundle.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the storing_repo from release_bundles table and the data from bundle_blobs table
 *
 * @author Inbar Tal
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DBBundleBlobsResult {
    private String storingRepo;
    private String data;
}
