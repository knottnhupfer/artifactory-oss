package org.artifactory.version.converter.v224;

import org.apache.commons.collections.CollectionUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Adds the new 'blockPushingSchema1' with value false to the local and remote repo descriptors, in order to continue
 * support of pushing images with manifest v2 schema1. The default value for a new repo will be blockPushingSchema1=true
 *
 * @author Rotem Kfir
 */
public class EnablePushingSchema1DockerConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(EnablePushingSchema1DockerConverter.class);
    static final String BLOCK_SCHEMA1_ELEMENT = "blockPushingSchema1";

    @Override
    public void convert(Document doc) {
        log.info("Started {}", this.getClass().getSimpleName());
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        convertRepositories("local", rootElement, namespace);
        convertRepositories("remote", rootElement, namespace);

        log.info("Finished {}", this.getClass().getSimpleName());
    }

    private void convertRepositories(String type, Element rootElement, Namespace namespace) {
        log.debug("Converting " + type + " repositories");
        Element repos = rootElement.getChild(type + "Repositories", namespace);
        if (repos != null) {
            List<Element> repositories = repos.getChildren(type + "Repository", namespace);
            if (CollectionUtils.isNotEmpty(repositories)) {
                for (Element repository : repositories) {
                    if (isDockerRepo(repository, namespace)) {
                        appendEnablePushingSchema1(repository, namespace);
                    }
                }
            }
        }
    }

    static boolean isDockerRepo(Element repository, Namespace namespace) {
        return "docker".equals(repository.getChild("type", namespace).getText());
    }

    private void appendEnablePushingSchema1(Element repository, Namespace namespace) {
        Element pushingSchema1Element = new Element(BLOCK_SCHEMA1_ELEMENT, namespace);
        pushingSchema1Element.setText("false");

        log.debug("Appending " + BLOCK_SCHEMA1_ELEMENT + "=false to '{}'", repository.getChild("key", namespace).getText());
        appendElementAfterAll(repository, pushingSchema1Element, namespace,
                "type", "description", "notes", "includesPattern", "excludesPattern",
                "repoLayoutRef", "dockerApiVersion", "forceNugetAuthentication", "blackedOut", "handleReleases",
                "handleSnapshots", "maxUniqueSnapshots", "maxUniqueTags");
    }

    private void appendElementAfterAll(Element appendTo, Element toAppend, Namespace namespace,
            String... elementNamesToAppendAfter) {
        int indexToAppendAfter = getIndexOfLastFoundElement(appendTo, namespace, elementNamesToAppendAfter);
        appendTo.addContent(indexToAppendAfter + 1, toAppend);
    }

    private int getIndexOfLastFoundElement(Element repositoryElement, Namespace namespace, String... elementNames) {
        int index = -1;
        for (String elementName : elementNames) {
            Element child = repositoryElement.getChild(elementName, namespace);
            if (child != null) {
                index = Math.max(index, repositoryElement.indexOf(child));
            }
        }
        return index;
    }
}