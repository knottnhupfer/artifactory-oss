package org.artifactory.webapp.servlet;

import org.apache.http.HttpHeaders;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Alexei Vainshtein
 */
public class RequestFilterTest extends PowerMockTestCase {

    private RequestFilter requestFilter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private RequestFilter.CapturingHttpServletResponseWrapper response;

    @BeforeMethod
    public void setUp() throws Exception {
        requestFilter = new RequestFilter();
        MockitoAnnotations.initMocks(this);
        Mockito.when(request.getHeader(HttpHeaders.CONTENT_LENGTH)).thenReturn("444");
        Mockito.when(response.getContentLength()).thenReturn(333L);
    }

    @Test(dataProvider = "providerMethodWithContent", description = "RTFACT-10404")
    public void testGetContentLengthNoGet(String method, boolean shouldReturnSize) {
        long contentLength = requestFilter.getContentLength(request, response, method);
        if (shouldReturnSize) {
            Assert.assertEquals(contentLength, 444L);
        } else {
            Assert.assertEquals(contentLength, 0);
        }
        Mockito.verify(request, Mockito.times(shouldReturnSize ? 1 : 0)).getHeader(HttpHeaders.CONTENT_LENGTH);
        Mockito.verify(response, Mockito.times(0)).getContentLength();
    }

    @Test(dataProvider = "providerMethodWithoutContent", description = "RTFACT-10404")
    public void testGetContentLengthWithGet(String method, boolean shouldReturnSize) {
        long contentLength = requestFilter.getContentLength(request, response, method);
        if (shouldReturnSize) {
            Assert.assertEquals(contentLength, 333L);
        } else {
            Assert.assertEquals(contentLength, 0);
        }
        Mockito.verify(response, Mockito.times(shouldReturnSize ? 1 : 0)).getContentLength();
        Mockito.verify(request, Mockito.times(0)).getHeader(HttpHeaders.CONTENT_LENGTH);
    }

    @DataProvider
    public static Object[][] providerMethodWithContent() {
        return new Object[][]{
                {"POST", true},
                {"PUT", true},
                {"PATCH", true},
                {"OPTIONS", false}
        };
    }

    @DataProvider
    public static Object[][] providerMethodWithoutContent() {
        return new Object[][] {
                {"GET", true},
                {"DELETE", false},
                {"TRACE", false}
        };
    }
}
