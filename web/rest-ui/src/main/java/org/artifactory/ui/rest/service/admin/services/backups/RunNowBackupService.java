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

package org.artifactory.ui.rest.service.admin.services.backups;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.BackupService;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RunNowBackupService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RunNowBackupService.class);

    @Autowired
    private BackupService backupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("RunNowBackup");
        runNowBackup(request, response);
    }

    /**
     * run now backup
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void runNowBackup(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse) {
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        BackupDescriptor backupDescriptor = (BackupDescriptor) artifactoryRequest.getImodel();

        if (backupDescriptor.isPrecalculate() && !backupService.isEnoughFreeSpace(backupDescriptor)) {
            artifactoryResponse.error("Not enough free space to perform backup " + backupDescriptor.getKey());
            log.error("Not enough free space to perform backup {}", backupDescriptor.getKey());
        } else {
            backupService.scheduleImmediateSystemBackup(backupDescriptor, statusHolder);
            updateResponseFeedback(artifactoryResponse, statusHolder);
        }
    }

    /**
     * update response with feedback
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param statusHolder        - msg status holder
     */
    private void updateResponseFeedback(RestResponse artifactoryResponse, BasicStatusHolder statusHolder) {
        if (statusHolder.isError()) {
            artifactoryResponse.error(statusHolder.getStatusMsg());
        } else {
            artifactoryResponse.info("System backup was successfully scheduled to run in the background.");
        }
    }
}
