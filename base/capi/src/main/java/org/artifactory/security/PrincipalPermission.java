package org.artifactory.security;

/**
 * Interface represent specific user/group permission.
 *
 * @author Shay Bagants
 */
public interface PrincipalPermission<T extends PermissionTarget> {

    /**
     * @return original permission target with it's repo's, include and exclude patterns
     */
    T getPermissionTarget();

    /**
     * @return principal ace with it's mask and principal type
     */
    AceInfo getAce();

}
