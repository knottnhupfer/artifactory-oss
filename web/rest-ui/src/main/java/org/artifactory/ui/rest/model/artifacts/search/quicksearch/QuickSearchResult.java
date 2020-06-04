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

package org.artifactory.ui.rest.model.artifacts.search.quicksearch;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("quick")
public class QuickSearchResult extends BaseSearchResult {

    private String relativePath;
    private String relativeDirPath;

    public QuickSearchResult() {
        // for jackson
    }

    public QuickSearchResult(ArtifactSearchResult artifactSearchResult, ArtifactoryRestRequest request) {
        super.setModifiedDate(artifactSearchResult.getLastModified());
        super.setModifiedString(artifactSearchResult.getLastModifiedString());
        this.relativePath = artifactSearchResult.getRelativePath();
        String relDirPath = artifactSearchResult.getRelDirPath();
        if (StringUtils.isBlank(relDirPath)) {
            relDirPath = "[root]";
        }
        this.relativeDirPath = relDirPath;
        super.setRepoKey(artifactSearchResult.getRepoKey());
        super.setName(artifactSearchResult.getName());
        RepoPath repoPath = InfoFactoryHolder.get().createRepoPath(artifactSearchResult.getRepoKey(),
                artifactSearchResult.getRelativePath());
        setDownloadLink(request.getDownloadLink(repoPath));
        this.repoPath = repoPath;
        super.updateActions();
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getRelativeDirPath() {
        return relativeDirPath;
    }

    public void setRelativeDirPath(String relativeDirPath) {
        this.relativeDirPath = relativeDirPath;
    }

    @Override
    public ItemSearchResult getSearchResult() {
        ItemInfo itemInfo;
        RepoPath repoPath = InternalRepoPathFactory.create(getRepoKey(), getRelativePath());
        try {
            itemInfo = ContextHelper.get().getRepositoryService().getItemInfo(repoPath);
        } catch (ItemNotFoundRuntimeException e) {
            itemInfo = getItemInfo(repoPath);
        }
        return new ArtifactSearchResult(itemInfo);
    }
}
