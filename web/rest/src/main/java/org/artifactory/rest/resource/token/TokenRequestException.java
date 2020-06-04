/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.rest.resource.token;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * @author Yinon Avraham
 */
public class TokenRequestException extends WebApplicationException {

    public TokenRequestException(@Nonnull TokenResponseErrorCode errorCode, @Nullable String errorDescription) {
        super(createResponse(errorCode, errorDescription));
    }

    public TokenRequestException(@Nonnull TokenResponseErrorCode errorCode, @Nonnull Throwable cause) {
        super(cause, createResponse(errorCode, cause.getMessage()));
    }

    private static Response createResponse(TokenResponseErrorCode errorCode, String errorDescription) {
        TokenResponseErrorModel model = new TokenResponseErrorModel();
        model.setError(errorCode.getErrorMessage());
        model.setErrorDescription(errorDescription);
        return Response
                .status(errorCode.getResponseCode())
                .entity(model)
                .type(APPLICATION_JSON_TYPE)
                .build();
    }
}
