package org.artifactory.security.permissions;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Omri Ziv
 */
@Data
@NoArgsConstructor
@Builder
public class SecurityEntityPermissionTargetModel {

    private String name;
    private SecurityEntityRepoPermissionTargetModel repo;
    private SecurityEntityRepoPermissionTargetModel build;
    private SecurityEntityRepoPermissionTargetModel releaseBundle;

}
