package org.artifactory.support.core.collectors.system;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.state.model.StateInitializer;
import org.jfrog.support.common.core.collectors.system.info.SystemInfoCollector;

import java.util.Map;

/**
 * @author Tamir Hadad
 */
public class ArtifactorySystemInfoCollector extends SystemInfoCollector {

    protected void addProductCustomData(Map<String, Multimap<String, String>> info) {
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        Map<String, Map<String, String>> pluginsStatus = artifactoryContext.beanForType(AddonsManager.class)
                .addonByType(PluginsAddon.class)
                .getPluginsInfoSupportBundleDumpAsMap();

        StateInitializer stateInitializer = artifactoryContext.beanForType(StateInitializer.class);
        Map<String, Map<String, String>> stateInfo = stateInitializer.getSupportBundleDump();
        for (Map.Entry<String, Map<String, String>> pluginInfo : pluginsStatus.entrySet()) {
            info.put(pluginInfo.getKey(), ImmutableMultimap.copyOf(pluginInfo.getValue().entrySet()));
        }
        for (Map.Entry<String, Map<String, String>> state : stateInfo.entrySet()) {
            info.put(state.getKey(), ImmutableMultimap.copyOf(state.getValue().entrySet()));
        }
    }
}
