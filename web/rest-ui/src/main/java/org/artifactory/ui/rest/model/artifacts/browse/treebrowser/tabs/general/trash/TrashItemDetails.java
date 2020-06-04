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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.trash;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.md.Properties;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Shay Yaakov
 */
public class TrashItemDetails extends BaseModel {

    private String deletedTime;
    private String deletedBy;
    private String originalRepository;
    private String originalRepositoryType;
    private String originalPath;

    public TrashItemDetails() {
    }

    public TrashItemDetails(Properties properties) {
        String deleted = properties.getFirst(TrashService.PROP_TRASH_TIME);
        if (StringUtils.isNotBlank(deleted)) {
            deletedTime = ContextHelper.get().getCentralConfig().format(Long.parseLong(deleted));
        }
        deletedBy = properties.getFirst(TrashService.PROP_DELETED_BY);
        originalRepository = properties.getFirst(TrashService.PROP_ORIGIN_REPO);
        originalRepositoryType = properties.getFirst(TrashService.PROP_ORIGIN_REPO_TYPE);
        originalPath = properties.getFirst(TrashService.PROP_ORIGIN_PATH);
    }

    public String getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(String deletedTime) {
        this.deletedTime = deletedTime;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getOriginalRepository() {
        return originalRepository;
    }

    public void setOriginalRepository(String originalRepository) {
        this.originalRepository = originalRepository;
    }

    public String getOriginalRepositoryType() {
        return originalRepositoryType;
    }

    public void setOriginalRepositoryType(String originalRepositoryType) {
        this.originalRepositoryType = originalRepositoryType;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }
}
