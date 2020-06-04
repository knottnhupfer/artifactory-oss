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

package org.artifactory.metrics.providers.features;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.api.callhome.FeatureGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *
 * This class represent the plugins feature group of the CallHome feature
 *
 * Created by royz on 11/05/2017.
 */
@Component
public class PluginsFeature implements CallHomeFeature {

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public FeatureGroup getFeature() {

        FeatureGroup pluginsFeature = new FeatureGroup("plugins");
        Map<String, String> plugins = addonsManager.addonByType(PluginsAddon.class).getPluginsStatus();
        if (!plugins.isEmpty()) {
            pluginsFeature.addFeatureAttribute("number_of_plugins", plugins.keySet().size());
            addPluginsStatus(pluginsFeature, plugins);
        }
        else
            pluginsFeature.addFeatureAttribute("number_of_plugins", 0);

        return pluginsFeature;
    }

    /**
     * Get plugins' statuses
     *
     * @param pluginsFeature that holds the entire security features
     */
    private void addPluginsStatus(FeatureGroup pluginsFeature, Map<String, String> plugins){

        for (String plugin : plugins.keySet()) {
            FeatureGroup feature = new FeatureGroup(plugin);
            feature.addFeatureAttribute("plugin_status", plugins.get(plugin));
            pluginsFeature.addFeature(feature);
        }
    }
}
