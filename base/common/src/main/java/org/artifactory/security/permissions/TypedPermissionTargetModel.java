package org.artifactory.security.permissions;

import org.artifactory.security.PrincipalConfiguration;

/**
 * @author Dan Feldman
 */
public interface TypedPermissionTargetModel {

    PrincipalConfiguration getActions();

}
