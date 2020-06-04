package org.artifactory.storage.binstore.service;

/**
 * @author Uriah Levy
 * Bussiness service that manages different GC strategy executions.
 */
public interface BinariesGarbageCollectorService {

    void startGCByStrategy(GarbageCollectorStrategy strategy);

}
