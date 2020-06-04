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

package org.artifactory.storage.fs.lock.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.artifactory.sapi.common.Lock;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.fs.session.StorageSessionHolder;
import org.artifactory.storage.spring.StorageContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author freds
 * @author Yossi Shaul
 */
public class LockingAdvice implements MethodInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LockingAdvice.class);

    private MethodInterceptor alwaysOnTxInterceptor;

    public LockingAdvice() {
        log.debug("Creating locking advice interceptor");
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        boolean createTransaction = true;
        if (isSynchronizationActive()) {
            log.trace("Tx already active on {} - no need starting a new one.", invocation.getMethod());
            createTransaction = false;
        }

        boolean currentInvocationCreatedSession = false;
        if (StorageSessionHolder.getSession() == null) {
            if (isActualTransactionActive()) {
                IllegalStateException exception = new IllegalStateException(
                        "Artifactory Storage Session doesn't exist in Lock annotated method." +
                        "Please add the " + Lock.class.getName() +
                        " annotation to your method before the " + Transactional.class.getName() + " one.");
                log.error(exception.getMessage(), exception);
                throw exception;
            }
            currentInvocationCreatedSession = true;
            createTransaction = true;
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Activating locking interceptor on {} with create tx {}",
                        invocation.getMethod(), createTransaction);
            }
            if (createTransaction) {
                log.debug("Tx init by: {}", invocation.getMethod());
                return getAlwaysOnTxInterceptor().invoke(invocation);
            } else {
                return invocation.proceed();
            }
        } catch (Exception e) {
            log.debug("Received exception on method invocation: " + invocation, e);
            throw e;
        } finally {
            if (currentInvocationCreatedSession) {
                StorageSession session = StorageSessionHolder.getSession();
                if (session != null) {
                    try {
                        log.error("Session exist after completion: {}", session);
                        session.releaseResources();
                    } finally {
                        StorageSessionHolder.removeSession();
                    }
                }
            }
        }
    }

    public static boolean isActualTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    public static boolean isSynchronizationActive() {
        return TransactionSynchronizationManager.isSynchronizationActive();
    }

    private MethodInterceptor getAlwaysOnTxInterceptor() {
        if (alwaysOnTxInterceptor == null) {
            alwaysOnTxInterceptor = StorageContextHelper.get().beanForType(
                    "alwaysOnTxInterceptor", MethodInterceptor.class);
            log.debug("Locking interceptor has Tx advice {}", alwaysOnTxInterceptor);
        }
        return alwaysOnTxInterceptor;
    }
}
