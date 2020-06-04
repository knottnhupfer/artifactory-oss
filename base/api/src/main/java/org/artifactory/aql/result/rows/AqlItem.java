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

package org.artifactory.aql.result.rows;


import org.artifactory.aql.model.AqlItemTypeEnum;

import java.util.Date;

import static org.artifactory.aql.model.AqlDomainEnum.items;
import static org.artifactory.aql.model.AqlPhysicalFieldEnum.*;

/**
 * @author Gidi Shabat
 */
@QueryTypes(value = items, physicalFields = {itemId, itemType, itemRepo, itemPath, itemName,
        itemDepth, itemCreated, itemCreatedBy, itemModified, itemModifiedBy, itemUpdated,
        itemSize, itemActualSha1, itemOriginalSha1, itemActualMd5, itemOriginalMd5, itemSha2})
public interface AqlItem extends AqlRowResult {
    Date getCreated();

    Date getModified();

    Date getUpdated();

    String getCreatedBy();

    String getModifiedBy();

    AqlItemTypeEnum getType();

    String getRepo();

    String getPath();

    String getName();

    long getSize();

    int getDepth();

    Long getNodeId();

    String getOriginalMd5();

    String getActualMd5();

    String getOriginalSha1();

    String getActualSha1();

    String getSha2();

    String[] getVirtualRepos();
}
