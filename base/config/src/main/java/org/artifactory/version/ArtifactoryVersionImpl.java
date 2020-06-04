package org.artifactory.version;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author gidis
 */
public class ArtifactoryVersionImpl implements ArtifactoryVersion {

    static final Pattern VERSION_PATTERN = Pattern
            .compile("(?<major>\\d{1,2})\\.(?<minor>\\d{1,2})\\.(?<patch>\\d{1,2})(-(?<rev>[mp]\\d{3}))?");
    static final int RELEASE_REVISION = 900;

    private String version;
    private long revision;
    private ArtifactoryVersionProvider provider;


    ArtifactoryVersionImpl(String version, long revision, ArtifactoryVersionProvider provider) {
        this.version = version;
        this.revision = revision;
        this.provider = provider;
    }

    @Override
    public ArtifactoryVersionProvider getProvider() {
        return provider;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public long getRevision() {
        return revision;
    }

    @Override
    public boolean isCurrent() {
        return ArtifactoryVersion.getCurrent().equals(this);
    }

    @Override
    public boolean before(ArtifactoryVersion otherVersion) {
        return otherVersion.getRevision() > this.revision;
    }

    @Override
    public boolean after(ArtifactoryVersion otherVersion) {
        return otherVersion.getRevision() < this.revision;
    }

    @Override
    public boolean beforeOrEqual(ArtifactoryVersion otherVersion) {
        return otherVersion.getRevision() >= this.revision;
    }

    @Override
    public boolean afterOrEqual(ArtifactoryVersion otherVersion) {
        return otherVersion.getRevision() <= this.revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArtifactoryVersionImpl)) {
            return false;
        }
        ArtifactoryVersionImpl that = (ArtifactoryVersionImpl) o;
        return revision == that.revision &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, revision);
    }

}