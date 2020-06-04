package org.artifactory.storage.binstore.service;

import org.artifactory.common.ConstantValues;
import org.artifactory.storage.GCCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dudim
 */
public class GCProviderWrapper implements GCProvider {

    private static final Logger log = LoggerFactory.getLogger(GCProviderWrapper.class);
    private AtomicInteger errorCount = new AtomicInteger(0);
    private boolean isEnabled = true;

    private GCProvider gcProvider;

    public GCProviderWrapper(GCProvider gcProvider) {
        this.gcProvider = gcProvider;
    }

    @Override
    public List<GCCandidate> getBatch() {
        if (isEnabled) {
            return gcProvider.getBatch();
        }
        return new ArrayList<>();
    }

    public GCFunction getAction() {
        return (gcCandidate, result) -> {
            try {
                return gcProvider.getAction().accept(gcCandidate, result);
            } catch (Exception e) {
                log.debug("Error occurred while performing cleanup action. {}", e.getMessage());
                log.trace("Error occurred while performing cleanup action. ", e);
                if (errorCount.incrementAndGet() >= ConstantValues.gcFailCountThreshold.getInt()) {
                    log.error("Provider reached maximum error count: {}, disabling provider", errorCount);
                    isEnabled = false;
                }
            }
            return false;
        };
    }

    @Override
    public boolean shouldReportAfterBatch() {
        return gcProvider.shouldReportAfterBatch();
    }

    @Override
    public String getName() {
        return gcProvider.getName();
    }
}