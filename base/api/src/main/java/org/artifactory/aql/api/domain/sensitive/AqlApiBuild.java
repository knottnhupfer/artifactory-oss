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
import org.artifactory.aql.result.rows.AqlBuild;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiBuild extends AqlBase<AqlApiBuild, AqlBuild> {

    public AqlApiBuild() {
        super(AqlBuild.class, true);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> number() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildNumber, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> url() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildUrl, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> started() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildStarted, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> created() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildCreated, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> createdBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildCreatedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> modified() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildModified, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> modifiedBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildModifiedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiModuleDynamicFieldsDomains<AqlApiBuild> module() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds, AqlDomainEnum.modules);
        return new AqlApiDynamicFieldsDomains.AqlApiModuleDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiBuildPromotionDynamicFieldsDomains<AqlApiBuild> buildPromotions() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds, AqlDomainEnum.buildPromotions);
        return new AqlApiDynamicFieldsDomains.AqlApiBuildPromotionDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiBuildPropertyDynamicFieldsDomains<AqlApiBuild> property() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds, AqlDomainEnum.buildProperties);
        return new AqlApiDynamicFieldsDomains.AqlApiBuildPropertyDynamicFieldsDomains(subDomains);
    }

    public static AqlApiBuild create() {
        return new AqlApiBuild();
    }
}