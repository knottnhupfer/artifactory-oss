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
import org.artifactory.aql.result.rows.AqlReleaseBundle;

import java.util.ArrayList;

/**
 * @author Nadavy
 */
public class AqlApiReleaseBundle extends AqlBase<AqlApiReleaseBundle, AqlReleaseBundle> {

    public AqlApiReleaseBundle(boolean useDefaultResults) {
        super(AqlReleaseBundle.class, useDefaultResults);
    }

    public static AqlApiReleaseBundle create() {
        return new AqlApiReleaseBundle(true);
    }

    public static AqlApiReleaseBundle createWithEmptyResults() {
        return new AqlApiReleaseBundle(false);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundle> id() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleId, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundle> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundle> version() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleVersion, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundle> status() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleStatus, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundle> created() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleCreated, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundle> signature() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleSignature, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundle> bundleType() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleType, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiReleaseBundle> storingRepo() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundles);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleStoringRepo, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiReleaseBundleFileDynamicFieldsDomains<AqlApiReleaseBundle> releaseArtifact() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.releaseBundles, AqlDomainEnum.releaseBundleFiles);
        return new AqlApiDynamicFieldsDomains.AqlApiReleaseBundleFileDynamicFieldsDomains<>(subDomains);
    }
}
