package org.artifactory.event.work;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Probes external services for status.
 *
 * @author Uriah Levy
 */
public interface StatusProbe {
    /**
     * Start probing (sync/async) using the provided probe function. Invoke the success function on success.
     *
     * @param probeFunction - the probing function
     * @param onSuccess     - invoked on success
     */
    void startProbing(ExecutorService executor, Supplier<Boolean> probeFunction, Runnable onSuccess);

    /**
     * Checks whether this status probe is currently running
     *
     * @return true if the probe is running
     */
    boolean isProbing();
}
