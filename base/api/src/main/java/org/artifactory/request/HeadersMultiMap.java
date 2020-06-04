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

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.jfrog.common.StreamSupportUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author saffih
 * Class acts as headers container.
 * The old contract had single value, while for docker use case support multimap caseinsensitive
 * The class header are intended to be used as is - with no change
 */
public class HeadersMultiMap implements HttpHeadersContainter {
    @Nonnull private Multimap<String, String> headers = TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, Comparator.reverseOrder());

    public HeadersMultiMap() {
    }

    /**
     * add all headers - accumulate in multimap
     *
     * @param stream headers name/value as entry map stream
     * @return the headers in container
     */
    public HeadersMultiMap addAll(@Nonnull Stream<Map.Entry<String, String>> stream) {
        stream.forEach(it -> this.addHeader(it.getKey(), it.getValue()));
        return this;
    }

    public HeadersMultiMap addAll(@Nonnull String key, @Nullable Enumeration<String> values) {
        if (values != null) {
            StreamSupportUtils.enumerationToStream(values)
                    .forEach(value -> this.addHeader(key, value));
        }
        return this;
    }

    public Collection<Map.Entry<String, String>> entries() {
        return headers.entries();
    }

    public boolean contains(String key) {
        return headers.containsKey(key);
    }

    /**
     * Used for testing
     */
    public Multimap<String, String> getMultiHeaders() {
        return headers;
    }

    public void setHeader(@Nonnull String key, String value) {
        this.headers.removeAll(key);
        if (value != null) {
            this.headers.put(key, value);
        }
    }

    /**
     * @param value remove header if value is null.
     */
    public void addHeader(@Nonnull String key, @Nullable String value) {
        if (value == null) {
            this.headers.removeAll(key);
        } else {
            this.headers.put(key, value);
        }
    }

    /**
     * replace existing key values
     *
     * @param headers Map with key and values replacing existing
     * @return HeadersMultiMap
     */
    public HeadersMultiMap updateHeaders(@Nonnull Map<String, String> headers) {
        headers.forEach(this::setHeader);
        return this;
    }

    @Override
    public Enumeration<String> getHeadersKeys() {
        return new IteratorEnumeration(headers.keySet().iterator());
    }

    @Override
    public Enumeration<String> getHeaderValues(@Nonnull String headerName) {
        return new IteratorEnumeration(headers.get(headerName).iterator());
    }

    /**
     * Accepts header can have multiple entries https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
     */
    public Stream<String> getAcceptHeadersMediaTypeWithoutOptions() {
        String key = "Accept";
        Enumeration<String> headerValues = getHeaderValues(key);
        return extractMediaTypeStringFromValues(headerValues);

    }

    public static Stream<String> extractMediaTypeStringFromValues(Enumeration<String> headerValues) {
        return StreamSupportUtils.enumerationToStream(headerValues)
                .flatMap(it -> Arrays.stream(it.split(","))
                                .map(mimeType -> mimeType.replaceFirst(";(?:.*)", ""))   //each value can have options with ";"
                .map(String::trim)
                .map(String::toLowerCase)
                );
    }

    @Override
    public String toString() {
        return "HeadersMultiMap{" +
                "headers=" + headers +
                '}';
    }
}
