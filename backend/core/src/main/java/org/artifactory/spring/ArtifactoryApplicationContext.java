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

package org.artifactory.spring;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffResult;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddonsImpl;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.WebstartAddon;
import org.artifactory.addon.ha.ClusterOperationsService;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.keys.KeysAddon;
import org.artifactory.addon.license.EdgeAddon;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.addon.replicator.ReplicatorAddon;
import org.artifactory.addon.signed.url.SignedUrlAddon;
import org.artifactory.addon.smartrepo.SmartRepoAddon;
import org.artifactory.addon.support.SupportAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.keys.TrustedKeysService;
import org.artifactory.api.release.bundle.ReleaseBundleService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.build.InternalBuildService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.converter.ConverterManager;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.importexport.ImportIsDisabledException;
import org.artifactory.logging.LoggingService;
import org.artifactory.repo.service.ExportJob;
import org.artifactory.repo.service.ImportJob;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.common.BaseSettings;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.schedule.TaskCallback;
import org.artifactory.schedule.TaskService;
import org.artifactory.security.ArtifactoryEncryptionService;
import org.artifactory.security.access.AccessService;
import org.artifactory.state.model.ArtifactoryStateManager;
import org.artifactory.storage.binstore.service.BinaryService;
import org.artifactory.storage.db.DBChannelService;
import org.artifactory.storage.security.service.AclStoreService;
import org.artifactory.update.utils.BackupUtils;
import org.artifactory.util.ZipUtils;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.artifactory.version.VersionProvider;
import org.artifactory.webapp.servlet.BasicConfigManagers;
import org.jfrog.client.util.PathUtils;
import org.jfrog.common.config.diff.DataDiff;
import org.jfrog.common.config.diff.DiffMerger;
import org.jfrog.common.config.diff.DiffUtils;
import org.jfrog.common.logging.logback.servlet.LogbackConfigListenerBase;
import org.jfrog.common.logging.logback.servlet.LogbackConfigManager;
import org.jfrog.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newLinkedList;
import static org.artifactory.common.ArtifactoryHome.*;
import static org.jfrog.common.FileUtils.handleSecretFileRemovalIfExist;
import static org.jfrog.security.file.SecurityFolderHelper.PERMISSIONS_MODE_755;
import static org.jfrog.security.file.SecurityFolderHelper.setPermissionsOnSecurityFolder;

/**
 * @author Yoav Landman
 */
public class ArtifactoryApplicationContext extends ClassPathXmlApplicationContext
        implements InternalArtifactoryContext, WebApplicationContext {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryApplicationContext.class);

    private static final String CURRENT_TIME_EXPORT_DIR_NAME = "current";
    private final BasicConfigManagers basicConfigManagers;
    private final String contextId;
    private final SpringConfigPaths springConfigPaths;
    private Set<Class<? extends ReloadableBean>> toInitialize = new HashSet<>();
    private ConcurrentHashMap<Class, Object> beansForType = new ConcurrentHashMap<>();
    private List<ReloadableBean> reloadableBeans;
    private volatile boolean ready;
    private long started;
    private boolean offline;
    private ServletContext servletContext;

    /**
     * Main artifactory context. This constructor is called by reflection by the context initializer.
     */
    public ArtifactoryApplicationContext(String contextId, SpringConfigPaths springConfigPaths,
            BasicConfigManagers basicConfigManagers, ServletContext servletContext) throws BeansException {
        super(springConfigPaths.getAllPaths(), false, null);
        this.contextId = contextId;
        this.basicConfigManagers = basicConfigManagers;
        this.springConfigPaths = springConfigPaths;
        this.servletContext = servletContext;
        this.started = System.currentTimeMillis();
        refresh();
        contextCreated();
    }

    @Override
    public ArtifactoryHome getArtifactoryHome() {
        return basicConfigManagers.artifactoryHome;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    @Override
    public String getDisplayName() {
        return contextId;
    }

    @Override
    public SpringConfigPaths getConfigPaths() {
        return springConfigPaths;
    }

    @Override
    public String getServerId() {
        //For a cluster node take it from the cluster property, otherwise use the license hash
        HaNodeProperties HaNodeProperties = getArtifactoryHome().getHaNodeProperties();
        if (HaNodeProperties != null) {
            return HaNodeProperties.getServerId();
        }
        return HaCommonAddon.ARTIFACTORY_PRO;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public void setOffline() {
        this.offline = true;
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return basicConfigManagers.configurationManager;
    }

    @Override
    public ConverterManager getConverterManager() {
        return basicConfigManagers.convertersManager;
    }

    @Override
    public VersionProvider getVersionProvider() {
        return basicConfigManagers.versionProvider;
    }

    @Override
    public LogbackConfigManager getLogbackConfigManager() {
        return (LogbackConfigManager)servletContext.getAttribute(LogbackConfigListenerBase.class.getName() + ".ATTR_LOGBACK_CONFIG_MANAGER");
    }

    @Override
    public long getUptime() {
        return System.currentTimeMillis() - started;
    }

    @Override
    public CentralConfigService getCentralConfig() {
        return beanForType(CentralConfigService.class);
    }

    @Override
    public SecurityService getSecurityService() {
        return beanForType(SecurityService.class);
    }

    @Override
    public AuthorizationService getAuthorizationService() {
        return beanForType(AuthorizationService.class);
    }

    @Override
    public TaskService getTaskService() {
        return beanForType(TaskService.class);
    }

    @Override
    public RepositoryService getRepositoryService() {
        return beanForType(InternalRepositoryService.class);
    }

    @Override
    public void addReloadableBean(Class<? extends ReloadableBean> beanClass) {
        toInitialize.add(beanClass);
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        try {
            setReady(false, "refresh");
            beansForType.clear();
            ArtifactoryContextThreadBinder.bind(this);
            super.refresh();
            reloadableBeans = new ArrayList<>(toInitialize.size());
            Set<Class<? extends ReloadableBean>> toInit = new HashSet<>(toInitialize);
            for (Class<? extends ReloadableBean> beanClass : toInitialize) {
                orderReloadableBeans(toInit, beanClass);
            }
            log.debug("Reloadable list of beans: {}", reloadableBeans);
            log.info("Artifactory context starting up {} Spring Beans...", reloadableBeans.size());
            // db should have been initialized by now -> we can read the db version
            getVersionProvider().init(getConfigurationManager().getDBChannel());
            for (ReloadableBean reloadableBean : reloadableBeans) {
                String beanIfc = getInterfaceName(reloadableBean);
                log.debug("Initializing {}", beanIfc);
                getConverterManager().serviceConvert(reloadableBean);
                try {
                    reloadableBean.init();
                } catch (Exception e) {
                    throw new BeanInitializationException("Failed to initialize bean '" + beanIfc + "'.", e);
                }
                log.debug("Initialized {}", beanIfc);
            }
            getConverterManager().afterServiceConvert();
            AddonsManager addonsManager = beanForType(AddonsManager.class);
            addonsManager.prepareAddonManager();
            setReady(true, "refresh");
            getConverterManager().afterContextReady();
        } finally {
            ArtifactoryContextThreadBinder.unbind();
        }
    }

    private void contextCreated() {
        try {
            ArtifactoryContextThreadBinder.bind(this);
            Map<String, ContextCreationListener> contextCreationListeners =
                    beansForType(ContextCreationListener.class);
            log.debug("Signaling context created to context readiness listener beans.");
            for (ContextCreationListener bean : contextCreationListeners.values()) {
                String beanIfc = getInterfaceName(bean);
                log.debug("Signaling context created to {}.", beanIfc);
                bean.onContextCreated();
            }
            handleSecretFileRemovalIfExist(new File(getArtifactoryHome().getEtcDir(), ".secrets/.temp.db.properties"));
        } finally {
            ArtifactoryContextThreadBinder.unbind();
        }
    }

    @Override
    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.prepareBeanFactory(beanFactory);
        //Add our own post processor that registers all reloadable beans auto-magically after construction
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                Class<?> targetClass = AopUtils.getTargetClass(bean);
                if (ReloadableBean.class.isAssignableFrom(targetClass)) {
                    Reloadable annotation;
                    if (targetClass.isAnnotationPresent(Reloadable.class)) {
                        annotation = targetClass.getAnnotation(Reloadable.class);
                        Class<? extends ReloadableBean> beanClass = annotation.beanClass();
                        addReloadableBean(beanClass);
                    } else {
                        throw new IllegalStateException("Bean " + targetClass.getName() +
                                " requires initialization beans to be initialized, but no such beans were found");
                    }
                }
                return bean;
            }

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                //Do nothing
                return bean;
            }
        });
    }

    @Override
    public void init() {
        // Nothing
    }

    @Override
    public void destroy() {
        setReady(false, "destroy");
        ArtifactoryContextThreadBinder.bind(this);
        ArtifactoryHome.bind(getArtifactoryHome());
        try {
            try {
                // First shutdown the config manager
                if (basicConfigManagers.configurationManager != null) {
                    getConfigurationManager().destroy();
                }
                if (reloadableBeans != null && !reloadableBeans.isEmpty()) {
                    // TODO[By Gidi] find better way to update the ArtifactoryStateManager on beforeDestroy event
                    beanForType(ArtifactoryStateManager.class).beforeDestroy();
                    log.info("Destroying {} Artifactory Spring Beans", reloadableBeans.size());
                    for (int i = reloadableBeans.size() - 1; i >= 0; i--) {
                        ReloadableBean bean = reloadableBeans.get(i);
                        String beanIfc = getInterfaceName(bean);
                        log.debug("Destroying {}", beanIfc);
                        try {
                            bean.destroy();
                        } catch (Exception e) {
                            if (log.isDebugEnabled() || ConstantValues.test.getBoolean()) {
                                log.error("Exception while destroying bean '" + beanIfc + "'.", e);
                            } else {
                                log.error("Exception while destroying {} ({}).", beanIfc, e.getMessage());
                            }
                        }
                        log.debug("Destroyed {}", beanIfc);
                    }
                }
                beanForType(AclStoreService.class).destroy();
            } finally {
                super.destroy();
            }
        } finally {
            ArtifactoryContextThreadBinder.unbind();
            ArtifactoryHome.unbind();
        }
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        // Not the job of reload to set to ready state if it was not before => Leave state unchanged
        boolean wasReady = isReady();
        if (wasReady) {
            setReady(false, "reload");
        }
        try {
            CentralConfigDescriptor newDescriptor = getCentralConfig().getDescriptor();

            DiffResult diffs = null;
            if (oldDescriptor != null) {
                diffs = getCentralConfig().findDiff(oldDescriptor, newDescriptor);
            }

            Set<String> updatedPaths = null;
            if (diffs != null) {
                updatedPaths = DiffUtils.hierarchicalPaths(diffs.getDiffs(), Diff::getFieldName).keySet();
                if (log.isDebugEnabled()) {
                    log.debug("Reloading for paths: {}", updatedPaths.stream().limit(10).collect(Collectors.toList()));
                }
            }

            log.debug("Reloading beans: {}", reloadableBeans);
            for (ReloadableBean reloadableBean : reloadableBeans) {
                CentralConfigKey[] reloadKeys = getReloadableAnnotation(reloadableBean).listenOn();
                long l = System.currentTimeMillis();
                boolean callReload = shouldCallReload(updatedPaths, reloadKeys);
                if (callReload) {
                    String beanIfc = getInterfaceName(reloadableBean);
                    log.debug("Reloading {}", beanIfc);
                    reloadableBean.reload(oldDescriptor,
                            diffs == null ? ImmutableList.of() : DiffMerger.diffToDataDiff(diffs.getDiffs()));
                    log.debug("Reloaded");
                }
                log.debug("Reloaded {} in {}. called: {}", getInterfaceName(reloadableBean),
                        (System.currentTimeMillis() - l),
                        callReload);
            }
        } catch (RuntimeException e) {
            // Reset ready to true to make sure context still works
            setReady(true, "reload on error: " + e.getMessage());
            throw e;
        }
        if (wasReady) {
            // TODO: [by fsi] Used to be set to true even in the middle of a non ready start call
            setReady(true, "reload");
        }
    }

    private boolean shouldCallReload(Set<String> diffs, CentralConfigKey[] reloadKeys) {
        if (diffs == null || reloadKeys.length == 0) {
            return true;
        }

        Set<String> keys = Stream.of(reloadKeys).map(CentralConfigKey::getKey).collect(Collectors.toSet());
        return diffs.stream().anyMatch(keys::contains);
    }

    private String getInterfaceName(Object bean) {
        return bean.getClass().getInterfaces()[0].getName();
    }

    private void orderReloadableBeans(Set<Class<? extends ReloadableBean>> beansLeftToInit,
            Class<? extends ReloadableBean> beanClass) {
        if (!beansLeftToInit.contains(beanClass)) {
            // Already done
            return;
        }
        ReloadableBean initializingBean = beanForType(beanClass);
        Class<? extends ReloadableBean>[] dependsUpon = getReloadableAnnotation(initializingBean).initAfter();
        for (Class<? extends ReloadableBean> doBefore : dependsUpon) {
            //Sanity check that prerequisite bean was registered
            //Sorry, really need this hack here.
            if (!toInitialize.contains(doBefore) && !(ClusterOperationsService.class.isAssignableFrom(doBefore))) {
                throw new IllegalStateException("Bean '" + beanClass.getName() + "' requires bean '"
                        + doBefore.getName() + "' to be initialized, but no such bean is registered for init.");
            }
            if (!doBefore.isInterface()) {
                throw new IllegalStateException("Cannot order bean with implementation class.\n" +
                        " Please provide an interface extending " + ReloadableBean.class.getName());
            }
            orderReloadableBeans(beansLeftToInit, doBefore);
        }
        // Avoid double init
        if (beansLeftToInit.remove(beanClass)) {
            reloadableBeans.add(initializingBean);
        }
    }

    private Reloadable getReloadableAnnotation(ReloadableBean initializingBean) {
        Reloadable annotation;
        Class<?> targetClass = AopUtils.getTargetClass(initializingBean);

        if (targetClass.isAnnotationPresent(Reloadable.class)) {
            annotation = targetClass.getAnnotation(Reloadable.class);
        } else {
            throw new IllegalStateException(
                    "Bean " + targetClass.getName() + " requires the @Reloadable annotation to be present.");
        }
        return annotation;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    private void setReady(boolean ready, String actionName) {
        if (ready && this.ready && (ConstantValues.dev.getBoolean() || ConstantValues.devHa.getBoolean() ||
                ConstantValues.test.getBoolean())) {
            Exception err = new Exception(
                    "Artifactory application context action '" + actionName + "' was already set to READY");
            log.error(err.getMessage(), err);
            return;
        }
        this.ready = ready;
        if (ready && hasBeanFactory()) {
            //Signal to all the context ready listener beans
            final Map<String, ContextReadinessListener> contextReadinessListeners =
                    beansForType(ContextReadinessListener.class);
            log.debug("Signaling context ready from {} to context readiness listener beans.", actionName);
            for (ContextReadinessListener bean : contextReadinessListeners.values()) {
                String beanIfc = getInterfaceName(bean);
                log.debug("Signaling context ready from {} to bean {}.", actionName, beanIfc);
                bean.onContextReady();
            }
            // Init configuration manager channels
            getConfigurationManager().setPermanentLogChannel();
            beanForType(AddonsManager.class).addonByType(HaAddon.class).initConfigBroadcast(this);
            DBChannelService dbChannelService = beanForType(DBChannelService.class);
            getConfigurationManager().setPermanentDBChannel(dbChannelService);
        }
        log.info("Artifactory application context set to {} by {}", (ready ? "READY" : "NOT READY"), actionName);
    }

    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public <T> T beanForType(Class<T> type) {
        //No sync needed. Sync is done on write, so in the worst case we might end up with
        //a bean with the same value, which is fine
        T bean = (T) beansForType.get(type);
        if (bean == null) {
            Map<String, T> beans = getBeansOfType(type);
            if (beans.isEmpty()) {
                throw new RuntimeException("Could not find bean of type '" + type.getName() + "'.");
            }

            bean = beans.values().iterator().next(); // default to the first bean encountered
            if (beans.size() > 1) {
                // prefer beans marked as primary
                for (Map.Entry<String, T> beanEntry : beans.entrySet()) {
                    BeanDefinition beanDefinition = getBeanFactory().getBeanDefinition(beanEntry.getKey());
                    if (isEdgeAddonAndJcrVersion(type, beanDefinition) || isSmartRemoteAddonAndJcrVersion(type, beanDefinition) ||  beanDefinition.isPrimary()) {
                        bean = beanEntry.getValue();
                    }
                }
            }
        }
        beansForType.put(type, bean);
        return bean;
    }

    /**
     * EdgeSmartRepoAddonImpl class implemented inside properties addon,
     * In Jcr we need the properties addon only
     */
    private <T> boolean isEdgeAddonAndJcrVersion(Class<T> type, BeanDefinition beanDefinition) {
        return type.getAnnotation(EdgeAddon.class) != null && isJcrRunningMode() && CoreAddonsImpl.class.getName().equals(beanDefinition.getBeanClassName());
    }

    private <T> boolean isSmartRemoteAddonAndJcrVersion(Class<T> type, BeanDefinition beanDefinition) {
        return type == SmartRepoAddon.class && isJcrRunningMode() && !CoreAddonsImpl.class.getName().equals(beanDefinition.getBeanClassName());
    }

    private boolean isJcrRunningMode() {
        return ContextHelper.get().beanForType(AddonsManager.class).getArtifactoryRunningMode().isJcrOrJcrAol();
    }

    @Override
    public <T> Map<String, T> beansForType(Class<T> type) {
        return getBeansOfType(type);
    }

    @Override
    public <T> T beanForType(String name, Class<T> type) {
        return getBean(name, type);
    }

    @Override
    public BinaryService getBinaryStore() {
        return beanForType(BinaryService.class);
    }

    @Override
    public void importFrom(ImportSettings settings) {
        if (!ConstantValues.systemImportEnabled.getBoolean()) {
            throw ImportIsDisabledException.buildSystemException();
        }

        MutableStatusHolder status = settings.getStatusHolder();
        status.status("### Beginning full system import ###", log);
        // First sync status and settings
        status.setFastFail(settings.isFailFast());
        status.setVerbose(settings.isVerbose());
        // First check the version of the folder imported
        ArtifactoryVersion backupVersion = BackupUtils.findVersion(settings.getBaseDir());
        // We don't support import from 125 and below
        ArtifactoryVersion supportFrom = ArtifactoryVersionProvider.v125.get();
        if (backupVersion.before(supportFrom)) {
            throw new IllegalArgumentException("Folder " + settings.getBaseDir().getAbsolutePath() +
                    " contains an export from a version older than " + supportFrom.getVersion() + ".\n" +
                    "Please use the dump-legacy-dbs first, to dump this version's data, then import it " +
                    "into Artifactory.");
        }
        ((ImportSettingsImpl) settings).setExportVersion(backupVersion);
        List<String> stoppedTasks = Lists.newArrayList();
        try {
            stopRelatedTasks(ImportJob.class, stoppedTasks);

            // Access server - needs to be before etc (so we will use old access credentials for the import request)
            // and before security service so that security.xml (if exists) will be imported after access users
            AccessService accessService = beanForType(AccessService.class);
            accessService.importFrom(settings);
            // etc - including importing artifactory key
            importResourcesFromEtcDirectory(settings, accessService.isUsingBundledAccessServer() || settings.isEnableCopySecurityAccessDir());
            // Access service initialization - needs to be after etc so it will use the imported access.creds, artifactory key...
            accessService.afterImport(settings);
            AddonsManager addonsManager = beanForType(AddonsManager.class);
            // In any case we need to delete all bundles upon import, and it needs to be done before importing
            // central configuration, else we won't be able to delete repositories and files
            beanForType(ReleaseBundleService.class).deleteAllBundles();
            beanForType(TrustedKeysService.class).deleteAllTrustedKeys();

            // import central configuration
            getCentralConfig().importFrom(settings);
            // import security settings
            getSecurityService().importFrom(settings);

            // import Trusted Keys
            addonsManager.addonByType(KeysAddon.class).importFrom(settings);
            // import URL signing key
            addonsManager.addonByType(SignedUrlAddon.class).importFrom(settings);
            // import webstart keystore
            addonsManager.addonByType(WebstartAddon.class).importKeyStore(settings);
            // import 3rd party licenses
            addonsManager.addonByType(LicensesAddon.class).importLicenses(settings);
            // import user plugins
            addonsManager.addonByType(PluginsAddon.class).importFrom(settings);
            // import builds
            beanForType(BuildService.class).importFrom(settings);
            // import logback conf
            beanForType(LoggingService.class).importFrom(settings);
            if (!settings.isExcludeContent()) {
                addonsManager.addonByType(SupportAddon.class).deleteAll();
                // import repositories content
                log.info("Importing repositories");
                getRepositoryService().importFrom(settings);
                // import release bundles
                if (!settings.isExcludeArtifactBundles()) {
                    addonsManager.addonByType(ReleaseBundleAddon.class).importFrom(settings);
                }
            }
            addonsManager.addonByType(ReplicatorAddon.class).reCreateReplicatorConfigFile();
            status.status("### Full system import finished ###", log);
        } catch (Exception e) {
            status.error("Failed system import", e, log);
        } finally {
            resumeTasks(stoppedTasks);
        }
    }

    @Override
    public void exportTo(ExportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        status.status("Beginning full system export...", log);
        String timestamp;
        boolean incremental = settings.isIncremental();
        if (!incremental) {
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");
            timestamp = formatter.format(settings.getTime());
        } else {
            timestamp = CURRENT_TIME_EXPORT_DIR_NAME;
        }
        File baseDir = settings.getBaseDir();

        //Only create a temp dir when not performing incremental backup
        File workingExportDir;
        if (incremental) {
            //Will always be baseDir/CURRENT_TIME_EXPORT_DIR_NAME
            workingExportDir = new File(baseDir, timestamp);
        } else {
            workingExportDir = new File(baseDir, timestamp + ".tmp");
            //Make sure the directory does not already exist
            try {
                FileUtils.deleteDirectory(workingExportDir);
            } catch (IOException e) {
                status.error("Failed to delete old temp export directory: " + workingExportDir.getAbsolutePath(), e,
                        log);
                return;
            }
        }
        status.status("Creating temp export directory: " + workingExportDir.getAbsolutePath(), log);
        try {
            FileUtils.forceMkdir(workingExportDir);
        } catch (IOException e) {
            status.error("Failed to create backup dir: " + workingExportDir.getAbsolutePath(), e, log);
            return;
        }
        status.status("Using backup directory: '" + workingExportDir.getAbsolutePath() + "'.", log);

        ExportSettingsImpl exportSettings = new ExportSettingsImpl(workingExportDir, settings);

        List<String> stoppedTasks = Lists.newArrayList();
        try {
            AddonsManager addonsManager = beanForType(AddonsManager.class);

            stopRelatedTasks(ExportJob.class, stoppedTasks);

            // central config
            getCentralConfig().exportTo(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // keystore
            WebstartAddon webstartAddon = addonsManager.addonByType(WebstartAddon.class);
            webstartAddon.exportKeyStore(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // licenses
            LicensesAddon licensesAddon = addonsManager.addonByType(LicensesAddon.class);
            licensesAddon.exportLicenses(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // Access server
            beanForType(AccessService.class).exportTo(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // artifactory.properties and etc files
            exportArtifactoryProperties(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }
            exportEtcDirectory(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // Trusted keys
            addonsManager.addonByType(KeysAddon.class).exportTo(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // URL signing key
            addonsManager.addonByType(SignedUrlAddon.class).exportTo(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // build info
            exportBuildInfo(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // repositories content
            if (settings.isIncludeMetadata() || !settings.isExcludeContent()) {
                getRepositoryService().exportTo(exportSettings);
            }
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // release bundles content
            if (!settings.isExcludeArtifactBundles() && !settings.isExcludeContent()) {
                addonsManager.addonByType(ReleaseBundleAddon.class).exportTo(exportSettings);
                if (status.isError() && settings.isFailFast()) {
                    return;
                }
            }

            if (incremental && settings.isCreateArchive()) {
                log.warn("Cannot create archive for an in place backup.");
            }
            if (!incremental) {
                //Create an archive if necessary
                if (settings.isCreateArchive()) {
                    createArchive(settings, status, timestamp, workingExportDir);
                } else {
                    moveTmpToBackupDir(settings, status, timestamp, workingExportDir);
                }
            } else {
                settings.setOutputFile(workingExportDir);
            }

            settings.cleanCallbacks();

            status.status("Full system export completed successfully.", log);
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            status.error("Full system export failed: " + e.getMessage(), e, log);
        } finally {
            resumeTasks(stoppedTasks);
        }
    }

    private void moveTmpToBackupDir(ExportSettings settings, MutableStatusHolder status, String timestamp,
            File workingExportDir) {
        //Delete any exiting final export dir
        File exportDir = new File(settings.getBaseDir(), timestamp);
        try {
            FileUtils.deleteDirectory(exportDir);
        } catch (IOException e) {
            status.warn("Failed to delete existing final export directory.", e, log);
        }
        //Switch the directories
        try {
            FileUtils.moveDirectory(workingExportDir, exportDir);
        } catch (IOException e) {
            status.error("Failed to move '" + workingExportDir + "' to '" + exportDir + "': " + e.getMessage(), e, log);
        } finally {
            settings.setOutputFile(exportDir);
        }
    }

    private void createArchive(ExportSettings settings, MutableStatusHolder status, String timestamp,
            File workingExportDir) {
        status.status("Creating archive...", log);

        File tempArchiveFile = new File(settings.getBaseDir(), timestamp + ".tmp.zip");
        try {
            ZipUtils.archive(workingExportDir, tempArchiveFile, true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create system export archive.", e);
        }
        //Delete the temp export dir
        try {
            FileUtils.deleteDirectory(workingExportDir);
        } catch (IOException e) {
            log.warn("Failed to delete temp export directory.", e);
        }

        // From now on use only java.io.File for the file actions!

        //Delete any exiting final archive
        File archive = new File(settings.getBaseDir(), timestamp + ".zip");
        if (archive.exists()) {
            boolean deleted = archive.delete();
            if (!deleted) {
                status.warn("Failed to delete existing final export archive.", log);
            }
        }
        //Rename the archive file
        try {
            FileUtils.moveFile(tempArchiveFile, archive);
        } catch (IOException e) {
            status.error(String.format("Failed to move '%s' to '%s'.", tempArchiveFile.getAbsolutePath(),
                    archive.getAbsolutePath()), e, log);
        } finally {
            settings.setOutputFile(archive.getAbsoluteFile());
        }
    }

    private void exportArtifactoryProperties(ExportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        CompoundVersionDetails runningVersion = getArtifactoryHome().getRunningArtifactoryVersion();
        try {
            FileUtils.writeStringToFile(new File(settings.getBaseDir(), "artifactory.properties"),
                    runningVersion.getFileDump());
        } catch (IOException e) {
            status.error("Failed to copy artifactory.properties file", e, log);
        }
    }

    private void exportEtcDirectory(ExportSettings settings) {
        try {
            File targetBackupDir = new File(settings.getBaseDir(), ArtifactoryHome.ETC_DIR_NAME);
            File artifactoryKeyFile = CryptoHelper.getArtifactoryKey(ArtifactoryHome.get());
            // Filter out the licenses (cluster, or single license) and master key from the export
            FileFilter filter = new NotFileFilter(new NameFileFilter(ImmutableList.of(
                    artifactoryKeyFile.getName(),
                    LICENSE_FILE_NAME,
                    CLUSTER_LICENSE_FILE_NAME,
                    MASTER_KEY_DEFAULT_FILE_NAME)));
            // Copy the entire directory
            FileUtils.copyDirectory(getArtifactoryHome().getEtcDir(), targetBackupDir, filter, true);
            // Copy the artifactory key into the target directory renamed to default
            // Order IS IMPORTANT - we copy the directory first, only then the master key
            if (artifactoryKeyFile.exists()) {
                File targetArtifactoryKeyFile = new File(targetBackupDir,
                        SECURITY_DIR_NAME + File.separator + ARTIFACTORY_KEY_DEFAULT_FILE_NAME);
                FileUtils.copyFile(artifactoryKeyFile, targetArtifactoryKeyFile, false);
            }
            // Set permission to 755 so we'll be able to tell that the security directory exists on import
            setPermissionsOnSecurityFolder(targetBackupDir.toPath(), PERMISSIONS_MODE_755);
            log.info("Note: the etc exported folder has excessive permissions. Be careful with the files.");
            checkSecurityFolder(targetBackupDir);
        } catch (IOException e) {
            settings.getStatusHolder().error(
                    "Failed to export etc directory: " + getArtifactoryHome().getEtcDir().getAbsolutePath(), e, log);
        }
    }

    private void checkSecurityFolder(File targetBackupDir) throws IOException {
        File artifactoryKeyDest = new File(targetBackupDir,
                "etc/" + ConstantValues.securityArtifactoryKeyLocation.getDefValue());
        if (artifactoryKeyDest.exists()) {
            setPermissionsOnSecurityFolder(artifactoryKeyDest.getParentFile());
        }
    }

    /**
     * Import selected files from the etc directory. Note that while the export simply copies the etc directory, here we
     * only wish to import some of the files while ignoring others. The reason is that the etc may contain custom
     * settings that are environment dependant (like db configuration) which will fail the import of will fail
     * Artifactory on the next startup. So changes to the repo.xml and/or artifactory.system.properties has to be
     * imported manually.
     *
     * @param settings basic settings with conf files
     * @param importFromAccessClientDir (true for unbundled). a flag that enable copy access client dir (i.e. adminTokenToken, serviceId access.creds)
     */
    private void importResourcesFromEtcDirectory(ImportSettings settings, boolean importFromAccessClientDir) {
        File importEtcDir = new File(settings.getBaseDir(), "etc");
        if (!importEtcDir.exists()) {
            // older versions didn't export the etc directory
            log.info("Skipping etc directory import. File doesn't exist: " + importEtcDir.getAbsolutePath());
            return;
        }
        copyLogo(importEtcDir, settings);
        File destSecurityFolder = new File(getArtifactoryHome().getEtcDir(), SECURITY_DIR_NAME);
        moveSshDirectoryIfNeeded(importEtcDir, destSecurityFolder, settings.getStatusHolder());
        importSecurityDir(importEtcDir, destSecurityFolder, settings, importFromAccessClientDir);
    }

    private void copyLogo(File importEtcDir, ImportSettings settings) {
        File customUiDir = new File(importEtcDir, "ui");
        if (customUiDir.exists()) {
            try {
                FileUtils.copyDirectory(customUiDir, getArtifactoryHome().getLogoDir(), false);
            } catch (IOException e) {
                settings.getStatusHolder().error(
                        "Failed to import ui directory: " + customUiDir.getAbsolutePath(), e, log);
            }
        }
    }

    /**
     * When importing from <5x, move ssh directory if needed
     */
    private void moveSshDirectoryIfNeeded(File importEtcDir, File destEtcSecurityDir,
            MutableStatusHolder statusHolder) {
        File backupSshDir = new File(importEtcDir, "ssh");
        if (backupSshDir.exists()) {
            try {
                FileUtils.copyDirectory(backupSshDir, destEtcSecurityDir);
                setPermissionsOnSecurityFolder(destEtcSecurityDir);
            } catch (IOException e) {
                statusHolder.error("Failed to import ssh directory " + backupSshDir.getAbsolutePath(), e, log);
            }
        }
    }

    private void importSecurityDir(File importEtcDir, File destSecurityFolder, ImportSettings settings, boolean importFromAccessClientDir) {
        File etcSecurityDir = new File(importEtcDir, SECURITY_DIR_NAME);
        if (etcSecurityDir.exists()) {
            try {
                importArtifactoryKeyFile(etcSecurityDir, settings);
                // Filter the keys - artifactory key is imported separately, the communication key should not be imported.
                LinkedList<String> filesNotToCopy = newLinkedList();
                filesNotToCopy.add(COMMUNICATION_KEY_FILE_NAME); //Here in case we missed removing it somehow, can deprecate in a few versions
                filesNotToCopy.add(COMMUNICATION_TOKEN_FILE_NAME);
                filesNotToCopy.add(ARTIFACTORY_KEY_DEFAULT_FILE_NAME);
                filesNotToCopy.add(MASTER_KEY_DEFAULT_FILE_NAME); //Master key is never imported, can be revoked with .import marker in filesystem
                filesNotToCopy.add(TRUSTED_KEYS_FILE_NAME);
                if (!importingAccessUsers(settings)) {
                    filesNotToCopy.add(ACCESS_CLIENT_CREDS_FILE_NAME);
                }
                if (!importFromAccessClientDir) {
                    log.debug("Artifactory is not using its bundled access server - skipping import from Access client dir.");
                    filesNotToCopy.add(ACCESS_CLIENT_DIR_NAME);
                }
                FileFilter filter = new NotFileFilter(new NameFileFilter(filesNotToCopy));
                // TODO: [by fsi] Find a way to copy with permissions kept
                // Don't preserve dates - otherwise the configuration manager will overwrite with the files from the DB.
                FileUtils.copyDirectory(etcSecurityDir, destSecurityFolder, filter, false);
                setPermissionsOnSecurityFolder(destSecurityFolder);
                // Force refresh of encryption wrapper
                getArtifactoryHome().unsetArtifactoryEncryptionWrapper();
            } catch (IOException e) {
                String err = "Failed to import security directory '" + etcSecurityDir.getAbsolutePath() + "': ";
                log.error(err, e);
                settings.getStatusHolder().error(err, e, log);
            }
        } else {
            log.warn("etc/security directory is missing or cannot be accessed during import. This might cause errors " +
                    "in case the exported system was encrypted.");
        }
    }

    /**
     * In case we are going to import a new artifactory key and Artifactory is currently encrypted:
     * - We decrypt db.properties using the current old key
     * - Rename the current old artifactory key and propagate the change
     * - Import the new artifactory key and propagate
     * Note: the artifactory key name in export/import is according to {@link ArtifactoryHome#ARTIFACTORY_KEY_DEFAULT_FILE_NAME}
     * while the current artifactory key file name according to the {@link ConstantValues#securityArtifactoryKeyLocation}
     * We look for the key in the export folder according to the first, save the imported key according to the latter.
     */
    private void importArtifactoryKeyFile(File importEtcSecurityDir, ImportSettings settings) throws IOException {
        File oldArtifactoryKeyFile = CryptoHelper.getArtifactoryKey(ArtifactoryHome.get());
        File newArtifactoryKeyFile = new File(importEtcSecurityDir, ARTIFACTORY_KEY_DEFAULT_FILE_NAME);
        if (newArtifactoryKeyFile.exists()) {
            AccessService accessService = beanForType(AccessService.class);
            ArtifactoryEncryptionService artifactoryEncryptionService = beanForType(ArtifactoryEncryptionService.class);
            // In case Artifactory is already encrypted we first remove the old artifactory key
            if (oldArtifactoryKeyFile.exists()) {
                log.info("Artifactory is currently encrypted. Replacing artifactory key with a new one.");
                if (!importingAccessUsers(settings)) {
                    // We haven't imported access creds, hence we need to decrypt it and encrypt it with the imported key
                    accessService.encryptOrDecrypt(false);
                }
                // Rename the old file
                File renamedKeyFile = CryptoHelper.removeArtifactoryKeyFile(ArtifactoryHome.get());
                // Propagate the change of old artifactory key file
                artifactoryEncryptionService.notifyArtifactoryKeyDeleted(oldArtifactoryKeyFile, renamedKeyFile);
            }
            // Copy the new artifactory key file
            FileUtils.copyFile(newArtifactoryKeyFile, oldArtifactoryKeyFile, false);
            // Propagate the new artifactory key file
            artifactoryEncryptionService.notifyArtifactoryKeyCreated();
            if (!importingAccessUsers(settings)) {
                accessService.encryptOrDecrypt(true);
            }
        }
    }

    /**
     * Only after 5.6.0 imports can override Access users, thus they need to include logic that can overwrite it.
     */
    private boolean importingAccessUsers(ImportSettings settings) {
        return settings instanceof ImportSettingsImpl &&
                ((ImportSettingsImpl) settings).getExportVersion().afterOrEqual(ArtifactoryVersionProvider.v560m001.get());
    }

    private void exportBuildInfo(ExportSettingsImpl exportSettings) {
        MutableStatusHolder status = exportSettings.getStatusHolder();
        if (exportSettings.isExcludeBuilds()) {
            status.status("Skipping build info ...", log);
            return;
        }
        if (beanForType(InternalBuildService.class).isBuildInfoReady()) {
            status.debug("Builds export will done as part of content export ", log);
            return;
        }
        exportSettings.setExcludeBuildInfoRepo(true);

        BuildService build = beanForType(BuildService.class);
        status.status("Exporting build info...", log);
        build.exportTo(exportSettings);
    }

    public List<ReloadableBean> getBeans() {
        return reloadableBeans;
    }

    private void stopRelatedTasks(Class<? extends TaskCallback> jobCommandClass, List<String> stoppedTokens) {
        if (TaskCallback.currentTaskToken() != null) {
            // Already stopped by standard task manager
            return;
        }
        TaskService taskService = getTaskService();
        taskService.stopRelatedTasks(jobCommandClass, stoppedTokens, BaseSettings.FULL_SYSTEM);
    }

    private void resumeTasks(List<String> tokens) {
        if (TaskCallback.currentTaskToken() != null) {
            // Already stopped by standard task manager
            return;
        }
        TaskService taskService = getTaskService();
        tokens.forEach(taskService::resumeTask);
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getContextPath() {
        return PathUtils.trimLeadingSlashes(getServletContext().getContextPath());
    }
}
