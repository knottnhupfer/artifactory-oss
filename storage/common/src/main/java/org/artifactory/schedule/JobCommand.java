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

package org.artifactory.schedule;

import java.lang.annotation.*;

/**
 * User: freds Date: 7/6/11 Time: 4:34 PM
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface JobCommand {
    boolean singleton() default false;

    TaskUser schedulerUser() default TaskUser.INVALID;

    /**
     * Set to allow manual activation and provide strategy for finding authentication token.
     */
    TaskUser manualUser() default TaskUser.INVALID;

    StopCommand[] commandsToStop() default {};

    String[] keyAttributes() default {};

    boolean runOnlyOnPrimary() default TaskBase.DEFAULT_TASK_RUN_ONLY_ON_PRIMARY;

    /**
     * Short optional description of the job.
     */
    String description() default "";
}

