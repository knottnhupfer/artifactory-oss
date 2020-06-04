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

import org.artifactory.api.repo.Async;

import java.util.concurrent.Future;

/**
 * This class is here only to simplify the async testing.
 *
 * @author Yossi Shaul
 */
public interface AsyncComponent {

    /**
     * @return Number of async invocations done on this component.
     */
    int asyncInvocationsCount();

    @Async
    Future<Integer> invokeAsync();

    /**
     * Sends a signal to the async method {@link AsyncComponent#invokeAsyncAndWaitForSignal()} to finish.
     */
    void signal();

    @Async
    void invokeAsyncAndWaitForSignal();

    @Async(delayUntilAfterCommit = true)
    void invokeAsyncAfterCommit();

}
