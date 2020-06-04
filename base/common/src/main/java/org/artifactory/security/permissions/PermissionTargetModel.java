package org.artifactory.security.permissions;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yuval Reches
 */
@Data
@NoArgsConstructor
public class PermissionTargetModel {
    private String name;
    private RepoPermissionTargetModel repo;
    private RepoPermissionTargetModel build;
    private RepoPermissionTargetModel releaseBundle;
}
