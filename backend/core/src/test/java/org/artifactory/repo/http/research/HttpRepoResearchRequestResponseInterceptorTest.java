package org.artifactory.repo.http.research;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.message.BasicStatusLine;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.artifactory.repo.http.research.HttpRepoResearchRequestResponseInterceptor.REQUEST_PATH;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Uriah Levy
 */
@Test
public class HttpRepoResearchRequestResponseInterceptorTest extends ArtifactoryHomeBoundTest {

    public void testRepoResearchRequestInterceptorEnabled() {
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(ConstantValues.remoteRepoResearchInterceptorRepoKeys.getPropertyName(), "npm-remote");
        HttpRepoResearchRequestResponseInterceptor requestInterceptor = new HttpRepoResearchRequestResponseInterceptor(
                "npm-remote");
        HttpRequest request = mock(HttpRequest.class);
        when(request.getRequestLine())
                .thenReturn(new BasicRequestLine("GET", "foo/bar", new ProtocolVersion("HTTP", 1, 1)));
        HttpClientContext context = new HttpClientContext();

        requestInterceptor.process(request, context);
        // Original request path recorded
        assertEquals("foo/bar", context.getAttribute(REQUEST_PATH));
    }


    public void testRepoResearchRequestInterceptorEnabledWithSpaces() {
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(ConstantValues.remoteRepoResearchInterceptorRepoKeys.getPropertyName(), "npm-remote ,npm-remote-2 ");
        HttpRepoResearchRequestResponseInterceptor requestInterceptor = new HttpRepoResearchRequestResponseInterceptor(
                "npm-remote");
        HttpRequest request = mock(HttpRequest.class);
        when(request.getRequestLine())
                .thenReturn(new BasicRequestLine("GET", "foo/bar", new ProtocolVersion("HTTP", 1, 1)));
        HttpClientContext context = new HttpClientContext();

        requestInterceptor.process(request, context);
        // Original request path recorded
        assertEquals("foo/bar", context.getAttribute(REQUEST_PATH));


        requestInterceptor = new HttpRepoResearchRequestResponseInterceptor(
                "npm-remote-2");
        request = mock(HttpRequest.class);
        when(request.getRequestLine())
                .thenReturn(new BasicRequestLine("GET", "foo/bar", new ProtocolVersion("HTTP", 1, 1)));
        context = new HttpClientContext();

        requestInterceptor.process(request, context);
        // Original request path recorded
        assertEquals("foo/bar", context.getAttribute(REQUEST_PATH));
    }

    public void testResponseWithInterceptorDisabled() {
        HttpRepoResearchRequestResponseInterceptor requestInterceptor = new HttpRepoResearchRequestResponseInterceptor(
                "npm-remote");
        HttpRequest request = mock(HttpRequest.class);
        when(request.getRequestLine())
                .thenReturn(new BasicRequestLine("GET", "foo/bar", new ProtocolVersion("HTTP", 1, 1)));
        HttpClientContext context = new HttpClientContext();

        requestInterceptor.process(request, context);

        assertNull(context.getAttribute(REQUEST_PATH));
    }

    public void testResponseWithInterceptorEnabled() {
        setResearchedKeys();
        HttpRepoResearchRequestResponseInterceptor responseInterceptor = new HttpRepoResearchRequestResponseInterceptor(
                "npm-remote");

        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusLine())
                .thenReturn(
                        new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_NOT_FOUND, "Not found"));
        HttpClientContext context = spy(new HttpClientContext());
        responseInterceptor.process(response, context);

        verify(context).getAttribute(REQUEST_PATH);
        unsetResearchedKeys();
    }

    public void testRepoResearchRequestInterceptorEnabledWithStatusOK() {
        setResearchedKeys();
        HttpRepoResearchRequestResponseInterceptor responseInterceptor = new HttpRepoResearchRequestResponseInterceptor(
                "npm-remote");

        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusLine())
                .thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "OK"));
        HttpClientContext context = spy(new HttpClientContext());
        responseInterceptor.process(response, context);

        verify(context, times(0)).getAttribute(REQUEST_PATH);
        unsetResearchedKeys();
    }

    public void testRepoResearchRequestInterceptorDisabled() {
        HttpRepoResearchRequestResponseInterceptor responseInterceptor = new HttpRepoResearchRequestResponseInterceptor(
                "npm-remote");

        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusLine())
                .thenReturn(
                        new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_NOT_FOUND, "Not found"));
        HttpClientContext context = spy(new HttpClientContext());
        responseInterceptor.process(response, context);

        verify(context, times(0)).getAttribute(REQUEST_PATH);
    }

    private void setResearchedKeys() {
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(ConstantValues.remoteRepoResearchInterceptorRepoKeys.getPropertyName(), "npm-remote");
    }

    private void unsetResearchedKeys() {
        ArtifactoryHome.get().getArtifactoryProperties()
                .removeProperty((ConstantValues.remoteRepoResearchInterceptorRepoKeys));
    }
}