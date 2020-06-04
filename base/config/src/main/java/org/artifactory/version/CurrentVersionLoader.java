package org.artifactory.version;

import org.artifactory.common.ArtifactoryHome;

import java.io.InputStream;
import java.net.URL;
import java.util.regex.Pattern;

import static org.artifactory.common.ArtifactoryHome.ARTIFACTORY_VERSION_PROPERTIES;
import static org.artifactory.version.ArtifactoryVersionReader.readPropsContent;

/**
 * @author dudim
 */
public class CurrentVersionLoader {
    static Pattern RELEASE_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(-[mp]\\d{3})?");

    private static volatile CurrentVersionLoader instance;
    private static ArtifactoryVersion currentVersion;

    public static CurrentVersionLoader getInstance() {
        if (instance == null) {
            synchronized (CurrentVersionLoader.class) {
                if (instance == null) {
                    instance = new CurrentVersionLoader();
                }
            }
        }
        return instance;
    }

    private CurrentVersionLoader() {
        readCurrentVersionFromFile();
    }

    private static void readCurrentVersionFromFile() {
        try {
            // Get default Artifactory version properties from resource
            URL url = ArtifactoryVersionImpl.class.getResource(ARTIFACTORY_VERSION_PROPERTIES);
            if (url == null) {
                throw new IllegalStateException(
                        "Could not read classpath resource '" + ARTIFACTORY_VERSION_PROPERTIES + "'.");
            }
            // Load the version properties from resource directly to the Properties instance
            InputStream inputStream = ArtifactoryHome.class.getResourceAsStream(ARTIFACTORY_VERSION_PROPERTIES);
            ArtifactoryVersionReader.VersionPropertiesContent versionPropertiesContent = readPropsContent(inputStream,
                    ARTIFACTORY_VERSION_PROPERTIES);
            String version = versionPropertiesContent.versionString;
            String revision = versionPropertiesContent.revisionString;

            if (revision.startsWith("dev")) {
                currentVersion = ArtifactoryVersionProvider.get(version, Integer.MAX_VALUE);
            } else {
                currentVersion = ArtifactoryVersionProvider.get(version, Integer.parseInt(revision));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load Artifactory version from resource file.", e);
        }
    }

    public ArtifactoryVersion getCurrent() {
        return currentVersion;

    }

}
