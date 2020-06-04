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

package org.artifactory.ui.rest.service.admin.security;

import org.artifactory.descriptor.security.UserLockPolicy;
import org.artifactory.rest.common.dataholder.PasswordContainer;
import org.artifactory.rest.common.service.admin.userprofile.CreateApiKeyService;
import org.artifactory.rest.common.service.admin.userprofile.GetApiKeyService;
import org.artifactory.rest.common.service.admin.userprofile.RevokeApiKeyService;
import org.artifactory.rest.common.service.admin.userprofile.UpdateApiKeyService;
import org.artifactory.ui.rest.model.admin.security.oauth.OAuthUserToken;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.artifactory.ui.rest.service.admin.security.accesstokens.GetAccessTokensService;
import org.artifactory.ui.rest.service.admin.security.accesstokens.RevokeAccessTokensService;
import org.artifactory.ui.rest.service.admin.security.auth.annotate.GetCanAnnotateService;
import org.artifactory.ui.rest.service.admin.security.auth.currentuser.GetCurrentUserService;
import org.artifactory.ui.rest.service.admin.security.auth.forgotpassword.ForgotPasswordService;
import org.artifactory.ui.rest.service.admin.security.auth.forgotpassword.LoginRelatedDataService;
import org.artifactory.ui.rest.service.admin.security.auth.forgotpassword.ResetPasswordService;
import org.artifactory.ui.rest.service.admin.security.auth.forgotpassword.ValidateResetTokenService;
import org.artifactory.ui.rest.service.admin.security.auth.login.LoginService;
import org.artifactory.ui.rest.service.admin.security.auth.logout.LogoutService;
import org.artifactory.ui.rest.service.admin.security.crowdsso.*;
import org.artifactory.ui.rest.service.admin.security.general.*;
import org.artifactory.ui.rest.service.admin.security.group.*;
import org.artifactory.ui.rest.service.admin.security.httpsso.GetHttpSsoService;
import org.artifactory.ui.rest.service.admin.security.httpsso.UpdateHttpSsoService;
import org.artifactory.ui.rest.service.admin.security.ldap.groups.*;
import org.artifactory.ui.rest.service.admin.security.ldap.ldapsettings.*;
import org.artifactory.ui.rest.service.admin.security.oauth.*;
import org.artifactory.ui.rest.service.admin.security.openid.RedirectService;
import org.artifactory.ui.rest.service.admin.security.permissions.*;
import org.artifactory.ui.rest.service.admin.security.saml.*;
import org.artifactory.ui.rest.service.admin.security.signingkeys.*;
import org.artifactory.ui.rest.service.admin.security.signingkeys.keystore.*;
import org.artifactory.ui.rest.service.admin.security.sshserver.*;
import org.artifactory.ui.rest.service.admin.security.ssl.AddCertificateService;
import org.artifactory.ui.rest.service.admin.security.ssl.GetAllCertificateDataService;
import org.artifactory.ui.rest.service.admin.security.ssl.GetCertificateDetails;
import org.artifactory.ui.rest.service.admin.security.ssl.RemoveCertificateService;
import org.artifactory.ui.rest.service.admin.security.user.*;
import org.artifactory.ui.rest.service.admin.security.user.userprofile.UnlockUserProfileService;
import org.artifactory.ui.rest.service.admin.security.user.userprofile.UpdateUserProfileService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.permission.GetRepoEffectivePermissionsByEntityService;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.List;

/**
 * @author Chen Keinan
 */
public abstract class SecurityServiceFactory {

    //authentication service
    @Lookup
    public abstract LoginService loginService();
    @Lookup
    public abstract ForgotPasswordService forgotPassword();
    @Lookup
    public abstract IsSamlAuthentication isSamlAuthentication();
    @Lookup
    public abstract RedirectService redirectService();

    @Lookup
    public abstract GetOAuthSettings getOAuthtSettings();
    @Lookup
    public abstract UpdateOrCreateOAuthSettings updateOAuthSettings();
    @Lookup
    public abstract AddOAuthProviderSettings addOAuthProviderSettings();
    @Lookup
    public abstract UpdateOAuthProviderSettings updateOAuthProviderSettings();
    @Lookup
    public abstract DeleteOAuthProviderSettings deleteOAuthProviderSettings();

    @Lookup
    public abstract GetOAuthTokensForUser getOAuthTokensForUser();

    @Lookup
    public abstract DeleteOAuthUserToken<OAuthUserToken> deleteOAuthUserToken();

    @Lookup
    public abstract ValidateResetTokenService validateToken();

    @Lookup
    public abstract LoginRelatedDataService loginRelatedData();

    @Lookup
    public abstract GetCurrentUserService getCurrentUser();

    @Lookup
    public abstract GetCanAnnotateService getCanAnnotateService();

    @Lookup
    public abstract ResetPasswordService resetPassword();

    @Lookup
    public abstract LogoutService logoutService();
    // user services
    @Lookup
    public abstract CreateUserService<User> createUser();

    @Lookup
    public abstract ChangePasswordService<PasswordContainer> changePassword();

    @Lookup
    public abstract ExpireUserPasswordService<String> expireUserPassword();

    @Lookup
    public abstract RevalidatePasswordService<String> unexpirePassword();

    @Lookup
    public abstract ExpirePasswordForAllUsersService expirePasswordForAllUsers();

    @Lookup
    public abstract RevalidatePasswordForAllUsersService unexpirePasswordForAllUsers();

    @Lookup
    public abstract CheckExternalStatusService<User> checkExternalStatus();

    @Lookup
    public abstract UpdateUserService<User> updateUser();

    @Lookup
    public abstract DeleteUserService deleteUser();

    @Lookup
    public abstract GetAllUsersService getAllUsers();

    @Lookup
    public abstract GetUserService getUser();

    @Lookup
    public abstract GetUserPermissionsService getUserPermissions();

    @Lookup
    public abstract CreateGroupService createGroup();

    @Lookup
    public abstract UpdateGroupService updateGroup();

    @Lookup
    public abstract DeleteGroupService deleteGroup();

    @Lookup
    public abstract GetGroupService getGroup();

    @Lookup
    public abstract GetAllGroupsService getAllGroups();

    @Lookup
    public abstract GetAllGroupNamesService getAllGroupNames();

    @Lookup
    public abstract UpdateSecurityConfigService updateSecurityConfig();
    @Lookup
    public abstract EncryptDecryptService encryptPassword();
    @Lookup
    public abstract GetSecurityConfigService getSecurityConfig();
    @Lookup
    public abstract UpdateUserLockPolicyService<UserLockPolicy> updateUserLockPolicy();
    @Lookup
    public abstract GetUserLockPolicyService getUserLockPolicy();
    @Lookup
    public abstract UnlockUserService<String> unlockUser();
    @Lookup
    public abstract UnlockUsersService<List> unlockUsers();
    @Lookup
    public abstract UnlockAllUsersService unlockAllUsers();
    @Lookup
    public abstract GetAllLockedUsersService getAllLockedUsers();
    @Lookup
    public abstract GetArtifactoryKeyService getArtifactoryKey();
    @Lookup
    public abstract UpdateHttpSsoService updateHttpSso();
    @Lookup
    public abstract GetHttpSsoService getHttpSso();
    @Lookup
    public abstract UpdateSshServerService updateSshServer();
    @Lookup
    public abstract GetSshServerService getSshServer();
    @Lookup
    public abstract InstallSshServerKeyService uploadSshServerKey();
    @Lookup
    public abstract AddCertificateService addPemClientCertificate();
    @Lookup
    public abstract GetAllCertificateDataService getCertificatesData();
    @Lookup
    public abstract GetCertificateDetails getCertificateDetails();
    @Lookup
    public abstract RemoveCertificateService removeCertificate();
    @Lookup
    public abstract GetSshServerKeyService getSshServerKey();
    @Lookup
    public abstract RemoveSshServerKeyService removeSshServerKeyService();
    @Lookup
    public abstract UpdateSamlService updateSaml();
    @Lookup
    public abstract GetSamlService getSaml();
    @Lookup
    public abstract GetSAMLPublicCertificateForEncryptionService getPublicCertificateForEncryption();
    @Lookup
    public abstract RegenerateSAMLPublicCertificateForEncryptionService regenerateSAMLPublicCertificateForEncryption();
    @Lookup
    public abstract GetSamlLoginRequestService handleLoginRequest();
    @Lookup
    public abstract GetSamlLoginResponseService handleLoginResponse();
    @Lookup
    public abstract GetSamlLogoutRequestService handleLogoutRequest();
    @Lookup
    public abstract GetCrowdIntegrationService getCrowdIntegration();
    @Lookup
    public abstract UpdateCrowdIntegration updateCrowdIntegration();
    @Lookup
    public abstract RefreshCrowdGroupsService refreshCrowdGroups();
    @Lookup
    public abstract ImportCrowdGroupsService importCrowdGroups();
    @Lookup
    public abstract CrowdSsoLoginService srowdSsoLoginService();
    @Lookup
    public abstract TestCrowdConnectionService testCrowdConnectionService();
    @Lookup
    public abstract CreateLdapSettingsService createLdapSettings();
    @Lookup
    public abstract UpdateLdapSettingsService updateLdapSettings();
    @Lookup
    public abstract GetLdapSettingsService getLdapSettings();
    @Lookup
    public abstract DeleteLdapSettingsService deleteLdapSettings();
    @Lookup
    public abstract TestLdapSettingsService testLdapSettingsService();
    @Lookup
    public abstract ReorderLdapSettingsService reorderLdapSettings();
    @Lookup
    public abstract CreateLdapGroupService createLdapGroup();
    @Lookup
    public abstract UpdateLdapGroupService updateLdapGroup();
    @Lookup
    public abstract GetLdapGroupService getLdapGroup();
    @Lookup
    public abstract GroupMappingStrategyService groupMappingStrategy();
    @Lookup
    public abstract DeleteLdapGroupService deleteLdapGroup();
    @Lookup
    public abstract RefreshLdapGroupService refreshLdapGroup();
    @Lookup
    public abstract ImportLdapGroupService importLdapGroup();
    @Lookup
    public abstract UnlockUserProfileService unlockUserProfile();
    @Lookup
    public abstract UpdateUserProfileService updateUserProfile();

    @Lookup
    public abstract GetApiKeyService getApiKey();

    @Lookup
    public abstract CreateApiKeyService createApiKey();

    @Lookup
    public abstract RevokeApiKeyService revokeApiKey();

    @Lookup
    public abstract UpdateApiKeyService regenerateApiKey();

    @Lookup
    public abstract InstallSigningKeyService uploadSigningKey();
    @Lookup
    public abstract GetSigningKeyService getSigningKey();
    @Lookup
    public abstract RemoveSigningKeyService removeSigningKeyService();
    @Lookup
    public abstract VerifySigningKeyService verifySigningKey();
    @Lookup
    public abstract UpdateSigningKeyService updateSigningKey();
    @Lookup
    public abstract AddKeyStoreService addKeyStore();
    @Lookup
    public abstract SaveKeyStoreService saveKeyStore();
    @Lookup
    public abstract GetKeyStoreService getKeyStore();

    @Lookup
    public abstract RemoveKeyStorePasswordService removeKeystorePassword();
    @Lookup
    public abstract CancelKeyPairService cancelKeyPair();
    @Lookup
    public abstract RemoveKeyStoreService removeKeyStore();
    @Lookup
    public abstract ChangeKeyStorePasswordService changeKeyStorePassword();
    @Lookup
    public abstract GetAllPermissionTargetsService getAllPermissionTargets();
    @Lookup
    public abstract GetPermissionsTargetService getPermissionsTarget();
    @Lookup
    public abstract GetPermissionTargetUsersService getPermissionsTargetUsers();
    @Lookup
    public abstract GetPermissionTargetGroupsService getPermissionsTargetGroups();
    @Lookup
    public abstract GetPermissionTargetResourcesService getPermissionsTargetResources();
    @Lookup
    public abstract GetAllUsersAndGroupsService getAllUsersAndGroups();
    @Lookup
    public abstract GetRepoEffectivePermissionsByEntityService getRepoEffectivePermissionServiceByEntity();
    @Lookup
    public abstract UpdatePermissionsTargetService updatePermissionsTarget();
    @Lookup
    public abstract CreatePermissionsTargetService createPermissionsTarget();
    @Lookup
    public abstract DeletePermissionsTargetService deletePermissionsTarget();
    @Lookup
    public abstract GetBuildPermissionsTargetBuildsService getBuildPermissionsPatterns();
    @Lookup
    public abstract GetReleaseBundlesByReposAndPatternsService getReleaseBundlesByReposAndPatterns();
    @Lookup
    public abstract GetBuildGlobalBasicReadAllowedService getBuildGlobalBasicReadAllowed();
    @Lookup
    public abstract GetAccessTokensService getAccessTokens();
    @Lookup
    public abstract RevokeAccessTokensService revokeAccessToken();
}
