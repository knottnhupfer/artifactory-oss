/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.util;

import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.artifactory.test.TestUtils.assertCausedBy;
import static org.artifactory.test.TestUtils.assertTiming;
import static org.testng.Assert.*;

/**
 * @author Yinon Avraham
 */
public class BlockOnTakeConcurrentQueueTest {

    @Test(expectedExceptions = {IllegalArgumentException.class}, dataProvider = "provideConstructQueueWithInvalidParams")
    public void testConstructQueueWithInvalidParams(int capacity, long cacheSizeForMs) {
        System.out.println("testConstructQueueWithInvalidParams: capacity=" + capacity + " , cacheSizeForMs=" + cacheSizeForMs);
        new BlockOnTakeConcurrentQueue<>(capacity, cacheSizeForMs);
    }

    @DataProvider
    private Object[][] provideConstructQueueWithInvalidParams() {
        return new Object[][] {
                { 0, 1 },
                { -1, 1 },
                { 1, -1 }
        };
    }

    @Test
    public void testEmptyQueue() {
        BlockOnTakeConcurrentQueue<Long> queue = new BlockOnTakeConcurrentQueue<>(5, 0);
        assertEquals(queue.capacity(), 5);
        assertTrue(queue.isEmpty());
        assertEquals(queue.size(), 0);
        assertNull(queue.poll());
        assertNull(queue.peek());
        assertFalse(queue.remove(1L));
        assertFalse(queue.removeAll(Arrays.asList(1L, 2L)));
        assertEquals(queue.toArray(), new Object[0]);
        assertEquals(queue.toArray(new Long[0]), new Long[0]);
        assertFalse(queue.contains(1L));
        assertFalse(queue.containsAll(Arrays.asList(1L, 2L)));
        assertFalse(queue.retainAll(Arrays.asList(1L, 2L)));
        assertEquals(queue.remainingCapacity(), 5);
    }

    @Test(expectedExceptions = {IllegalStateException.class}, expectedExceptionsMessageRegExp = "Queue is already full")
    public void testAddExceedsCapacity() {
        BlockOnTakeConcurrentQueue<Long> queue = new BlockOnTakeConcurrentQueue<>(3, 0);
        assertTrue(queue.add(1L));
        assertFalse(queue.isEmpty());
        assertEquals(queue.size(), 1);
        assertTrue(queue.add(2L));
        assertEquals(queue.size(), 2);
        assertTrue(queue.add(3L));
        assertEquals(queue.size(), 3);
        queue.add(4L);
    }

    @Test
    public void testOfferExceedsCapacity() {
        BlockOnTakeConcurrentQueue<Long> queue = new BlockOnTakeConcurrentQueue<>(3, 0);
        assertTrue(queue.offer(1L));
        assertFalse(queue.isEmpty());
        assertEquals(queue.size(), 1);
        assertTrue(queue.offer(2L));
        assertEquals(queue.size(), 2);
        assertTrue(queue.offer(3L));
        assertEquals(queue.size(), 3);
        assertFalse(queue.offer(4L));
        assertEquals(queue.size(), 3);
    }

    @Test(dataProvider = "provideSimpleUsageCacheSizeFrMs")
    public void testSimpleUsage(int cacheSizeForMs) {
        System.out.println("testSimpleUsage: cacheSizeForMs=" + cacheSizeForMs);
        BlockOnTakeConcurrentQueue<Long> queue = new BlockOnTakeConcurrentQueue<>(5, cacheSizeForMs);
        queue.add(1L);
        assertEquals(queue.remainingCapacity(), 4);
        queue.addAll(Arrays.asList(2L, 3L));
        queue.offer(4L);
        assertEquals(queue.remainingCapacity(), 1);
        assertEquals(queue.size(), 4);
        assertEquals(queue.peek(), Long.valueOf(1));
        assertEquals(queue.peek(), Long.valueOf(1)); // test again to see it was not changed
        assertEquals(queue.element(), Long.valueOf(1));
        assertEquals(queue.element(), Long.valueOf(1)); // test again to see it was not changed
        assertEquals(queue.size(), 4);
        assertEquals(queue.poll(), Long.valueOf(1));
        assertEquals(queue.size(), 3);
        assertEquals(queue.remove(), Long.valueOf(2));
        assertEquals(queue.size(), 2);
        assertEquals(queue.remainingCapacity(), 3);
        assertTrue(queue.removeAll(Arrays.asList(3L, 4L)));
        assertTrue(queue.isEmpty());
    }

    @DataProvider
    private Object[][] provideSimpleUsageCacheSizeFrMs() {
        return new Object[][] {
                { 0 },
                { 1 },
                { 2 },
                { 10 },
                { 100 },
                { 1000 }
        };
    }

    @Test
    public void testIteratorIsUnmodifiable() {
        BlockOnTakeConcurrentQueue<Long> queue = new BlockOnTakeConcurrentQueue<>(5, 0);
        queue.offer(1L);
        queue.offer(2L);
        queue.offer(3L);
        queue.offer(4L);
        Iterator<Long> iterator = queue.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(iterator.next(), Long.valueOf(1));
        assertTrue(iterator.hasNext());
        assertEquals(iterator.next(), Long.valueOf(2));
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        //assertActionThrows(iterator::remove, UnsupportedOperationException.class);
        assertTrue(iterator.hasNext());
        assertEquals(iterator.next(), Long.valueOf(3));
        assertTrue(iterator.hasNext());
        assertEquals(iterator.next(), Long.valueOf(4));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testTakeWithTimeout() throws InterruptedException {
        BlockOnTakeConcurrentQueue<Long> queue = new BlockOnTakeConcurrentQueue<>(5, 0);
        queue.offer(1L);
        queue.offer(2L);
        queue.offer(3L);
        assertTiming(0, 10, () ->
                assertEquals(queue.take(100, TimeUnit.MILLISECONDS), Long.valueOf(1)));
        assertTiming(0, 10, () ->
                assertEquals(queue.take(100, TimeUnit.MILLISECONDS), Long.valueOf(2)));
        assertTiming(0, 10, () ->
                assertEquals(queue.take(100, TimeUnit.MILLISECONDS), Long.valueOf(3)));
        assertTiming(90, 110, () ->
                assertEquals(queue.take(100, TimeUnit.MILLISECONDS), null));
    }

    @Test
    public void testTake() throws InterruptedException {
        Thread thread = null;
        try {
            final BlockOnTakeConcurrentQueue<Long> queue = new BlockOnTakeConcurrentQueue<>(5, 0);
            queue.offer(1L);
            queue.offer(2L);
            queue.offer(3L);
            Long[] lastTake = new Long[] { null };
            List<Long> result = Lists.newArrayList();

            thread = async(() -> {
                for (long i = 1; i < 10; i++) { // 10 is just a number above 4, to keep the loop running...
                    lastTake[0] = queue.take();
                    result.add(lastTake[0]);
                    assertEquals(lastTake[0], Long.valueOf(i));
                }
            });
            final Thread t = thread;
            final Throwable[] te = new Exception[] { null };
            t.setUncaughtExceptionHandler((t1, e) -> te[0] = e);

            //take should block since there is no 4th element
            assertTiming(900, 1100, () -> t.join(1000));
            assertEquals(result, Arrays.asList(1L, 2L, 3L));
            assertEquals(lastTake[0], Long.valueOf(3));

            //add another element to the queue, it should be taken shortly
            queue.offer(4L);
            assertTiming(90, 110, () -> t.join(100));
            assertEquals(result, Arrays.asList(1L, 2L, 3L, 4L));
            assertEquals(lastTake[0], Long.valueOf(4));
            assertTrue(t.isAlive());

            //interrupt the async consumer, it should be terminated
            t.interrupt();
            t.join(10);
            assertFalse(t.isAlive());
            assertEquals(result, Arrays.asList(1L, 2L, 3L, 4L));
            assertEquals(lastTake[0], Long.valueOf(4));
            assertCausedBy(te[0], InterruptedException.class);
        } finally {
            assert thread != null;
            thread.interrupt();
        }
    }

    private Thread async(ThrowingRunnable action) {
        Thread t = new Thread(() -> {
            run(action);
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void run(ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable e) {
            throw asRuntimeException(e);
        }
    }

    private RuntimeException asRuntimeException(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException)e;
        }
        return new RuntimeException(e);
    }
}