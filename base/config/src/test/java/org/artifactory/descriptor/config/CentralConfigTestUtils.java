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

package org.artifactory.descriptor.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.addon.AddonSettings;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.cleanup.CleanupConfigDescriptor;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.eula.EulaDescriptor;
import org.artifactory.descriptor.gc.GcConfigDescriptor;
import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.quota.QuotaConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.distribution.rule.DistributionCoordinates;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.descriptor.repo.vcs.VcsType;
import org.artifactory.descriptor.security.*;
import org.artifactory.descriptor.security.accesstoken.AccessClientSettings;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.descriptor.security.ldap.group.LdapGroupPopulatorStrategies;
import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;
import org.artifactory.descriptor.security.oauth.OAuthProviderSettings;
import org.artifactory.descriptor.security.oauth.OAuthSettings;
import org.artifactory.descriptor.security.signingkeys.SigningKeysSettings;
import org.artifactory.descriptor.security.sshserver.SshServerSettings;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.descriptor.security.sso.HttpSsoSettings;
import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.descriptor.subscription.SubscriptionConfig;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.jfrog.common.config.diff.*;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jfrog.common.ExceptionUtils.wrapException;

/**
 * @author Noam Shemesh
 */
public class CentralConfigTestUtils {
    public static CentralConfigDescriptorImpl initCentralConfig() {
        CentralConfigDescriptorImpl cc = new CentralConfigDescriptorImpl();

        LocalRepoDescriptor local1 = new LocalRepoDescriptor();
        local1.setKey("local1");
        local1.setOptionalIndexCompressionFormats(Lists.newArrayList("fileFormat" + 1));
        cc.addLocalRepository(local1);
        LocalRepoDescriptor local2 = new LocalRepoDescriptor();
        local2.setKey("local2");
        local2.setOptionalIndexCompressionFormats(Lists.newArrayList("fileFormat" + 2));
        cc.addLocalRepository(local2);

        RemoteRepoDescriptor remote1 = new HttpRepoDescriptor();
        remote1.setKey("remote1");
        cc.addRemoteRepository(remote1);

        VirtualRepoDescriptor virtual1 = new VirtualRepoDescriptor();
        virtual1.setKey("virtual1");
        cc.addVirtualRepository(virtual1);

        ProxyDescriptor proxy1 = new ProxyDescriptor();
        proxy1.setKey("proxy1");
        cc.addProxy(proxy1, false);

        ProxyDescriptor proxy2 = new ProxyDescriptor();
        proxy2.setKey("proxy2");
        cc.addProxy(proxy2, false);

        BackupDescriptor backup1 = new BackupDescriptor();
        backup1.setKey("backup1");
        cc.addBackup(backup1);

        BackupDescriptor backup2 = new BackupDescriptor();
        backup2.setKey("backup2");
        cc.addBackup(backup2);

        BackupDescriptor backup3 = new BackupDescriptor();
        backup3.setKey("backup3");
        backup3.setExcludeNewRepositories(true);
        cc.addBackup(backup3);

        PropertySet set1 = new PropertySet();
        set1.setName("set1");
        cc.addPropertySet(set1);

        PropertySet set2 = new PropertySet();
        set2.setName("set2");
        cc.addPropertySet(set2);

        RepoLayout repoLayout1 = new RepoLayout();
        repoLayout1.setName("layout1");
        cc.addRepoLayout(repoLayout1);

        RepoLayout repoLayout2 = new RepoLayout();
        repoLayout2.setName("layout2");
        cc.addRepoLayout(repoLayout2);

        RemoteReplicationDescriptor remoteReplication1 = new RemoteReplicationDescriptor();
        remoteReplication1.setCronExp("0 0/5 * * * ?");
        remoteReplication1.setRepoKey("remote1");
        cc.addRemoteReplication(remoteReplication1);

        RemoteReplicationDescriptor remoteReplication2 = new RemoteReplicationDescriptor();
        remoteReplication2.setCronExp("0 0/6 * * * ?");
        remoteReplication2.setRepoKey("remote2");
        cc.addRemoteReplication(remoteReplication2);

        LocalReplicationDescriptor localReplication1 = new LocalReplicationDescriptor();
        localReplication1.setCronExp("0 0/7 * * * ?");
        localReplication1.setRepoKey("local1");
        localReplication1.setUrl("http://momo.com");
        localReplication1.setUsername("user1");
        localReplication1.setPassword("password1");
        localReplication1.setEnableEventReplication(true);
        localReplication1.setReplicationKey("local1_http___momo");
        cc.addLocalReplication(localReplication1);

        LocalReplicationDescriptor localReplication2 = new LocalReplicationDescriptor();
        localReplication2.setCronExp("0 0/8 * * * ?");
        localReplication2.setRepoKey("local2");
        localReplication2.setUrl("http://popo.com");
        localReplication2.setUsername("user2");
        localReplication2.setPassword("password2");
        localReplication1.setReplicationKey("local2_http___popo");
        localReplication1.setEnableEventReplication(true);
        cc.addLocalReplication(localReplication2);

        AddonSettings addonSettings = new AddonSettings();
        addonSettings.setShowAddonsInfoCookie("Hello");
        cc.setAddons(addonSettings);
        return cc;
    }

    public static CentralConfigDescriptorImpl getFullConfig() {
        CentralConfigDescriptorImpl cc = initCentralConfig();

        cc.getPropertySets().forEach(propertySet -> {
            Property property = new Property(propertySet.getName() + "property");
            Property property2 = new Property(propertySet.getName() + "property2");
            property.setPredefinedValues(Lists.newArrayList(new PredefinedValue("123", true), new PredefinedValue("456", false)));
            property2.setPredefinedValues(Lists.newArrayList(new PredefinedValue("abc", true), new PredefinedValue("def", false)));
            property.setPropertyType("type1");
            property2.setPropertyType("type2");
            propertySet.setProperties(Lists.newArrayList(property, property2));
        });

        cc.getRepoLayouts().forEach(repoLayout -> {
            repoLayout.setArtifactPathPattern("artifactpath" + repoLayout.getName());
            repoLayout.setDescriptorPathPattern("descpath" + repoLayout.getName());
            repoLayout.setFileIntegrationRevisionRegExp("fileintg" + repoLayout.getName());
            repoLayout.setFolderIntegrationRevisionRegExp("folderintg" + repoLayout.getName());
        });

        cc.getLocalRepositoriesMap().values().forEach(repo -> {
            setRepo(cc, repo);
            repo.setPropertySets(cc.getPropertySets());

            repo.setRepoLayout(cc.getRepoLayout("layout1"));
            repo.setXrayConfig(new XrayRepoConfig());
            repo.setDownloadRedirectConfig(new DownloadRedirectRepoConfig());
            repo.setYumGroupFileNames("yum" + repo.getKey());
            repo.setChecksumPolicyType(LocalRepoChecksumPolicyType.SERVER);
            repo.setSnapshotVersionBehavior(SnapshotVersionBehavior.UNIQUE);
        });

        cc.getRemoteRepositoriesMap().values().forEach(repo -> {
            setRepo(cc, repo);
            repo.setRemoteRepoLayout(getRepoLayout(repo.getKey()));
            repo.setBower(new BowerConfiguration());
            repo.getBower().setBowerRegistryUrl("bower" + repo.getKey());
            repo.setChecksumPolicyType(ChecksumPolicyType.GEN_IF_ABSENT);
            repo.setCocoaPods(new CocoaPodsConfiguration());
            repo.getCocoaPods().setCocoaPodsSpecsRepoUrl("repourl" + repo.getKey());
            repo.getCocoaPods().setSpecRepoProvider(new VcsGitConfiguration());
            repo.getCocoaPods().getSpecRepoProvider().setDownloadUrl("downloadurl" + repo.getKey());
            repo.getCocoaPods().getSpecRepoProvider().setProvider(VcsGitProvider.GITHUB);
            repo.setComposer(new ComposerConfiguration());
            repo.getComposer().setComposerRegistryUrl("registryurl" + repo.getKey());
            repo.setContentSynchronisation(new ContentSynchronisation());
            repo.setMismatchingMimeTypesOverrideList("mismatch" + repo.getKey());
            repo.setNuget(new NuGetConfiguration());
            repo.getNuget().setDownloadContextPath("downloadpath" + repo.getKey());
            repo.getNuget().setFeedContextPath("feedcontext" + repo.getKey());
            repo.setP2OriginalUrl("p2o" + repo.getKey());
            repo.setPypi(new PypiConfiguration());
            repo.getPypi().setPyPIRegistryUrl("registryurl" + repo.getKey());
            repo.getPypi().setIndexContextPath("index" + repo.getKey());
            repo.getPypi().setPackagesContextPath("package" + repo.getKey());
            repo.setUrl("urlrl" + repo.getKey());
            repo.setVcs(new VcsConfiguration());
            repo.getVcs().setGit(new VcsGitConfiguration());
            repo.getVcs().getGit().setProvider(repo.getCocoaPods().getSpecRepoProvider().getProvider());
            repo.getVcs().getGit().setDownloadUrl("download" + repo.getKey());
            repo.getVcs().setType(VcsType.GIT);
            repo.setPropertySets(cc.getPropertySets());
            repo.setXrayConfig(new XrayRepoConfig());
            repo.setDownloadRedirectConfig(new DownloadRedirectRepoConfig());
            if (repo instanceof HttpRepoDescriptor) {
                ((HttpRepoDescriptor) repo).setClientTlsCertificate("tls-certificate");
                ((HttpRepoDescriptor) repo).setLocalAddress("http://localaddress");
                ((HttpRepoDescriptor) repo).setProxy(cc.getProxy("proxy" + repo.getKey().charAt(repo.getKey().length() - 1)));
                ((HttpRepoDescriptor) repo).setUsername("username-" + repo.getKey());
                ((HttpRepoDescriptor) repo).setPassword("password-" + repo.getKey());
                ((HttpRepoDescriptor) repo).setQueryParams("?query-params&for-repo-" + repo.getKey());
            }
            repo.setExternalDependencies(new ExternalDependenciesConfig());
            repo.getExternalDependencies().setPatterns(Lists.newArrayList("123" + repo.getKey(), "456" + repo.getKey()));
        });

        cc.getVirtualRepositoriesMap().values().forEach(repo -> {
            setRepo(cc, repo);
            repo.setRepositories(new ArrayList<>(cc.getVirtualRepositoriesMap().values()));
            repo.setDefaultDeploymentRepo(cc.getLocalRepositoriesMap().get("local1"));
            repo.setExternalDependencies(new ExternalDependenciesConfig());
            repo.getExternalDependencies().setRemoteRepo(cc.getRemoteRepositoriesMap().get("remote1"));
            repo.getExternalDependencies().setPatterns(Lists.newArrayList("123" + repo.getKey(), "456" + repo.getKey()));
            repo.setKeyPair("keypair" + repo.getKey());
            repo.setP2(new P2Configuration());
            repo.getP2().setUrls(Lists.newArrayList("url1" + repo.getKey(), "url2" + repo.getKey()));
            repo.setPomRepositoryReferencesCleanupPolicy(PomCleanupPolicy.discard_any_reference);
            repo.setVirtualCacheConfig(new VirtualCacheConfig());
            repo.getVirtualCacheConfig().setVirtualRetrievalCachePeriodSecs(1010);
            repo.setDebianDefaultArchitectures("i386,amd64");
            repo.setDebianOptionalIndexCompressionFormats(Lists.newArrayList("bz2"));
        });

        cc.getBackups().forEach(backup -> {
            backup.setCronExp("123");
            backup.setDir(new File("/tmp"));
            backup.setRetentionPeriodHours(1000);
            backup.setExcludedRepositories(new ArrayList<>(cc.getLocalRepositoriesMap().values()));
        });

        cc.getProxies().forEach(proxy -> {
            proxy.setHost("host" + proxy.getKey());
            proxy.setNtHost("nthost" + proxy.getKey());
            proxy.setPassword("password" + proxy.getKey());
            proxy.setRedirectedToHosts("redirected" + proxy.getKey());
            proxy.setUsername("username" + proxy.getKey());
            proxy.setDomain("domain" + proxy.getKey());
        });

        cc.getLocalReplications().forEach(lrd -> {
            lrd.setPathPrefix("pathPrefix" + lrd.getRepoKey());
            lrd.setProxy(cc.getProxy("proxy" + lrd.getRepoKey().charAt(lrd.getRepoKey().length() - 1)));
        });

        cc.getBintrayApplications().put("abc", new BintrayApplicationConfig("abc", "client", "secret", "org", "scope"));
        cc.getBintrayApplications().get("abc").setRefreshToken("refreshToken");
        cc.getBintrayApplications().put("foo", new BintrayApplicationConfig("foo", "client2", "secret2", "org2", "scope2"));
        cc.getBintrayApplications().get("foo").setRefreshToken("refreshToken2");

        cc.setBintrayConfig(new BintrayConfigDescriptor());
        cc.getBintrayConfig().setApiKey("apiKey");
        cc.getBintrayConfig().setUserName("username11");

        cc.setCleanupConfig(new CleanupConfigDescriptor());
        cc.getCleanupConfig().setCronExp("cron-exp");
        
        cc.setReverseProxies(Lists.newArrayList(getReverseProxy(cc, 1), getReverseProxy(cc, 2)));

        cc.setDistributionRepositoriesMap(new HashMap<>());
        cc.getDistributionRepositoriesMap().put("dist-repo-1", getDistributionRepo(cc, 1));
        cc.getDistributionRepositoriesMap().put("dist-repo-2", getDistributionRepo(cc, 2));

        cc.setReleaseBundlesRepositoriesMap(new HashMap<>());
        cc.getReleaseBundlesRepositoriesMap().put("rb-repo-1", getReleaseBundleRepo(cc, 1));
        cc.getReleaseBundlesRepositoriesMap().put("rb-repo-2", getReleaseBundleRepo(cc, 2));

        cc.setFolderDownloadConfig(new FolderDownloadConfigDescriptor());
        cc.setFooter("footer");

        cc.setGcConfig(new GcConfigDescriptor());
        cc.getGcConfig().setCronExp("cron-ta");

        cc.setIndexer(new IndexerDescriptor());
        cc.getIndexer().setCronExp("abcder");
        EulaDescriptor eulaDescriptor = new EulaDescriptor();
        eulaDescriptor.setAccepted(true);
        eulaDescriptor.setAcceptDate("xyz");
        cc.setEulaConfig(eulaDescriptor);
        SubscriptionConfig subscriptionConfig = new SubscriptionConfig();
        subscriptionConfig.setEmails(new HashSet<>());
        cc.setSubscriptionConfig(subscriptionConfig);
        SortedSet<RepoBaseDescriptor> sortedSet = new TreeSet<>();
        sortedSet.add(cc.getLocalRepositoriesMap().get("local1"));
        sortedSet.add(cc.getLocalRepositoriesMap().get("local2"));
        cc.getIndexer().setIncludedRepositories(sortedSet);
        cc.setLogo("logo!");
        cc.setMailServer(new MailServerDescriptor());
        cc.getMailServer().setArtifactoryUrl("artifacturl");
        cc.getMailServer().setFrom("from");
        cc.getMailServer().setHost("host");
        cc.getMailServer().setPassword("password");
        cc.getMailServer().setSubjectPrefix("subjectprefix");
        cc.getMailServer().setUsername("username");

        cc.setQuotaConfig(new QuotaConfigDescriptor());

        cc.setRemoteReplications(Lists.newArrayList(getRemoteReplication(cc, 1), getRemoteReplication(cc, 2)));

        addSecurityToCc(cc);

        cc.setServerName("servername");

        cc.setSumoLogicConfig(new SumoLogicConfigDescriptor());
        cc.getSumoLogicConfig().setProxy(cc.getProxy("proxy1"));
        cc.getSumoLogicConfig().setBaseUri("baseurl");
        cc.getSumoLogicConfig().setClientId("clientid");
        cc.getSumoLogicConfig().setCollectorUrl("collectorId");
        cc.getSumoLogicConfig().setDashboardUrl("dashboard");
        cc.getSumoLogicConfig().setSecret("secret");
        cc.setSystemMessageConfig(new SystemMessageDescriptor());
        cc.getSystemMessageConfig().setMessage("messssage");
        cc.getSystemMessageConfig().setTitle("title");
        cc.getSystemMessageConfig().setTitleColor("color!?");
        cc.setUrlBase("urlbase");
        cc.setVirtualCacheCleanupConfig(new CleanupConfigDescriptor());
        cc.getVirtualCacheCleanupConfig().setCronExp("croni");

        cc.setXrayConfig(new XrayDescriptor());
        cc.getXrayConfig().setArtifactoryId("art1");
        cc.getXrayConfig().setBaseUrl("base1");
        cc.getXrayConfig().setPassword("password");
        cc.getXrayConfig().setUser("user");
        cc.getXrayConfig().setXrayId("1212");
        cc.getXrayConfig().setProxy("charles-proxy");

        return cc;
    }

    // Reenable to print the configuration yaml for documentation purposes
    @Test(enabled = false)
    public void printFullCentralConfigYaml() {
        System.out.println(objectToYaml(getFullConfig(), ""));
    }

    private String objectToYaml(Object object, String prefix) {
        if (object instanceof Collection) {
            return ((Collection<Object>) object).stream()
                    .map(val -> objectToYaml(val, prefix + "  "))
                    .map(StringUtils::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("\n" + prefix + "- ", "\n" + prefix + "- ", ""));
        }

        if (object instanceof Map) {
            return ((Map<Object, Object>) object).entrySet().stream()
                    .map(entry -> prefix + entry.getKey() + ":" +
                            objectToYaml(entry.getValue(), prefix + "  "))
                    .map(StringUtils::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("\n" + prefix, "\n" + prefix, ""));
        }

        if (DiffMerger.isPrimitiveOrWrapperOrAtomic(object.getClass(), null)) {
            return "" + object;
        }

        return Stream.of(object.getClass().getMethods())
                .filter(method -> method.getName().startsWith("get") || method.getName().startsWith("is"))
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> method.getAnnotation(DiffIgnore.class) == null)
                .filter(method -> !method.getName().equals("getClass") &&
                        !method.getName().equals("getDeclaringClass") &&
                        !method.getName().equals("isEmpty"))
                .sorted((method1, method2) -> {
                    if (method1.getAnnotation(DiffKey.class) != null) {
                        return -1;
                    }
                    if (method2.getAnnotation(DiffKey.class) != null) {
                        return 1;
                    }
                    return method1.getName().compareTo(method2.getName());
                })
                .map(method -> {
                    try {
                        Object newObject = wrapException(() -> method.invoke(object), IllegalStateException.class);
                        if (newObject == null) {
                            System.out.println(method.getName() + " invoke is null");
                            return "";
                        }
                        if (StringUtils.isEmpty(newObject.toString())) {
                            return "";
                        }
                        if (ClassUtils.isAssignable(method.getReturnType(), Collection.class)) {
                            Class<?> parameterType = DiffMerger.findParameterType(
                                    ((ParameterizedType) method.getGenericReturnType())
                                            .getActualTypeArguments()[0]);
                            boolean primitive = DiffMerger.isPrimitiveOrWrapperOrAtomic(parameterType, null);
                            Function<Object, Object> keyMethod = primitive ? Function.identity() :
                                    v -> wrapException(() -> DiffMerger.findKeyMethod(parameterType).invoke(v), IllegalStateException.class);
                            if (method.getAnnotation(DiffReference.class) != null) {
                                newObject = ((Collection) newObject).stream()
                                        .map(keyMethod)
                                        .collect(Collectors.toList());
                            } else if (!primitive) {
                                String keyName = DiffUtils.toFieldName(DiffMerger.findKeyMethod(parameterType).getName());
                                newObject = ((Collection) newObject).stream()
                                        .collect(Collectors.toMap(keyMethod, Function.identity()));

                                ((Map) newObject).forEach((key, val) ->
                                        wrapException(() -> DiffMerger.findSetter(parameterType.getMethods(), keyName).invoke(val, ""), IllegalStateException.class));
                            }
                        } else if (method.getAnnotation(DiffReference.class) != null) {
                            Method keyMethod = DiffMerger.findKeyMethod(method.getReturnType());
                            Object fNewObject = newObject;
                            newObject = wrapException(() -> keyMethod.invoke(fNewObject), IllegalStateException.class);
                        } else if (ClassUtils.isAssignable(method.getReturnType(), Map.class)) {
                            Class<?> parameterType = DiffMerger.findParameterType(
                                    ((ParameterizedType) method.getGenericReturnType())
                                            .getActualTypeArguments()[1]);
                            String keyName = DiffUtils.toFieldName(DiffMerger.findKeyMethod(parameterType).getName());

                            ((Map) newObject).forEach((key, val) ->
                                    wrapException(() -> DiffMerger.findSetter(parameterType.getMethods(), keyName).invoke(val, ""), IllegalStateException.class));
                        }

                        String methodName = (method.getAnnotation(DiffElement.class) != null &&
                                !method.getAnnotation(DiffElement.class).name().isEmpty() ?
                                method.getAnnotation(DiffElement.class).name() : method.getName());

                        return prefix + DiffUtils.toFieldName(methodName) + ": " +
                                objectToYaml(newObject, prefix + "  ");
                    } catch (IllegalStateException | IllegalArgumentException e) {
                        throw new IllegalStateException("Error while processing " + method.getName(), e);
                    }
                })
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n", "\n", ""));
    }

    private static void addSecurityToCc(CentralConfigDescriptorImpl cc) {
        cc.setSecurity(new SecurityDescriptor());
        cc.getSecurity().setLdapSettings(Lists.newArrayList(getLdapSettings(cc, 1), getLdapSettings(cc, 2)));
        cc.getSecurity().setAccessClientSettings(new AccessClientSettings());
        cc.getSecurity().getAccessClientSettings().setAdminToken("admintoken");
        cc.getSecurity().getAccessClientSettings().setServerUrl("serverUrl");
        cc.getSecurity().getAccessClientSettings().setConnectionTimeout(1000);
        cc.getSecurity().getAccessClientSettings().setSocketTimeout(10000);
        cc.getSecurity().getAccessClientSettings().setTokenVerifyResultCacheExpirySeconds(10120L);
        cc.getSecurity().getAccessClientSettings().setTokenVerifyResultCacheSize(10100L);
        cc.getSecurity().getAccessClientSettings().setUserTokenMaxExpiresInMinutes(1010101L);
        cc.getSecurity().getAccessClientSettings().setMaxConnections(1010);
        cc.getSecurity().setCrowdSettings(new CrowdSettings());
        cc.getSecurity().getCrowdSettings().setApplicationName("applica");
        cc.getSecurity().getCrowdSettings().setPassword("password");
        cc.getSecurity().getCrowdSettings().setServerUrl("serverurl");
        cc.getSecurity().getCrowdSettings().setCustomCookieTokenKey("customcookie");
        cc.getSecurity().setHttpSsoSettings(new HttpSsoSettings());
        cc.getSecurity().getHttpSsoSettings().setRemoteUserRequestVariable("remoter");
        cc.getSecurity().setLdapGroupSettings(Lists.newArrayList(getLdapGroupSettings(cc, 1), getLdapGroupSettings(cc, 2)));
        cc.getSecurity().setOauthSettings(new OAuthSettings());
        cc.getSecurity().getOauthSettings().setDefaultNpm("defaultNpm");
        cc.getSecurity().getOauthSettings().setOauthProvidersSettings(Lists.newArrayList(getOauthProvider(cc, 1), getOauthProvider(cc, 2)));
        cc.getSecurity().setPasswordSettings(new PasswordSettings());
        cc.getSecurity().getPasswordSettings().setEncryptionPolicy(EncryptionPolicy.REQUIRED);
        cc.getSecurity().getPasswordSettings().setExpirationPolicy(new PasswordExpirationPolicy());
        cc.getSecurity().getPasswordSettings().getExpirationPolicy().setCurrentPasswordValidFor(100);
        cc.getSecurity().getPasswordSettings().setResetPolicy(new PasswordResetPolicy());
        cc.getSecurity().getPasswordSettings().getResetPolicy().setMaxAttemptsPerAddress(1000);
        cc.getSecurity().setSamlSettings(new SamlSettings());
        cc.getSecurity().getSamlSettings().setCertificate("certificate");
        cc.getSecurity().getSamlSettings().setEmailAttribute("emailattrs");
        cc.getSecurity().getSamlSettings().setGroupAttribute("groupAttrs");
        cc.getSecurity().getSamlSettings().setLoginUrl("loginUrl");
        cc.getSecurity().getSamlSettings().setLogoutUrl("logoutUrl");
        cc.getSecurity().getSamlSettings().setServiceProviderName("serviceProvider");
        cc.getSecurity().setSigningKeysSettings(new SigningKeysSettings());
        cc.getSecurity().getSigningKeysSettings().setKeyStorePassword("abcaa");
        cc.getSecurity().getSigningKeysSettings().setPassphrase("aasscaa");
        cc.getSecurity().setSshServerSettings(new SshServerSettings());
        cc.getSecurity().setUserLockPolicy(new UserLockPolicy());
    }

    private static OAuthProviderSettings getOauthProvider(CentralConfigDescriptorImpl cc, int i) {
        OAuthProviderSettings oauth = new OAuthProviderSettings();
        oauth.setApiUrl("apiUrl" + i);
        oauth.setAuthUrl("authUrl" + i);
        oauth.setBasicUrl("baseUrl" + i);
        oauth.setDomain("domain" + i);
        oauth.setId("id" + i);
        oauth.setName("name" + i);
        oauth.setProviderType("pt" + i);
        oauth.setSecret("secret" + i);
        oauth.setTokenUrl("token" + i);
        return oauth;
    }

    private static LdapGroupSetting getLdapGroupSettings(CentralConfigDescriptorImpl cc, int i) {
        LdapGroupSetting lgs = new LdapGroupSetting();
        lgs.setEnabledLdap("enabled" + i);
        lgs.setDescriptionAttribute("desc" + i);
        lgs.setName("name" + i);
        lgs.setFilter("filter" + i);
        lgs.setGroupBaseDn("groupbase" + i);
        lgs.setGroupMemberAttribute("groupMember" + i);
        lgs.setGroupNameAttribute("groupName" + i);
        lgs.setStrategy(LdapGroupPopulatorStrategies.STATIC);

        return lgs;
    }

    private static LdapSetting getLdapSettings(CentralConfigDescriptorImpl cc, int i) {
        LdapSetting ls = new LdapSetting();
        ls.setKey("ldap" + i);
        ls.setLdapUrl("ldapUrl" + i);
        ls.setEmailAttribute("email" + i);
        ls.setLdapPoisoningProtection(true);
        ls.setSearch(new SearchPattern());
        ls.getSearch().setManagerDn("manager" + i);
        ls.getSearch().setManagerPassword("managerpass" + i);
        ls.getSearch().setSearchBase("searchbase" + i);
        ls.getSearch().setSearchFilter("searchfilter" + i);
        ls.setUserDnPattern("userppatt" + i);
        return ls;
    }

    private static RemoteReplicationDescriptor getRemoteReplication(CentralConfigDescriptorImpl cc, int i) {
        RemoteReplicationDescriptor rr = new RemoteReplicationDescriptor();
        rr.setCronExp("0 0/7 * * * ?" + i);
        rr.setRepoKey("repokey" + i);
        rr.setPathPrefix("http://momo.com" + i);
        rr.setReplicationKey("repokey" + i);
        return rr;
    }

    private static RepoLayout getRepoLayout(String key) {
        RepoLayout rl = new RepoLayout();
        rl.setFileIntegrationRevisionRegExp("fileIntg" + key);
        rl.setFolderIntegrationRevisionRegExp("folderintg" + key);
        rl.setDescriptorPathPattern("descPathPattern" + key);
        rl.setName("name" + key);
        rl.setArtifactPathPattern("artifactoryPathPattern" + key);
        return rl;
    }

    private static void setRepo(CentralConfigDescriptor cc, RepoBaseDescriptor repo) {
        repo.setDescription("hello" + repo.getKey());
        repo.setExcludesPattern("excluding" + repo.getKey());
        repo.setIncludesPattern("including" + repo.getKey());
        repo.setNotes("notes" + repo.getKey());
        repo.setRepoLayout(cc.getRepoLayout("layout1"));
        repo.setDockerApiVersion(DockerApiVersion.V2.name());
        repo.setNotes("notes" + repo.getKey());
        repo.setType(RepoType.Generic);
        repo.setRepoLayout(getRepoLayout(repo.getKey()));
    }

    private static DistributionRepoDescriptor getDistributionRepo(CentralConfigDescriptorImpl cc, int i) {
        DistributionRepoDescriptor drd = new DistributionRepoDescriptor();
        drd.setKey("dist-repo-" + i);
        drd.setProxy(cc.getProxy("proxy1"));
        drd.setBintrayApplication(cc.getBintrayApplication("abc"));
        drd.setDefaultLicenses(Sets.newHashSet("default-license1" + i, "default-license2" + i));
        drd.setDefaultVcsUrl("default-vcs-url" + i);
        drd.setGpgPassPhrase("grg-pass-phrase" + i);
        drd.setProductName("product-name" + i);
        drd.setRules(Lists.newArrayList(getDistributionRule(cc, i, 1), getDistributionRule(cc, i, 2)));
        drd.setWhiteListedProperties(Sets.newHashSet("white-list-1" + i, "white-list-2" + i));
        drd.setChecksumPolicyType(LocalRepoChecksumPolicyType.CLIENT);
        drd.setPropertySets(cc.getPropertySets());
        drd.setNotes("notes-1" + i);
        drd.setExcludesPattern("excludes-pattern" + i);
        drd.setDescription("description-1" + i);
        drd.setYumGroupFileNames("yum-group-file-names" + i);
        drd.setRepoLayout(cc.getRepoLayout("layout1"));
        drd.setXrayConfig(new XrayRepoConfig());
        drd.setDownloadRedirectConfig(new DownloadRedirectRepoConfig());
        drd.setOptionalIndexCompressionFormats(Lists.newArrayList("fileFormat" + i));
        return drd;
    }

    private static ReleaseBundlesRepoDescriptor getReleaseBundleRepo(CentralConfigDescriptorImpl cc, int i) {
        ReleaseBundlesRepoDescriptor rbrd = new ReleaseBundlesRepoDescriptor();
        rbrd.setKey("rb-repo-" + i);
        rbrd.setNotes("notes-1" + i);
        rbrd.setExcludesPattern("excludes-pattern" + i);
        rbrd.setDescription("description-1" + i);
        rbrd.setYumGroupFileNames("yum-group-file-names" + i);
        rbrd.setRepoLayout(cc.getRepoLayout("layout1"));
        rbrd.setXrayConfig(new XrayRepoConfig());
        rbrd.setDownloadRedirectConfig(new DownloadRedirectRepoConfig());
        rbrd.setOptionalIndexCompressionFormats(Lists.newArrayList("fileFormat" + i));
        return rbrd;
    }

    private static DistributionRule getDistributionRule(CentralConfigDescriptorImpl cc, int i, int j) {
        return new DistributionRule("name" + i + j, RepoType.NuGet, "filter" + i + j, "pathFilter" + i + j,
                new DistributionCoordinates("repo" + i + j, "pkg" + i + j, "version" + i + j, "path" + i + j));
    }

    private static ReverseProxyDescriptor getReverseProxy(CentralConfigDescriptor cc, int i) {
        ReverseProxyDescriptor rp = new ReverseProxyDescriptor();
        rp.setKey("reverseproxy" + i);
        rp.setArtifactoryAppContext("app-context" + i);
        rp.setArtifactoryServerName("server-name" + i);
        rp.setDockerReverseProxyMethod(ReverseProxyMethod.NOVALUE);
        rp.setPublicAppContext("publicAppDcontext" + i);

        ReverseProxyRepoConfig reverseProxyRepoConfig = new ReverseProxyRepoConfig();
        reverseProxyRepoConfig.setRepoRef(cc.getLocalRepositoriesMap().get("local1"));
        reverseProxyRepoConfig.setServerName("servername" + i);
        reverseProxyRepoConfig.setPort(1010 + i);

        ReverseProxyRepoConfig reverseProxyRepoConfig2 = new ReverseProxyRepoConfig();
        reverseProxyRepoConfig2.setRepoRef(cc.getLocalRepositoriesMap().get("local2"));
        reverseProxyRepoConfig2.setServerName("servername2" + i);
        reverseProxyRepoConfig2.setPort(2020 + i);

        rp.setReverseProxyRepoConfigs(Lists.newArrayList(reverseProxyRepoConfig, reverseProxyRepoConfig2));
        rp.setServerName("servername" + i);
        rp.setServerNameExpression("servernameexpression" + i);
        rp.setSslCertificate("ssl-cert" + i);
        rp.setSslKey("ssl-key" + i);
        rp.setUpStreamName("upstreamname" + i);
        rp.setWebServerType(WebServerType.APACHE);

        return rp;
    }
}
