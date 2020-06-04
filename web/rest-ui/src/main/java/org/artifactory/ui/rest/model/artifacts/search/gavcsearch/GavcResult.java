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

package org.artifactory.ui.rest.model.artifacts.search.gavcsearch;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.gavc.GavcSearchResult;
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
@JsonTypeName("gavc")
public class GavcResult extends BaseSearchResult {

    private String relativePath;
    private String classifier;
    private String version;
    private String artifactID;
    private String groupID;

    public GavcResult() {
    }

    public GavcResult(GavcSearchResult gavcSearchResult,ArtifactoryRestRequest request) {
        super.setRepoKey(gavcSearchResult.getRepoKey());
        super.setName(gavcSearchResult.getName());
        super.setModifiedDate(gavcSearchResult.getLastModified());
        super.setModifiedString(gavcSearchResult.getLastModifiedString());
        classifier = gavcSearchResult.getClassifier();
        artifactID = gavcSearchResult.getArtifactId();
        groupID = gavcSearchResult.getGroupId();
        version = gavcSearchResult.getVersion();
        relativePath = gavcSearchResult.getItemInfo().getRelPath();
        RepoPath repoPath = InfoFactoryHolder.get().createRepoPath(gavcSearchResult.getRepoKey(),
                gavcSearchResult.getRelativePath());
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

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArtifactID() {
        return artifactID;
    }

    public void setArtifactID(String artifactID) {
        this.artifactID = artifactID;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }


    @Override
    public ItemSearchResult getSearchResult() {
        RepoPath repoPath = InternalRepoPathFactory.create(getRepoKey(), getRelativePath());
        ItemInfo itemInfo;
        try {
            itemInfo = ContextHelper.get().getRepositoryService().getItemInfo(repoPath);
        } catch (ItemNotFoundRuntimeException e) {
            itemInfo = getItemInfo(repoPath);
        }
        return new ArtifactSearchResult(itemInfo);
    }
}
