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

package org.artifactory.webapp.servlet;

import org.artifactory.api.TestService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.security.HttpAuthenticationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Detects and prints transaction leak info.
 *
 * @author Yossi Shaul
 */
class TransactionLeakDetector {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryFilter.class);

    private TransactionLeakDetector() {
        // utility class
    }

    /**
     * Tests if there's transaction active. If there is, print info and try to release.
     */
    static void detectAndRelease(ArtifactoryContext context, ServletRequest request) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            try {
                String leakInfo = buildLeakInfo(request);
                log.error("{}", leakInfo);
                reportForTests(context, leakInfo);
            } finally {
                TransactionSynchronizationManager.clear();
            }
        }
    }

    private static void reportForTests(ArtifactoryContext context, String leakInfo) {
        context.beanForType(TestService.class).transactionLeak(context, leakInfo);
    }

    private static String buildLeakInfo(ServletRequest req) {
        StringBuilder sb = new StringBuilder("Artifactory transaction still active in RepoFilter!\n");
        sb.append("Thread Name: '").append(Thread.currentThread().getName()).append("' ");
        sb.append("TX Name: '").append(TransactionSynchronizationManager.getCurrentTransactionName()).append("' ");
        sb.append("Isolation level: ").append(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel());
        sb.append(" TX active: ").append(TransactionSynchronizationManager.isActualTransactionActive());
        sb.append(" read only: ").append(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        sb.append(" sync active: ").append(TransactionSynchronizationManager.isSynchronizationActive());
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            sb.append("\nTX sync: ").append(TransactionSynchronizationManager.getSynchronizations());
        }
        sb.append("\nTX resources: ").append(TransactionSynchronizationManager.getResourceMap());
        sb.append("\nRequest: ").append(requestInfo((HttpServletRequest) req));
        return sb.toString();
    }

    private static String requestInfo(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return request.getMethod() + " (" + new HttpAuthenticationDetails(request).getRemoteAddress() + ") " +
                RequestUtils.getServletPathFromRequest(request) + (queryString != null ? queryString : "");
    }

}
