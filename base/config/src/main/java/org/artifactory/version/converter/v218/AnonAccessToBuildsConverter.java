package org.artifactory.version.converter.v218;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Removes 'anonAccessToBuildInfosDisabled' under 'security' config section.
 *
 * Adding two fields:
 * buildGlobalBasicReadAllowed - setting to TRUE (always true for upgrade),
 * buildGlobalBasicReadForAnonymous - setting to the negative of 'anonAccessToBuildInfosDisabled' value, in case this
 * field already existed.
 *
 * We create a marker file for the Access converter to use.
 * File content is 'anonAccessToBuildInfosDisabled'.
 * In case 'anonAccessToBuildInfosDisabled' didn't exist we create the file with value "false".
 * (Access converter is triggered by this file)
 *
 * @author Yuval Reches
 */
public class AnonAccessToBuildsConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(AnonAccessToBuildsConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting 'anonAccessToBuildInfosDisabled' conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        // Flag will always be added as true
        Element buildGlobalBasicReadAllowed = new Element("buildGlobalBasicReadAllowed", namespace).setText("true");
        Element security = rootElement.getChild("security", namespace);
        if (security != null) {
            convertAndCreateMarkerFile(security, namespace, buildGlobalBasicReadAllowed);
        } else {
            addSecurityAndFlagAndCreateMarkerFile(rootElement, namespace, buildGlobalBasicReadAllowed);
        }
        log.info("Finished 'anonAccessToBuildInfosDisabled' conversion");
    }

    /**
     * Adds "Security" section and "buildGlobalBasicReadAllowed" "buildGlobalBasicReadForAnonymous" fields set to True
     * Creating marker file with False
     */
    private void addSecurityAndFlagAndCreateMarkerFile(Element rootElement, Namespace namespace,
            Element buildGlobalBasicReadAllowed) {
        Element security = new Element("security", namespace);
        security.addContent(new Text("\n                "));
        security.addContent(buildGlobalBasicReadAllowed);
        createNewAnonymousField(security, namespace, true);
        // Finding the suitable location in the main repo element for adding the Security element
        int lastLocation = findLocationToInsertSecurity(rootElement);
        rootElement.addContent(lastLocation + 1, new Text("\n            "));
        rootElement.addContent(lastLocation + 2, security);
        createMarkerFile(false);
        log.debug("Added 'Security' section with 'buildGlobalBasicReadAllowed' set to True");
    }

    /**
     * Adds the "buildGlobalBasicReadAllowed" field set to TRUE in Security section
     *
     * In case "anonAccessToBuildInfosDisabled" field exists:
     * - adds "buildGlobalBasicReadForAnonymous" with value that is the negative of the old field
     * - deletes the old field
     */
    private void convertAndCreateMarkerFile(Element securityElement, Namespace namespace,
            Element buildGlobalBasicReadAllowed) {
        addBasicReadFlag(securityElement, buildGlobalBasicReadAllowed);
        log.debug("Added 'buildGlobalBasicReadAllowed' set to True");

        Element anonAccess = securityElement.getChild("anonAccessToBuildInfosDisabled", namespace);
        if (anonAccess == null) {
            log.debug("Old field 'anonAccessToBuildInfosDisabled' doesn't exist, adding anonymous new field as true");
            createMarkerFile(false);
            createNewAnonymousField(securityElement, namespace, true);
            return;
        }
        boolean disableAnonymousAccessValue = Boolean.parseBoolean(anonAccess.getValue());
        createMarkerFile(disableAnonymousAccessValue);
        createNewAnonymousField(securityElement, namespace, !disableAnonymousAccessValue);
        securityElement.removeChild("anonAccessToBuildInfosDisabled", namespace);
    }

    private void addBasicReadFlag(Element securityElement, Element buildGlobalBasicReadAllowed) {
        securityElement.addContent(new Text("\n        "));
        securityElement.addContent(buildGlobalBasicReadAllowed);
    }

    private void createNewAnonymousField(Element securityElement, Namespace namespace,
            boolean enabledAnonymousAccessValue) {
        // Creating new field and setting value according to negative of old value
        Element buildGlobalBasicReadForAnonymous = new Element("buildGlobalBasicReadForAnonymous", namespace)
                .setText(Boolean.toString(enabledAnonymousAccessValue));
        securityElement.addContent(new Text("\n        "));
        securityElement.addContent(buildGlobalBasicReadForAnonymous);
    }

    private void createMarkerFile(boolean disableAnonymousAccessValue) {
        log.debug("anonAccessToBuildInfosDisabled is set to {}. Creating marker file.", disableAnonymousAccessValue);
        File markerFile = ArtifactoryHome.get().getCreateDefaultBuildPermissionMarkerFile();
        try {
            FileUtils.write(markerFile, Boolean.toString(disableAnonymousAccessValue));
            log.debug("anonAccessToBuildInfosDisabled marker file created.", disableAnonymousAccessValue);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write default build permission marker file", e);
        }
    }

    /**
     * Finding the lowest available element in the rootElement that security section should be after.
     * We send a list of fields in a reversed order. First element is the last element in the rootElement config we look for.
     * (As not all the fields are required, some may not appear.)
     */
    private int findLocationToInsertSecurity(Element rootElement) {
        return findLastLocation(rootElement,
                "bintrayConfig",
                "xrayConfig",
                "mailServer",
                "addons",
                "dateFormat",
                "revision",
                "fileUploadMaxSizeMb",
                "helpLinksEnabled",
                "offlineMode",
                "serverName");
    }

}
