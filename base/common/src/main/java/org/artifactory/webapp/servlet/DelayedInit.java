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

package org.artifactory.webapp.servlet;

import javax.servlet.ServletException;

/**
 * An interface for servlet filters and servlets that should only be initiated after the application context is ready
 *
 * @author Yoav Landman
 */
public interface DelayedInit {

    /**
     * The lock attribute key is placed just before filters initialization starts and removed when it's finished
     * (on success or failure)
     */
    String APPLICATION_CONTEXT_LOCK_KEY = "org.artifactory.webapp.lock";
    String FILTER_SHORTCUT_ATTR = "shortcutFilterTo"; //Signifies to other filters down the chain to skip as necessary

    void delayedInit() throws ServletException;
}
