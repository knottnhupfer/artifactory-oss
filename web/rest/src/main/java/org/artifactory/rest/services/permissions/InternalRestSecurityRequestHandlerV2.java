package org.artifactory.rest.services.permissions;

import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.security.SearchStringPermissionFilter;
import org.artifactory.rest.common.service.security.permissions.RestSecurityRequestHandlerV2;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.sapi.common.Lock;
import org.artifactory.security.Acl;
import org.artifactory.security.PermissionTargetNaming;
import org.artifactory.security.permissions.PermissionTargetModel;

import java.util.List;

/**
 * Internal use methods that maintain atomicity for crud operations over permission targets
 * (since any permission target show to the user is potentially 3 targets now)
 *
 * @author Dan Feldman
 */
public interface InternalRestSecurityRequestHandlerV2 extends RestSecurityRequestHandlerV2 {

    @Lock
    void createAcl(Acl... acls) throws BadRequestException;

    @Lock
    void updateAcl(List<Acl> toUpdate, List<Acl> toDelete, List<Acl> toAdd) throws BadRequestException;

    @Lock
    void deleteAcl(Acl... acls) throws BadRequestException;

}
