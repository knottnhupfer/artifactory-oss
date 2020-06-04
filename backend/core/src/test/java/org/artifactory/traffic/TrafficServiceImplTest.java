package org.artifactory.traffic;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

/**
 * @author dudim
 */
@Test
public class TrafficServiceImplTest extends ArtifactoryHomeBoundTest {

    public void testGetDefaultTrafficLogDir() {
        TrafficServiceImpl trafficServiceSpy = spy(new TrafficServiceImpl());
        doReturn(new File("originalPath")).when(trafficServiceSpy).getDefaultTrafficLogDir();
        String logDirPath = trafficServiceSpy.getTrafficLogDir().getPath();
        assertEquals(logDirPath, "originalPath");
    }

    public void testGetNotExistingPathTrafficLogDir() {
        setTrafficLogsDirSystemPropertyProperty("NotExistPath");
        TrafficServiceImpl trafficServiceSpy = spy(new TrafficServiceImpl());
        doReturn(new File("originalPath")).when(trafficServiceSpy).getDefaultTrafficLogDir();
        String logDirPath = trafficServiceSpy.getTrafficLogDir().getPath();
        assertEquals(logDirPath, "originalPath");
    }

    public void testGetNewTrafficLogDir() {
        setTrafficLogsDirSystemPropertyProperty("/");
        TrafficServiceImpl trafficServiceSpy = new TrafficServiceImpl();
        String logDirPath = trafficServiceSpy.getTrafficLogDir().getPath();
        assertEquals(logDirPath, "/");
    }

    public void testGetEmptyDirectoryTrafficLogDir() {
        setTrafficLogsDirSystemPropertyProperty("");
        TrafficServiceImpl trafficServiceSpy = spy(new TrafficServiceImpl());
        doReturn(new File("originalPath")).when(trafficServiceSpy).getDefaultTrafficLogDir();
        String logDirPath = trafficServiceSpy.getTrafficLogDir().getPath();
        assertEquals("originalPath", logDirPath);
    }

    public void testGetSpaceDirectoryTrafficLogDir() {
        setTrafficLogsDirSystemPropertyProperty(" ");
        TrafficServiceImpl trafficServiceSpy = spy(new TrafficServiceImpl());
        doReturn(new File("originalPath")).when(trafficServiceSpy).getDefaultTrafficLogDir();
        String logDirPath = trafficServiceSpy.getTrafficLogDir().getPath();
        assertEquals(logDirPath, "originalPath");
    }

    public void testGetNullDirectoryTrafficLogDir() {
        setTrafficLogsDirSystemPropertyProperty(null);
        TrafficServiceImpl trafficServiceSpy = spy(new TrafficServiceImpl());
        doReturn(new File("originalPath")).when(trafficServiceSpy).getDefaultTrafficLogDir();
        String logDirPath = trafficServiceSpy.getTrafficLogDir().getPath();
        assertEquals(logDirPath, "originalPath");
    }

    private void setTrafficLogsDirSystemPropertyProperty(String dirPath) {
        ArtifactorySystemProperties artifactoryProperties = ArtifactoryHome.get().getArtifactoryProperties();
        artifactoryProperties.setProperty(ConstantValues.trafficLogsDirectory.getPropertyName(), dirPath);
    }
}