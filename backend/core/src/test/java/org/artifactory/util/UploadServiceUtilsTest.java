package org.artifactory.util;

import org.apache.http.HttpStatus;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.request.ArtifactoryRequest;
import org.jfrog.storage.binstore.common.ReaderTrackingInputStream;
import org.jfrog.storage.binstore.ifc.UsageTracking;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

import static org.artifactory.util.UploadServiceUtils.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertFalse;

/**
 * @author dudim
 */
public class UploadServiceUtilsTest {

    @Test
    public void testPopulateAndValidateItemPropertiesFromRequestChecksumDeployWithNoAnnotatePermission()
            throws RepoRejectException, UnsupportedEncodingException {
        ArtifactoryRequest requestMock = mock(ArtifactoryRequest.class);
        PropertiesImpl requestProps = new PropertiesImpl();
        requestProps.put("propKey", "propValue");
        when(requestMock.getProperties()).thenReturn(requestProps);
        when(requestMock.getHeader(ArtifactoryRequest.CHECKSUM_DEPLOY)).thenReturn("true");
        when(requestMock.getHeader(ArtifactoryRequest.CHECKSUM_SHA256)).thenReturn("15e470ec647ae0e6734ec2a397d8be444063aebab70fb6775c1a1e5f042b0ae6");
        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/uploaded/binary.tgz");
        AuthorizationService authServiceMock = mock(AuthorizationService.class);
        when(authServiceMock.canAnnotate(repoPath)).thenReturn(false);
        UsageTracking usageTrackingMock = mock(UsageTracking.class);
        when(usageTrackingMock.incrementNoDeleteLock("SHA1")).thenReturn(1);
        ReaderTrackingInputStream readerTrackingInputStream = new ReaderTrackingInputStream(new StringInputStream("file body"),"SHA1", usageTrackingMock);
        Properties properties = populateAndValidateItemPropertiesFromRequest(requestMock, readerTrackingInputStream, repoPath, authServiceMock);
        assertNotNull(properties);
        assertEquals(properties.size(), 1);
        assertEquals(properties.getFirst("sha256"), "15e470ec647ae0e6734ec2a397d8be444063aebab70fb6775c1a1e5f042b0ae6");
        verify(authServiceMock, times(1)).canAnnotate(repoPath);
    }

    @Test
    public void testPopulateAndValidateItemPropertiesFromRequestChecksumDeployValidScenario()
            throws RepoRejectException, UnsupportedEncodingException {
        ArtifactoryRequest requestMock = mock(ArtifactoryRequest.class);
        PropertiesImpl requestProps = new PropertiesImpl();
        requestProps.put("propKey", "propValue");
        when(requestMock.getProperties()).thenReturn(requestProps);
        when(requestMock.getHeader(ArtifactoryRequest.CHECKSUM_DEPLOY)).thenReturn("true");
        when(requestMock.getHeader(ArtifactoryRequest.CHECKSUM_SHA256)).thenReturn("15e470ec647ae0e6734ec2a397d8be444063aebab70fb6775c1a1e5f042b0ae6");
        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/uploaded/binary.tgz");
        AuthorizationService authServiceMock = mock(AuthorizationService.class);
        when(authServiceMock.canAnnotate(repoPath)).thenReturn(true);
        UsageTracking usageTrackingMock = mock(UsageTracking.class);
        when(usageTrackingMock.incrementNoDeleteLock("SHA1")).thenReturn(1);
        ReaderTrackingInputStream readerTrackingInputStream = new ReaderTrackingInputStream(new StringInputStream("file body"),"SHA1", usageTrackingMock);
        Properties properties = populateAndValidateItemPropertiesFromRequest(requestMock, readerTrackingInputStream, repoPath, authServiceMock);
        assertNotNull(properties);
        assertEquals(properties.size(), 2);
        assertEquals(properties.getFirst("sha256"), "15e470ec647ae0e6734ec2a397d8be444063aebab70fb6775c1a1e5f042b0ae6");
        assertEquals(properties.getFirst("propKey"), "propValue");
        verify(authServiceMock, times(1)).canAnnotate(repoPath);
    }

    @Test
    public void testPopulateAndValidateItemPropertiesFromRequestNoAnnotatePermission() throws RepoRejectException {
        ArtifactoryRequest requestMock = mock(ArtifactoryRequest.class);
        PropertiesImpl requestProps = new PropertiesImpl();
        requestProps.put("propKey", "propValue");
        when(requestMock.getProperties()).thenReturn(requestProps);

        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/uploaded/binary.tgz");

        AuthorizationService authServiceMock = mock(AuthorizationService.class);
        when(authServiceMock.canAnnotate(repoPath)).thenReturn(false);
        Properties properties = populateAndValidateItemPropertiesFromRequest(requestMock, null, repoPath, authServiceMock);
        assertNull(properties);
        verify(authServiceMock, times(1)).canAnnotate(repoPath);
    }

    @Test
    public void testPopulateAndValidateItemPropertiesFromRequestWithNullProperties() throws RepoRejectException {
        ArtifactoryRequest requestMock = mock(ArtifactoryRequest.class);
        when(requestMock.getProperties()).thenReturn(null);

        AuthorizationService authServiceMock = mock(AuthorizationService.class);

        Properties properties = populateAndValidateItemPropertiesFromRequest(requestMock, null, null, authServiceMock);
        assertNull(properties);
        verify(authServiceMock, times(0)).canAnnotate(any());
    }

    @Test
    public void testPopulateAndValidateItemPropertiesFromRequestWithEmptyProperties() throws RepoRejectException {
        ArtifactoryRequest requestMock = mock(ArtifactoryRequest.class);
        when(requestMock.getProperties()).thenReturn(new PropertiesImpl());

        AuthorizationService authServiceMock = mock(AuthorizationService.class);

        Properties properties = populateAndValidateItemPropertiesFromRequest(requestMock, null, null, authServiceMock);
        assertNull(properties);
        verify(authServiceMock, times(0)).canAnnotate(any());
    }

    @Test(expectedExceptions = RepoRejectException.class)
    public void testPopulateAndValidateItemPropertiesFromRequestWithUnvalidProperties() throws RepoRejectException {
        ArtifactoryRequest requestMock = mock(ArtifactoryRequest.class);
        PropertiesImpl requestProps = new PropertiesImpl();
        requestProps.put("555propKey", "propValue");
        when(requestMock.getProperties()).thenReturn(requestProps);

        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/uploaded/binary.tgz");

        AuthorizationService authServiceMock = mock(AuthorizationService.class);
        when(authServiceMock.canAnnotate(repoPath)).thenReturn(true);

        try {
            populateAndValidateItemPropertiesFromRequest(requestMock, null, repoPath, authServiceMock);
        } catch (RepoRejectException e) {
            assertEquals(e.getErrorCode(), HttpStatus.SC_BAD_REQUEST);
            throw e;
        }
    }

    @Test
    public void testPopulateAndValidateItemPropertiesFromRequestValidScenario() throws RepoRejectException {
        ArtifactoryRequest requestMock = mock(ArtifactoryRequest.class);
        PropertiesImpl requestProps = new PropertiesImpl();
        requestProps.put("propKey", "propValue");
        when(requestMock.getProperties()).thenReturn(requestProps);

        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/uploaded/binary.tgz");

        AuthorizationService authServiceMock = mock(AuthorizationService.class);
        when(authServiceMock.canAnnotate(repoPath)).thenReturn(true);
        Properties properties = populateAndValidateItemPropertiesFromRequest(requestMock, null, repoPath,
                authServiceMock);

        assertNotNull(properties);
        assertEquals(properties.get("propKey").size(), 1);
        assertTrue(properties.get("propKey").contains("propValue"));
    }

    @Test
    public void testValidateCanAnnotateNoAnnotatePermission() throws RepoRejectException {
        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/uploaded/binary.tgz");
        AuthorizationService authServiceMock = mock(AuthorizationService.class);
        when(authServiceMock.canAnnotate(repoPath)).thenReturn(false);
        assertFalse(validateCanAnnotate(repoPath, authServiceMock));
    }

    @Test
    public void testValidateCanAnnotateValidScenario() throws RepoRejectException {
        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/uploaded/binary.tgz");
        AuthorizationService authServiceMock = mock(AuthorizationService.class);
        when(authServiceMock.canAnnotate(repoPath)).thenReturn(true);
        assertTrue(validateCanAnnotate(repoPath, authServiceMock));
    }

    @Test(expectedExceptions = RepoRejectException.class)
    public void testValidatePropertiesUnvalidProps() throws RepoRejectException {
        PropertiesImpl properties = new PropertiesImpl();
        properties.put("555propKey", "value");
        try {
            validateProperties(properties);
        } catch (RepoRejectException e) {
            assertEquals(e.getErrorCode(), HttpStatus.SC_BAD_REQUEST);
            throw e;
        }
    }

    @Test
    public void testValidatePropertiesValidProps() throws RepoRejectException {
        PropertiesImpl properties = new PropertiesImpl();
        properties.put("propKey", "value");
        validateProperties(properties);
    }
}