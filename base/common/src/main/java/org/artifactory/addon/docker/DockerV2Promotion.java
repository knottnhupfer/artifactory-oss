package org.artifactory.addon.docker;

/**
 * @author Hezi Cohen
 */
public class DockerV2Promotion {

    public String getTargetRepo() {
        return targetRepo;
    }

    public void setTargetRepo(String targetRepo) {
        this.targetRepo = targetRepo;
    }

    public String getDockerRepository() {
        return dockerRepository;
    }

    public void setDockerRepository(String dockerRepository) {
        this.dockerRepository = dockerRepository;
    }

    public String getTargetDockerRepository() {
        return targetDockerRepository;
    }

    public void setTargetDockerRepository(String targetDockerRepository) {
        this.targetDockerRepository = targetDockerRepository;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTargetTag() {
        return targetTag;
    }

    public void setTargetTag(String targetTag) {
        this.targetTag = targetTag;
    }

    public boolean isCopy() {
        return copy;
    }

    public void setCopy(boolean copy) {
        this.copy = copy;
    }

    private String targetRepo;
    private String dockerRepository;
    private String targetDockerRepository;
    private String tag;
    private String targetTag;
    private boolean copy;
}
