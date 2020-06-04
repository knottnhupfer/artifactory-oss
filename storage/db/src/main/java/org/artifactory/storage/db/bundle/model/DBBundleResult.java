package org.artifactory.storage.db.bundle.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the name and the storing_repo from release_bundles table
 *
 * @author Inbar Tal
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DBBundleResult {
    private String name;
    private String storingRepo;
}
