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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.security.AuthorizationException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.access.*;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.exception.AuthorizationRestException;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.token.exception.TokenScopeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.*;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.artifactory.api.security.AuthorizationService.ROLE_ADMIN;
import static org.artifactory.api.security.AuthorizationService.ROLE_USER;
import static org.artifactory.rest.resource.token.TokenResource.PATH_ROOT;
import static org.artifactory.rest.resource.token.TokenResponseErrorCode.*;
import static org.jfrog.access.util.TokenScopeUtils.scopeToList;

/**
 * @author Yinon Avraham
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(PATH_ROOT)
@RolesAllowed({ROLE_ADMIN, ROLE_USER})
public class TokenResource {

    public static final String PATH_ROOT = "security/token";
    private static final Logger log = LoggerFactory.getLogger(TokenResource.class);
    private static final Set<String> REQUIRED_REFRESH_TOKEN_REQUEST_PARAMS = ImmutableSet.of("grant_type", "refresh_token", "access_token");
    private static final Set<String> OPTIONAL_REFRESH_TOKEN_REQUEST_PARAMS = ImmutableSet.of("expires_in");

    @Autowired
    private AccessService accessService;

    @Autowired
    private AuthorizationService authorizationService;

    @Context
    private HttpServletRequest request;

    @POST
    @Consumes(APPLICATION_FORM_URLENCODED)
    @Produces(APPLICATION_JSON)
    public Response createOrRefreshToken(
            @FormParam("grant_type") String grantTypeName,
            @FormParam("username") String username,
            @FormParam("refresh_token") String refreshToken,
            @FormParam("access_token") String accessToken,
            @FormParam("scope") String scope,
            @FormParam("expires_in") Long expiresIn,
            @FormParam("refreshable") @DefaultValue("false") boolean refreshable,
            @FormParam("audience") String audience) {
        GrantType grantType = requireGrantType(grantTypeName);
        assertPermittedOperation(grantType);
        expiresIn = expiresInOrDefault(expiresIn);
        List<String> scopeTokens;
        try {
            scopeTokens = scopeToList(scope);
        }
        catch (TokenScopeException e){
            throw new TokenRequestException(InvalidScope, e.getMessage());
        }
        List<String> audienceList = audienceToList(audience);
        try {
            TokenSpec tokenSpec = createTokenSpec(username)
                    .scope(scopeTokens)
                    .expiresIn(expiresIn)
                    .refreshable(refreshable)
                    .audience(audienceList);
            switch (grantType) {
                case ClientCredentials:
                    assertSupportedTokenSpecForCreate(tokenSpec);
                    requireUsername(username);
                    return createToken(tokenSpec);
                case RefreshToken:
                    return refreshToken(tokenSpec, accessToken, refreshToken);
                default:
                    throw new GrantTypeNotSupportedException(grantType.getSignature());
            }
        } catch (IllegalArgumentException | AuthorizationException e) {
            throw new TokenRequestException(InvalidRequest, e);
        } catch (NotFoundException e) {
            throw new TokenRequestException(InvalidRepoPath, e);
        }
    }

    private TokenSpec<? extends TokenSpec> createTokenSpec(String username) {
        if (GenericTokenSpec.accepts(username)) {
            return GenericTokenSpec.create(username);
        }
        return UserTokenSpec.create(username);
    }

    @RolesAllowed({ROLE_ADMIN})
    @GET
    @Produces(APPLICATION_JSON)
    public Response getTokenInfos() {
        List<TokenInfo> tokens = accessService.getTokenInfos();
        List<TokenInfoModel> result = tokens.stream().map(TokenInfoModel::new).collect(Collectors.toList());
        TokensResponseModel responseModel = new TokensResponseModel();
        responseModel.setTokens(result);
        return Response.ok(responseModel).build();
    }

    /**
     * Check that the given token spec is supported for creating tokens by Artifactory.
     * This check is done here, in the resource, and not in the service to limit the possibilities from the REST API,
     * but keep the flexibility inside the system.
     * @param tokenSpec the token spec to check
     */
    private void assertSupportedTokenSpecForCreate(TokenSpec tokenSpec) {
        if (!tokenSpec.isRefreshable() || (tokenSpec.getExpiresIn() != null && tokenSpec.getExpiresIn() <= 0)) {
            //Only refreshable tokens with expiry can be used in other artifactory instances/clusters
            if (!targetAudienceIsOnlyThisArtifactoryService(tokenSpec)) {
                throw new TokenRequestException(InvalidRequest,
                        "Only refreshable tokens with expiry can have custom audience");
            }
        }
    }

    private boolean targetAudienceIsOnlyThisArtifactoryService(TokenSpec tokenSpec) {
        List<String> audience = tokenSpec.getAudience();
        return audience.isEmpty() ||
                audience.equals(singletonList(accessService.getArtifactoryServiceId().getFormattedName()));
    }

    private void assertPermittedOperation(@Nonnull GrantType grantType) {
        if (!authorizationService.isAdmin()) {
            switch (grantType) {
                case RefreshToken:
                    if (nonAdminCanRefreshToken()) {
                        return;
                    }
                    break;
                case ClientCredentials:
                    if (nonAdminCanCreateToken()) {
                        return;
                    }
                    break;
                default:
                    throw new GrantTypeNotSupportedException(grantType.getSignature());
            }
            if (!authorizationService.isAuthenticated() || authorizationService.isAnonymous()) {
                throw new WebApplicationException(HttpStatus.SC_UNAUTHORIZED);
            }
            throw new AuthorizationRestException();
        }
    }

    private boolean nonAdminCanCreateToken() {
        return authorizationService.isAuthenticated() && !authorizationService.isAnonymous();
    }

    private boolean nonAdminCanRefreshToken() {
        if (isSimpleRefreshTokenRequestParams()) {
            log.debug("Simple refresh token request - no need to authenticate user, does not require admin");
            return true;
        }
        return false;
    }

    private boolean isSimpleRefreshTokenRequestParams() {
        Set<String> paramNames = Sets.newHashSet(Collections.list(request.getParameterNames()));
        if (!paramNames.containsAll(REQUIRED_REFRESH_TOKEN_REQUEST_PARAMS)) {
            // One of the required params is missing
            return false;
        }
        paramNames.removeAll(REQUIRED_REFRESH_TOKEN_REQUEST_PARAMS);
        for (String param : paramNames) {
            if (!OPTIONAL_REFRESH_TOKEN_REQUEST_PARAMS.contains(param)) {
                // Param is not allowed.
                return false;
            }
        }
        return true;
    }

    /**
     * Revoke a token by the access token or refresh token
     * See <a href="https://tools.ietf.org/html/rfc7009">RFC7009 - Section 2 - Token Revocation</a>
     * Another option is to give the token ID. This option and the first option (by token & hint) are mutually exclusive.
     * @param token the access token or refresh token to revoke
     * @param tokenTypeHint the token type hint (access token or refresh token)
     * @param tokenId the ID of the token to revoke
     */
    @POST
    @Consumes(APPLICATION_FORM_URLENCODED)
    @Produces({TEXT_PLAIN, APPLICATION_JSON})
    @Path("revoke")
    @RolesAllowed({ROLE_ADMIN})
    public Response revokeToken(
            @FormParam("token") String token,
            @FormParam("token_type_hint") String tokenTypeHint,
            @FormParam("token_id") String tokenId) {
        RevokeTokenOption option = assertValidRevokeTokenRequest(token, tokenTypeHint, tokenId);
        TokenType tokenType = tokenTypeHint == null ? TokenType.AccessToken : TokenType.fromSignature(tokenTypeHint);
        //The token type hint is ignored for now... Just validate it in case it is provided.
        String successMessage = "Token revoked";
        try {
            switch (option) {
                case Token:
                    accessService.revokeToken(token);
                    break;
                case TokenId:
                    accessService.revokeTokenById(tokenId);
                    break;
                default:
                    log.warn("Unexpected revoke token option: {}", option);
                    throw new TokenRequestException(InvalidRequest, "token or token_id are required");
            }
        } catch (TokenIssuedByOtherServiceException e) {
            throw new TokenRequestException(InvalidRequest, e);
        } catch (TokenNotFoundException e) {
            log.warn("Ignoring revoke token request, token not found.");
            log.debug("Ignoring revoke token request, token not found.", e);
            successMessage = "Token not found";
        }
        return Response.ok(successMessage).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    private RevokeTokenOption assertValidRevokeTokenRequest(String token, String tokenTypeHint, String tokenId) {
        if (isNotBlank(token)) {
            if (isNotBlank(tokenId)) {
                throw new TokenRequestException(InvalidRequest, "token and token_id are mutually exclusive");
            }
            return RevokeTokenOption.Token;
        }
        if (isNotBlank(tokenId)) {
            return RevokeTokenOption.TokenId;
        }
        throw new TokenRequestException(InvalidRequest, "token or token_id are required");
    }

    private enum RevokeTokenOption {
        Token, TokenId
    }

    private Response refreshToken(@Nonnull TokenSpec tokenSpec, String accessToken, String refreshToken) {
        try {
            requireAccessToken(accessToken);
            requireRefreshToken(refreshToken);
            CreatedTokenInfo createdTokenInfo = accessService.refreshToken(tokenSpec, accessToken, refreshToken);
            TokenResponseModel model = toTokenResponseModel(createdTokenInfo);
            return Response.ok(model).build();
        } catch (TokenNotFoundException | AuthorizationException e) {
            throw new TokenRequestException(InvalidGrant, e);
        } catch (TokenIssuedByOtherServiceException e) {
            throw new TokenRequestException(InvalidRequest, e);
        }
    }

    @Nonnull
    private String requireAccessToken(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            throw new TokenRequestException(InvalidGrant, "access token is required.");
        }
        return accessToken;
    }

    @Nonnull
    private String requireRefreshToken(String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new TokenRequestException(InvalidGrant, "refresh token is required.");
        }
        return refreshToken;
    }

    @Nonnull
    private Response createToken(@Nonnull TokenSpec tokenSpec) {
        CreatedTokenInfo createdTokenInfo = accessService.createToken(tokenSpec);
        TokenResponseModel model = toTokenResponseModel(createdTokenInfo);
        return Response.ok(model).build();
    }

    @Nonnull
    private TokenResponseModel toTokenResponseModel(CreatedTokenInfo createdTokenInfo) {
        TokenResponseModel model = new TokenResponseModel();
        model.setAccessToken(createdTokenInfo.getTokenValue());
        model.setExpiresIn(createdTokenInfo.getExpiresIn());
        model.setScope(createdTokenInfo.getScope());
        model.setTokenType(createdTokenInfo.getTokenType());
        model.setRefreshToken(createdTokenInfo.getRefreshToken());
        return model;
    }

    @Nullable
    private Long expiresInOrDefault(Long expiresIn) {
        //If the caller did not specify an expiry, use the default value
        if (expiresIn == null) {
            return ConstantValues.accessTokenExpiresInDefault.getLong();
        }
        //zero means no expiry...
        //if (expiresIn == 0) {
        //    return 0L;
        //}
        if (expiresIn < 0) {
            throw new TokenRequestException(InvalidRequest, "Invalid expires_in value: " + expiresIn);
        }
        return expiresIn;
    }

    @Nullable
    private List<String> audienceToList(@Nullable String audience) {
        if (isBlank(audience)) {
            return emptyList();
        }
        return asList(audience.split(" "));
    }

    @Nonnull
    private String requireUsername(String username) {
        if (isBlank(username)) {
            throw new TokenRequestException(InvalidRequest, "username is required");
        }
        return username;
    }

    @Nonnull
    private GrantType requireGrantType(@Nullable String grantTypeName) {
        if (grantTypeName == null) {
            return GrantType.ClientCredentials;
        }
        return GrantType.fromSignature(grantTypeName);
    }

}
