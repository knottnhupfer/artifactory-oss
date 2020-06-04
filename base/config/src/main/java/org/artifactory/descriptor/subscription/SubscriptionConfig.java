package org.artifactory.descriptor.subscription;

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.DiffAtomic;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Set;

/**
 * @author dudim
 */
@XmlRootElement
@XmlType(name = "Subscription", propOrder = {"emails"}, namespace = Descriptor.NS)
@XmlAccessorType(XmlAccessType.FIELD)
@GenerateDiffFunction
public class SubscriptionConfig implements Serializable {

    @XmlElementWrapper(name = "emails")
    @XmlElement(name = "email", type = String.class)
    @DiffAtomic
    private Set<String> emails;

    public Set<String> getEmails() {
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }
}