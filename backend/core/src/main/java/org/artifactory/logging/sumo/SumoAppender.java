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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.util.BlockOnTakeConcurrentQueue;
import org.artifactory.util.HttpClientConfigurator;

import javax.management.ObjectName;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Appender that sends log messages to Sumo Logic.
 *
 * @author Shay Yaakov
 */
public class SumoAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final String LOGGING_MBEAN_TYPE = "Logging";
    private static final String MBEAN_PROP_PREFIX = "SumoLogic/";
    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private static final int UNDEFINED = -1;
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final long DEFAULT_BATCH_QUIET_PERIOD = 100;
    private static final long CACHE_QUEUE_SIZE_MS = 100;

    private BlockOnTakeConcurrentQueue<ILoggingEvent> queue;
    private Layout<ILoggingEvent> layout;
    private SumoCategory category = SumoCategory.CONSOLE;
    private int queueSize = DEFAULT_QUEUE_SIZE;
    private int discardingThreshold = UNDEFINED;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private long batchQuietPeriod = DEFAULT_BATCH_QUIET_PERIOD;
    private boolean enabled = true;
    private String collectorUrl = null;
    private String artifactoryHost = null;
    private String artifactoryNode = null;
    private ProxyDescriptor proxy = null;
    private CloseableHttpClient httpClient = null;
    private boolean mbeanRegistered = false;

    private Worker worker;

    @Override
    public void start() {
        if (!enabled) {
            addInfo("Sumo appender is disabled: " + getName());
            return;
        }

        checkValue(layout, Objects::nonNull, "layout is required");
        checkValue(collectorUrl, Objects::nonNull, "collectorUrl is required");
        checkValue(category, Objects::nonNull, "category is required");
        checkValue(queueSize, size -> size > 0, "queueSize must be positive integer");
        if (discardingThreshold == UNDEFINED) {
            discardingThreshold = queueSize / 5;
        }
        addInfo("Setting discardingThreshold to " + discardingThreshold);
        checkValue(discardingThreshold, val -> val >= 0 && val <= queueSize,
                "discardingThreshold must be a non-negative integer, less than or equals to queueSize");
        checkValue(batchSize, size -> size > 0, "batchSize must be positive integer");
        checkValue(batchQuietPeriod, time -> time >= 0, "batchQuietPeriod must be non-negative long");

        queue = new BlockOnTakeConcurrentQueue<>(queueSize, CACHE_QUEUE_SIZE_MS);
        closeHttpClient();
        worker = new Worker(batchSize, batchQuietPeriod);
        worker.setDaemon(true);
        worker.setName("SumoAppender-Worker-" + worker.getName());
        // make sure this instance is marked as "started" before staring the worker Thread
        super.start();
        worker.start();
    }

    private CloseableHttpClient createClient() {
        RequestConfig.Builder configBuilder = RequestConfig.custom()
                .setMaxRedirects(20)
                .setSocketTimeout(15000)
                .setConnectTimeout(1500);
        BasicHttpClientConnectionManager connectionMgr = new BasicHttpClientConnectionManager();
        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setUserAgent("Artifactory")
                .disableCookieManagement()
                .setConnectionManager(connectionMgr)
                .setKeepAliveStrategy(HttpClientConfigurator.createConnectionKeepAliveStrategy())
                .disableAutomaticRetries();
        if (proxy != null && proxy.getHost() != null) {
            BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            HttpClientConfigurator.configureProxy(proxy, clientBuilder, configBuilder, credsProvider);
            if (credsProvider.getCredentials(AuthScope.ANY) != null) {
                clientBuilder.setDefaultCredentialsProvider(credsProvider);
            }
        }
        clientBuilder.setDefaultRequestConfig(configBuilder.build());
        return clientBuilder.build();
    }

    private void closeHttpClient() {
        IOUtils.closeQuietly(httpClient);
        httpClient = null;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (enabled) {
            if (isQueueBelowDiscardingThreshold() && isDiscardable(event)) {
                addWarn("Logging event is lost - queue reached discarding threshold: " + event);
                return;
            }
            // Init the thread name if it was not already set (see: ch.qos.logback.classic.spi.LoggingEvent#getThreadName),
            // assuming this method is running in the caller's thread. Otherwise, the thread name might be set to the
            // worker's thread name.
            event.getThreadName();
            //Try to enqueue the event
            if (!queue.offer(event)) {
                addWarn("Logging event is lost - queue is full: " + event);
            }
        }
    }

    private boolean isQueueBelowDiscardingThreshold() {
        return (queue.remainingCapacity() < discardingThreshold);
    }

    private boolean isDiscardable(ILoggingEvent event) {
        Level level = event.getLevel();
        return level.toInt() <= Level.DEBUG_INT;
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }
        super.stop();
        stopWorker();
        closeHttpClient();
        unregisterMBean();
    }

    private void stopWorker() {
        worker.interrupt();
        try {
            worker.join(1000);
        } catch (InterruptedException e) {
            addError("Failed to join worker thread", e);
        }
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public void setCategory(String value) {
        SumoCategory category = SumoCategory.findByName(value.trim());
        if (category == null) {
            categoryWarn(value);
        } else {
            this.category = category;
        }
    }

    private void categoryWarn(String val) {
        Status status = new WarnStatus("[" + val + "] should be one of " + Arrays.toString(SumoCategory.values()), this);
        status.add(new WarnStatus("Using previously set sumo category, artifactory/console by default.", this));
        addStatus(status);
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public void setDiscardingThreshold(int discardingThreshold) {
        this.discardingThreshold = discardingThreshold;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setBatchQuietPeriod(long batchQuietPeriod) {
        this.batchQuietPeriod = batchQuietPeriod;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCollectorUrl(String collectorUrl) {
        this.collectorUrl = collectorUrl;
    }

    public void setArtifactoryNode(String artifactoryNode) {
        this.artifactoryNode = artifactoryNode;
    }

    public void setArtifactoryHost(String artifactoryHost) {
        this.artifactoryHost = artifactoryHost;
    }

    public void setProxy(ProxyDescriptor proxy) {
        this.proxy = proxy;
    }

    private void registerMBean() {
        if (!mbeanRegistered) {
            addInfo("Registering MBean for SumoAppender with category '" + category + "'.");
            ObjectName objectName = MBeanRegistrationService.createObjectName(LOGGING_MBEAN_TYPE, MBEAN_PROP_PREFIX + category.getName());
            MBeanRegistrationService.register(new ManagedSumoAppender(this), objectName);
            mbeanRegistered = true;
        }
    }

    private void unregisterMBean() {
        if (mbeanRegistered) {
            ObjectName objectName = MBeanRegistrationService.createObjectName(LOGGING_MBEAN_TYPE, MBEAN_PROP_PREFIX + category.getName());
            MBeanRegistrationService.unregister(objectName);
            mbeanRegistered = false;
        }
    }

    private void initHttpClient() {
        if (httpClient == null) {
            httpClient = createClient();
        }
    }

    private class Worker extends Thread {

        private final int batchSize;
        private final long batchQuietPeriod;
        private final List<String> batchData;

        Worker(int batchSize, long batchQuietPeriod) {
            this.batchSize = batchSize;
            this.batchQuietPeriod = batchQuietPeriod;
            this.batchData = new ArrayList<>(batchSize);
        }

        @Override
        public void run() {
            initHttpClient();
            registerMBean();
            // loop while the parent is started
            while (isStarted()) {
                try {
                    handleLoggingEvent(queue.take(batchQuietPeriod, TimeUnit.MILLISECONDS));
                } catch (InterruptedException ie) {
                    break;
                }
            }

            addInfo("Worker thread will flush remaining events before exiting. ");
            queue.forEach(this::handleLoggingEvent);
        }

        private void handleLoggingEvent(ILoggingEvent event) {
            if (event != null && !Thread.currentThread().getName().equals(event.getThreadName())) {
                batchData.add(layout.doLayout(event));
            }

            if (!batchData.isEmpty() && (event == null || batchData.size() >= batchSize)) {
                sendBatchToSumo();
            }
        }

        private void sendBatchToSumo() {
            String data = Joiner.on("").join(batchData);
            try {
                HttpPost post = new HttpPost(collectorUrl);
                post.setEntity(new StringEntity(data, "UTF-8"));
                post.addHeader("X-Sumo-Category", category.headerValue());
                post.addHeader("X-Sumo-Host", artifactoryHost);
                if (artifactoryNode != null) {
                    post.addHeader("X-Sumo-Name", artifactoryNode);
                }
                try(CloseableHttpResponse response = httpClient.execute(post)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        addWarn("Received HTTP error from Sumo Service: " + statusCode);
                    }
                    //need to consume the body if you want to re-use the connection.
                    EntityUtils.consume(response.getEntity());
                }
                batchData.clear();
            } catch (IOException e) {
                addError("Could not send log to Sumo Logic: " + e.toString());
            }
        }

    }

    public interface ManagedSumoAppenderMBean {
        int getQueueMaxSize();
        int getQueueCurrentSize();
        int getQueueDiscardingThreshold();
        int getBatchMaxSize();
        int getBatchCurrentSize();
        long getBatchQuietPeriod();
        String getCategoryHeader();
    }

    private static class ManagedSumoAppender implements ManagedSumoAppenderMBean {
        private final SumoAppender appender;

        ManagedSumoAppender(SumoAppender appender) {
            this.appender = appender;
        }

        @Override
        public int getQueueMaxSize() {
            return appender.queueSize;
        }

        @Override
        public int getQueueCurrentSize() {
            return Optional.ofNullable(appender.queue).map(q -> q.size()).orElse(0);
        }

        @Override
        public int getQueueDiscardingThreshold() {
            return appender.discardingThreshold;
        }

        @Override
        public int getBatchMaxSize() {
            return appender.batchSize;
        }

        @Override
        public int getBatchCurrentSize() {
            return Optional.ofNullable(appender.worker).map(w -> w.batchData.size()).orElse(0);
        }

        @Override
        public long getBatchQuietPeriod() {
            return appender.batchQuietPeriod;
        }

        @Override
        public String getCategoryHeader() {
            return appender.category.headerValue();
        }
    }

    private static <T> void checkValue(T value, Predicate<T> predicate, String message) {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException(message);
        }
    }
}
