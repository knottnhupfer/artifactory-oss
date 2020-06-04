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

import org.artifactory.api.context.ArtifactoryContext;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

import javax.servlet.*;
import java.io.IOException;

/**
 * Delegating filter from a delayed context filter to {@link SessionRepositoryFilter}
 *
 * @author Shay Yaakov
 */
public class SessionFilter extends DelayedFilterBase {

    private SessionRepositoryFilter delegate;

    @Override
    public void initLater(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        ArtifactoryContext context = RequestUtils.getArtifactoryContext(servletContext);
        SessionRepository sessionRepository = context.beanForType(SessionRepository.class);
        delegate = new SessionRepositoryFilter(sessionRepository);
        delegate.setServletContext(servletContext);

    }

    @Override
    public void destroy() {
        if (delegate != null) {
            delegate.destroy();
        }
    }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
        if (shouldSkipFilter(req)) {
            chain.doFilter(req, resp);
            return;
        }
        delegate.doFilter(req, resp, chain);
    }
}