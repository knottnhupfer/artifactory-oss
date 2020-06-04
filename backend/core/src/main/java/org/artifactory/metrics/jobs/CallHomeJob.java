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

package org.artifactory.metrics.jobs;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.cron.CronUtils;
import org.artifactory.metrics.TimeRandomizer;
import org.artifactory.metrics.services.CallHomeService;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.TaskUser;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A job to trigger CallHome request.
 *
 * @author Michael Pasternak
 */
@JobCommand(description = "Artifactory Version Checker",
        singleton = true, runOnlyOnPrimary = false, schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.SYSTEM)
public class CallHomeJob extends QuartzCommand {
    private static final Logger log = LoggerFactory.getLogger(CallHomeJob.class);

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        log.debug("CallHome job started");
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        CallHomeService callHomeService = artifactoryContext.beanForType(CallHomeService.class);
        callHomeService.callHome();
        log.debug("CallHome job finished");
    }

    /**
     * To lower load on bintray we produce random execution time
     *
     * @return Quartz scheduling expression
     */
    public static String buildRandomQuartzExp() {
        // adding an option to override CRON expression for testing purposes
        String predefinedExp = ConstantValues.testCallHomeCron.getString();
        if(!StringUtils.isBlank(predefinedExp)) {
            if(CronUtils.isValid(predefinedExp))
                return predefinedExp;
        }
        return String.format(
                "0 %d %d ? * *",
                TimeRandomizer.randomMinute(),
                TimeRandomizer.randomHourAtNight()
        );
    }
}

