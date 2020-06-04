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

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import java.util.concurrent.BlockingQueue;

/**
 * @author Yoav Landman
 */
public abstract class DelayedFilterBase implements DelayedInit, Filter {

    private FilterConfig filterConfig;

    @Override
    @SuppressWarnings({"unchecked"})
    public final void init(FilterConfig filterConfig) throws ServletException {
        BlockingQueue<Filter> waiters = (BlockingQueue<Filter>) filterConfig.getServletContext()
                .getAttribute(APPLICATION_CONTEXT_LOCK_KEY);
        this.filterConfig = filterConfig;
        if (waiters != null) {
            waiters.add(this);
        } else {
            //Servlet 2.5 lazy filter initing
            delayedInit();
        }
    }

    @Override
    public void delayedInit() throws ServletException {
        initLater(filterConfig);
    }

    private String getFilterName() {
        return filterConfig.getFilterName();
    }

    /**
     * If {@link DelayedInit#FILTER_SHORTCUT_ATTR} is set check if the current filter should be skipped.
     */
    boolean shouldSkipFilter(final ServletRequest req) {
        String filterShortcut = (String) req.getAttribute(FILTER_SHORTCUT_ATTR);
        return filterShortcut != null && !getFilterName().equals(filterShortcut);
    }

    protected abstract void initLater(FilterConfig config) throws ServletException;
}