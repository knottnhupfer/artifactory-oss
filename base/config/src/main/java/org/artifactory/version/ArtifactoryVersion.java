package org.artifactory.version;

import org.apache.commons.lang.StringUtils;

/**
 * @author dudim
 */
public interface ArtifactoryVersion {

    String getVersion();

    long getRevision();

    boolean isCurrent();

    boolean before(ArtifactoryVersion otherVersion);

    boolean after(ArtifactoryVersion otherVersion);

    boolean beforeOrEqual(ArtifactoryVersion otherVersion);

    boolean afterOrEqual(ArtifactoryVersion otherVersion);

    static ArtifactoryVersion[] values() {
        ArtifactoryVersionProvider[] values = ArtifactoryVersionProvider.values();
        ArtifactoryVersion[] artifactoryVersions = new ArtifactoryVersion[values.length];
        for (int i = 0; i < values.length; i++) {
            artifactoryVersions[i]=values[i].get();
        }
        return artifactoryVersions;
    }

    static ArtifactoryVersion getCurrent() {
        return CurrentVersionLoader.getInstance().getCurrent();
    }

    static boolean isCurrentVersion(String versionString, String revisionString) {
        if (StringUtils.isBlank(versionString)) {
            throw new IllegalArgumentException("Version value is empty!");
        }
        // Dev version
        if (getCurrent().getVersion().equals(convertToMavenVersion(versionString))
                || versionString.endsWith("-SNAPSHOT")
                || versionString.contains(".x-DOWN-")
                || versionString.contains(".x-DOWN.")
                || versionString.startsWith("${") // TODO: [by fsi] why we keep the old issue
                ) {
            return true;
        }
        // No revision, from version it is not latest => Probably an error here?
        if (revisionString == null) {
            return false;
        }
        // Dev revision
        if (revisionString.startsWith("dev") || revisionString.startsWith("${")) {
            return true;
        }
        // Stork dev branch version
        return (versionString.contains(".x.") || versionString.contains(".x_")) &&
                StringUtils.isNumeric(revisionString);

    }

    static ArtifactoryVersion valueOf(String versionOverride) {
        return ArtifactoryVersionProvider.get(versionOverride);
    }

    static String convertToMavenVersion(String version) {
        if (version.contains("_m") || version.contains("_p")) {
            version = version.replace("_m", "-m");
            version = version.replace("_p", "-p");
        }
        return version;
    }

    ArtifactoryVersionProvider getProvider();
}