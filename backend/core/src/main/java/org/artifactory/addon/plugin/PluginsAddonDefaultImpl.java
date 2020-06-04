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

package org.artifactory.addon.plugin;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.build.staging.BuildStagingStrategy;
import org.artifactory.request.Request;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PluginsAddonDefaultImpl implements PluginsAddon {

    @Override
    public <C> void execPluginActions(Class<? extends PluginAction> type, C context, Object... args) {
        //Nothing to do in the default impl
    }

    @Override
    public ResponseCtx execute(String executionName, String method, Map params, ResourceStreamHandle body,
            boolean async) {
        throw new UnsupportedOperationException("Executing plugin actions requires Artifactory Pro.");
    }

    @Override
    public BuildStagingStrategy getStagingStrategy(String strategyName, String buildName, Map params) {
        throw new UnsupportedOperationException("Build staging strategy actions requires Artifactory Pro.");
    }

    @Override
    public Map<String, List<PluginInfo>> getPluginInfo(@Nullable String pluginType) {
        return Maps.newHashMap();
    }

    @Override
    public ResponseCtx promote(String promotionName, String buildName, String buildNumber, Map params) {
        throw new UnsupportedOperationException("Build promotion actions requires Artifactory Pro.");
    }

    @Override
    public ResponseCtx deployPlugin(Reader pluginContent, String scriptName) {
        throw new UnsupportedOperationException("Deploying user plugins requires Artifactory Pro.");
    }

    @Override
    public ResponseCtx reloadPlugins(boolean wait) {
        throw new UnsupportedOperationException("Reloading user plugins requires Artifactory Pro.");
    }

    @Override
    public void executeAdditiveRealmPlugins(Request servletRequest) {
        //Not Relevant
    }

    @Override
    public Map<String, String> getPluginsStatus() {
        return Maps.newHashMap();
    }

    @Override
    public String getPluginsInfoSupportBundleDump() {
        return StringUtils.EMPTY;
    }

    @Override
    @Nonnull
    public Map<String, Map<String, String>> getPluginsInfoSupportBundleDumpAsMap() {
        return new HashMap<>();
    }

    @Override
    public ResponseCtx downloadPlugin(String pluginName) {
        throw new UnsupportedOperationException("Downloading user plugins requires Artifactory Pro.");
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public void exportTo(ExportSettings settings) {
        //Not Relevant
    }

    @Override
    public void importFrom(ImportSettings settings) {
        //Not Relevant
    }
}