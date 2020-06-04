package org.artifactory.engine;

import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.ArtifactoryRequest;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * @author dudim
 */
public class UploadServiceImplTest {

    @Test
    public void testAnnotateWithRequestPropertiesIfPermittedNoAnnotatePermission()
            throws RepoRejectException, IOException {
        PropertiesService propertiesServiceMock = mock(PropertiesService.class);
        AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
        InternalRepositoryService repoServiceMock = mock(InternalRepositoryService.class);
        UploadServiceImpl uploadServiceSpy = spy(new UploadServiceImpl());
        uploadServiceSpy.setAuthService(authorizationServiceMock);
        uploadServiceSpy.setRepoService(repoServiceMock);
        uploadServiceSpy.setPropertiesService(propertiesServiceMock);

        ArtifactoryRequest artifactoryRequestMock = mock(ArtifactoryRequest.class);
        PropertiesImpl properties = new PropertiesImpl();
        properties.put("propKey", "propValue");
        when(artifactoryRequestMock.getProperties()).thenReturn(properties);
        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/folder");
        when(artifactoryRequestMock.getRepoPath()).thenReturn(repoPath);
        when(authorizationServiceMock.canAnnotate(repoPath)).thenReturn(false);
        when(repoServiceMock.mkdirs(repoPath)).thenReturn(true);
        doNothing().when(uploadServiceSpy).sendSuccessfulResponse(any(), any(), any(), anyBoolean());
        uploadServiceSpy.createDirectory(artifactoryRequestMock, mock(ArtifactoryResponse.class));
        verify(propertiesServiceMock, times(0)).setProperties(eq(repoPath), any(), anyBoolean());
        verify(uploadServiceSpy, times(0)).validateRequestProperties(any());
    }

    @Test
    public void testAnnotateWithRequestPropertiesIfPermittedNoRequestProps() throws RepoRejectException, IOException {
        PropertiesService propertiesServiceMock = mock(PropertiesService.class);
        AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
        InternalRepositoryService repoServiceMock = mock(InternalRepositoryService.class);
        UploadServiceImpl uploadServiceSpy = spy(new UploadServiceImpl());
        uploadServiceSpy.setAuthService(authorizationServiceMock);
        uploadServiceSpy.setRepoService(repoServiceMock);
        uploadServiceSpy.setPropertiesService(propertiesServiceMock);

        ArtifactoryRequest artifactoryRequestMock = mock(ArtifactoryRequest.class);
        when(artifactoryRequestMock.getProperties()).thenReturn(null);
        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/folder");
        when(artifactoryRequestMock.getRepoPath()).thenReturn(repoPath);
        when(authorizationServiceMock.canAnnotate(repoPath)).thenReturn(true);
        when(repoServiceMock.mkdirs(repoPath)).thenReturn(true);
        doNothing().when(uploadServiceSpy).sendSuccessfulResponse(any(), any(), any(), anyBoolean());
        uploadServiceSpy.createDirectory(artifactoryRequestMock, mock(ArtifactoryResponse.class));
        verify(propertiesServiceMock, times(0)).setProperties(eq(repoPath), any(), anyBoolean());
        verify(uploadServiceSpy, times(1)).validateRequestProperties(any());
    }

    @Test
    public void testAnnotateWithRequestPropertiesIfPermittedEmptyRequestProps() throws RepoRejectException,
            IOException {
        PropertiesService propertiesServiceMock = mock(PropertiesService.class);
        AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
        InternalRepositoryService repoServiceMock = mock(InternalRepositoryService.class);
        UploadServiceImpl uploadServiceSpy = spy(new UploadServiceImpl());
        uploadServiceSpy.setAuthService(authorizationServiceMock);
        uploadServiceSpy.setRepoService(repoServiceMock);
        uploadServiceSpy.setPropertiesService(propertiesServiceMock);

        ArtifactoryRequest artifactoryRequestMock = mock(ArtifactoryRequest.class);
        when(artifactoryRequestMock.getProperties()).thenReturn(new PropertiesImpl());
        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/folder");
        when(artifactoryRequestMock.getRepoPath()).thenReturn(repoPath);
        when(authorizationServiceMock.canAnnotate(repoPath)).thenReturn(true);
        when(repoServiceMock.mkdirs(repoPath)).thenReturn(true);
        doNothing().when(uploadServiceSpy).sendSuccessfulResponse(any(), any(), any(), anyBoolean());
        uploadServiceSpy.createDirectory(artifactoryRequestMock, mock(ArtifactoryResponse.class));
        verify(propertiesServiceMock, times(0)).setProperties(eq(repoPath), any(), anyBoolean());
        verify(uploadServiceSpy, times(1)).validateRequestProperties(any());
    }

    @Test(expectedExceptions = RepoRejectException.class)
    public void testAnnotateWithRequestPropertiesIfPermittedUnvalidProps() throws RepoRejectException, IOException {
        PropertiesService propertiesServiceMock = mock(PropertiesService.class);
        AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
        InternalRepositoryService repoServiceMock = mock(InternalRepositoryService.class);
        UploadServiceImpl uploadServiceSpy = spy(new UploadServiceImpl());
        uploadServiceSpy.setAuthService(authorizationServiceMock);
        uploadServiceSpy.setRepoService(repoServiceMock);
        uploadServiceSpy.setPropertiesService(propertiesServiceMock);

        ArtifactoryRequest artifactoryRequestMock = mock(ArtifactoryRequest.class);
        PropertiesImpl properties = new PropertiesImpl();
        properties.put("11propKey", "propValue");
        when(artifactoryRequestMock.getProperties()).thenReturn(properties);
        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/folder");
        when(artifactoryRequestMock.getRepoPath()).thenReturn(repoPath);
        when(authorizationServiceMock.canAnnotate(repoPath)).thenReturn(true);
        RepoRejectException repoRejectException = null;

        try {
            uploadServiceSpy.createDirectory(artifactoryRequestMock, mock(ArtifactoryResponse.class));
        } catch (RepoRejectException e) {
            assertEquals(e.getErrorCode(), 400);
            repoRejectException = e;
        }
        verify(propertiesServiceMock, times(0)).setProperties(eq(repoPath), any(), anyBoolean());
        verify(uploadServiceSpy, times(1)).validateRequestProperties(any());
        verify(repoServiceMock, times(0)).mkdirs(eq(repoPath));
        throw repoRejectException;
    }

    @Test
    public void testAnnotateWithRequestPropertiesIfPermittedValidCase() throws RepoRejectException, IOException {
        PropertiesService propertiesServiceMock = mock(PropertiesService.class);
        AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
        InternalRepositoryService repoServiceMock = mock(InternalRepositoryService.class);
        UploadServiceImpl uploadServiceSpy = spy(new UploadServiceImpl());
        uploadServiceSpy.setAuthService(authorizationServiceMock);
        uploadServiceSpy.setRepoService(repoServiceMock);
        uploadServiceSpy.setPropertiesService(propertiesServiceMock);

        ArtifactoryRequest artifactoryRequestMock = mock(ArtifactoryRequest.class);
        PropertiesImpl properties = new PropertiesImpl();
        properties.put("propKey", "propValue");
        when(artifactoryRequestMock.getProperties()).thenReturn(properties);
        RepoPath repoPath = RepoPathFactory.create("repoKey", "path/to/folder");
        when(artifactoryRequestMock.getRepoPath()).thenReturn(repoPath);
        when(authorizationServiceMock.canAnnotate(repoPath)).thenReturn(true);
        when(repoServiceMock.mkdirs(repoPath)).thenReturn(true);
        doNothing().when(uploadServiceSpy).sendSuccessfulResponse(any(), any(), any(), anyBoolean());
        uploadServiceSpy.createDirectory(artifactoryRequestMock, mock(ArtifactoryResponse.class));
        verify(propertiesServiceMock, times(1)).setProperties(eq(repoPath), eq(properties), eq(false));
        verify(uploadServiceSpy, times(1)).validateRequestProperties(any());
    }
}