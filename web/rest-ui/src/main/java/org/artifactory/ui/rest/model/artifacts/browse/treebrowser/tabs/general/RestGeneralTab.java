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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author Chen Keinan
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = FileGeneralArtifactInfo.class, name = "file"),
                @JsonSubTypes.Type(value = FolderGeneralArtifactInfo.class, name = "folder"),
                @JsonSubTypes.Type(value = ArchiveGeneralArtifactInfo.class, name = "archive"),
                @JsonSubTypes.Type(value = RepositoryGeneralArtifactInfo.class, name = "repository"),
                @JsonSubTypes.Type(value = VirtualRemoteRepoGeneralArtifactInfo.class, name = "virtualRemoteRepository"),
                @JsonSubTypes.Type(value = VirtualRemoteFolderGeneralArtifactInfo.class, name = "virtualRemoteFolder"),
                @JsonSubTypes.Type(value = VirtualRemoteFileGeneralArtifactInfo.class, name = "virtualRemoteFile")})
public interface RestGeneralTab extends RestModel {

    void populateGeneralData();

    void populateGeneralData(VirtualRepoItem item);

    RepoPath retrieveRepoPath();
}
