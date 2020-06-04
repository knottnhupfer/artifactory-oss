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

package org.artifactory.util;

/**
 * @author Yoav Landman
 */
public abstract class ExceptionUtils {
    private ExceptionUtils() {
        // utility class
    }

    /**
     * Unwrap an exception
     *
     * @param throwable  the throwable to examine
     * @param causeType the desired cause type to find
     * @return The wrapped cause or null if not found
     */
    public static Throwable getCauseOfType(Throwable throwable, Class<? extends Throwable> causeType) {
        if (throwable != null) {
            if (causeType.isAssignableFrom(throwable.getClass())) {
                return throwable;
            } else {
                return getCauseOfType(throwable.getCause(), causeType);
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the root cause of the exception. If the input throwable has no cause will return the input.
     *
     * @param throwable the throwable to examine
     * @return The root cause or itself if has no cause
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Wraps an exception with {@link RuntimeException} if the exception is not already runtime exception
     *
     * @param e The exception to wrap
     * @return Runtime exception with the input as cause, or the input exception is already runtime exception
     */
    public static RuntimeException toRuntimeException(Exception e) {
        return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
    }

}
