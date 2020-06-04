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

package org.artifactory.environment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.jfrog.storage.binstore.config.model.Param;
import org.jfrog.storage.binstore.ifc.BinaryProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * When adding a field here you must also add it under BinaryProviderMetaDataFiller
 *
 * @author gidis
 */
public class BinaryStoreProperties {
    private static final Logger log = LoggerFactory.getLogger(BinaryStoreProperties.class);
    private static final String DEFAULT_MAX_CACHE_SIZE = "5000000000";
    private final String baseDataDir;
    private final String accessServiceDir;
    private String clusterDataDir;
    private Properties props = new Properties();

    public BinaryStoreProperties(File storagePropsFile, String baseDataDir, String securityDir) throws IOException {
        try (FileInputStream pis = new FileInputStream(storagePropsFile)) {
            props.load(pis);
        }
        this.baseDataDir = baseDataDir;
        this.accessServiceDir = securityDir + "/binstore";
        this.clusterDataDir = null;
        trimValues();
        // verify that the database is supported (will throw an exception if not found)
        log.debug("Loaded storage properties");
    }

    public BinaryStoreProperties(String baseDataDir, String securityDir) {
        this.baseDataDir = baseDataDir;
        this.accessServiceDir = securityDir + "/binstore";
    }

    /**
     * Returns true if parameter key isn't configured in this class Key enum as a key or a prefix,
     * or param value is the key's default
     */
    public static boolean isDefault(Param param, String providerSignature) {
        String paramKey = param.getName();
        if (paramKey.startsWith(Key.binaryProviderS3Param.key + ".") ||
                paramKey.startsWith(Key.binaryProviderGCParam.key + ".")) {
            return false;
        }
        Key key = getKey(paramKey, providerSignature);
        if (key == null) {
            return true;
        }
        String value = param.getValue();
        return (key.defaultValue == null && StringUtils.isBlank(value)) ||
                (key.defaultValue != null && key.defaultValue.toString().equals(value));
    }

    private static Key getKey(String providerKey, String providerSignature) {
        for (Key key : Key.values()) {
            if (key.types.contains(providerSignature.trim())) {
                if (providerKey.equals(key.providerKey)) {
                    return key;
                }
            }
        }
        return null;
    }

    public void setClusterDataDir(String clusterDataDir) {
        this.clusterDataDir = clusterDataDir;
    }

    public String getTempDir() {
        return getProperty(Key.binaryProviderFilesystemTempDir, "_pre");
    }

    public Map<String, String> getS3Params() {
        return getProperties(Key.binaryProviderS3Param.key + ".");
    }

    public Map<String, String> getGCParams() {
        return getProperties(Key.binaryProviderGCParam.key + ".");
    }

    public String getProperty(Key property) {
        return props.getProperty(property.key);
    }

    public String getProperty(Key property, String defaultValue) {
        return props.getProperty(property.key, defaultValue);
    }

    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    private void trimValues() {
        Properties newProperties = new Properties();
        for (Object key : props.keySet()) {
            String value = props.getProperty((String) key);
            newProperties.put(key, StringUtils.trimToEmpty(value));
        }
        props = newProperties;
    }

    public Map<String, String> getProperties(String prefix) {
        Map<String, String> result = Maps.newHashMap();
        for (Object keyObject : props.keySet()) {
            String key = (String) keyObject;
            String value = props.getProperty(key);
            if (key.startsWith(prefix)) {
                String reminder = key.replace(prefix, "");
                if (!StringUtils.isBlank(reminder)) {
                    result.put(reminder, value);
                }
            }
        }
        return result;
    }

    public BinaryProviderConfig toDefaultValues() {
        BinaryProviderConfig defaultValues = new BinaryProviderConfig();
        for (Key key : Key.values()) {
            if (!key.isBinaryProviderField()) {
                continue;
            }
            if (Key.binaryProviderCacheMaxSize.equals(key)) {
                // TODO: [by fsi] check with Gidi, looks fishy?
                String value = getProperty(key, DEFAULT_MAX_CACHE_SIZE);
                defaultValues.addParam(key.signature, value);
            } else {
                String defaultValue = key.getDefaultValue() != null ? key.getDefaultValue().toString() : null;
                defaultValues.addParam(key.signature, getProperty(key.key(), defaultValue));
            }
        }
        defaultValues.addParam("baseDataDir", baseDataDir);
        //TODO: [by YS] this should be passed as parameter to interested party (and not used as part of binary provider config)
        defaultValues.addParam("accessServiceDir", accessServiceDir);
        if (clusterDataDir != null) {
            defaultValues.addParam("clusterDataDir", clusterDataDir);
        }
        addS3Properties(defaultValues);
        addGCProperties(defaultValues);
        return defaultValues;
    }

    private void addS3Properties(BinaryProviderConfig binaryProviderConfig) {
        Map<String, String> s3Params = getS3Params();
        for (String key : s3Params.keySet()) {
            binaryProviderConfig.addProperty(key, s3Params.get(key));
        }
    }

    private void addGCProperties(BinaryProviderConfig binaryProviderConfig) {
        Map<String, String> s3Params = getGCParams();
        for (String key : s3Params.keySet()) {
            binaryProviderConfig.addProperty(key, s3Params.get(key));
        }
    }

    public enum Key {

        binaryProviderType("binary.provider.type", "", "storageType", "storageType", BinaryProviderType.filesystem.name()),  // see BinaryProviderType
        binaryProviderCacheMaxSize("binary.provider.cache.maxSize", "cache-fs", "maxCacheSize", "maxCacheSize", DEFAULT_MAX_CACHE_SIZE),
        binaryProviderBinariesDir("binary.provider.binaries.dir", "state-aware,file-system,cache-fs,external,external-wrapper", "binariesDir", "binariesDir", null),
        binaryProviderCacheCacheSynchQuitePeriod("binary.provider.cache.synch.quite.period", "cache-fs", "cacheSynchQuietPeriod", "cacheSynchQuietPeriod", 60 * 1000),
        binaryProviderCacheDir("binary.provider.cache.dir", "cache-fs", "cacheProviderDir", "cacheProviderDir", "cache"),
        binaryProviderParallelDownloadWindowInMillis("binary.provider.cache.parallelDownloadWindowInMillis", "cache-fs,multi-read-cache-fs", "parallelDownloadWindowInMillis", "parallelDownloadWindowInMillis", 30 * 60 * 1000),
        binaryProviderMultiReadEnabled("binary.provider.cache.multiReadEnabled", "cache-fs", "multiReadEnabled", "multiReadEnabled", true),
        binaryProviderValidateChecksum("binary.provider.cache.validateChecksum", "cache-fs", "validateChecksum", "validateChecksum", true),
        binaryProviderFilesystemDir("binary.provider.filesystem.dir", "state-aware,file-system,cache-fs,external-wrapper", "fileStoreDir", "fileStoreDir", "filestore"),
        binaryProviderFilesystemTempDir("binary.provider.filesystem.temp.dir", "state-aware,cache-fs,file-system,external,external-wrapper", "tempDir", "tempDir", "_pre"),
        binaryProviderExternalDir("binary.provider.external.dir", "external", "externalDir", "externalDir", null),
        binaryProviderExternalMode("binary.provider.external.mode", "external-wrapper", "connectMode", "connectMode", null),
        binaryProviderInfoEvictionTime("binary.provider.info.eviction.time", "cache-fs", "binaryProviderInfoEvictionTime", "binaryProviderInfoEvictionTime", 1000 * 5),

        // Retry binary provider
        binaryProviderRetryMaxRetriesNumber("binary.provider.retry.max.retries.number", "retry", "maxTrys", "maxTrys", 5),
        binaryProviderRetryDelayBetweenRetries("binary.provider.retry.delay.between.retries", "retry", "interval", "interval", 5000),

        // HDFS binary provider
        binaryProviderHdfsKeyStoreLocation("binary.provider.hdfs.keystore.location", "hdfs", "hdfsKeyStoreLocation", "keyStoreLocation", null),
        binaryProviderHdfsKeyStorePassword("binary.provider.hdfs.keystore.password", "hdfs", "hdfsKeyStorePassword", "keyStorePassword", null),
        binaryProviderHdfsPrincipal("binary.provider.hdfs.principal", "hdfs", "hdfsPrincipal", "principal", null),
        binaryProviderHdfsPrincipalPassword("binary.provider.hdfs.principal.password", "hdfs", "hdfsPrincipalPassword", "principalPassword", null),
        binaryProviderHdfsNameNode("binary.provider.hdfs.name.node", "hdfs", "hdfsNameNode", "nameNode", null),

        // Azure binary provider
        binaryProviderAzureContainerName("binary.provider.azure.container.name", "azure-blob-storage", "azureContainerName", "containerName", null),
        binaryProviderAzureAccountName("binary.provider.azure.account.name", "azure-blob-storage", "azureAccountName", "accountName", null),
        binaryProviderAzureAccountKey("binary.provider.azure.account.key", "azure-blob-storage", "azureAccountKey", "accountKey", null),

        // S3 binary provider
        binaryProviderS3Identity("binary.provider.s3.identity", "s3,s3Old", "s3Identity", "identity", null),
        binaryProviderS3UseSignature("binary.provider.s3.use.signature", "s3,s3Old", "s3UseSignature", "useSignature", false),
        binaryProviderS3SignatureExpirySeconds("binary.provider.s3.signature.expiry.seconds", "s3", "s3SignatureExpirySeconds", "signatureExpirySeconds", 300),
        binaryProviderS3SignedUrlExpirySeconds("binary.provider.s3.signed.url.expiry.seconds", "s3", "s3SignedUrlExpirySeconds", "signedUrlExpirySeconds", 30),
        binaryProviderS3CloudFrontDomainName("binary.provider.s3.cloudfront.domain.name", "s3", "s3CloudFrontDomainName", "cloudFrontDomainName", null),
        binaryProviderS3CloudFrontKeyPairId("binary.provider.s3.cloudfront.keypair.id", "s3", "s3CloudFrontKeyPairId", "cloudFrontKeyPairId", null),
        binaryProviderS3CloudFrontPrivateKey("binary.provider.s3.cloudfront.private.key", "s3", "s3CloudFrontPrivateKey", "cloudFrontPrivateKey", null),
        binaryProviderS3Credential("binary.provider.s3.credential", "s3,s3Old", "s3Credential", "credential", null),
        binaryProviderS3BlobVerifyTimeout("binary.provider.s3.blob.verification.timeout", "s3,s3Old", "s3VerificationTimeout", "verificationTimeout", 60000),
        binaryProviderS3ProxyIdentity("binary.provider.s3.proxy.identity", "s3,s3Old", "s3ProxyIdentity", "proxyIdentity", null),
        binaryProviderS3ProxyCredential("binary.provider.s3.proxy.credential", "s3,s3Old", "s3ProxyCredential", "proxyCredential", null),
        binaryProviderS3BucketName("binary.provider.s3.bucket.name", "s3,s3Old", "s3BucketName", "bucketName", null),
        binaryProviderS3BucketPath("binary.provider.s3.bucket.path", "s3,s3Old", "s3Path", "path", "filestore"),
        binaryProviderS3ProviderId("binary.provider.s3.provider.id", "s3,s3Old", "s3ProviderId", "providerId", "s3"),
        binaryProviderS3Endpoint("binary.provider.s3.endpoint", "s3,s3Old", "s3Endpoint", "endpoint", null),
        binaryProviderS3EndpointPort("binary.provider.s3.endpoint.port", "s3,s3Old", "s3Port", "port", -1),
        binaryProviderS3awsVersion("binary.provider.s3.aws.version", "s3,s3Old", "s3AwsVersion", "s3AwsVersion", null),
        binaryProviderS3folderPrefixLength("binary.provider.s3.root.folders.name.length", "s3,s3Old", "rootFoldersNameLength", "rootFoldersNameLength", 2),
        binaryProviderS3Region("binary.provider.s3.region", "s3,s3Old", "s3Region", "region", null),
        binaryProviderS3MultiPartLimit("binary.provider.s3.multi.part.limit", "s3,s3Old", "s3MultiPartLimit", "multiPartLimit", 100 * 1000 * 1000),
        binaryProviderS3HttpsOnly("binary.provider.s3.https.only", "s3,s3Old", "s3HttpsOnly", "httpsOnly", true),
        binaryProviderS3HttpsPort("binary.provider.s3.https.port", "s3,s3Old", "s3HttpsPort", "httpsPort", -1),
        binaryProviderS3TestConnection("binary.provider.s3.test.connection", "s3,s3Old", "s3TestConnection", "testConnection", true),
        binaryProviderS3ProxyPort("binary.provider.s3.proxy.port", "s3,s3Old", "s3ProxyPort", "proxyPort", -1),
        binaryProviderS3ProxyHost("binary.provider.s3.proxy.host", "s3,s3Old", "s3ProxyHost", "proxyHost", null),
        binaryProviderS3RoleName("binary.provider.s3.role.name", "s3,s3Old", "s3RoleName", "roleName", null),
        binaryProviderS3RefreshCredentials("binary.provider.s3.refresh.credentials", "s3,s3Old", "s3refreshCredentials", "refreshCredentials", null),
        binaryProviderS3instanceMetadataUrl("binary.provider.s3.instance.metadata.url", "s3,s3Old", "s3instanceMetadataUrl", "instanceMetadataUrl", null),

        // For testing the KMS
        binaryProviderS3Sse("s3service.server-side-encryption", "s3", "s3service.server-side-encryption", "s3service.server-side-encryption", null),
        binaryProviderS3SseKey("s3service.server-side-encryption-aws-kms-key-id", "s3", "s3service.server-side-encryption-aws-kms-key-id", "s3service.server-side-encryption-aws-kms-key-id", null),

        // Google binary provider
        binaryProviderGsBucketExist("binary.provider.gs.bucket.exist", "google-storage", "gsBucketExists",
                "bucketExists", false),
        binaryProviderGsIdentity("binary.provider.gs.identity", "google-storage", "gsIdentity", "identity", null),
        binaryProviderGsCredential("binary.provider.gs.credential", "google-storage", "gsCredential", "credential", null),
        binaryProviderGsProxyIdentity("binary.provider.gs.proxy.identity", "google-storage", "gsProxyIdentity", "proxyIdentity", null),
        binaryProviderGsProxyCredential("binary.provider.gs.proxy.credential", "google-storage", "gsProxyCredential", "proxyCredential", null),
        binaryProviderGsBucketName("binary.provider.gs.bucket.name", "google-storage", "gsBucketName", "bucketName", null),
        binaryProviderGsBucketPath("binary.provider.gs.bucket.path", "google-storage", "gsPath", "path", "filestore"),
        binaryProviderGsHttpPort("binary.provider.gs.endpoint.http.port", "google-storage", "gsPort", "port", 80),
        binaryProviderGsEndPoint("binary.provider.gs.endpoint", "google-storage", "gsEndpoint", "endpoint", "commondatastorage.googleapis.com"),
        binaryProviderGsHttpsOnly("binary.provider.gs.https.only", "google-storage", "gsHttpsOnly", "httpsOnly", true),
        binaryProviderGsHttpsPort("binary.provider.gs.https.port", "google-storage", "gsHttpsPort", "httpsPort", 443),
        binaryProviderGsTestConnection("binary.provider.gs.test.connection", "google-storage", "gsTestConnection", "testConnection", true),
        binaryProviderGsProxyPort("binary.provider.gs.proxy.port", "google-storage", "gsProxyPort", "proxyPort", -1),
        binaryProviderGsProxyHost("binary.provider.gs.proxy.host", "google-storage", "gsProxyHost", "proxyHost", null),
        binaryProviderGsUsesignature("binary.provider.gs.use.signature", "google-storage", "gsUseSignature", "useSignature", null),
        binaryProviderGsMultiPartLimit("binary.provider.gs.multi.part.limit", "google-storage", "gsMultiPartLimit", "multiPartLimit", null),
        binaryProviderGsRegion("binary.provider.gs.region", "google-storage", "gsRegion", "region", null),
        binaryProviderGsProviderId("binary.provider.gs.provider.id", "google-storage", "gsProviderId", "providerId", null),

        // Dynamic S3 Param
        binaryProviderS3Param("binary.provider.s3.env", "S3,S3Old", null, null, null),

        // Dynamic GC Param
        binaryProviderGCParam("binary.provider.gc.env", "google-storage", null, null, null),

        // Eventually persisted binary provider
        binaryProviderEventuallyPersistedMaxNumberOfTreads(
                "binary.provider.eventually.persisted.max.number.of.threads", "eventual", "numberOfThreads", "numberOfThreads", 5),
        binaryProviderEventuallyPersistedTimeOut("binary.provider.eventually.persisted.timeout", "eventual", "timeout", "timeout", 120000),
        binaryProviderEventuallyPersistedDispatcherSleepTime(
                "binary.provider.eventually.dispatcher.sleep.time", "eventual", "dispatcherInterval", "dispatcherInterval", 5000), // in millis
        binaryProviderEventuallyPersistedWaitHazelcastTime(
                "binary.provider.eventually.persisted.wait.hazelcast.time", "eventual", "hazelcastWaitingTime", "hazelcastWaitingTime", 5000), // in millis
        binaryProviderEventuallyPersistedQueueSize(
                "binary.provider.eventually.persisted.queue.size", "eventual", "queueSize", "queueSize", 64),

        // All Sharding
        binaryProviderShardingZone("binary.provider.sharding.zone", "sharding", "zone", "zone", "empty-zone"),
        binaryProviderShardingReadBehavior("binary.provider.sharding.read.behavior", "sharding", "readBehavior", "readBehavior", "roundRobin"),
        binaryProviderShardingWriteBehavior("binary.provider.sharding.write.behavior", "sharding", "writeBehavior", "writeBehavior", "roundRobin"),
        binaryProviderShardingRedundancy("binary.provider.sharding.redundancy", "sharding", "redundancy", "redundancy", "1"),
        binaryProviderShardingConcurrentStreamWaitTimeout("binary.provider.sharding.concurrent.stream.wait.timeout", "sharding", "concurrentStreamWaitTimeout", "concurrentStreamWaitTimeout", "30000"),
        binaryProviderShardingConcurrentStreamBufferKb("binary.provider.sharding.concurrent.stream.buffer.kb", "sharding", "concurrentStreamBufferKb", "concurrentStreamBufferKb", "32"),
        binaryProviderShardingMaxBalancingRunTime("binary.provider.sharding.max.balancing.run.time", "sharding", "maxBalancingRunTime", "maxBalancingRunTime", "36000000"),
        binaryProviderShardingFreeSpaceSampleInterval("binary.provider.sharding.free.space.sample.interval", "sharding", "freeSpaceSampleInterval", "freeSpaceSampleInterval", "36000000"),
        binaryProviderShardingMinSpareUploaderExecutor("binary.provider.sharding.min.spare.uploader.executor", "sharding", "minSpareUploaderExecutor", "minSpareUploaderExecutor", "2"),
        binaryProviderShardingUploaderCleanupIdleTime("binary.provider.sharding.uploader.cleanup.idle.time", "sharding", "uploaderCleanupIdleTime", "uploaderCleanupIdleTime", "120000");

        private final String key;
        private List<String> types;
        private final String signature;
        private String providerKey;
        private final Object defaultValue;

        Key(String key, String types, String signature, String providerKey, Object defaultValue) {
            this.key = key;
            this.providerKey = providerKey;
            String[] split = StringUtils.split(types, ",");
            this.types = Lists.newArrayList();
            for (String type : split) {
                this.types.add(type.trim());
            }
            this.signature = signature;
            this.defaultValue = defaultValue;
        }

        public String key() {
            return key;
        }

        public String getProviderKey() {
            return providerKey;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public boolean isBinaryProviderField() {
            return !(this.equals(binaryProviderS3Param) || this.equals(binaryProviderGCParam));
        }

        public String getSignature() {
            return signature;
        }

    }

    public enum BinaryProviderType {
        filesystem, // binaries are stored in the filesystem
        fullDb,     // binaries are stored as blobs in the db, filesystem is used for caching unless cache size is 0
        cachedFS,   // binaries are stored in the filesystem, but a front cache (faster access) is added
        S3,         // binaries are stored in S3 JClouds API
        S3Old,        // binaries are stored in S3 Jets3t API
        goog        // binaries are stored in S3 Jets3t API
    }
}
