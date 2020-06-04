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

package org.artifactory.common;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * @author freds
 * Oct 10, 2008
 */
@SuppressWarnings({"EnumeratedConstantNamingConvention"})
public enum ConstantValues {
    test("runMode.test", FALSE), //Use and set only in specific itests - has serious performance implications
    qa("runMode.qa", FALSE),
    dev("runMode.dev", FALSE),
    devHa("runMode.devHa", FALSE),
    artifactoryVersion("version"),
    artifactoryRevision("revision"),
    artifactoryBuildNumber("buildNumber"),
    artifactoryTimestamp("timestamp"),
    contextPath("context.path", "/artifactory"),
    accessContextPath("access.context.path", "/access"),
    supportUrlSessionTracking("servlet.supportUrlSessionTracking", FALSE),
    disabledAddons("addons.disabled", ""),
    addonsInfoUrl("addons.info.url", "http://service.jfrog.org/artifactory/addons/info/%s"),
    addonsConfigureUrl("addons.info.url", "http://www.jfrog.com/confluence/display/RTF/%s"),
    jcrAddonsConfigureUrl("jcr.addons.info.url", "http://www.jfrog.com/confluence/display/JCR/%s"),
    springConfigDir("spring.configDir"),
    asyncCorePoolSize("async.corePoolSize", 4 * Runtime.getRuntime().availableProcessors()),
    asyncPoolTtlSecs("async.poolTtlSecs", 60),
    asyncPoolMaxQueueSize("async.poolMaxQueueSize", 10000),
    versioningQueryIntervalSecs("versioningQueryIntervalSecs", Seconds.HOUR * 2),
    logsViewRefreshRateSecs("logs.viewRefreshRateSecs", 10),
    locksTimeoutSecs("locks.timeoutSecs", 120),
    locksDebugTimeouts("locks.debugTimeouts", FALSE),
    taskCompletionLockTimeoutRetries("task.completionLockTimeoutRetries", 100),
    substituteRepoKeys("repo.key.subst."),
    repoConcurrentDownloadSyncTimeoutSecs("repo.concurrentDownloadSyncTimeoutSecs", Seconds.MINUTE * 15),
    downloadStatsEnabled("repo.downloadStatsEnabled", TRUE),
    saveGetResource("repo.download.saveGetResource", FALSE),
    disableGlobalRepoAccess("repo.global.disabled", TRUE),
    fsItemCacheIdleTimeSecs("fsitem.cache.idleTimeSecs", Seconds.MINUTE * 20),
    dockerTokensCacheIdleTimeSecs("docker.tokens.cache.idleTimeSecs", Seconds.MINUTE * 4),
    genericTokensCacheIdleTimeSecs("artifactory.tokens.cache.idleTimeSecs", Seconds.MINUTE * 10),
    cacheFSSyncquietPeriodSecs("cacheFS.sync.quietPeriodSecs", Seconds.MINUTE * 15),
    searchMaxResults("search.maxResults", 500),
    searchUserQueryLimit("search.userQueryLimit", 1000),
    searchUserMaxListSize("search.max.list.size", 600),
    searchUserSqlQueryLimit("search.userSqlQueryLimit", 2000),
    searchLimitAnonymousUserOnly("search.limitAnonymousUsersOnly", true),
    searchMaxFragments("search.content.maxFragments", 500),
    searchMaxFragmentsSize("search.content.maxFragmentsSize", 5000),
    searchArchiveMinQueryLength("search.archive.minQueryLength", 3),
    searchPatternTimeoutSecs("search.pattern.timeoutSecs", 30),
    gcUseIndex("gc.useIndex", FALSE),
    gcIntervalSecs("gc.intervalSecs", Seconds.DAY),
    gcDelaySecs("gc.delaySecs", Seconds.HOUR * 2),
    gcSleepBetweenNodesMillis("gc.sleepBetweenNodesMillis", 20),
    gcScanStartSleepingThresholdMillis("gc.scanStartSleepingThresholdMillis", 20000),
    gcScanSleepBetweenIterationsMillis("gc.scanSleepBetweenIterationsMillis", 200),
    gcFileScanSleepIterationMillis("gc.fileScanSleepIterationMillis", 1000),
    gcFileScanSleepMillis("gc.fileScanSleepMillis", 250),
    gcMaxCacheEntries("gc.maxCacheEntries", 10000),
    gcReadersMaxTimeSecs("gc.readersMaxTimeSecs", Seconds.HOUR * 3),
    gcFailCountThreshold("gc.failCount.threshold", 10),
    gcBinariesJoinWithNodesQuery("gc.binaries.joinWithNodes", TRUE),
    gcBinariesQuery("gc.binaries.query", null),
    gcNumberOfWorkersThreads("gc.numberOfWorkersThreads", 3),
    gcSkipFullGcBetweenMinorIterations("gc.skipFullGcBetweenMinorIterations", 20),
    trafficCollectionActive("traffic.collectionActive", FALSE),
    trafficLogsDirectory("traffic.logs.directory", null),
    securityAuthenticationCacheInitSize("security.authentication.cache.initSize", 100),
    securityAuthenticationCacheIdleTimeSecs("security.authentication.cache.idleTimeSecs", Seconds.MINUTE * 5),
    securityAuthenticationCacheForTokenEnabled("security.authentication.cache.for.token.enabled", TRUE),
    userLastAccessUpdatesResolutionSecs("security.userLastAccessUpdatesResolutionSecs", 5),
    securityArtifactoryKeyLocation("security.master.key", "security" + File.separator + "artifactory.key"), //Deprecated! users use master.key const val although it really points to artifactory.key
    securityArtifactoryKeyNumOfFallbackKeys("security.master.key.numOfFallbackKeys", 5), //Deprecated! use artifactory.key  // increased to 5 since import mess the last modified - intentionally
    securityDisableRememberMe("security.disableRememberMe", FALSE),
    securityRememberMeLifeTimeSecs("security.rememberMe.lifetimeSecs", TimeUnit.DAYS.toSeconds(14)),
    ldapForceGroupMemberAttFullDN("security.ldap.forceGroupMemberAttFullDN", FALSE),
    ldapDisableGroupSearchAttributesLimitation("security.ldap.disable.group.search.attributes.limitation", FALSE),
    ldapCleanGroupOnFail("security.ldap.group.policy.cleanOnFail", TRUE),
    ldapGroupNamesSearchFilterThreshold("security.ldap.group.search.filterThreshold", 0),
    groupsCacheRetentionSecs("security.ldap.group.cacheRetentionSecs", Seconds.MINUTE * 5),
    ldapMaxPageSize("ldap.max.page.size", 1000),
    ldapPagingSupport("ldap.paging.support", TRUE),
    enableAqlReadCommitted("enable.aql.read.committed", FALSE),
    mvnCentralHostPattern("mvn.central.hostPattern", ".maven.org"),
    mvnCentralIndexerMaxQueryIntervalSecs("mvn.central.indexerMaxQueryIntervalSecs", Seconds.DAY),
    mvnMetadataPluginCalculationWorkers("mvn.metadata.plugin.calculation.workers", 2),
    mvnMetadataCalculationWorkers("mvn.metadata.calculation.workers", 8),
    mvnMetadataVersionsComparator("mvn.metadataVersionsComparatorFqn"),
    mvnMetadataSnapshotComparator("mvn.metadataSnapshotComparatorFqn"),
    mvnDynamicMetadataCacheRetentionSecs("mvn.dynamicMetadata.cacheRetentionSecs", 10),
    mvnMetadataVersion3Enabled("mvn.metadata.version3.enabled", TRUE),
    mvnCustomTypes("mvn.custom.types", "tar.gz,tar.bz2"),
    requestDisableVersionTokens("request.disableVersionTokens", FALSE),
    requestSearchLatestReleaseByDateCreated("request.searchLatestReleaseByDateCreated", FALSE),
    npmTagLatestByPublish("npm.tag.tagLatestByPublish", FALSE),
    buildMaxFoldersToScanForDeletionWarnings("build.maxFoldersToScanForDeletionWarnings", 2),
    missingBuildChecksumCacheIdeTimeSecs("build.checksum.cache.idleTimeSecs", Seconds.MINUTE * 5),
    artifactoryUpdatesRefreshIntervalSecs("updates.refreshIntervalSecs", Seconds.HOUR * 4),
    artifactoryUpdatesUrl("updates.url", "http://service.jfrog.org/artifactory/updates"),
    artifactoryRequestsToGlobalCanRetrieveRemoteArtifacts("artifactoryRequestsToGlobalCanRetrieveRemoteArtifacts", FALSE),
    uiSyntaxColoringMaxTextSizeBytes("ui.syntaxColoringMaxTextSizeBytes", 512000),
    pluginScriptsRefreshIntervalSecs("plugin.scripts.refreshIntervalSecs", 0),
    aolPluginSupport("plugin.aol.support", FALSE),
    aolDedicatedServer("aol.dedicated.server", FALSE),
    aolDisplayAccountManagementLink("aol.displayAccountManagementLink", TRUE),
    aolSecurityHttpSsoEnabled("aol.security.http.sso.enable", FALSE),
    aolSecurityAnonAccessEnabled("aol.security.anon.access.enable", FALSE),
    aolIndexerEnabled("aol.indexer.enable", FALSE),
    uiChroot("ui.chroot"),
    uiSessionTimeoutInMinutes("ui.session.timeout.minutes", 30),
    artifactoryLicenseDir("licenseDir"),
    fileRollerMaxFilesToRetain("file.roller.maxFileToRetain", 10),
    backupFileExportSleepIterationMillis("backup.fileExportSleepIterationMillis", 2000),
    backupFileExportSleepMillis("backup.fileExportSleepMillis", 250),
    s3backupBucket("backup.s3.bucket"),
    s3backupFolder("backup.s3.folder"),
    s3backupAccountId("backup.s3.accountId"),
    s3existsCheckAfterAddingStream("s3.existsCheckAfterAddingStream", true),
    s3backupAccountSecretKey("backup.s3.accountSecretKey"),
    binaryProviderArtifactsExistenceCacheExpirySeconds("binary.provider.artifacts.existence.cache.expirySecs", 60),
    binaryProviderArtifactsExistenceCacheSize("binary.provider.artifacts.existence.cache.size", 20000),
    httpAcceptEncodingGzip("http.acceptEncoding.gzip", true),
    httpUseExpectContinue("http.useExpectContinue", false),
    httpForceForbiddenResponse("http.forceForbiddenResponse", FALSE),
    httpConnectionPoolTimeToLive("http.connectionPool.timeToLive", 30),
    enableCookieManagement("http.enableCookieManagement", false),
    filteringResourceSizeKb("filtering.resourceSizeKb", 64),
    searchForExistingResourceOnRemoteRequest("repo.remote.checkForExistingResourceOnRequest", TRUE),
    versionQueryEnabled("version.query.enabled", true),
    hostId("host.id"),
    responseDisableContentDispositionFilename("response.disableContentDispositionFilename", FALSE),
    composerMetadataExtractorWorkers("composer.metadata.extractor.workers", 20),
    composerMetadataIndexWorkers("composer.metadata.index.workers", 10),
    chefMetadataIndexWorkers("chef.metadata.index.workers", 10),
    yumVirtualMetadataCalculationWorkers("yum.virtual.metadata.calculation.workers", 5),
    rpmMetadataCalculationWorkers("rpm.metadata.calculation.workers", 8),
    rpmMetadataHistoryCyclesToKeep("rpm.metadata.history.cycles.to.keep", 3),
    debianMetadataQueue("debian.metadata.queue", ""),
    debianMetadataCalculationWorkers("debian.metadata.calculation.workers", 8),
    debianMetadataAutoCalculationDisabled("debian.metadata.auto.calculation.disabled", FALSE),
    debianCoordinatesCalculationWorkers("debian.coordinates.calculation.workers", 4),
    debianVirtualMetadataCalculationWorkers("debian.virtual.metadata.calculation.workers", 5),
    debianRemoteETagSupport("debian.remote.etag", TRUE),
    debianMetadataValidation("debian.metadata.validation", true),
    debianMetadataMd5InPackages("debian.metadata.calculateMd5InPackagesFiles", false),
    debianUseAcquireByHash("debian.use.acquire.byhash" , true),
    debianPackagesByHashHistoryCyclesToKeep("debian.packages.byhash.history.cycles.to.Keep", 3),
    globalExcludes("repo.includeExclude.globalExcludes"),
    archiveLicenseFileNames("archive.licenseFile.names", "license,LICENSE,license.txt,LICENSE.txt,LICENSE.TXT"),
    uiSearchMaxRowsPerPage("ui.search.maxRowsPerPage", 20),
    replicationChecksumDeployMinSizeKb("replication.checksumDeploy.minSizeKb", 10),
    replicationConsumerQueueSize("replication.consumer.queueSize", 1),
    replicationLocalIterationSleepThresholdMillis("replication.local.iteration.sleepThresholdMillis", 1000),
    replicationLocalIterationSleepMillis("replication.local.iteration.sleepMillis", 100),
    replicationEventQueueSize("replication.event.queue.size", 50000),
    replicationPropertiesMaxLength("replication.properties.max.length", 100000),
    replicationStatisticsMaxLength("replication.statistics.max.length", 5000),
    replicationInitContextTaskIntervalSecs("replication.initContext.task.intervalSecs", 60),
    replicationInitContextTaskInitialDelaySecs("replication.initContext.task.initialDelaySecs", 5),
    replicationSaveFullTree("replication.push.fullTree.saveLocally", true),
    replicationSaveFullTreeFreeDiskSpaceThresholdInBytes("replication.push.fullTree.saveLocally.free.disk.threshold.bytes", 1000L * 1000L * 100L ),
    replicationSupportPushCaseDifference("replication.push.caseDifference.support", false),
    requestExplodedArchiveExtensions("request.explodedArchiveExtensions", "zip,tar,tar.gz,tgz,7z,tar.bz2"),
    jCenterUrl("bintray.jcenter.url", "http://jcenter.bintray.com"),
    bintrayUrl("bintray.url", "https://bintray.com"),
    bintrayApiUrl("bintray.api.url", "https://api.bintray.com"),
    bintrayOAuthTokenExpirySeconds("bintray.token.expirySecs", 3600),
    bintrayDistributionRegexTimeoutMillis("bintray.distributionRegex.timeoutMillis", 180000),
    bintrayUIHideUploads("bintray.ui.hideUploads", FALSE),
    bintrayUIHideInfo("bintray.ui.hideInfo", FALSE),
    bintrayUIHideRemoteSearch("bintray.ui.hideRemoteSearch", FALSE),
    bintraySystemUser("bintray.system.user"),
    bintraySystemUserApiKey("bintray.system.api.key"),
    bintrayClientThreadPoolSize("bintray.client.threadPool.size", 5),
    enableUiPagesInIframe("enable.ui.pages.in.Iframe", false),
    bintrayClientRequestTimeout("bintray.client.requestTimeoutMS",150000),
    bintrayClientDistributionRequestTimeout("bintray.client.distribution.requestTimeoutMS", 30000),
    bintrayClientSignRequestTimeout("bintray.client.signRequestTimeoutMS", 45000),
    useUserNameAutoCompleteOnLogin("useUserNameAutoCompleteOnLogin", "on"),
    uiHideEncryptedPassword("ui.hideEncryptedPassword", FALSE),
    statsFlushIntervalSecs("stats.flushIntervalSecs", 30),
    statsRemoteFlushIntervalSecs("stats.remote.flushIntervalSecs", 35),
    statsFlushTimeoutSecs("stats.flushTimeoutSecs", 120),
    integrationCleanupIntervalSecs("integrationCleanup.intervalSecs", 300),
    integrationCleanupQuietPeriodSecs("integrationCleanup.quietPeriodSecs", 60),
    folderPruningIntervalSecs("folderPruning.intervalSecs", 300),
    folderPruningQuietPeriodSecs("folderPruning.quietPeriodSecs", 60),
    virtualCleanupMaxAgeHours("repo.virtualCacheCleanup.maxAgeHours", 168),
    virtualCleanupNamePattern("repo.virtualCacheCleanup.pattern", "*.pom"),
    defaultSaltValue("security.authentication.password.salt", "CAFEBABEEBABEFAC"),
    dbIdGeneratorFetchAmount("db.idGenerator.fetch.amount", 2000),
    dbIdGeneratorMaxUpdateRetries("db.idGenerator.max.update.retries", 50),
    gemsLocalIndexTaskIntervalSecs("gems.localIndexTaskIntervalSecs", 30),
    gemsVirtualIndexTaskIntervalSecs("gems.virtualIndexTaskIntervalSecs", 300),
    gemsRemoteAllowCacheDependencies("gems.remoteAllowCacheDependencies", true),
    gemsIndexTaskQueueLimit("gems.gemsIndexTaskQueueLimit", 20000),
    gemsAfterRepoInitHack("gems.gemsAfterRepoInitHack", true),
    gemsNumberOfSplitIterations("gems.number.of.split.iterations", 1),
    jrubyRuntimeLocalContextMode("jruby.runtime.localContextMode", "CONCURRENT"),
    securityCrowdGroupStartIndex("security.authentication.crowd.group.startIndex", 0),
    securityCrowdMaxGroupResults("security.authentication.crowd.group.maxResults", 9999),
    uiHideChecksums("ui.hideChecksums", TRUE),
    archiveIndexerTaskIntervalSecs("archive.indexer.intervalSecs", 60),
    xrayIndexerTaskIntervalSecs("xray.indexer.intervalSecs", 60),
    xrayClientTokenAuthenticationSupport("xray.client.token.authentication.support", FALSE),
    xrayClientTokenExpiryMinutes("xray.client.token.expiry.minutes", 1440),
    xrayClientBlockCacheExpirationIntervalSecs("xray.client.block.cache.expiration.intervalSecs", 300),
    xrayClientBlockUnScannedCacheExpirationIntervalSecs("xray.client.block.unscanned.cache.expiration.intervalSecs",
            120),
    xrayEventJobReadLimit("xray.event.job.read.limit", 10000),
    xrayClientBlockCacheSize("xray.client.block.cache.size", 100000),
    xrayClientHeartbeatIntervalSecs("xray.client.heartbeat.intervalSecs", 5),
    xrayClientRepositoriesPoliciesUpdateIntervalSecs("xray.client.repositories.policies.update.intervalSecs", 20),
    xrayClientRepositoriesPoliciesUpdateQuietPeriodSecs("xray.client.repositories.policies.update.quietPeriodSecs", 5),
    xrayClientMaxConnections("xray.client.max.connections", 50),
    xrayClientConnectionTimeoutMillis("xray.client.connection.timeout.millis", 5000),
    xrayClientBuildsSocketTimeoutMillis("xray.client.builds.socket.timeout.millis", 600000),
    xrayClientNormalSocketTimeoutMillis("xray.client.normal.socket.timeout.millis", 25000),
    xrayCleanupJobQuietPeriodSecs("xray.cleanup.job.quietPeriodSecs", 60),
    xrayCleanupJobDbQueryLimit("xray.cleanup.job.db.query.limit", 100),
    xrayCleanupJobBatchSize("xray.cleanup.job.batch.size", 100),
    xrayCleanupJobSleepIntervalMillis("xray.cleanup.job.sleep.interval.millis", 0),
    xrayBlockUnScannedRecheckIntervalSecs("xray.block.unscanned.recheck.intervalSecs", 20),
    inMemoryNuGetRemoteCaches("nuget.inMemoryRemoteCaches", TRUE),
    nuGetRequireAuthentication("nuget.forceAuthentication", FALSE),
    nuGetAllowRootGetWithAnon("nuget.allowRootGetWithAnon", FALSE),
    nuGetDisableSemVer2SearchFilterForLocalRepos("nuget.disableSemVer2SearchFilterForLocalRepos", FALSE),
    nuGetDisableSemverComparator("nuget.disableSemverComparator", FALSE),
    nuGetIgnoreIsLatestVersionFilter("nuget.ignoreIsLatestVersionFilter", TRUE),
    nuGetXStreamReusageForRemoteMetadataEnabled("nuget.xstream.reusage.for.remote.metadata.enabled", true),
    haHeartbeatIntervalSecs("ha.heartbeat.intervalSecs", 5),
    haHeartbeatStaleIntervalSecs("ha.heartbeat.staleSecs", 30),
    haHeartbeatRecentlyWorkedTriggerDays("ha.heartbeat.recently.worked.trigger.days", 7),
    haPropagationHttpSocketTimeout("ha.propagation.http.socketTimeoutMs", 5000),
    haPropagationHttpConnectionTimeout("ha.propagation.http.connectionTimeoutMs", 5000),
    haPropagationHttpMaxConnectionsPerRoute("ha.propagation.http.maxConnectionsPerRoute", 50),
    haPropagationHttpMaxTotalConnections("ha.propagation.http.maxTotalConnections", 150),
    haPropagationCallTimeoutSecs("ha.propagation.CallTimeoutSecs", 30),
    binaryStoreErrorNotificationsIntervalSecs("binary.store.error.notification.intervalSecs", 30),
    binaryStoreErrorNotificationsStaleIntervalSecs("binary.store.error.notification.staleSecs", 30),
    haMembersIntroductionIntervalSecs("ha.membersIntroduction.intervalSecs", 30),
    haMembersIntroductionStaleIntervalSecs("ha.membersIntroduction.staleSecs", 30),
    npmRemoteMetadataContentValidation("npm.remote.metadata.content.validation", true),
    npmRemoteMetadataJsonAcceptHeader("npm.remote.metadata.json.accept.header", true),
    npmAlternativeDownloadEnabled("npm.alternative.download.enabled", FALSE),
    npmDefaultAuditProvider("npm.default.audit.provider", "https://registry.npmjs.org"),
    npmMinimalXrayAuditSupport("npm.minimal.xray.audit.support", "2.8.0"),
    npmVirtualCacheItemTtlSeconds("npm.virtual.cache.item.ttl.seconds", 600),
    npmVirtualCacheCleanupBatchSize("npm.virtual.cache.cleanup.batch.size", 1000),
    npmSemver4jEnabled("npm.semver4j.enabled", TRUE),
    npmGithubPackagesUrl("npm.github.packages.url", "https://npm.pkg.github.com/"),
    importMaxParallelRepos("import.max.parallelRepos", Runtime.getRuntime().availableProcessors() - 1),
    debianDistributionPath("debian.distribution.path", "dists"),
    opkgIndexQuietPeriodSecs("opkg.index.quietPeriodSecs", 60),
    opkgIndexCycleSecs("opkg.index.cycleSecs", 2),
    debianDefaultArchitectures("debian.default.architectures", "i386,amd64"),
    pypiIndexQuietPeriodSecs("pypi.index.quietPeriodSecs", 60),
    pypiIndexSleepSecs("pypi.index.sleepMilliSecs", 60),
    pypiPreferMetadataFilesOnRootDepth("pypi.index.preferMetadataOnRootDepth", TRUE),
    dockerCleanupMaxAgeMillis("docker.cleanup.maxAgeMillis", Seconds.DAY * 1000),
    dockerCleanupUploadsTmpFolderJobMillis("docker.cleanup.uploadsTmpFolderJobMillis", Seconds.DAY * 1000),
    dockerCleanupTmpFolderMaxSearchResult("docker.cleanup.tmpFolder.maxSearchResult", 5000),
    dockerTagsCleanupIntervalSecs("docker.tags.cleanup.intervalSecs", 300),
    dockerTagsCleanupQuietPeriodSecs("docker.tags.cleanup.quietPeriodSecs", 60),
    dockerManifestMarkerLockTimeoutMillis("docker.manifest.marker.lock.timeoutMillis", 30000),
    httpRangeSupport("http.range.support", true),
    aclDirtyReadsTimeout("acl.dirty.read.timeout", 20000),
    aclVersionCacheAsyncReload("acl.version.cache.async.reload", FALSE),
    aclVersionCacheAsyncWaitingTimeMillis("acl.version.cache.async.waiting.time.millis", 60000),
    aclVersionCacheAsyncWaitOnErrorTimeMillis("acl.version.cache.async.waiting.on.error.time.millis", 60000),
    aclMinimumWaitRefreshDataSeconds("acl.minimum.wait.refresh.data.seconds", 30),
    centralConfigDirtyReadsTimeoutMillis("central.config.dirty.read.timeout.millis", 2000),
    centralConfigLatestRevisionsExpireAfterAccessSeconds("central.config.latest.revisions.expire.after.access.seconds", Seconds.HOUR * 6),
    centralConfigLatestRevisionsDictionarySize("central.config.latest.revisions.dictionary.size", 20),
    centralConfigSaveNumberOfRetries("central.config.save.number.of.retries", 5),
    centralConfigSaveBackoffMaxDelay("central.config.save.backoff.max.delay", 8000),
    centralConfigSaveBackoffMultiplier("central.config.save.backoff.multiplier", 2),
    repositoriesDirtyReadsTimeoutMillis("repositories.dirty.read.timeout.millis", 5000),
    allowUnauthenticatedPing("ping.allowUnauthenticated", FALSE), // in milliseconds
    idleConnectionMonitorInterval("repo.http.idleConnectionMonitorInterval", 10),
    disableIdleConnectionMonitoring("repo.http.disableIdleConnectionMonitoring", FALSE),
    contentCollectionAwaitTimeout("support.core.bundle.contentCollectionAwaitTimeout", 60),
    waitForSlotBeforeWithdraw("support.core.bundle.waitForSlotBeforeWithdraw", 600),
    maxBundles("support.core.bundle.maxBundles", 5),
    testCallHomeCron("post.jobs.callHome.cron", null),
    callHomeOverrideUrl("usage.job.url.override", null),
    binaryProviderZones("binary.provider.zones","a,b,c"),
    binaryProviderPruneChunkSize("binary.provider.prune.chunk.size", 500),
    propertiesSearchChunkSize("properties.search.chunk.size", 500),
    useFrontCacheForBlockedUsers("security.useFrontCacheForBlockedUsers", true),
    loginBlockDelay("security.loginBlockDelay", 500),
    loginMaxBlockDelay("security.maxLoginBlockDelay", 5000),
    maxIncorrectLoginAttempts("security.max.incorrect.login.attempts", 2),
    maxUsersToTrack("security.max.users.toTrack", 10000),
    passwordExpireNotificationJobIntervalSecs("security.password.expiry.passwordExpireNotificationJobIntervalSecs", 86400),
    passwordExpireJobIntervalSecs("security.password.expiry.passwordExpireJobIntervalSecs", 43200),
    passwordDaysToNotifyBeforeExpiry("security.password.expiry.daysToNotifyBefore", 5),
    httpClientMaxTotalConnections("http.client.max.total.connections", 50),
    httpClientMaxConnectionsPerRoute("http.client.max.connections.per.route", 50),
    HttpClientMaxDockerTokenConnections("http.client.max.docker.token.connections", 2),
    hazelcastMaxLockLeaseTime("hazelcast.max.lock.lease.time.minutes", 30),
    blockedMismatchingMimeTypes("repo.remote.blockedMismatchingMimeTypes","text/html,application/xhtml+xml"),
    mvnMetadataCalculationSkipDeleteEvent("mvn.metadata.calculation.skip.delete.event", false),
    remoteBrowsingContentLengthLimitKB("repo.remote.browsing.content.length.limit.KB", 1024),
    syncPropertiesBlacklistUrls("repo.remote.syncProperties.blacklistUrls" , ""),
    whitelistRemoteRepoUrls("remote.repo.url.whitelist.prefix", null),
    remoteRepoBlockUrlStrictPolicy("remote.repo.url.strict.policy", false),
    remoteRepoResearchInterceptorRepoKeys("remote.repo.research.interceptor.repoKeys", null),
    hazelcastManagement("hazelcast.management",false),
    hazelcastManagementUrl("hazelcast.management.url", null),
    hazelcastMapMaxBackupCount("hazelcast.map.max.backup.count", 1),
    sumoLogicApiUrl("sumologic.api.url", "https://auth.sumologic.com"),
    moveCopyMaxFoldersCacheSize("move.copy.max.folder.cache.size", 1000000),
    moveCopyDefaultTransactionSize("move.copy.default.transaction.size", 1000),
    nodePropertiesReplaceAll("node.properties.replace.all", false),
    nodePropertiesLogPerformance("node.properties.log.performance", false),
    workQueueSyncExecutionTimeoutMillis("workQueue.execution.syncExecutionTimeoutMillis", 120000),
    workQueueJobEnabled("workQueue.dojob.enabled", true),
    workQueueDoJobIntervalSecs("workQueue.dojob.intervalSecs", 5),
    workItemMaxLockLeaseTime("workitem.max.lock.lease.time.minutes", 30),
    haMessagesWorkers("ha.messages.workers", 10),
    securityCommunicationConstant("security.communication.constant", "ArtifactorySecurityCommunicationConstant"),
    disablePermissionCheckOnNuGetSearch("nuget.disablePermissionCheck",FALSE),
    orderTreeBrowserRepositoriesByType("treebrowser.sortRepositories.sortByType","virtual,distribution,local,remote,cached"),
    treebrowserFolderCompact("treebrowser.folder.compact",true),
    publishMavenMetadataModelVersion("maven.metadata.publishModelVersion", true),
    accessTokenExpiresInDefault("access.token.expiresIn.default", TimeUnit.HOURS.toSeconds(1)),
    accessTokenNonAdminMaxExpiresIn("access.token.non.admin.max.expires.in", TimeUnit.HOURS.toSeconds(1)),
    accessClientTokenVerifyResultCacheSize("access.client.token.verify.result.cache.size", -1), //-1: use client default
    accessClientTokenVerifyResultCacheExpiry("access.client.token.verify.result.cache.expiry", -1), //-1: use client default
    accessClientServerUrlOverride("access.client.serverUrl.override", null), //if has value - overrides any other config
    accessClientWaitForServer("access.client.waitForServer", 90), //time in seconds to wait for access server when Artifactory starts
    accessClientMaxConnections("access.client.max.connections", 50),
    accessClientConnectionTimeout("access.client.connection.timeout", -1),
    accessClientSocketTimeout("access.client.socket.timeout", -1),
    accessClientIgnoreServerVersionAssertion("access.client.ignore.server.version.assertion", false),
    accessClientForceRest("access.client.force.rest", false),
    accessClientForceGrpc("access.client.force.grpc", false),
    accessServerBundled("access.server.bundled", null), //If has value - dictates the state (default otherwise: false)
    metadataClientMaxConnections("metadata.client.max.connections", 50),
    metadataClientConnectionTimeout("metadata.client.connection.timeout", 10_000),
    metadataClientSocketTimeout("metadata.client.socket.timeout", TimeUnit.MINUTES.toMillis(5)),
    metadataClientServerUrlOverride("metadata.client.serverUrl.override", "http://localhost:8086"),
    metadataEventOperatorThreads("metadata.event.operator.threads", 5),
    metadataEventOperatorDispatcherIntervalSecs("metadata.event.operator.dispatcher.interval.secs",
            TimeUnit.MINUTES.toSeconds(1)),
    metadataEventUnstableMarginSecs("metadata.event.unstable.margin.secs", 60),
    metadataServerEventsEnabled("metadata.native.ui", FALSE),
    metadataServerEventsPersistence("metadata.server.events.persistence", "distributed"),
    activePrincipalTokenTtl("security.active.principal.token.ttl", 600),
    activePrincipalTokenCacheMaxSize("security.active.principal.token.ttl", 10_000),
    puppetMetadataCalculationWorkers("puppet.metadata.calculation.workers", 5),
    puppetRepoMetadataCalculationWorkers("puppet.repo.metadata.calculation.workers", 5),
    puppetReindexPeriodInSeconds("puppet.reindex.period", TimeUnit.MINUTES.toSeconds(30)),
    puppetAdditionalModuleGroups("puppet.additional.modulegroup", ""),
    puppetAdditionalEndorsements("puppet.additional.endorsement", ""),
    skipOnboardingWizard("onboarding.skipWizard", FALSE),
    configurationManagerRetryAmount("configuration.manager.retry.amount", 3),
    watchAggregationTimeWindowSecs("aggregation.time.window.secs", 60),
    mostDownloadedCacheIdleTimeSecs("most.downloaded.cache.idleTimeSecs", TimeUnit.MINUTES.toSeconds(15)),
    replicationReconnectionMaxDelay("replication.eventBased.connection.maxDelay", 1800000),
    eventBasedReplicationWorkers("replications.eventbased.workers", 8),
    maxEventReplicationQueueItems("replication.eventbased.maxQueueItems", 500),
    eventBasedMaxPullReplicationPerRepo("replication.eventbased.maxPullReplicationsPerRepo", 30),
    eventBasedPullDispatcherIntervals("replication.eventbased.pullDispatcher.timeMillis", 1511),
    buildRetentionWorkers("build.retention.workers", 10),
    buildRetentionAlwaysAsync("build.retention.always.async", false),
    buildInfoMigrationJobEnabled("build.info.migration.job.enabled", TRUE),
    buildInfoMigrationJobQueueWorkers("build.info.migration.job.queue.workers", 2),
    buildInfoMigrationFixProperties("build.info.migration.fix.properties", false),
    buildUiSkipDeletePermissionCheck("build.ui.skip.delete.permission.check", false),
    lockingProviderType("locking.provider.type", "db"),
    mapProviderType("map.provider.type", null),
    dbLockCleanupJobIntervalSec("db.lock.cleanup.job.interval", 10),
    dbLockCleanupJobStaleIntervalSec("db.lock.cleanup.job.stale.interval", 10),
    dbMsSqlPropertyValueMaxSize("db.mssql.property.value.max.size", 900),
    dbPostgresPropertyValueMaxSize("db.postgres.property.value.max.size", 2400),
    migrationJobWaitForClusterSleepIntervalMillis("migration.job.waitForCluster.sleepIntervalMillis", TimeUnit.MINUTES.toMillis(5)),
    migrationJobDbQueryLimit("migration.job.dbQueryLimit", 100),
    migrationJobBatchSize("migration.job.batchSize", 10),
    migrationJobSleepIntervalMillis("migration.job.sleepIntervalMillis", TimeUnit.SECONDS.toMillis(5)),
    sha2MigrationJobQueueWorkers("sha2.migration.job.queue.workers", 2),
    sha2MigrationJobEnabled("sha2.migration.job.enabled", FALSE),
    sha2MigrationJobForceRunOnNodeId("sha2.migration.job.forceRunOnNodeId", null),
    pathChecksumMigrationJobEnabled("pathChecksum.migration.job.enabled", FALSE),
    pathChecksumMigrationJobQueueWorkers("pathChecksum.migration.job.queue.workers", 2),
    pathChecksumMigrationJobForceRunOnNodeId("pathChecksum.migration.job.forceRunOnNodeId", null),
    allowAnyUpgrade("upgrade.allowAnyUpgrade.forVersion", null),
    failUploadOnChecksumValidationError("upload.failOnChecksumValidationError", FALSE),
    remoteDownloadInVainConsumeLimitInMegaBytes("remote.download.inVain.consume.limit.inMegaBytes", 1),
    artifatoryServiceName("service.name", "https://localhost:8080/artifactory/webapp/"),
    helmMetadataCalculationWorkers("helm.metadata.calculation.workers", 5),
    helmVirtualMetadataCalculationWorkers("helm.virtual.metadata.calculation.workers", 5),
    cranMetadataCalculationWorkers("cran.metadata.calculation.workers", 5),
    conanMetadataCalculationWorkers("conan.metadata.calculation.workers", 5),
    conanTimestampOverrideTimeMillis("conan.index.timestamp.override.threshold.millis", TimeUnit.MINUTES.toMillis(1)),
    conanV2MigrationJobEnabled("conan.v2.migration.job.enabled", TRUE),
    conanV2MigrationJobQueueWorkers("conan.v2.migration.job.queue.workers", 2),
    nodesDaoSqlGetNodeByPath("sql.nodesDao.getNodeByPath", null),
    nodesDaoSqlGetNodeIdByPath("sql.nodesDao.getNodeIdByPath", null),
    nodesDaoSqlNodeExists("sql.nodesDao.exists", null),
    nodesDaoSqlSearchFilesByProperty("sql.nodesDao.searchFilesByProperty", null),
    nodesDaoSqlGetItemType("sql.nodesDao.getNodeType", null),
    nodesDaoSqlNodeHasChildren("sql.nodesDao.hasChildren", null),
    nodesDaoSqlNodeGetChildren("sql.nodesDao.getChildren", null),
    allowExternalConversionScripts("sql.converter.allowExternalConversionScripts", false),
    masterKeyWaitingTimeout("master.key.waiting.timeout.millis", 60000),
    joinKeyWaitingTimeout("join.key.waiting.timeout.millis", 60000),
    bootstrapLoggerDebug("bootstrap.logger.debug", false),
    enableContextAwareLoggerEnabled("enable.context.aware.logger", false),
    sendOverwritesToTrashcan("send.overwrites.to.trashcan", true),
    trashcanMaxSearchResults("trashcan.max.search.results", 10000),
    enableReplicatorUse("enable.replicator.use", true),
    startLocalReplicator("start.local.replicator", false),
    minReplicatorUseFileSizeInBytes("min.replicator.use.filesize.in.bytes", 8_000_000),
    blockOnConversionCacheTimeoutInMillis("block.on.conversion.cache.timeout.in.millis", 0L),
    /**
     * whether to enable recording of tree node events
     */
    nodeEventsEnabled("node.events.enabled", TRUE),
    /**
     * whether to enable recording of tree node event metrics
     */
    nodeEventsMetricsEnabled("node.events.metrics.enabled", TRUE),
    /**
     * max time in millis to keep node events records
     */
    nodeEventsMaxTime("node.events.max.time", TimeUnit.DAYS.toMillis(365)),
    /**
     * Jobs
     */
    jobsTableTimeToLiveMillis("jobs.table.ttl.millis", TimeUnit.DAYS.toMillis(7)),
    jobsTableCleanupCron("jobs.table.cleanup.cron", null),
    jobsTableFetchLimit("jobs.table.fetch.limit", 10000L),
    /**
     * using node.events for incremental replication
     */
    replicationFullUseEventLog("replication.full.useEventLog", FALSE),
    replicationEventLogCommitDurationMillis("replication.full.eventlog.commit.durationMillis", 5000L),
    replicationFullEventLogForceSuccessfulFullTree("replication.full.eventlog.forceSuccessfulFullTree", FALSE),
    replicationPrimaryOnly("replication.primaryOnly", TRUE),
    replicationErrorQueueMaxErrors("replication.errorQueue.maxErrors", 100),
    replicationErrorQueueEnabled("replication.errorQueue.enabled", FALSE),
    replicationErrorQueueRetryCount("replication.errorQueue.retryCount", 10),
    releaseBundleCleanupIntervalSecs("releasebundle.cleanup.intervalSecs", Seconds.HOUR),
    releaseBundleDistributeStreamingIntervalSecs("releasebundle.distribute.streaming.intervalSecs", 15),
    artifactoryLocalPortForReplicator("local.port.for.replicator", 8081),
    packageNativeUiResults("package.native.ui.results", 1000),
    packageNativeUiIncludeTotalDownload("package.native.ui.include.total.downloads", true),
    packageNativeUiSearchByPath("package.native.ui.search.by.path", false),
    httpSsoCreateSessionUiRequestOnly("http.sso.create.session.ui.request.only", true),
    skipAutoBucketCreation("s3.autoBucketCreation.skip", false),
    globalMessageCacheAgeSecs("global.message.cache.ageSecs", 10),
    applyLocalReposPermissionsOnRemoteRepos("apply.local.repos.permissions.on.remote.repos", false),
    applyRemoteReposPermissionsOnLocalRepos("apply.remote.repos.permissions.on.local.repos", false),
    uiCustomFooterMessageFile("ui.custom.footer.message.file", null),
    uiContinuePagingLimit("ui.continue.paging.limit", 500L),
    csrfProtectionHeaderValue("csrf.protection.header.value", null),
    condaMetadataCalculationWorkers("conda.metadata.calculation.workers", 5),
    npmMetadataCalculationWorkers("npm.metadata.calculation.workers", 10),
    npmReindexMetadataCalculationWorkers("npm.reindex.metadata.calculation.workers", 10),
    goUseRawGithubContent("go.github.useRaw", false),
    goAllowPathFallback("go.pathFallback.allow", false),
    goSumDbUrlOverride("go.sumdb.url.override", null),
    goSumDbEnabled("go.sumdb.enabled", true),
    goSkipGithubGetImportPathRequest("go.skip.github.get.import.path.request", true),
    dbSessionCleanupCron("db.session.cleanup.cron", null),
    nativeUiSearchMaxTimeMillis("native.ui.search.max.time.millis", 60_000),
    accessAdminDefaultPasswordCheckPeriod("access.admin.default.password.check.period.millis", 60_000),
    signedUrlForCdnValidForSeconds("signed.url.cdn.validForSecs", 30L),
    signedUrlValidForSeconds("signed.url.validForSecs", TimeUnit.DAYS.toSeconds(1)),
    shouldRemoveNativePropKeys("should.include.native.prop.keys", true),
    replicationSkipCheckLocalGenerated("replication.skip.check.localgenerated", false),
    dockerVirtualManifestSyncerUseHeuristic("docker.virtual.manifest.syncer.use.heuristic", false),
    dockerCatalogsTagsFallbackFetchFromRemoteCache("docker.catalogs.tags.fallback.fetch.remote.cache", false),
    sqlServerQueryBuilderForceAddOffsetToQuery("sql.server.query.builder.force.add.offset.to.query", false),
    supportBundlesRetentionCount("support.bundles.retention.count", 15),
    maxArtifactsPermissionTestOnChecksumDeploy("max.artifacts.permission.test.on.checksum.deploy", 10),
    ignoreChecksumDeployPermissionCheck("ignore.checksum.deploy.permission.check" , false),
    listOfReposAllowedSendRedirectUrl("list.of.repos.allowed.send.redirect.url", ""),
    cloudBinaryProviderRedirectThresholdInBytes("cloud.binary.provider.redirect.threshold.in.bytes", 200 * 1024),
    isAddUserNameToSignUrlForRedirect("is.add.user.name.to.sign.url.for.redirect" , false),
    testingPort("testing.port" , -1),
    cipherBlackList("cipher.black.list" , "arcfour256"),
    maxReleaseBundleSizeToDisplay("max.release.bundle.size.to.display" , 3000),
    eventsLogCleanupTaskPeriodHours("events.log.cleanup.task.period.hours", 13),
    ageOfEventsLogEntriesToDiscardDays("events.log.cleanup.age.of.entries.to.discard.days", 60),
    replicationEventsFetchBatchLimit("replication.events.fetch.batch.limit", 1000),
    eventsCleanupIntervalHours("events.log.cleanup.interval.hours", 4),
    eventsCleanupIterationSleepTimeMillis("events.log.cleanup.iteration.sleep.time.millis", 20),
    shutDownOnInvalidDBScheme("shutdown.on.invalid.db.scheme", false),
    dockerBlobsCachesEnabled("docker.blobs.caches.enabled", true),
    dockerBlobsCacheSizeInBytes("docker.blobs.cache.size.in.bytes", null),
    dockerBlobsCacheStatsPrintIntervalMillis("docker.blobs.cache.stat.print.interval.millis",
            TimeUnit.MINUTES.toMillis(5)),
    allowInsecureTemplates("allow.insecure.templates", false),
    calculateStorageSummaryJobCron("update.storage.summary.cron", null),
    systemImportEnabled("system.import.enabled", true),
    repositoryImportEnabled("repository.import.enabled", true),
    allowExportImportInsecurePaths("allow.export.import.insecure.paths", false),
    ;


    public static final String SYS_PROP_PREFIX = "artifactory.";

    private final String propertyName;
    private final String defValue;

    ConstantValues(String propertyName) {
        this(propertyName, null);
    }

    ConstantValues(String propertyName, Object defValue) {
        this.propertyName = SYS_PROP_PREFIX + propertyName;
        this.defValue = defValue == null ? null : defValue.toString();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getDefValue() {
        return defValue;
    }

    public String getString() {
        return ArtifactoryHome.get().getArtifactoryProperties().getProperty(this);
    }

    public int getInt() {
        return (int) getLong();
    }

    public long getLong() {
        return ArtifactoryHome.get().getArtifactoryProperties().getLongProperty(this);
    }

    public boolean getBoolean() {
        return ArtifactoryHome.get().getArtifactoryProperties().getBooleanProperty(this);
    }

    public boolean isSet() {
        return ArtifactoryHome.get().getArtifactoryProperties().hasProperty(this);
    }

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * The following are used for when we need to get values but home is not bound (i.e. encryption operations in     *
     * post context ready state)                                                                                      *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    public String getString(ArtifactoryHome home) {
        return home.getArtifactoryProperties().getProperty(this);
    }

    public int getInt(ArtifactoryHome home) {
        return (int) getLong(home);
    }

    public long getLong(ArtifactoryHome home) {
        return home.getArtifactoryProperties().getLongProperty(this);
    }

    public boolean getBoolean(ArtifactoryHome home) {
        return home.getArtifactoryProperties().getBooleanProperty(this);
    }

    public boolean isSet(ArtifactoryHome home) {
        return home.getArtifactoryProperties().hasProperty(this);
    }

    public static boolean isDevOrTest(ArtifactoryHome artifactoryHome) {
        return ConstantValues.test.getBoolean(artifactoryHome) ||
                ConstantValues.dev.getBoolean(artifactoryHome) ||
                ConstantValues.devHa.getBoolean(artifactoryHome);
    }

    private static class Seconds {
        private static final int MINUTE = 60;
        private static final int HOUR = MINUTE * 60;
        private static final int DAY = HOUR * 24;
    }
}
