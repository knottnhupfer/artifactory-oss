package org.artifactory.repo.http.research;

import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.protocol.HttpContext;
import org.artifactory.common.ConstantValues;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adds visibility into outbound requests by printing a log line. Also prints a log line when the response code does not indicate success
 *
 * @author Uriah Levy
 */
public class HttpRepoResearchRequestResponseInterceptor implements HttpRequestInterceptor, HttpResponseInterceptor {
    private static final Logger log = LoggerFactory.getLogger(HttpRepoResearchRequestResponseInterceptor.class);
    private boolean interceptorEnabled;
    private String repositoryKey;
    private AtomicInteger requestCounter;
    static final String REQUEST_PATH = "artifactory_request_path";

    public HttpRepoResearchRequestResponseInterceptor(String repositoryKey) {
        this.repositoryKey = repositoryKey;
        String researchedRepoKeys = ConstantValues.remoteRepoResearchInterceptorRepoKeys.getString();
        if (researchedRepoKeys != null) {
            this.interceptorEnabled = Arrays.stream(researchedRepoKeys.split(","))
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .anyMatch(listed -> listed.equals(repositoryKey));
            if (interceptorEnabled) {
                this.requestCounter = new AtomicInteger();
            }
        }
    }

    @Override
    public void process(HttpRequest request, HttpContext context) {
        RequestLine requestLine = request.getRequestLine();
        if (interceptorEnabled && requestLine != null) {
            log.info("Remote repository '{}' is sending a remote request to path '{}'", repositoryKey,
                    requestLine.getUri());
            // Dump the stack trace of the current thread every 100 requests
            if (requestCounter.incrementAndGet() % 100 == 0) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                log.info(
                        "HTTP Repo Response Interceptor is printing the thread stack trace for debugging purposes");
                for (StackTraceElement element : stackTrace) {
                    log.info("\tat {}", element);
                }
            }
            context.setAttribute(REQUEST_PATH, requestLine.getUri());
        }
    }

    @Override
    public void process(HttpResponse response, HttpContext context) {
        StatusLine statusLine = response.getStatusLine();
        if (interceptorEnabled && statusLine != null &&
                !HttpUtils.isSuccessfulResponseCode(statusLine.getStatusCode())) {
            String requestPath = (String) context.getAttribute(HttpRepoResearchRequestResponseInterceptor.REQUEST_PATH);
            if (requestPath != null) {
                log.info(
                        "Request to remote repository '{}' with path '{}' received an unsuccessful response code from upstream: {}",
                        repositoryKey, requestPath, statusLine.getStatusCode());
            }
        }
    }
}
