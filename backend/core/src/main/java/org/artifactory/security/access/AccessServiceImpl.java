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

package org.artifactory.security.access;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.security.AuthorizationException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.security.access.*;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.PasswordExpirationPolicy;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.accesstoken.AccessClientSettings;
import org.artifactory.event.AccessImportEvent;
import org.artifactory.event.CacheType;
import org.artifactory.event.InvalidateCacheEvent;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.security.*;
import org.artifactory.spring.ContextCreationListener;
import org.artifactory.spring.LicenseEventListener;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.security.service.access.GroupMapper;
import org.artifactory.storage.db.security.service.access.UserMapper;
import org.artifactory.storage.fs.lock.ThreadDumpUtils;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.access.client.*;
import org.jfrog.access.client.token.TokenRequest;
import org.jfrog.access.client.token.TokenResponse;
import org.jfrog.access.client.token.TokenVerifyResult;
import org.jfrog.access.client.token.TokensInfoResponse;
import org.jfrog.access.common.AccessAuthz;
import org.jfrog.access.common.Issuer;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.common.SubjectFQN;
import org.jfrog.access.rest.imports.ImportEntitiesRequest;
import org.jfrog.access.rest.user.LoginRequest;
import org.jfrog.access.router.RouterGrpcClient;
import org.jfrog.access.token.JwtAccessToken;
import org.jfrog.common.ClockUtils;
import org.jfrog.common.config.diff.DataDiff;
import org.jfrog.security.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.concat;
import static java.lang.Thread.sleep;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.artifactory.common.ArtifactoryHome.*;
import static org.artifactory.security.ArtifactoryResourceType.*;
import static org.artifactory.security.access.AccessServiceConstants.ARTIFACTORY_SERVICE_ANY_ID_PATTERN;
import static org.artifactory.security.access.AccessServiceConstants.ARTIFACTORY_SERVICE_TYPE;
import static org.artifactory.security.access.ArtifactoryAdminScopeToken.V1_SCOPE_SERVICE_ID_ARTIFACTORY_ADMIN_PATTERN;
import static org.artifactory.security.access.ArtifactoryRepoPathScopeToken.*;
import static org.artifactory.security.access.MemberOfGroupsScopeToken.SCOPE_MEMBER_OF_GROUPS_PATTERN;
import static org.artifactory.storage.db.security.service.access.AclMapper.toFullAccessPermission;
import static org.jfrog.access.common.SubjectFQN.USERS_NAME_PART;
import static org.jfrog.access.token.JwtAccessToken.SCOPE_DELIMITER;
import static org.jfrog.access.util.AccessCredsFileHelper.ADMIN_DEFAULT_USERNAME;
import static org.jfrog.config.wrappers.ConfigurationManagerAdapter.normalizedFilesystemPath;
import static org.jfrog.security.file.SecurityFolderHelper.PERMISSIONS_MODE_644;
import static org.jfrog.security.file.SecurityFolderHelper.setPermissionsOnSecurityFile;

/**
 * @author Yinon Avraham
 */
@Service
@Lazy
@Reloadable(beanClass = AccessService.class, initAfter = InternalCentralConfigService.class,
        listenOn = {CentralConfigKey.securityAccessClientSettings, CentralConfigKey.securityPasswordSettings})
public class AccessServiceImpl implements AccessService, ContextCreationListener, LicenseEventListener {

    private static final Logger log = LoggerFactory.getLogger(AccessServiceImpl.class);
    private static final int MIN_INSTANCE_ID_LENGTH = 20;

    //Accepted scopes:
    private static final String SCOPE_API = "api:*";
    private static final Pattern SCOPE_API_PATTERN = Pattern.compile(Pattern.quote(SCOPE_API));
    private static final String DEFAULT_ADMIN_PASSWORD = "password";
    private static final String JFROG_ROUTER_URL = "JFROG_ROUTER_URL";

    private final List<Pattern> acceptedScopePatternsByNonAdmin = Lists
            .newArrayList(SCOPE_API_PATTERN, SCOPE_MEMBER_OF_GROUPS_PATTERN, SCOPE_ARTIFACTORY_REPO_PATH_PATTERN);
    private final List<Pattern> acceptedScopePatternsByAdmin = Lists.newArrayList(
            V1_SCOPE_SERVICE_ID_ARTIFACTORY_ADMIN_PATTERN);
    private final List<Pattern> acceptedScopePatterns = Lists.newArrayList(
            concat(acceptedScopePatternsByNonAdmin, acceptedScopePatternsByAdmin));

    public static final String ACCESS_BOOTSTRAP_JSON = "access.bootstrap.json";
    private static final String ARTIFACTORY_SERVICE_ID = "artifactory.service_id";

    private final AddonsManager addonsManager;
    private UserGroupStoreService userGroupStore;
    private AuthorizationService authorizationService;
    private SecurityService securityService;
    private InternalCentralConfigService centralConfigService;
    private UserGroupService userGroupService;
    private RepositoryService repositoryService;
    private final ApplicationEventPublisher publisher;
    private final ConfigsService configsService;
    private ArtifactoryHome artifactoryHome;
    private ArtifactoryJoinKeyBootstrapper joinKeyBootstrapper;

    private ServiceId serviceId;
    private AccessClient accessClient;
    private String accessServerName;
    private ArtifactoryAccessClientConfigStore configStore;
    private final Object accessClientLock = new Object();

    private final ContextStateDependantActionRunner contextStateDependantActionRunner = new ContextStateDependantActionRunner();
    private long lastAdminSecurityIssueCheck;
    private Boolean hasAdminSecurityIssue;

    @Autowired
    public AccessServiceImpl(AddonsManager addonsManager, ApplicationEventPublisher publisher,
            ConfigsService configsService) {
        this.addonsManager = addonsManager;
        this.publisher = publisher;
        this.configsService = configsService;
    }

    @Autowired
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Autowired
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Autowired
    public void setCentralConfigService(InternalCentralConfigService centralConfigService) {
        this.centralConfigService = centralConfigService;
    }

    @Autowired
    public void setUserGroupService(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    @Autowired
    public void setUserGroupStore(UserGroupStoreService userGroupStore) {
        this.userGroupStore = userGroupStore;
    }

    @Autowired
    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    SecurityService securityService() {
        return securityService;
    }

    InternalCentralConfigService centralConfigService() {
        return centralConfigService;
    }

    ArtifactoryHome artifactoryHome() {
        return artifactoryHome;
    }

    @Override
    public void registerAcceptedScopePattern(@Nonnull Pattern pattern) {
        synchronized (acceptedScopePatterns) {
            log.debug("Registering accepted scope pattern: {}", requireNonNull(pattern, "pattern is required"));
            if (acceptedScopePatterns.stream().noneMatch(p -> p.pattern().equals(pattern.pattern()))) {
                acceptedScopePatterns.add(pattern);
            } else {
                log.debug("Pattern already exists in the accepted scope patterns: '{}'", pattern);
            }
        }
    }

    @Nonnull
    @Override
    public List<TokenInfo> getTokenInfos() {
        try {
            TokensInfoResponse tokensInfoResponse = ensureAuth(() -> getAccessClient().token().getTokensInfo());
            return tokensInfoResponse.getTokens().stream()
                    .map(this::toTokenInfo)
                    .filter(this::isNonInternalToken)
                    .collect(Collectors.toList());
        } catch (AccessClientException e) {
            confirmAdminAccessTokenValidOrElseClientBootstrap();
            throw new AccessClientException("Failed to get tokens information.", e);
        }
    }

    @Override
    public AccessClient getAccessClient() {
        synchronized (accessClientLock) {
            return accessClient;
        }
    }

    @Override
    public void encryptOrDecrypt(boolean encrypt) {
        configStore.encryptOrDecryptAccessCreds(encrypt);
    }

    @Override
    public <T> T ensureAuth(Callable<T> call) {
        try {
            try {
                return call.call();
            } catch (AccessClientHttpException e) {
                if (e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    log.warn("Access Token is invalid, trying to refresh access token");
                    bootstrapAccessClient();
                    return call.call();
                }
                throw e;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AccessClientException(e);
        }
    }

    private void runWithFallback(Runnable runnable) {
        ensureAuth(() -> {
            runnable.run();
            return null;
        });
    }

    private TokenInfo toTokenInfo(org.jfrog.access.client.token.TokenInfo clientTokenInfo) {
        return new TokenInfoImpl(clientTokenInfo.getTokenId(),
                clientTokenInfo.getIssuer(),
                clientTokenInfo.getSubject(),
                clientTokenInfo.getExpiry(),
                clientTokenInfo.getIssuedAt(),
                clientTokenInfo.isRefreshable());
    }

    private boolean isNonInternalToken(TokenInfo tokenInfo) {
        try {
            SubjectFQN subject = SubjectFQN.fromFullyQualifiedName(tokenInfo.getSubject());
            if (subject.getServiceId().equals(getArtifactoryServiceId())) {
                return UserTokenSpec.isUserToken(tokenInfo);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void init() {
        artifactoryHome = ArtifactoryHome.get();
        joinKeyBootstrapper = new ArtifactoryJoinKeyBootstrapper(artifactoryHome.getJoinKeyFile());
        String oldServiceId = initServiceId();
        initAccessClientIfNeeded(oldServiceId);
        updateAccessConfiguration(centralConfigService.getDescriptor().getSecurity());
        initAccessAdminDefaultPasswordCheck();
    }

    private void initAccessClientIfNeeded(String oldServiceId) {
        if (accessClient == null) {
            initAccessService(oldServiceId);
        }
    }

    @Override
    public void initAccessService(String oldServiceId) {
        synchronized (accessClientLock) {
            AccessClient oldAccessClient = getAccessClient();
            boolean shouldCloseOldClient = false;
            try {
                configStore = new ArtifactoryAccessClientConfigStore(this, serviceId);
                bootstrapAccessClient();
                shouldCloseOldClient = true;
                accessServerName = getAccessServerName();
                sendLicenseToAccess();
                registerInFederation(oldServiceId, false);
                log.info("Initialized access service successfully with client id {}, closing old client id {}",
                        getAccessClient().hashCode(), oldAccessClient != null ? oldAccessClient.hashCode() : "[null]");
            } finally {
                if (shouldCloseOldClient) {
                    IOUtils.closeQuietly(oldAccessClient);
                }
            }
        }
    }

    AccessClient buildAccessClient() {
        return configStore.newClientBuilder()
                .originUser(() -> authorizationService.currentUsername())
                .originUserIp(() -> AuthenticationHelper.getRemoteAddress(AuthenticationHelper.getAuthentication()))
                .create();
    }

    private void waitForAccessServer(@Nonnull AccessClient accessClient) {
        // When we wait we do not use auth we just want to check if the service is up
        try {
            accessClient = accessClient.useAuth(null);
            long secondsToWait = ConstantValues.accessClientWaitForServer.getLong(artifactoryHome);
            waitForAccessServer(TimeUnit.SECONDS.toMillis(secondsToWait), accessClient);
            if (ConstantValues.accessClientIgnoreServerVersionAssertion.getBoolean() ||
                    ConstantValues.dev.getBoolean()) {
                log.warn("Skipping access client/server versions match assertion. This config is not recommended.");
            } else {
                accessClient.assertClientServerVersionsMatch();
            }
        } catch (AccessClientException | IllegalStateException e) {
            if (log.isTraceEnabled()) {
                StringBuilder message = new StringBuilder("Access client might got stuck  ");
                ThreadDumpUtils.builder()
                        .count(3)
                        .build()
                        .dumpThreads(message);
                log.trace(message.toString());
            }
            throw e;
        }
    }

    private void waitForAccessServer(long timeoutMillis, AccessClient accessClient) {
        log.info("Waiting for access server...");
        long startTime = System.currentTimeMillis();
        AccessClient accessClientNoAuth = accessClient.useAuth(null);
        while (ClockUtils.epochMillis() - startTime < timeoutMillis) {
            try {
                log.debug("Pinging access server...");
                accessClientNoAuth.ping();
                log.info("Got response from Access server after {} ms, continuing.",
                        System.currentTimeMillis() - startTime);
                return;
            } catch (AccessClientException e) {
                log.error("Could not ping access server: {}", e);
                pause();
            }
        }
        throw new IllegalStateException("Waiting for access server to respond timed-out");
    }

    /**
     * Initializes the Artifactory Service ID. The service_id resides in the database, but can also be imported from
     * the filesystem to support transition from older versions to recent ones, and system import. The import from the
     * filesystem can also be made from the legacy $CLUSTER_HOME directory. If the service_id changes for some reason,
     * the Access client admin token is revoked.
     *
     * @return original ServiceId if existed
     */
    public String initServiceId() {
        boolean serviceIdChanged = false;
        String originalServiceId = configsService.getConfig(ARTIFACTORY_SERVICE_ID);
        // Try to import from filesystem if it exists
        tryToImportServiceId();
        String serviceId = configsService.getConfig(ARTIFACTORY_SERVICE_ID);
        if (StringUtils.isNotBlank(serviceId)) {
            serviceIdChanged = setServiceIdAndCheckIfChanged(originalServiceId, serviceId);
        } else {
            // Case of first Artifactory startup
            String instanceId = generateServiceInstanceID();
            this.serviceId = new ServiceId(ARTIFACTORY_SERVICE_TYPE, instanceId);
            try {
                configsService.addConfig(ARTIFACTORY_SERVICE_ID, this.serviceId.toString(), System.currentTimeMillis());
                log.debug("Successfully generated serviceId and uploaded to DB: " + this.serviceId);
            } catch (StorageException e) {
                // Failed over insert, other node insert serviceId already, get what is inside DB.
                log.debug("Couldn't upload newly generated serviceId to DB, retrying reading from DB for existing one");
                pause();
                serviceId = configsService.getConfig(ARTIFACTORY_SERVICE_ID);
                if (StringUtils.isNotBlank(serviceId)) {
                    this.serviceId = ServiceId.fromFormattedName(serviceId);
                    log.debug("Successfully loaded serviceId from DB: " + this.serviceId);
                } else {
                    // This means a true sql error, throw it.
                    throw new RuntimeException("Failed to create/load serviceId", e);
                }
            }
        }
        log.info("Initialized new service id: " + this.serviceId);
        return serviceIdChanged ? originalServiceId : null;
    }

    protected boolean setServiceIdAndCheckIfChanged(String originalServiceId, String serviceId) {
        this.serviceId = ServiceId.fromFormattedName(serviceId);
        log.debug("Successfully loaded serviceId from config: " + this.serviceId);
        if (StringUtils.isNotBlank(originalServiceId) && !originalServiceId.equals(serviceId)) {
            log.info("The service_id has changed. This will entail re-generation of the Access admin token " +
                    "and re-registration in Access Federation");
            return true;
        }
        return false;
    }

    private void tryToImportServiceId() {
        // Trying to load service id from file system (could be either system import or old Artifactory version)
        File serviceIdFile = getAccessServiceIdFile();
        // Support legacy cluster file and corrupted states where cluster.id file exist but service_id file not
        File clusterIdFile = getClusterIdFile();
        HaCommonAddon haCommonAddon = addonsManager.addonByType(HaCommonAddon.class);
        // In case HA&&Primary or Not-HA we read the file and update DB
        if (!haCommonAddon.isHaEnabled() || haCommonAddon.isPrimary()) {
            try {
                File candidate = chooseBestCandidate(serviceIdFile, clusterIdFile);
                // If candidate file exists, import it and delete it. Otherwise, do nothing.
                if (candidate.exists()) {
                    log.debug("Instance is HA-Primary or Pro, handling serviceId local file");
                    String formattedServiceId = Files.readAllLines(candidate.toPath()).get(0);
                    String dbServiceId = configsService.getConfig(ARTIFACTORY_SERVICE_ID);
                    // Update db
                    if (StringUtils.isNotBlank(dbServiceId)) {
                        // Case there is an old serviceId in DB
                        overwriteServiceId(formattedServiceId, dbServiceId);
                    } else {
                        // Case there is no serviceId in DB
                        configsService.addConfigInNewTransaction(ARTIFACTORY_SERVICE_ID, formattedServiceId,
                                System.currentTimeMillis());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize the service ID.", e);
            }
        } else {
            log.debug("Instance is HA non-primary, no need to handle serviceId local file");
        }
        // Rename the file to avoid future imports (even on non-primary nodes)
        renameOldFileName(serviceIdFile);
        renameOldFileName(clusterIdFile);
    }

    private File chooseBestCandidate(File serviceIdFile, File clusterIdFile) {
        HaCommonAddon haCommonAddon = addonsManager.addonByType(HaCommonAddon.class);
        if (!serviceIdFile.exists() && haCommonAddon.isHaConfigured()) {
            return clusterIdFile;
        }
        return serviceIdFile;
    }

    private void overwriteServiceId(String formattedServiceId, String dbServiceId) {
        configsService
                .updateConfigInNewTransaction(ARTIFACTORY_SERVICE_ID, formattedServiceId, System.currentTimeMillis());
        log.warn("ServiceId overwrite: detected {} file. In case of HA: if you want to set a new " +
                        "ServiceId you need to restart each node. Old value: {}, new value: {}",
                ACCESS_SERVICE_ID, dbServiceId, formattedServiceId);
    }

    private File getAccessServiceIdFile() {
        return new File(artifactoryHome.getAccessClientDir(), getServiceIdFilePath());
    }

    public static String getServiceIdFilePathUnderEtc() {
        return normalizedFilesystemPath(SECURITY_DIR_NAME, ACCESS_CLIENT_DIR_NAME, getServiceIdFilePath());
    }

    private static String getServiceIdFilePath() {
        return normalizedFilesystemPath(ACCESS_KEYS_DIR_NAME, ACCESS_SERVICE_ID);
    }

    private File getClusterIdFile() {
        return new File(artifactoryHome.getEtcDir(), CLUSTER_ID);
    }

    private void renameOldFileName(File file) {
        // If file doesn't exist no need to move it.
        if (!file.exists()) {
            return;
        }
        long date = System.currentTimeMillis();
        File targetFile = new File(file.getAbsolutePath() + "." + date + ".bak");
        if (!file.renameTo(targetFile)) {
            log.warn("Failed to delete old file from file system: {}", file.getAbsoluteFile());
        }
    }

    private ServiceId createServiceId(String serviceId) {
        return ServiceId.fromFormattedName(serviceId);
    }

    private String generateServiceInstanceID() {
        String id = ULID.random().toLowerCase();
        return normalizeInstanceId(id);
    }

    static String normalizeInstanceId(String id) {
        Matcher matcher = ServiceId.ELEMENT_PATTERN.matcher(id);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            //Fill the gap (if any)
            for (int i = builder.length(); i < matcher.start(); i++) {
                builder.append("_");
            }
            builder.append(matcher.group());
        }
        String normalizedId = builder.toString();
        if (normalizedId.length() < MIN_INSTANCE_ID_LENGTH) {
            throw new IllegalArgumentException("Instance ID is too short. (normalized='" + normalizedId +
                    "', original='" + id + "')");
        }
        return normalizedId;
    }

    @Nonnull
    @Override
    public TokenResponse createTokenWithAccessAdminCredentials(@Nonnull String serviceIdAsString) {
        ServiceId tokenSubjectServiceId;
        try {
            tokenSubjectServiceId = createServiceId(serviceIdAsString);
        } catch (RuntimeException oops) {
            log.error("Failed to parse '{}' as valid service id for token request: {}", serviceIdAsString,
                    oops.getMessage());
            log.debug("", oops);
            throw oops;
        }
        try {
            List<String> scopes = Collections.singletonList(AccessAuthz.ADMIN);
            String accessServiceId = ensureAuth(
                    () -> getAccessClient().system().getAccessServiceId().getFormattedName());
            TokenRequest tokenRequest = new TokenRequest(scopes, false, tokenSubjectServiceId.toString(), null,
                    AccessClientBootstrap.SERVICE_ADMIN_TOKEN_EXPIRY, null, Lists.newArrayList(accessServiceId));
            assertAdminUserCanCreateToken(tokenRequest);
            tokenRequest = replacePathTokenScopeWithChecksum(tokenRequest);
            return getAccessClient().token().create(tokenRequest);
        } catch (AccessClientException e) {
            log.error("Failed to create token for subject '{}': {}", tokenSubjectServiceId, e.getMessage());
            log.debug("Failed to create token for subject '{}'", tokenSubjectServiceId.toString(), e);
            throw new RuntimeException("Failed to create token for subject '" + tokenSubjectServiceId.toString() + "'.",
                    e);
        }
    }

    @Nonnull
    @Override
    public CreatedTokenInfo createToken(@Nonnull TokenSpec tokenSpec) {
        return createToken(tokenSpec, false);
    }

    @Override
    @Nonnull
    public CreatedTokenInfo createToken(@Nonnull TokenSpec tokenSpec,
            boolean skipUserCanCreateLongLivedTokenAssertion) {
        return ensureAuth(() -> {
            String subject = null;
            try {
                subject = tokenSpec.createSubject(serviceId).toString();
                List<String> scope = getEffectiveScope(tokenSpec);
                List<String> audience = getEffectiveAudience(tokenSpec);
                TokenRequest tokenRequest = new TokenRequest(scope, tokenSpec.isRefreshable(), subject, getIssuer(),
                        tokenSpec.getExpiresIn(), null, audience);
                assertLoggedInCanCreateToken(tokenRequest, skipUserCanCreateLongLivedTokenAssertion);
                tokenRequest = replacePathTokenScopeWithChecksum(tokenRequest);
                TokenResponse tokenResponse = getAccessClient().token().create(tokenRequest);
                return toCreatedTokenInfo(tokenResponse, scope);
            } catch (AccessClientException e) {
                log.error("Failed to create token for subject '{}': {}", subject, e.getMessage());
                log.debug("Failed to create token for subject '{}'", subject, e);
                confirmAdminAccessTokenValidOrElseClientBootstrap();
                throw new AccessClientException("Failed to create token for subject '" + subject + "'.", e);
            }
        });
    }

    @Nonnull
    @Override
    public CreatedTokenInfo createNoPermissionToken(@Nonnull List<String> scope,
            @Nonnull TokenSpec tokenSpec, @Nullable String extraData) {
        return ensureAuth(() -> {
            String subject = null;
            try {
                subject = tokenSpec.createSubject(serviceId).toString();
                List<String> audience = getEffectiveAudience(tokenSpec);
                TokenRequest tokenRequest = new TokenRequest(scope, tokenSpec.isRefreshable(), subject, null,
                        tokenSpec.getExpiresIn(), extraData, audience);
                tokenRequest = replacePathTokenScopeWithChecksum(tokenRequest);
                TokenResponse tokenResponse = getAccessClient().token().create(tokenRequest);
                return toCreatedTokenInfo(tokenResponse, scope);
            } catch (AccessClientException e) {
                log.error("Failed to create token for subject '{}': {}", subject, e.getMessage());
                log.debug("Failed to create token for subject '{}'", subject, e);
                confirmAdminAccessTokenValidOrElseClientBootstrap();
                throw new AccessClientException("Failed to create token for subject '" + subject + "'.", e);
            }
        });
    }

    /**
     * Replace all ArtifactoryRepoPathScopeToken tokens with a checksum repoPath.
     */
    private TokenRequest replacePathTokenScopeWithChecksum(TokenRequest tokenRequest) {
        List<String> newScope = Lists.newArrayList();
        List<String> scope = tokenRequest.getScope();
        for (String scopeToken : scope) {
            if (ArtifactoryRepoPathScopeToken.accepts(scopeToken)) {
                ArtifactoryRepoPathScopeToken parse = ArtifactoryRepoPathScopeToken.parse(scopeToken);
                if (!parse.isChecksumPath()) {
                    newScope.add(
                            PATH_CHECKSUM_PREFIX + ":" + parse.getRepoPathChecksum() + ":" + parse.getPermissions());
                } else { // already checksum
                    newScope.add(scopeToken);
                }
            } else {
                newScope.add(scopeToken);
            }
        }
        return new TokenRequest(newScope, tokenRequest.isRefreshable(), tokenRequest.getSubject(), tokenRequest.getIssuer(),
                tokenRequest.getExpiresIn(),
                tokenRequest.getExtension(), tokenRequest.getAudience());
    }

    private void assertLoggedInCanCreateToken(TokenRequest tokenRequest,
            boolean skipUserCanCreateLongLivedTokenAssertion) {
        String currentUsername = authorizationService.currentUsername();
        if (authorizationService.isAdmin() || SecurityService.USER_SYSTEM.equals(currentUsername)) {
            assertAdminUserCanCreateToken(tokenRequest);
        } else {
            assertNonAdminUserCanCreateToken(tokenRequest, currentUsername, skipUserCanCreateLongLivedTokenAssertion);
        }
        assertAllAudienceAreArtifactoryInstances(tokenRequest);
    }

    private void assertAllAudienceAreArtifactoryInstances(TokenRequest tokenRequest) {
        Optional<String> illegalAudience = tokenRequest.getAudience().stream()
                .filter(aud -> !ARTIFACTORY_SERVICE_ANY_ID_PATTERN.matcher(aud).matches())
                .findFirst();
        if (illegalAudience.isPresent()) {
            throw new AuthorizationException("Illegal audience: " + illegalAudience.get() + ", audience can contain " +
                    "only service IDs of Artifactory servers.");
        }
    }

    private void assertNonAdminUserCanCreateToken(TokenRequest tokenRequest, String currentUsername,
            boolean skipUserCanCreateLongLivedTokenAssertion) {
        //non-admin users can only create tokens for themselves under this artifactory service ID
        if (!UserTokenSpec.isUserTokenSubject(tokenRequest.getSubject())) {
            throw new AuthorizationException(
                    String.format("The user: '%s' can only create user token(s) for themselves (requested: %s)",
                            currentUsername, tokenRequest.getSubject()));
        }
        String subjectUsername = UserTokenSpec.extractUsername(tokenRequest.getSubject());
        if (!currentUsername.equals(subjectUsername)) {
            throw new AuthorizationException(
                    String.format("The user: '%s' can only create user token(s) for themselves (requested: %s)",
                            currentUsername, subjectUsername));
        }
        ServiceId subjectServiceId = SubjectFQN.fromFullyQualifiedName(tokenRequest.getSubject()).getServiceId();
        if (!serviceId.equals(subjectServiceId)) {
            throw new AuthorizationException(String.format(
                    "The user: '%s' can only create user token(s) for themselves under this Artifactory service ID (requested: %s)",
                    currentUsername, tokenRequest.getSubject()));
        }
        assertValidScopeForNonAdmin(tokenRequest.getScope());
        //non-admin users can have limited expires in
        assertValidExpiresInForNonAdmin(tokenRequest, currentUsername, skipUserCanCreateLongLivedTokenAssertion);
    }

    private void assertAdminUserCanCreateToken(TokenRequest tokenRequest) {
        tokenRequest.getScope().forEach(scopeToken -> {
            if (ArtifactoryAdminScopeToken.accepts(scopeToken) &&
                    !ArtifactoryAdminScopeToken.isAdminScopeOnService(scopeToken, getArtifactoryServiceId())) {
                throw new AuthorizationException("Admin can create token with admin privileges only on this " +
                        "Artifactory instance: " + getArtifactoryServiceId() +
                        " (requested: " + serviceId + ")");
            }
            if (ArtifactoryRepoPathScopeToken.accepts(scopeToken)) {
                assertPathInTokenExists(ArtifactoryRepoPathScopeToken.getPath(scopeToken));
            }
        });
    }

    private void assertValidScopeForNonAdmin(@Nonnull JwtAccessToken accessToken) {
        Issuer issuer = new Issuer(accessToken.getIssuer());
        String issuedBy = issuer.getIssuedBy();
        if (issuedBy == null) {
            log.debug("Token owner wasn't found - allow refreshing token {}", accessToken.getTokenId());
            return;
        }
        UserInfo user;
        try {
            user = userGroupService.findUser(issuedBy);
        } catch (UsernameNotFoundException e) {
            log.debug("Cannot refresh token " + accessToken.getTokenId(), e);
            throw new AuthorizationException("Cannot refresh token, token owner does not exist");
        }
        if (!user.isEffectiveAdmin() && !SecurityService.USER_SYSTEM.equals(issuedBy)) {
            if (!issuedBy.equals(SubjectFQN.extractUsername(accessToken.getSubject()))) {
                throw new AuthorizationException(
                        String.format("Cannot refresh token, user: '%s' is not compatible with token subject",
                                issuedBy));
            }
            assertValidGroupsForNonAdmin(accessToken.getScope(), user);
        }
    }

    private void assertValidScopeForNonAdmin(List<String> scopes) {
        // check if any of the given scopes isn't supported by non admin user
        Optional<String> unsupportedScopeToken = scopes.stream()
                .filter(this::scopeUnsupportedForNonAdmin)
                .findFirst();
        if (unsupportedScopeToken.isPresent()) {
            throw new AuthorizationException("Logged in user cannot request token with scope: " +
                    unsupportedScopeToken.get());
        }
        assertValidGroupsForNonAdmin(scopes, userGroupService.currentUser());
        assertValidRepoPathsForNonAdmin(scopes);
    }

    private boolean scopeUnsupportedForNonAdmin(String scopeToken) {
        return acceptedScopePatternsByNonAdmin.stream()
                .noneMatch(pattern -> pattern.matcher(scopeToken).matches());
    }

    /**
     * Assert non admin user has permissions on given path, and that those paths exists
     */
    private void assertValidRepoPathsForNonAdmin(List<String> scope) {
        List<String> repoPathsFromScope = collectRepoPathsFromScope(scope);
        repoPathsFromScope.forEach(this::assertPathInTokenExists);
        boolean hasPermissionsOnAllRepos = scope.stream()
                .filter(ArtifactoryRepoPathScopeToken::accepts)
                .allMatch(this::hasPermissionsOnRepoPath);
        if (!hasPermissionsOnAllRepos) {
            throw new AuthorizationException("Logged in user cannot create repo path token with scope: " +
                    "no permissions on paths");
        }
    }

    private void assertPathInTokenExists(String repoPathName) {
        RepoPath repoPath = RepoPathFactory.create(repoPathName);
        ItemInfo itemInfo;
        try {
            itemInfo = repositoryService.getItemInfo(repoPath);
        } catch (ItemNotFoundRuntimeException nfe) {
            log.debug("", nfe);
            throw new NotFoundException(
                    "Cannot create a token with provided scope - path doesn't exist : " + repoPath.toString());
        }
        if (itemInfo.isFolder()) {
            throw new NotFoundException(
                    "Cannot create a token with provided scope - path is a directory : " + repoPath.toString());
        }
    }

    private boolean hasPermissionsOnRepoPath(String scopeToken) {
        RepoPath repoPath = RepoPathFactory.create(ArtifactoryRepoPathScopeToken.getPath(scopeToken));
        return READ_PERMISSION.equals(ArtifactoryRepoPathScopeToken.parse(scopeToken).getPermissions()) &&
                authorizationService.canRead(repoPath);
    }

    /**
     * for non-admin users - Check user is a member of requested groups
     */
    private void assertValidGroupsForNonAdmin(List<String> scope, UserInfo userInfo) {
        Set<String> requestedGroupNames = collectGroupNamesFromScope(scope);
        // In case the token scope contains member-of-groups:* there is no need to verify groups
        if (requestedGroupNames.size() == 1 && requestedGroupNames.contains("*")) {
            return;
        }
        Set<String> userGroups = getUserGroupsNames(userInfo);
        requestedGroupNames.removeAll(userGroups);
        if (!requestedGroupNames.isEmpty()) {
            throw new AuthorizationException(
                    String.format("The user: '%s' is not a member of the following groups: %s", userInfo.getUsername(),
                            String.join(",", requestedGroupNames)));
        }
    }

    private Set<String> getUserGroupsNames(UserInfo user) {
        Set<UserGroupInfo> userGroups = user.getGroups();
        return Optional.ofNullable(userGroups)
                .map(groups -> groups.stream()
                        .map(UserGroupInfo::getGroupName)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    void assertValidExpiresInForNonAdmin(TokenRequest tokenRequest, String currentUsername,
            boolean skipUserCanCreateLongLivedTokenAssertion) {
        SecurityDescriptor securityDescriptor = centralConfigService.getDescriptor().getSecurity();
        AccessClientSettings accessClientSettings = securityDescriptor.getAccessClientSettings();
        long maxExpiresInSeconds = Optional.ofNullable(accessClientSettings.getUserTokenMaxExpiresInMinutes())
                .map(TimeUnit.MINUTES::toSeconds)
                .orElse(ConstantValues.accessTokenNonAdminMaxExpiresIn.getLong(artifactoryHome));
        if (maxExpiresInSeconds != AccessClientSettings.USER_TOKEN_MAX_EXPIRES_UNLIMITED) {
            long expiresIn = Optional.ofNullable(tokenRequest.getExpiresIn()).orElse(Long.MAX_VALUE);
            if (!skipUserCanCreateLongLivedTokenAssertion && (expiresIn > maxExpiresInSeconds || expiresIn <= 0)) {
                throw new AuthorizationException(String.format(
                        "The user: '%s' can only create user token with expires in larger than 0 and smaller than %d seconds (requested: %d)",
                        currentUsername, maxExpiresInSeconds, tokenRequest.getExpiresIn()));
            }
        }
    }

    @Nonnull
    @Override
    public CreatedTokenInfo refreshToken(@Nonnull TokenSpec tokenSpec, @Nonnull String tokenValue,
            @Nonnull String refreshToken) {
        JwtAccessToken accessToken = parseToken(tokenValue);
        assertTokenCreatedByThisService(accessToken);
        assertValidScopeForNonAdmin(accessToken);
        try {
            try {
                List<String> scope = isNotEmpty(tokenSpec.getScope()) ? tokenSpec.getScope() : accessToken.getScope();
                //false if specified, true otherwise (a refreshable token keeps to be refreshable, unless specified otherwise)
                boolean refreshable = Boolean.FALSE.equals(tokenSpec.getRefreshable());
                List<String> audience =
                        isNotEmpty(tokenSpec.getAudience()) ? tokenSpec.getAudience() : accessToken.getAudience();
                TokenResponse tokenResponse = ensureAuth(() -> {
                    TokenRequest tokenRequest = new TokenRequest(scope, refreshable, accessToken.getSubject(),
                            null, tokenSpec.getExpiresIn(), null, audience);
                    return getAccessClient().token().refresh(refreshToken, tokenRequest);
                });
                return toCreatedTokenInfo(tokenResponse, scope);
            } catch (AccessClientHttpException e) {
                if (e.getStatusCode() == 404) {
                    throw new TokenNotFoundException("Invalid access token or refresh token", e);
                } else if (e.getStatusCode() == 403) {
                    throw new AuthorizationException("Refresh token operation rejected", e);
                }
                throw new AccessClientException("Access server refused to refresh the token", e);
            } catch (AccessClientException e) {
                log.error("Failed to refresh token for subject '{}': {}", accessToken.getSubject(), e.getMessage());
                log.debug("Failed to refresh token with id '{}' for subject '{}'",
                        accessToken.getTokenId(), accessToken.getSubject(), e);
                throw new AccessClientException(
                        "Failed to refresh token for subject '" + accessToken.getSubject() + "'.", e);
            }
        } catch (AccessClientException e) {
            confirmAdminAccessTokenValidOrElseClientBootstrap();
            throw e;
        }
    }

    /**
     * Asserts that the given token was created by this service (artifactory instance/cluster). This method accepts both
     * access tokens and refresh tokens, to support revoke by both. In case the token is not a valid access token (e.g.
     * a refresh token) the token is not checked.
     *
     * @param tokenValue the token to check, can be either an access token or a refresh token
     */
    private void assertTokenCreatedByThisService(@Nonnull String tokenValue) {
        try {
            JwtAccessToken accessToken = parseToken(tokenValue);
            assertTokenCreatedByThisService(accessToken);
        } catch (IllegalArgumentException e) {
            log.debug("Could not parse token value, it might be a refresh token, ignoring.", e);
        }
    }

    /**
     * Asserts that the given token was created by this service (artifactory instance/cluster).
     * This is currently important for refreshing and revoking tokens because it can only be done by the same service.
     *
     * @param accessToken the access token to check
     */
    private void assertTokenCreatedByThisService(@Nonnull JwtAccessToken accessToken) {
        Issuer issuer = new Issuer(accessToken.getIssuer());
        ServiceId issuerServiceId = issuer.getServiceId();
        if (!issuerServiceId.equals(this.serviceId)) {
            throw new TokenIssuedByOtherServiceException("Provided access token with ID '" + accessToken.getTokenId() +
                    "' was issued by a different service with ID '" + issuerServiceId + "' (current service ID: '" +
                    this.serviceId + "')", this.serviceId, issuerServiceId);
        }
    }

    private CreatedTokenInfo toCreatedTokenInfo(TokenResponse tokenResponse, List<String> scopeTokens) {
        String scope = String.join(SCOPE_DELIMITER, scopeTokens);
        OptionalLong expiresInOptional = tokenResponse.getExpiresIn();
        Long expiresIn = expiresInOptional.isPresent() ? expiresInOptional.getAsLong() : null;
        return new CreatedTokenInfoImpl(tokenResponse.getTokenValue(), tokenResponse.getTokenType(),
                tokenResponse.getRefreshToken().orElse(null), scope, expiresIn);
    }

    private List<String> getEffectiveScope(TokenSpec tokenSpec) {
        List<String> effectiveScope = Lists.newArrayList(tokenSpec.getScope());
        addUserGroupsToScopeIfNeeded(effectiveScope, tokenSpec);
        addApiToScopeIfNeeded(effectiveScope);
        assertAcceptedScope(effectiveScope);
        return effectiveScope;
    }

    private void assertAcceptedScope(List<String> scope) {
        scope.stream()
                .filter(scopeToken ->
                        !acceptedScopePatterns.stream().anyMatch(pattern -> pattern.matcher(scopeToken).matches()))
                .findFirst()
                .ifPresent(scopeToken -> {
                    throw new IllegalArgumentException("Unaccepted scope: '" + scopeToken + "'");
                });
        if (scope.isEmpty() || scope.equals(singletonList(SCOPE_API))) {
            throw new IllegalArgumentException("Insufficient scope: '" + String.join(SCOPE_DELIMITER, scope) + "'");
        }
    }

    private void addApiToScopeIfNeeded(List<String> scope) {
        if (!scope.contains(SCOPE_API)) {
            scope.add(SCOPE_API);
        }
    }

    private void addUserGroupsToScopeIfNeeded(List<String> scope, TokenSpec tokenSpec) {
        if (tokenSpec instanceof UserTokenSpec) {
            UserInfo user = userGroupStore.findUser(((UserTokenSpec) tokenSpec).getUsername());
            if (user != null) {
                //Add user's assigned groups by default
                if (scope.isEmpty() || scope.equals(singletonList(SCOPE_API))) {
                    Set<UserGroupInfo> groups = user.getGroups();
                    if (groups != null && !groups.isEmpty()) {
                        List<String> groupNames = groups.stream()
                                .map(UserGroupInfo::getGroupName)
                                .collect(Collectors.toList());
                        String groupsConcat = String.join(",", groupNames);
                        if (groupsConcat.contains(" ")) {
                            groupsConcat = "\"" + groupsConcat + "\"";
                        }
                        scope.add("member-of-groups:" + groupsConcat);
                    }
                }
            }
        }
    }

    private Set<String> collectGroupNamesFromScope(List<String> scope) {
        return scope.stream()
                .filter(MemberOfGroupsScopeToken::accepts)
                .flatMap(scopeToken -> MemberOfGroupsScopeToken.parse(scopeToken).getGroupNames().stream())
                .collect(Collectors.toSet());
    }

    /**
     * collect all ArtifactoryRepoPathScopeToken repoPaths from scope
     */
    private List<String> collectRepoPathsFromScope(List<String> scope) {
        return scope.stream()
                .filter(ArtifactoryRepoPathScopeToken::accepts)
                .map(ArtifactoryRepoPathScopeToken::getPath)
                .collect(Collectors.toList());
    }

    private List<String> getEffectiveAudience(TokenSpec tokenSpec) {
        List<String> effectiveAudience = Lists.newArrayList(tokenSpec.getAudience());
        // If audience was not specified - use this service by default, otherwise use the specified audience,
        // even if it does not contain this service.
        if (effectiveAudience.isEmpty()) {
            String thisServiceIdName = serviceId.getFormattedName();
            effectiveAudience.add(thisServiceIdName);
        } else {
            // Replace "any-type" with the artifactory type (creating tokens through artifactory allows targeting only artifactory.)
            effectiveAudience = effectiveAudience.stream()
                    .map(aud -> {
                        if ("*".equals(aud) || "*@*".equals(aud)) {
                            return ARTIFACTORY_SERVICE_TYPE + "@*";
                        }
                        return aud;
                    }).collect(Collectors.toList());
        }
        return effectiveAudience;
    }

    private String getIssuer() {
        return serviceId.getFormattedName() + "/" + USERS_NAME_PART + "/" + authorizationService.currentUsername();
    }

    @Nullable
    @Override
    public String extractSubjectUsername(@Nonnull JwtAccessToken accessToken) {
        try {
            return UserTokenSpec.extractUsername(accessToken.getSubject());
        } catch (Exception e) {
            log.debug("Failed to extract subject username from access token: {}", accessToken, e);
            return null;
        }
    }

    @Override
    @Nonnull
    public Collection<String> extractAppliedGroupNames(@Nonnull JwtAccessToken accessToken) {
        return collectGroupNamesFromScope(accessToken.getScope());
    }

    @Override
    public void revokeToken(@Nonnull String tokenValue) {
        assertTokenCreatedByThisService(tokenValue);
        try {
            try {
                runWithFallback(() -> getAccessClient().token().revoke(tokenValue));
            } catch (AccessClientHttpException e) {
                if (e.getStatusCode() == 404) {
                    throw new TokenNotFoundException("Invalid access token or refresh token", e);
                } else if (e.getStatusCode() == 403) {
                    throw new AuthorizationException("Revoke token operation rejected", e);
                } else {
                    throw e;
                }
            } catch (AccessClientException e) {
                String tokenId = getTokenIdFromTokenValueSafely(tokenValue, "UNKNOWN");
                log.error("Failed to revoke token with id '{}': {}", tokenId, e.getMessage());
                log.debug("Failed to revoke token with id '{}'", tokenId, e);
                throw new AccessClientException("Failed to revoke token.", e);
            }
        } catch (AccessClientException e) {
            confirmAdminAccessTokenValidOrElseClientBootstrap();
            throw e;
        }
    }

    @Override
    public void revokeTokenById(@Nonnull String tokenId) {
        try {
            try {
                boolean found = ensureAuth(() -> getAccessClient().token().revokeById(tokenId));
                if (!found) {
                    throw new TokenNotFoundException("Token not found with id: " + tokenId);
                }
            } catch (AccessClientHttpException e) {
                if (e.getStatusCode() == 404) {
                    throw new TokenNotFoundException("Token not found with id: " + tokenId, e);
                } else if (e.getStatusCode() == 403) {
                    throw new AuthorizationException("Revoke token operation rejected", e);
                } else {
                    throw e;
                }
            } catch (AccessClientException e) {
                log.error("Failed to revoke token by id '{}': {}", tokenId, e.getMessage());
                log.debug("Failed to revoke token by id '{}'", tokenId, e);
                throw new AccessClientException("Failed to revoke token by id '" + tokenId + "'", e);
            }
        } catch (AccessClientException e) {
            confirmAdminAccessTokenValidOrElseClientBootstrap();
            throw e;
        }
    }

    @Nullable
    private String getTokenIdFromTokenValueSafely(@Nonnull String tokenValue, @Nullable String defaultValue) {
        try {
            return parseToken(tokenValue).getTokenId();
        } catch (IllegalArgumentException e) {
            log.debug("Failed to parse token value, returning default value '{}' instead of the token ID.",
                    defaultValue, e);
            return defaultValue;
        }
    }

    @Override
    @Nonnull
    public JwtAccessToken parseToken(@Nonnull String tokenValue) throws IllegalArgumentException {
        requireNonNull(tokenValue, "Token value is required");
        return ensureAuth(() -> getAccessClient().token().parse(tokenValue));
    }

    @Override
    public boolean verifyToken(@Nonnull JwtAccessToken accessToken) {
        TokenVerifyResult result = verifyAndGetResult(accessToken);
        if (result.isSuccessful()) {
            return true;
        } else {
            log.debug("Token with id '{}' failed verification, reason: {}", accessToken.getTokenId(),
                    result.getReason());
        }
        return false;
    }

    @Override
    public boolean verifyTokenIfServiceIdChanged(@Nonnull JwtAccessToken accessToken) {
        boolean serviceIdChanged = false;
        synchronized (accessClientLock) {
            String originalServiceId = serviceId.getFormattedName();
            String currentId = configsService.getConfig(ARTIFACTORY_SERVICE_ID);
            if (StringUtils.isNotBlank(currentId)) {
                log.debug("Checking if service_id changed");
                serviceIdChanged = setServiceIdAndCheckIfChanged(originalServiceId, currentId);
            }
            if (serviceIdChanged) {
                configStore = new ArtifactoryAccessClientConfigStore(this, serviceId);
                bootstrapAccessClient();
            }
            boolean tokenValid = verifyAndGetResult(accessToken).isSuccessful();
            if (tokenValid) {
                log.debug("Token reloaded successfully and verified.");
            }
        }
        return false;
    }

    @Override
    public TokenVerifyResult verifyAndGetResult(@Nonnull JwtAccessToken accessToken) {
        requireNonNull(accessToken, "Access token is required");
        try {
            return ensureAuth(() -> getAccessClient().token().verify(accessToken.getTokenValue()));
        } catch (AccessClientException e) {
            String tokenId = accessToken.getTokenId();
            log.error("Failed to verify access token with id '{}': {}", tokenId, e.getMessage());
            log.debug("Failed to verify access token with id '{}'", tokenId, e);
            confirmAdminAccessTokenValidOrElseClientBootstrap();
            throw new AccessClientException("Failed to verify access token with id '" + tokenId + "'", e);
        }
    }

    @Nonnull
    @Override
    public ServiceId getArtifactoryServiceId() {
        return serviceId;
    }

    @Override
    public boolean isTokenAppliesScope(@Nonnull JwtAccessToken accessToken, @Nonnull String requiredScope) {
        //TODO [YA] this is currently enough, but will probably need to be more sophisticated in the near future...
        return accessToken.getScope().stream().anyMatch(scope -> scope.equals(requiredScope));
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        log.info("Reloading Access Service");
        updateAccessServerConfigurationIfNeeded(oldDescriptor);
    }

    @Override
    public void unregisterFromRouterIfNeeded() {
        String jfrogRouterUrl = System.getProperty(JFROG_ROUTER_URL);
        if (StringUtils.isNotBlank(jfrogRouterUrl)) {
            ArtifactoryRouterRegistrationData artifactoryRouterRegistrationData = new ArtifactoryRouterRegistrationData(
                    getArtifactoryServiceId(),
                    ContextHelper.get().getArtifactoryHome().getHaAwareHostId());
            RouterGrpcClient router = getAccessClient().router();
            router.unregisterService(artifactoryRouterRegistrationData.build());
        }
    }

    private void confirmAdminAccessTokenValidOrElseClientBootstrap() {
        try {
            if (configStore.isAdminTokenExists()) {
                String adminToken = configStore.getAdminToken();
                boolean verifyAdminToken = ensureAuth(
                        () -> getAccessClient().token().verifyAdminToken(adminToken));
                if (verifyAdminToken) {
                    // due to bug (expired are not verified in db) we can't trust the verify method. JA-209
                    pingUsingAdminToken(adminToken);
                    return;
                } else {
                    log.info("AccessAdminToken does not exist/expired - bootstrapping fresh client.");
                }
            } else {
                log.info("AccessAdminToken missing - bootstrapping fresh client.");
            }
        } catch (AccessClientException ex) {
            log.info("AccessAdminToken rejected - bootstrapping fresh client.", ex);
        } catch (Exception e) {
            log.error("Can't check access admin token.", e);
        }
        try {
            bootstrapAccessClient();
        } catch (Exception e) {
            log.error("Failed bootstrapAccessClient.", e);
        }
    }

    public void pingUsingAdminToken(String adminToken) {
        runWithFallback(() -> {
            AccessClient accessHttpClient = getAccessClient().useAuth(new AccessAuthToken(adminToken));
            accessHttpClient.ping();
        });
    }

    protected void updateAccessClient(AccessClient newAccessClient) {
        synchronized (accessClientLock) {
            this.accessClient = newAccessClient;
        }
    }

    private void updateAccessServerConfigurationIfNeeded(CentralConfigDescriptor oldDescriptor) {
        PasswordExpirationPolicy oldExpirationPolicy = oldDescriptor.getSecurity().getPasswordSettings()
                .getExpirationPolicy();
        PasswordExpirationPolicy expirationPolicy = centralConfigService.getDescriptor().getSecurity()
                .getPasswordSettings()
                .getExpirationPolicy();

        if (expirationPolicy == null && oldExpirationPolicy == null) {
            return;
        }

        if (expirationPolicy != null && expirationPolicy.equals(oldExpirationPolicy)) {
            return;
        }

        updateAccessConfiguration(centralConfigService.getDescriptor().getSecurity());
    }

    private void bootstrapAccessClient() {
        synchronized (accessClientLock) {
            AccessClient oldAccessClient = getAccessClient();
            boolean shouldCloseOldClient = false;
            try {
                AccessClient newAccessClient = buildAccessClient();
                waitForAccessServer(newAccessClient);
                // don't close newAccessClient - they share the same httpClientContext. no auth is needed here.
                newAccessClient = bootstrapAccessClient(newAccessClient.useAuth(null)).getAccessClient();
                updateAccessClient(newAccessClient);
                shouldCloseOldClient = true;
                configStore.invalidateAdminCredentialsCache();
            } finally {
                if (shouldCloseOldClient) {
                    IOUtils.closeQuietly(oldAccessClient);
                }
            }
        }
    }

    private AccessClientBootstrap bootstrapAccessClient(AccessClient accessClientWithoutAuth) {
        String jfrogRouterUrl = System.getProperty(JFROG_ROUTER_URL);
        if (StringUtils.isNotBlank(jfrogRouterUrl)) {
            ArtifactoryRouterRegistrationData artifactoryRouterRegistrationData = new ArtifactoryRouterRegistrationData(
                    getArtifactoryServiceId(), artifactoryHome.getHaAwareHostId());
            return new AccessClientBootstrap(configStore, accessClientWithoutAuth,
                    joinKeyBootstrapper.getJoinKey(isUsingBundledAccessServer()),
                    artifactoryRouterRegistrationData.build());
        }
        return new AccessClientBootstrap(configStore, accessClientWithoutAuth,
                joinKeyBootstrapper.getJoinKey(isUsingBundledAccessServer()));
    }

    private void updateAccessConfiguration(SecurityDescriptor security) {
        log.info("Updating access configuration with password expiration data");
        Map<String, Object> configMap = securityToConfigMap(security);
        updateConfigurationModel(configMap);
    }

    private void updateConfigurationModel(Map<String, Object> configMap) {
        try {
            getAccessClient().config().updateConfigurationModel(configMap);
        } catch (AccessClientException e) {
            log.error("Error while trying to update Access configuration", e);
        }
    }

    private Map<String, Object> securityToConfigMap(SecurityDescriptor security) {
        PasswordExpirationPolicy expirationPolicy = security.getPasswordSettings().getExpirationPolicy();
        return ImmutableMap.of("security",
                ImmutableMap.of("user-lock-policy",
                        ImmutableMap.of("password-expiry-days",
                                expirationPolicy.getEnabled() ? expirationPolicy.getPasswordMaxAge() : 0)));
    }

    @Override
    public void destroy() {
        IOUtils.closeQuietly(getAccessClient());
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {

    }

    @Override
    public void ping() {
        try {
            runWithFallback(() -> getAccessClient().useAuth(null).ping());
        } catch (AccessClientException e) {
            throw new AccessClientException("Access service is unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportTo(ExportSettings settings) {
        try {
            exportServiceId(settings);
        } catch (IOException e) {
            log.debug("Error during serviceId backup", e);
            settings.getStatusHolder().error("Error during serviceId backup", e, log);
        }

        if (!isUsingBundledAccessServer()) {
            log.debug("Artifactory is not using its bundled access server - skipping triggering access server export");
            return;
        }

        try {
            log.info("Triggering export in access server...");
            getAccessClient().system().exportAccessServer();
            Path exportEtcFolder = settings.getBaseDir().toPath().resolve("etc");
            File accessServerBackupDir = new File(artifactoryHome.getBundledAccessHomeDir(), "backup");
            try (Stream<Path> backupListStream = Files.list(accessServerBackupDir.toPath())) {
                backupListStream
                        .filter(p -> !p.toFile().isDirectory())
                        .filter(p -> p.toFile().getName().matches("access\\.backup\\..+\\.json"))
                        .max(Comparator.comparing(p -> p.toFile().lastModified()))
                        .ifPresent(path -> copyFile(path, exportEtcFolder));
            }
        } catch (Exception e) {
            log.debug("Error during access server backup", e);
            settings.getStatusHolder().error("Error during access server backup", e, log);
        }
    }

    private void exportServiceId(ExportSettings settings) throws IOException {
        log.info("Exporting serviceId to file");
        File targetBackupEtcDir = new File(settings.getBaseDir(), ETC_DIR_NAME);
        File serviceIdFile = new File(targetBackupEtcDir, getServiceIdFilePathUnderEtc());
        FileUtils.forceMkdir(serviceIdFile.getParentFile());
        Files.write(serviceIdFile.toPath(), serviceId.toString().getBytes());
    }

    private Path copyFile(Path srcFile, Path targetFolder) {
        Path trgFile = null;
        try {
            Files.createDirectories(targetFolder);
            trgFile = targetFolder.resolve(ACCESS_BOOTSTRAP_JSON);
            Path destination_path = Files.copy(srcFile, trgFile, COPY_ATTRIBUTES, REPLACE_EXISTING);
            // Set permissions to 644 in order to be able to read the file on import (Access sets permission to 700)
            setPermissionsOnSecurityFile(destination_path, PERMISSIONS_MODE_644);
            return destination_path;
        } catch (IOException e) {
            String error = "Unable to copy the file from '" + srcFile + "' to '" + trgFile + "'";
            log.debug(error, e);
            throw new RuntimeException(error, e);
        }
    }

    @Override
    public void importSecurityEntities(SecurityInfo securityInfo, boolean override) {
        try {
            runWithFallback(() ->
                    getAccessClient().imports().importSecurityEntities(buildImportRequest(securityInfo), override));
        } finally {
            configStore.invalidateAdminCredentialsCache();
        }
    }

    private ImportEntitiesRequest buildImportRequest(SecurityInfo securityInfo) {
        ImportEntitiesRequest.Builder entities = ImportEntitiesRequest.builder();
        List<GroupInfo> groups = securityInfo.getGroups();

        if (groups != null) {
            for (GroupInfo group : groups) {
                entities.addGroup(GroupMapper.toFullAccessGroup(group));
            }
        }
        List<UserInfo> users = securityInfo.getUsers();
        if (users != null) {
            for (UserInfo user : users) {
                entities.addUser(UserMapper.toFullAccessUser(user));
            }
        }
        String serviceId = getArtifactoryServiceId().getFormattedName();
        List<RepoAcl> repoAcls = securityInfo.getRepoAcls();
        if (repoAcls != null) {
            for (RepoAcl acl : repoAcls) {
                entities.addPermission(toFullAccessPermission(acl, serviceId, REPO));
            }
        }
        List<BuildAcl> buildAcls = securityInfo.getBuildAcls();
        if (buildAcls != null) {
            for (BuildAcl acl : buildAcls) {
                entities.addPermission(toFullAccessPermission(acl, serviceId, BUILD));
            }
        }
        List<ReleaseBundleAcl> releaseBundleAcls = securityInfo.getReleaseBundleAcls();
        if (releaseBundleAcls != null) {
            for (ReleaseBundleAcl acl : releaseBundleAcls) {
                entities.addPermission(toFullAccessPermission(acl, serviceId, RELEASE_BUNDLES));
            }
        }
        return entities.build();
    }

    @Override
    public void importFrom(ImportSettings settings) {
        importAccessServer(settings);
    }

    @Override
    public void afterImport(ImportSettings settings) {
        FileUtils.deleteQuietly(artifactoryHome.getAccessAdminTokenFile());
        String oldServiceId = initServiceId();
        initAccessService(oldServiceId);
        // We call the registration here as well since the default registration happens on afterContextCreated,
        // which will not happen after import.
        registerInFederation(null, true);
        publisher.publishEvent(new AccessImportEvent(this));
    }

    private void importAccessServer(ImportSettings settings) {
        if (!isUsingBundledAccessServer()) {
            log.debug("Artifactory is not using its bundled access server - skipping triggering access server import");
            return;
        }
        log.info("Triggering import in access server...");
        File bootstrapFile = new File(settings.getBaseDir(), "etc" + File.separator + ACCESS_BOOTSTRAP_JSON);
        if (bootstrapFile.exists()) {
            Path targetPath = new File(artifactoryHome.getBundledAccessHomeDir(),
                    "etc" + File.separator + ACCESS_BOOTSTRAP_JSON)
                    .toPath();
            boolean failed = false;
            try {
                FileUtils.forceMkdir(targetPath.toFile().getParentFile());
                Files.copy(bootstrapFile.toPath(), targetPath, COPY_ATTRIBUTES, REPLACE_EXISTING);
            } catch (IOException e) {
                failed = true;
                log.debug("Failed to import access bootstrap file: {}", bootstrapFile.getAbsolutePath(), e);
                settings.getStatusHolder()
                        .error("Failed to import access bootstrap file: " + bootstrapFile.getAbsolutePath()
                                + " Please check file permissions", e, log);
            }
            if (!failed) {
                try {
                    getAccessClient().system().importAccessServer();
                    configStore.invalidateCache();
                    bootstrapAccessClient();
                    publisher.publishEvent(new InvalidateCacheEvent(this, CacheType.ACL));
                } catch (Exception e) {
                    log.debug("Error during access server restore", e);
                    settings.getStatusHolder().error("Error during access server restore", e, log);
                }
            }
        }
    }

    @Override
    public boolean isUsingBundledAccessServer() {
        return configStore.isUsingBundledAccessServer();
    }

    @Override
    public void revokeAllForUserAndScope(String username, String scope) {
        accessClient.token().revokeAllForUserAndScope(username, scope);
    }

    @Override
    public boolean isAdminUsingOldDefaultPassword() {

        long timeDifference = ConstantValues.accessAdminDefaultPasswordCheckPeriod.getLong();
        long now = System.currentTimeMillis();
        if (BooleanUtils.isNotFalse(hasAdminSecurityIssue)
                && lastAdminSecurityIssueCheck < now - timeDifference) {
            LoginRequest loginRequest = new LoginRequest()
                    .username(ADMIN_DEFAULT_USERNAME)
                    .password(DEFAULT_ADMIN_PASSWORD);
            lastAdminSecurityIssueCheck = now;
            try {
                accessClient.auth().authenticate(loginRequest);
                hasAdminSecurityIssue = true;
            } catch (AccessClientException e) {
                hasAdminSecurityIssue = false;
            } catch (Exception e) {
                log.error("There is a Problem testing access admin request");
                return true;
            }
        }
        return hasAdminSecurityIssue;
    }

    @Override
    public void onLicenseLoaded() {
        sendLicenseToAccess();
    }

    private void initAccessAdminDefaultPasswordCheck() {
        if (isAdminUsingOldDefaultPassword()) {
            log.warn("Your Artifactory instance is set up with default credentials to connect to the security " +
                    "datastore via localhost. It is highly recommended to change the default password. " +
                    "Additional details here: " +
                    "https://jfrog.com/knowledge-base/How-to-change-the-default-password-for-access-admin-user");
        }
    }

    private void sendLicenseToAccess() {
        if (StringUtils.isBlank(accessServerName)) {
            accessServerName = getAccessServerName();
        }
        if (addonsManager.isLicenseInstalled()) {
            String licenseKey = addonsManager.getLicenseKey() == null ? "" : addonsManager.getLicenseKey();
            ImmutableList<ImmutableMap<String, String>> licenses =
                    ImmutableList.of(ImmutableMap.of("server-name", accessServerName, "key", licenseKey));
            updateConfigurationModel(ImmutableMap.of("licenses", licenses));
        }
    }

    private String getAccessServerName() {
        return ensureAuth(() -> getAccessClient().system().getAccessServerName());
    }

    private static class ContextStateDependantActionRunner implements ContextCreationListener {

        private final List<Runnable> onContextCreatedActions = Lists.newArrayList();
        private boolean contextCreated = false;

        @Override
        public void onContextCreated() {
            contextCreated = true;
            onContextCreatedActions.forEach(Runnable::run);
            onContextCreatedActions.clear();
        }

        void runAfterContextCreated(Runnable action) {
            if (contextCreated) {
                action.run();
            } else {
                onContextCreatedActions.add(action);
            }
        }
    }

    void runAfterContextCreated(Runnable action) {
        contextStateDependantActionRunner.runAfterContextCreated(action);
    }

    @Override
    public void onContextCreated() {
        contextStateDependantActionRunner.onContextCreated();
        // Reason for the registration being here and not after init of the Access client is that we don't want to
        // screw up the startup and it's converters in case the registration fails.
        registerInFederation(null, true);
    }

    /**
     * Register/Unregister Artifactory's ServiceId in Access Federation.
     * In case *registration* fails we log an error, however if the license is Edge or Enterprise+ we stop the startup.
     * In case of *un-register* fails we don't check the license type, only log an error.
     *
     * @param register TRUE for register, FALSE for un-register
     */
    private void registerInFederation(@Nullable String originalServiceId, boolean register) {
        if (!register && originalServiceId == null) {
            log.debug("No original serviceId detected, no need to unregister in Access Federation");
            return;
        }
        String action = register ? "register" : "unregister";
        String serviceId = register ? getArtifactoryServiceId().getFormattedName() : originalServiceId;
        try {
            if (register) {
                runWithFallback(() -> getAccessClient().registry().registerServiceId(getArtifactoryServiceId()));
            } else {
                runWithFallback(() -> getAccessClient().registry().unregisterServiceId(createServiceId(serviceId)));
            }
            log.info("Successful {} of Artifactory serviceId {} in Access Federation", action, serviceId);
        } catch (AccessClientException | IllegalArgumentException e) {
            log.error("Failed to {} Artifactory serviceId {}: {}", action, serviceId, e.getMessage());
            log.debug("Failed to {} Artifactory serviceId", action, e);
            // For registration if the license is edge or e+ we stop the instance
            if (register && (addonsManager.isEdgeLicensed() || addonsManager.isEnterprisePlusInstalled())) {
                throw new IllegalStateException("Can't start Artifactory without registering in Access Federation", e);
            }
        }
    }

    private void pause() {
        try {
            log.debug("Pinging access server did not succeed, waiting for 2000ms before retrying...");
            sleep(2000);
        } catch (InterruptedException e) {
            log.warn("Waiting for access server got interrupted. Continue.", e);
            throw new RuntimeException("Waiting for access server got interrupted.", e);
        }
    }

}
