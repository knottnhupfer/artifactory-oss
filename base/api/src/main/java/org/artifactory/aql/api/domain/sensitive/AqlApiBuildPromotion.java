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
import org.artifactory.aql.result.rows.AqlBuildPromotion;

import java.util.ArrayList;

/**
 * @author gidis
 */
public class AqlApiBuildPromotion extends AqlBase<AqlApiBuildPromotion, AqlBuildPromotion> {

    public AqlApiBuildPromotion() {
        super(AqlBuildPromotion.class, true);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildPromotion> created() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildPromotions);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildPromotionCreated, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildPromotion> createdBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildPromotions);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildPromotionCreatedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildPromotion> comment() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildPromotions);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildPromotionComment, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildPromotion> status() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildPromotions);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildPromotionStatus, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildPromotion> repo() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildPromotions);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildPromotionRepo, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildPromotion> userName() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildPromotions);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.buildPromotionUserName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains<AqlApiBuildPromotion> build() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildPromotions, AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains(subDomains);
    }

    public static AqlApiBuildPromotion create() {
        return new AqlApiBuildPromotion();
    }
}