package org.artifactory.storage.tx;

import org.artifactory.sapi.fs.VfsItem;

/**
 * @author Uriah Levy
 * A handler interface for low-level artifact related events
 */
public interface SessionEventHandler {

    void itemCreated(VfsItem item);

    void itemUpdated(VfsItem item);

    void itemDeleted(VfsItem item);

    void propertiesModified(VfsItem item);
}