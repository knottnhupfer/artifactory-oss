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

package org.artifactory.schedule.aop.test;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is here only to simplify the async testing.
 *
 * @author Yossi Shaul
 */
@Component
public class AsyncComponentImpl implements AsyncComponent {

    private int invocationsCounter = 0;
    private volatile boolean signal;

    public int asyncInvocationsCount() {
        return invocationsCounter;
    }

    public Future<Integer> invokeAsync() {
        invocationsCounter++;
        return new Future<Integer>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            public boolean isCancelled() {
                return false;
            }

            public boolean isDone() {
                return true;
            }

            public Integer get() throws InterruptedException, ExecutionException {
                return invocationsCounter;
            }

            public Integer get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return get();
            }
        };
    }

    public void signal() {
        signal = true;
    }

    public void invokeAsyncAndWaitForSignal() {
        while (!signal) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void invokeAsyncDelayUntilAfterCommit() {
        // just for testing
    }

    public void invokeAsyncAfterCommit() {
        // just for testing
    }

}