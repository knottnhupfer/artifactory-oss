package org.artifactory.ui.rest.resource.jcr;

/**
 * @author dudim
 */
public class EulaRequired {
    private boolean isRequired;

    public EulaRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public EulaRequired() {
    }

    public boolean isRequired() {
        return this.isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }
}
