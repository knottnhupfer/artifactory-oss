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

package org.artifactory.ui.rest.model.artifacts.search.classsearch;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.archive.ArchiveSearchResult;
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
@JsonTypeName("class")
public class ClassSearchResult extends BaseSearchResult {

    private String archiveName;
    private String archivePath;

    public ClassSearchResult() {
    }

    public ClassSearchResult(ArchiveSearchResult archiveSearchResult,ArtifactoryRestRequest request) {
        super.setRepoKey(archiveSearchResult.getRepoKey());
        super.setName(archiveSearchResult.getEntryPath());
        super.setModifiedDate(archiveSearchResult.getLastModified());
        super.setModifiedString(archiveSearchResult.getLastModifiedString());
        archiveName = archiveSearchResult.getItemInfo().getName();
        archivePath = archiveSearchResult.getItemInfo().getRelPath();
        RepoPath repoPath = InfoFactoryHolder.get().createRepoPath(archiveSearchResult.getRepoKey(),
                archiveSearchResult.getRelativePath());
        setDownloadLink(request.getDownloadLink(repoPath));
        this.repoPath = repoPath;
        updateActions();
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public String getArchivePath() {
        String path = archivePath.replaceAll(archiveName, "");
        return StringUtils.isBlank(path) ? "[root]" : path;
    }

    public void setArchivePath(String archivePath) {
        this.archivePath = archivePath;
    }

    @Override
    protected void updateActions() {
        super.updateActions();
        getActions().remove("Delete");
    }

    @Override
    public ItemSearchResult getSearchResult() {
        RepoPath repoPath = InternalRepoPathFactory.create(getRepoKey(), archivePath);
        ItemInfo itemInfo;
        try {
            itemInfo = ContextHelper.get().getRepositoryService().getItemInfo(repoPath);
        } catch (ItemNotFoundRuntimeException e) {
            itemInfo = getItemInfo(repoPath);
        }
        return new ArtifactSearchResult(itemInfo);
    }
}
