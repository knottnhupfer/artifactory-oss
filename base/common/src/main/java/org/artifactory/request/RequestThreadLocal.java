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

package org.artifactory.request;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Lior Azar
 */
public class RequestThreadLocal {

    private static ThreadLocal<RequestThreadLocalContext> context = new ThreadLocal<>();

    private RequestThreadLocal() {
    }

    public static void bind(RequestWrapper requestWrapper) {
        context.set(RequestThreadLocalContext.create(requestWrapper));
    }

    public static void unbind() {
        context.remove();
    }

    public static HttpServletRequest getRequest() {
        RequestThreadLocalContext requestThreadLocalContext = context.get();
        return requestThreadLocalContext != null ? requestThreadLocalContext.getRequestThreadLocal().getRequest() : null;
    }

    public static String getClientAddress() {
        RequestThreadLocalContext requestThreadLocalContext = context.get();
        if (requestThreadLocalContext != null) {
            return requestThreadLocalContext.getRequestThreadLocal().getClientAddress();
        }
        return "";
    }

    public static String getBaseUrl() {
        RequestThreadLocalContext requestThreadLocalContext = context.get();
        if (requestThreadLocalContext != null) {
            return requestThreadLocalContext.getRequestThreadLocal().getBaseUrl();
        }
        return "";
    }
}