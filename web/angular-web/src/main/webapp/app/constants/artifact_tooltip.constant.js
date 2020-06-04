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
export default {
    admin: {
        advanced: {
            maintenance: {
                garbageCronExpression: `The Cron expression that determines the frequency of garbage collection.
For detailed information, see <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html" target="_blank">The CronTrigger Tutorial</a>.`,
                cleanupCronExpression: `The Cron expression that determines the frequency of artifacts cleanup.
For detailed information, see <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html" target="_blank">The CronTrigger Tutorial</a>.`,
                runNow: `Remove unreferenced binaries from the underlying datastore.
Artifactory periodically runs garbage collection to remove unused (deleted) binaries from the datastore.
You may also run datastore cleanup manually using this button.`,
                enableQuotaControl: `Enable control over the amount of storage space used for binaries to avoid running out of disk space.`,
                storageSpaceLimit: `The maximum percentage of disk capacity that the partition containing the binaries folder is allowed to use.
Once this limit has been reached, deployment is rejected with a 413 error (request entity
too large) and an error message is displayed in the UI (visible to admin users only).
When using filesystem storage, the partition checked is the one containing the
'$ARTIFACTORY_HOME/data/filestore' directory.
When using database BLOB storage, the partition checked is the one containing the
'$ARTIFACTORY_HOME/data/cache' directory.`,
                storageSpaceWarning: `The percentage of disk space usage, by the partition containing the binaries folder, that will trigger a warning.
Once this limit is reached a warning is logged and a warning message is displayed in the UI
(visible to admin users only).`,
                cronExpressionCleanup: `The Cron expression that determines the frequency at which unused artifacts are cleaned up. For detailed information, see <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html" target="_blank">The CronTrigger Tutorial</a>.`,
                runUnusedCachedArtifactsCleanup: `Remove unused artifacts from all remote repository caches
according to the 'Keep Unused Artifacts(Hours)' setting for each remote repository.
Artifactory periodically cleans up unused artifacts from all remote repository caches.
You can also run the cleanup manually using this button.`,
                cleanVirtualRepositoriesNow: `Clean up internal data used by virtual repositories.
Cached POM files older than the number of hours defined in the 'virtualCacheCleanup.maxAgeHours' system property will be deleted.
The default is 168 hours (one week). Artifacts accessed through virtual repositories will not be affected by this.`,
                compressTheInternetDatabase: `When using the internal Derby database, use this to clean up fragmented space that may remain
after delete operations.
NOTE! It is recommended to run this when Artifactory activity is low because compression may not run its full course when
storage is busy (although this has no detrimental effect on the storage).`,
                pruneUnreferencedData: `Running Artifactory with the wrong file system permissions on storage folders, or running out of storage space,
can result in unreferenced binary files and empty folders present in the filestore or cache folders. This action
removes unreferenced files and empty folders.`

            },
            storageSummary: {
                itemsCount: `The total number of items (both files and folders) in your system.`,
                optimization: `The ratio of Binaries Size to Artifacts Size.
This reflects how much the usage of storage in your system has been reduced by Artifactory using checksum storage.`,
                artifactsCount: `The total number of artifacts pointing to the physical binaries stored on your system.`,
                storageDirectory: `If Storage Type is "filesystem" then this is the path to the physical file store.
If Storage Type is "fullDb" then this is the path to the directory that caches binaries when they are extracted from the database.
If Storage Type is "S3" then this is the path to the directory that caches binaries from S3.

If Storage Type is "Advanced Configuration" then these are the paths of the corresponding binary providers, supplied by the advanced configuration.`,
                centralConfigurationDescriptor: ``,
                securityConfigurationDescriptor: ``,
                fileSystemStorage: `The storage percentage represents the used/available space on the mount point where the <a href="https://www.jfrog.com/confluence/display/RTF/Monitoring+Storage#MonitoringStorage-FileStore" target="_blank">Artifactory filestore data</a> is stored.`
            }
        },
        configuration: {
            general: {
                serverName: `A name that uniquely identifies this artifactory server instance across the network.`,
                customURLBase: `A hard-coded URL prefix used to calculate relative URLs.`,
                fileUploadMaxSize: `The maximum size (in MB) allowed for artifacts uploaded through the web UI.
Set to '0' for unlimited size.`,
                bintrayMaxFilesUpload: `The maximum number of files that can be uploaded to Bintray in a single operation.`,
                dateFormat: `The format used to display dates.
For a detailed explanation see: <a href="http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html" target="_blank">Joda DateTimeFormat</a>`,
                globalOfflineMode: `If set, Artifactory does not try to access remote resources to fetch artifacts.
Only cached and local artifacts are served`,
                showAvailableAddonsInfo: `When set, Artifactory displays information about available Add-ons. This overrides any user-specific setting to hide information.`,
                folderDownloadMaxFiles: `The maximum amount of artifacts that can be downloaded under one folder.`,
                folderDownloadMaxSize: `The maximum size (in MB) of a folder that is allowed to be downloaded.`,
                folderDownloadMaxParallel: `The maximum amount of folder download requests Artifactory will allow to run together.`,
                retentionPeriodDays: `The maximum number of days to keep artifacts in the trashcan.`,
                allowPermDeletes: `When deleting, users will be given an option to bypass the trash can and delete artifacts permanently.`,
                blockReplications: `When set, replication will not be triggered regardless of configuration.`,
                blockPushReplications: ` When set, push replication will not be triggered regardless of configuration.`,
                blockPullReplications: `When set, pull replication will not be triggered regardless of configuration.`,
	            releaseBundlesCleanup: `The number of hours to wait before a release bundle is deemed “incomplete” and eligible for cleanup from the temporary folder. A value of 0 means automatic cleanup is disabled. 
Note that only release bundles that were partially distributed are eligible for cleanup.`,
                signedUrlMaxValidForSecs: `The maximum number of seconds a signed URL can be valid.`,
                downloadRedirectFileMinimumSize: `The minimal size of an artifact for which direct download from the cloud is enabled.
Requests for artifacts smaller than this size are served through Artifactory as usual.`
            },
            licenseForm: {
                licenseKey: `A unique short name identifying the license.`,
                longName: `A descriptive name for the license.`,
                URLs: `A URL (or URLs separated by semicolon) pointing to the license's homepage.`,
                regExp: `A regular expression used to match licenses of this type against license details in artifact module information.
For regular expression syntax reference please refer to the Pattern javadoc.`

            },
            propertySetsForm: {},
            proxyForm: {
                systemDefault: `Make this proxy the default for new remote repositories and for internal HTTP requests.`,
                redirectingProxyTargetHosts: `An optional list of host names to which the proxy may redirect requests.
The credentials of the proxy are reused by requests redirected to any of these hosts.`

            },
            reverseProxy: {
                serverName: `The server name that will be used to access Artifactory.
Should be correlated with the base URL value.`,
                publicAppContext: `The path which will be publicly used to access Artifactory. If Artifactory is accessible on the root of the server leave empty.`,
                artifactoryServerName: `The internal server name for Artifactory which will be used by the web server to access the Artifactory machine.
If the web server is installed on the same machine as Artifactory you can use localhost, otherwise use the IP or hostname.`,
                artifactoryAppContext: `The path which will be used to access the Artifactory application. If Artifactory is accessible on the root of the server leave empty.`,
                sslCertificate: `The full path of the certificate file on the web server.`,
                sslKey: `The full path of the key file on the web server.`
            },
            mail: {
                enable: `The activity state of the configuration.`,
                from: `The "from" address header to use in all outgoing messages (optional). `,
                subjectPrefix: `A prefix to use for the subject of all outgoing messages.`,
                artifactoryURL: `The Artifactory URL to to link to in all outgoing messages (optional).`
            },
            bintray: {
                bintrayUsername: `The default Bintray user name that will be used by Artifactory in cases where an Artifactory
user doesn't have Bintray credentials defined.`,
                bintrayAPIKey: `The default Bintray API Key that will be used by Artifactory in cases where an Artifactory
user doesn't have Bintray credentials defined.`
            },
            registerPro: {
                licenseKey: `The license key is required for using Artifactory Add-ons.`
            },
            xray: {
                allowDownloadsBlocked: `Allows download of all artifacts, even those that<br>have been blocked for download by Xray.`,
                allowWhenXrayUnavilable: `Overrides Artifactory’s default behavior of blocking artifact<br>download when Xray becomes unavailable.`,
                bypassDefaultProxy: `Bypass the default system proxy configuration.`,
                overrideDefaultProxy: `Choose a Proxy configuration to override the default system proxy.`,
                blockUnscannedTimeoutSeconds: `When a repository is configured to block downloads of unscanned artifacts, this setting will 
make every download request connection to remain open for the time configured (in seconds), allowing Xray sufficient time to scan the artifact and then return the artifact or block it based on scan results.`
            }
        },
        import_export: {
            repositories: {
                createM2CompatibleExport: `Include Maven 2 repository metadata and checksum files as part of the export`,
                outputVerboseLog: `Lowers the log level to debug and redirects the output from the standard log to the import-export log.
You can monitor the log in the <a href="./#/admin/advanced/system_logs">'System Logs'</a> page.`,
                targetLocalRepository: `Specifies the repository in which to place imported content.
When importing to a single repository, the file structure within the folder you import from should be similar to:
SELECTED_DIR
|
|--LIB_DIR_1
But when importing to all repositories, the file structure within the folder you import from should be similar to:
SELECTED_DIR
|
|--REPOSITORY_NAME_DIR_1
| |
| |--LIB_DIR_1

When importing to all repositories, make sure the names of the directories representing
the repositories in the archive, match the names of the target repositories in Artifactory.`,
                repositoryZipFile: `The archive file from which to import content.
When importing to a single repository, the file structure within the archive should be similar to:
ARCHIVE.ZIP
|
|--LIB_DIR_1
When importing to all repositories, the file structure within the archive you import from should be similar to:
ARCHIVE.ZIP
|
|--REPOSITORY_NAME_DIR_1
| |
| |--LIB_DIR_1
When importing to all repositories, make sure the names of the directories representing
the repositories in the archive, match the names of the target repositories in Artifactory.
NOTE! Uploading the archive, does not import its content.
To import, select the Target Local Repository, upload the archive and click Import.`
            },
            system: {
                excludeBuilds: `Exclude all builds from the export.`,
                createM2CompatibleExport: `Include Maven 2 repository metadata and checksum files as part of the export.`,
                outputVerboseLog: `Lowers the log level to debug and redirects the output from the standard log to the import-export log.
You can monitor the log in the <a href="./#/admin/advanced/system_logs">'System Logs'</a> page.`

            },
            stash: {
                createM2CompatibleExport: `Include Maven 2 repository metadata and checksum files as part of the export.`,
                outputVerboseLog: `Lowers the log level to debug and redirects the output from the standard log to the import-export log.
You can monitor the log in the <a href="./#/admin/advanced/system_logs">'System Logs'</a> page.`,
                createArchive: `Export the results as a zip archive.`
            }
        },
        repositories: {
            customURLBase: `A hard-coded URL prefix used to calculate relative URLs.`,
            localForm: {
                publicDescription: `Textual description of the repository. This description is displayed when the repository is selected in the Tree Browser.`,
                internalDescription: `Additional notes that are only displayed in this form.`,
                includesPattern: `List of artifact patterns to include when evaluating artifact requests in the form of x/y/**/z/*. When used, only artifacts matching one of the include patterns are served. By default, all artifacts are included (**/*).`,
                excludedPattern: `List of artifact patterns to exclude when evaluating artifact requests, in the form of x/y/**/z/*. By default no artifacts are excluded.`,
                repositoryLayout: `The layout that the repository should use to store and identify modules.`,
                checksumPolicy: `Checksum policy determines how Artifactory behaves when a client checksum for a deployed resource is missing or conflicts with the locally calculated checksum (bad checksum).
For more details, please refer to <a href="https://www.jfrog.com/confluence/display/RTF/Local+Repositories#LocalRepositories-ChecksumPolicy">Checksum Policy</a>.`,
                mavenSnapshotVersionBehavior: `Specifies the naming convention for Maven SNAPSHOT versions.
The options are -
Unique: Version number is based on a time-stamp (default)
Non-unique: Version number uses a self-overriding naming pattern of artifactId-version-SNAPSHOT.type
Deployer: Respects the settings in the Maven client that is deploying the artifact.`,
                maxUniqueSnapshots: `The maximum number of unique snapshots of a single artifact to store.
Once the number of snapshots exceeds this setting, older versions are removed.
A value of 0 (default) indicates there is no limit, and unique snapshots are not cleaned up.`,
                maxUniqueTags: `The maximum number of unique tags of a single Docker image to store in this repository.
Once the number tags for an image exceeds this setting, older tags are removed. A value of 0 (default) indicates there is no limit.`,
                blackedOut: `When set, the repository does not participate in artifact resolution and new artifacts cannot be deployed.`,
                yumMetadataFolderDepth: `The depth, relative to the repository's root folder, where RPM metadata is created.
This is useful when your repository contains multiple RPM repositories under parallel hierarchies.
For example, if your RPMs are stored under 'fedora/linux/$releasever/$basearch', specify a depth of 4.`,
                yumGroupFileNames: `A list of XML file names containing RPM group component definitions.
Artifactory includes the group definitions as part of the calculated RPM metadata, as well as automatically generating a gzipped version of the group files, if required.`,
                allowContentBrowsing: `When set, you may view content such as HTML or Javadoc files directly from Artifactory.
This may not be safe and therefore requires strict content moderation to prevent malicious users from uploading content that may compromise security (e.g., cross-site scripting attacks).`,
                selectPropertySets: `Specifies the Property Sets to be used to construct the list of properties displayed when assigning properties to artifacts in this repository.
This is a convenience; not a restrictive measure. You can still assign any property to artifacts from the Properties tab.`,
                cronExpressionReplication: `The Cron expression that determines when the next replication will be triggered. For detailed information, see <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html" target="_blank">The CronTrigger Tutorial</a>.`,
                nextReplicationTime: `The next replication time based on the Cron expression.`,
                enableEventReplication: `When set, each event will trigger replication of the artifacts changed in the event (e.g. add, delete, change property).`,
                trivialLayout: `When set, the repository will use the deprecated trivial layout.`,
                forceNugetAuth:'Force basic authentication credentials in order to use this repository.',
                pathPrefix: `Limit replication to artifacts matching this sub-path under the source repository.`,
                syncDeletedArtifacts: `When set, items that were deleted locally should also be deleted remotely (also applies to properties metadata).`,
                syncArtifactProperties: `When set, properties of replicated artifacts and folders will also be replicated.`,
                syncArtifactStatistics: `When set, artifact download statistics will also be replicated. Set to avoid inadvertent cleanup at the target instance when setting up replication for disaster recovery.`,
                dockerBlockPushingSchema1: `When set, Artifactory will block the pushing of Docker images with manifest v2 schema 1 to this repository.`
            },
            remoteForm: {
                publicDescription: `Textual description of the repository.
This description is displayed when the repository is selected in the Tree Browser.`,
                internalDescription: `Additional notes that are only displayed in this form. `,
                includesPattern: `List of artifact patterns to include when evaluating artifact requests in the form of x/y/**/z/*. When used, only artifacts matching one of the include patterns are served.
By default, all artifacts are included (**/*).`,
                nuGetDownloadContextPath: `The context path prefix through which NuGet downloads are served.
For example, the NuGet Gallery download URL is 'https://nuget.org/api/v2/package', so the repository
URL should be configured as 'https://nuget.org' and the download context path should be configured as 'api/v2/package'.`,
                v3FeedUrl: `The URL to the NuGet v3 feed.
For example the feed URL for the official nuget.org repository is (also the default value): 
"https://api.nuget.org/v3/index.json"`,
                eagerlyFetchJars: `When marked, the repository attempts to eagerly fetch the jar in the background each time a POM is requested.`,
                eagerlyFetchSources: `When marked, the repository attempts to eagerly fetch the source jar in the background each time a jar is requested.`,
                excludedPattern: `List of artifact patterns to exclude when evaluating artifact requests, in the form of x/y/**/z/*.
By default no artifacts are excluded.`,
                dockerEnableTokenAuthentication: `Enable token (Bearer) based authentication.`,
                checksumPolicy: `Checksum policy determines how Artifactory behaves when a client checksum for a deployed resource is missing or conflicts with the locally calculated checksum (bad checksum).
For more details, please refer to <a href="https://www.jfrog.com/confluence/display/RTF/Remote+Repositories#RemoteRepositories-ChecksumPolicy">Checksum Policy</a>.`,
                maxUniqueSnapshots: `The maximum number of unique snapshots of a single artifact to store.
Once the number of snapshots exceeds this setting, older versions are removed.
A value of 0 (default) indicates there is no limit, and unique snapshots are not cleaned up.`,
                listRemoteFolderItems: `Lists the items of remote folders in simple and list browsing. Required for dynamic resolution that depends on remote folder content information, such as remote Ivy version lookups. The remote content is cached according to the value of the
'Retrieval Cache Period'.`,
                blackedOut: `When set, the repository or its local cache do not participate in artifact resolution.`,
                globalOfflineMode: `If set, Artifactory does not try to access remote resources to fetch artifacts. Only cached and local artifacts are served.`,
                offline: `If set, Artifactory does not try to fetch remote artifacts. Only locally-cached artifacts are retrieved.`,
                shareConfiguration: `If set, the configuration details of this remote repository can be publicly shared with remote clients such as other Artifactory servers.`,
                repositoryLayout: `The layout that the repository should use to store and identify modules.`,
                remoteLayoutMapping: `The layout that best matches that of the remote repository.
Path-mapping takes place if the remote layout is different from the local layout.
In this case, remote module artifacts and descriptors are stored according to the local repository layout (e.g., Maven 1->Maven 2, or Maven 2->Ivy).`,
                localAddress: `The local address to be used when creating connections.
Useful for specifying the interface to use on systems with multiple network interfaces.`,
                username: `Username for HTTP authentication.`,
                password: `Password for HTTP authentication.`,
                socketTimeout: `Network timeout (in ms) to use when establishing a connection and for unanswered requests.
Timing out on a network operation is considered a retrieval failure.`,
                lenientHostAuthentication: `Allow credentials of this repository to be used on requests redirected to any other host.`,
                enableCookieManagement: `Enables cookie management if the remote repository uses cookies to manage client state.`,
                keepUnusedArtifacts: `The number of hours to wait before an artifact is deemed "unused" and eligible for cleanup from the repository.
A value of 0 means automatic cleanup of cached artifacts is disabled.`,
                assumedOfflineLimit: `The number of seconds the repository stays in assumed offline state after a connection error. At the end of this time, an online check is attempted in order to reset the offline status.
A value of 0 means the repository is never assumed offline.`,
                retrievalCachePeriod: `This value refers to the number of seconds to cache metadata files before checking for newer versions on remote server. A value of 0 indicates no caching.`,
                missedRetrievalCachePeriod: `The number of seconds to cache artifact retrieval misses (artifact not found).     A value of 0 indicates no caching.`,
                queryParams: `Custom HTTP query parameters that will be automatically included in all remote resource requests.
For example: param1=val1&ampparam2=val2&ampparam3=val3`,
                allowContentBrowsing: `When set, you may view content such as HTML or Javadoc files directly from Artifactory.
This may not be safe and therefore requires strict content moderation to prevent malicious users from uploading content that may compromise security (e.g., cross-site scripting attacks).`,
                storeArtifactsLocally: `When set, the repository should store cached artifacts locally. When not set, artifacts are not stored locally, and direct repository-to-client streaming is used. This can be useful for multi-server setups over a high-speed LAN, with one Artifactory caching certain data on central storage, and streaming it directly to satellite pass-though Artifactory servers.`,
                synchronizeArtifactoryProperties: `When set, remote artifacts are fetched along with their properties.`,
                selectPropertySets: `Specifies the Property Sets to be used to construct the list of properties displayed when assigning properties to artifacts in this repository.
This is a convenience; not a restrictive measure. You can still assign any property to artifacts from the Properties tab.`,
                cronExpression: `The Cron expression that determines artifact cleanup frequency. For detailed information, see <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html" target="_blank">The CronTrigger Tutorial</a>.`,
                cronExpressionReplication: `The Cron expression that determines when the next replication will be triggered. For detailed information, see <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html" target="_blank">The CronTrigger Tutorial</a>.`,
                syncDeletes: `Delete artifacts and folders that no longer exist in the source repository.`,
                syncProperties: `When set, artifact download statistics will also be replicated. Set to avoid inadvertent cleanup at the target instance when setting up replication for disaster recovery.`,
                pathPrefix: `Limit replication to artifacts matching this sub-path under the source repository.`,
                bowerRegistryURL: `The remote Bower registry URL to communicate with.
Usually the default value (https://bower.herokuapp.com) will be correct unless the remote resource is an Artifactory instance. In this case the value should match the remote repository URL.`,
                nugetFeedContextPath: `The context path prefix through which the NuGet feeds are served.
For example, the NuGet Gallery feed URL is 'https://nuget.org/api/v2', so the repository URL should be configured as 'https://nuget.org' and the feed context path should be configured as 'api/v2'.`,
                nugetDownloadContextPath: `The context path prefix through which NuGet downloads are served.
For example, the NuGet Gallery download URL is 'https://nuget.org/api/v2/package', so the repository
URL should be configured as 'https://nuget.org' and the download context path should be configured as 'api/v2/package'.`,
                smartSyncStatistics: `If set, download statistics for the artifact at the remote Artifactory instance will be updated each time a cached item is downloaded from your repository.`,
                smartSyncProperties: `If set, properties for artifacts that have been cached in this repository will be updated if they are modified in the artifact hosted at the remote Artifactory instance.`,
                smartListRemoteFolderItems: `If set, Artifactory lets you navigate the contents of the repository at the remote Artifactory instance, for all package types, even if the artifacts have not been cached in this repository.`,
                originAbsenceDetection: `If set, Artifactory will check that cached artifacts' sources are available in the origin repository.`,
                propagateQueryParams: `If set, the query params passed with the request to Artifactory, will be passed on to the remote repo.`,
                rejectInvalidJars: `Reject the caching of jar files that are found to be invalid.
For example, pseudo jars retrieved behind a "captive portal".`,
                enableEventReplication: `When set, in addition to running replication based on its Cron expression, each event on the remote repository will trigger replication of the artifacts changed in the event (e.g. add, delete, change property).
To set event replication, the remote replication source must be an Artifactory repository. The Artifactory version must be 5.5 or above.`,
                enableEventReplicationDisabledNoEnterprise: `When set, in addition to running replication based on its Cron expression, each event on the remote repository will trigger replication of the artifacts changed in the event (e.g. add, delete, change property).
To set event replication, you need an Enterprise license.`,
                blockMismatchingMimeTypes: `If set, artifacts will fail to download if a mismatch is detected between requested and received mimetype, according to the list specified in the system properties file under blockedMismatchingMimeTypes. You can override by adding mimetypes to the override list below.`,
                bypassHeadRequests: `Before caching an artifact, Artifactory first sends a HEAD request to the remote resource. In some remote resources, HEAD requests are disallowed and therefore rejected, even though downloading the artifact is allowed. When checked, Artifactory will bypass the HEAD request and cache the artifact directly using a GET request.`,
                foreignLayersCachingPatterns: `Optional include patterns to match external URLs. Ant-style path expressions are supported (*, **, ?).
For example, specifying **/github.com/** will only allow downloading foreign layers from github.com host.`,
                pypiRepositorySuffix: `Usually should be left as a default for 'simple', unless the remote is a PyPI server that has custom registry suffix, like +simple in DevPI`,
                dockerBlockPushingSchema1: `When set, Artifactory will block the pulling of Docker images with manifest v2 schema 1 from the remote repository (i.e. the upstream). It will be possible to pull images with manifest v2 schema 1 that exist in the cache.`
            },
            virtualForm: {
                publicDescription: `Textual description of the repository.
This description is displayed when the repository is selected in the Tree Browser.`,
                internalDescription: `Additional notes that are only displayed in this form. `,
                cleanupRepositoryReferencesinPOM: `(1) Discard Active References - Removes repository elements that are declared directly under project or under a profile in the same POM that is activeByDefault.
(2) Discard Any References - Removes all repository elements regardless of whether they are included in an active profile or not.
(3) Nothing - Does not remove any repository elements declared in the POM.`,
                 pathSuffix: `An optional sub-path inside the local repository where P2 metadata files reside.
When left empty, P2 metadata files (content, artifacts, compositeContent, etc.) are assumed to reside directly under the repository's root.
If you have a Tycho repository deployed as a single archive, specify the archive's root path. For example: 'eclipse-repository.zip!'. `,
                includesPattern: `List of artifact patterns to include when evaluating artifact requests in the form of x/y/**/z/*. When used, only artifacts matching one of the include patterns are served.
By default, all artifacts are included (**/*).`,
                excludedPattern: `List of artifact patterns to exclude when evaluating artifact requests, in the form of x/y/**/z/*.
By default no artifacts are excluded.`,
                resolvedRepositories: `The resolved list of repositories.
Repositories starting with an exclamation mark ('!') indicate that not all tokens can be mapped between the layout of this virtual repository and the marked repository.
Path translations may not work as expected.`,
                artifactoryRequestCanRetrieveRemoteArtifacts: `Determines whether artifact requests coming from other instance of Artifactory can be fulfilled by accessing this virtual repository's remote repositories, or by only accessing its caches (default).`,
                resolveDockerTagsByTimestamp: `When enabled, in cases where the same Docker tag exists in two or more of the aggregated repositories, Artifactory will return the tag that has the latest timestamp.`,
                externalDependenciesPatterns: `Optional include patterns to match external dependencies. Ant-style path expressions are supported (*, **, ?).
For example, specifying **/github.com/** will only allow external dependencies from github.com host.`,
                externalDependenciesPatternsGo: `A whitelist of Ant-style path patterns that determine which remote VCS roots Artifactory will follow to download remote modules from, when presented with 'go-import' meta tags in the remote repository response. By default, this is set to '**', which means that remote modules may be downloaded from any external VCS source.`,
                enableGoImportMetaTag: `When checked (default), Artifactory will automatically follow remote VCS roots in 'go-import' meta tags to download remote modules.`,
                cleanupRepositoryReferencesInPOMs: `(1) Discard Active References - Removes repository elements that are declared directly under a project or a profile in the same POM that is activeByDefault.
(2) Discard Any References - Removes all repository elements regardless of whether they are included in an active profile or not.
(3) Nothing - Does not remove any repository elements declared in the POM.`,
                virtualRetrievalCachePeriod: `This value refers to the number of seconds to cache metadata files before checking for newer versions on aggregated repositories. A value of 0 indicates no caching.`,
                virtualDebianDefaultArchitectures: `Specifies the architectures which will be indexed for the included remote repositories, For example: i386, arm64.`,
                keyPair: `A named key-pair that is used to sign artifacts automatically.`,
                forceMavenAuth:'User authentication is required when accessing the repository. An anonymous request will display an HTTP 401 error. This is also enforced when aggregated repositories support anonymous requests.'

            },
            distributionForm: {
                includesPattern: 'Properties on distributed artifacts with key that matches the list below will be added as a version attributes in Bintray.',
                repositoryVisibility: 'Creating private Bintray repositories is available for <a href="https://www.jfrog.com/bintray/bintray-private-repo/" target="_blank">premium Bintray</a> accounts.',
                distributeProduct: 'When set, the ${productName} token will be replaced in the distribution rules with the product name configured below. Also, Artifactory will create a <a href="https://bintray.com/docs/usermanual/uploads/uploads_products.html" target="_blank">Bintray product</a> and link the deployed packages to the product.'
            },
            layoutsForm: {
                artifactPathPattern: `Please refer to: <a href="https://www.jfrog.com/confluence/display/RTF/Repository+Layouts#RepositoryLayouts-ModulesandPathPatternsusedbyRepositoryLayouts" target="_blank">Path Patterns</a> in the Artifactory Wiki documentation.`,
                distinctiveDescriptorPathPattern: `Please refer to: <a href="https://www.jfrog.com/confluence/display/RTF/Repository+Layouts#RepositoryLayouts-DescriptorPathPatterns" target="_blank">Descriptor Path Patterns</a> in the Artifactory Wiki documentation.`,
                folderIntegrationRevisionRegExp: `A regular expression matching the integration revision string appearing in a folder name
as part of the artifact's path. For example, 'SNAPSHOT', in Maven.
Note! Take care not to introduce any regexp capturing groups within this expression.
If not applicable use '.*'.`,
                fileIntegrationRevisionRegExp: `A regular expression matching the integration revision string appearing in a file name
as part of the artifact's path. For example, 'SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+))',
in Maven.
Note! Take care not to introduce any regexp capturing groups within this expression.
If not applicable use '.*'.`
            },
            reverseProxy: {
                registryPort: `This port will be binded to the Docker registry.`
            },
            rulesPopup:{
                repositoryFilterTooltip:'<b>Repository Filter</b> -(Optional) Rule will only apply to repositories matching the regular expression.' +
                                        'You can reuse the capture values in anyone of the Bintray output fields. <a href="https://www.jfrog.com/confluence/display/RTF/Distribution+Repository#DistributionRepository-RepositoryandPathFilterParameters" target="_blank"> Click here </a> to read more about this field.',
                pathFilterToolip: '<b>Path Filter</b> - (Optional) Rule will only apply to artifacts with path matching the regular expression' +
                                  'You can reuse the capture values in anyone of the Bintray output fields. <a href="https://www.jfrog.com/confluence/display/RTF/Distribution+Repository#DistributionRepository-RepositoryandPathFilterParameters" target="_blank"> Click here </a> to read more about this field.',
            },
            downloadRedirectConfig: `When set, download requests to this repository will redirect the client to download the artifact directly from the cloud storage provider. Available in Enterprise+ and Edge licenses only.`
        },
        security: {
            general: {
                hideExistenceOfUnauthorizedResources: `When set, Artifactory hides the existence of unauthorized resources by sending a 404
response (not found) to requests for resources that are not accessible by the user. Otherwise,
the response implies that the resource exists, but is protected,  by requesting authentication
for anonymous requests (401), or by denying an authenticated request for unauthorized users.`,
                passwordEncryptionPolicy: `Determines the password requirements from users identified to Artifactory from a remote client such as Maven.
The options are:
(1) Supported (default): Users can authenticate using secure encrypted passwords or clear-text passwords.
(2) Required: Users must authenticate using secure encrypted passwords. Clear-text authentication fails.
(3) Unsupported: Only clear-text passwords can be used for authentication.`,
                buildGlobalBasicReadAllowed: `When checked, all users can view the published modules for all builds in the system regardless of any specific permissions applied to a particular build.`,
                encrypt: `Artifactory will generate a Master Encryption Key and encrypt all passwords in your configuration.`,
                decrypt: `Artifactory will decrypt all passwords in your configuration.`,
                passwordMaxAge: `The time interval in which users will be obligated to change their password`,
                notifyByMail: `Users will receive an email notification X days before password will expire.
Mail server must be enabled and configured correctly.`
            },
            usersForm: {
                disableInternalPassword: `When set, user's password is cleared which means that only external authentication is allowed (for example via an LDAP server).`
            },
            permissionsForm: {
                repoPatterns: `Simple comma separated wildcard patterns for repository artifact paths (with no leading slash).
Ant-style path expressions are supported (*, **, ?).
For example: "org/apache/**"`,
                buildPatterns: {
                    includePatterns: `Use Ant-style wildcard patterns to specify build names (i.e. artifact paths) in the build info repository (without a leading slash) that will be included in this permission target.
Ant-style path expressions are supported (*, **, ?).
For example, an "apache/**" pattern will include the "apache" build info in the permission.`,
                    excludePatterns: `Use Ant-style wildcard patterns to specify build names (i.e. artifact paths) in the build info repository (without a leading slash) that will be excluded from this permission target.
Ant-style path expressions are supported (*, **, ?).
For example, an "apache/**" pattern will exclude the "apache" build info from the permission.`
                },
                adminIcon: {
                    user: `Users with admin privileges cannot be added to a Permission Target`,
                    group: `Groups with admin privileges cannot be added to a Permission Target`,
                },

                repositoriesPermissions: `<b>Read</b> - Allows reading and downloading of artifacts
<b>Annotate</b> - Allows annotating artifacts and folders with metadata and properties
<b>Upload / Cache</b> - Allows uploading artifacts to local repositories and caching artifacts from remote repositories
<b>Delete / Overwrite</b> - Allows deletion or overwriting of artifacts
<b>Manage</b> - Allows changing repository permission settings for other users on this permission target`,
                buildsPermissions: `<b>Read</b> - Allows reading and downloading of build info artifacts and viewing the corresponding build in the Builds page
<b>Annotate</b> - Allows annotating build info artifacts and folders with metadata and properties
<b>Upload</b> - Allows uploading and promoting build info artifacts
<b>Delete</b> - Allows deletion of build info artifacts
<b>Manage</b> -  Allows changing build info permission settings for other users on this permission target
`,
            },
            LDAPSettingsForm: {
                LDAPURL: `Location of the LDAP server in the following format:
ldap://myserver:myport/dc=sampledomain,dc=com`,
                allowUserToAccessProfile: `Auto created users will have access to their profile page and will be able to perform actions such as generating an API key.`,
                userDNPattern: `A DN pattern that can be used to log users directly in to LDAP.
This pattern is used to create a DN string for 'direct' user authentication where the pattern is relative to the base DN in the LDAP URL.
The pattern argument {0} is replaced with the username. This only works if anonymous binding is allowed and a direct user DN can
be used, which is not the default case for Active Directory (use User DN search filter instead).
Example: uid={0},ou=People`,
                autoCreateArtifactoryUsers: `When set, users are automatically created when using LDAP. Otherwise, users are transient
and associated with auto-join groups defined in Artifactory.`,
                emailAttribute: `An attribute that can be used to map a user's email address to a user created
automatically in Artifactory.`,
                searchFilter: `A filter expression used to search for the user DN used in LDAP authentication.
This is an LDAP search filter (as defined in 'RFC 2254') with optional arguments.
In this case, the username is the only argument, and is denoted by '{0}'.
Possible examples are:
(uid={0}) - This searches for a username match on the attribute.
Authentication to LDAP is performed from the DN found if successful.`,
                searchBase: `(Optional) A context name to search in relative to the base DN of the LDAP URL. For example, 'ou=users'
With the LDAP Group Add-on enabled, it is possible to enter multiple search base entries
separated by a pipe ('|') character.`,
                manageDN: `The full DN of the user that binds to the LDAP server to perform user searches.
Only used with "search" authentication.
`,
                managerPassword: `The password of the user that binds to the LDAP server to perform the search.
Only used with "search" authentication.`,
                subTreeSearch: `When set, enables deep search through the sub tree of the LDAP URL + search base.`
            },
            LDAPGroupsForm: {
                settingsName: `LDAP group key.`,
                LDAPSetting: `Select the LDAP setting you want to use for group retrieval`,
                static: `Groups have a multi-value member attribute containing user DNs or User IDs.`,
                dynamic: `Users have a mutli-value member attribute containing DNs or names of imported groups.
Default group association strategy for Active Directory.`,
                hierarchy: `User DN contains one or more hierarchical name attributes of imported groups.
For example: cn=joe,ou=sales,ou=europe,dc=acme,dc=com implies Joe's membership in the 'sales' and 'europe' groups.`,
                groupMemberAttribute: `A multi-value attribute on the group entry containing user DNs or IDs of the group members (e.g., uniqueMember,member).`,
                groupNameAttribute: `Attribute on the group entry denoting the group name. Used when importing groups.`,
                descriptionAttribute: `An attribute on the group entry which denoting the group description. Used when importing groups.`,
                filter: `The LDAP filter used to search for group entries. Used when importing groups.`,
                searchBase: `A search base for group entry DNs, relative to the DN on the LDAP server's URL (and not relative to the LDAP Setting's "Search Base"). Used when importing groups.`
            },
            crowd_integration: {
                sessionValidationInterval: `The time window (min) during which the session does not need to be validated.`,
                useJIRAUserServer: `Authenticate using credentials instead of the default session, token-based authentication.
This is required when using the JIRA User Server.`,
                autoCreateArtifactoryUsers: `When set, authenticated users are automatically created in Artifactory.
When not set, for every request from a Crowd user, the user is temporarily associated with default groups (if such groups are defined),
and the permissions for these groups apply. Without automatic user creation, you must manually create the user in Artifactory to manage
user permissions not attached to their default groups.`,
                allowUserToAccessProfile: `Auto created users will have access to their profile page and will be able to perform actions such as generating an API key.`,
                useDefaultProxyConfiguration: `If a default proxy definition exists, it is used to pass through to the Crowd Server.`

            },
            SAMLSSOSettings: {
                SAMLLoginURL: `The identity provider login URL (when you try to login, the service provider redirects to this URL).`,
                SAMLLogoutURL: `The identity provider logout URL (when you try to logout, the service provider redirects to this URL).`,
                SAMLServiceProviderName: `The Artifactory name in the SAML federation.`,
                SAMLCertificate: `The certificate for SAML Authentication.
NOTE! The certificate must contain the public key to allow Artifactory to verify sign-in requests.`,
                autoCreateArtifactoryUsers: `When set, authenticated users are automatically created in Artifactory.
When not set, for every request from a SAML user, the user is temporarily associated with default groups (if such groups are defined),
and the permissions for these groups apply. Without automatic user creation, you must manually create the user inside Artifactory to manage
user permissions not attached to their default groups.`,
                allowUserToAccessProfile: `Auto created users will have access to their profile page and will be able to perform actions such as generating an API key.`,
                useEncryptedAssertion: `When set, an X.509 public certificate will be created by Artifactory. Download this certificate and upload it to your IDP and choose your own encryption algorithm. This process will let you encrypt the assertion section in your SAML response.`,
                autoRedirect: `When set, clicking on the login link will direct users to the configured SAML login URL.`,
                syncGroups: "When set, in addition to the groups the user is already associated with, he will also be associated with the groups returned in the SAML login " +
                "response. Note that the user's association with the returned groups is not persistent. It is only valid for the current login session.",
                groupAttribute: `The group attribute in the SAML login XML response.`,
                emailAttribute: `If Auto Create Artifactory Users is enabled or an internal user exists, Artifactory will set the user's email to the value in this attribute that is returned by the SAML login XML response.`
            },
            OAuthSSO: {
                id: `Your OAuth2 id, given by the provider.`,
                secret: `Your OAuth2 shared secret, given by the provider.`,
                domain: `Google App domain accepted for authentication.`,
                basicUrl: `The url used to acquire a token via basic auth.`,
                authUrl: `The url used for the initial authentication step.`,
                apiUrl: `The url used for api access, if needed to get user data.`,
                tokenUrl: `The url used to acquire a token from the provider.`,
                allowUserToAccessProfile: `Auto created users will have access to their profile page and will be able to perform actions such as generating an API key.`
            },
            HTTPSSO: {
                artifactoryIsProxiedByASecureHTTPServer: `When set, Artifactory trusts incoming requests and reuses the remote user originally set on the request by the SSO of the HTTP server.
This is useful if you want to use existing enterprise SSO integrations, such as the powerful authentication schemes provided by Apache (mod_auth_ldap, mod_auth_ntlm, mod_auth_kerb, etc.).
When Artifactory is deployed as a webapp on Tomcat behind Apache:
If using mod_jk, be sure to use the "JkEnvVar REMOTE_USER" directive in Apache's configuration.`,
                remoteUserRequestVariable: `The name of the HTTP request variable to use for extracting the user identity.
Default is: REMOTE_USER.`,
                autoCreateArtifactoryUsers: `When set, authenticated users are automatically created in Artifactory.
When not set, for every request from an SSO user, the user is temporarily associated with default groups (if such groups are defined),
and the permissions for these groups apply. Without automatic user creation, you must manually create the user inside Artifactory to manage
user permissions not attached to their default groups.`,
                allowUserToAccessProfile: `Auto created users will have access to their profile page and will be able to perform actions such as generating an API key.`,
                autoAssociateLDAPGroups: `When set, the user will be associated with the groups returned in the LDAP login response. Note that the user's association with the returned groups is persistent if the 'Auto Create Artifactory Users' is set.`
            },
            SSHSERVER: {
                enableSshServer: `Enable SSH authentication.`,
                serverPublicKey: `SSH Public Key to identify your server.`,
                serverPrivateKey: `SSH Private Key to identify your server.`,
                sshServerPort: `The port to use for SSH authentication. Default: 1337`,
                customURLBase: `A hard-coded URL prefix used to calculate relative URLs.`
            },
            signingKeys: {
                passPhrase: `Pass phrase required to use the installed keys. It can be saved or supplied with the REST API calls.
The "Verify" button checks that the keys and pass phrase match, and can be used to verify the pass phrase without saving it.
If keys are saved, we highly recommend using the Master Encryption Key feature.`
            }

        },
        services: {
            backupsForm: {
                cronExpression: `The Cron expression that determines backup frequency. For detailed information, see <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html" target="_blank">The CronTrigger Tutorial</a>`,
                serverPathForBackup: `The directory to which local repository data is backed up as files.
The default is $ARTIFACTORY_HOME/backup/[backup_key]`,
                sendMailToAdminsIfThereAreBackupErrors: `Requires properly configured email settings and valid email addresses for admin users.`,
                precalculateSpaceBeforeBackup: `If set, Artifactory will verify that the backup target location has enough disk space available to hold the backed up data. If there is not enough space available, Artifactory will abort the backup and write a message in the log file. Applicable only to non-incremental backups.`,
                excludeBuilds: `Exclude all builds from the backup.`,
                excludeNewRepositories: `Automatically exclude new repositories from the backup.`,
                retentionPeriod: `The maximum number of hours to keep old backups in the destination directory.
Setting the "Incremental" checkbox, indicates that backups are incrementally written
(delta only) to the same directory: \${backupDir}/current. This "in place" backup is suitable
for file-system based backup support. In this mode, cleanup of old backups is inactive.
The default is 168 hours (7 days).`,
                backUpToAZipArchive: `When set, the backup output should be a zip archive.
Otherwise the output is to a directory (default).`

            },
            mavenIndexer: {
                 cronExpression: `The Cron expression that determines indexer frequency. For detailed information, see <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html" target="_blank">The CronTrigger Tutorial</a>`
                 }
        },
        logAnalytics: {
            newConnection: "Use this option if you don't have a Client ID and Secret for Artifactory."
        }
    },
    artifacts: {
        deploy: {
            targetRepo: `The target repository to which the artifact should be deployed.`,
            targetPath: `The relative path in the target repository. You can add key-value matrix parameters to deploy the artifacts with properties.
For more details, please refer to <a href="https://www.jfrog.com/confluence/display/RTF/Using+Properties+in+Deployment+and+Resolution#UsingPropertiesinDeploymentandResolution-IntroducingMatrixParameters" target="_blank"> Introducing Matrix Parameters</a>.`,
            deployAsMaven: `Deploy a Maven artifact where the target deployment path is based on Maven attributes.
If you want to specify the target deployment path manually, unset this option.`,
            deployAsDebian: `Deploying a Debian file to a Debian repository requires coordinates. Setting this flag lets you configure the Debian file coordinates`,
            noDeployPermission: `You do not have deploy permission`,
        },
        pushToBintray: {
            bintrayPackageName: `A target package name under the repository. You must create the package in Bintray first if it does not exist.`,
            bintrayPackageVersion: `A target version under the package.If the version does not yet exist in Bintray, it is automatically created.`
        },
        browse: {
            created: `The time this artifact was deployed to or cached in Artifactory.`,
            lastModified: `The time this artifact was modified. If this value is not available, the artifact's 'Created' value is used.
This can occur if the artifact is deployed without the 'X-Artifactory-Last-Modified' request header.`,
            licenses: `Scans the archive for a textual license file. The following file names are searched for:
license,LICENSE,license.txt,LICENSE.txt,LICENSE.TXT
(You can override this list by using the 'artifactory.archive.licenseFile.names' property).`,
            filtered: `Set this to have Artifactory serve the file as a filtered resource.
A filtered textual resource is processed by the <a href="https://freemarker.apache.org/" target="_blank">FreeMarker</a> engine before being returned to clients.
The context accessible to the template includes:
Properties ,Security and Request. Javadocs can be found in the <a href="https://repo.jfrog.org/artifactory/libs-releases-local/org/artifactory/artifactory-papi/%5BRELEASE%5D/artifactory-papi-%5BRELEASE%5D-javadoc.jar!/index.html" target="_blank">Artifactory Public API</a>.`,
            lastReplicationStatus: `Displays the result of the latest run of this repository's scheduled replication.
Can be one of the following:
Never ran: Replication has not yet run.
Incomplete: Replication has not yet completed or was interrupted.
Completed with errors: Replication errors were logged.
Completed with warnings: Replication warnings were logged.
Completed successfully: No errors or warnings logged.
Inconsistent: Replication status cannot be interpreted.`,
            recursive: `When checked, the property will be added to the selected folder and to all of the artifacts, folders and sub-folders under this folder.`
        },
        search: {
            stash: `The Stash lets you store search results for later use.
Once it is populated, you can add, subtract or intersect new search results to assemble just the right set of artifacts you need.
The Stash Browser displays all the artifacts in your stash and provides a convenient way to perform bulk operations.
You can copy or move the entire Stash to a repository, or perform actions on individual items.`
        },

        general: {
            /*name: 'Copy this link to navigate directly to this item in the tree browser.',*/
            created: `The time this artifact was deployed to or cached in Artifactory`,
            filtered: `Set this to have Artifactory serve the file as a filtered resource.
A filtered textual resource is processed by the <a href="https://freemarker.apache.org/" target="_blank">FreeMarker</a> engine before being returned to clients.
The context accessible to the template includes:
Properties ,Security and Request. Javadocs can be found in the <a href="https://repo.jfrog.org/artifactory/libs-releases-local/org/artifactory/artifactory-papi/%5BRELEASE%5D/artifactory-papi-%5BRELEASE%5D-javadoc.jar!/index.html" target="_blank">Artifactory Public API</a>.`
        },
        selectTargetPathModal: {
            targetRepoInput: [`Selects the target repository for the transferred items.`,
`Repositories starting with an exclamation mark (\'!\') indicate that not all tokens`,
`can be mapped between the layouts of the source repository and the marked repository.`,
`Path translations may not work as expected.`].join(' '),
            copyToCustomCheckbox: {
                copy: [`Enable copying and renaming to a custom target path. WARNING: This will cause`,
`the operation to suppress cross-layout translation when copying to different layouts.`,
`This means that your client may not be able to resolve the artifacts even in cases of a same-layout move.`].join(' '),
                move: [`Enable moving and renaming to a custom target path. WARNING: This will cause`,
`the operation to suppress cross-layout translation when moving to different layouts.`,
`This means that your client may not be able to resolve the artifacts even in cases of a same-layout move.`].join(' ')
            },
            customPathInput: {
                copy: [`Type the path in the target repository where the selected source should be copied to.`,
`NOTE: Copy operations are executed using Unix conventions (e.g.copying org/jfrog/example from`,
`a source repository to org/jfrog/example in a target repository will result in the contents of the source`,
`being copied to org/jfrog/example/example). To achieve the same path in the target repository, copy`,
`the source into one folder up in the hierarchy (i.e. copy source org/jfrog/example into target org/jfrog).`,
`If you leave the Target Path empty, the source will be moved into the target repository\'s root folder.`].join(' '),
                move: [`Type the path in the target repository where the selected source should be moved to.`,
`NOTE: Move operations are executed using Unix conventions (e.g. moving org/jfrog/example from`,
`a source repository to org/jfrog/example in a target repository will result in the contents of the source`,
`being moved to org/jfrog/example/example). To achieve the same path in the target repository, move`,
`the source into one folder up in the hierarchy (i.e. move source org/jfrog/example into target org/jfrog).`,
`If you leave the Target Path empty, the source will be moved into the target repository\'s root folder.`].join(' ')
            }

        }
    },
    builds: {
        summary: `An artifact license can have one of the following statuses:
Unapproved: The license found is not approved.
Unknown: License information was found but cannot be related to any license managed in Artifactory.
Not Found: No license information could be found for the artifact.
Neutral: The license found is not approved, however another approved license was found for the artifact.
Approved: The license found is approved.`,
        includePublishedArtifacts: `Include the build's published module artifacts in the license report if they are also used as dependencies for other modules in this build.`,
        IncludeDependenciesOfTheFollowingScopes: `Include the build's published module dependencies in the license report.
You can optionally select the dependency scopes to include.`,
        autoFindLicenses: `Automatically extract license data from artifacts' module information.
When an artifact has conflicting licenses already attached, you can select whether
to override these licenses with the ones found.`,
        name: `The Code Center application name. Click on the link to navigate to this application in Code Center.`
    },
    userProfile: {
        apiKey: `Your API key can be used to authenticate you when using the REST API.
To use the API key, add the following header to all REST API calls: 'X-JFrog-Art-Api: &ltYOUR_API_KEY&gt'`
    }

}