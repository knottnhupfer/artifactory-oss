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

package org.artifactory.logging.sumo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.Layout;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.testng.annotations.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Queue;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.artifactory.test.TestUtils.getField;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.testng.Assert.*;

/**
 * <p>Created on 21/07/16
 *
 * @author Yinon Avraham
 */
public class SumoAppenderTest {

    private SumoAppender appender;

    @BeforeClass
    private void setupClass() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @BeforeMethod
    private void setupTest() {
        appender = new SumoAppender();
        Context context = createNiceMock(LoggerContext.class);
        appender.setContext(context);
        replay(context);
    }

    @AfterMethod
    private void tearDownTest() {
        if (appender != null) {
            appender.stop();
        }
    }

    @Test
    public void testDontStartWhenDisabled() {
        appender.setEnabled(false);
        appender.start();
        assertFalse(appender.isStarted());
        assertNull(getWorker(appender));
        assertNull(getHttpClient(appender));
    }

    @Test(dataProvider = "provideStartInputValidations")
    public void testStartInputValidations(Layout<ILoggingEvent> layout, String collectorUrl,
            int queueSize, int discardingThreshold, int batchSize, long batchQuietPeriod, String expectedMessageRegex) {
        System.out.println("Expected message: " + expectedMessageRegex);
        appender.setEnabled(true);
        appender.setLayout(layout);
        appender.setCollectorUrl(collectorUrl);
        appender.setQueueSize(queueSize);
        appender.setDiscardingThreshold(discardingThreshold);
        appender.setBatchSize(batchSize);
        appender.setBatchQuietPeriod(batchQuietPeriod);

        try {
            appender.start();
            fail("Appender should fail to start");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().matches(expectedMessageRegex));
        }
        assertFalse(appender.isStarted());
        assertNull(getWorker(appender));
        assertNull(getHttpClient(appender));
    }

    @DataProvider
    private Object[][] provideStartInputValidations() {
        return new Object[][]{
                {null, "the-collector-url", 10, 2, 4, 50, "layout is required"},
                {createLayout(), null, 10, 2, 4, 50, "collectorUrl is required"},
                {createLayout(), "the-collector-url", 0, 2, 4, 50, "queueSize must be positive integer"},
                {createLayout(), "the-collector-url", -1, 2, 4, 50, "queueSize must be positive integer"},
                {createLayout(), "the-collector-url", 10, -2, 4, 50, "discardingThreshold must be a non-negative integer, less than or equals to queueSize"},
                {createLayout(), "the-collector-url", 10, 11, 4, 50, "discardingThreshold must be a non-negative integer, less than or equals to queueSize"},
                {createLayout(), "the-collector-url", 10, 2, 0, 50, "batchSize must be positive integer"},
                {createLayout(), "the-collector-url", 10, 2, -1, 50, "batchSize must be positive integer"},
                {createLayout(), "the-collector-url", 10, 2, 4, -1, "batchQuietPeriod must be non-negative long"},
        };
    }

    @Test
    public void testAppendAddsToTheQueueWhenEnabled() {
        appender.setEnabled(true);
        appender.setCollectorUrl("the-collector-url");
        appender.setLayout(createLayout());
        appender.start();
        stopWorker(appender);
        Queue<ILoggingEvent> queue = getQueue(appender);
        assertTrue(queue.isEmpty());

        LoggingEvent event = new LoggingEvent();
        appender.append(event);

        ILoggingEvent queuedEvent = queue.poll();
        assertEquals(queuedEvent, event);

        appender.setEnabled(false);
        event = new LoggingEvent();
        appender.append(event);

        assertTrue(queue.isEmpty());
        queuedEvent = queue.poll();
        assertNull(queuedEvent);
    }

    @Test
    public void testAppendControlsQueueSize() {
        appender.setEnabled(true);
        appender.setCollectorUrl("the-collector-url");
        appender.setQueueSize(6);
        appender.setDiscardingThreshold(4);
        appender.setLayout(createLayout());
        appender.start();
        stopWorker(appender);
        Queue<ILoggingEvent> queue = getQueue(appender);
        assertTrue(queue.isEmpty());

        appender.append(createEvent(Level.INFO, ""));
        assertEquals(queue.size(), 1);
        appender.append(createEvent(Level.TRACE, ""));
        assertEquals(queue.size(), 2);
        appender.append(createEvent(Level.DEBUG, ""));
        assertEquals(queue.size(), 3);
        appender.append(createEvent(Level.DEBUG, ""));
        assertEquals(queue.size(), 3);
        appender.append(createEvent(Level.TRACE, ""));
        assertEquals(queue.size(), 3);
        appender.append(createEvent(Level.INFO, ""));
        assertEquals(queue.size(), 4);
        appender.append(createEvent(Level.WARN, ""));
        assertEquals(queue.size(), 5);
        appender.append(createEvent(Level.ERROR, ""));
        assertEquals(queue.size(), 6);
        appender.append(createEvent(Level.INFO, ""));
        assertEquals(queue.size(), 6);
        appender.append(createEvent(Level.ERROR, ""));
        assertEquals(queue.size(), 6);

        queue.poll();
        assertEquals(queue.size(), 5);

        appender.append(createEvent(Level.INFO, ""));
        assertEquals(queue.size(), 6);
    }

    @Test
    public void testSendLogs() throws Exception {
        runWireMockTest(baseUrl -> {
            SumoCategory category = SumoCategory.CONSOLE;
            appender.setEnabled(true);
            appender.setCollectorUrl(baseUrl);
            appender.setArtifactoryHost("the-host");
            appender.setBatchSize(1);
            appender.setLayout(createLayout());
            appender.start();
            stubFor(post(urlEqualTo("/")).willReturn(aResponse().withStatus(200)));
            appender.append(createEvent(Level.TRACE, "message 1"));
            appender.append(createEvent(Level.DEBUG, "message 2"));
            appender.append(createEvent(Level.INFO, "message 3"));
            appender.append(createEvent(Level.WARN, "message 4"));
            appender.append(createEvent(Level.ERROR, "message 5"));
            sleep(100);
            verifyMessageSent(messageBody("[TRACE] - message 1"), category, "the-host", null);
            verifyMessageSent(messageBody("[DEBUG] - message 2"), category, "the-host", null);
            verifyMessageSent(messageBody("[INFO ] - message 3"), category, "the-host", null);
            verifyMessageSent(messageBody("[WARN ] - message 4"), category, "the-host", null);
            verifyMessageSent(messageBody("[ERROR] - message 5"), category, "the-host", null);
        });
    }

    @Test
    public void testSendLogsInBatches() throws Exception {
        runWireMockTest(baseUrl -> {
            SumoCategory category = SumoCategory.CONSOLE;
            appender.setEnabled(true);
            appender.setCategory(category.getName());
            appender.setCollectorUrl(baseUrl);
            appender.setArtifactoryHost("the-host");
            appender.setArtifactoryNode("the-node");
            appender.setBatchSize(3);
            appender.setBatchQuietPeriod(50);
            appender.setLayout(createLayout());
            appender.start();
            stubFor(post(urlEqualTo("/")).willReturn(aResponse().withStatus(200)));
            appender.append(createEvent(Level.TRACE, "message 1"));
            appender.append(createEvent(Level.DEBUG, "message 2"));
            appender.append(createEvent(Level.INFO, "message 3"));
            appender.append(createEvent(Level.WARN, "message 4"));
            appender.append(createEvent(Level.ERROR, "message 5"));
            appender.append(createEvent(Level.TRACE, "message 6"));
            appender.append(createEvent(Level.DEBUG, "message 7"));
            appender.append(createEvent(Level.INFO, "message 8"));
            appender.append(createEvent(Level.WARN, "message 9"));
            appender.append(createEvent(Level.ERROR, "message 10"));
            sleep(200);
            verifyMessageSent(messageBody("[TRACE] - message 1", "[DEBUG] - message 2", "[INFO ] - message 3"), category, "the-host", "the-node");
            verifyMessageSent(messageBody("[WARN ] - message 4", "[ERROR] - message 5", "[TRACE] - message 6"), category, "the-host", "the-node");
            verifyMessageSent(messageBody("[DEBUG] - message 7", "[INFO ] - message 8", "[WARN ] - message 9"), category, "the-host", "the-node");
            verifyMessageSent(messageBody("[ERROR] - message 10"), category, "the-host", "the-node");
        });
    }

    private String messageBody(String... messages) {
        return String.join(System.lineSeparator(), messages) + System.lineSeparator();
    }

    private void verifyMessageSent(String expectedMessage, SumoCategory expectedCategory, String expectedHost, String expectedNode) {
        RequestPatternBuilder expectedRequest = postRequestedFor(urlEqualTo("/"))
                .withHeader("X-Sumo-Category", equalTo("artifactory/" + expectedCategory.getName()))
                .withHeader("X-Sumo-Host", equalTo(expectedHost))
                .withRequestBody(equalTo(expectedMessage));
        if (expectedNode == null) {
            expectedRequest.withoutHeader("X-Sumo-Name");
        } else {
            expectedRequest.withHeader("X-Sumo-Name", equalTo(expectedNode));
        }
        verify(1, expectedRequest);
    }

    public static void runWireMockTest(WireMockTest test) throws Exception {
        int port = findAvailablePort();
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(port));
        try {
            wireMockServer.start();
            configureFor(port);
            test.run("http://localhost:" + port);
        } finally {
            try {
                WireMock.shutdownServer();
                wireMockServer.stop();
            } catch (Exception e) {
                System.err.println("Failed to stop wire mock server: " + e.toString());
            }
        }
    }

    public interface WireMockTest {
        void run(String baseUrl) throws Exception;
    }

    public static int findAvailablePort() {
        try (ServerSocket server = new ServerSocket(0)) {
            return server.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Could not find an available port", e);
        }
    }

    private LoggingEvent createEvent(Level level, String message) {
        LoggingEvent event = new LoggingEvent();
        event.setLevel(level == null ? Level.INFO : level);
        event.setMessage(message);
        return event;
    }

    private void stopWorker(SumoAppender appender) {
        Thread worker = getWorker(appender);
        assertNotNull(worker);
        assertTrue(worker.isAlive());
        assertTrue(worker.isDaemon());
        worker.interrupt();
        waitForThread(worker, 20000);
    }

    private void waitForThread(Thread thread, long timeoutMillis) {
        long start = System.currentTimeMillis();
        long elapsed = 0;
        while (thread.isAlive() && (elapsed = System.currentTimeMillis() - start) < timeoutMillis) {
            sleep(50);
        }
        if (elapsed > timeoutMillis) {
            fail("Thread is still alive after " + elapsed + "ms");
        } else {
            System.out.println("Thread stopped after " + elapsed + "ms");
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    private Layout<ILoggingEvent> createLayout() {
        PatternLayout layout = new PatternLayout();
        layout.setPattern("[%-5level] - %message%n");
        layout.setContext(appender.getContext());
        layout.start();
        return layout;
    }

    private Thread getWorker(SumoAppender appender) {
        return getField(appender, "worker", Thread.class);
    }

    private CloseableHttpClient getHttpClient(SumoAppender appender) {
        return getField(appender, "httpClient", CloseableHttpClient.class);
    }

    private Queue<ILoggingEvent> getQueue(SumoAppender appender) {
        return getField(appender, "queue", Queue.class);
    }

}