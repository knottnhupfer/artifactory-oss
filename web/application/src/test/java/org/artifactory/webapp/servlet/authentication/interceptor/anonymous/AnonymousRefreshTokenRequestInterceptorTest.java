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

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertEquals;

/**
 * Created by Yinon Avraham.
 */
public class AnonymousRefreshTokenRequestInterceptorTest {

    private static final String ANONYMOUS_BASIC_AUTH = "Basic " + Base64.getEncoder().encodeToString("anonymous:".getBytes());
    private static final String SOME_USER_BASIC_AUTH = "Basic " + Base64.getEncoder().encodeToString("someuser:password".getBytes());

    private final AnonymousRefreshTokenRequestInterceptor interceptor = new AnonymousRefreshTokenRequestInterceptor();

    @Test(dataProvider = "provideAcceptArguments")
    public void testAccept(String test, String method, String path, String authHeader, Map<String, String> queryParams, boolean expected) throws Exception {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getMethod()).andReturn(method).anyTimes();
        expect(request.getHeader("Authorization")).andReturn(authHeader).anyTimes();
        expect(request.getParameter(anyObject(String.class))).andAnswer(() -> queryParams.get(getCurrentArguments()[0].toString()));
        expect(request.getParameterNames()).andReturn(Collections.enumeration(queryParams.keySet()));
        expect(request.getContextPath()).andReturn("");
        expect(request.getRequestURI()).andReturn(path);
        replay(request);
        assertEquals(interceptor.accept(request), expected);
    }

    @DataProvider(name = "provideAcceptArguments")
    public static Object[][] provideAcceptArguments() {
        return new Object[][] {
                { "accept: no auth",           "POST", "/api/security/token", null, ImmutableMap.of("grant_type", "refresh_token", "refresh_token", "asdf", "access_token", "qwer"), true },
                { "accept: anonymous",         "POST", "/api/security/token", ANONYMOUS_BASIC_AUTH, ImmutableMap.of("grant_type", "refresh_token", "refresh_token", "asdf", "access_token", "qwer"), true },
                { "reject: method",            "GET",  "/api/security/token", null, ImmutableMap.of("grant_type", "refresh_token", "refresh_token", "asdf", "access_token", "qwer"), false },
                { "reject: url1",              "POST", "/api/security/token/revoke", null, ImmutableMap.of("grant_type", "refresh_token", "refresh_token", "asdf", "access_token", "qwer"), false },
                { "reject: url2",              "POST", "/api/security", null, ImmutableMap.of("grant_type", "refresh_token", "refresh_token", "asdf", "access_token", "qwer"), false },
                { "reject: grant_type",        "POST", "/api/security/token", null, ImmutableMap.of("grant_type", "client_credentials", "refresh_token", "asdf", "access_token", "qwer"), false },
                { "reject: too many q params", "POST", "/api/security/token", null, ImmutableMap.of("grant_type", "refresh_token", "refresh_token", "asdf", "access_token", "qwer", "scope", "afasfa"), false },
                { "reject: access_token",      "POST", "/api/security/token", null, ImmutableMap.of("grant_type", "refresh_token", "refresh_token", "asdf"), false },
                { "reject: refresh_token",     "POST", "/api/security/token", null, ImmutableMap.of("grant_type", "refresh_token", "access_token", "qwer"), false },
                { "reject: wrong method",      "POST", "/api/security/token", SOME_USER_BASIC_AUTH, ImmutableMap.of("grant_type", "refresh_token", "refresh_token", "asdf", "access_token", "qwer"), false }
        };
    }
}