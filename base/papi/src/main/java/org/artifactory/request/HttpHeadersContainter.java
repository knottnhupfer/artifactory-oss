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

import org.jfrog.common.StreamSupportUtils;

import javax.annotation.Nonnull;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Saffi Hartal
 */
public interface HttpHeadersContainter {
    Enumeration<String> getHeadersKeys();

    Enumeration<String> getHeaderValues(@Nonnull String headerName);

    /**
     * @deprecated replaced by org.artifactory.request.HasHttpHeaders#getHeaderValues(java.lang.String)
     * Backward compatible map getheader (return first)
     * Unless we use HeadersMultimap which use natural reversed order which is the last - for having map like order last overrides current)
     * @param headerName
     * @return
     */
    @Deprecated
    default String getHeader(String headerName) {
        Enumeration<String> enumeration = getHeaderValues(headerName);
        if ((enumeration == null) || (!enumeration.hasMoreElements())) {
            return null;
        }
        return enumeration.nextElement();
    }

    /**
     * @deprecated replaced by org.artifactory.request.HasHttpHeaders#getHeaderValues(java.lang.String)
     * The method may be broken in internal request. Old implementation ignored the parameter RTFACT-17547
     *
     * @param headerName
     * @return
     */
    @Deprecated
    default Enumeration<String> getHeaders(String headerName) {
        return getHeaderValues(headerName);
    }

    /**
     * @deprecated replaced by using that object and later iterate org.artifactory.request.HasHttpHeaders#getHeadersKeys() and for each use org.artifactory.request.HasHttpHeaders#getHeaderValues(java.lang.String)
     * used only for testing - HeadersMultimap class - in transition from single key to multi key headers.
     */
    @Deprecated
    default Map<String, String> getHeaders() {
        HashMap<String, String> result = new HashMap<>();
        StreamSupportUtils.enumerationToStream(getHeadersKeys())
                .forEach(key->
                        StreamSupportUtils.enumerationToStream(getHeaderValues(key))
                        .forEach( value->
                                result.put(key, value)));
        return result;
    }
}
