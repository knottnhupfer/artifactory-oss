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

package org.artifactory.schedule.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.artifactory.api.repo.Async;
import org.artifactory.api.repo.AsyncWorkQueueProviderService;
import org.artifactory.api.repo.WorkItem;
import org.artifactory.api.repo.WorkQueue;
import org.artifactory.common.ConstantValues;
import org.artifactory.sapi.common.Lock;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.fs.lock.aop.LockingAdvice;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.fs.session.StorageSessionHolder;
import org.artifactory.storage.tx.SessionResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Yoav Landman
 */
public class AsyncAdvice implements MethodInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AsyncAdvice.class);
    private static final Future<Boolean> DUMMY_TRUE_FUTURE = new Future<Boolean>() {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public Boolean get() {
            return true;
        }
    };

    @Override
    public Future<?> invoke(final MethodInvocation invocation) {
        MethodAnnotation<Lock> lockMethodAnnotation = getMethodAnnotation(invocation, Lock.class);
        if (lockMethodAnnotation.annotation != null) {
            throw new RuntimeException("The @Async annotation cannot be used with the @Lock annotation. " +
                    "Use @Async#transactional=true instead: " + lockMethodAnnotation.method);
        }
        MethodAnnotation<Async> asyncMethodAnnotation = getMethodAnnotation(invocation, Async.class);
        boolean delayExecutionUntilCommit = asyncMethodAnnotation.annotation.delayUntilAfterCommit();
        boolean failIfNotScheduledFromTransaction = asyncMethodAnnotation.annotation
                .failIfNotScheduledFromTransaction();
        boolean inSession = StorageSessionHolder.getSession() != null;
        validateAnnotations(asyncMethodAnnotation, delayExecutionUntilCommit, failIfNotScheduledFromTransaction,
                inSession);
        TraceableMethodInvocation traceableInvocation;
        // Submit invocation use cases:
        // 1. submit async call with delayAfterCommit=false
        // 2. submit async call with delayAfterCommit=true
        // 3. submit work queue with delayAfterCommit=false
        // 4. submit work queue with delayAfterCommit=true
        if (asyncMethodAnnotation.annotation.workQueue()) {
            validateWorkQueueParams(invocation, asyncMethodAnnotation);
            WorkQueue<WorkItem> workQueue = getWorkQueue(invocation);
            WorkExecution workExecution = new WorkExecution(workQueue, (WorkItem) invocation.getArguments()[0],
                    asyncMethodAnnotation.annotation.blockUntilFinished());
            traceableInvocation = new TraceableMethodInvocation(invocation, Thread.currentThread().getName(),
                    workExecution);
        } else {
            validateNonWorkQueueParams(asyncMethodAnnotation);
            traceableInvocation = new TraceableMethodInvocation(invocation, Thread.currentThread().getName());
            addInvocationForTest(traceableInvocation);
        }
        log.trace("Adding: {}", traceableInvocation);
        return executeInvocation(delayExecutionUntilCommit, inSession, traceableInvocation);
    }

    private Future<?> executeInvocation(boolean delayExecutionUntilCommit, boolean inSession,
            TraceableMethodInvocation traceableInvocation) {
        try {
            if (delayExecutionUntilCommit && inSession) {
                //Schedule task submission for session save()
                // 1. submit async call with delayAfterCommit=true
                // 3. submit work queue with delayAfterCommit=true
                StorageSession session = StorageSessionHolder.getSession();
                MethodCallbackSessionResource sessionCallbacks =
                        session.getOrCreateResource(MethodCallbackSessionResource.class);
                sessionCallbacks.setAdvice(this);
                sessionCallbacks.addInvocation(traceableInvocation);
                //No future
                return null;
            } else {
                //Submit immediately
                Future<?> future;
                if (traceableInvocation.getWorkExecution() != null) {
                    // Submit work queue with delayAfterCommit=false
                    future = submit(traceableInvocation.getWorkExecution(), traceableInvocation.getMethod());
                } else {
                    // Submit async call with delayAfterCommit=false
                    future = submit(traceableInvocation);
                }
                return future;
            }
        } catch (Exception e) {
            // making sure to remove the invocation from the pending/executing
            removeInvocationForTest(traceableInvocation);
            throw e;
        }
    }

    private void validateAnnotations(MethodAnnotation<Async> asyncMethodAnnotation, boolean delayExecutionUntilCommit,
            boolean failIfNotScheduledFromTransaction, boolean inSession) {
        if (!inSession && delayExecutionUntilCommit) {
            if (failIfNotScheduledFromTransaction) {
                throw new IllegalStateException("Async invocation scheduled for after commit, " +
                        "cannot be scheduled outside a transaction: " + asyncMethodAnnotation.method);
            } else {
                log.debug("Async invocation scheduled for after commit, but not scheduled inside a transaction: {}",
                        asyncMethodAnnotation.method);
            }
        }
    }

    private void validateWorkQueueParams(MethodInvocation invocation, MethodAnnotation<Async> asyncMethodAnnotation) {
        Type[] genericParameterTypes = invocation.getMethod().getGenericParameterTypes();
        if (genericParameterTypes.length != 1 || !((genericParameterTypes[0].getClass().isInstance(WorkItem.class)))) {
            throw new IllegalStateException("Async invocation " + asyncMethodAnnotation.method +
                    " using work queue, but not using single work item parameter");
        }
    }

    private void validateNonWorkQueueParams(MethodAnnotation<Async> asyncMethodAnnotation) {
        if (asyncMethodAnnotation.annotation.blockUntilFinished()) {
            throw new IllegalStateException("Async invocation " + asyncMethodAnnotation.method + " is using " +
                    "annotation 'blockUntilFinished' which can't be used without the 'workQueue' annotation.");
        }
    }

    private WorkQueue<WorkItem> getWorkQueue(MethodInvocation invocation) {
        return InternalContextHelper.get().beanForType(AsyncWorkQueueProviderService.class)
                .getWorkQueue(invocation.getMethod(), invocation.getThis());
    }

    @SuppressWarnings({"unchecked"})
    private <T extends Annotation> MethodAnnotation<T> getMethodAnnotation(MethodInvocation invocation,
            Class<T> annotationClass) {
        Method method = invocation.getMethod();
        T annotation = method.getAnnotation(annotationClass);
        //Try to read the class level annotation if the interface is not found
        if (annotation != null) {
            return new MethodAnnotation(method, annotation);
        }
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(
                ((ReflectiveMethodInvocation) invocation).getProxy());
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        annotation = specificMethod.getAnnotation(annotationClass);
        return new MethodAnnotation(specificMethod, annotation);
    }

    private Future<?> submit(final List<TraceableMethodInvocation> workQueueInvocations) {
        workQueueInvocations
                .forEach(queueInvocation -> submit(queueInvocation.getWorkExecution(), queueInvocation.getMethod()));
        return null;
    }

    private Future<?> submit(WorkExecution workExecution, Method method) {
        WorkQueue<WorkItem> workQueue = workExecution.workQueue;
        Future<?> result = submitWorkQueueTask(workQueue, workExecution.workItem, method);
        if (workExecution.blockUntilFinished) {
            workQueue.waitForItemDone(workExecution.workItem);
        }
        return result;
    }

    private Future<?> submitWorkQueueTask(WorkQueue<WorkItem> workQueue, WorkItem workItem, Method method) {
        Future<?> result = null;
        boolean added = workQueue.offerWork(workItem, method);
        if (!added) {
            // return a dummy future
            return DUMMY_TRUE_FUTURE;
        }
        if (workQueue.availablePermits() != 0) {
            // there's a small chance we will miss event (especially if the work queue contain only single worker)
            CachedThreadPoolTaskExecutor executor = InternalContextHelper.get()
                    .beanForType(CachedThreadPoolTaskExecutor.class);
            result = executor.submit(workQueue::doJobs);
        }
        return result;
    }

    private Future<?> submit(final CompoundInvocation invocation) {
        InternalArtifactoryContext context = InternalContextHelper.get();
        CachedThreadPoolTaskExecutor executor = context.beanForType(CachedThreadPoolTaskExecutor.class);
        try {
            executor.submit(() -> {
                try {
                    if (TransactionSynchronizationManager.isSynchronizationActive()) {
                        //Sanity check we should never have a tx sync on an existing pooled thread
                        throw new IllegalStateException(
                                "An async invocation (" + invocation.getMethod() + ") " +
                                        "should not be associated with an existing transaction.");
                    }
                    invocation.proceed();
                } catch (Throwable throwable) {
                    Throwable loggedThrowable;
                    loggedThrowable = throwable;
                    Method method = invocation.getLatestMethod();
                    log.error("Could not execute async method: '" + method + "'.", loggedThrowable);
                }
                return null;
            });
        } catch (TaskRejectedException e) {
            log.error("Task {} rejected by scheduler: {}", invocation, e.getMessage());
            log.debug("Task {} rejected by scheduler: {}", invocation, e.getMessage(), e);
        }
        return null;
    }

    private Future<?> submit(final TraceableMethodInvocation invocation) {
        InternalArtifactoryContext context = InternalContextHelper.get();
        if (invocation.getWorkExecution() != null) {
            throw new IllegalStateException("Cannot call submit on work execution for " + invocation);
        }

        CachedThreadPoolTaskExecutor executor = context.beanForType(CachedThreadPoolTaskExecutor.class);
        Future<?> future = null;
        try {
            future = executor.submit(() -> {
                try {
                    if (TransactionSynchronizationManager.isSynchronizationActive()) {
                        //Sanity check we should never have a tx sync on an existing pooled thread
                        throw new IllegalStateException(
                                "An async invocation (" + invocation.getMethod() + ") " +
                                        "should not be associated with an existing transaction.");
                    }
                    Object result = doInvoke(invocation);
                    // if the result is not of type Future don't bother returning it (unless you are fond of ClassCastExceptions)
                    if (result instanceof Future) {
                        return ((Future) result).get();
                    } else {
                        return null;
                    }
                } catch (Throwable throwable) {
                    Throwable original = invocation.getThrowable();
                    original.initCause(throwable);
                    return null;
                }
            });
        } catch (TaskRejectedException e) {
            log.error("Task {} rejected by scheduler: {}", invocation, e.getMessage());
            log.debug("Task {} rejected by scheduler: {}", invocation, e.getMessage(), e);
        }
        // only return the future result if the method returns a Future object
        if (Future.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
            return future;
        } else {
            return null;
        }
    }

    Object doInvoke(TraceableMethodInvocation invocation) throws Throwable {
        Authentication originalAuthentication = null;
        try {
            MethodAnnotation<Async> methodAnnotation =
                    getMethodAnnotation(invocation.getWrapped(), Async.class);
            if (methodAnnotation.annotation == null) {
                throw new IllegalArgumentException(
                        "An async invocation (" + invocation.getMethod() +
                                ") should be used with an @Async annotated invocation.");
            }
            if (methodAnnotation.annotation.authenticateAsSystem()) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                originalAuthentication = securityContext.getAuthentication();
                InternalContextHelper.get().getSecurityService().authenticateAsSystem();
            }
            Object result;
            if (methodAnnotation.annotation.transactional()) {
                //Wrap in a transaction
                log.trace("Invoking {} in transaction", invocation);
                result = new LockingAdvice().invoke(invocation);
                log.trace("Finished {} in transaction", invocation);
            } else {
                log.trace("Invoking {} ", invocation);
                result = invocation.proceed();
                log.trace("Finished {}", invocation);
            }
            return result;
        } finally {
            if (originalAuthentication != null) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(originalAuthentication);
            }

            // remove the invocations here (called from the Compound also)
            removeInvocationForTest(invocation);
        }
    }

    public static class MethodCallbackSessionResource extends SessionResourceAdapter {
        final CompoundInvocation sharedInvocations = new CompoundInvocation();
        final List<TraceableMethodInvocation> sharedWorkQueueExecutions = new ArrayList<>();
        AsyncAdvice advice;

        void setAdvice(AsyncAdvice advice) {
            this.advice = advice;
            sharedInvocations.setAdvice(advice);
        }

        void addInvocation(TraceableMethodInvocation invocation) {
            if (invocation.getWorkExecution() != null) {
                sharedWorkQueueExecutions.add(invocation);
            } else {
                sharedInvocations.add(invocation);
            }
        }

        @Override
        public void afterCompletion(boolean commit) {
            if (commit) {
                if (!sharedWorkQueueExecutions.isEmpty()) {
                    advice.submit(sharedWorkQueueExecutions);
                }
                if (!sharedInvocations.isEmpty()) {
                    advice.submit(sharedInvocations);
                }
            } else {
                sharedInvocations.clear();
            }
        }
    }

    private static class MethodAnnotation<T extends Annotation> {
        private final Method method;
        private final T annotation;

        private MethodAnnotation(Method method, T annotation) {
            this.method = method;
            this.annotation = annotation;
        }
    }

    // -------------- FOR TEST PURPOSES ONLY -------------

    // holds all the pending and running invocations. used only during tests
    private ConcurrentHashMap<TraceableMethodInvocation, TraceableMethodInvocation>
            pendingInvocations = new ConcurrentHashMap<>();

    public Collection<TraceableMethodInvocation> getPendingInvocations() {
        if (!isInTestMode()) {
            throw new IllegalStateException("Access to pending invocation field valid in test mode only!");
        }
        return pendingInvocations.values();
    }

    private void addInvocationForTest(TraceableMethodInvocation traceableInvocation) {
        if (isInTestMode()) {
            pendingInvocations.put(traceableInvocation, traceableInvocation);
        }
    }

    private void removeInvocationForTest(TraceableMethodInvocation invocation) {
        if (isInTestMode()) {
            log.trace("Removing: {}", invocation);
            pendingInvocations.remove(invocation);
        }
    }

    private boolean isInTestMode() {
        return ConstantValues.test.getBoolean();
    }

}