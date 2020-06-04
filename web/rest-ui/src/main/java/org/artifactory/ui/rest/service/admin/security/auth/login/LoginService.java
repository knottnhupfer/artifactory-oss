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

package org.artifactory.ui.rest.service.admin.security.auth.login;

import org.apache.commons.lang.StringUtils;
import org.artifactory.UiAuthenticationDetails;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.md.Properties;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.security.*;
import org.artifactory.ui.rest.model.admin.security.general.SecurityConfig;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.artifactory.ui.rest.model.admin.security.user.BaseUser;
import org.artifactory.ui.rest.service.admin.security.general.GetSecurityConfigService;
import org.artifactory.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.artifactory.addon.sso.crowd.CrowdAddon.CROWD_NEXT_VALIDATION_HEADER;
import static org.artifactory.api.security.UserGroupService.UI_VIEW_BLOCKED_USER_PROP;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoginService extends AbstractLoginService {
    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private GetSecurityConfigService getSecurityConfigService;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Performs login
     *
     * @param request  - encapsulate all data require for request processing
     * @param response - encapsulate all data require from response
     */
    @Override
    public void doExecute(ArtifactoryRestRequest request, RestResponse response) {
        UserLogin userLogin = (UserLogin) request.getImodel();
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        String username = userLogin.getUser();
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, userLogin.getPassword());

        getSecurityConfigService.execute(request, response);
        SecurityConfig securityConfig = (SecurityConfig) response.getIModel();

        handleAnonymous(username, securityConfig);
        // authenticate user
        Authentication authentication = authenticateCredential(authenticationToken, artifactoryContext, request);

        // authenticate credential against security providers
        if (authentication != null) {
            // check if can view ui
            if (uiViewBlocked(response, userLogin)) {
                return;
            }
            // update session and remember me service with login data
            updateSessionAndRememberMeServiceWithLoginData(request, response, userLogin,
                    artifactoryContext, authenticationToken, authentication);
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            addonsManager.addonByType(PluginsAddon.class).executeAdditiveRealmPlugins(
                    new HttpLoginArtifactoryRequest(request.getServletRequest()));
            //update response with user login data
            updateResponseWithLoginUser(response, userLogin, artifactoryContext, securityConfig);
            SecurityDescriptor securityDescriptor = ContextHelper.get().getCentralConfig().getDescriptor()
                    .getSecurity();
            CrowdSettings crowdSettings = securityDescriptor.getCrowdSettings();
            if (crowdSettings != null && crowdSettings.isEnableIntegration()) {
                long sessionValidationInterval = crowdSettings.getSessionValidationInterval();
                response.getServletResponse()
                        .addHeader(CROWD_NEXT_VALIDATION_HEADER, String.valueOf(sessionValidationInterval));
            }
            // TODO: [chenk] fix this ^^^, not reachable without dependency to artifactory-core
        }
    }

    private void handleAnonymous(String username, SecurityConfig securityConfig) {
        if (UserInfo.ANONYMOUS.equals(username) && !securityConfig.isAnonAccessEnabled()) {
            throw new AuthenticationServiceException("Cannot login with anonymous as a user");
        }
    }

    private boolean uiViewBlocked(RestResponse response, UserLogin userLogin) {
        Properties xrayProps = userGroupService.findPropertiesForUser(userLogin.getUser());
        String uiBlockedUserProp = xrayProps.getFirst(UI_VIEW_BLOCKED_USER_PROP);
        if (StringUtils.isNotBlank(uiBlockedUserProp) && Boolean.valueOf(uiBlockedUserProp)) {
            response.error("UI Access is Disabled For This User").responseCode(HttpServletResponse.SC_UNAUTHORIZED);
            return true;
        }
        return false;
    }

    /**
     * update session and remember me service with user login data
     *
     * @param artifactoryRequest  - encapsulate all data related for request
     * @param artifactoryResponse - encapsulate all data require for response
     * @param userLogin           - user login data
     * @param artifactoryContext  - artifactory application context
     * @param authenticationToken - authentication token created with username and password
     * @param authentication      - authentication created after authenticating the token against providers
     */
    private void updateSessionAndRememberMeServiceWithLoginData(ArtifactoryRestRequest artifactoryRequest,
            RestResponse artifactoryResponse, UserLogin userLogin, ArtifactoryContext artifactoryContext,
            UsernamePasswordAuthenticationToken authenticationToken, Authentication authentication) {
        // update authentication data to session and DB
        boolean isUpdateSucceeded = updateSessionAndDB(artifactoryContext, userLogin.getUser(),
                authenticationToken, authentication, artifactoryRequest);
        // update remember me service if session update succeeded
        updateRememberMeService(artifactoryContext, isUpdateSucceeded, artifactoryRequest, artifactoryResponse);
    }

    /**
     * update response with Login User data
     *
     * @param artifactoryResponse - encapsulate all data require for response
     * @param userLogin           - user login data
     * @param artifactoryContext  - artifactory application context
     */
    private void updateResponseWithLoginUser(RestResponse<RestModel> artifactoryResponse, UserLogin userLogin,
            ArtifactoryContext artifactoryContext, SecurityConfig securityConfig) {
        BaseUser baseUser = getResponseModel(artifactoryContext, userLogin, securityConfig);
        artifactoryResponse.iModel(baseUser);
    }

    /**
     * update response data with user login model data
     *
     * @param artifactoryContext - artifactory web context
     * @param userLogin          - user login nae
     */
    private BaseUser getResponseModel(ArtifactoryContext artifactoryContext, UserLogin userLogin,
            SecurityConfig securityConfig) {
        boolean proWithoutLicense = false;
        if (!(addonsManager instanceof OssAddonsManager) && !addonsManager.isLicenseInstalled()) {
            proWithoutLicense = true;
        }
        boolean offlineMode = true;
        CentralConfigDescriptor descriptor = ContextHelper.get().getCentralConfig().getDescriptor();
        if (ConstantValues.versionQueryEnabled.getBoolean() && !descriptor.isOfflineMode()) {
            offlineMode = false;
        }
        boolean isAdmin = authorizationService.isAdmin();
        // Determines whether to display the 'permission' item in UI admin menu
        boolean canManage = authorizationService.hasPermission(ArtifactoryPermission.MANAGE);
        boolean isBuildBasicView = authorizationService.hasBuildBasicReadPermission();
        BaseUser baseUser = new BaseUser(userLogin.getUser(), isAdmin);
        baseUser.setCanCreateReleaseBundle(
                authorizationService.hasReleaseBundlePermission(ArtifactoryPermission.DEPLOY));
        baseUser.setCanDeploy(baseUser.isCanCreateReleaseBundle() ||
                authorizationService.hasPermission(ArtifactoryPermission.DEPLOY));
        baseUser.setCanManage(canManage);
        baseUser.setBuildBasicView(isBuildBasicView);
        baseUser.setProfileUpdatable(authorizationService.isUpdatableProfile());
        baseUser.setProWithoutLicense(proWithoutLicense);
        baseUser.setOfflineMode(offlineMode);
        baseUser.setRequireProfileUnlock(authorizationService.requireProfileUnlock());
        baseUser.setRequireProfilePassword(authorizationService.requireProfilePassword());
        baseUser.setExistsInDB(!authorizationService.isTransientUser());
        baseUser.setCurrentPasswordValidFor(
                securityConfig.getPasswordSettings().getExpirationPolicy().getCurrentPasswordValidFor());
        return baseUser;
    }


    /**
     * update spring remember me service with login status
     *
     * @param artifactoryContext      - artifactory web context
     * @param isUpdateSucceeded       - if true authentication has been updated successfully
     * @param artifactoryRestRequest  - encapsulate data related to request
     * @param artifactoryRestResponse - encapsulate data needed for response
     */
    private void updateRememberMeService(ArtifactoryContext artifactoryContext,
            boolean isUpdateSucceeded, ArtifactoryRestRequest artifactoryRestRequest,
            RestResponse artifactoryRestResponse) {
        HttpServletRequest servletRequest = artifactoryRestRequest.getServletRequest();
        HttpServletResponse servletResponse = artifactoryRestResponse.getServletResponse();
        if (isUpdateSucceeded) {
            RememberMeServices rememberMeServices = artifactoryContext.beanForType("rememberMeServices",
                    RememberMeServices.class);
            if (!ConstantValues.securityDisableRememberMe.getBoolean()) {
                try {
                    rememberMeServices.loginSuccess(servletRequest, servletResponse,
                            AuthenticationHelper.getAuthentication());
                } catch (UsernameNotFoundException e) {
                    log.warn("Remember Me service is not supported for transient external users.");
                }
            } else {
                if (!ConstantValues.securityDisableRememberMe.getBoolean()) {
                    rememberMeServices.loginFail(servletRequest, servletResponse);
                }
            }
        }
    }

    /**
     * authenticate credential against Security providers (Artifactory,Ldap , crown and etc)
     *
     * @param authenticationToken    - user credentials
     * @param artifactoryContext     - artifactory web context
     * @param artifactoryRestRequest - encapsulate data related to request
     * @return Authentication Data
     */
    private Authentication authenticateCredential(UsernamePasswordAuthenticationToken authenticationToken,
            ArtifactoryContext artifactoryContext, ArtifactoryRestRequest artifactoryRestRequest) {
        WebAuthenticationDetails details = new UiAuthenticationDetails(artifactoryRestRequest.getServletRequest());
        authenticationToken.setDetails(details);
        AuthenticationManager authenticationManager = artifactoryContext.beanForType(
                "authenticationManager", AuthenticationManager.class);
        return authenticationManager.authenticate(authenticationToken);
    }

    /**
     * update session and DB with authentication data
     *
     * @param artifactoryContext     - artifactory web context
     * @param userName               - login user name
     * @param authenticationToken    - login authentication token
     * @param authentication         - spring authentication
     * @param artifactoryRestRequest - encapsulate data related to request
     * @return if true  data save successfully
     */
    private boolean updateSessionAndDB(ArtifactoryContext artifactoryContext, String userName,
            UsernamePasswordAuthenticationToken authenticationToken, Authentication authentication,
            ArtifactoryRestRequest artifactoryRestRequest) {
        boolean isAuthenticate = true;
        try {
            if (authentication.isAuthenticated()) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authenticationToken);
                setLoginDataToSessionAndDB(securityContext, userName, artifactoryContext, authentication,
                        artifactoryRestRequest.getServletRequest());
            }
        } catch (AuthenticationException e) {
            isAuthenticate = false;
            AccessLogger.loginDenied(authenticationToken);
            if (log.isDebugEnabled()) {
                log.debug("Failed to authenticate " + userName, e);
            }
        }
        return isAuthenticate;
    }

    /**
     * set login data to session and db if succeeded
     *
     * @param securityContext - spring security context
     * @param userName        - user name
     */
    private void setLoginDataToSessionAndDB(SecurityContext securityContext, String userName,
            ArtifactoryContext context, Authentication authentication, HttpServletRequest servletRequest) {
        setAuthentication(authentication, securityContext, servletRequest);
        if (isNotBlank(userName) && (!userName.equals(UserInfo.ANONYMOUS))) {
            SecurityService securityService = context.beanForType(SecurityService.class);
            String remoteAddress = new HttpAuthenticationDetails(servletRequest).getRemoteAddress();
            securityService.updateUserLastLogin(userName, System.currentTimeMillis(), remoteAddress);
        }
    }

    /**
     * set session with authentication data
     *
     * @param authentication  - spring authentication
     * @param securityContext - spring security context
     * @param servletRequest  - http servlet request
     */
    void setAuthentication(Authentication authentication, SecurityContext securityContext,
            HttpServletRequest servletRequest) {
        if (authentication.isAuthenticated()) {
            //Log authentication if not anonymous
            if (!isAnonymous(authentication)) {
                AccessLogger.loggedIn(authentication);
            }
            //Set a http session token so that we can reuse the login in direct repo browsing
            SessionUtils.setAuthentication(servletRequest, authentication, true);
            //Update the spring  security context
            bindAuthentication(securityContext, authentication);
        }
    }

    /**
     * @return True is anonymous user is logged in to this session.
     */
    boolean isAnonymous(Authentication authentication) {
        return authentication != null && UserInfo.ANONYMOUS.equals(authentication.getPrincipal().toString());
    }

    /**
     * bind authentication to spring security context
     */
    void bindAuthentication(SecurityContext securityContext, Authentication authentication) {
        securityContext.setAuthentication(authentication);
    }
}
