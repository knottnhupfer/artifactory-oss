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

package org.artifactory.api.bintray.distribution.reporting;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayProductModel;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayRepoModel;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.StatusEntryLevel;
import org.artifactory.common.StatusHolder;
import org.artifactory.exception.CancelException;
import org.jfrog.common.BiOptional;
import org.jfrog.common.MultimapCollectors;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * A specialized {@link StatusHolder} that aggregates status entries by paths.
 *
 * @author Dan Feldman
 */
public class DistributionReporter implements StatusHolder {

    private final transient Multimap<String, StatusEntry> statusEntries = HashMultimap.create();
    private final transient Multimap<String, StatusEntry> warningEntries = HashMultimap.create();
    private final transient Multimap<String, StatusEntry> errorEntries = HashMultimap.create();
    private final transient Map<String, BintrayRepoModel> repos = Maps.newHashMap();
    private transient BintrayProductModel product; //Can be only one per distribution (only one per dist repo)


    private static final int CODE_OK = 200;
    public static final String GENERAL_MSG = "--general--"; //All general errors are stored in the map under this key
    private boolean activateLogging;
    private StatusEntry lastStatusEntry;
    private StatusEntry lastErrorStatusEntry;
    private StatusEntry lastWarningStatusEntry;


    public DistributionReporter(boolean activateLogging) {
        this.activateLogging = activateLogging;
    }

    public final void debug(String statusMsg, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(CODE_OK, StatusEntryLevel.DEBUG, statusMsg, null), null, logger);
    }

    public final void debug(String fullRepoPath, String statusMsg, Throwable throwable, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(CODE_OK, StatusEntryLevel.DEBUG, statusMsg, throwable), fullRepoPath, logger);
    }

    //Does not insert message to entries map, only logs it if needed.
    public final void status(String statusMsg, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(CODE_OK, StatusEntryLevel.INFO, statusMsg, null), null, logger);
    }

    public final void status(String fullRepoPath, String statusMsg, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(CODE_OK, StatusEntryLevel.INFO, statusMsg, null), fullRepoPath, logger);
    }

    public void warn(String fullRepoPath, String statusMsg, int statusCode, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(statusCode, StatusEntryLevel.WARNING, statusMsg, null), fullRepoPath, logger);
    }

    public void warn(String statusMsg, int statusCode, Throwable throwable, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(statusCode, StatusEntryLevel.WARNING, statusMsg, throwable), GENERAL_MSG, logger);
    }

    public void warn(String statusMsg, int statusCode, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(statusCode, StatusEntryLevel.WARNING, statusMsg, null), GENERAL_MSG, logger);
    }

    public void warn(String fullRepoPath, String statusMsg, int statusCode, Throwable throwable, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(statusCode, StatusEntryLevel.WARNING, statusMsg, throwable), fullRepoPath, logger);
    }

    public void error(String fullRepoPath, String statusMsg, int statusCode, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(statusCode, StatusEntryLevel.ERROR, statusMsg, null), fullRepoPath, logger);
    }

    public void error(String statusMsg, int statusCode, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(statusCode, StatusEntryLevel.ERROR, statusMsg, null), GENERAL_MSG, logger);
    }

    public void error(String statusMsg, int statusCode, Throwable throwable, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(statusCode, StatusEntryLevel.ERROR, statusMsg, throwable), GENERAL_MSG, logger);
    }

    public void error(String fullRepoPath, String statusMsg, int statusCode, Throwable throwable, @Nonnull Logger logger) {
        addEntryAndLog(new StatusEntry(statusCode, StatusEntryLevel.ERROR, statusMsg, throwable), fullRepoPath, logger);
    }

    public void registerRepo(BintrayRepoModel bintrayRepo) {
        BintrayRepoModel existingRepo = repos.get(bintrayRepo.repoName);
        if (existingRepo == null) {
            repos.put(bintrayRepo.repoName, bintrayRepo);
        } else {
            existingRepo.merge(bintrayRepo);
        }
    }

    public void registerProduct(BintrayProductModel productModel) {
        if (this.product == null) {
            this.product = productModel;
        } else {
            this.product.merge(productModel);
        }
    }

    public void merge(BasicStatusHolder statusHolder) {
        statusHolder.getErrors().forEach(this::addMergedEntry);
        statusHolder.getWarnings().forEach(this::addMergedEntry);
    }

    public boolean hasErrors() {
        return !errorEntries.isEmpty();
    }

    public boolean hasWarnings() {
        return !warningEntries.isEmpty();
    }

    public Collection<BintrayRepoModel> getRegisteredRepos() {
        return repos.values();
    }

    public BintrayProductModel getRegisteredProduct() {
        return product;
    }

    public Multimap<String, StatusEntry> getPathErrors() {
        return errorEntries.entries().stream()
                .filter(entry -> !entry.getKey().equals(GENERAL_MSG))
                .collect(MultimapCollectors.multimapFromEntrySet(HashMultimap::create));
    }

    public Multimap<String, StatusEntry> getGeneralErrors() {
        return errorEntries.entries().stream()
                .filter(entry -> entry.getKey().equals(GENERAL_MSG))
                .collect(MultimapCollectors.multimapFromEntrySet(HashMultimap::create));
    }

    public Multimap<String, StatusEntry> getPathWarnings() {
        return warningEntries.entries().stream()
                .filter(entry -> !entry.getKey().equals(GENERAL_MSG))
                .collect(MultimapCollectors.multimapFromEntrySet(HashMultimap::create));
    }

    public Multimap<String, StatusEntry> getGeneralWarnings() {
        return warningEntries.entries().stream()
                .filter(entry -> entry.getKey().equals(GENERAL_MSG))
                .collect(MultimapCollectors.multimapFromEntrySet(HashMultimap::create));
    }

    @Override
    public boolean isError() {
        return lastErrorStatusEntry != null;
    }

    @Override
    public StatusEntry getLastError() {
        return lastErrorStatusEntry;
    }

    @Override
    public StatusEntry getLastWarning() {
        return lastWarningStatusEntry;
    }

    @Override
    public StatusEntry getLastStatusEntry() {
        return lastStatusEntry;
    }

    @Override
    public String getStatusMsg() {
        StatusEntry latestEntry = getLastMostSevere();
        return latestEntry == null ? null : latestEntry.getMessage();
    }

    @Override
    public Throwable getException() {
        StatusEntry latestEntry = getLastMostSevere();
        return latestEntry == null ? null : latestEntry.getException();
    }

    @Override
    public CancelException getCancelException() {
        //Unused
        return null;
    }

    @Override
    public int getStatusCode() {
        StatusEntry latestEntry = getLastMostSevere();
        return latestEntry == null ? -1 : latestEntry.getStatusCode();
    }

    @Override
    public boolean isVerbose() {
        return false;
    }

    private void addMergedEntry(StatusEntry err) {
        addEntry(new StatusEntry(err.getStatusCode(), err.getLevel(), err.getMessage(), null), GENERAL_MSG);
    }

    private void addEntryAndLog(StatusEntry entry, @Nullable String fullRepoPath, @Nonnull Logger logger) {
        if (activateLogging) {
            doLogEntry(entry, logger);
        }
        addEntry(entry, fullRepoPath);
    }

    private void doLogEntry(@Nonnull StatusEntry entry, @Nonnull Logger logger) {
        if (entry.isError() && logger.isErrorEnabled()) {
            if (entry.getException() != null) {
                logger.error(entry.getMessage(), entry.getException());
            } else {
                logger.error(entry.getMessage());
            }
        } else if (entry.isWarning() && logger.isWarnEnabled()) {
            logger.warn(entry.getMessage());
        } else if (entry.isInfo() && logger.isInfoEnabled()) {
            logger.info(entry.getMessage());
        }
        //Always output stack trace to debug log automatically
        if (entry.getException() != null && logger.isDebugEnabled()) {
            logger.debug(entry.getMessage(), entry.getException());
        } else if (entry.isDebug() && logger.isDebugEnabled()) {
            logger.debug(entry.getMessage());
        }
    }

    private void addEntry(StatusEntry entry, @Nullable String fullRepoPath) {
        if (fullRepoPath != null) {
            if (entry.isError()) {
                insertIfNew(fullRepoPath, entry, errorEntries);
                lastErrorStatusEntry = entry;
            } else if (entry.isWarning()) {
                insertIfNew(fullRepoPath, entry, warningEntries);
                lastWarningStatusEntry = entry;
            } else if (entry.isInfo()) {
                insertIfNew(fullRepoPath, entry, statusEntries);
                lastStatusEntry = entry;
            }
        }
    }

    /**
     * Inserts {@param newEntry} to {@param entryMap} only if there's no identical entry already in it.
     * Entries are mapped by {@param fullRepoPath}
     */
    private void insertIfNew(@Nonnull String fullRepoPath, StatusEntry newEntry, Multimap<String, StatusEntry> entryMap) {
        BiOptional.of(entryMap.get(fullRepoPath).stream()
                .filter(entry -> entry.getLevel().equals(newEntry.getLevel()))
                .filter(entry -> entry.getMessage().equals(newEntry.getMessage()))
                .filter(entry -> entry.getStatusCode() == newEntry.getStatusCode())
                .findAny())
                .ifNotPresent(() -> entryMap.put(fullRepoPath, newEntry));
    }

    @Nullable
    private StatusEntry getLastMostSevere() {
        StatusEntry lastError = getLastError();
        if (lastError != null) {
            return lastError;
        }
        StatusEntry lastWarning = getLastWarning();
        if (lastWarning != null) {
            return lastWarning;
        }
        StatusEntry statusEntry = getLastStatusEntry();
        return statusEntry != null ? statusEntry : null;
    }
}
