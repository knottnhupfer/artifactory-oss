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

package org.artifactory.traffic;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.schedule.TaskService;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.spring.ReloadableBean;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.artifactory.traffic.entry.TrafficEntry;
import org.artifactory.traffic.entry.TransferEntry;
import org.artifactory.traffic.mbean.Traffic;
import org.artifactory.traffic.read.TrafficReader;
import org.artifactory.util.CollectionUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Traffic service persists the traffic (download/upload) in Artifactory and can retrieve it by date range.
 *
 * @author Yoav Landman
 */
@Service
@Reloadable(beanClass = InternalTrafficService.class, initAfter = {InternalRepositoryService.class, TaskService.class},
        listenOn = CentralConfigKey.none)
public class TrafficServiceImpl implements InternalTrafficService, ReloadableBean {
    private static final Logger log = LoggerFactory.getLogger(TrafficServiceImpl.class);

    @Autowired
    private ArtifactoryServersCommonService serversService;

    @Override
    public void init() {
        //Register a mbean
        InternalArtifactoryContext context = InternalContextHelper.get();
        Traffic traffic = new Traffic(context.beanForType(InternalTrafficService.class));
        ContextHelper.get().beanForType(MBeanRegistrationService.class).register(traffic);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        //nop
    }

    @Override
    public void destroy() {
        //nop
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        //When conversion is needed, remove all old stats
    }

    @Override
    public void handleTrafficEntry(TrafficEntry entry) {
        if (isActive()) {
            if (entry instanceof TransferEntry) {
                TrafficLogger.logTransferEntry((TransferEntry) entry);
            }
        }
    }

    @Override
    public void validateDateRange(Calendar startDate, Calendar endDate) {
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("The start date cannot be later than the end date");
        }
    }

    /**
     * Returns traffic entries
     *
     * @param from Traffic start time
     * @param to   Traffic end time
     * @return {@link List<TrafficEntry>} taken from the traffic log files or the database
     */
    @Override
    public List<TrafficEntry> getEntryList(Calendar from, Calendar to) {
        return getTrafficReader().getEntries(from, to);
    }

    /**
     * Returns Xray traffic entries
     *
     * @param from Traffic start time
     * @param to   Traffic end time
     * @return {@link List<TrafficEntry>} taken from the traffic log files or the database
     */
    @Override
    public List<TrafficEntry> getXrayEntryList(Calendar from, Calendar to) {
        return getTrafficReader().getXrayEntries(from, to);
    }

    private TrafficReader getTrafficReader() {
        return new TrafficReader(getTrafficLogDir());
    }

    /**
     * Returns transfer usage
     *
     * @param startTime  Traffic start time in long
     * @param endTime    Traffic end time in long
     * @param ipToFilter filter the traffic by list of ip
     * @return TransferUsage taken from the traffic log files or the database
     */
    @Override
    public TransferUsage getTrafficUsageWithFilterCurrentNode(long startTime, long endTime,
            List<String> ipToFilter) {
        Calendar from = Calendar.getInstance();
        from.setTimeInMillis(startTime);
        Calendar to = Calendar.getInstance();
        to.setTimeInMillis(endTime);
        validateDateRange(from, to);
        List<TrafficEntry> allTrafficEntries = getEntryList(from, to);
        List<TrafficEntry> xrayTrafficEntries = getXrayEntryList(from, to);
        TransferUsage transferUsage = new TransferUsage();
        setAllTrafficUsage(allTrafficEntries, ipToFilter, transferUsage);
        setXrayTrafficUsage(xrayTrafficEntries, ipToFilter, transferUsage);
        return transferUsage;
    }

    @Override
    public boolean isActive() {
        return ConstantValues.trafficCollectionActive.getBoolean();
    }

    @Override
    public TransferUsage getTrafficUsageWithFilter(long startTime, long endTime, List<String> ipsToFilter) {
        List<TransferUsage> transferUsageList = Lists.newArrayList();
        TransferUsage currentNodeUsage = getTrafficUsageWithFilterCurrentNode(startTime, endTime, ipsToFilter);
        log.debug("usage from current node - upload: {}, download: {}, xrayUpload: {}, xrayDownload: {}",
                currentNodeUsage.getUpload(), currentNodeUsage.getDownload(), currentNodeUsage.getXrayUpload(),
                currentNodeUsage.getXrayDownload());
        transferUsageList.add(currentNodeUsage);

        HaCommonAddon haAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(HaCommonAddon.class);
        if (haAddon.isHaEnabled() && haAddon.isHaConfigured()) {
            List<ArtifactoryServer> allOtherActiveHaServers = serversService.getOtherActiveMembers();
            List<TransferUsage> responseList = haAddon.propagateTrafficCollector(startTime, endTime, ipsToFilter,
                    allOtherActiveHaServers, TransferUsage.class);
            if (CollectionUtils.isNullOrEmpty(responseList)) {
               return currentNodeUsage;
            }
            transferUsageList.addAll(responseList);
        }
        return aggregateUsage(transferUsageList);
    }

    private TransferUsage aggregateUsage(List<TransferUsage> transferUsageList) {
        TransferUsage finalTransferUsage = new TransferUsage();
        for (TransferUsage transferUsage : transferUsageList) {
            finalTransferUsage.setDownload(finalTransferUsage.getDownload() + transferUsage.getDownload());
            finalTransferUsage.setXrayDownload(finalTransferUsage.getXrayDownload() + transferUsage.getXrayDownload());
            finalTransferUsage.setUpload(finalTransferUsage.getUpload() + transferUsage.getUpload());
            finalTransferUsage.setXrayUpload(finalTransferUsage.getXrayUpload() + transferUsage.getXrayUpload());
            finalTransferUsage.setRedirect(finalTransferUsage.getRedirect() + transferUsage.getRedirect());
            finalTransferUsage.setExcludedDownload(
                    finalTransferUsage.getExcludedDownload() + transferUsage.getExcludedDownload());
            finalTransferUsage.setExcludedXrayDownload(
                    finalTransferUsage.getExcludedXrayDownload() + transferUsage.getExcludedXrayDownload());
            finalTransferUsage.setExcludedUpload(
                    finalTransferUsage.getExcludedUpload() + transferUsage.getExcludedUpload());
            finalTransferUsage.setExcludedXrayUpload(
                    finalTransferUsage.getExcludedXrayUpload() + transferUsage.getExcludedXrayUpload());
            finalTransferUsage.setExcludedRedirect(
                    finalTransferUsage.getExcludedRedirect() + transferUsage.getExcludedRedirect());
        }
        return finalTransferUsage;
    }

    private void setAllTrafficUsage(List<TrafficEntry> allTrafficEntries,
            List<String> ipToFilter, TransferUsage transferUsage) {
        List<TrafficEntry> trafficEntriesUsage = Lists.newArrayList();
        List<TrafficEntry> trafficEntriesExcludedUsage = Lists.newArrayList();
        setEntriesByFilter(trafficEntriesUsage, trafficEntriesExcludedUsage, allTrafficEntries, ipToFilter);

        fillUsage(trafficEntriesExcludedUsage, transferUsage, true);
        fillUsage(trafficEntriesUsage, transferUsage, false);
    }

    private void setXrayTrafficUsage(List<TrafficEntry> xrayTrafficEntries,
            List<String> ipToFilter, TransferUsage transferUsage) {
        List<TrafficEntry> xrayTrafficEntriesUsage = Lists.newArrayList();
        List<TrafficEntry> xrayTrafficEntriesExcludedUsage = Lists.newArrayList();
        setEntriesByFilter(xrayTrafficEntriesUsage, xrayTrafficEntriesExcludedUsage, xrayTrafficEntries, ipToFilter);

        fillXrayUsage(xrayTrafficEntriesExcludedUsage, transferUsage, true);
        fillXrayUsage(xrayTrafficEntriesUsage, transferUsage, false);
    }

    private void setEntriesByFilter(List<TrafficEntry> entries, List<TrafficEntry> excludedEntries,
            List<TrafficEntry> allEntries, List<String> ipToFilter) {
        if (ipToFilter == null || ipToFilter.isEmpty()) {
            entries.addAll(allEntries);
        } else {
            for (TrafficEntry trafficEntry : allEntries) {
                if (isExcludedTraffic(trafficEntry, ipToFilter)) {
                    excludedEntries.add(trafficEntry);
                } else {
                    entries.add(trafficEntry);
                }
            }
        }
    }

    private boolean isExcludedTraffic(TrafficEntry trafficEntry, List<String> ipsToFilter) {
        TransferEntry transferEntry = ((TransferEntry) trafficEntry);
        return ipsToFilter.stream()
                .anyMatch(ipToFilter -> ipToFilter.equals(transferEntry.getUserAddress()));
    }

    private void fillUsage(List<TrafficEntry> trafficEntriesUsage, TransferUsage transferUsage,
            boolean isExcludedUsage) {
        long uploadTransferUsage = 0;
        long downloadTransferUsage = 0;
        long redirectTransferUsage = 0;

        for (TrafficEntry trafficEntry : trafficEntriesUsage) {
            long contentLength = ((TransferEntry) trafficEntry).getContentLength();
            switch (trafficEntry.getAction()) {
                case UPLOAD:
                    uploadTransferUsage += contentLength;
                    break;
                case DOWNLOAD:
                    downloadTransferUsage += contentLength;
                    break;
                case REDIRECT:
                    redirectTransferUsage += contentLength;
                    break;
                default:
                    log.info("Traffic entity type {} is not supported", trafficEntry.getAction());
            }
        }
        if (isExcludedUsage) {
            transferUsage.setExcludedUpload(uploadTransferUsage);
            transferUsage.setExcludedDownload(downloadTransferUsage);
            transferUsage.setExcludedRedirect(redirectTransferUsage);
        } else {
            transferUsage.setUpload(uploadTransferUsage);
            transferUsage.setDownload(downloadTransferUsage);
            transferUsage.setRedirect(redirectTransferUsage);
        }
    }

    private void fillXrayUsage(List<TrafficEntry> trafficEntriesUsage, TransferUsage transferUsage,
            boolean isExcludedUsage) {
        long uploadTransferUsage = 0;
        long downloadTransferUsage = 0;

        for (TrafficEntry trafficEntry : trafficEntriesUsage) {
            long contentLength = ((TransferEntry) trafficEntry).getContentLength();
            switch (trafficEntry.getAction()) {
                case UPLOAD:
                    uploadTransferUsage += contentLength;
                    break;
                case DOWNLOAD:
                    downloadTransferUsage += contentLength;
                    break;
                default:
                    log.info("Traffic entity type {} is not supported", trafficEntry.getAction());
            }
        }
        if (isExcludedUsage) {
            transferUsage.setExcludedXrayUpload(uploadTransferUsage);
            transferUsage.setExcludedXrayDownload(downloadTransferUsage);
        } else {
            transferUsage.setXrayUpload(uploadTransferUsage);
            transferUsage.setXrayDownload(downloadTransferUsage);
        }
    }

    File getTrafficLogDir() {
        if (ConstantValues.trafficLogsDirectory.isSet()) {
            String trafficLogDirPath = ConstantValues.trafficLogsDirectory.getString();
            if (isNotBlank(trafficLogDirPath) && new File(trafficLogDirPath).exists()) {
                return new File(trafficLogDirPath);
            } else {
                log.error("Directory: \"{}\" not exist/valid, using default directory", trafficLogDirPath);
            }
        }
        return getDefaultTrafficLogDir();
    }

    File getDefaultTrafficLogDir() {
        return ContextHelper.get().getArtifactoryHome().getLogDir();
    }
}