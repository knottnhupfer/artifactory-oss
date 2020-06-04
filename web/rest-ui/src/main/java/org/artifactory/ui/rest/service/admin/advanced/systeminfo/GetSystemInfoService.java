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

package org.artifactory.ui.rest.service.admin.advanced.systeminfo;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.info.InfoWriter;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.storage.StorageService;
import org.artifactory.ui.rest.model.admin.advanced.systeminfo.SystemInfo;
import org.jfrog.common.Strings;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.management.*;
import java.util.*;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSystemInfoService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetSystemInfo");
        SystemInfo systemInfo = new SystemInfo();
        // collect system info
        collectSystemInfo(systemInfo.getSystemInfo());
        // update response with system info
        response.iModel(systemInfo);
    }

    /**
     * Return a formatted string of the system info to display
     */
    private void collectSystemInfo(Map<String, Map<String, String>> systemInfo) {
        Map<String, String> storageInfo = new LinkedHashMap<>();
        // update storage info
        updateStorageInfo(systemInfo, storageInfo);
        // update system properties
        updateSystemProperties(systemInfo);
        // update jvm info
        updateJvmInfo(systemInfo);
        // update vm args
        updateVmArgs(systemInfo);
        // update plugins status
        updatePluginsInfo(systemInfo);
    }

    /**
     * update system info with  vm args
     *
     * @param systemInfo - storage info map
     */
    private void updateVmArgs(Map<String, Map<String, String>> systemInfo) {
        Map<String, String> vmArgs = new LinkedHashMap<>();
        RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
        StringBuilder vmArgumentBuilder = new StringBuilder();
        List<String> vmArguments = RuntimemxBean.getInputArguments();
        if (vmArguments != null) {
            for (String vmArgument : vmArguments) {
                if (InfoWriter.shouldMaskValue(vmArgument)) {
                    vmArgument = Strings.maskKeyValue(vmArgument);
                }
                vmArgumentBuilder.append(vmArgument);
                if (vmArguments.indexOf(vmArgument) != (vmArguments.size() - 1)) {
                    vmArgumentBuilder.append("\n");
                }
            }
        }
        vmArgs.put("Args", vmArgumentBuilder.toString());
        systemInfo.put("JVM Arguments", vmArgs);
    }

    /**
     * update system info with jvm info
     *
     * @param systemInfo - storage info map
     */
    private void updateJvmInfo(Map<String, Map<String, String>> systemInfo) {
        Map<String, String> generalJvmInfo = new LinkedHashMap<>();
        OperatingSystemMXBean systemBean = ManagementFactory.getOperatingSystemMXBean();
        generalJvmInfo.put("Available Processors", Integer.toString(systemBean.getAvailableProcessors()));
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
        generalJvmInfo.put("Heap Memory Usage-Committed", Long.toString(heapMemoryUsage.getCommitted()));
        generalJvmInfo.put("Heap Memory Usage-Init", Long.toString(heapMemoryUsage.getInit()));
        generalJvmInfo.put("Heap Memory Usage-Max", Long.toString(heapMemoryUsage.getMax()));
        generalJvmInfo.put("Heap Memory Usage-Used", Long.toString(heapMemoryUsage.getUsed()));
        MemoryUsage nonHeapMemoryUsage = memoryBean.getNonHeapMemoryUsage();
        generalJvmInfo.put("Non-Heap Memory Usage-Committed", Long.toString(nonHeapMemoryUsage.getCommitted()));
        generalJvmInfo.put("Non-Heap Memory Usage-Init", Long.toString(nonHeapMemoryUsage.getInit()));
        generalJvmInfo.put("Non-Heap Memory Usage-Max", Long.toString(nonHeapMemoryUsage.getMax()));
        generalJvmInfo.put("Non-Heap Memory Usage-Used", Long.toString(nonHeapMemoryUsage.getUsed()));
        systemInfo.put("General JVM Info", generalJvmInfo);
    }

    /**
     * update system properties info
     */
    private void updateSystemProperties(Map<String, Map<String, String>> systemInfo) {
        Map<String, String> systemProperties = new LinkedHashMap<>();
        Properties properties = System.getProperties();
        properties.setProperty(ConstantValues.artifactoryVersion.getPropertyName(),
                ConstantValues.artifactoryVersion.getString());
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        systemProperties.put("artifactory.running.mode", addonsManager.getArtifactoryRunningMode().name());
        systemProperties.put("artifactory.running.state", ContextHelper.get().isOffline() ? "Offline" : "Online");
        addFromProperties(properties, systemProperties);
        addHaProperties(systemProperties);
        systemInfo.put("System Properties", systemProperties);
    }

    /**
     * update system info with storage info
     *
     * @param systemInfo  - system info
     * @param storageInfo - storage info
     */
    private void updateStorageInfo(Map<String, Map<String, String>> systemInfo, Map<String, String> storageInfo) {
        ArtifactoryDbProperties dbProperties = ContextHelper.get().beanForType(ArtifactoryDbProperties.class);
        storageInfo.put("Database Type", dbProperties.getDbType().toString());

        storageInfo.put("Connection Url", dbProperties.getConnectionUrl());
        systemInfo.put("Storage Info", storageInfo);

        StorageService storageService = ContextHelper.get().beanForType(StorageService.class);
        storageInfo.put("Storage Type", storageService.getBinaryProviderInfo().template);
    }

    private void updatePluginsInfo(Map<String, Map<String, String>> systemInfo) {
        Map<String, String> pluginsStatus = ContextHelper.get().beanForType(AddonsManager.class)
                .addonByType(PluginsAddon.class).getPluginsStatus();
        if (!pluginsStatus.isEmpty()) {
            systemInfo.put("User Plugins Status", pluginsStatus);
        }
    }

    /**
     * add properties to system info
     *
     * @param properties       = system properties
     * @param systemProperties - system info map
     */
    private void addFromProperties(Properties properties, Map<String, String> systemProperties) {
        TreeSet sortedSystemPropKeys = new TreeSet<>(properties.keySet());
        for (Object key : sortedSystemPropKeys) {
            systemProperties.put(String.valueOf(key), String.valueOf(properties.get(key)));
        }
    }

    /**
     * update ha properties to system info
     *
     * @param systemProperties - system info map
     */
    private void addHaProperties(Map<String, String> systemProperties) {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        if (artifactoryHome.isHaConfigured()) {
            HaNodeProperties haNodeProperties = artifactoryHome.getHaNodeProperties();
            if (haNodeProperties != null) {
                addFromProperties(haNodeProperties.getProperties(), systemProperties);
            }
        }
    }
}
