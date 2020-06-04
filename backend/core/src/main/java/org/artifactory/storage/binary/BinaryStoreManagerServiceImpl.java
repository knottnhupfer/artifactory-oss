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

package org.artifactory.storage.binary;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.mail.MailService;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.schedule.*;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.binstore.service.BinaryService;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author gidis
 */
@Service
@Reloadable(beanClass = BinaryStoreManagerService.class, initAfter = {TaskService.class}, listenOn = CentralConfigKey.none)
public class BinaryStoreManagerServiceImpl implements BinaryStoreManagerService {

    @Autowired
    private TaskService taskService;

    @Override
    public void init() {
        registersErrorPollingJob();
    }

    /**
     * creates & starts HeartbeatJob
     */
    private void registersErrorPollingJob() {
        TaskBase errorNotificationJob = TaskUtils.createRepeatingTask(BinaryStoreErrorNotificationJob.class,
                TimeUnit.SECONDS.toMillis(ConstantValues.binaryStoreErrorNotificationsIntervalSecs.getLong()),
                TimeUnit.SECONDS.toMillis(ConstantValues.binaryStoreErrorNotificationsStaleIntervalSecs.getLong()));
        taskService.startTask(errorNotificationJob, false);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {

    }

    @JobCommand(singleton = true, runOnlyOnPrimary = false, description = "binary store error notification",
            schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.SYSTEM)
    public static class BinaryStoreErrorNotificationJob extends QuartzCommand {
        @Override
        protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
            ArtifactoryContext context = ContextHelper.get();
            BinaryService binaryService = context.beanForType(BinaryService.class);
            Set<String> errorMessages = new HashSet<>(binaryService.getAndManageErrors());
            if (!errorMessages.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(errorMessages.size())
                        .append(" critical errors occurred in Artifactory binary provider of server '");
                stringBuilder.append(context.getServerId()).append("' :\r\n");
                for (String error : errorMessages) {
                    stringBuilder.append(error).append("\r\n");
                }
                AddonsManager addonsManager = context.beanForType(AddonsManager.class);
                MailService mailService = context.beanForType(MailService.class);
                CoreAddons coreAddons = addonsManager.addonByType(CoreAddons.class);
                List<String> adminEmails = coreAddons.getUsersForBackupNotifications();
                if (!adminEmails.isEmpty()) {
                    mailService.sendMail(adminEmails.toArray(new String[adminEmails.size()]),
                            "Critical binary provider error",
                            stringBuilder.toString());
                }
            }
        }
    }
}

