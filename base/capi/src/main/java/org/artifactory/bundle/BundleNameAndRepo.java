package org.artifactory.bundle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the bundle name and storing repository.
 *
 * @author Inbar Tal
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BundleNameAndRepo {
    private String name;
    private String storingRepo;
}
