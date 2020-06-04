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

package org.artifactory.util.test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.google.common.collect.Lists;
import org.artifactory.api.TestService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.traffic.read.TrafficReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

/**
 * An internal service used during integration tests.
 *
 * @author Yossi Shaul
 */
@Service
public class InternalTestService implements TestService {
    private static final Logger log = LoggerFactory.getLogger(InternalTestService.class);

    private List<String> transactionLeaks = Lists.newArrayList();

    public LoggerContext printAndGetContext(String message) {
        message += ". home dir name " + ArtifactoryHome.get().getHomeDir().getName();
        log.info(message);
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    public ContextSelector getContextSelector() {
        return ContextSelectorStaticBinder.getSingleton().getContextSelector();
    }

    public Logger getLogger() {
        return log;
    }

    public void rotateTrafficLog() {
        //TODO: [by YS] do we really need to rotate of we just need to clean?
        TrafficReader trafficReader = new TrafficReader(ContextHelper.get().getArtifactoryHome().getLogDir());
        Calendar from = Calendar.getInstance();
        from.add(Calendar.YEAR, -1);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.YEAR, 1);
        trafficReader.readTrafficFiles(from.getTime(), to.getTime()).forEach(file -> file.delete());
        trafficReader.readXrayTrafficFiles(from.getTime(), to.getTime()).forEach(file -> file.delete());
        rollover("org.artifactory.traffic.TrafficLogger", "TRAFFIC");
        rollover("org.artifactory.traffic.XrayTrafficLogger", "XRAY_TRAFFIC");
    }

    private void rollover(String loggerName, String appender) {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getILoggerFactory()
                .getLogger(loggerName);
        RollingFileAppender file = (RollingFileAppender) logger.getAppender(appender);
        if (file != null) {
            file.rollover();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
        }
    }

    @Override
    public void transactionLeak(ArtifactoryContext context, String details) {
        if (isInTestMode(context)) {
            transactionLeaks.add(details);
        }
    }

    public List<String> getTransactionLeaks() {
        return transactionLeaks;
    }

    private boolean isInTestMode(ArtifactoryContext context) {
        return context.getArtifactoryHome().getArtifactoryProperties()
                .getBooleanProperty(ConstantValues.test);
    }

}
