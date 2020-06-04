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

package org.artifactory.ui.rest.service.admin.advanced.support;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.support.ArtifactorySupportBundleConfig;
import org.artifactory.addon.support.SupportAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.jfrog.support.common.core.exceptions.BundleGenerationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.artifactory.util.HttpUtils.getServletContextUrl;

/**
 * @author Michael Pasternak
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SupportServiceGenerateBundle<T extends BundleConfigurationWrapper> implements RestService<T> {

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        SupportAddon supportAddon = addonsManager.addonByType(SupportAddon.class);

        String result;
        if (supportAddon.isSupportAddonEnabled()) {
            BundleConfigurationWrapper bundleConfigurationContainer = request.getImodel();
            try {
                ArtifactorySupportBundleConfig config = bundleConfigurationContainer
                        .getArtifactorySupportBundleConfig();
                if (StringUtils.isEmpty(config.getId())) {
                    String id = System.currentTimeMillis() + "";
                    if(StringUtils.isNotEmpty(config.getName())){
                        config.setId(config.getName() + "-" + id);
                    }
                }
                result = supportAddon.generateSupportBundle(
                        config, getServletContextUrl(request.getServletRequest()));
            } catch (IOException e) {
                response.error("Support content collection has failed, see 'support.log' for more details");
                return;
            } catch (BundleGenerationException e) {
                response.responseCode(e.getResult().getStatusCode());
                return;
            } catch (UnsupportedOperationException e) {
                response.error(e.getMessage());
                return;
            }
            response.iModel(result);
        }
    }
}
