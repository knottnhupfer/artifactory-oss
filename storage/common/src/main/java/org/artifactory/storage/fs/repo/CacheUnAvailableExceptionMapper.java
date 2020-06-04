package org.artifactory.storage.fs.repo;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Inbar Tal
 */
@Provider
public class CacheUnAvailableExceptionMapper implements ExceptionMapper<CacheUnAvailableException> {

    @Override
    public Response toResponse(CacheUnAvailableException e) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON)
                .entity(e.getMessage()).build();
    }
}
