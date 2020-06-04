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

package org.artifactory.ui.rest.service.admin.configuration.general;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.jfrog.config.wrappers.FileEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;

/**
 * @author chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteUploadedLogoService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(DeleteUploadedLogoService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String logoDir = ContextHelper.get().getArtifactoryHome().getLogoDir().getAbsolutePath();
            File file = new File(logoDir, "logo");
            Files.deleteIfExists(file.toPath());
            ContextHelper.get().getConfigurationManager().forceFileChanged(file,"artifactory.ui.",
                    FileEventType.DELETE);
        } catch (Exception e) {
            String message = "error deleting logo";
            log.error(message, e);
            response.error(message);
        }
    }
}
