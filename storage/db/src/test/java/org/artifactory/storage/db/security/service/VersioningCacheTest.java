package org.artifactory.storage.db.security.service;

import org.artifactory.test.TestUtils;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

/**
 * @author Shay Bagants
 */
@Test
public class VersioningCacheTest {

    private VersioningCacheImpl versioningCacheImpl;

    public void testVersionCache() {
        AtomicInteger timesCallableCalled = new AtomicInteger(0);
        Callable<BasicCacheModel> callable = getCacheCallable(timesCallableCalled);
        versioningCacheImpl = new VersioningCacheImpl(20000, callable);
        // call the cache twice, ensure it never reached the callable more than once
        versioningCacheImpl.get();
        BasicCacheModel cache = versioningCacheImpl.get();
        assertEquals(cache.getVersion(), 1);
        // validate callable called only once
        assertEquals(timesCallableCalled.get(), 1);

        versioningCacheImpl.promoteVersion();
        // call the cache twice, ensure it never reached the callable more than once
        versioningCacheImpl.get();
        cache = versioningCacheImpl.get();
        assertEquals(cache.getVersion(), 2);
        // validate callable called only 2 times
        assertEquals(timesCallableCalled.get(), 2);

        // promote the cache 10 more times
        TestUtils.repeat(10, () -> versioningCacheImpl.promoteVersion());
        // call the cache twice, ensure it never reached the callable more than once
        versioningCacheImpl.get();
        cache = versioningCacheImpl.get();
        assertEquals(cache.getVersion(), 12);
        // validate callable called 3 times
        assertEquals(timesCallableCalled.get(), 3);
    }

    private Callable<BasicCacheModel> getCacheCallable(AtomicInteger atomicInteger) {
        return () -> {
            atomicInteger.incrementAndGet();
            return new CacheModel();
        };
    }

    public static class CacheModel implements BasicCacheModel {
        long version = 0;

        @Override
        public long getVersion() {
            return version;
        }

        @Override
        public void setVersion(long ver) {
            version = ver;
        }

        @Override
        public void destroy() {

        }
    }
}