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

package org.artifactory.descriptor.repo;

import org.jfrog.client.http.model.ProxyConfig;
import org.testng.annotations.Test;
import org.testng.internal.Utils;

import static org.testng.Assert.*;

/**
 * Tests the ProxyDescriptor.
 *
 * @author Yossi Shaul
 */
@Test
public class ProxyDescriptorTest {

    public void defaultConstructor() {
        ProxyDescriptor proxy = new ProxyDescriptor();
        assertNull(proxy.getKey());
        assertNull(proxy.getHost());
        assertEquals(proxy.getPort(), 0);
        assertNull(proxy.getUsername());
        assertNull(proxy.getPassword());
        assertNull(proxy.getDomain());
    }

    public void redirectedToHosts() {
        ProxyDescriptor proxy = new ProxyDescriptor();
        String redirectedToHosts = "a,b;c\nd e";
        proxy.setRedirectedToHosts(redirectedToHosts);
        assertNotNull(proxy.getRedirectedToHosts());
        assertEquals(proxy.getRedirectedToHosts(), redirectedToHosts);
        String[] hostsList = proxy.getRedirectedToHostsList();
        assertNotNull(hostsList);
        assertEquals(hostsList.length, 5, "Unexpected redirect host list: " + Utils.arrayToString(hostsList));
        assertEquals(hostsList[2], "c");
    }

    public void toProxyConfig() {
        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setDefaultProxy(true);
        proxy.setDomain("domain");
        proxy.setHost("host");
        proxy.setKey("key");
        proxy.setNtHost("nthost");
        proxy.setUsername("user");
        proxy.setPassword("password");
        proxy.setRedirectedToHosts("redirect\nto;trusted,hosts");
        proxy.setPort(9999);

        ProxyConfig proxyConfig = proxy.toProxyConfig();

        assertEquals(proxy.getDomain(), proxyConfig.getDomain());
        assertEquals(proxy.getHost(), proxyConfig.getHost());
        assertEquals(proxy.getNtHost(), proxyConfig.getNtHost());
        assertEquals(proxy.getUsername(), proxyConfig.getUsername());
        assertEquals(proxy.getPassword(), proxyConfig.getPassword());
        assertEquals(proxy.getRedirectedToHosts(), proxyConfig.getRedirectedToHosts());
        assertEquals(proxy.getRedirectedToHostsList(), proxyConfig.getRedirectedToHostsList());
        assertEquals(proxy.getPort(), proxyConfig.getPort());
    }

}
