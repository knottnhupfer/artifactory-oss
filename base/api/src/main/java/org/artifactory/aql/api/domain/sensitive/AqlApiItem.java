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
import org.artifactory.aql.model.AqlLogicalFieldEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.result.rows.AqlItem;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiItem extends AqlBase<AqlApiItem, AqlItem> {
    public AqlApiItem(boolean useDefaultResults) {
        super(AqlItem.class, useDefaultResults);
    }

    public static AqlApiItem create() {
        return new AqlApiItem(true);
    }

    public static AqlApiItem createWithEmptyResults() {
        return new AqlApiItem(false);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> itemId() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemId, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> repo() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemRepo, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> path() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemPath, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> type() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemType, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> size() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemSize, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> created() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemCreated, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> createdBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemCreatedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> modified() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemModified, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> modifiedBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemModifiedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> sha1Actual() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemActualSha1, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> sha1Orginal() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemOriginalSha1, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> md5Actual() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemActualMd5, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> md5Orginal() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemOriginalMd5, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> sha2() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemSha2, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> repoPathChecksum() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.repoPathChecksum, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiArchiveDynamicFieldsDomains<AqlApiItem> archive() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.archives);
        return new AqlApiDynamicFieldsDomains.AqlApiArchiveDynamicFieldsDomains<>(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemPropertyDynamicFieldsDomains<AqlApiItem> property() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.properties);
        return new AqlApiDynamicFieldsDomains.AqlApiItemPropertyDynamicFieldsDomains<>(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiStatisticDynamicFieldsDomains<AqlApiItem> statistic() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiStatisticDynamicFieldsDomains<>(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiArtifactDynamicFieldsDomains<AqlApiItem> artifact() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.artifacts);
        return new AqlApiDynamicFieldsDomains.AqlApiArtifactDynamicFieldsDomains<>(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiDependencyDynamicFieldsDomains<AqlApiItem> dependency() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.dependencies);
        return new AqlApiDynamicFieldsDomains.AqlApiDependencyDynamicFieldsDomains<>(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> depth() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.itemDepth, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiField<AqlApiItem> virtualRepos() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiField<>(AqlLogicalFieldEnum.itemVirtualRepos, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiReleaseBundleFileDynamicFieldsDomains<AqlApiItem> releaseArtifact() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.releaseBundleFiles);
        return new AqlApiDynamicFieldsDomains.AqlApiReleaseBundleFileDynamicFieldsDomains<>(subDomains);
    }
}
