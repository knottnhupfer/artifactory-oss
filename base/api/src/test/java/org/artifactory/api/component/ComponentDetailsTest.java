package org.artifactory.api.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.MimeType;
import org.jfrog.common.ArgUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ComponentDetailsTest {

    @Test
    public void testUnMarshall() throws IOException {
        ComponentDetails componentDetails = ComponentDetails.builder()
                .componentType(RepoType.Maven)
                .extension("jar")
                .mimeType("application/java-archive")
                .name("yoaz.jar")
                .version("1.0.x")
                .build();
        String jsonDetails = ComponentDetails.toJson(componentDetails);
        ArgUtils.requireNullOrJson(jsonDetails, jsonDetails + " is not a valid json");
        JsonNode jsonNode = new ObjectMapper().readTree(jsonDetails);
        assertEquals("\"jar\"", jsonNode.get("extension").toString());
        assertEquals("\"application/java-archive\"", jsonNode.get("mimeType").toString());
        assertEquals("\"1.0.x\"", jsonNode.get("version").toString());
        assertEquals("\"1.0.x\"", jsonNode.get("version").toString());
        assertEquals("\"Maven\"", jsonNode.get("componentType").toString());
    }

    @Test
    public void testMarshall() {
        String str = "{\"name\":\"yoaz.jar\",\"version\":\"1.0.x\",\"componentType\":\"Maven\",\"extension\":\"jar\",\"mimeType\":\"application/java-archive\"}";
        ComponentDetails componentDetails = ComponentDetails.fromJson(str);
        assertNotNull(componentDetails);
        assertEquals("yoaz.jar", componentDetails.getName());
        assertEquals("1.0.x", componentDetails.getVersion());
        assertEquals(RepoType.Maven, componentDetails.getComponentType());
        assertEquals("jar", componentDetails.getExtension());
        assertEquals("application/java-archive", componentDetails.getMimeType());
    }

    @Test
    public void testMarshallDefaultMimeType() {
        String str = "{\"name\":\"yoaz.jar\",\"version\":\"1.0.x\",\"componentType\":\"Maven\",\"extension\":\"jar\"}";
        ComponentDetails componentDetails = ComponentDetails.fromJson(str);
        assertNotNull(componentDetails);
        assertEquals("yoaz.jar", componentDetails.getName());
        assertEquals("1.0.x", componentDetails.getVersion());
        assertEquals(RepoType.Maven, componentDetails.getComponentType());
        assertEquals("jar", componentDetails.getExtension());
        assertEquals(MimeType.def.getType(), componentDetails.getMimeType());
    }

}