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

package org.artifactory.logging.layout;

/**
 * Kept for backward compatibility since existing logback configurations use this fully qualified class name.
 *
 * Keep in mind - the logback configuration file is read before the conversions are executed, so even the converter
 * created for this case does not help...
 *
 * @deprecated Instead you should use {@link org.jfrog.common.logging.logback.layout.BackTracePatternLayout} directly.
 * @author Yinon Avraham
 */
@Deprecated
public class BackTracePatternLayout extends org.jfrog.common.logging.logback.layout.BackTracePatternLayout {
}
