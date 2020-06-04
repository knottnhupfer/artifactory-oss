package org.artifactory.storage.db.security.service;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.artifactory.test.TestUtils.repeat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Shay Bagants
 */
@Test
public class AsyncVersioningCacheImplTest {

    private VersioningCacheImpl versioningCacheImpl;
    private final int NUM_OF_THREADS = 10;

    @Test(invocationCount = 5)
    public void testVersionCacheAsync() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREADS);
        AtomicInteger timesCacheLoaderCalled = new AtomicInteger(0);
        // the cache loader callable is waiting until we mark that it should return
        AtomicBoolean cacheLoaderShouldStopWaiting = new AtomicBoolean(false);

        Callable<BasicCacheModel> callable = getAsyncCacheCallable(timesCacheLoaderCalled,
                cacheLoaderShouldStopWaiting);
        versioningCacheImpl = new AsyncVersioningCacheImpl(20000, callable, 1000 * 60, 0, "name");
        // if no cache exists yet, update cache should be sync
        BasicCacheModel cacheModel = versioningCacheImpl.get();
        assertNotNull(cacheModel);
        assertEquals(cacheModel.getVersion(), 1);

        // promote the cache and make sure we are getting the old version of the cache until it is updated async
        versioningCacheImpl.promoteVersion();
        List<Future<BasicCacheModel>> futures = Lists.newArrayList();
        repeat(NUM_OF_THREADS, () -> futures.add(executor.submit(() -> versioningCacheImpl.get())));
        assertEquals(futures.size(), NUM_OF_THREADS);
        for (Future<BasicCacheModel> future : futures) {
            // expect the versioning cache to return the old cache as we the async cache population is still running
            assertEquals(future.get().getVersion(), 1);
        }

        // mark the cache loader callable that it should complete it's calculation
        cacheLoaderShouldStopWaiting.set(true);
        // wait until the cache loading callable finished it's calculation
        waitInCondition(() -> timesCacheLoaderCalled.get() == 2);

        // expect the cache to be v2
        List<Future<BasicCacheModel>> futureList = Lists.newArrayList();
        repeat(NUM_OF_THREADS, () -> futureList.add(executor.submit(() -> versioningCacheImpl.get())));
        assertEquals(futureList.size(), NUM_OF_THREADS);
        for (Future<BasicCacheModel> future : futureList) {
            assertEquals(future.get().getVersion(), 2);
        }
        executor.shutdown();
        executor.awaitTermination(1000, TimeUnit.SECONDS);
    }

    /**
     * Test the following scenario:
     * 1. Call the cache by 10 threads, expect the cache to be loaded once and returned
     * 2. Promote the cache once and call it by 10 threads. Expect the background thread to call the cache, catch exception and re-call the cache again
     * 3. Promote the cache once again and call it by 10 threads while the cache loader(callable) takes long time to complete.
     * Assert that we return the old cache in the meanwhile and threads are not waiting on lock
     * 4. Make sure the cache loading completed, call the cache by 10 threads, expect to get the correct version of the cache
     */
    @Test(invocationCount = 5)
    public void testVersionCacheAsyncWithException() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREADS);
        AtomicInteger timesCacheLoaderCalled = new AtomicInteger(0);
        // the cache loader callable is waiting until we mark that it should return
        AtomicBoolean cacheLoaderShouldStopWaiting = new AtomicBoolean(false);

        // callable will be called 3 times, first time throw exception, second time as retry by thread, third time after promoting the cache
        Callable<BasicCacheModel> callable = getAsyncCacheCallableThatThrowException(timesCacheLoaderCalled,
                cacheLoaderShouldStopWaiting);
        versioningCacheImpl = new AsyncVersioningCacheImpl(20000, callable, 1, 1, "name");

        // first call, will be sync
        List<Future<BasicCacheModel>> futures = Lists.newArrayList();
        repeat(NUM_OF_THREADS, () -> futures.add(executor.submit(() -> versioningCacheImpl.get())));
        for (Future<BasicCacheModel> future : futures) {
            // expect the versioning cache to return the old cache as we the async cache population is still running
            assertEquals(future.get().getVersion(), 1);
            assertEquals(timesCacheLoaderCalled.get(), 1);
        }

        // promote the cache and wait for the thread to call the callable which will throw an exception, but will retry calling the cache while active
        versioningCacheImpl.promoteVersion();
        waitInCondition(() -> timesCacheLoaderCalled.get() == 3);
        List<Future<BasicCacheModel>> secondFuturesBatch = Lists.newArrayList();
        repeat(NUM_OF_THREADS, () -> secondFuturesBatch.add(executor.submit(() -> versioningCacheImpl.get())));
        for (Future<BasicCacheModel> future : secondFuturesBatch) {
            // expect the versioning cache to return the old cache as we the async cache population is still running
            assertEquals(future.get().getVersion(), 2);
            assertEquals(timesCacheLoaderCalled.get(), 3);
        }

        // promote the version. Since the cache callable stuck, expect serving requests from the old cache
        versioningCacheImpl.promoteVersion();
        List<Future<BasicCacheModel>> thirdFuturesBatch = Lists.newArrayList();
        repeat(NUM_OF_THREADS, () -> thirdFuturesBatch.add(executor.submit(() -> versioningCacheImpl.get())));
        for (Future<BasicCacheModel> future : thirdFuturesBatch) {
            // cache can be either 2 or 3, depends if the background thread updated the cache already
            assertEquals(future.get().getVersion(), 2);
            assertEquals(timesCacheLoaderCalled.get(), 3);
        }

        // release the cache loader
        cacheLoaderShouldStopWaiting.set(true);
        waitInCondition(() -> timesCacheLoaderCalled.get() == 4);
        List<Future<BasicCacheModel>> forthFuturesBatch = Lists.newArrayList();
        repeat(NUM_OF_THREADS, () -> forthFuturesBatch.add(executor.submit(() -> versioningCacheImpl.get())));
        for (Future<BasicCacheModel> future : forthFuturesBatch) {
            // cache can be either 2 or 3, depends if the background thread updated the cache already
            assertEquals(future.get().getVersion(), 3);
            assertEquals(timesCacheLoaderCalled.get(), 4);
        }

        versioningCacheImpl.destroy();
        executor.shutdown();
        executor.awaitTermination(1000, TimeUnit.SECONDS);
    }

    private Callable<BasicCacheModel> getAsyncCacheCallableThatThrowException(AtomicInteger atomicInteger,
            AtomicBoolean shouldStopWaiting) {
        return () -> {
            // for the first time, the call is sync, therefore, we should not delay the response
            if (atomicInteger.get() == 3) {
                waitInCondition(shouldStopWaiting::get);
            }
            atomicInteger.incrementAndGet();
            if (atomicInteger.get() == 2) {
                throw new RuntimeException("Throwing runtime exception at the first time");
            }
            return new VersioningCacheTest.CacheModel();
        };
    }

    private Callable<BasicCacheModel> getAsyncCacheCallable(AtomicInteger atomicInteger,
            AtomicBoolean shouldStopWaiting) {
        return () -> {
            // for the first time, the call is sync, therefore, we should not delay the response
            if (atomicInteger.get() > 0) {
                waitInCondition(shouldStopWaiting::get);
            }
            atomicInteger.incrementAndGet();
            return new VersioningCacheTest.CacheModel();
        };
    }

    private void waitInCondition(BooleanSupplier booleanSupplier) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (!booleanSupplier.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > 5000) {
                Assert.fail("Condition didn't match within 3 seconds");
            }
            Thread.sleep(10);
        }
    }
}
