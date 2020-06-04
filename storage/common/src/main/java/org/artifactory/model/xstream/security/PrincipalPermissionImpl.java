package org.artifactory.model.xstream.security;

import org.artifactory.security.AceInfo;
import org.artifactory.security.PermissionTarget;
import org.artifactory.security.PrincipalPermission;

/**
 * Class represent specific user/group permission.
 *
 * @author Shay Bagants
 */
public class PrincipalPermissionImpl<T extends PermissionTarget> implements PrincipalPermission<T> {

    private T permissionTarget;
    private AceInfo aceInfo;

    public PrincipalPermissionImpl(T permissionTarget, AceInfo aceInfo) {
        this.permissionTarget = permissionTarget;
        this.aceInfo = aceInfo;
    }

    @Override
    public T getPermissionTarget() {
        return permissionTarget;
    }

    @Override
    public AceInfo getAce() {
        return aceInfo;
    }
}