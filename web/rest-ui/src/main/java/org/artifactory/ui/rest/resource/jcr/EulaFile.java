package org.artifactory.ui.rest.resource.jcr;

/**
 * @author dudim
 */
public class EulaFile {
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public EulaFile(String content) {
        this.content = content;
    }

    public EulaFile() {
        
    }
}
