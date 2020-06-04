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

package org.artifactory.session;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.metrics.TimeRandomizer;
import org.artifactory.schedule.*;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.spring.Reloadable;
import org.artifactory.state.model.ArtifactoryStateManager;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.jdbc.JdbcOperationsSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Shay Yaakov
 * @author Tamir Hadad
 */
@Service(value = "sessionRepository")
@Reloadable(beanClass = ArtifactorySessionRepository.class, initAfter = {ArtifactoryStateManager.class},
        listenOn = CentralConfigKey.none)
public class ArtifactorySessionRepositoryImpl<T extends Session>
        implements ArtifactorySessionRepository, SessionRepository<T> {
    private static final Logger log = LoggerFactory.getLogger(ArtifactorySessionRepositoryImpl.class);

    private SessionRepository<T> delegate;
    private AddonsManager addonsManager;
    private TaskService taskService;

    @Autowired
    public ArtifactorySessionRepositoryImpl(AddonsManager addonsManager, TaskService taskService) {
        this.addonsManager = addonsManager;
        this.taskService = taskService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init() {
        long timeoutInMinutes = ConstantValues.uiSessionTimeoutInMinutes.getLong();
        long timeoutInSeconds = TimeUnit.MINUTES.toSeconds(timeoutInMinutes);
        // The assignment should be safe
        delegate = (SessionRepository) addonsManager.addonByType(HaCommonAddon.class)
                .createSessionRepository((int) timeoutInSeconds);
        if (delegate instanceof JdbcOperationsSessionRepository) {
            TaskBase task = TaskUtils.createCronTask(ArtifactorySessionRepositoryImpl.DeleteExpiredSessions.class,
                    getSessionCleanupCron());
            taskService.startTask(task, true);
        }
    }

    @Override
    public T createSession() {
        return delegate.createSession();
    }

    @Override
    public void save(T session) {
        delegate.save(session);
    }

    @Override
    public T getSession(String id) {
        try {
            return delegate.getSession(id);
        } catch (RuntimeException e) {
            log.debug("Deleting session after an error in session's deserialization.", e);
            try {
                delete(id);
            } catch (RuntimeException rte) {
                log.trace("Exception caught during session removal.", rte);
                // this is expected, as if it throws an exception in getSession, it will throw the same exception in remove.
                // the item will be deleted as the serialization of the removed items happens after it was removed from the map
            }
            // if ExpiringSession can't be deserialized a runtime exception will be thrown.
            // (like in case of hazelcast map still saving an older version of the session authentication)
            if (!e.getMessage().contains("Serialization") && !e.getMessage().contains("serialVersionUID") &&
                    !e.getClass().getName().contains("Serialization")) {
                throw e;
            }
            return null;
        }
    }

    @Override
    public void delete(String id) {
        delegate.delete(id);
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

    @Override
    public SessionRepository getDelegateSessionRepo() {
        return delegate;
    }

    private String getSessionCleanupCron() {
        String cron = ConstantValues.dbSessionCleanupCron.getString();
        if (StringUtils.isEmpty(cron)) {
            cron = String.format(
                    "0 %d * ? * *",
                    TimeRandomizer.randomMinute()
            );
        }
        return cron;
    }

    @JobCommand(singleton = true, description = "Expired sessions cleanup job",
            schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.SYSTEM)
    public static class DeleteExpiredSessions extends QuartzCommand {
        private static final Logger log = LoggerFactory.getLogger(DeleteExpiredSessions.class);

        @Override
        protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
            try {
                ArtifactoryContext context = ContextHelper.get();
                if (context == null) {
                    log.warn("Context is not bound.");
                    return;
                }
                deleteExpiredSessions(context);
            } catch (Exception e) {
                log.error("Expired sessions cleanup job could not be completed. {}", e.getMessage());
                log.debug("Expired sessions cleanup job could not be completed.", e);
            }
        }

        private void deleteExpiredSessions(ArtifactoryContext context) {
            AddonsManager addonsManager = context.beanForType(AddonsManager.class);
            HaAddon haAddon = addonsManager.addonByType(HaAddon.class);
            if (haAddon.isHaEnabled()) {
                try {
                    log.info("Start cleaning expired sessions");
                    SessionRepository sessionRepository = context
                            .beanForType("sessionRepository", SessionRepository.class);
                    if (sessionRepository instanceof ArtifactorySessionRepository) {
                        SessionRepository delegateSessionRepo = ((ArtifactorySessionRepository) sessionRepository)
                                .getDelegateSessionRepo();
                        if (delegateSessionRepo instanceof JdbcOperationsSessionRepository) {
                            ((JdbcOperationsSessionRepository) delegateSessionRepo).cleanUpExpiredSessions();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed cleaning expired sessions. {}", e.getMessage());
                    log.debug("Failed cleaning expired sessions.", e);
                }
            }
        }
    }
}
