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

package org.artifactory.ui.rest.model.artifacts.search;

import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.property.PropertySearchResult;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.NamingUtils;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("stash")
public class StashResult extends BaseSearchResult {

    private String relativePath;
    private String relativeDirPath;
    private String resultType;
    private String mimeType;
    private RepoType repoPkgType = null;
    private String fileType;

    public StashResult(PropertySearchResult propertyResult) {
        super.setRepoKey(propertyResult.getRepoKey());

    }

    public StashResult(String name, String relativePath, String repoKey, RepoType repoPkgType, boolean isArchive) {
        super.setRepoKey(repoKey);
        setName(name);
        this.relativePath = relativePath;
        this.setRepoKey(repoKey);
        this.mimeType = NamingUtils.getMimeType(relativePath).getType();
        setRepoPkgType(repoPkgType);
        if (isArchive) {
            setFileType("archive");
        }
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

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public RepoType getRepoPkgType() {
        return repoPkgType;
    }

    public void setRepoPkgType(RepoType repoPkgType) {
        this.repoPkgType = repoPkgType;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public ItemSearchResult getSearchResult() {
        return null;
    }
}
