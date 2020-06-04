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

package org.artifactory.converter;

import com.google.common.collect.Maps;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryCommonDbPropertiesService;
import org.artifactory.common.property.ArtifactoryConverter;
import org.artifactory.common.storage.db.properties.DbVersionInfo;
import org.artifactory.config.CronConfigurationException;
import org.artifactory.converter.postinit.PostInitConverterAdapter;
import org.artifactory.environment.converter.local.PreInitConverter;
import org.artifactory.environment.converter.local.version.v1.NoNfsClusterHomeConverter;
import org.artifactory.environment.converter.shared.SharedEnvironmentConverter;
import org.artifactory.logging.converter.LoggingConverter;
import org.artifactory.version.CompoundVersionDetails;
import org.artifactory.version.VersionProvider;
import org.jfrog.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.artifactory.converter.ConverterType.*;


/**
 * The class manages the conversions process during Artifactory life cycle.
 * It creates a complete separation between the HOME, CLUSTER and database
 * environments, thoughts Artifactory converts each environment independently,
 * each environment has its own original version and each original version might
 * trigger the relevant conversion.
 * For HA environment, only the primary master node can do a conversion of the
 * cluster home and DB. And once primary is up and running,
 * shutdown events are sent to the slaves.
 *
 * @author Gidi Shabat
 */
public class ConvertersManagerImpl implements ConverterManager {
    private static final Logger log = LoggerFactory.getLogger(ConvertersManagerImpl.class);

    private final ConverterBlocker rollbackBlocker;
    private final ArtifactoryHome artifactoryHome;
    private final VersionProvider vp;
    private Map<ConverterType, ArrayList<ArtifactoryConverterAdapter>> converters;
    private boolean preInitConversionRunning;
    private boolean homeSyncConversionRunning;
    private boolean homeConversionRunning;
    private boolean databaseConversionRunning;
    private boolean postInitConversionRunning;
    private ConfigurationManager configurationManager;

    public ConvertersManagerImpl(ArtifactoryHome artifactoryHome, VersionProvider vp, ConfigurationManager configurationManager) {
        // Initialize
        initConvertersMap();
        this.configurationManager = configurationManager;
        this.artifactoryHome = artifactoryHome;
        this.vp = vp;
        // TODO [shayb] Talk with Yossi about this hack
        rollbackBlocker = createRollbackPreventer();
        // This converter runs on all nodes before Artifactory initialization
        converters.get(PRE_INIT).add(new PreInitConverter(artifactoryHome));
        //This converter run once (on the first node that start after upgrade)
        converters.get(HOME_SYNC_FILES).add(new SharedEnvironmentConverter(artifactoryHome));
        // This converter runs on all nodes after Artifactory initialization
        converters.get(HOME_FILES).add(new LoggingConverter(artifactoryHome.getEtcDir()));
        converters.get(HOME_FILES).add(new NoNfsClusterHomeConverter(artifactoryHome));
        //This converter run once after full start
        converters.get(POST_INIT).add(new PostInitConverterAdapter());
    }

    private ConverterBlocker createRollbackPreventer() {
        try {
            return (ConverterBlocker) Class.forName("org.artifactory.addon.ConverterBlockerImpl").newInstance();
        } catch (ClassNotFoundException e) {
            // We're in OSS no need for any action
            return (artifactoryHome, configurationManager) -> false;
        } catch (Exception e) {
            String msg = "Unable to initialize the ConverterBlocker which verifies if the instance is ready for conversion";
            log.error(msg, e.getMessage());
            log.debug(msg, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Converters for db-synced files. in normal usecases only the master will run this, nodes will retrieve from db.
     */
    @Override
    public void convertHomeSync() {
        homeSyncConversionRunning = convert(HOME_SYNC_FILES);
    }

    /**
     * Converters for stuff like logback which is not synced, every node may run this based on the 'originalVersion'
     * info it gets from the artifactory.properties file.
     */
    @Override
    public void convertHome() {
        homeConversionRunning = convert(HOME_FILES);
    }

    /**
     * <b> Local Environment converters always run! they should be smart enough to know when and if to run. </b>
     */
    @Override
    public void convertPreInit() {
        preInitConversionRunning = convert(PRE_INIT);
    }

    private boolean convert(ConverterType type) {
        try {
            CompoundVersionDetails originalVersion = getOriginalVersion(type);
            CompoundVersionDetails running = vp.getRunning();
            if (convertersInterested(converters.get(type), originalVersion)) {
                // Before we perform the actual convert, we check if it is allowed using the ConverterBlocker interface.
                if ((ConverterType.HOME_FILES == type || ConverterType.HOME_SYNC_FILES == type) &&
                        rollbackBlocker.shouldBlockConvert(artifactoryHome, configurationManager)) {
                    throw new RuntimeException("Converter can't run since no matching license found, please add new license");
                }
                String fromVersion = getFromVersion(originalVersion);
                log.info("Triggering " + type.name() + " conversion, from {} to {}", fromVersion, running.getVersion());
                runConverters(converters.get(type), originalVersion, running);
                log.info("Finished " + type.name() + " conversion, current version is: {}", running.getVersion());
                return true;
            }
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }

    @Override
    public void serviceConvert(ArtifactoryConverter artifactoryConverter) {
        try {
            if (artifactoryConverter.isInterested(vp.getOriginalDbVersion(), vp.getRunning())) {
                if(rollbackBlocker.shouldBlockConvert(artifactoryHome, configurationManager)){
                    throw new RuntimeException("Converter can't run since no matching license found, please add new license");
                }
                assertDatabaseConversionOnPrimaryOnly();
                CompoundVersionDetails running = vp.getRunning();
                CompoundVersionDetails originalService = vp.getOriginalDbVersion();
                log.debug("Starting ReloadableBean conversion for: {}, from {} to {}",
                        artifactoryConverter.getClass().getName(), originalService, running);
                databaseConversionRunning = true;
                artifactoryConverter.convert(originalService, running);
                log.debug("Finished ReloadableBean conversion for: {}", artifactoryConverter.getClass().getName());
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void afterServiceConvert() {
        postInitConversionRunning = convert(POST_INIT);
        if (isConverting()) {
            try {
                // HA etc cluster home or DB for non HA or primary only
                if (isNonHaOrConfiguredPrimary()) {
                    //Insert the new version to the database only if have to
                    ArtifactoryCommonDbPropertiesService dbPropertiesService = ContextHelper.get().beanForType(
                            ArtifactoryCommonDbPropertiesService.class);
                    if (isDatabaseConversionInterested()) {
                        log.info("Updating database properties to running version {}", vp.getRunning());
                        dbPropertiesService.updateDbVersionInfo(createDbVersionInfoFromVersion(vp.getRunning()));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to finish conversion", e);
            }
        }
    }

    @Override
    public void afterContextReady() {
        if (isConverting()) {
            homeSyncConversionRunning = false;
            homeConversionRunning = false;
            databaseConversionRunning = false;
            postInitConversionRunning = false;
            preInitConversionRunning = false;
        }
        artifactoryHome.writeArtifactoryProperties();
    }

    private void handleException(Exception e) {
        if (e instanceof CronConfigurationException) {
            // we let saving cron (even if its no longer valid/malformed)
            // rather than fail upgrade, later on user can change it manually
            log.warn(e.getMessage());
            log.debug("{}", e);
        } else {
            String errMsg = e.getCause() != null ? e.getMessage() + " : " + e.getCause().getMessage() : e.getMessage();
            log.error("Conversion failed. You should analyze the error and retry launching Artifactory. Error is: {}",
                    errMsg);
            homeSyncConversionRunning = false;
            homeConversionRunning = false;
            databaseConversionRunning = false;
            postInitConversionRunning = false;
            preInitConversionRunning = false;
            throw new RuntimeException(errMsg, e);
        }
    }

    private DbVersionInfo createDbVersionInfoFromVersion(CompoundVersionDetails versionDetails) {
        long installTime = System.currentTimeMillis();
        return new DbVersionInfo(installTime,
                versionDetails.getVersionName(),
                (int) versionDetails.getRevision(),
                versionDetails.getTimestamp()
        );
    }

    @Override
    public boolean isConverting() {
        return homeSyncConversionRunning || homeConversionRunning || preInitConversionRunning ||
                databaseConversionRunning || postInitConversionRunning;
    }

    private boolean convertersInterested(List<ArtifactoryConverterAdapter> converters, CompoundVersionDetails originalVersion) {
        CompoundVersionDetails toVersion = vp.getRunning();
        for (ArtifactoryConverterAdapter converter : converters) {
            if (converter.isInterested(originalVersion, toVersion)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDatabaseConversionInterested() {
        return vp.getOriginalDbVersion() != null && !vp.getOriginalDbVersion().isCurrent();
    }

    private void runConverters(List<ArtifactoryConverterAdapter> converters, CompoundVersionDetails fromVersion,
            CompoundVersionDetails toVersion) {
        for (ArtifactoryConverterAdapter converter : converters) {
            if (converter.isInterested(fromVersion, toVersion)) {
                converter.backup();
                converter.convert(fromVersion, toVersion);
            }
        }
    }

    private void assertDatabaseConversionOnPrimaryOnly() {
        if (artifactoryHome.isHaConfigured() && !isConfiguredPrimary()) {
            throw new RuntimeException("Stopping Artifactory, couldn't start Artifactory upgrade, on slave node!\n" +
                    "Please run Artifactory upgrade on the master first!");
        }
    }

    private boolean isNonHaOrConfiguredPrimary() {
        return (!artifactoryHome.isHaConfigured() || isConfiguredPrimary());
    }

    private boolean isConfiguredPrimary() {
        return artifactoryHome.isHaConfigured() && artifactoryHome.getHaNodeProperties() != null
                && artifactoryHome.getHaNodeProperties().isPrimary();
    }

    private String getFromVersion(CompoundVersionDetails originalVersion) {
        String fromVersion = "new installation";
        if (originalVersion != null) {
            fromVersion = originalVersion.getVersion().getVersion();
        }
        return fromVersion;
    }

    public Map<ConverterType, ArrayList<ArtifactoryConverterAdapter>> getConverters() {
        return converters;
    }

    /**
     * Inits the converters map
     */
    private void initConvertersMap() {
        converters = Maps.newHashMap();
        converters.put(HOME_FILES, new ArrayList<>());
        converters.put(HOME_SYNC_FILES, new ArrayList<>());
        converters.put(POST_INIT, new ArrayList<>());
        converters.put(PRE_INIT, new ArrayList<>());
    }

    public void addPreInitConverter(ArtifactoryConverterAdapter converterAdapter) {
        converters.get(PRE_INIT).add(converterAdapter);
    }

    public void addHomeConverter(ArtifactoryConverterAdapter converterAdapter) {
        converters.get(HOME_FILES).add(converterAdapter);
    }

    public void addSyncFilesConverter(ArtifactoryConverterAdapter converterAdapter) {
        converters.get(HOME_SYNC_FILES).add(converterAdapter);
    }

    @Override
    public void assertPreInitNoConversionBlock() {
        assertNoConversionBlock(ConverterType.PRE_INIT);
        assertNoConversionBlock(ConverterType.HOME_FILES);
    }

    @Override
    public void assertPostInitNoConversionBlock() {
        assertNoConversionBlock(ConverterType.HOME_SYNC_FILES);
        assertNoConversionBlock(ConverterType.POST_INIT);
    }

    private void assertNoConversionBlock(ConverterType converterType) {
        CompoundVersionDetails fromVersion = getOriginalVersion(converterType);
        CompoundVersionDetails toVersion = vp.getRunning();
        try {
            converters.get(converterType).stream()
                    .filter(converter -> converter.isInterested(fromVersion, toVersion))
                    .forEach(converter -> converter
                            .assertConversionPrecondition(artifactoryHome, fromVersion, toVersion));
        } catch (ConverterPreconditionException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    private CompoundVersionDetails getOriginalVersion(ConverterType type) {
        switch (type) {
            case PRE_INIT:
            case HOME_FILES:
                return vp.getOriginalHomeVersion();
            case HOME_SYNC_FILES:
            case POST_INIT:
                return vp.getOriginalDbVersion();
            default:
                throw new UnsupportedOperationException(
                        "No original version can be found for type " + type.name() + " conversion");
        }
    }
}
