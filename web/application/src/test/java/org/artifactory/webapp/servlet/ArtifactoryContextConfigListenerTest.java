package org.artifactory.webapp.servlet;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author dudim
 */
@PrepareForTest({ArtifactoryEdition.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*"})
public class ArtifactoryContextConfigListenerTest extends PowerMockTestCase {

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(ArtifactoryEdition.class);
    }

    @Test
    public void testBuildEditionMessageJcrAol() throws Exception {
        ArtifactoryHome artifactoryHome = new ArtifactoryHome(message -> { });
        when(ArtifactoryEdition.detect(eq(artifactoryHome))).thenReturn(ArtifactoryEdition.aolJCR);

        ArtifactoryContextConfigListener artifactoryContextConfigListener = new ArtifactoryContextConfigListener();
        String editionMessage = artifactoryContextConfigListener.buildEditionMessage(artifactoryHome, new CompoundVersionDetails(
                ArtifactoryVersion.getCurrent(),"55", System.currentTimeMillis()));

        Pattern unexpectedPattern = Pattern.compile("Version: .*", Pattern.DOTALL);
        Pattern expectedPattern = Pattern.compile("Revision: .*", Pattern.DOTALL);
        Assert.assertFalse(unexpectedPattern.matcher(editionMessage).find());
        Assert.assertTrue(expectedPattern.matcher(editionMessage).find());

    }

    @Test
    public void testBuildEditionMessageJcr() throws Exception {
        ArtifactoryHome artifactoryHome = new ArtifactoryHome(message -> { });
        when(ArtifactoryEdition.detect(eq(artifactoryHome))).thenReturn(ArtifactoryEdition.jcr);

        ArtifactoryContextConfigListener artifactoryContextConfigListener = new ArtifactoryContextConfigListener();
        String editionMessage = artifactoryContextConfigListener.buildEditionMessage(artifactoryHome, new CompoundVersionDetails(
                ArtifactoryVersion.getCurrent(),"55", System.currentTimeMillis()));

        Pattern unexpectedPattern = Pattern.compile("Version: .*", Pattern.DOTALL);
        Pattern expectedPattern = Pattern.compile("Revision: .*", Pattern.DOTALL);
        Assert.assertTrue(unexpectedPattern.matcher(editionMessage).find());
        Assert.assertTrue(expectedPattern.matcher(editionMessage).find());
    }
}