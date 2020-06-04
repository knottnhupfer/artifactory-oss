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

package org.artifactory.security;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.ha.message.HaBaseMessage;
import org.artifactory.addon.ha.workitem.HaAclMessageWorkItem;
import org.artifactory.addon.sso.HttpSsoAddon;
import org.artifactory.addon.sso.saml.SamlSsoAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.mail.MailService;
import org.artifactory.api.security.*;
import org.artifactory.api.security.ldap.LdapService;
import org.artifactory.build.InternalBuildService;
import org.artifactory.bundle.BundleNameAndRepo;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.config.ConfigurationException;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.security.*;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.oauth.OAuthSettings;
import org.artifactory.descriptor.security.sso.HttpSsoSettings;
import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.environment.converter.local.version.MarkerFileConverter;
import org.artifactory.event.CacheType;
import org.artifactory.event.InvalidateCacheEvent;
import org.artifactory.exception.InvalidNameException;
import org.artifactory.exception.ValidationException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.io.checksum.ChecksumUtils;
import org.artifactory.model.xstream.security.MutableRepoAclImpl;
import org.artifactory.repo.*;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.security.access.AccessService;
import org.artifactory.security.access.AccessTokenAuthentication;
import org.artifactory.security.access.ArtifactoryRepoPathScopeToken;
import org.artifactory.security.access.emigrate.AccessConverters;
import org.artifactory.security.auth.ActivePrincipalTokenStore;
import org.artifactory.security.exceptions.LoginDisabledException;
import org.artifactory.security.exceptions.PasswordChangeException;
import org.artifactory.security.exceptions.PasswordExpireException;
import org.artifactory.security.exceptions.UserLockedException;
import org.artifactory.security.interceptor.SecurityConfigurationChangesInterceptors;
import org.artifactory.security.jobs.CredentialsWatchJob;
import org.artifactory.security.jobs.PasswordExpireNotificationJob;
import org.artifactory.security.props.auth.ApiKeyManager;
import org.artifactory.security.props.auth.DockerTokenManager;
import org.artifactory.security.props.auth.PropsAuthenticationToken;
import org.artifactory.security.props.auth.model.TokenKeyValue;
import org.artifactory.security.providermgr.ArtifactoryTokenProvider;
import org.artifactory.security.signature.SignedUrlAuthenticationToken;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.security.service.access.UserMapper;
import org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper;
import org.artifactory.storage.security.service.AclCache;
import org.artifactory.storage.security.service.AclStoreService;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.artifactory.storage.security.service.UserLockInMemoryService;
import org.artifactory.update.security.SecurityInfoReader;
import org.artifactory.update.security.SecurityVersion;
import org.artifactory.update.utils.BackupUtils;
import org.artifactory.util.*;
import org.artifactory.util.distribution.DistributionConstants;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.access.client.AccessClientHttpException;
import org.jfrog.access.client.model.MessageModel;
import org.jfrog.access.rest.user.UpdateUserRequest;
import org.jfrog.access.rest.user.UserRequest;
import org.jfrog.build.api.Build;
import org.jfrog.common.ClockUtils;
import org.jfrog.common.StreamSupportUtils;
import org.jfrog.common.config.diff.DataDiff;
import org.jfrog.security.crypto.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.artifactory.build.BuildServiceUtils.getBuildJsonPathInRepo;
import static org.artifactory.descriptor.repo.SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME;
import static org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor.RELEASE_BUNDLE_DEFAULT_REPO;
import static org.artifactory.repo.RepoPath.REMOTE_CACHE_SUFFIX;
import static org.artifactory.security.ArtifactoryPermission.*;
import static org.artifactory.security.ArtifactoryResourceType.BUILD;
import static org.artifactory.security.ArtifactoryResourceType.REPO;
import static org.artifactory.security.GroupInfo.READERS;
import static org.artifactory.security.PermissionTarget.*;
import static org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService.GroupFilter;

@Service
@Reloadable(beanClass = InternalSecurityService.class,
        initAfter = {InternalCentralConfigService.class, DbService.class, AccessService.class},
        listenOn = CentralConfigKey.security)
public class SecurityServiceImpl implements InternalSecurityService {
    private static final Logger log = LoggerFactory.getLogger(SecurityServiceImpl.class);

    private static final String DELETE_FOR_SECURITY_MARKER_FILENAME = ".deleteForSecurityMarker";
    private static final String ERR_CAUSE = "Cause: ";
    private static final String ERR_USER = "User ";
    private static final int MAX_SOURCES_TO_TRACK = 10000;
    private static final int MIN_DELAY_BETWEEN_FORGOT_PASSWORD_ATTEMPTS_PER_SOURCE = 500; // ms
    private static final int MAX_USERS_TO_TRACK = 10000; // max locked users to keep in cache

    // cache meaning  <userName, incorrect-login-timestampts>
    private final Cache<String, List<Long>> unknownUsersCache = CacheBuilder.newBuilder()
            .maximumSize(MAX_USERS_TO_TRACK).
                    expireAfterWrite(1, TimeUnit.HOURS).build();

    private Cache<String, List<Long>> resetPasswordAttemptsBySourceCache;

    @Autowired
    private DockerTokenManager dockerTokenManager;
    @Autowired
    private ActivePrincipalTokenStore activePrincipalTokenStore;
    @Autowired
    private AccessService accessService;
    @Autowired
    private AclStoreService aclStoreService;
    @Autowired
    private UserGroupStoreService userGroupStoreService;
    @Autowired
    private CentralConfigService centralConfig;
    @Autowired
    private InternalRepositoryService repositoryService;
    @Autowired
    private MailService mailService;
    @Autowired
    private AddonsManager addons;
    @Autowired
    private LdapService ldapService;
    @Autowired
    private SecurityConfigurationChangesInterceptors interceptors;
    @Autowired
    private CachedThreadPoolTaskExecutor executor;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserPassAuthenticationProvider userPassAuthenticationProvider;
    @Autowired
    private AccessConverters accessConverters;
    @Autowired
    private UserLockInMemoryService userLockInMemoryService;
    @Autowired
    private ApiKeyManager apiKeyManager;
    @Autowired
    private InternalBuildService buildService;
    @Autowired
    private ArtifactoryGroupCachingRepoImpl groupCachingRepo;
    @Autowired
    private ApplicationEventPublisher publisher;

    private InternalArtifactoryContext context;

    private final TreeSet<SecurityListener> securityListeners = new TreeSet<>();

    /**
     * @param user The authentication token.
     * @return An array of sids of the current user and all it's groups.
     */
    static Set<ArtifactorySid> getUserEffectiveSids(SimpleUser user) {
        Set<UserGroupInfo> groups = user.getDescriptor().getGroups();
        Set<ArtifactorySid> sids = new LinkedHashSet<>(groups.size() + 1);
        sids.add(new ArtifactorySid(user.getUsername(), false));
        for (UserGroupInfo group : groups) {
            sids.add(new ArtifactorySid(group.getGroupName(), true));
        }
        return sids;
    }

    private static boolean isAdmin(Authentication authentication) {
        return isAuthenticated(authentication) && getSimpleUser(authentication).isEffectiveAdmin();
    }

    private static boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private static SimpleUser getSimpleUser(Authentication authentication) {
        return (SimpleUser) authentication.getPrincipal();
    }

    private static boolean matches(RepoPermissionTarget aclPermissionTarget, String path, boolean folder) {
        return PathMatcher.matches(path, aclPermissionTarget.getIncludes(), aclPermissionTarget.getExcludes(), folder);
    }

    @Autowired
    private void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = (InternalArtifactoryContext) context;
    }

    @Override
    public void init() {
        // if we need to dump the current security config (.deleteForSecurityMarker doesn't exist)
        // and unlock all admin users

        if (shouldRestoreLoginCapabilities()) {
            dumpCurrentSecurityConfig();
            unlockAdminUsers();
        }

        //Locate and import external configuration file
        checkForExternalConfiguration();
        CoreAddons coreAddon = addons.addonByType(CoreAddons.class);
        if (coreAddon.isCreateDefaultAdminAccountAllowed() && !userGroupStoreService.adminUserExists()) {
            createDefaultAdminUser();
        }
        createDefaultAnonymousUser();

        // start CredentialsWatchJob
        TaskBase credentialsWatchJob = TaskUtils.createRepeatingTask(CredentialsWatchJob.class,
                TimeUnit.SECONDS.toMillis(ConstantValues.passwordExpireJobIntervalSecs.getLong()),
                TimeUnit.SECONDS.toMillis(30L));
        taskService.startTask(credentialsWatchJob, false);

        // start PasswordExpireNotificationJob
        TaskBase passwordExpireNotificationJob = TaskUtils.createRepeatingTask(PasswordExpireNotificationJob.class,
                TimeUnit.SECONDS.toMillis(ConstantValues.passwordExpireNotificationJobIntervalSecs.getLong()),
                TimeUnit.SECONDS.toMillis(60L));
        taskService.startTask(passwordExpireNotificationJob, false);

        initResetPasswordCache(getPasswordResetPolicy(centralConfig.getDescriptor()));
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        // Need to check if security conf changed then clear security caches
        if (!centralConfig.getDescriptor().getSecurity().equals(oldDescriptor.getSecurity())) {
            clearSecurityListeners();
            // Need to check if password reset policy changed
            PasswordResetPolicy passwordResetPolicy = getPasswordResetPolicy(centralConfig.getDescriptor());
            if (!passwordResetPolicy.equals(getPasswordResetPolicy(oldDescriptor))) {
                initResetPasswordCache(passwordResetPolicy);
            }
        }
    }

    private void initResetPasswordCache(PasswordResetPolicy passwordResetPolicy) {
        resetPasswordAttemptsBySourceCache = CacheBuilder.newBuilder()
                .maximumSize(MAX_SOURCES_TO_TRACK)
                .expireAfterWrite(passwordResetPolicy.getTimeToBlockInMinutes(), TimeUnit.MINUTES).build();
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        return getAccessEmigrateMarkerFile().exists() ||
                getAccessResourceTypeConverterMarkerFile().exists() ||
                getAccessUserCustomDataDecryptionMarkerFile().exists() ||
                getCreateDefaultBuildPermissionMarkerFile().exists() ||
                InternalSecurityService.super.isInterested(source, target);
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        // TODO [NS] Generalize service converter mechanism
        MarkerFileConverter.convertIfExistsAndDeleteMarkerFile(getAccessEmigrateMarkerFile(),
                accessConverters.getSecurityEmigrator()::convert);

        MarkerFileConverter.convertIfExistsAndDeleteMarkerFile(getAccessResourceTypeConverterMarkerFile(),
                accessConverters.getResourceTypeConverter()::convert);

        MarkerFileConverter.convertIfExistsAndDeleteMarkerFile(getAccessUserCustomDataDecryptionMarkerFile(),
                accessConverters.getUserCustomDataDecryptionConverter()::convert);

        MarkerFileConverter.convertIfExistsAndDeleteMarkerFile(getCreateDefaultBuildPermissionMarkerFile(),
                accessConverters.getDefaultBuildAclConverter()::convert);
    }

    private File getAccessUserCustomDataDecryptionMarkerFile() {
        return ArtifactoryHome.get().getAccessUserCustomDataDecryptionMarkerFile();
    }

    private File getAccessEmigrateMarkerFile() {
        return ArtifactoryHome.get().getAccessEmigrateMarkerFile();
    }

    private File getAccessResourceTypeConverterMarkerFile() {
        return ArtifactoryHome.get().getAccessResourceTypeConverterMarkerFile();
    }

    private File getCreateDefaultBuildPermissionMarkerFile() {
        return ArtifactoryHome.get().getCreateDefaultBuildPermissionMarkerFile();
    }

    private void dumpCurrentSecurityConfig() {
        try {
            if (accessService.getAccessClient() == null) {
                throw new IllegalStateException("Access client isn't initialized yet");
            }
            accessService.getAccessClient().system().exportAccessServer();
            log.debug("Successfully dumped access security file");

            createSecurityDumpMarkerFile();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't export access server", e);
        }
    }

    /**
     * @return true if SecurityDumpMarkerFile is unavailable
     * otherwise false
     */
    private boolean shouldRestoreLoginCapabilities() {
        File deleteForConsistencyFix = getSecurityDumpMarkerFile();
        return !deleteForConsistencyFix.exists();
    }

    /**
     * Creates/recreates the file that enabled security descriptor dump.
     * Also checks we have proper write access to the data folder.
     */
    private void createSecurityDumpMarkerFile() {
        File securityDumpMarkerFile = getSecurityDumpMarkerFile();
        try {
            securityDumpMarkerFile.createNewFile();
        } catch (IOException e) {
            log.debug("Could not create file: '" + securityDumpMarkerFile.getAbsolutePath() + "'.", e);
        }
    }

    private File getSecurityDumpMarkerFile() {
        return new File(ArtifactoryHome.get().getDataDir(), DELETE_FOR_SECURITY_MARKER_FILENAME);
    }

    /**
     * Checks for an externally supplied configuration file ($ARTIFACTORY_HOME/etc/security.xml). If such a file is
     * found, it will be deserialized to a security info (descriptor) object and imported to the system. This option is
     * to be used in cases like when an administrator is locked out of the system, etc'.
     */
    private void checkForExternalConfiguration() {
        ArtifactoryContext ctx = ContextHelper.get();
        File etcDir = ctx.getArtifactoryHome().getEtcDir();
        File configurationFile = new File(etcDir, "security.import.xml");
        //Work around Jackrabbit state visibility issues within the same tx by forking a separate tx (RTFACT-4526)
        Callable callable = () -> {
            String configAbsolutePath = configurationFile.getAbsolutePath();
            if (configurationFile.isFile()) {
                if (!configurationFile.canRead() || !configurationFile.canWrite()) {
                    throw new ConfigurationException(
                            "Insufficient permissions. Security configuration import requires " +
                                    "both read and write permissions for " + configAbsolutePath
                    );
                }
                try {
                    SecurityInfo descriptorToSave = new SecurityInfoReader().read(configurationFile);
                    //InternalSecurityService txMe = ctx.beanForType(InternalSecurityService.class);
                    getAdvisedMe().importSecurityData(descriptorToSave);
                    Files
                            .switchFiles(configurationFile, new File(etcDir, "security.bootstrap.xml"));
                    log.info("Security configuration imported successfully from {}", configAbsolutePath);
                } catch (Exception e) {
                    throw new IllegalArgumentException("An error has occurred while deserializing the file " +
                            configAbsolutePath +
                            ". Please assure it's validity or remove it from the 'etc' folder.", e);
                }
            }
            return null;
        };
        @SuppressWarnings("unchecked") Future<Set<String>> future = executor.submit(callable);
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException("Could not import external security config.", e);
        }
    }

    @Override
    public boolean isAnonymous() {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        return authentication != null && isAnonymousUser(authentication.getName());
    }

    @Override
    public boolean requireProfileUnlock() {
        SecurityDescriptor security = ContextHelper.get().getCentralConfig().getDescriptor().getSecurity();
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        boolean allowSsoAuth = isAllowSsoAuth(security, addonsManager);
        boolean allowSamlAuth = isAllowSamlAuth(security, addonsManager);
        boolean allowPropsAuth = isAllowPropsAuth(security);
        return !(allowPropsAuth || allowSamlAuth || allowSsoAuth);
    }

    private boolean isAllowPropsAuth(SecurityDescriptor security) {
        OAuthSettings oauthSettings = security.getOauthSettings();
        boolean allowUserToAccessProfileOauth = (oauthSettings != null) && oauthSettings.isAllowUserToAccessProfile();
        return AuthenticationHelper.getAuthentication() instanceof PropsAuthenticationToken &&
                allowUserToAccessProfileOauth;
    }

    private boolean isAllowSamlAuth(SecurityDescriptor security, AddonsManager addonsManager) {
        SamlSettings samlSettings = security.getSamlSettings();
        boolean allowUserToAccessProfileSaml = (samlSettings != null) && samlSettings.isAllowUserToAccessProfile();
        return addonsManager.addonByType(SamlSsoAddon.class).isSamlAuthentication() && allowUserToAccessProfileSaml;
    }

    private boolean isAllowSsoAuth(SecurityDescriptor security, AddonsManager addonsManager) {
        HttpSsoSettings httpSsoSettings = security.getHttpSsoSettings();
        boolean allowUserToAccessProfileSso = (httpSsoSettings != null) && httpSsoSettings.isAllowUserToAccessProfile();
        return addonsManager.addonByType(HttpSsoAddon.class).isHttpSsoAuthentication() && allowUserToAccessProfileSso;
    }

    @Override
    public boolean requireProfilePassword() {
        UserInfo userInfo = findUser(currentUsername(), false);
        boolean userHasPassword = false;
        if (userInfo != null && userInfo.getPassword() != null) {
            userHasPassword = userInfo.getPassword().length() > 0;
        }
        SecurityDescriptor security = ContextHelper.get().getCentralConfig().getDescriptor().getSecurity();
        HttpSsoSettings httpSsoSettings = security.getHttpSsoSettings();
        boolean allowUserToAccessProfileSso =
                httpSsoSettings != null && httpSsoSettings.isAllowUserToAccessProfile();
        SamlSettings samlSettings = security.getSamlSettings();
        boolean allowUserToAccessProfileSaml = (samlSettings != null) && samlSettings.isAllowUserToAccessProfile();
        OAuthSettings oauthSettings = security.getOauthSettings();
        boolean allowUserToAccessProfileOauth =
                (oauthSettings != null) && oauthSettings.isAllowUserToAccessProfile();
        Authentication authentication = AuthenticationHelper.getAuthentication();
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        SamlSsoAddon samlSsoAddon = addonsManager.addonByType(SamlSsoAddon.class);
        HttpSsoAddon httpSsoAddon = addonsManager.addonByType(HttpSsoAddon.class);
        return !doNotRequirePasswordFromExtUser(userHasPassword, allowUserToAccessProfileSso,
                allowUserToAccessProfileSaml, allowUserToAccessProfileOauth,
                authentication, samlSsoAddon, httpSsoAddon);
    }

    private boolean doNotRequirePasswordFromExtUser(boolean userHasPassword, boolean allowUserToAccessProfileSso,
            boolean allowUserToAccessProfileSaml, boolean allowUserToAccessProfileOauth, Authentication authentication,
            SamlSsoAddon samlSsoAddon, HttpSsoAddon httpSsoAddon) {
        return ((authentication instanceof PropsAuthenticationToken && !allowUserToAccessProfileOauth) ||
                (samlSsoAddon.isSamlAuthentication() && !allowUserToAccessProfileSaml) ||
                (httpSsoAddon.isHttpSsoAuthentication() && !allowUserToAccessProfileSso)) && !userHasPassword;
    }

    @Override
    public boolean isAnonAccessEnabled() {
        SecurityDescriptor security = centralConfig.getDescriptor().getSecurity();
        return security.isAnonAccessEnabled();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        return isAuthenticated(authentication);
    }

    @Override
    public boolean isAdmin() {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        return isAdmin(authentication);
    }

    @Override
    public void createAcl(Acl acl) {
        assertAdmin();
        validatePermissionNameExists(acl.getPermissionTarget().getName());
        MutableAcl<? extends PermissionTarget> compatibleAcl = createAclAdapter(acl);
        cleanupAclInfo(compatibleAcl);
        aclStoreService.createAcl(compatibleAcl);
        interceptors.onPermissionsAdd(compatibleAcl);
        notifyClusterMembers();
    }

    @Override
    public void updateAcl(Acl acl) {
        //If the editing user is not a sys-admin
        if (!isAdmin()) {
            //Assert that no unauthorized modifications were performed
            validateUnmodifiedPermissionTarget(acl.getPermissionTarget());
        }

        MutableAcl<? extends PermissionTarget> compatibleAcl = createAclAdapter(acl);

        // Removing empty Ace
        cleanupAclInfo(compatibleAcl);
        aclStoreService.updateAcl(compatibleAcl);
        interceptors.onPermissionsUpdate(compatibleAcl);
        notifyClusterMembers();
    }

    private void validatePermissionNameExists(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("ACL name cannot be null");
        }
    }

    private MutableAcl<? extends PermissionTarget> createAclAdapter(Acl acl) {
        if (acl instanceof RepoAcl) {
            return makeNewAclRemoteRepoKeysAclCompatible((MutableRepoAclImpl) acl);
        }
        return (MutableAcl<? extends PermissionTarget>) acl;
    }

    @Override
    public void deleteAcl(Acl acl) {
        aclStoreService.deleteAcl(acl);
        interceptors.onPermissionsDelete((MutableAcl<? extends PermissionTarget>) acl);
        notifyClusterMembers();
    }

    private void notifyClusterMembers() {
        HaAddon haAddon = addons.addonByType(HaAddon.class);
        HaBaseMessage haMessage = new HaBaseMessage(haAddon.getCurrentMemberServerId());
        HaAclMessageWorkItem workItem = new HaAclMessageWorkItem(haMessage);
        haAddon.notifyAsync(workItem);
    }

    @Override
    public List<RepoPermissionTarget> getRepoPermissionTargets(ArtifactoryPermission permission) {
        return getAllRepoAcls(permission).stream()
                .map(Acl::getPermissionTarget)
                .collect(Collectors.toList());
    }

    @Override
    public List<RepoPermissionTarget> getRepoPermissionTargets(UserInfo user, ArtifactoryPermission permission) {
        return getAllRepoAcls(user, permission).stream()
                .map(Acl::getPermissionTarget)
                .collect(Collectors.toList());
    }

    @Override
    public List<RepoAcl> getAllRepoAcls(ArtifactoryPermission permission) {
        return aclStoreService.getAllRepoAcls().stream()
                .filter(acl -> hasPermissionOnAcl(acl, permission))
                .collect(Collectors.toList());
    }

    @Override
    public Map<Character, List<PermissionTargetAcls>> getAllAclsMappedByPermissionTargetFirstChar(boolean reversed) {
        return aclStoreService.getMapPermissionTargetAcls(reversed);
    }

    private List<RepoAcl> getAllRepoAcls(UserInfo user, ArtifactoryPermission permission) {
        SimpleUser simpleUser = new SimpleUser(user);
        // Iterating the permissions - we don't care about the repo name, only permission
        return aclStoreService.getAllRepoAcls()
                .stream()
                .filter(acl -> hasPermissionOnAcl(acl, permission, simpleUser, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildAcl> getAllBuildAcls(ArtifactoryPermission permission) {
        return aclStoreService.getAllBuildAcls().stream()
                .filter(acl -> hasPermissionOnAcl(acl, permission))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReleaseBundleAcl> getAllReleaseBundleAcls(ArtifactoryPermission permission) {
        return getAllReleaseBundleAclsByLicense().stream()
                .filter(acl -> hasPermissionOnAcl(acl, permission))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isUpdatableProfile() {
        UserInfo simpleUser = currentUser();
        return simpleUser != null && simpleUser.isUpdatableProfile();
    }

    @Override
    public boolean isTransientUser() {
        UserInfo simpleUser = currentUser();
        return simpleUser != null && simpleUser.isTransientUser();
    }

    @Override
    @Nonnull
    public String currentUsername() {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        //Do not return a null username or this will cause a constraint violation
        return (authentication != null ? authentication.getName() : SecurityService.USER_SYSTEM);
    }

    @Override
    public UserInfo currentUser() {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        if (authentication == null) {
            return null;
        }
        SimpleUser user = getSimpleUser(authentication);
        return user.getDescriptor();
    }

    @Override
    @Nonnull
    public UserInfo findUser(String username) {
        return findUser(username, true);
    }

    /**
     * Returns the user details for the given username.
     *
     * @param username       The unique username
     * @param errorOnAbsence throw error if user is not found
     * @return UserInfo if user with the input username exists
     */
    @Override
    @Nullable
    public UserInfo findUser(String username, boolean errorOnAbsence) {
        UserInfo user = userGroupStoreService.findUser(username);
        if (errorOnAbsence && user == null) {
            throw new UsernameNotFoundException(ERR_USER + username + " does not exist!");
        }
        return user;
    }

    @Override
    public RepoAcl getRepoAcl(String permTargetName) {
        return aclStoreService.getRepoAcl(permTargetName);
    }

    @Override
    public BuildAcl getBuildAcl(String permTargetName) {
        return aclStoreService.getBuildAcl(permTargetName);
    }

    @Override
    public ReleaseBundleAcl getReleaseBundleAcl(String permTargetName) {
        return aclStoreService.getReleaseBundleAcl(permTargetName);
    }

    @Override
    public ReleaseBundleAcl getReleaseBundleAclByLicense(String permTargetName) {
        return hasEdgeOrEnterprisePlusLic() ? aclStoreService.getReleaseBundleAcl(permTargetName) : null;
    }

    private void cleanupAclInfo(MutableAcl<? extends PermissionTarget> acl) {
        acl.getMutableAces().removeIf(aceInfo -> aceInfo.getMask() == 0);
    }

    @Override
    public List<RepoAcl> getAllRepoAcls() {
        return new ArrayList<>(aclStoreService.getAllRepoAcls());
    }

    @Override
    public List<BuildAcl> getAllBuildAcls() {
        return new ArrayList<>(aclStoreService.getAllBuildAcls());
    }

    @Override
    public List<ReleaseBundleAcl> getAllReleaseBundleAcls(boolean getByLicense) {
        return getByLicense ? new ArrayList<>(getAllReleaseBundleAclsByLicense()) :
                new ArrayList<>(aclStoreService.getAllReleaseBundleAcls());
    }

    @Override
    public List<Acl<? extends PermissionTarget>> getAllAcls() {
        return Lists.newArrayList(aclStoreService.getAllRepoAcls(), aclStoreService.getAllBuildAcls(),
                getAllReleaseBundleAclsByLicense()).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Boolean> getAllUsersAndAdminStatus(boolean justAdmins) {
        return userGroupStoreService.getAllUsersAndAdminStatus(justAdmins);
    }

    @Override
    public List<UserInfo> getAllUsers(boolean includeAdmins) {
        return getAllUsers(includeAdmins, false);
    }

    @Override
    public List<UserInfo> getAllUsers(boolean includeAdmins, boolean includePasswordsAndEncrypted) {
        return userGroupStoreService.getAllUsers(includeAdmins, includePasswordsAndEncrypted);
    }

    @Override
    public List<RepoAcl> getRepoPathAcls(RepoPath repoPath) {
        LocalRepoDescriptor localRepo = repositoryService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        boolean isRemoteCache = localRepo.isCache();
        boolean isDistRepo = localRepo instanceof DistributionRepoDescriptor;
        boolean isBuildRepo = buildService.getBuildInfoRepoKey().equals(localRepo.getKey());
        return aclStoreService.getAllRepoAcls().stream()
                .filter(acl -> isRepoKeyInAcl(acl.getPermissionTarget().getRepoKeys(), repoPath, isRemoteCache,
                        isDistRepo, isBuildRepo))
                .filter(acl -> isRepoPathInAclPermissions(acl.getPermissionTarget(), repoPath))
                .collect(Collectors.toList());
    }

    /**
     * Filter Acl from the AclsCache that contain the repoKey or any ANYs keys
     */
    private boolean isRepoKeyInAcl(List<String> repoKeys, RepoPath repoPath, boolean isRemote, boolean isDistribution,
            boolean isBuildRepo) {
        if (isBuildRepo) {
            return repoKeys.contains(repoPath.getRepoKey());
        }
        return repoKeys.contains(repoPath.getRepoKey()) ||
                repoKeys.contains(ANY_REPO) ||
                (isDistribution && repoKeys.contains(ANY_DISTRIBUTION_REPO)) ||
                (isRemote ? repoKeys.contains(ANY_REMOTE_REPO) : repoKeys.contains(ANY_LOCAL_REPO));
    }

    private boolean isRepoPathInAclPermissions(RepoPermissionTarget permissionTarget, RepoPath repoPath) {
        return isRepoPathInAclPermissions(permissionTarget.getIncludes(), permissionTarget.getExcludes(), repoPath);
    }

    @Override
    public boolean isBuildInPermissions(List<String> includePatterns, List<String> excludePatterns, String buildName) {
        return isRepoPathInAclPermissions(includePatterns, excludePatterns,
                getRootPathInRepo(buildName, buildService.getBuildInfoRepoKey()));
    }

    @Override
    public boolean isReleaseBundleInPermission(List<String> includePatterns, List<String> excludePatterns,
            BundleNameAndRepo bundle) {
        return isRepoPathInAclPermissions(includePatterns, excludePatterns,
                getRootPathInRepo(bundle.getName(), bundle.getStoringRepo()));
    }

    /**
     * Checks if the permission target include/exclude match the build name
     */
    private boolean isRepoPathInAclPermissions(List<String> includePatterns, List<String> excludePatterns,
            RepoPath repoPath) {
        String path = getPathToCheckPermissionFor(repoPath);
        return PathMatcher.matches(path, includePatterns, excludePatterns, repoPath.isFolder());
    }

    @Override
    public List<BuildAcl> getBuildPathAcls(String buildName, String buildNumber, String buildStarted) {
        return aclStoreService.getAllBuildAcls().stream()
                .filter(acl -> isRepoPathInAclPermissions(acl.getPermissionTarget(),
                        getBuildJsonPathInRepo(buildName, buildNumber, buildStarted,
                                buildService.getBuildInfoRepoKey())))
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildAcl> getBuildPathAcls(RepoPath repoPath) {
        return aclStoreService.getAllBuildAcls().stream()
                .filter(acl -> isRepoPathInAclPermissions(acl.getPermissionTarget(), repoPath))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReleaseBundleAcl> getReleaseBundleAcls(RepoPath repoPath) {
        return getAllReleaseBundleAclsByLicense().stream()
                .filter(acl -> isRepoPathInAclPermissions(acl.getPermissionTarget(), repoPath))
                .collect(Collectors.toList());
    }

    @Override
    public org.artifactory.md.Properties findPropertiesForUser(String username) {
        return userGroupStoreService.findPropertiesForUser(username);
    }

    @Override
    public boolean addUserProperty(String username, String key, String value) {
        return userGroupStoreService.addUserProperty(username, key, value);
    }

    @Override
    public boolean updateUserProperty(String username, String key, String value) {
        boolean isUpdateSucceeded = userGroupStoreService.deleteUserProperty(username, key);
        if (isUpdateSucceeded) {
            isUpdateSucceeded = userGroupStoreService.addUserProperty(username, key, value);
        }
        return isUpdateSucceeded;
    }

    @Override
    public String getUserProperty(String username, String key) {
        return userGroupStoreService.findUserProperty(username, key);
    }

    @Override
    public void deleteUserProperty(String userName, String propertyKey) {
        userGroupStoreService.deleteUserProperty(userName, propertyKey);
    }

    @Override
    public void deletePropertyFromAllUsers(String propertyKey) {
        userGroupStoreService.deletePropertyFromAllUsers(propertyKey);
    }

    @Override
    public String getPropsToken(String userName, String propsKey) {
        return userGroupStoreService.findUserProperty(userName, propsKey);
    }

    @Override
    public boolean revokePropsToken(String userName, String propsKey) {
        invalidateAuthCacheEntries(userName);
        return userGroupStoreService.deleteUserProperty(userName, propsKey);
    }

    @Override
    public boolean createPropsToken(String userName, String propsKey, String propsValue) {
        boolean isPropsAddSucceeded = false;
        try {
            isPropsAddSucceeded = userGroupStoreService.addUserProperty(userName, propsKey, propsValue);
        } catch (Exception e) {
            log.debug("Error adding {}: {} to db", propsKey, propsValue);
        }
        return isPropsAddSucceeded;
    }

    @Override
    public void revokeAllPropsTokens(String propsKey) {
        userGroupStoreService.deletePropertyFromAllUsers(propsKey);
        invalidateAuthCacheEntriesForAllUsers();
    }

    @Override
    public boolean updatePropsToken(String userName, String propsKey, String propsValue) {
        boolean isUpdateSucceeded = userGroupStoreService.deleteUserProperty(userName, propsKey);
        if (isUpdateSucceeded) {
            isUpdateSucceeded = userGroupStoreService.addUserProperty(userName, propsKey, propsValue);
            invalidateAuthCacheEntries(userName);
        }
        return isUpdateSucceeded;
    }

    /**
     * Locks user upon incorrect login attempt
     */
    @Override
    public void lockUser(@Nonnull String userName) {
        if (!isAnonymousUser(userName)) {
            log.debug("Locking user due to incorrect login attempts");
            userGroupStoreService.lockUser(userName);
            userLockInMemoryService.lockUser(userName);
        }
    }

    /**
     * Unlocks locked in user
     */
    @Override
    public void unlockUser(@Nonnull String userName) {
        if (!isAnonymousUser(userName)) {
            log.debug("Unlocking user: '{}'", userName);
            userLockInMemoryService.unlockUser(userName);
            userGroupStoreService.unlockUser(userName);
            unknownUsersCache.invalidate(userName);
            resetUserIncorrectLoginAttempt(userName);
        }
    }

    /**
     * Unlocks all locked in users
     */
    @Override
    public void unlockAllUsers() {
        log.debug("Unlocking all users");
        userGroupStoreService.unlockAllUsers();
        userLockInMemoryService.unlockAllUsers();
        unknownUsersCache.invalidateAll();
        getAllUsers(true).forEach(user -> resetUserIncorrectLoginAttempt(user.getUsername()));
    }

    private void unlockAdminUsers() {
        log.debug("Unlocking all admin users");
        userGroupStoreService.unlockAdminUsers();
    }

    /**
     * Registers incorrect login attempt
     */
    @Override
    public void registerIncorrectLoginAttempt(@Nonnull String userName) {
        if (!isAnonymousUser(userName)) {
            log.debug("Registering incorrect login attempt");
            userLockInMemoryService.registerIncorrectLoginAttempt(userName);
        }
    }

    /**
     * @return List of locked in users
     */
    @Override
    public Set<String> getLockedUsers() {
        return userGroupStoreService.getLockedUsers();
    }

    @Override
    public ImmutableSetMultimap<String, String> getAllUsersInGroups() {
        return userGroupStoreService.getAllUsersInGroups();
    }

    @Override
    public boolean adminUserExists() {
        return userGroupStoreService.adminUserExists();
    }

    private void resetUserIncorrectLoginAttempt(String userName) {
        if (!isAnonymousUser(userName)) {
            log.debug("Resetting incorrect login attempt");
            userLockInMemoryService.resetIncorrectLoginAttempts(userName);
        }
    }

    /**
     * Triggered when user success to login
     *
     * @param userName user to intercept
     */
    @Override
    public void interceptLoginSuccess(@Nonnull String userName) {
        resetUserIncorrectLoginAttempt(userName);
    }

    /**
     * Resets incorrect login attempts
     */
    @Override
    public void resetIncorrectLoginAttempts(@Nonnull String userName) {
        log.debug("Resetting incorrect login attempts for '{}'", userName);
        userLockInMemoryService.resetIncorrectLoginAttempts(userName);
    }

    /**
     * Triggered when user fails to login and
     * locks it if amount of login failures exceeds
     * {@see LockPolicy#loginAttempts}
     *
     * @param userName   user to intercept
     * @param accessTime session creation time
     */
    @Override
    public void interceptLoginFailure(@Nonnull String userName, long accessTime) {
        if (!isAnonymousUser(userName)) {
            log.debug("Registering login attempt failure");
            registerIncorrectLoginAttempt(userName);
            UserInfo user = userGroupStoreService.findUser(userName);
            if (user == null) {
                interceptUnknownUserLoginFailure(userName, accessTime);
            } else {
                interceptKnownUserLoginFailure(user);
            }
        }
    }

    /**
     * Intercepts login failure for (known to artifactory) user
     *
     * @param user user to intercept failure for
     */
    private void interceptKnownUserLoginFailure(UserInfo user) {
        log.debug("Number of incorrect login attempts: " +
                userLockInMemoryService.getIncorrectLoginAttempts(user.getUsername()) +
                ", Max allowed login attempts: " + getAllowedMaxLoginAttempts());
        if (isUserLockPolicyEnabled() && !user.isLocked() &&
                userLockInMemoryService.getIncorrectLoginAttempts(user.getUsername()) >= getAllowedMaxLoginAttempts()) {
            lockUser(user.getUsername());
        }
    }

    /**
     * Intercepts login failure for (unknown to artifactory) user
     *
     * @param userName   user to intercept failure for
     * @param accessTime access time
     */
    private void interceptUnknownUserLoginFailure(String userName, long accessTime) {
        log.trace("Memorizing (not a user) for blocking");
        if (!isUserLocked(userName)) {
            List<Long> incorrectLoginAttempts = unknownUsersCache.getIfPresent(userName);
            if (incorrectLoginAttempts == null) {
                registerUnknownUser(userName);
                // memorize incorrect login attempt
                unknownUsersCache.put(userName, Lists.newArrayList(accessTime));
            } else {
                incorrectLoginAttempts.add(accessTime);
                if (isUserLockPolicyEnabled() &&
                        incorrectLoginAttempts.size() >= getAllowedMaxLoginAttempts()) {
                    lockUser(userName);
                    unknownUsersCache.invalidate(userName); // no need to track this user as it got locked
                }
            }
        }
    }

    /**
     * Registers unknown user in cache
     */
    private void registerUnknownUser(String userName) {
        if (!isAnonymousUser(userName)) {
            log.trace("Registering incorrect login attempt for unknown user");
            unknownUsersCache.put(userName, new ArrayList<>(getAllowedMaxLoginAttempts()));
        }
    }

    /**
     * @return whether {@link UserLockPolicy} is enabled
     */
    @Override
    public boolean isUserLockPolicyEnabled() {
        UserLockPolicy userLockPolicy = centralConfig.getDescriptor().getSecurity().getUserLockPolicy();
        return userLockPolicy.isEnabled();
    }

    /**
     * @return whether {@link PasswordExpirationPolicy} is enabled
     */
    @Override
    public boolean isPasswordExpirationPolicyEnabled() {
        if (centralConfig.getMutableDescriptor().getSecurity().getPasswordSettings().getExpirationPolicy() != null) {
            return centralConfig.getMutableDescriptor()
                    .getSecurity()
                    .getPasswordSettings()
                    .getExpirationPolicy()
                    .getEnabled();
        }
        return false;
    }

    /**
     * @return MaxLoginAttempts allowed before user gets locked out
     */
    private int getAllowedMaxLoginAttempts() {
        UserLockPolicy userLockPolicy =
                centralConfig.getMutableDescriptor()
                        .getSecurity().getUserLockPolicy();

        return userLockPolicy.getLoginAttempts();
    }

    /**
     * @return Number of days for password to get expired
     */
    private int getPasswordExpirationDays() {
        PasswordExpirationPolicy passwordExpirationPolicy =
                centralConfig.getMutableDescriptor()
                        .getSecurity().getPasswordSettings().getExpirationPolicy();
        return passwordExpirationPolicy.getPasswordMaxAge();
    }

    /**
     * @return Max number of attempts for requesting password reset
     */
    private PasswordResetPolicy getPasswordResetPolicy(CentralConfigDescriptor centralConfig) {
        return Optional.ofNullable(centralConfig)
                .map(CentralConfigDescriptor::getSecurity)
                .map(SecurityDescriptor::getPasswordSettings)
                .map(PasswordSettings::getResetPolicy)
                .orElseGet(PasswordResetPolicy::new);
    }

    /**
     * Throws LockedException if user is locked
     */
    @Override
    public void ensureUserIsNotLocked(@Nonnull String userName) throws UserLockedException {
        log.debug("Checking if user is not locked");
        if (!isAnonymousUser(userName) &&
                isUserLockPolicyEnabled() && isUserLocked(userName)) {
            log.debug("User is locked, denying login");
            throw UserLockedException.userLocked();
        }
    }

    /**
     * Throws LockedException if user is locked
     */
    @Override
    public void ensureSessionIsNotLocked(@Nonnull String sessionIdentifier) throws UserLockedException {
        log.debug("Checking if session is not locked");
        if (isUserLockPolicyEnabled() && isUserLocked(sessionIdentifier)) {
            log.debug("Session is locked, denying login");
            throw UserLockedException.sessionLocked();
        }
    }

    /**
     * Checks whether given user is locked
     * <p>
     * note: this method using caching in sake
     * of DB load preventing
     *
     * @return boolean
     */
    @Override
    public boolean isUserLocked(String userName) {
        return !isAnonymousUser(userName) &&
                userGroupStoreService.isUserLocked(userName);
    }

    /**
     * Throws LoginDelayedException if user has performed
     * incorrect login in past and now should wait before
     * performing another login attempt
     */
    @Override
    public void ensureLoginShouldNotBeDelayed(@Nonnull String userName, long sessionTimeStampMilliseconds) {
        if (!isAnonymousUser(userName)) {
            log.debug("Ensuring that user should not be blocked");
            long nextLogin = userLockInMemoryService.getNextLoginTime(userName);
            List<Long> list = unknownUsersCache.getIfPresent(userName);
            if (nextLogin < 0 && list != null) {
                // check frontend cache for unknown users
                nextLogin = userLockInMemoryService.getNextLoginTime(list.size(), list.get(list.size() - 1));
            }

            if (nextLogin > 0 && nextLogin > System.currentTimeMillis()) {
                log.debug("User is blocked due to incorrect login attempts till {}", nextLogin);
                userLockInMemoryService.updateUserAccess(userName,
                        isUserLockPolicyEnabled(),
                        sessionTimeStampMilliseconds);
                userLockInMemoryService.registerIncorrectLoginAttempt(userName);
                throw LoginDisabledException.userLocked(nextLogin);
            }
        }
    }

    /**
     * Throws LoginDelayedException if session has performed
     * incorrect login in past and now should wait before
     * performing another login attempt
     */
    @Override
    public void ensureSessionShouldNotBeDelayed(@Nonnull String sessionIdentifier) {
        if (!isAnonymousUser(sessionIdentifier)) {
            log.debug("Ensuring that user should not be blocked");
            // Try to calculate next login time for known user
            long nextLogin = userLockInMemoryService.getNextLoginTime(sessionIdentifier);
            // If user is unknown calculate next login for unknown user
            List<Long> list = unknownUsersCache.getIfPresent(sessionIdentifier);
            if (nextLogin < 0 && list != null) {
                // check frontend cache for unknown users
                nextLogin = userLockInMemoryService.getNextLoginTime(list.size(), list.get(list.size() - 1));
            }
            if (nextLogin > 0 && nextLogin > System.currentTimeMillis()) {
                log.debug("Session is blocked due to incorrect login attempts till {}", nextLogin);
                throw LoginDisabledException.sessionLocked(nextLogin);
            }
        }
    }

    /**
     * Performs check whther given user is anonymous
     *
     * @return true/false
     */
    private boolean isAnonymousUser(String userName) {
        return userName != null &&
                userName.length() == UserInfo.ANONYMOUS.length() &&
                UserInfo.ANONYMOUS.equals(userName);
    }

    @Override
    public boolean createUser(MutableUserInfo user) {
        user.setUsername(user.getUsername().toLowerCase());
        try {
            boolean userCreated = userGroupStoreService.createUser(user);
            if (userCreated) {
                interceptors.onUserAdd(user.getUsername());
            }
            return userCreated;
        } catch (AccessClientHttpException e) {
            List<MessageModel> errors = e.getErrorsModel().getErrors();
            if (CollectionUtils.notNullOrEmpty(errors)) {
                String error = errors.get(0).getMessage();
                log.error("Unable to create user: '{}': {}", user.getUsername(), error);
                throw new IllegalArgumentException(error, e);
            }
            log.error("Unable to create user: '{}'", user.getUsername(), e);
            return false;
        }
    }

    @Override
    public boolean createUserWithNoUIAccess(MutableUserInfo user) {
        if (createUser(user)) {
            userGroupStoreService.addUserProperty(user.getUsername(), UI_VIEW_BLOCKED_USER_PROP, "true");
            return true;
        }
        return false;
    }

    /**
     * Changes user password
     *
     * @param userName     user name
     * @param oldPassword  old password
     * @param newPassword1 new password
     * @param newPassword2 replication of new password
     */
    @Override
    public void changePassword(String userName, String oldPassword, String newPassword1, String newPassword2)
            throws PasswordChangeException {
        try {
            UserInfo user = findUser(userName);

            // perform user account validity check
            ensureUserIsNotLocked(userName);
            ensureLoginShouldNotBeDelayed(userName, user.getLastLoginTimeMillis());

            if (isOldPasswordValid(findUser(userName), oldPassword, newPassword1, newPassword2)) {
                changePasswordWithoutValidation(user, newPassword1);
            }
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            log.debug(ERR_CAUSE, e);
            throw new PasswordChangeException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(ERR_CAUSE, e);
            throw new PasswordChangeException("Changing password has failed, see logs for more details");
        }
    }

    /**
     * Changes user password without validating old password
     *
     * @param user        user to change password to
     * @param newPassword new password
     */
    @Override
    public void changePasswordWithoutValidation(@Nonnull UserInfo user, String newPassword)
            throws PasswordChangeException {
        try {
            userGroupStoreService.changePassword(user, generateSaltedPassword(newPassword), newPassword);
            invalidateAuthCacheEntries(user.getUsername());
            dockerTokenManager.revokeToken(user.getUsername());
            log.info("Password for user: '{}' has been successfully changed", user.getUsername());
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(ERR_CAUSE, e);
            throw new PasswordChangeException("", e);
        }
    }

    /**
     * Checks whether old password is valid
     *
     * @param user         a user to getAndCheckSidPermissions password details for
     * @param oldPassword  old password
     * @param newPassword1 new password
     * @param newPassword2 replication of new password
     * @return true/false
     */
    private boolean isOldPasswordValid(UserInfo user, String oldPassword, String newPassword1, String newPassword2) {
        if (!userPassAuthenticationProvider.canUserLogin(user.getUsername(), oldPassword)) {
            throw new PasswordChangeException("Old password is incorrect");
        }

        if ((newPassword1 != null && !newPassword1.equals(newPassword2)) ||
                (newPassword2 != null && !newPassword2.equals(newPassword1))) {
            throw new PasswordChangeException("New passwords do not match");
        }

        if (oldPassword != null && oldPassword.equals(newPassword1)) {
            throw new PasswordChangeException("New password has to be different from the old one");
        }

        if (newPassword1 == null) {
            throw new PasswordChangeException("New passwords cannot be empty");
        }
        return true;
    }

    @Override
    public void updateUser(MutableUserInfo user, boolean activateListeners) {
        String userName = user.getUsername().toLowerCase();
        user.setUsername(userName);

        userGroupStoreService.updateUser(user);
        if (activateListeners) {
            invalidateAuthCacheEntries(user.getUsername());
            dockerTokenManager.revokeToken(user.getUsername());
            activePrincipalTokenStore.invalidateToken(user.getUsername());
        }
    }

    @Override
    public void deleteUser(String username) {
        aclStoreService.removeAllUserAces(username);
        userGroupStoreService.deleteUser(username);
        interceptors.onUserDelete(username);
        invalidateAuthCacheEntries(username);
        dockerTokenManager.revokeToken(username);
    }

    /**
     * Removes the user's cache entries from the non-ui and Docker auth caches
     *
     * @param userName user to remove
     */
    private void invalidateAuthCacheEntries(String userName) {
        //Currently the only listener is AccessFilter - take care if more are added
        for (SecurityListener listener : securityListeners) {
            listener.onUserUpdate(userName);
        }
        //Also invalidate docker auth cache entries
        ContextHelper.get().beanForType(ArtifactoryTokenProvider.class).invalidateUserCacheEntries(userName);
    }

    /**
     * Removes cache entries from the non-ui and Docker auth caches for all users
     */
    private void invalidateAuthCacheEntriesForAllUsers() {
        //Clear the entire non-ui cache
        clearSecurityListeners();
        ContextHelper.get().beanForType(ArtifactoryTokenProvider.class).invalidateCacheEntriesForAllUsers();
    }

    @Override
    public void updateGroup(MutableGroupInfo groupInfo) {
        boolean groupUpdated = userGroupStoreService.updateGroup(groupInfo);
        if (groupUpdated) {
            interceptors.onGroupUpdate(groupInfo.getGroupName(), groupInfo.getRealm());
        }
    }

    @Override
    public boolean createGroup(MutableGroupInfo groupInfo) {
        boolean groupCreated = userGroupStoreService.createGroup(groupInfo);
        if (groupCreated) {
            interceptors.onGroupAdd(groupInfo.getGroupName(), groupInfo.getRealm());
        }
        return groupCreated;
    }

    @Override
    public void updateGroupUsers(MutableGroupInfo group, List<String> usersInGroup) {
        // remove users from groups
        removePrevGroupUsers(group);
        // add users to group
        addUserToGroup(usersInGroup, group.getGroupName());
    }

    @Override
    public void deleteGroup(String groupName) {
        aclStoreService.removeAllGroupAces(groupName);
        if (userGroupStoreService.deleteGroup(groupName)) {
            interceptors.onGroupDelete(groupName);
        }
    }

    @Override
    public List<GroupInfo> getAllGroups() {
        return new ArrayList<>(groupCachingRepo.fetchAllGroups().values());
    }

    @Override
    public List<String> getAllAdminGroupsNames() {
        return new ArrayList<>(groupCachingRepo.getGroupsByFilter(GroupFilter.ADMIN).keySet());
    }

    @Override
    public Map<String, GroupInfo> getAllExternalGroups() {
        return groupCachingRepo.getGroupsByFilter(GroupFilter.EXTERNAL);
    }

    @Override
    public Map<String, GroupInfo> getAllGroupsByGroupNames(List<String> groupNames) {
        return groupCachingRepo.getGroupInfosByNames(groupNames).entrySet()
                .stream()
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    @Override
    public List<GroupInfo> getNewUserDefaultGroups() {
        return userGroupStoreService.getNewUserDefaultGroups();
    }

    @Override
    public List<GroupInfo> getInternalGroups() {
        return userGroupStoreService.getInternalGroups();
    }

    @Override
    public Set<String> getNewUserDefaultGroupsNames() {
        return userGroupStoreService.getNewUserDefaultGroupsNames();
    }

    @Override
    public void addUsersToGroup(String groupName, List<String> usernames) {
        userGroupStoreService.addUsersToGroup(groupName, usernames);
        interceptors.onAddUsersToGroup(groupName, usernames);
        for (String username : usernames) {
            invalidateAuthCacheEntries(username);
            dockerTokenManager.revokeToken(username);
        }
    }

    @Override
    public void removeUsersFromGroup(String groupName, List<String> usernames) {
        userGroupStoreService.removeUsersFromGroup(groupName, usernames);
        interceptors.onRemoveUsersFromGroup(groupName, usernames);
        for (String username : usernames) {
            invalidateAuthCacheEntries(username);
            dockerTokenManager.revokeToken(username);
        }
    }

    @Override
    public List<String> findUsersInGroup(String groupName) {
        return userGroupStoreService.findUsersInGroup(groupName);
    }

    @Override
    public String resetPassword(String userName, String remoteAddress, String resetPageUrl) {
        validateResetPasswordAttempt(remoteAddress);
        UserInfo userInfo = null;
        try {
            userInfo = findUser(userName);
        } catch (UsernameNotFoundException e) {
            //Alert in the log when trying to reset a password of an unknown user
            log.warn("An attempt has been made to reset a password of unknown user: '{}'", userName);
        }

        //If the user is found, and has an email address
        if (userInfo != null && !StringUtils.isEmpty(userInfo.getEmail())) {

            //If the user hasn't got sufficient permissions
            if (!userInfo.isUpdatableProfile()) {
                throw new RuntimeException("The specified user is not permitted to reset his password.");
            }

            //Generate and send a password reset key
            try {
                generatePasswordResetKey(userName, remoteAddress, resetPageUrl);
            } catch (EmailException ex) {
                String message = ex.getMessage() + " Please contact your administrator.";
                throw new RuntimeException(message);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        } else if (userInfo != null) {
            log.warn("Reset password e-mail was not really sent, e-mail not set for user: '{}'", userName);
        }
        return "We have sent you via email a link for resetting your password. Please check your inbox.";
    }

    /**
     * Validates a reset password attempt from the given remote address.
     *
     * @throws ResetPasswordException on an invalid attempt (e.g. too many/frequent attempts)
     */
    void validateResetPasswordAttempt(String remoteAddress) {
        PasswordResetPolicy policy = getPasswordResetPolicy(centralConfig.getDescriptor());
        if (policy.isEnabled()) {
            try {
                List<Long> attempts = resetPasswordAttemptsBySourceCache.get(remoteAddress, Lists::newArrayList);
                if (!attempts.isEmpty()) {
                    if (attempts.size() >= policy.getMaxAttemptsPerAddress()) {
                        throw ResetPasswordException.tooManyAttempts(remoteAddress);
                    }
                    if (System.currentTimeMillis() - attempts.get(attempts.size() - 1) <
                            MIN_DELAY_BETWEEN_FORGOT_PASSWORD_ATTEMPTS_PER_SOURCE) {
                        throw ResetPasswordException.tooFrequentAttempts(remoteAddress);
                    }
                }
                attempts.add(System.currentTimeMillis());
            } catch (ExecutionException e) {
                String errorMessage =
                        "Unexpected error while verifying legitimacy of password reset requested from remote address " +
                                remoteAddress;
                log.error(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        }
    }

    @Override
    public UserInfo findOrCreateExternalAuthUser(String userName, boolean transientUser) {
        return findOrCreateExternalUser(userName, transientUser, false);
    }

    @Override
    public UserInfo findOrCreateExternalAuthUser(String userName, boolean transientUser, boolean updateProfile) {
        return findOrCreateExternalUser(userName, transientUser, updateProfile);
    }

    /**
     * find or create external user in db
     *
     * @param userName      - user name to find
     * @param transientUser - if true ,  user mark as transient (not created in db)
     * @param updateProfile - if true ,  user will be able to update it own profile
     * @return {@link UserInfo}
     */
    private UserInfo findOrCreateExternalUser(String userName, boolean transientUser, boolean updateProfile) {
        UserInfo userInfo;
        try {
            userInfo = findUser(userName.toLowerCase());
        } catch (UsernameNotFoundException nfe) {
            try {
                userInfo = autoCreateUser(userName, transientUser, updateProfile);
            } catch (ValidationException ve) {
                log.error("Auto-Creation of '" + userName + "' has failed, " + ve.getMessage());
                throw new InvalidNameException(userName, ve.getMessage(), ve.getIndex());
            }
        }
        return userInfo;
    }


    /**
     * remove group users before update
     *
     * @param group - group data
     */
    private void removePrevGroupUsers(MutableGroupInfo group) {
        List<String> usersInGroup = findUsersInGroup(group.getGroupName());
        if (usersInGroup != null && !usersInGroup.isEmpty()) {
            removeUsersFromGroup(group.getGroupName(), usersInGroup);
        }
    }

    /**
     * @param users     - user list to be added to group
     * @param groupName - group name
     */
    private void addUserToGroup(List<String> users, String groupName) {
        if (users != null && !users.isEmpty()) {
            addUsersToGroup(groupName, users);
        }
    }

    /**
     * Auto create user
     *
     * @return {@link UserInfo}
     *
     * @throws ValidationException if userName is invalid
     */
    private UserInfo autoCreateUser(String userName, boolean transientUser,
            boolean updateProfile) throws ValidationException {
        UserInfo userInfo;
        log.debug("Creating new external user: '{}'", userName);

        // make sure username answer artifactory standards RTFACT-8259
        NameValidator.validate(userName);

        //set All new external users to have a disabled internal password
        UserInfoBuilder userInfoBuilder = new UserInfoBuilder(userName.toLowerCase()).updatableProfile(updateProfile)
                .passwordDisabled(true);
        userInfoBuilder.internalGroups(getNewUserDefaultGroupsNames());
        if (transientUser) {
            userInfoBuilder.transientUser();
        }
        userInfo = userInfoBuilder.build();

        // Save non transient user
        if (!transientUser) {
            boolean success = userGroupStoreService.createUser(userInfo);
            if (!success) {
                log.error("The user: '{}' was not created!", userInfo);
            }
        }
        return userInfo;
    }

    @Override
    @Nullable
    public GroupInfo findGroup(String groupName) {
        return groupCachingRepo.getGroupInfoByName(groupName);
    }

    @Override
    public String createEncryptedPasswordIfNeeded(UserInfo user, String password) {
        if (isPasswordEncryptionEnabled() && StringUtils.isNotBlank(password)) {
            // Checking if the password sent by user is an API key. If so we don't encrypt it, just return it.
            if (EncodingType.ARTIFACTORY_API_KEY.isEncodedByMe(password)) {
                log.debug("Returning API Key as encrypted password.");
                return password;
            }
            EncodedKeyPair encodedKeyPair;
            EncryptionWrapper encryptionWrapper;
            try {
                if (StringUtils.isBlank(user.getPrivateKey())) {
                    encodedKeyPair = createKeyPairForUser(user);
                } else {
                    encodedKeyPair = convertOldKeyFormatIfNeeded(user);
                }
                encryptionWrapper = EncryptionWrapperFactory.createKeyWrapper(null, encodedKeyPair);
                return encryptionWrapper.encryptIfNeeded(password);
            } catch (Exception e) {
                String err = String
                        .format("Error inferring keypair for user: '%s': %s generating a new keypair if called from profile page.",
                                user.getUsername(), e.getMessage());
                if (log.isDebugEnabled()) {
                    log.error(err, e);
                } else {
                    log.error(err);
                }
                //Throw PasswordEncryptionFailureException. The UnlockUserProfileService#updateUserInfo should fallback
                //and re-invoke the password
                throw new PasswordEncryptionFailureException("Failed to encrypt password. " +
                        "To retrieve a new encrypted password unlock your user profile page again.");
            }
        }
        return password;
    }

    /**
     * Creates and saves a new keypair for {@param user} using {@param masterWrapper}
     */
    private EncodedKeyPair createKeyPairForUser(UserInfo user) {
        EncodedKeyPair encodedKeyPair;
        log.info("Creating keys for user: '{}'", user.getUsername());
        MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(user);
        DecodedKeyPair decodedKeyPair = new DecodedKeyPair(JFrogCryptoHelper.generateKeyPair());
        encodedKeyPair = new EncodedKeyPair(decodedKeyPair, null);
        saveEncodedKeyPairInUser(mutableUser, encodedKeyPair);
        return encodedKeyPair;
    }

    /**
     * Converts old format keypair to new format for {@param user} using {@param masterWrapper} if required,
     * May generate a new keypair if old format encoding is used which we don't support any more.
     */
    private EncodedKeyPair convertOldKeyFormatIfNeeded(UserInfo user) {
        EncodedKeyPair encodedKeyPair;
        encodedKeyPair = new EncodedKeyPair(user.getPrivateKey(), user.getPublicKey());
        EncodedKeyPair toSaveEncodedKeyPair = encodedKeyPair
                .toSaveEncodedKeyPair(ArtifactoryHome.get().getArtifactoryEncryptionWrapper());
        if (toSaveEncodedKeyPair != null) {
            log.info("Reformatting keys for user: '{}'", user.getUsername());
            MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(user);
            saveEncodedKeyPairInUser(mutableUser, toSaveEncodedKeyPair);
            encodedKeyPair = toSaveEncodedKeyPair;
        }
        return encodedKeyPair;
    }

    private void saveEncodedKeyPairInUser(MutableUserInfo mutableUser, EncodedKeyPair encodedKeyPair) {
        mutableUser.setPrivateKey(encodedKeyPair.getEncodedPrivateKey());
        mutableUser.setPublicKey(encodedKeyPair.getEncodedPublicKey());
        updateUser(mutableUser, false);
    }

    /**
     * Generates a password recovery key for the specified user and send it by mail
     *
     * @param username      User to rest his password
     * @param remoteAddress The IP of the client that sent the request
     * @param resetPageUrl  The URL to the password reset page
     */
    @Override
    public void generatePasswordResetKey(String username, String remoteAddress, String resetPageUrl) throws Exception {
        UserInfo userInfo;
        try {
            userInfo = findUser(username);
        } catch (UsernameNotFoundException e) {
            //If can't find user
            throw new IllegalArgumentException("Could not find specified username.", e);
        }

        //If user has valid email
        if (!StringUtils.isEmpty(userInfo.getEmail())) {
            if (!userInfo.isUpdatableProfile()) {
                //If user is not allowed to update his profile
                throw new AuthorizationException("User is not permitted to reset his password.");
            }

            //Build key by UUID + current time millis + client ip -> encoded in B64
            UUID uuid = UUID.randomUUID();
            String passwordKey = uuid.toString() + ":" + System.currentTimeMillis() + ":" + remoteAddress;
            byte[] encodedKey = Base64.encodeBase64URLSafe(passwordKey.getBytes(Charsets.UTF_8));
            String encodedKeyString = new String(encodedKey, Charsets.UTF_8);

            MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(userInfo);
            mutableUser.setGenPasswordKey(encodedKeyString);
            updateUser(mutableUser, false);

            //Add encoded key to page url
            String resetPage = resetPageUrl + "?key=" + encodedKeyString;

            //If there are any admins with valid email addresses, add them to the list that the message will contain
            //String adminList = getAdminListBlock(userInfo);
            InputStream stream = null;
            try {
                //Get message body from properties and substitute variables
                stream = getClass().getResourceAsStream("/org/artifactory/email/messages/resetPassword.properties");
                ResourceBundle resourceBundle = new PropertyResourceBundle(stream);
                String body = resourceBundle.getString("body");
                body = MessageFormat.format(body, username, remoteAddress, resetPage);
                mailService.sendMail(new String[]{userInfo.getEmail()}, "Reset password request", body);
            } catch (EmailException e) {
                log.error("Error while resetting password for user: '{}'.", username, e);
                throw e;
            } finally {
                IOUtils.closeQuietly(stream);
            }
            log.info("The user: '{}' has been sent a password reset message by mail.", username);
        }
    }

    @Override
    public SerializablePair<Date, String> getPasswordResetKeyInfo(String username) {
        UserInfo userInfo = findUser(username);
        String passwordKey = userInfo.getGenPasswordKey();
        if (StringUtils.isEmpty(passwordKey)) {
            return null;
        }

        byte[] decodedKey = Base64.decodeBase64(passwordKey.getBytes(Charsets.UTF_8));
        String decodedKeyString = new String(decodedKey, Charsets.UTF_8);
        String[] splitKey = decodedKeyString.split(":");

        //Key must be in 3 parts
        if (splitKey.length < 3) {
            throw new IllegalArgumentException("Password reset key must contain 3 parts - 'UUID:Date:IP'");
        }

        String time = splitKey[1];
        String ip = splitKey[2];

        Date date = new Date(Long.parseLong(time));

        return new SerializablePair<>(date, ip);
    }

    @Override
    public SerializablePair<String, Long> getUserLastLoginInfo(String username) {
        UserInfo userInfo;
        try {
            userInfo = findUser(username);
        } catch (UsernameNotFoundException e) {
            //If can't find user (might be transient user)
            log.trace("Could not retrieve last login info for username '{}'.", username);
            return null;
        }

        SerializablePair<String, Long> pair = null;
        String lastLoginClientIp = userInfo.getLastLoginClientIp();
        long lastLoginTimeMillis = userInfo.getLastLoginTimeMillis();
        if (!StringUtils.isEmpty(lastLoginClientIp) && (lastLoginTimeMillis != 0)) {
            pair = new SerializablePair<>(lastLoginClientIp, lastLoginTimeMillis);
        }
        return pair;
    }

    @Override
    public boolean isHasPriorLogin() {
        return userGroupStoreService.getAllUsers(true, false)
                .stream()
                .anyMatch(user -> user.getLastLoginTimeMillis() != 0);
    }

    @Override
    public void updateUserLastLogin(String username, long loginTimeMillis, String clientIp) {
        long lastLoginBufferTimeSecs = ConstantValues.userLastAccessUpdatesResolutionSecs.getLong();
        if (lastLoginBufferTimeSecs < 1) {
            log.debug("Skipping the update of the last login time for the user: '{}': tracking is disabled.", username);
            return;
        }
        long lastLoginBufferTimeMillis = TimeUnit.SECONDS.toMillis(lastLoginBufferTimeSecs);
        UserInfo userInfo = userGroupStoreService.findUser(username);
        if (userInfo == null) {
            // user not found (might be a transient user)
            log.trace("Could not update non-exiting username: {}'.", username);
            return;
        }
        long timeSinceLastLogin = loginTimeMillis - userInfo.getLastLoginTimeMillis();
        if (timeSinceLastLogin < lastLoginBufferTimeMillis) {
            log.debug("Skipping the update of the last login time for the user: '{}': " +
                    "was updated less than {} seconds ago.", username, lastLoginBufferTimeSecs);
            return;
        }

        userGroupStoreService.updateUserLastLogin(username, loginTimeMillis, clientIp);
    }

    /**
     * Updates user last access time, if user is not exist in artifactory
     * keeps track of it in volatile cache
     *
     * @param userName         Name of user that performed an action
     * @param clientIp         The IP of the client that has accessed
     * @param accessTimeMillis The time of access
     */
    @Override
    public void updateUserLastAccess(String userName, String clientIp, long accessTimeMillis) {
        log.debug("Updating access details for user, time={}, ip={}", accessTimeMillis, clientIp);
        userLockInMemoryService.updateUserAccess(userName, isUserLockPolicyEnabled(), accessTimeMillis);
    }

    @Override
    public boolean isHttpSsoProxied() {
        HttpSsoSettings httpSsoSettings = centralConfig.getDescriptor().getSecurity().getHttpSsoSettings();
        return httpSsoSettings != null && httpSsoSettings.isHttpSsoProxied();
    }

    @Override
    public boolean isNoHttpSsoAutoUserCreation() {
        HttpSsoSettings httpSsoSettings = centralConfig.getDescriptor().getSecurity().getHttpSsoSettings();
        return httpSsoSettings != null && httpSsoSettings.isNoAutoUserCreation();
    }

    @Override
    public String getHttpSsoRemoteUserRequestVariable() {
        HttpSsoSettings httpSsoSettings = centralConfig.getDescriptor().getSecurity().getHttpSsoSettings();
        if (httpSsoSettings == null) {
            return null;
        } else {
            return httpSsoSettings.getRemoteUserRequestVariable();
        }
    }

    @Override
    public boolean hasRepoPermission(ArtifactoryPermission permission) {
        return isAdmin() || getAllRepoAcls().stream().anyMatch(acl -> hasPermissionOnAcl(acl, permission));
    }

    @Override
    public boolean hasBuildPermission(ArtifactoryPermission permission) {
        return isAdmin() || getAllBuildAcls().stream().anyMatch(acl -> hasPermissionOnAcl(acl, permission));
    }

    @Override
    public boolean hasReleaseBundlePermission(ArtifactoryPermission permission) {
        return isAdmin() || getAllReleaseBundleAcls(true).stream().anyMatch(acl -> hasPermissionOnAcl(acl, permission));
    }

    @Override
    public boolean hasPermission(ArtifactoryPermission permission) {
        return hasRepoPermission(permission) || hasBuildPermission(permission) ||
                hasReleaseBundlePermission(permission);
    }

    @Override
    public boolean hasBuildBasicReadPermission() {
        return isAdmin() || isBuildGlobalBasicReadAllowed() ||
                getAllBuildAcls().stream().anyMatch(acl -> hasPermissionOnAcl(acl, ArtifactoryPermission.READ));
    }

    @Override
    public boolean hasBasicReadPermissionForAllBuilds() {
        return isAdmin() || isBuildGlobalBasicReadAllowed();
    }

    @Override
    public boolean hasAnyPermission(GroupInfo group, ArtifactoryPermission permission) {
        // Iterating the permissions - we don't care about the repo name, only permission
        return aclStoreService.getAllRepoAcls()
                .stream()
                .anyMatch(acl -> hasPermissionOnAcl(acl, permission, group));
    }

    @Override
    public boolean canRead(RepoPath repoPath) {
        return hasPermission(repoPath, ArtifactoryPermission.READ);
    }

    @Override
    public boolean canAnnotate(RepoPath repoPath) {
        return hasPermission(repoPath, ArtifactoryPermission.ANNOTATE);
    }

    @Override
    public boolean canDeploy(RepoPath repoPath) {
        return hasPermission(repoPath, ArtifactoryPermission.DEPLOY);
    }

    @Override
    public boolean canDelete(RepoPath repoPath) {
        return hasPermission(repoPath, ArtifactoryPermission.DELETE);
    }

    @Override
    public boolean canManage(RepoPath repoPath) {
        return hasPermission(repoPath, ArtifactoryPermission.MANAGE);
    }

    @Override
    public boolean canManage(PermissionTarget target) {
        return hasPermissionOnPermissionTarget(target, ArtifactoryPermission.MANAGE);
    }

    @Override
    public boolean canManage(Acl<? extends PermissionTarget> acl) {
        return hasPermissionOnAcl(acl, ArtifactoryPermission.MANAGE);
    }

    @Override
    public boolean canRead(UserInfo user, PermissionTarget target) {
        return hasPermissionOnPermissionTarget(target, ArtifactoryPermission.READ, new SimpleUser(user));
    }

    @Override
    public boolean canAnnotate(UserInfo user, PermissionTarget target) {
        return hasPermissionOnPermissionTarget(target, ArtifactoryPermission.ANNOTATE, new SimpleUser(user));
    }

    @Override
    public boolean canDeploy(UserInfo user, PermissionTarget target) {
        return hasPermissionOnPermissionTarget(target, ArtifactoryPermission.DEPLOY, new SimpleUser(user));
    }

    @Override
    public boolean canDelete(UserInfo user, PermissionTarget target) {
        return hasPermissionOnPermissionTarget(target, ArtifactoryPermission.DELETE, new SimpleUser(user));
    }

    @Override
    public boolean canManage(UserInfo user, PermissionTarget target) {
        return hasPermissionOnPermissionTarget(target, ArtifactoryPermission.MANAGE, new SimpleUser(user));
    }

    @Override
    public boolean canRead(UserInfo user, RepoPath path) {
        return hasPermission(new SimpleUser(user), path, ArtifactoryPermission.READ);
    }

    @Override
    public boolean canAnnotate(UserInfo user, RepoPath path) {
        return hasPermission(new SimpleUser(user), path, ArtifactoryPermission.ANNOTATE);
    }

    @Override
    public boolean canDelete(UserInfo user, RepoPath path) {
        return hasPermission(new SimpleUser(user), path, ArtifactoryPermission.DELETE);
    }

    @Override
    public boolean canDeploy(UserInfo user, RepoPath path) {
        return hasPermission(new SimpleUser(user), path, ArtifactoryPermission.DEPLOY);
    }

    @Override
    public boolean canManage(UserInfo user, RepoPath path) {
        return hasPermission(new SimpleUser(user), path, ArtifactoryPermission.MANAGE);
    }

    @Override
    public boolean canRead(GroupInfo group, RepoPath path) {
        return hasPermission(group, path, ArtifactoryPermission.READ);
    }

    @Override
    public boolean canAnnotate(GroupInfo group, RepoPath path) {
        return hasPermission(group, path, ArtifactoryPermission.ANNOTATE);
    }

    @Override
    public boolean canDelete(GroupInfo group, RepoPath path) {
        return hasPermission(group, path, ArtifactoryPermission.DELETE);
    }

    @Override
    public boolean canDeploy(GroupInfo group, RepoPath path) {
        return hasPermission(group, path, ArtifactoryPermission.DEPLOY);
    }

    @Override
    public boolean canManage(GroupInfo group, RepoPath path) {
        return hasPermission(group, path, ArtifactoryPermission.MANAGE);
    }

    @Override
    public Map<PermissionTarget, AceInfo> getUserPermissionByPrincipal(String username, ArtifactoryResourceType type) {
        Map<PermissionTarget, AceInfo> aceInfoMap = Maps.newHashMap();
        UserInfo user = userGroupStoreService.findUser(username);
        if (user == null) {
            return Maps.newHashMap();
        }
        Set<ArtifactorySid> sids = getUserEffectiveSids(new SimpleUser(user));
        getAclsByType(type).forEach(acl -> addSidsPermissions(aceInfoMap, sids, acl));
        return aceInfoMap;
    }

    @Override
    public Multimap<PermissionTarget, AceInfo> getUserPermissionAndItsGroups(String username,
            ArtifactoryResourceType type) {
        Multimap<PermissionTarget, AceInfo> aceInfoMap = ArrayListMultimap.create();
        UserInfo user = userGroupStoreService.findUser(username);
        if (user == null) {
            return ArrayListMultimap.create();
        }
        Set<ArtifactorySid> sids = getUserEffectiveSids(new SimpleUser(user));
        getAclsByType(type).forEach(acl -> addSidsPermissions(aceInfoMap, sids, acl));
        return aceInfoMap;
    }

    @Override
    public Multimap<PermissionTarget, AceInfo> getGroupsPermissions(List<String> groups,
            ArtifactoryResourceType type) {
        Multimap<PermissionTarget, AceInfo> aceInfoMap = HashMultimap.create();
        getAclsByType(type).forEach(acl -> acl.getAces().stream()
                .filter(ace -> ace.isGroup() && groups.contains(ace.getPrincipal()))
                .forEach(ace -> aceInfoMap.put(acl.getPermissionTarget(), ace)));
        return aceInfoMap;
    }

    @Override
    public Map<PermissionTarget, AceInfo> getUserPermissions(String userName, ArtifactoryResourceType type) {
        Map<PermissionTarget, AceInfo> aceInfoMap = Maps.newHashMap();
        getAclsByType(type).forEach(acl -> acl.getAces()
                .stream()
                .filter(ace -> !ace.isGroup())
                .filter(ace -> userName.equals(ace.getPrincipal()))
                .forEach(ace -> aceInfoMap.put(acl.getPermissionTarget(), ace)));
        return aceInfoMap;
    }

    private Collection<? extends Acl<? extends PermissionTarget>> getAclsByType(ArtifactoryResourceType type) {
        switch (type) {
            case REPO:
                return aclStoreService.getAllRepoAcls();
            case BUILD:
                return aclStoreService.getAllBuildAcls();
            case RELEASE_BUNDLES:
                return getAllReleaseBundleAclsByLicense();
            default:
                throw new IllegalStateException("ACL type is unknown. Must be repo/build");
        }
    }

    /**
     * add artifactory sids permissions to map
     *
     * @param aceInfoMap - permission target and principal info map
     * @param sids       -permissions related sids
     * @param acl        - permissions acls
     */
    private void addSidsPermissions(Map<PermissionTarget, AceInfo> aceInfoMap, Set<ArtifactorySid> sids,
            Acl<? extends PermissionTarget> acl) {
        //Check that we match the sids
        acl.getAces()
                .stream()
                .filter(ace -> sids.contains(new ArtifactorySid(ace.getPrincipal(), ace.isGroup())))
                .forEach(ace -> aceInfoMap.put(acl.getPermissionTarget(), ace));
    }

    private void addSidsPermissions(Multimap<PermissionTarget, AceInfo> aceInfoMap, Set<ArtifactorySid> sids,
            Acl<? extends PermissionTarget> acl) {
        //Check that we match the sids
        acl.getAces()
                .stream()
                .filter(ace -> sids.contains(new ArtifactorySid(ace.getPrincipal(), ace.isGroup())))
                .forEach(ace -> aceInfoMap.put(acl.getPermissionTarget(), ace));
    }

    @Override
    public boolean userHasPermissionsOnRepositoryRoot(String repoKey) {
        Repo repo = repositoryService.repositoryByKey(repoKey);
        if (repo == null) {
            // Repo does not exists => No permissions
            return false;
        }
        // If it is a real (i.e local or cached simply check permission on root.
        if (repo.isReal()) {
            // If repository is real, check if the user has any permission on the root.
            if (repo instanceof RemoteRepo) {
                RepoPath remoteRepoPath = InternalRepoPathFactory.repoRootPath(repoKey);
                repoKey = InternalRepoPathFactory.cacheRepoPath(remoteRepoPath).getRepoKey();
            }
            return hasPermissionOnRoot(repoKey);
        } else {
            // If repository is virtual go over all repository associated with it and check if user has permissions
            // on it root.
            VirtualRepo virtualRepo = (VirtualRepo) repo;
            // Go over all resolved cached repos, i.e. if we have virtual repository aggregation,
            // This will give the resolved cached repos.
            Set<LocalCacheRepo> localCacheRepoList = virtualRepo.getResolvedLocalCachedRepos();
            for (LocalCacheRepo localCacheRepo : localCacheRepoList) {
                LocalRepo localRepo = repositoryService.localOrCachedRepositoryByKey(localCacheRepo.getKey());
                if (localRepo != null) {
                    if (hasPermissionOnRoot(localRepo.getKey())) {
                        return true;
                    }
                }
            }
            // Go over all resolved local repositories, will bring me the resolved local repos from aggregation.
            Set<LocalRepo> repoList = virtualRepo.getResolvedLocalRepos();
            for (LocalRepo localCacheRepo : repoList) {
                LocalRepo localRepo = repositoryService.localOrCachedRepositoryByKey(localCacheRepo.getKey());
                if (localRepo != null) {
                    if (hasPermissionOnRoot(localRepo.getKey())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDisableInternalPassword() {
        UserInfo simpleUser = currentUser();
        return (simpleUser == null);
    }

    @Override
    public String currentUserEncryptedPassword() {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        if ((authentication != null) && authentication.isAuthenticated()) {
            String authUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
            String password = (String) authentication.getCredentials();
            if (StringUtils.isNotBlank(password)) {
                UserInfo user = userGroupStoreService.findUser(authUsername);
                if (user == null) {
                    log.warn("Can't return the encrypted password of the unfound user: '{}'", authUsername);
                } else {
                    return createEncryptedPasswordIfNeeded(user, password);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isApiKeyAuthentication() {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        if (authentication == null) {
            return false;
        }
        try {
            String password = (String) authentication.getCredentials();
            return CryptoHelper.isApiKey(password);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String currentUserEncryptedPasswordOrApiKey() {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        if ((authentication != null) && authentication.isAuthenticated()) {
            String authUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
            UserInfo user = userGroupStoreService.findUser(authUsername);
            String password = (String) authentication.getCredentials();
            if (user == null) {
                log.warn("Can't return the encrypted password of the unfound user: '{}'", authUsername);
            } else if (user.isExternal()) {
                // For users without internal password (e.g. OAuth users) return the API key if exists (it's already encrypted)
                TokenKeyValue token = apiKeyManager.getToken(authUsername);
                if (token != null) {
                    return token.getToken();
                }
                // If no API key rollback to password
                if (StringUtils.isNotBlank(password)) {
                    return createEncryptedPasswordIfNeeded(user, password);
                }
            } else {
                if (StringUtils.isNotBlank(password)) {
                    return createEncryptedPasswordIfNeeded(user, password);
                }
            }
        }
        return null;
    }

    /**
     * Getting a score per repo in the PermissionHeuristicScore range:
     * Rating is by the readability of the repo based on the current user permissions.
     * Highest is admin --> readAll --> readWithExclusion --> readNotAllowed
     * If there is read access:
     * In case repo permission target has no exclusion AND inclusion is default (ANY_PATH)  --> readAll
     * else -->  readWithExclusion
     *
     * @return Score of the repo in the PermissionHeuristicScore
     */
    @Override
    public PermissionHeuristicScore getStrongestReadPermissionTarget(String repoKey) {
        AclCache<RepoPermissionTarget> aclCache = aclStoreService.getAclCache();
        Authentication authentication = AuthenticationHelper.getAuthentication();
        SimpleUser simpleUser = getSimpleUser(authentication);
        if (simpleUser.isEffectiveAdmin()) {
            return PermissionHeuristicScore.admin;
        }
        Set<ArtifactorySid> sids = getUserEffectiveSids(simpleUser);
        PermissionHeuristicScore bestScore = PermissionHeuristicScore.readNotAllowed;
        for (ArtifactorySid sid : sids) {
            Map<String, Map<String, Set<PrincipalPermission<RepoPermissionTarget>>>> map = getAclCacheRelevantMap(
                    aclCache, sid);
            Map<String, Set<PrincipalPermission<RepoPermissionTarget>>> principalRepKeyToAclInfoMap = map
                    .get(sid.getPrincipal());
            // permissions for the current sid only, does not contains all ace in the Acl
            Set<PrincipalPermission<RepoPermissionTarget>> repoAclSet = populateAclInfoSetByRepo(repoKey,
                    principalRepKeyToAclInfoMap);
            // Check for READ permission per acl
            for (PrincipalPermission<RepoPermissionTarget> repoAcl : repoAclSet) {
                if (!isGranted(repoAcl, ArtifactoryPermission.READ, sid.getPrincipal())) {
                    continue;
                }

                RepoPermissionTarget permissionTarget = repoAcl.getPermissionTarget();
                // User has read access, now determine the score
                if (permissionTarget.getExcludes().isEmpty() && permissionTarget.getIncludes().size() == 1 &&
                        ANY_PATH.equals(permissionTarget.getIncludes().get(0))) {
                    bestScore = bestScore.ordinal() < PermissionHeuristicScore.readAll.ordinal() ?
                            PermissionHeuristicScore.readAll : bestScore;
                } else {
                    bestScore = bestScore.ordinal() < PermissionHeuristicScore.readWithExclusion.ordinal() ?
                            PermissionHeuristicScore.readWithExclusion : bestScore;
                }
            }
        }
        return bestScore;
    }

    private RepoPath getRepoPathForBuild(String buildName, String buildNumber, String buildStarted, String permission) {
        RepoPath buildJsonPathInRepo = getBuildJsonPathInRepo(buildName, buildNumber, buildStarted,
                buildService.getBuildInfoRepoKey());
        log.debug("Checking build {} permission for build {}:{}:{} at '{}'", permission, buildName, buildNumber,
                buildStarted, buildJsonPathInRepo);
        return buildJsonPathInRepo;
    }

    private boolean isBuildGlobalBasicReadAllowed() {
        SecurityDescriptor security = centralConfig.getDescriptor().getSecurity();
        boolean buildGlobalBasicReadAllowed = security.isBuildGlobalBasicReadAllowed();
        boolean buildGlobalBasicReadForAnonymous = security.isBuildGlobalBasicReadForAnonymous();
        // For Anonymous user we have to validate anonymous is allowed basic view
        if (isAnonymous()) {
            return buildGlobalBasicReadForAnonymous && buildGlobalBasicReadAllowed;
        }
        // Any other user, only by global basic read flag
        return buildGlobalBasicReadAllowed;
    }

    @Override
    public boolean isBuildBasicRead(String buildName) {
        return isBuildGlobalBasicReadAllowed() || canReadBuild(buildName);
    }

    @Override
    public boolean isBuildBasicRead(String buildName, String buildNumber, String buildStarted) {
        if (isBuildGlobalBasicReadAllowed()) {
            return true;
        }
        return canReadBuild(buildName, buildNumber, buildStarted);
    }

    @Override
    public boolean canReadBuild(String buildName) {
        return hasPermission(getRootPathInRepo(buildName,
                buildService.getBuildInfoRepoKey()), ArtifactoryPermission.READ);
    }

    @Override
    public boolean canReadBuild(String buildName, String buildNumber) {
        Build build = buildService.getLatestBuildByNameAndNumberInternally(buildName, buildNumber);
        if (build == null) {
            log.warn("Couldn't find matching build for {}:{} in order to assert permission", buildName, buildNumber);
            return false;
        }
        return canReadBuild(buildName, buildNumber, build.getStarted());
    }

    @Override
    public boolean canReadBuild(String buildName, String buildNumber, String buildStarted) {
        RepoPath buildPath = getRepoPathForBuild(buildName, buildNumber, buildStarted, READ.getDisplayName());
        return hasPermission(buildPath, ArtifactoryPermission.READ);
    }

    @Override
    public boolean canUploadBuild(String buildName, String buildNumber) {
        Build build = buildService.getLatestBuildByNameAndNumberInternally(buildName, buildNumber);
        if (build == null) {
            log.warn("Couldn't find matching build for {}:{} in order to assert permission", buildName, buildNumber);
            return false;
        }
        return canUploadBuild(buildName, buildNumber, build.getStarted());
    }

    @Override
    public boolean canUploadBuild(String buildName, String buildNumber, String buildStarted) {
        RepoPath buildPath = getRepoPathForBuild(buildName, buildNumber, buildStarted, DEPLOY.getDisplayName());
        return hasPermission(buildPath, ArtifactoryPermission.DEPLOY);
    }

    @Override
    public boolean canDeleteBuild(String buildName) {
        RepoPath buildPath = getRootPathInRepo(buildName, buildService.getBuildInfoRepoKey());
        return hasPermission(buildPath, ArtifactoryPermission.DELETE);
    }

    @Override
    public boolean canDeleteBuild(String buildName, String buildNumber, String buildStarted) {
        RepoPath buildPath = getRepoPathForBuild(buildName, buildNumber, buildStarted, DELETE.getDisplayName());
        return hasPermission(buildPath, ArtifactoryPermission.DELETE);
    }

    @Override
    public boolean canManageBuild(String buildName, String buildNumber, String buildStarted) {
        RepoPath buildPath = getRepoPathForBuild(buildName, buildNumber, buildStarted, MANAGE.getDisplayName());
        return hasPermission(buildPath, ArtifactoryPermission.MANAGE);
    }

    @Override
    public boolean hasPermissionOnAcl(Acl<? extends PermissionTarget> acl, ArtifactoryPermission permission) {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        if (!isAuthenticated(authentication)) {
            return false;
        }
        // Admins has permissions on any target, if not check if has permissions on acl
        return isAdmin(authentication) || hasPermissionOnAcl(acl, permission, getSimpleUser(authentication), true);
    }

    /**
     * Aggregate all aclInfo of a repo + Any_Repo + Any_Local_Repo into one set
     */
    private Set<PrincipalPermission<RepoPermissionTarget>> populateAclInfoSetByRepo(String repoKey,
            Map<String, Set<PrincipalPermission<RepoPermissionTarget>>> principalRepKeyToAclInfoMap) {
        Set<PrincipalPermission<RepoPermissionTarget>> repoAclSet = new HashSet<>();
        if (principalRepKeyToAclInfoMap != null) {
            addAllToAclSetByRepoKey(principalRepKeyToAclInfoMap, repoAclSet, ANY_REPO);
            addAllToAclSetByRepoKey(principalRepKeyToAclInfoMap, repoAclSet, ANY_LOCAL_REPO);
            addAllToAclSetByRepoKey(principalRepKeyToAclInfoMap, repoAclSet, repoKey);
        }
        return repoAclSet;
    }

    /**
     * In case the repo AclInfoMp is not empty --> we add the relevant AclInfo from the map to the aclInfoSet
     */
    private <T extends PermissionTarget> void addAllToAclSetByRepoKey(
            Map<String, Set<PrincipalPermission<T>>> repKeyToAclInfoMap,
            Set<PrincipalPermission<T>> repoAclSet, String key) {
        if (!CollectionUtils.isNullOrEmpty(repKeyToAclInfoMap.get(key))) {
            repoAclSet.addAll(repKeyToAclInfoMap.get(key));
        }
    }

    /**
     * Checks whether the sid is per user or group and returns the relevant map from the acl cache
     *
     * @return Map of user/group name TO Map of repoPath/buildName to aclInfo
     */
    private <T extends RepoPermissionTarget> Map<String, Map<String, Set<PrincipalPermission<T>>>> getAclCacheRelevantMap(
            AclCache<T> aclCache, ArtifactorySid sid) {
        Map<String, Map<String, Set<PrincipalPermission<T>>>> map;
        if (sid.isGroup()) {
            map = aclCache.getGroupResultMap();
        } else {
            map = aclCache.getUserResultMap();
        }
        return map;
    }

    private boolean hasPermissionOnRoot(String repoKey) {
        RepoPath path = InternalRepoPathFactory.repoRootPath(repoKey);
        for (ArtifactoryPermission permission : ArtifactoryPermission.values()) {
            if (hasPermission(path, permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermission(RepoPath repoPath, ArtifactoryPermission permission) {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        if (!isAuthenticated(authentication)) {
            log.debug("Not authenticated for {}", repoPath);
            return false;
        }
        // _intransit system repo is visible only to admin
        if (repoPath.getRepoKey().equals(DistributionConstants.IN_TRANSIT_REPO_KEY) &&
                ArtifactoryPermission.READ.equals(permission)) {
            return isAdmin(authentication);
        }
        // Admins has permissions  for all paths and all repositories
        if (isAdmin(authentication)) {
            return true;
        }
        // Anonymous users are checked only if anonymous access is enabled
        if (isAnonymous() && !isAnonAccessEnabled()) {
            log.debug("Anonymous but AnonAccess disabled for {}", repoPath);
            return false;
        }
        if (RepoPathUtils.isTrash(repoPath) && !isAdmin()) {
            log.debug("Trash repo allowed only for admins for {}", repoPath);
            return false;
        }
        if (SUPPORT_BUNDLE_REPO_NAME.equals(repoPath.getRepoKey()) && !isAdmin()) {
            log.debug("Support bundles allowed only for admins for {}", repoPath);
            return false;
        }
        boolean grantedBySids = isGranted(repoPath, permission, getSimpleUser(authentication).getDescriptor());
        boolean grantedByAccessToken = isGrantedByAccessToken(repoPath, authentication);
        boolean grantedBySignedUrl = isGrantedBySignedUrl(authentication);
        boolean isGranted = grantedBySids ||
                (ArtifactoryPermission.READ.equals(permission) && (grantedByAccessToken || grantedBySignedUrl));
        log.debug("Access granted: {}. Granted by access token: {}. Granted by signed URL: {} for {}",
                grantedBySids, grantedByAccessToken, grantedBySignedUrl, repoPath);
        return isGranted;
    }

    /**
     * If authentication is of type {@link AccessTokenAuthentication}, test it's scope tokens for read access from a
     * {@link ArtifactoryRepoPathScopeToken}
     */
    private boolean isGrantedByAccessToken(RepoPath repoPath, Authentication authentication) {
        if (authentication instanceof AccessTokenAuthentication) {
            AccessTokenAuthentication accessTokenAuthentication = (AccessTokenAuthentication) authentication;
            String repoPathChecksum = calculateRepoPathCheckSum(repoPath);
            List<String> scope = accessTokenAuthentication.getAccessToken().getScope();
            return scope.stream().anyMatch(scopeToken -> isGrantedByAccessScopeToken(scopeToken, repoPathChecksum));
        }
        return false;
    }

    /**
     * Check if the scope token is of type {@link ArtifactoryRepoPathScopeToken}, and then compare repoPath checksum with token checksum
     */
    private boolean isGrantedByAccessScopeToken(String scopeToken, String repoPathChecksum) {
        return ArtifactoryRepoPathScopeToken.accepts(scopeToken) &&
                repoPathChecksum.equals(ArtifactoryRepoPathScopeToken.getPath(scopeToken));
    }

    private String calculateRepoPathCheckSum(RepoPath repoPath) {
        String repoPathName = repoPath.getRepoKey() + RepoPath.PATH_SEPARATOR + repoPath.getPath();
        return ChecksumUtils.calculateSha1(new ByteArrayInputStream(repoPathName.getBytes()));
    }

    private boolean isGrantedBySignedUrl(Authentication authentication) {
        // If this is a SignedUrlAuthenticationToken then all security checks have already been made
        return (authentication instanceof SignedUrlAuthenticationToken);
    }

    private boolean hasPermission(SimpleUser user, RepoPath repoPath, ArtifactoryPermission permission) {
        // Admins has permissions for all paths and all repositories
        if (user.isEffectiveAdmin()) {
            return true;
        }

        // Anonymous users are checked only if anonymous access is enabled
        if (user.isAnonymous() && !isAnonAccessEnabled()) {
            return false;
        }
        return isGranted(repoPath, permission, user.getDescriptor());
    }

    private boolean hasPermission(GroupInfo group, RepoPath repoPath, ArtifactoryPermission permission) {
        if (group.isAdminPrivileges()) {
            return true;
        }
        return isGranted(repoPath, permission, group);
    }

    private boolean isPermissionTargetIncludesRepoKey(String repoKey, RepoPermissionTarget permissionTarget) {
        // checks if repo key is part of the permission target repository keys taking into account
        // the special logical repo keys of a permission target like "Any", "All Local" etc.
        List<String> repoKeys = permissionTarget.getRepoKeys();
        return repoKeys.contains(ANY_REPO) || repoKeys.contains(repoKey) ||
                (repoKeys.contains(ANY_REMOTE_REPO) && isRemote(repoKey)) ||
                (repoKeys.contains(ANY_LOCAL_REPO) && isLocal(repoKey)) ||
                (repoKeys.contains(ANY_DISTRIBUTION_REPO) && isDistribution(repoKey));
    }

    private boolean isRemote(String repoKey) {
        if (StringUtils.endsWith(repoKey, REMOTE_CACHE_SUFFIX)) {
            if (repositoryService.localRepositoryByKey(repoKey) != null) {
                return false;
            }
            repoKey = StringUtils.removeEnd(repoKey, REMOTE_CACHE_SUFFIX);
        }
        return repositoryService.remoteRepositoryByKey(repoKey) != null;
    }

    private boolean isLocal(String repoKey) {
        return repositoryService.localRepositoryByKey(repoKey) != null;
    }

    private boolean isDistribution(String repoKey) {
        return repositoryService.distributionRepoByKey(repoKey) != null;
    }

    private boolean hasPermissionOnPermissionTarget(PermissionTarget permTarget, ArtifactoryPermission permission) {
        Acl<? extends PermissionTarget> acl;
        String permTargetName = permTarget.getName();
        if (permTarget instanceof BuildPermissionTarget) {
            acl = aclStoreService.getBuildAcl(permTargetName);
        } else if (permTarget instanceof ReleaseBundlePermissionTarget) {
            acl = aclStoreService.getReleaseBundleAcl(permTargetName);
        } else if (permTarget instanceof RepoPermissionTarget) {
            acl = aclStoreService.getRepoAcl(permTargetName);
        } else {
            throw new IllegalStateException(
                    "Permission target '" + permTargetName + "' is not of allowed types repo/build/release-bundle");
        }
        return hasPermissionOnAcl(acl, permission);
    }

    private boolean hasPermissionOnPermissionTarget(PermissionTarget permTarget, ArtifactoryPermission permission,
            SimpleUser user) {
        RepoAcl acl = aclStoreService.getRepoAcl(permTarget.getName());
        return hasPermissionOnAcl(acl, permission, user, true);
    }

    /**
     * @param includeEffective True will include all the groups that the user is part of. False will not.
     */
    private boolean hasPermissionOnAcl(Acl<? extends PermissionTarget> acl, ArtifactoryPermission permission,
            SimpleUser user, boolean includeEffective) {
        // Admins has permissions on any target, if not check if has permissions
        if (user.isEffectiveAdmin()) {
            return true;
        }
        return includeEffective ? isGranted(acl, permission, user.getDescriptor()) :
                isGrantedWithoutEffective(acl, permission, user.getDescriptor());
    }


    private boolean hasPermissionOnAcl(Acl<? extends PermissionTarget> acl, ArtifactoryPermission permission,
            GroupInfo group) {
        // Admins has permissions on any target, if not check if has permissions
        return group.isAdminPrivileges() || isGranted(acl, permission, group);
    }

    private boolean isGranted(Acl<? extends PermissionTarget> acl, ArtifactoryPermission permission, UserInfo user) {
        Set<UserGroupInfo> groupInfos = user.getGroupsReference();
        for (AceInfo ace : acl.getMutableAces()) {
            if (!ace.isGroup()) {
                if (isGranted(ace, permission, user.getUsername())) {
                    return true;
                }
            } else {
                for (UserGroupInfo groupInfo : groupInfos) {
                    if (isGranted(ace, permission, groupInfo.getGroupName())) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    private boolean isGrantedWithoutEffective(Acl<? extends PermissionTarget> acl, ArtifactoryPermission permission,
            UserInfo user) {
        for (AceInfo ace : acl.getMutableAces()) {

            if (!ace.isGroup() && isGranted(ace, permission, user.getUsername())) {
                return true;
            }
        }
        return false;
    }

    private boolean isGranted(Acl<? extends PermissionTarget> acl, ArtifactoryPermission permission,
            GroupInfo groupInfo) {
        for (AceInfo ace : acl.getMutableAces()) {
            if (ace.isGroup() && isGranted(ace, permission, groupInfo.getGroupName())) {
                    return true;
            }
        }
        return false;
    }

    private boolean isGranted(PrincipalPermission<? extends PermissionTarget> acl, ArtifactoryPermission permission,
            String principal) {
        AceInfo ace = acl.getAce();
        return isGranted(ace, permission, principal);
    }

    private boolean isGranted(AceInfo ace, ArtifactoryPermission permission, String principal) {
        if (ace == null) {
            log.debug("Invalid null Ace.");
            return false;
        }
        if (!StringUtils.equals(principal, ace.getPrincipal())) {
            return false;
        }
        //Any of the permissions is enough for granting
        return (ace.getMask() & permission.getMask()) > 0;
    }

    private <T extends RepoPermissionTarget> boolean isGranted(RepoPath repoPath, ArtifactoryPermission permission,
            UserInfo user) {
        Set<UserGroupInfo> groupInfos = user.getGroupsReference();
        AclCache<T> aclCache = getAclCacheByType(repoPath);
        Map<String, Map<String, Set<PrincipalPermission<T>>>> aclCacheUserResultMap = aclCache.getUserResultMap();
        List<String> remoteRepoKeyAclCompatible = makeRemoteRepoKeyAclCompatible(repoPath.getRepoKey());
        if (getAndCheckSidPermissions(repoPath, remoteRepoKeyAclCompatible, permission, user.getUsername(),
                aclCacheUserResultMap)) {
            return true;
        }
        aclCacheUserResultMap = aclCache.getGroupResultMap();
        for (UserGroupInfo groupInfo : groupInfos) {
            if (getAndCheckSidPermissions(repoPath, remoteRepoKeyAclCompatible, permission, groupInfo.getGroupName(),
                    aclCacheUserResultMap)) {
                return true;
            }
        }

        return false;
    }

    private <T extends RepoPermissionTarget> boolean isGranted(RepoPath repoPath, ArtifactoryPermission permission,
            GroupInfo groupInfo) {
        AclCache<T> aclCache = getAclCacheByType(repoPath);
        Map<String, Map<String, Set<PrincipalPermission<T>>>> map = aclCache.getGroupResultMap();
        List<String> remoteRepoKeyAclCompatible = makeRemoteRepoKeyAclCompatible(repoPath.getRepoKey());
        return getAndCheckSidPermissions(repoPath, remoteRepoKeyAclCompatible, permission, groupInfo.getGroupName(),
                map);
    }

    private <T extends RepoPermissionTarget> AclCache<T> getAclCacheByType(RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        if (isBuildInfoRepo(repoKey)) {
            return aclStoreService.getBuildsAclCache();
        }
        if (isReleaseBundlesRepo(repoKey)) {
            if (hasEdgeOrEnterprisePlusLic()) {
                return aclStoreService.getReleaseBundlesAclCache();
            }
            return new AclCache<>(Maps.newHashMap(), Maps.newHashMap());
        }
        return aclStoreService.getAclCache();
    }

    private boolean isBuildInfoRepo(String repoKey) {
        return StringUtils.equals(repoKey, buildService.getBuildInfoRepoKey());
    }

    private boolean isReleaseBundlesRepo(String repoKey) {
        return StringUtils.equals(repoKey, RELEASE_BUNDLE_DEFAULT_REPO) ||
                repositoryService.releaseBundlesRepoDescriptorByKey(repoKey) != null;
    }

    private <T extends RepoPermissionTarget> boolean getAndCheckSidPermissions(RepoPath repoPath,
            List<String> remoteRepoKeyAclCompatible,
            ArtifactoryPermission permission,
            String principal, Map<String, Map<String, Set<PrincipalPermission<T>>>> map) {

        Map<String, Set<PrincipalPermission<T>>> repoSidPermissions = map.get(principal);
        if (repoSidPermissions != null) {
            if (getAndCheckAcl(repoPath.getRepoKey(),
                    ImmutableList.<String>builder().addAll(remoteRepoKeyAclCompatible)
                            .addAll(getRemoteLocalReposVariation(repoPath.getRepoKey())).build(),
                    repoSidPermissions, repoPath,
                    permission, principal)) {
                return true;
            }
            // Build/Release Bundle permissions are not contained in "ANY REPO", no need to check
            if (isBuildInfoRepo(repoPath.getRepoKey()) || isReleaseBundlesRepo(repoPath.getRepoKey())) {
                return false;
            }
            if (getAndCheckAcl(ANY_LOCAL_REPO, remoteRepoKeyAclCompatible, repoSidPermissions, repoPath, permission,
                    principal)) {
                return true;
            }
            if (getAndCheckAcl(ANY_REMOTE_REPO, remoteRepoKeyAclCompatible, repoSidPermissions, repoPath, permission,
                    principal)) {
                return true;
            }
            if (getAndCheckAcl(ANY_DISTRIBUTION_REPO, remoteRepoKeyAclCompatible, repoSidPermissions, repoPath,
                    permission, principal)) {
                return true;
            }
            if (getAndCheckAcl(ANY_REPO, remoteRepoKeyAclCompatible, repoSidPermissions, repoPath, permission,
                    principal)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getRemoteLocalReposVariation(String repoKey) {
        String realRepoName;
        boolean repoNameSuspectedAsRemote = repoKey.endsWith(LocalCacheRepoDescriptor.PATH_SUFFIX);
        if (repoNameSuspectedAsRemote) {
            realRepoName = repoKey.substring(0, repoKey.lastIndexOf(LocalCacheRepoDescriptor.PATH_SUFFIX));
        } else {
            realRepoName = repoKey;
        }

        boolean remote = repositoryService.remoteRepoDescriptorByKey(realRepoName) != null;

        if (!remote && !repoNameSuspectedAsRemote &&
                ConstantValues.applyRemoteReposPermissionsOnLocalRepos.getBoolean()) {
            return ImmutableList.of(repoKey.concat(LocalCacheRepoDescriptor.PATH_SUFFIX));
        }

        if (remote && repoNameSuspectedAsRemote &&
                ConstantValues.applyLocalReposPermissionsOnRemoteRepos.getBoolean()) {
            return ImmutableList.of(realRepoName);
        }

        return ImmutableList.of();
    }

    /**
     * @param repoSidPermissions Map of repoPath to aclInfo
     */
    private <T extends RepoPermissionTarget> boolean getAndCheckAcl(String checkedRepo,
            List<String> remoteRepoKeyAclCompatible,
            Map<String, Set<PrincipalPermission<T>>> repoSidPermissions, RepoPath repoPath,
            ArtifactoryPermission permission,
            String principal) {
        Set<PrincipalPermission<T>> allItemPermissions = repoSidPermissions.get(checkedRepo);

        if (allItemPermissions == null && MapUtils.isNotEmpty(repoSidPermissions)) {
            allItemPermissions = StreamSupportUtils.stream(remoteRepoKeyAclCompatible)
                    .map(repoSidPermissions::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        if (allItemPermissions != null) {
            List<String> remoteRepoKeyAclCompatibleWithRepoKey = ImmutableList.<String>builder()
                    .add(repoPath.getRepoKey())
                    .addAll(remoteRepoKeyAclCompatible).build();
            return permissionCheckOnAcl(allItemPermissions, repoPath, remoteRepoKeyAclCompatibleWithRepoKey, permission,
                    principal);
        }
        return false;
    }

    <T extends RepoPermissionTarget> boolean permissionCheckOnAcl(
            Collection<PrincipalPermission<T>> principalPermissions,
            RepoPath repoPath,
            List<String> remoteRepoKeyAclCompatibleList,
            ArtifactoryPermission permissionType, String principal) {
        String path = getPathToCheckPermissionFor(repoPath);
        boolean folder = repoPath.isFolder();
        boolean checkPartialPath = (permissionType.getMask() &
                (ArtifactoryPermission.READ.getMask() | ArtifactoryPermission.DEPLOY.getMask())) != 0;
        boolean behaveAsFolder = folder && checkPartialPath;

        for (PrincipalPermission<T> principalPermission : principalPermissions) {
            T aclPermissionTarget = principalPermission.getPermissionTarget();
            boolean anyMatch = StreamSupportUtils.stream(remoteRepoKeyAclCompatibleList)
                    .anyMatch(key -> isPermissionTargetIncludesRepoKey(key, aclPermissionTarget));
            if (anyMatch) {
                boolean pathMatch = matches(aclPermissionTarget, path, behaveAsFolder);
                if (pathMatch && isGranted(principalPermission, permissionType, principal)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * If the path being checked belongs to the build-info repo the path being checked needs to be URL decoded, since
     * build names and numbers are specially encoded upon deployment using {@link org.artifactory.util.encoding.ArtifactoryBuildRepoPathElementsEncoder}
     */
    private String getPathToCheckPermissionFor(RepoPath repoPath) {
        String path = repoPath.getPath();
        if (buildService.getBuildInfoRepoKey().equals(repoPath.getRepoKey())) {
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.warn(
                        "Failed to decode path '{}' for permission check, this will produce non-deterministic behavior: {}",
                        repoPath.toPath(), e.getMessage());
                log.debug("", e);
            }
        }
        return path;
    }

    @Override
    public void exportTo(ExportSettings settings) {
        throw new UnsupportedOperationException("Logic moved to access");
    }

    /**
     * @deprecated as access should manage security data
     */
    @Deprecated
    @Override
    public SecurityInfo getSecurityData() {
        List<UserInfo> users = getAllUsers(true, true);
        List<GroupInfo> groups = getAllGroups();
        List<RepoAcl> repoAcls = getAllRepoAcls();
        List<BuildAcl> buildAcls = getAllBuildAcls();
        List<ReleaseBundleAcl> releaseBundleAcls = getAllReleaseBundleAcls(false);
        SecurityInfo descriptor = InfoFactoryHolder.get()
                .createSecurityInfo(users, groups, repoAcls, buildAcls, releaseBundleAcls);
        descriptor.setVersion(SecurityVersion.last().name());
        return descriptor;
    }

    @Override
    public void importFrom(ImportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        status.status("Importing security...", log);
        invalidateAclAndGroupCaches();
        importSecurityXml(settings, status);
        // TODO: [NS] find a better way? Must run this converter post to all access related tasks, and this is definitely
        // TODO: a concern of the security service
        ArtifactoryVersion backupVersion = BackupUtils.findVersion(settings.getBaseDir());
        if (backupVersion.before(AccessConverters.DECRYPT_USERS_VERSION)) {
            accessConverters.getUserCustomDataDecryptionConverter().convert();
        }
    }

    private void importSecurityXml(ImportSettings settings, MutableStatusHolder status) {
        //Import the new security definitions
        File baseDir = settings.getBaseDir();
        // First check for security.xml file
        File securityXmlFile = new File(baseDir, FILE_NAME);
        if (!securityXmlFile.exists()) {
            log.info("Security file {} does not exist no import of security will" +
                    " be done (access import should handle security for new imports)", securityXmlFile);
            return;
        }
        SecurityInfo securityInfo;
        try {
            securityInfo = new SecurityInfoReader().read(securityXmlFile);
        } catch (Exception e) {
            status.warn("Could not read security file", log);
            return;
        }
        SecurityService me = InternalContextHelper.get().beanForType(SecurityService.class);
        me.importSecurityData(securityInfo);
    }

    @Override
    public void createDefaultAdminUser() {
        log.info("Creating the default super user: '{}', since no admin user exists!", DEFAULT_ADMIN_USER);
        UserInfo defaultAdmin = userGroupStoreService.findUser(DEFAULT_ADMIN_USER);

        UserInfoBuilder builder = new UserInfoBuilder(DEFAULT_ADMIN_USER)
                .password(new SaltedPassword(DEFAULT_ADMIN_PASSWORD, null))
                .admin(true)
                .updatableProfile(true)
                .enabled(true);

        if (defaultAdmin != null) {
            log.error("No admin user where found, but the default user: '{}' exists and is not admin!\nUpdating the super user: '{}' with default state and password!",
                    DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER);
            builder
                    .email(defaultAdmin.getEmail())
                    .lastLogin(defaultAdmin.getLastLoginTimeMillis(), defaultAdmin.getLastLoginClientIp())
                    .updatableProfile(true);

            updateUser(builder.build(), false);
        } else {
            createUser(builder.build());
        }
    }

    @Override
    public void createDefaultAnonymousUser() {
        UserInfo anonymousUser = userGroupStoreService.findUser(UserInfo.ANONYMOUS);
        if (anonymousUser != null) {
            log.debug("Anonymous user: '{}' already exists", anonymousUser);
            return;
        }
        log.info("Creating the default anonymous user, since it does not exist!");
        UserInfoBuilder builder = new UserInfoBuilder(UserInfo.ANONYMOUS);
        builder.password(new SaltedPassword(null, null)).email(null).enabled(true).updatableProfile(false);
        MutableUserInfo anonUser = builder.build();
        boolean createdAnonymousUser = createUser(anonUser);

        if (createdAnonymousUser) {
            MutableGroupInfo readersGroup = InfoFactoryHolder.get().createGroup(READERS);
            readersGroup.setRealm(SecurityConstants.DEFAULT_REALM);
            readersGroup.setDescription("A group for read-only users");
            readersGroup.setNewUserDefault(true);
            createGroup(readersGroup);
            createDefaultSecurityEntities(anonUser, readersGroup, currentUsername());
        }
    }

    /**
     * Called after the anonymous user is created for the first time
     */
    @Override
    public void createDefaultSecurityEntities(UserInfo anonUser, GroupInfo readersGroup, String currentUsername) {
        if (!UserInfo.ANONYMOUS.equals(anonUser.getUsername())) {
            throw new IllegalArgumentException(
                    "Default anything permissions should be created for the anonymous user only");
        }

        // create or update read permissions on "anything"
        createPermissionAnything(anonUser, readersGroup, currentUsername);

        // create or update read and deploy permissions on all remote repos
        RepoAcl anyRemoteAcl = getRepoAcl(ANY_REMOTE_PERMISSION_TARGET_NAME);
        HashSet<AceInfo> anyRemoteAces = new HashSet<>(2);
        anyRemoteAces.add(InfoFactoryHolder.get().createAce(
                anonUser.getUsername(), false,
                READ.getMask() | DEPLOY.getMask()));
        if (anyRemoteAcl == null) {
            MutableRepoPermissionTarget anyRemoteTarget =
                    InfoFactoryHolder.get().createRepoPermissionTarget(ANY_REMOTE_PERMISSION_TARGET_NAME,
                            Lists.newArrayList(ANY_REMOTE_REPO));
            anyRemoteTarget.setIncludesPattern(ANY_PATH);
            anyRemoteAcl = InfoFactoryHolder.get()
                    .createRepoAcl(anyRemoteTarget, anyRemoteAces, currentUsername, ClockUtils.epochMillis());
            aclStoreService.createAcl(anyRemoteAcl);
        } else {
            MutableAcl<RepoPermissionTarget> acl = InfoFactoryHolder.get()
                    .createRepoAcl(anyRemoteAcl.getPermissionTarget());
            acl.setAces(anyRemoteAces);
            acl.setUpdatedBy(currentUsername);
            aclStoreService.updateAcl(acl);
        }
    }

    private void invalidateAclAndGroupCaches() {
        publisher.publishEvent(new InvalidateCacheEvent(this, CacheType.ACL));
        publisher.publishEvent(new InvalidateCacheEvent(this, CacheType.GROUPS));
    }

    private void createPermissionAnything(UserInfo anonUser, GroupInfo readersGroup, String currentUsername) {
        createPermissionAnythingPerType(anonUser, readersGroup, currentUsername, REPO);
        createPermissionAnythingPerType(anonUser, readersGroup, currentUsername, BUILD);
    }

    private void createPermissionAnythingPerType(UserInfo anonUser, GroupInfo readersGroup, String currentUsername,
            ArtifactoryResourceType type) {
        Acl anyAnyAcl = getAclByType(type);
        Set<AceInfo> anyAnyAces = new HashSet<>(2);
        anyAnyAces.add(InfoFactoryHolder.get().createAce(
                anonUser.getUsername(), false, ArtifactoryPermission.READ.getMask()));
        anyAnyAces.add(InfoFactoryHolder.get().createAce(
                readersGroup.getGroupName(), true, ArtifactoryPermission.READ.getMask()));
        if (anyAnyAcl == null) {
            MutablePermissionTarget anyAnyTarget = createPermissionTargetByType(type);
            anyAnyAcl = createNewAclByType(currentUsername, anyAnyAces, anyAnyTarget, type);
            aclStoreService.createAcl(anyAnyAcl);
        } else {
            MutableAcl<? extends PermissionTarget> acl = copyPermissionTargetByType(anyAnyAcl.getPermissionTarget(),
                    type);
            acl.setAces(anyAnyAces);
            acl.setUpdatedBy(currentUsername);
            aclStoreService.updateAcl(acl);
        }
    }

    private MutableAcl<? extends PermissionTarget> copyPermissionTargetByType(PermissionTarget permissionTarget,
            ArtifactoryResourceType type) {
        switch (type) {
            case REPO:
                return InfoFactoryHolder.get().createRepoAcl((RepoPermissionTarget) permissionTarget);
            case BUILD:
                return InfoFactoryHolder.get().createBuildAcl((BuildPermissionTarget) permissionTarget);
            default:
                throw new IllegalStateException("No valid type detected for default ACL creation");
        }

    }

    private Acl createNewAclByType(String currentUsername, Set<AceInfo> anyAnyAces,
            MutablePermissionTarget anyAnyTarget, ArtifactoryResourceType type) {
        switch (type) {
            case REPO:
                return InfoFactoryHolder.get().createRepoAcl((RepoPermissionTarget) anyAnyTarget, anyAnyAces,
                        currentUsername, ClockUtils.epochMillis());
            case BUILD:
                return InfoFactoryHolder.get().createBuildAcl((BuildPermissionTarget) anyAnyTarget, anyAnyAces,
                        currentUsername, ClockUtils.epochMillis());
            default:
                throw new IllegalStateException("No valid type detected for default ACL creation");
        }

    }

    private Acl getAclByType(ArtifactoryResourceType type) {
        switch (type) {
            case REPO:
                return getRepoAcl(ANY_PERMISSION_TARGET_NAME);
            case BUILD:
                return getBuildAcl(ANY_PERMISSION_TARGET_NAME);
            default:
                throw new IllegalStateException("No valid type detected for default ACL creation");
        }
    }

    private MutablePermissionTarget createPermissionTargetByType(ArtifactoryResourceType type) {
        switch (type) {
            case REPO:
                MutableRepoPermissionTarget repoPermissionTarget = InfoFactoryHolder.get().createRepoPermissionTarget(
                        ANY_PERMISSION_TARGET_NAME, Lists.newArrayList((ANY_REPO)));
                repoPermissionTarget.setIncludesPattern(ANY_PATH);
                return repoPermissionTarget;
            case BUILD:
                MutableBuildPermissionTarget buildPermissionTarget = InfoFactoryHolder.get()
                        .createBuildPermissionTarget(
                                ANY_PERMISSION_TARGET_NAME, Lists.newArrayList((buildService.getBuildInfoRepoKey())));
                buildPermissionTarget.setIncludesPattern(ANY_PATH);
                return buildPermissionTarget;
            default:
                throw new IllegalStateException("No valid type detected for default ACL creation");
        }
    }

    @Override
    public void importSecurityData(String securityXml) {
        importSecurityData(new SecurityInfoReader().read(securityXml), false);
    }

    @Override
    public void importSecurityData(String securityXml, boolean override) {
        importSecurityData(new SecurityInfoReader().read(securityXml), override);
    }

    @Override
    public void importSecurityData(SecurityInfo securityInfo) {
        importSecurityData(securityInfo, false);
    }

    private void importSecurityData(SecurityInfo securityInfo, boolean override) {
        interceptors.onBeforeSecurityImport(securityInfo);
        clearSecurityListeners();

        try {
            accessService.importSecurityEntities(securityInfo, override);
            decryptAllUserProps();
        } finally {
            invalidateAclAndGroupCaches();
        }

        boolean hasAnonymous =
                securityInfo.getUsers() != null && securityInfo.getUsers().stream().anyMatch(UserInfo::isAnonymous);
        if (!hasAnonymous) {
            createDefaultAnonymousUser();
        }

        interceptors.onAfterSecurityImport(securityInfo);
    }

    @Override
    public void decryptAllUserProps() {
        try {
            accessService.getAccessClient().users().findUsers().getUsers().forEach(user -> {
                MutableBoolean updated = new MutableBoolean(false);
                UpdateUserRequest updateRequest = UpdateUserRequest.create();
                updateRequest.username(user.getUsername());

                // Converting only custom data that was changed because of cryptographic changes
                // Running through UserMapper to decrypt all relevant data
                UserRequest newUser = UserMapper.toAccessUser(UserMapper.toArtifactoryUser(user), true);

                Optional.ofNullable(newUser.getCustomData())
                        .orElseGet(HashMap::new).forEach((propKey, newValue) -> {
                    if ((newValue.isSensitive() || UserPropertiesSearchHelper.hasSearchableSuffix(propKey)) &&
                            !newValue.getValue().equals(user.getCustomData(propKey))) {
                        log.debug("Updating property {} of user: '{}'", propKey, user.getUsername());
                        updateRequest.addCustomData(propKey, newValue.getValue(), newValue.isSensitive());
                        updated.setTrue();
                    }
                });

                if (updated.booleanValue()) {
                    accessService.getAccessClient().users().updateUser(updateRequest);
                }
            });
        } catch (Exception e) {
            log.error("Could not decrypt user props/keys, cause: ", e);
            throw new StorageException("Could not decrypt user props/keys, see logs for more details");
        }
    }

    @Override
    public void addListener(SecurityListener listener) {
        securityListeners.add(listener);
    }

    @Override
    public void removeListener(SecurityListener listener) {
        securityListeners.remove(listener);
    }

    @Override
    public void authenticateAsSystem() {
        SecurityContextHolder.getContext().setAuthentication(new SystemAuthenticationToken());
    }

    @Override
    public void doAsSystem(@Nonnull Runnable runnable) {
        Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            authenticateAsSystem();
            runnable.run();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
        }
    }

    @Override
    public void nullifyContext() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Override
    public SaltedPassword generateSaltedPassword(String rawPassword) {
        return generateSaltedPassword(rawPassword, getDefaultSalt());
    }

    @Override
    public SaltedPassword generateSaltedPassword(@Nonnull String rawPassword, @Nullable String salt) {
        return new SaltedPassword(rawPassword, salt);
    }

    @Override
    public String getDefaultSalt() {
        return ConstantValues.defaultSaltValue.getString();
    }

    @Override
    public BasicStatusHolder testLdapConnection(LdapSetting ldapSetting, String username, String password) {
        return ldapService.testLdapConnection(ldapSetting, username, password);
    }

    @Override
    public boolean isPasswordEncryptionEnabled() {
        CentralConfigDescriptor cc = centralConfig.getDescriptor();
        return cc.getSecurity().getPasswordSettings().isEncryptionEnabled();
    }

    @Override
    public boolean userPasswordMatches(String passwordToCheck) {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        return authentication != null && passwordToCheck.equals(authentication.getCredentials());
    }

    @Override
    public boolean canDeployToLocalRepository() {
        return !repositoryService.getDeployableRepoDescriptors().isEmpty();
    }

    private void clearSecurityListeners() {
        //Notify security listeners
        securityListeners.forEach(SecurityListener::onClearSecurity);
    }

    private void assertAdmin() {
        if (!isAdmin()) {
            throw new SecurityException(
                    "The attempted action is permitted to users with administrative privileges only.");
        }
    }

    /**
     * Validates that the edited given permission target is not different from the existing one. This method should be
     * called before an ACL is being modified by a non-sys-admin user
     *
     * @param newInfo Edited permission target
     * @throws AuthorizationException Thrown in case an unauthorized modification has occurred
     */
    private <T extends PermissionTarget> void validateUnmodifiedPermissionTarget(T newInfo)
            throws AuthorizationException {
        if (newInfo == null) {
            return;
        }

        Acl oldAcl = getAclByPermissionTargetType(newInfo);
        if (oldAcl == null) {
            return;
        }

        PermissionTarget oldInfo = oldAcl.getPermissionTarget();
        if (oldInfo == null) {
            return;
        }

        if (newInfo instanceof RepoPermissionTarget) {
            validateUnmodifiedRepoPermission((RepoPermissionTarget) newInfo, (RepoPermissionTarget) oldInfo);
        }

        List<String> oldEntities = getEntitiesFromPermissionTargetByType(oldInfo);
        List<String> newEntities = getEntitiesFromPermissionTargetByType(newInfo);
        Sets.SetView<String> entities = Sets.symmetricDifference(Sets.newHashSet(oldEntities),
                Sets.newHashSet(newEntities));
        if (!entities.isEmpty()) {
            alertModifiedField("repositories, builds or release-bundles");
        }
    }

    private void validateUnmodifiedRepoPermission(RepoPermissionTarget newInfo, RepoPermissionTarget oldInfo) {
        Sets.SetView<String> excludes = Sets.symmetricDifference(Sets.newHashSet(oldInfo.getExcludes()),
                Sets.newHashSet(newInfo.getExcludes()));
        if (!excludes.isEmpty()) {
            alertModifiedField("excludes pattern");
        }

        if (!oldInfo.getExcludesPattern().equals(newInfo.getExcludesPattern())) {
            alertModifiedField("exclude pattern");
        }

        Sets.SetView<String> includes = Sets.symmetricDifference(Sets.newHashSet(oldInfo.getIncludes()),
                Sets.newHashSet(newInfo.getIncludes()));
        if (!includes.isEmpty()) {
            alertModifiedField("include pattern");
        }

        if (!oldInfo.getIncludesPattern().equals(newInfo.getIncludesPattern())) {
            alertModifiedField("include pattern");
        }
    }

    private Acl getAclByPermissionTargetType(PermissionTarget permissionTarget) {
        String permissionTargetName = permissionTarget.getName();
        if (permissionTarget instanceof BuildPermissionTarget) {
            return getBuildAcl(permissionTargetName);
        }
        if (permissionTarget instanceof ReleaseBundlePermissionTarget) {
            return getReleaseBundleAcl(permissionTargetName);
        }
        if (permissionTarget instanceof RepoPermissionTarget) {
            return getRepoAcl(permissionTargetName);
        }
        log.warn("Permission target {} is not of type repo/build/release-bundles", permissionTargetName);
        return null;
    }

    private List<String> getEntitiesFromPermissionTargetByType(PermissionTarget permissionTarget) {
        if (permissionTarget instanceof BuildPermissionTarget) {
            return ((BuildPermissionTarget) permissionTarget).getRepoKeys();
        }
        if (permissionTarget instanceof ReleaseBundlePermissionTarget) {
            return ((ReleaseBundlePermissionTarget) permissionTarget).getRepoKeys();
        }
        if (permissionTarget instanceof RepoPermissionTarget) {
            // make repo keys compatible with acl cached data
            return makeRemoteRepoKeysAclCompatible(((RepoPermissionTarget) permissionTarget).getRepoKeys());
        }
        return Collections.emptyList();
    }

    /**
     * Throws an AuthorizationException alerting an un-authorized change of configuration
     *
     * @param modifiedFieldName Name of modified field
     */
    private void alertModifiedField(String modifiedFieldName) {
        throw new AuthorizationException("User is not permitted to modify " + modifiedFieldName);
    }

    /**
     * Retrieves the Async advised instance of the service
     *
     * @return InternalSecurityService - Async advised instance
     */
    private InternalSecurityService getAdvisedMe() {
        return context.beanForType(InternalSecurityService.class);
    }

    @Override
    public List<String> convertCachedRepoKeysToRemote(List<String> repoKeys) {
        List<String> altered = Lists.newArrayList();
        for (String repoKey : repoKeys) {
            String repoKeyCacheOmitted;

            if (repoKey.contains(LocalCacheRepoDescriptor.PATH_SUFFIX)) {
                repoKeyCacheOmitted = repoKey.substring(0,
                        repoKey.lastIndexOf(LocalCacheRepoDescriptor.PATH_SUFFIX.charAt(0)));
            } else {
                altered.add(repoKey);
                continue;
            }
            if (repositoryService.remoteRepoDescriptorByKey(repoKeyCacheOmitted) != null) {
                altered.add(repoKeyCacheOmitted);
            } else {
                altered.add(repoKey); //Its Possible that someone named their local repo '*-cache'
            }
        }
        return altered;
    }

    /**
     * Converts remote repo keys contained in the list to have the '-cache' suffix as acls currently
     * only support this notation.
     *
     * @return repoKeys with all remote repository keys concatenated with '-cache' suffix
     */
    private List<String> makeRemoteRepoKeysAclCompatible(List<String> repoKeys) {
        List<String> altered = Lists.newArrayList();
        for (String repoKey : repoKeys) {
            if (repositoryService.remoteRepoDescriptorByKey(repoKey) != null) {
                altered.add(repoKey.concat(LocalCacheRepoDescriptor.PATH_SUFFIX));
            } else {
                altered.add(repoKey);
            }
        }
        return altered;
    }

    private List<String> makeRemoteRepoKeyAclCompatible(String repoKey) {
        return makeRemoteRepoKeysAclCompatible(ImmutableList.of(repoKey));
    }

    private MutableRepoAcl makeNewAclRemoteRepoKeysAclCompatible(MutableRepoAcl acl) {
        //Make repository keys acl-compatible before update
        MutableRepoPermissionTarget mutablePermissionTargetInfo = InfoFactoryHolder.get().copyRepoPermissionTarget
                (acl.getPermissionTarget());
        List<String> compatibleRepoKeys = makeRemoteRepoKeysAclCompatible(mutablePermissionTargetInfo.getRepoKeys());
        mutablePermissionTargetInfo.setRepoKeys(compatibleRepoKeys);
        acl.setPermissionTarget(mutablePermissionTargetInfo);
        return acl;
    }

    @Override
    public RepoAcl convertNewAclCachedRepoKeysToRemote(MutableRepoAcl acl) {
        //Make repository keys acl-compatible before update
        MutableRepoPermissionTarget mutablePermissionTargetInfo = InfoFactoryHolder.get().copyRepoPermissionTarget
                (acl.getPermissionTarget());
        List<String> compatibleRepoKeys = convertCachedRepoKeysToRemote(mutablePermissionTargetInfo.getRepoKeys());
        mutablePermissionTargetInfo.setRepoKeys(compatibleRepoKeys);
        acl.setPermissionTarget(mutablePermissionTargetInfo);
        return acl;
    }

    /**
     * Makes user password expired
     */
    @Override
    public void expireUserCredentials(String userName) {
        try {
            if (unknownUsersCache.getIfPresent(userName) != null) {
                log.debug("The user: '{}' is registered in unknown users cache, no password to expire ...", userName);
                throw new UsernameNotFoundException(ERR_USER + userName + " does not exist");
            }
            UserInfo user = findUser(userName);
            if (!user.isCredentialsExpired()) {
                userGroupStoreService.expireUserPassword(userName);
                invalidateAuthCacheEntries(userName);
                dockerTokenManager.revokeToken(userName);
            }
        } catch (StorageException e) {
            throw new PasswordExpireException(
                    "Expiring password for \"" + userName + "\" has failed, " + e.getMessage(), e);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            log.debug(ERR_CAUSE, e);
            throw new PasswordExpireException("Expiring password has failed, " + e.getMessage());
        }
    }

    /**
     * Makes user password expired
     */
    @Override
    public void unexpirePassword(String userName) {
        try {
            if (unknownUsersCache.getIfPresent(userName) != null) {
                log.debug("The user: '{}' is registered in unknown users cache, no password to expire ...", userName);
                throw new UsernameNotFoundException(ERR_USER + userName + " does not exist");
            }
            findUser(userName); // todo: [mp] use plain user fetch (rather than heavy groups join)
            userGroupStoreService.revalidatePassword(userName);
        } catch (StorageException e) {
            throw new PasswordExpireException(
                    "Expiring password for \"" + userName + "\" has failed, " + e.getMessage(), e);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            log.debug("Cause: {}", e);
            throw new PasswordExpireException("Expiring password has failed, " + e.getMessage());
        }
    }

    /**
     * Makes all users passwords expired
     */
    @Override
    public void expireCredentialsForAllUsers() {
        try {
            userGroupStoreService.expirePasswordForAllUsers();
            //Invalidate all auth caches
            invalidateAuthCacheEntriesForAllUsers();
            dockerTokenManager.revokeAllTokens();
        } catch (StorageException e) {
            log.debug("Expiring all users credentials have failed, cause: {}", e);
            throw new PasswordExpireException("Expiring all users credentials have failed, see logs for more details");
        }
    }

    /**
     * Makes all users passwords not expired
     */
    @Override
    public void unexpirePasswordForAllUsers() {
        try {
            userGroupStoreService.revalidatePasswordForAllUsers();
        } catch (StorageException e) {
            log.debug("Un-expiring all users credentials have failed, cause: {}", e);
            throw new PasswordExpireException(
                    "Un-expiring all users credentials have failed, see logs for more details");
        }
    }


    /**
     * Fetches users with password is about to expire
     *
     * @return list of users
     */
    @Override
    public Set<PasswordExpiryUser> getUsersWhichPasswordIsAboutToExpire() {
        return userGroupStoreService.getUsersWhichPasswordIsAboutToExpire(
                ConstantValues.passwordDaysToNotifyBeforeExpiry.getInt(), getPasswordExpirationDays());
    }

    /**
     * Marks user.credentialsExpired=True where password has expired
     *
     * @param daysToKeepPassword after what period password should be changed
     */
    @Override
    public void markUsersCredentialsExpired(int daysToKeepPassword) {
        List<String> expiredUsers = userGroupStoreService.markUsersCredentialsExpired(daysToKeepPassword);
        expiredUsers.forEach(user -> {
            invalidateAuthCacheEntries(user);
            dockerTokenManager.revokeToken(user);
        });
    }


    /**
     * @return number of days left till password will expire
     * or negative value if password already expired
     * or NULL if password expiration feature is disabled
     */
    @Override
    public Integer getUserPasswordDaysLeft(String userName) {
        Integer daysLeft = null;
        if (isPasswordExpirationPolicyEnabled()) {
            UserInfo user = userGroupStoreService.findUser(userName);
            if (user != null && !user.isAnonymous() && !user.isPasswordDisabled()) {
                Long userPasswordCreationTime = userGroupStoreService.getUserPasswordCreationTime(userName);
                if (userPasswordCreationTime != null) {
                    daysLeft = getDaysLeftUntilPasswordExpires(userPasswordCreationTime);
                } else {
                    log.debug("Password creation time for user: '{}' returned no value", userName);
                }
            }
        }
        return daysLeft;
    }

    private Integer getDaysLeftUntilPasswordExpires(Long userPasswordCreationTime) {
        int daysLeft;
        DateTime created = new DateTime(userPasswordCreationTime.longValue());
        int expiresIn = getPasswordExpirationDays();
        DateTime now = DateTime.now();
        daysLeft = created.plusDays(expiresIn).minusDays(now.getDayOfYear()).getDayOfYear();
        if ((daysLeft == 365 || daysLeft == 366) && created.plusDays(expiresIn).dayOfYear().get() != daysLeft) {
            daysLeft = 0;
        }
        return daysLeft;
    }

    /**
     * Generates a root {@link RepoPath} in given Build info/Release bundle repository according to given build/release-bundle name.
     **/
    private static RepoPath getRootPathInRepo(String name, String repoKey) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Cannot generate path - name is missing");
        }
        return RepoPathFactory.create(repoKey, name + "/");
    }

    /**
     * If user has EntPlus/Edge license returns all release-bundles acls from cache, otherwise returns empty collection.
     */
    private Collection<ReleaseBundleAcl> getAllReleaseBundleAclsByLicense() {
        return hasEdgeOrEnterprisePlusLic() ? aclStoreService.getAllReleaseBundleAcls() :
                new HashMap<String, ReleaseBundleAcl>().values();
    }

    /**
     * All release-bundle section in the permission target should be visible/enabled only if user has EntPlus/Edge license.
     */
    private boolean hasEdgeOrEnterprisePlusLic() {
        return addons.isClusterEnterprisePlus() || (!addons.isEdgeMixedInCluster() && addons.isEdgeLicensed());
    }
}
