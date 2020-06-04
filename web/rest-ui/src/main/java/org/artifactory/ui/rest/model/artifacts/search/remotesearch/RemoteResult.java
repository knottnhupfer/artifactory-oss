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

package org.artifactory.ui.rest.model.artifacts.search.remotesearch;

import org.artifactory.api.bintray.BintrayItemInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class RemoteResult extends BaseSearchResult {
    private static final Logger log = LoggerFactory.getLogger(RemoteResult.class);
    private static final String STARTED_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private String path;
    private String packageName;
    private String release;
    private boolean cached;
    private long releaseAsLong;

    public RemoteResult(BintrayItemInfo bintrayItemInfo,ArtifactoryRestRequest request) {
        this.setName(bintrayItemInfo.getName());
        path = bintrayItemInfo.getPath();
        packageName = bintrayItemInfo.getPackage();
        formatReleaseDate(bintrayItemInfo);
        cached = bintrayItemInfo.isCached();
        setRepoKey(bintrayItemInfo.getRepo());
        RepoPath repoPath = InfoFactoryHolder.get().createRepoPath(getRepoKey(),getPath());
        setDownloadLink(request.getDownloadLink(repoPath));
        updateActions();
    }

    /**
     * format release date
     *
     * @param bintrayItemInfo
     * @throws ParseException
     */
    private void formatReleaseDate(BintrayItemInfo bintrayItemInfo) {
        DateFormat simpleDateFormat = new SimpleDateFormat(STARTED_FORMAT);
        try {
            releaseAsLong = simpleDateFormat.parse(bintrayItemInfo.getCreated()).getTime();
            release = ContextHelper.get().getCentralConfig().format(releaseAsLong);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @JsonProperty("package")
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getPath() {

        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    @Override
    protected void updateActions() {
        List<String> actions = new ArrayList<>();
        if (ContextHelper.get().getAuthorizationService().canDeploy(RepoPathFactory.create("jcenter", path))) {
            actions.add("Download");
        }
        actions.add("ShowInBintray");
        setActions(actions);
    }

    public long getReleaseAsLong() {
        return releaseAsLong;
    }

    public void setReleaseAsLong(long releaseAsLong) {
        this.releaseAsLong = releaseAsLong;
    }

    @Override
    public ItemSearchResult getSearchResult() {
        return null;
    }
}
