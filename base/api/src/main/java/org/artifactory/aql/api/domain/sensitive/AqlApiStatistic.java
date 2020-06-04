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

package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.result.rows.AqlStatistics;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
@SuppressWarnings("unchecked")
public class AqlApiStatistic extends AqlBase<AqlApiStatistic, AqlStatistics> {

    private AqlApiStatistic() {
        super(AqlStatistics.class, true);
    }

    public static AqlApiStatistic create() {
        return new AqlApiStatistic();
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> downloads() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.statDownloads, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> downloadBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.statDownloadedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> downloaded() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.statDownloaded, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> remoteDownloads() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.statRemoteDownloads, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> remoteDownloadBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.statRemoteDownloadedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> remoteDownloaded() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.statRemoteDownloaded, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> remoteOrigin() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.statRemoteOrigin, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> remotePath() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.statRemotePath, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> statId() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.statId, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains<AqlApiStatistic> item() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics, AqlDomainEnum.items);
        subDomains.add(AqlDomainEnum.modules);
        return new AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains(subDomains);
    }

}
