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

package org.artifactory.webapp.servlet.authentication.interceptor.anonymous;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation that holds and calls all registered {@link AnonymousAuthenticationInterceptor} instances that
 * were added to it.
 *
 * @author Dan Feldman
 */
public class AnonymousAuthenticationInterceptors implements AnonymousAuthenticationInterceptor {

    private final List<AnonymousAuthenticationInterceptor> interceptors = new ArrayList<>();

    public void addInterceptors(Collection<AnonymousAuthenticationInterceptor> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    @Override
    public boolean accept(HttpServletRequest request) {
        for (AnonymousAuthenticationInterceptor interceptor : interceptors) {
            if(interceptor.accept(request)) {
                return true;
            }
        }
        return false;
    }
}
