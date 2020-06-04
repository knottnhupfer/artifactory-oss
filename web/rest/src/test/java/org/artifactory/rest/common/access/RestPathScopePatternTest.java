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

package org.artifactory.rest.common.access;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;


/**
 * @author Yinon Avraham.
 */

public class RestPathScopePatternTest {

    @Test(dataProvider = "provideMatchRequestToPathScopePattern")
    public void testMatchRequestToPathScopePattern(String scopeToken, String basePath, String path, boolean expected) {
        Optional<RestPathScopePattern> scopePattern = RestPathScopePattern.parseOptional(scopeToken);
        assertTrue(scopePattern.isPresent());
        ContainerRequest request = createNiceMock(ContainerRequest.class);
        expect(request.getBaseUri()).andReturn(URI.create(basePath)).anyTimes();
        expect(request.getPath(anyBoolean())).andReturn(path).anyTimes();
        expect(request.getUriInfo()).andAnswer(() -> new UriRoutingContext(request)).anyTimes();
        replay(request);
        assertEquals(scopePattern.get().matches(request), expected);
    }

    @DataProvider(name = "provideMatchRequestToPathScopePattern")
    public static Object[][] provideMatchRequestToPathScopePattern() {
        return new Object[][]{
                {"api:*", "http://acme.com/foo/api/", "foo/bar", true},
                {"api:*", "http://acme.com/foo/api/", "", true},
                {"ui:*", "http://acme.com/foo/api/", "foo", false},
                {"ui:*", "http://acme.com/foo/ui/", "foo", true},
                {"api:foo/*", "http://acme.com/foo/api/", "foo/bar", true},
                {"api:foo/*", "http://acme.com/foo/api/", "foo/bar/zoo", true},
                {"api:foo/*", "http://acme.com/foo/api/", "bar/foo", false},
                {"api:foo/bar", "http://acme.com/foo/api/", "foo/bar", true},
                {"api:foo/bar", "http://acme.com/foo/api/", "foo/bar/zoo", false}
        };
    }

    @Test(dataProvider = "provideParsePathPatternScope")
    public void testParseOptionalPathPatternScope(String scopeToken, boolean expected) {
        Optional<RestPathScopePattern> scopePattern = RestPathScopePattern.parseOptional(scopeToken);
        assertEquals(scopePattern.isPresent(), expected);
    }

    @DataProvider(name = "provideParsePathPatternScope")
    public static Object[][] provideParsePathPatternScope() {
        return new Object[][]{
                {"api:*", true},
                {"api:foo/bar", true},
                {"api:foo/bar/*", true},
                {"ui:*", true},
                {"ui:foo/bar", true},
                {"ui:foo/bar/*", true},
                {"foo/bar", false},
                {"foo:bar", false},
                {"foo:*", false},
                {"bar:*", false}
        };
    }

    @Test(dataProvider = "provideParsePathPatternScope")
    public void testParseThrowsForNonPathPatternScopeToken(String scopeToken, boolean successExpected) {
        try {
            RestPathScopePattern pattern = RestPathScopePattern.parse(scopeToken);
            if (!successExpected) {
                fail("Parse was expected to fail for scope token: " + scopeToken);
            }
            assertNotNull(pattern);
        } catch (IllegalArgumentException e) {
            if (successExpected) {
                fail("Parse was expected to succeed for scope token: " + scopeToken);
            }
        }
    }
}
