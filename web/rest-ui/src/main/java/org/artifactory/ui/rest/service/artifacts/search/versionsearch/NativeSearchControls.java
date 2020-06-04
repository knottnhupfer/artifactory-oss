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

package org.artifactory.ui.rest.service.artifacts.search.versionsearch;

import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.common.ConstantValues;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeModelHandler;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeModelHandlersFactory;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestDateEnum;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Nadav Yogev
 */
@Data
@Builder
public class NativeSearchControls {

    private List<AqlUISearchModel> searches;
    private String type;
    private String packageName;
    private String versionName;
    private String repoName;
    @Builder.Default private String sort = "";
    @Builder.Default private String order = "";
    private String limit;
    private String from;
    @Builder.Default private int limitModifier = 1;
    private int offset;
    private boolean withXray;
    @ToString.Exclude private PackageNativeModelHandler modelHandler;

    public void addFromDateToQuery(AqlApiItem.AndClause query) {
        if (StringUtils.isNotBlank(from)) {
            DateTime fromDate = PackageNativeRestDateEnum.valueOf(from).getFromDate();
            query.append(AqlApiItem.modified().greater(fromDate));
        }
    }

    public void addOffset() {
        this.offset += limit();
    }

    public int limit() {
        return (!Strings.isNullOrEmpty(limit) ? Integer.parseInt(limit) :
                ConstantValues.packageNativeUiResults.getInt()) * limitModifier;
    }

    public PackageNativeModelHandler getModelHandler() {
        if (modelHandler == null) {
            modelHandler = PackageNativeModelHandlersFactory.getModelHandler(type);
        }
        return modelHandler;
    }
}
