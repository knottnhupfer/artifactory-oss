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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;

/**
 * @author Chen Keinan
 */
public class FileNode extends BaseNode {

    private String type = "file";
    private String mimeType;
    private String fileType;

    public FileNode(RepoPath repoPath, String text, String repoType, RepoType repoPkgType, boolean isArchive) {
        super(repoPath);
        setText(text);
        setRepoType(repoType);
        if (!repoType.equals("remote") && !repoType.equals("virtual")) {
            mimeType = NamingUtils.getMimeType(getPath()).getType();
            if (isArchive) {
                setHasChild(true);
            }
        }
        setRepoPkgType(repoPkgType);
        if (isArchive) {
            setFileType("archive");
        }
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
