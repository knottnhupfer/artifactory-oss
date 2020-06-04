package org.artifactory.storage.replication.errors;

/**
 * The replication event type code for persistency
 *
 * @author Shay Bagants
 */
public enum InternalReplicationEventType {
    DEPLOY(1), // deploy file
    MKDIR(2),  // create directory
    DELETE(3), // delete file/directory
    PROPERTY_CHANGE(4); // property changed (created/updated/deleted) on file or folder

    /**
     * The replication event type code for persistency
     */
    private final int code;

    InternalReplicationEventType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static InternalReplicationEventType fromCode(int code) {
        switch (code) {
            case 1:
                return DEPLOY;
            case 2:
                return MKDIR;
            case 3:
                return DELETE;
            case 4:
                return PROPERTY_CHANGE;
            default:
                throw new IllegalArgumentException("Unknown replication event type code: " + code);
        }
    }
}
