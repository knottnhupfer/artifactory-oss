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

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * @author gidis
 */
public class TraceableMethodInvocation implements MethodInvocation {

    private final MethodInvocation wrapped;
    private final Throwable throwable;
    private final WorkExecution workExecution;

    TraceableMethodInvocation(MethodInvocation wrapped, String threadName) {
        this(wrapped, threadName, null);
    }

    TraceableMethodInvocation(MethodInvocation wrapped, String threadName, WorkExecution workExecution) {
        this.wrapped = wrapped;
        String msg = "[" + threadName + "] async call to '" + wrapped.getMethod() + "' completed with error.";
        this.throwable = new Throwable(msg);
        this.workExecution = workExecution;
    }

    public WorkExecution getWorkExecution() {
        return workExecution;
    }

    public MethodInvocation getWrapped() {
        return wrapped;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public Method getMethod() {
        return wrapped.getMethod();
    }

    @Override
    public Object[] getArguments() {
        return wrapped.getArguments();
    }

    @Override
    public Object proceed() throws Throwable {
        return wrapped.proceed();
    }

    @Override
    public Object getThis() {
        return wrapped.getThis();
    }

    @Override
    public AccessibleObject getStaticPart() {
        return wrapped.getStaticPart();
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }
}

