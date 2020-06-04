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

package org.artifactory.api.request;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.artifactory.request.HeadersMultiMap;
import org.jfrog.common.StreamSupportUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.request.HeadersMultiMap.extractMediaTypeStringFromValues;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Saffi Hartal
 */
public class HeadersMultiMapTest {

    @Test
    public void addLikeMap() throws Exception {
        HeadersMultiMap map = new HeadersMultiMap();
        map.setHeader("a", null);
        assertEquals(map.getHeader("a"), null);
        map.setHeader("a", "b");
        assertEquals(map.getHeader("a"), "b");
        map.setHeader("a", "c");
        assertEquals(map.getHeader("a"), "c");
        map.setHeader("a", null);
        assertEquals(map.getHeader("a"), null);
    }

    @Test
    public void addLikeMultiMap() {
        HeadersMultiMap map = new HeadersMultiMap();
        map.addHeader("a", "b");
        map.addHeader("a", "c");
        assertEquals(map.getMultiHeaders().get("a").toString(), "[c, b]");
    }

    @Test
    public void addAllTest() {
        Multimap<String, String> map = TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, Comparator.reverseOrder());
        map.put("a", "b");
        map.put("A", "b");
        map.put("Liza", "k");
        map.put("lizA", "l");
        map.put("b", "b");
        map.put("b", "something");
        map.put("accept",
                "application/vnd.docker.distribution.manifest.v2+json , application/vnd.docker.distribution.manifest.list.v2+json");
        HeadersMultiMap headerMap = new HeadersMultiMap();
        headerMap.addAll(map.entries().stream());

        Multimap<String, String> firstSecondDifference =
                Multimaps.filterEntries(map, e -> !headerMap.getMultiHeaders().containsEntry(e.getKey(), e.getValue()));

        Multimap<String, String> secondFirstDifference =
                Multimaps.filterEntries(headerMap.getMultiHeaders(), e -> !map.containsEntry(e.getKey(), e.getValue()));
        assertTrue(firstSecondDifference.isEmpty() && secondFirstDifference.isEmpty());
        Map<String, String> updateMap = new HashMap<>();
        updateMap.put("lIza", "new");
        updateMap.put("B", "new");
        updateMap.put("a", "newA");
        headerMap.updateHeaders(updateMap);

        firstSecondDifference = Multimaps
                .filterEntries(map, e -> !headerMap.getMultiHeaders().containsEntry(e.getKey(), e.getValue()));

        secondFirstDifference = Multimaps
                .filterEntries(headerMap.getMultiHeaders(), e -> !map.containsEntry(e.getKey(), e.getValue()));
        assertTrue(!firstSecondDifference.isEmpty() && !secondFirstDifference.isEmpty());

    }

    @Test
    public void testSetHeadergetHeaders() throws Exception {
        HeadersMultiMap map = new HeadersMultiMap();
        map.setHeader("a", null);
        assertEquals(map.getHeaders().values().toString(), Arrays.asList().toString());
        map.setHeader("a", "b");
        map.setHeader("a", "c");
        map.getHeaders().values();
        assertEquals(map.getMultiHeaders().values().toString(), Arrays.asList("c").toString());
        map.setHeader("a", null);
        assertEquals(map.getHeader("a"), null);
    }

    @Test
    public void testGetHeaders() throws Exception {
        HeadersMultiMap map = new HeadersMultiMap();
        map.addHeader("a", null);
        assertEquals(map.getHeaders().values().toString(), Arrays.asList().toString());
        map.addHeader("a", "b");
        map.addHeader("a", "c");
        map.getHeaders().values();
        assertEquals(map.getMultiHeaders().values().toString(), Arrays.asList("c", "b").toString());
        map.addHeader("a", null);
        assertEquals(map.getHeader("a"), null);
    }

    @Test
    public void testExtractMediaTypeStringFromValuesTest(){
        Stream<String> res = extractMediaTypeStringFromValues(new IteratorEnumeration(Stream.of("audio/*; q=0.2, audio/basic", "text/plain; q=0.5, text/html,\n" +
                "             text/x-dvi; q=0.8, text/x-c").collect(Collectors.toList()).iterator()
        ));
        Assert.assertEquals(res.collect(Collectors.toSet()), Stream.of("audio/*", "audio/basic", "text/plain", "text/html", "text/x-dvi", "text/x-c").collect(Collectors.toSet()));
    }

    @Test
    public void testCheckInsensitive() throws Exception {
        HeadersMultiMap map = new HeadersMultiMap();
        map.setHeader("a", null);
        assertEquals(map.getHeader("a"), null);
        map.setHeader("a", "B");
        map.setHeader("A", "b");
        Enumeration<String> aValues = map.getHeaderValues("A");
        assertEquals(StreamSupportUtils.enumerationToStream(aValues).collect(Collectors.toList()).size(), 1);
    }

    @Test
    public void testDeprecated() throws Exception {
        HeadersMultiMap map = new HeadersMultiMap();
        map.setHeader("a", "B");
        map.setHeader("A", "b");
        map.setHeader("A", "bCC");
        map.setHeader("other", "other");
        Enumeration<String> aValues = map.getHeaderValues("A");
        Enumeration<String> same = map.getHeaders("A");
        assertEquals(
                StreamSupportUtils.enumerationToStream(aValues).collect(Collectors.toList()),
                StreamSupportUtils.enumerationToStream(same).collect(Collectors.toList())
        );
    }

}