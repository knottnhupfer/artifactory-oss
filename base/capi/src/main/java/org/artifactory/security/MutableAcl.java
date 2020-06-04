package org.artifactory.security;

import java.util.Set;

/**
 * @author Yuval Reches
 */
public interface MutableAcl<T extends PermissionTarget> extends Acl<T> {

    void setPermissionTarget(T permissionTarget);

    void setAces(Set<AceInfo> aces);

    void setUpdatedBy(String updatedBy);

    void setLastUpdated(long lastUpdated);
}
