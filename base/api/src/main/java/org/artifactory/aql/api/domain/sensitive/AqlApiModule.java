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
import org.artifactory.aql.result.rows.AqlBuildModule;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiModule extends AqlBase<AqlApiArtifact, AqlBuildModule> {

    public AqlApiModule() {
        super(AqlBuildModule.class, true);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiModule> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.modules);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.moduleName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiArtifactDynamicFieldsDomains<AqlApiModule> artifact() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.modules, AqlDomainEnum.artifacts);
        return new AqlApiDynamicFieldsDomains.AqlApiArtifactDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiDependencyDynamicFieldsDomains<AqlApiModule> dependency() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.modules, AqlDomainEnum.dependencies);
        return new AqlApiDynamicFieldsDomains.AqlApiDependencyDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains<AqlApiModule> build() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.modules, AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains(subDomains);
    }

    public static AqlApiModule create() {
        return new AqlApiModule();
    }
}