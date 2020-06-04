package org.artifactory.event;

/**
 * @author Uriah Levy
 */
public enum EventOperatorId {
    METADATA_OPERATOR_ID("metadata");
    
    private final String id;

    EventOperatorId(String id) {
        this.id = id;
    }

    public String getId() {
        return id + "-operator";
    }
}
