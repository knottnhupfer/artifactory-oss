package org.artifactory.ui.rest.model.continuous.translators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.artifactory.build.BuildId;
import org.artifactory.build.BuildInfoUtils;
import org.artifactory.ui.rest.model.builds.GeneralBuildInfo;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertNull;


@Test
public class ContinueBuildTranslatorTest {

    private long now = System.currentTimeMillis();
    private String nowStr = BuildInfoUtils.formatBuildTime(now);

    public void testBuildIdEntityToBase64() throws IOException {
        GeneralBuildInfo generalBuildInfo = new GeneralBuildInfo();
        generalBuildInfo.setBuildName("name");
        generalBuildInfo.setBuildNumber("1");
        generalBuildInfo.setLastBuildTime(nowStr);
        generalBuildInfo.setTime(now);
        String b64 = ContinueBuildTranslator.buildIdToBase64(generalBuildInfo);
        JsonNode jsonNode = new ObjectMapper().readTree(Base64.getDecoder().decode(b64));
        assertEquals("name", jsonNode.get("buildName").asText());
    }

    public void testBase64ToBuildIdEntity() {
        String json = "{\"buildName\":\"name\", \"buildNumber\":\"1\"," +
                " \"time\": " + now +"}";
        String b64 = Base64.getEncoder().encodeToString(json.getBytes());
        BuildId buildId = ContinueBuildTranslator.base64ToBuildId(b64);
        assertNotNull(buildId);
        assertEquals("1", buildId.getNumber());
        assertEquals(now, buildId.getStartedDate().getTime());
        assertEquals("name", buildId.getName());
    }

    public void testBoth() {
        GeneralBuildInfo generalBuildInfo = new GeneralBuildInfo();
        generalBuildInfo.setBuildName("name");
        generalBuildInfo.setBuildNumber("1");
        generalBuildInfo.setLastBuildTime(nowStr);
        generalBuildInfo.setTime(now);
        String b64 = ContinueBuildTranslator.buildIdToBase64(generalBuildInfo);
        generalBuildInfo = ContinueBuildTranslator.base64ToGeneralBuildInfo(b64);
        assertNotNull(generalBuildInfo);
        assertEquals("1", generalBuildInfo.getBuildNumber());
        assertEquals(now, generalBuildInfo.getTime().longValue());
        assertEquals("name", generalBuildInfo.getBuildName());
    }

    public void testBase64ToBuildId() {
        long now = System.currentTimeMillis();
        String json = "{\"buildName\":\"name\", \"buildNumber\":\"1\"," +
                " \"time\": " + now +"}";
        String b64 = Base64.getEncoder().encodeToString(json.getBytes());
        BuildId buildId = ContinueBuildTranslator.base64ToBuildId(b64);
        assertNotNull(buildId);
        assertEquals("1", buildId.getNumber());
        assertEquals(now, buildId.getStartedDate().getTime());
        assertEquals("name", buildId.getName());
    }

    public void testBase64ToBuildNull() {
        GeneralBuildInfo generalBuildInfo = ContinueBuildTranslator.base64ToGeneralBuildInfo(null);
        assertNull(generalBuildInfo);
    }

    public void testBase64ToBuildIdNull() {
        BuildId buildId = ContinueBuildTranslator.base64ToBuildId(null);
        assertNull(buildId);
    }

}
