package org.artifactory.descriptor.eula;

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Shay Bagants
 */
@XmlType(name = "EulaType", propOrder = {"accepted", "acceptDate"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class EulaDescriptor implements Descriptor {

    @XmlElement(defaultValue = "false")
    private boolean accepted;

    private String acceptDate;

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(String acceptDate) {
        this.acceptDate = acceptDate;
    }
}
