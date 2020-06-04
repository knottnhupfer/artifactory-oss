package org.artifactory.version.converter.v224;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.artifactory.version.converter.v224.EnablePushingSchema1DockerConverter.BLOCK_SCHEMA1_ELEMENT;
import static org.artifactory.version.converter.v224.EnablePushingSchema1DockerConverter.isDockerRepo;
import static org.testng.Assert.*;

/**
 * Tests {@link EnablePushingSchema1DockerConverter}
 *
 * @author Rotem Kfir
 */
@Test
public class EnablePushingSchema1DockerConverterTest extends XmlConverterTest {

    public void testAddBlockPushingSchema1Field() throws Exception {
        Document doc = convertXml("/config/test/config.2.2.3.with.docker.repos.xml",
                new EnablePushingSchema1DockerConverter());
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        
        assertFieldAddedToDockerRepos("local", rootElement, namespace);
        assertFieldAddedToDockerRepos("remote", rootElement, namespace);
    }

    private void assertFieldAddedToDockerRepos(String type, Element rootElement, Namespace namespace) {
        Element repos = rootElement.getChild(type + "Repositories", namespace);
        assertNotNull(repos);
        List<Element> repositories = repos.getChildren(type + "Repository", namespace);
        assertNotNull(repositories);
        assertTrue(repositories.size() > 0);
        for (Element repository : repositories) {
            if (isDockerRepo(repository, namespace)) {
                Element element = repository.getChild(BLOCK_SCHEMA1_ELEMENT, namespace);
                assertNotNull(element);
                assertEquals(element.getText(), "false");
            }
        }
    }
}