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

package org.artifactory.concurrent;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Noam Shemesh
 */
public class ArtifactoryRunnable implements Runnable {
    private final Runnable delegate;
    private ArtifactoryContext context;
    private final Authentication authentication;


    public ArtifactoryRunnable(Runnable delegate, ArtifactoryContext context, Authentication authentication) {
        this.delegate = delegate;
        this.context = context;
        this.authentication = authentication;
    }

    @Override
    public void run() {
        try {
            ArtifactoryContextThreadBinder.bind(context);
            ArtifactoryHome.bind(context.getArtifactoryHome());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            delegate.run();
        } finally {
            // in case an async operation is fired while shutdown (i.e gc) the context holder strategy is
            // cleared and NPE can happen after the async finished (or is finishing). see RTFACT-2812
            if (context.isReady()) {
                SecurityContextHolder.clearContext();
            }
            ArtifactoryContextThreadBinder.unbind();
            ArtifactoryHome.unbind();
        }
    }
}
