package org.artifactory.api.rest.subscription;

import java.io.Serializable;
import java.util.Set;

/**
 * @author dudim
 */
public class Subscription implements Serializable {

    private Set<String> emails;

    public Subscription() {
        //for serialization
    }

    public Subscription(Set<String> emails) {
        this.emails = emails;
    }

    public Set<String> getEmails() {
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }
}