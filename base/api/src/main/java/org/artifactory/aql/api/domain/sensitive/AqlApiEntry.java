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
import org.artifactory.aql.result.rows.AqlArchiveEntryItem;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiEntry  extends AqlBase<AqlApiEntry, AqlArchiveEntryItem> {

    public AqlApiEntry() {
        super(AqlArchiveEntryItem.class, true);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiEntry> path() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.entries);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.archiveEntryPath, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiEntry> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.entries);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.archiveEntryName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiArchiveDynamicFieldsDomains<AqlApiEntry> archive() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.entries, AqlDomainEnum.archives);
        return new AqlApiDynamicFieldsDomains.AqlApiArchiveDynamicFieldsDomains(subDomains);
    }



    public static AqlApiEntry create() {
        return new AqlApiEntry();
    }
}

