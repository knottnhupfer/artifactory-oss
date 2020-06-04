package org.artifactory.repo;

import org.apache.http.*;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Test
public class JFrogRedirectStrategyTest {

    private static final int REDIRECT_PERMANENTLY = 308;

    @Test
    void testRedirectedPermanently() throws ProtocolException {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        RequestLine requestLine = mock(RequestLine.class);
        when(statusLine.getStatusCode()).thenReturn(REDIRECT_PERMANENTLY);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(request.getRequestLine()).thenReturn(requestLine);
        when(requestLine.getMethod()).thenReturn("GET");
        JFrogRedirectStrategy jFrogRedirectStrategy = new JFrogRedirectStrategy();
        assertTrue(jFrogRedirectStrategy.isRedirected(request, response, null));
    }
}