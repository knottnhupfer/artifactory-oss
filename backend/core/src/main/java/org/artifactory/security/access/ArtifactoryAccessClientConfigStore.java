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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.security.accesstoken.AccessClientSettings;
import org.artifactory.util.ExceptionUtils;
import org.jfrog.access.client.AccessAuthToken;
import org.jfrog.access.client.AccessClientBuilder;
import org.jfrog.access.client.AccessClientException;
import org.jfrog.access.client.RootCertificateHolder;
import org.jfrog.access.client.confstore.AccessClientConfigStore;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.util.AccessCredsFileHelper;
import org.jfrog.access.version.AccessVersion;
import org.jfrog.common.TomcatUtils;
import org.jfrog.config.wrappers.FileEventType;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.file.PemHelper;
import org.jfrog.security.file.SecurityFolderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.artifactory.common.ConstantValues.isDevOrTest;
import static org.jfrog.access.util.AccessCredsFileHelper.ADMIN_DEFAULT_USERNAME;
import static org.jfrog.security.file.SecurityFolderHelper.PERMISSIONS_MODE_600;

/**
 * @author Yinon Avraham.
 */
public class ArtifactoryAccessClientConfigStore implements AccessClientConfigStore {

    private static final Logger log = LoggerFactory.getLogger(ArtifactoryAccessClientConfigStore.class);
    private static final String CACHED_SERVER_URL = "serverUrl";
    private static final String CACHED_ADMIN_TOKEN = "adminToken";
    private static final String CACHED_ADMIN_CREDS = "adminCreds";
    public static final int ACCESS_PREFERRED_PORT = 8040;

    private final Cache<String, Object> cachedValues = CacheBuilder.newBuilder().build();
    private final InternalCentralConfigService configService;
    private final ArtifactoryHome artifactoryHome;
    private final File rootCrtFile;
    private final File clientVersionFile;
    private final File bootstrapCredsFile;
    private final File accessAdminCredsFile;
    private ServiceId serviceId;

    ArtifactoryAccessClientConfigStore(AccessServiceImpl accessService, ServiceId serviceId) {
        this.configService = requireNonNull(accessService.centralConfigService(), "central config service is required");
        this.artifactoryHome = requireNonNull(accessService.artifactoryHome(), "Artifactory home is required");
        this.rootCrtFile = new File(artifactoryHome.getAccessClientDir(), "keys/root.crt");
        this.clientVersionFile = new File(artifactoryHome.getAccessClientDir(), "data/access.version.properties");
        this.bootstrapCredsFile = new File(artifactoryHome.getAccessClientDir(), "bootstrap.creds");
        this.accessAdminCredsFile = artifactoryHome.getAccessAdminCredsFile();
        this.serviceId = requireNonNull(serviceId, "service ID is required");
        convertClientConfigFromEmbeddedAccessServerIfNeeded();
    }

    private void convertClientConfigFromEmbeddedAccessServerIfNeeded() {
        try {
            // Convert the access client config from embedded server to bundled server according to the existence of the
            // admin token file.
            Path keysFolder = artifactoryHome.getAccessClientDir().toPath().resolve("keys");
            File adminTokenFile = keysFolder.resolve(serviceId + ".token").toFile();
            if (adminTokenFile.exists()) {
                log.debug("Admin token file exists: '{}', starting access client config conversion from " +
                        "embedded server to bundled server.", adminTokenFile.getAbsolutePath());
                // Create the access.creds file with default credentials (this is under the assumption that the embedded
                // access server always had these credentials)
                AccessCredsFileHelper.saveAccessCreds(accessAdminCredsFile, ADMIN_DEFAULT_USERNAME, "password");
                SecurityFolderHelper.setPermissionsOnSecurityFile(accessAdminCredsFile.toPath(), PERMISSIONS_MODE_600);
                // remove the old format of the admin token
                Files.delete(adminTokenFile.toPath());
                // Remove obsolete files
                Files.deleteIfExists(keysFolder.resolve(serviceId + ".token"));
                Files.deleteIfExists(keysFolder.resolve(serviceId + ".key"));
                Files.deleteIfExists(keysFolder.resolve(serviceId + ".crt"));
                Files.deleteIfExists(keysFolder.resolve("keystore.jks"));
            } else {
                log.debug("Admin token file does not exist: '{}', skipping access client config conversion from " +
                        "embedded server to bundled server.", adminTokenFile.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert access client config from embedded to bundled server.", e);
        }
    }

    @Nonnull
    @Override
    public AccessClientBuilder newClientBuilder() {
        String adminToken = getAccessAdminToken();
        AccessClientBuilder clientBuilder = AccessClientBuilder.newBuilder()
                .serverUrl(getAccessServerSettings().getUrl())
                .serviceId(getServiceId())
                .rootCertificate(newRootCertificateHolder())
                .defaultAuth(adminToken == null ? null : new AccessAuthToken(adminToken))
                .forceRest(isForceRest())
                .forceGrpc(isForceGrpc());

        if (StringUtils.isNotBlank(System.getProperty("JFROG_ROUTER_URL"))) {
            try {
                URL url = new URL(getAccessServerSettings().getUrl());
                clientBuilder.grpcUrl(url.getHost() + ":" + url.getPort());
            } catch (MalformedURLException e) {
                log.error("Invalid Router URL", e);
                throw new AccessClientException(e);
            }
        }

        getTokenVerifyResultCacheSize().ifPresent(clientBuilder::tokenVerificationResultCacheSize);
        getTokenVerifyResultCacheExpiry().ifPresent(clientBuilder::tokenVerificationResultCacheExpiry);
        getConnectionTimeout().ifPresent(clientBuilder::connectionTimeout);
        getSocketTimeout().ifPresent(clientBuilder::socketTimeout);
        getMaxConnections().ifPresent(clientBuilder::maxConnections);

        return clientBuilder;
    }

    private boolean isForceGrpc() {
        return ConstantValues.accessClientForceGrpc.getBoolean(artifactoryHome);
    }

    private boolean isForceRest() {
        return ConstantValues.accessClientForceRest.getBoolean(artifactoryHome);
    }

    private OptionalInt getMaxConnections() {
        return getClientIntNonNegativeSetting(ConstantValues.accessClientMaxConnections,
                AccessClientSettings::getMaxConnections);
    }

    private OptionalInt getSocketTimeout() {
        return getClientIntNonNegativeSetting(ConstantValues.accessClientSocketTimeout,
                AccessClientSettings::getSocketTimeout);
    }

    private OptionalInt getConnectionTimeout() {
        return getClientIntNonNegativeSetting(ConstantValues.accessClientConnectionTimeout,
                AccessClientSettings::getConnectionTimeout);
    }

    private OptionalLong getTokenVerifyResultCacheSize() {
        return getClientLongSetting(ConstantValues.accessClientTokenVerifyResultCacheSize,
                AccessClientSettings::getTokenVerifyResultCacheSize);
    }

    private OptionalLong getTokenVerifyResultCacheExpiry() {
        return getClientLongSetting(ConstantValues.accessClientTokenVerifyResultCacheExpiry,
                AccessClientSettings::getTokenVerifyResultCacheExpirySeconds);
    }

    private OptionalInt getClientIntNonNegativeSetting(ConstantValues constantValue,
            Function<AccessClientSettings, Integer> configGetter) {
        OptionalLong optionalLong = getClientLongSetting(constantValue,
                clientSettings ->
                        Optional.ofNullable(configGetter.apply(clientSettings))
                                .map(Integer::longValue).orElse(null),
                value -> value <= 0
        );

        if (optionalLong.isPresent()) {
            return OptionalInt.of(Long.valueOf(optionalLong.getAsLong()).intValue());
        }

        return OptionalInt.empty();
    }

    private OptionalLong getClientLongSetting(ConstantValues constantValue,
            Function<AccessClientSettings, Long> configGetter) {
        return getClientLongSetting(constantValue, configGetter, value -> value < 0);
    }

    private OptionalLong getClientLongSetting(ConstantValues constantValue,
            Function<AccessClientSettings, Long> configGetter,
            Predicate<Long> defaultTester) {
        long value = constantValue.getLong(artifactoryHome);
        log.debug("sys-prop value: {}={}", constantValue.getPropertyName(), value);
        if (defaultTester.test(value)) {
            AccessClientSettings clientSettings = getAccessClientSettings();
            if (clientSettings != null) {
                Long settingValue = configGetter.apply(clientSettings);
                log.debug("Client settings config: {}={}", constantValue.getPropertyName(), value);
                value = settingValue == null ? value : settingValue;
            }
        }

        return defaultTester.test(value) ? OptionalLong.empty() : OptionalLong.of(value);
    }

    private RootCertificateHolder newRootCertificateHolder() {
        return new RootCertificateHolder() {
            @Nullable
            @Override
            public Certificate get() {
                return getRootCertificate();
            }

            @Override
            public void set(@Nullable Certificate certificate) {
                if (certificate != null) {
                    storeRootCertificate(certificate);
                }
            }
        };
    }

    public void setServiceId(@Nonnull ServiceId serviceId) {
        this.serviceId = requireNonNull(serviceId, "service ID is required");
    }

    @Nonnull
    @Override
    public ServiceId getServiceId() {
        if (serviceId == null) {
            throw new IllegalStateException("Service ID was not set");
        }
        return serviceId;
    }

    @Nonnull
    @Override
    public String getServiceNodeId() {
        return artifactoryHome.getHaAwareHostId();
    }

    @Override
    public void storeRootCertificate(@Nonnull Certificate certificate) {
        try {
            FileUtils.forceMkdir(rootCrtFile.getParentFile());
            PemHelper.saveCertificate(rootCrtFile, certificate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save root.crt file to path: " + rootCrtFile.getAbsolutePath(), e);
        }
    }

    @Nonnull
    @Override
    public Certificate getRootCertificate() {
        try {
            return PemHelper.readCertificate(rootCrtFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read root certificate from file: " + rootCrtFile.getAbsolutePath(),
                    e);
        }
    }

    @Override
    public boolean isRootCertificateExists() {
        return rootCrtFile.exists();
    }

    @Override
    public void storeAdminToken(@Nonnull String tokenValue) {
        setAdminToken(tokenValue);
    }

    @Nonnull
    @Override
    public String getAdminToken() {
        return Optional.ofNullable(getAccessAdminToken())
                .orElseThrow(() -> new IllegalStateException("Admin token missing"));
    }

    @Override
    public boolean isAdminTokenExists() {
        String adminToken = getCachedAdminToken();
        return ((adminToken != null) ? adminToken : getStoredRawAdminToken()) != null;
    }

    @Nullable
    private String getAccessAdminToken() {
        String adminToken = getCachedAdminToken();
        if (adminToken == null) {
            adminToken = getDecryptStoredAccessAdminToken();
            if (adminToken != null) {
                setCachedAdminToken(adminToken);
            }
        }
        return adminToken;
    }

    private String getDecryptStoredAccessAdminToken() {
        String storedAdminToken = getStoredRawAdminToken();
        return decryptIfNeeded(storedAdminToken);
    }

    private String decryptIfNeeded(String token) {
        if (token != null) {
            EncryptionWrapper encryptionWrapper = artifactoryHome.getArtifactoryEncryptionWrapper();
            token = encryptionWrapper.decryptIfNeeded(token).getDecryptedData();
        }
        return token;
    }

    @Nullable
    private String getStoredRawAdminToken() {
        File accessAdminTokenFile = artifactoryHome.getAccessAdminTokenFile();
        if (accessAdminTokenFile.exists()) {
            return getTokenFromAccessTokenFile(accessAdminTokenFile);
        }
        return null;
    }

    @Override
    @Nonnull
    public Stream<String> getAllAdminTokenCandidatesWithNullsForMissing() {
        File accessAdminTokenFile = artifactoryHome.getAccessAdminTokenFile();

        Stream<File> accessAdminTokenFiles = Stream.of(accessAdminTokenFile);// first the token file
        Stream<String> fileTokens = accessAdminTokenFiles.map(this::getTokenFromAccessTokenFile);

        AccessClientSettings clientSettings = getAccessClientSettings();
        if (clientSettings != null) { // add config xml candidate
            fileTokens = Stream.concat(fileTokens, Stream.of(clientSettings.getAdminToken()));
        }
        return fileTokens.map(this::decryptIfNeeded);
    }

    private String getTokenFromAccessTokenFile(File accessAdminTokenFile) {
        String adminToken = null;
        try {
            if (!accessAdminTokenFile.exists()) {
                return null;
            }
            String line = readFirstLine(accessAdminTokenFile);
            if (line != null) {
                adminToken = line;
                log.debug("Got raw access token: from file: {}", artifactoryHome.getAccessAdminTokenFile());
            }
        } catch (IOException e) {
            log.warn("Got IOException reading token file", e);
        }
        return adminToken;
    }


    String getCachedAdminToken() {
        return (String) cachedValues.getIfPresent(CACHED_ADMIN_TOKEN);
    }

    @Override
    public void revokeAdminToken() {
        setAdminToken(null);
    }

    private void setAdminToken(String adminToken) {
        if (adminToken == null) {
            cachedValues.invalidate(CACHED_ADMIN_TOKEN);
        } else {
            cachedValues.put(CACHED_ADMIN_TOKEN, adminToken);
        }

        File adminTokenFile = artifactoryHome.getAccessAdminTokenFile();
        saveAccessTokenToFile(adminTokenFile, adminToken);
    }

    void invalidateAdminCredentialsCache() {
        cachedValues.invalidate(CACHED_ADMIN_CREDS);
    }

    void setCachedAdminToken(String value) {
        cachedValues.put(CACHED_ADMIN_TOKEN, value);
    }

    @Nonnull
    @Override
    public String[] getBootstrapAdminCredentials() {
        try {
            String[] adminCreds = (String[]) cachedValues.get(CACHED_ADMIN_CREDS, () -> {
                String credsNotFoundMessage = "bootstrap admin credentials do not exist in the config store";
                IOThrowingSupplier<Map<String, String>> credsSupplier;
                if (bootstrapCredsFile.exists()) {
                    log.debug("Found bootstrap admin ");
                    credsSupplier = () -> AccessCredsFileHelper.readAccessCreds(bootstrapCredsFile);
                } else if (accessAdminCredsFile.exists()) {
                    credsSupplier = this::readAdminCredentials;
                } else {
                    throw new NoSuchElementException(credsNotFoundMessage);
                }
                Entry<String, String> creds = credsSupplier.get().entrySet().stream()
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException(credsNotFoundMessage));
                return new String[]{creds.getKey(), creds.getValue()};
            });
            return Arrays.copyOf(adminCreds, adminCreds.length);
        } catch (Exception e) {
            Throwable noSuchElementException = ExceptionUtils.getCauseOfType(e, NoSuchElementException.class);
            if (noSuchElementException != null) {
                throw (NoSuchElementException) noSuchElementException;
            }
            throw new RuntimeException("Failed to read admin credentials.", e);
        }
    }

    private interface IOThrowingSupplier<T> {
        T get() throws IOException;
    }

    private Map<String, String> readAdminCredentials() throws IOException {
        EncryptionWrapper encryptionWrapper = artifactoryHome.getArtifactoryEncryptionWrapper();
        String fileContent = FileUtils.readFileToString(accessAdminCredsFile);
        String accessCredsContent = encryptionWrapper.decryptIfNeeded(fileContent).getDecryptedData();
        return AccessCredsFileHelper.readAccessCreds(accessCredsContent);
    }

    @Override
    public boolean isBootstrapAdminCredentialsExist() {
        return bootstrapCredsFile.exists() || accessAdminCredsFile.exists();
    }

    @Override
    public void discardBootstrapAdminCredentials() {
        // deprecated
    }

    void invalidateCache() {
        cachedValues.invalidateAll();
    }

    @Override
    public AccessVersion getAccessClientVersion() {
        if (clientVersionFile.exists()) {
            Properties props = new Properties();
            try (InputStream input = new FileInputStream(clientVersionFile)) {
                props.load(input);
                return AccessVersion.read(props);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read access client version from file: " +
                        clientVersionFile.getAbsolutePath(), e);
            }
        }
        return null;
    }

    @Override
    public void storeAccessClientVersion(@Nonnull AccessVersion accessVersion) {
        Properties props = new Properties();
        accessVersion.write(props);
        try {
            FileUtils.forceMkdir(clientVersionFile.getParentFile());
            try (OutputStream out = new FileOutputStream(clientVersionFile)) {
                props.store(out, "JFrog Access Client Version");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save access client version to file: " + this.clientVersionFile, e);
        }
    }

    /**
     * Encrypt or decrypt the stored access credentials (if exists and if needed)
     *
     * @param encrypt flag to indicate the required action, <code>true</code> to encrypt, <code>false</code> to decrypt.
     */
    void encryptOrDecryptAccessCreds(boolean encrypt) {
        EncryptionWrapper encryptionWrapper = artifactoryHome.getArtifactoryEncryptionWrapper();
        if (encrypt) {
            applyEncryptDecryptOnAccessCreds("encrypt", encryptionWrapper::encryptIfNeeded);
        } else {
            applyEncryptDecryptOnAccessCreds("decrypt", s -> encryptionWrapper.decryptIfNeeded(s).getDecryptedData());
        }
    }

    private void applyEncryptDecryptOnAccessCreds(String action, Function<String, String> encryptDecrypt) {
        if (accessAdminCredsFile.exists()) {
            try {
                String adminCreds = FileUtils.readFileToString(accessAdminCredsFile);
                FileUtils.write(accessAdminCredsFile, encryptDecrypt.apply(adminCreds));
                // Forcing a change to the other nodes - update file to db and propagate
                ContextHelper.get().getConfigurationManager().forceFileChanged(accessAdminCredsFile,
                        "artifactory.security.", FileEventType.MODIFY);
            } catch (IOException e) {
                log.error("Could not " + action + " access admin credentials file '" +
                        accessAdminCredsFile.getAbsolutePath() + "': " + e.toString());
                log.debug("Could not " + action + " access admin credentials file '" +
                        accessAdminCredsFile.getAbsolutePath() + "'.", e);
            } catch (Exception e) {
                log.debug("Failed to propagate access.creds change", e);
                log.warn("Failed to propagate access.creds change");
            }
        }
    }

    private void saveAccessTokenToFile(@Nonnull File tokenFile, String tokenString) {
        if (tokenString == null) {
            try {
                log.debug("Deleting access.admin.token");
                Files.deleteIfExists(tokenFile.toPath());
            } catch (IOException e) {
                log.error("Failed delete token file {}", tokenFile, e);
            }
        } else {
            try {
                tokenString = artifactoryHome.getArtifactoryEncryptionWrapper().encryptIfNeeded(tokenString);
                log.debug("Storing token in file access.admin.token");
                Files.write(tokenFile.toPath(), Collections.singletonList(tokenString));
                SecurityFolderHelper.setPermissionsOnSecurityFile(tokenFile.toPath(), PERMISSIONS_MODE_600);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write token to file '" +
                        tokenFile.getAbsolutePath() + "'", e);
            }
        }
    }

    /**
     * Check whether using the bundled access server.
     *
     * @see #getAccessServerSettings()
     */
    @Override
    public boolean isUsingBundledAccessServer() {
        return getAccessServerSettings().isBundled();
    }

    /**
     * Chooses the bundled Access Server URL to use with the following strategy:
     * <ol>
     * <li>System property {@link ConstantValues#accessClientServerUrlOverride} - can be used in tests, online, etc.
     * (NOT-BUNDLED, unless defined otherwise using {@link ConstantValues#accessServerBundled})</li>
     * <li>Config descriptor - Access Client Settings
     * (NOT-BUNDLED, unless defined otherwise using {@link ConstantValues#accessServerBundled})</li>
     * <li>Dev/test mode by default uses a spawned access standalone process with default port
     * (yields NOT-BUNDLED)</li>
     * <li>Detect port on localhost - this is the default case for production Artifactory with bundled Access
     * (yields BUNDLED)</li>
     * </ol>
     *
     * @return the Access Server URL
     *
     * @see ConstantValues#accessClientServerUrlOverride
     * @see ConstantValues#accessServerBundled
     * @see ConstantValues#test
     * @see ConstantValues#dev
     * @see ConstantValues#devHa
     */
    private AccessServerSettings getAccessServerSettings() {
        try {
            AccessServerSettings server = (AccessServerSettings) cachedValues.get(CACHED_SERVER_URL, () -> {
                BooleanSupplier accessServerBundled = () ->
                        ConstantValues.accessServerBundled.isSet(artifactoryHome) &&
                                ConstantValues.accessServerBundled.getBoolean(artifactoryHome);
                AccessServerSettings serverSettings = null;
                String serverUrl;
                //1- sys-prop with url
                log.debug("Checking for overriding server URL constant value.");
                serverUrl = ConstantValues.accessClientServerUrlOverride.getString(artifactoryHome);
                //By default the default server URL is null, only if it was set explicitly then it is used
                if (isNotBlank(serverUrl)) {
                    boolean bundled = accessServerBundled.getAsBoolean();
                    serverSettings = new AccessServerSettings(serverUrl, bundled, "system property");
                }
                //2- Config descriptor
                log.debug("Checking for Access Server URL in the config descriptor");
                serverUrl = getAccessClientSettings().getServerUrl();
                if (isNotBlank(serverUrl)) {
                    if (serverSettings == null) {
                        boolean bundled = accessServerBundled.getAsBoolean();
                        serverSettings = new AccessServerSettings(serverUrl, bundled, "config");
                    } else {
                        log.warn(
                                "*** Access Server URL is defined in both config XML ({}) and the '{}' system property ({}) ***" +
                                        "\nThis is not a healthy state - only a single method shall be defined! " +
                                        "Currently the URL from the system property will be used.", serverUrl,
                                ConstantValues.accessClientServerUrlOverride.getPropertyName(),
                                serverSettings.getUrl());
                    }
                }
                //3- dev/test mode - default url
                if (serverSettings == null) {
                    serverUrl = getDevOrTestDefaultAccessServerUrl();
                    if (isNotBlank(serverUrl)) {
                        log.debug("Running in dev/test mode - using Access server URL: {}", serverUrl);
                        serverSettings = new AccessServerSettings(serverUrl, false, "dev/test default");
                    }
                }
                //4- detect running in the same web container
                if (serverSettings == null) {
                    serverUrl = detectBundledAccessServerUrl();
                    log.debug("Detected bundled server URL: {}", serverUrl);
                    serverSettings = new AccessServerSettings(serverUrl, true, "detected");
                }
                log.info("Using Access Server URL: {}", serverSettings);
                return serverSettings;
            });
            return server;
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not get access server url and bundled mode", e);
        }
    }

    private AccessClientSettings getAccessClientSettings() {
        return configService.getDescriptor().getSecurity().getAccessClientSettings();
    }

    private String detectBundledAccessServerUrl() {
        log.debug("Heuristically detecting bundled server URL.");
        TomcatUtils.ConnectorDetails connector = TomcatUtils.getConnector("http", ACCESS_PREFERRED_PORT);
        return connector.buildUrl("localhost", "access");
    }

    private String getDevOrTestDefaultAccessServerUrl() {
        log.debug("Checking for dev/test server URL.");
        if (isDevOrTest(artifactoryHome)) {
            return "https://localhost:8340";
        }
        return null;
    }

    private static class AccessServerSettings {
        private final String url;
        private final boolean bundled;
        private final String source;

        private AccessServerSettings(String url, boolean bundled, String source) {
            this.url = url;
            this.bundled = bundled;
            this.source = source;
        }

        public String getUrl() {
            return url;
        }

        public String getGrpcUrl() {
            return System.getProperty("JFROG_ROUTER_URL");
        }

        public boolean isBundled() {
            return bundled;
        }

        @Override
        public String toString() {
            return url + " (" + (bundled ? "" : "not ") + "bundled) source: " + source;
        }
    }

    private static String readFirstLine(File file) throws IOException {
        Path path = file.toPath();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return reader.readLine();
        }
    }
}
