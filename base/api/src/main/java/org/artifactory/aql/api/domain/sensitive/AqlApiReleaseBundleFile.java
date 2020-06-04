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
import org.artifactory.aql.result.rows.AqlReleaseBundleFile;

import java.util.ArrayList;

/**
 * @author Nadavy
 */
public class AqlApiReleaseBundleFile extends AqlBase<AqlApiReleaseBundleFile, AqlReleaseBundleFile> {

    public AqlApiReleaseBundleFile(boolean useDefaultResults) {
        super(AqlReleaseBundleFile.class, useDefaultResults);
    }

    public static AqlApiReleaseBundleFile create() {
        return new AqlApiReleaseBundleFile(true);
    }

    public static AqlApiReleaseBundleFile createWithEmptyResults() {
        return new AqlApiReleaseBundleFile(false);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundleFile> id() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundleFiles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleFileId, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundleFile> nodeId() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundleFiles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleFileNodeId,
                subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundleFile> bundleId() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundleFiles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleFileBundleId,
                subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundleFile> repoPath() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundleFiles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleFileRepoPath,
                subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiReleaseBundleDynamicFieldsDomains<AqlApiReleaseBundleFile> release() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundleFiles, AqlDomainEnum.releaseBundles);
        return new AqlApiDynamicFieldsDomains.AqlApiReleaseBundleDynamicFieldsDomains<>(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains<AqlApiReleaseBundleFile> item() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundleFiles, AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains<>(subDomains);
    }
}
