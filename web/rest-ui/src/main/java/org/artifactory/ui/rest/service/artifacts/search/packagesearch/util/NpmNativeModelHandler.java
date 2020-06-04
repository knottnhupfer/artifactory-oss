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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.util;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.NativeSummaryModel;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionNativeModel;

import java.util.Date;
import java.util.Set;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeConstants.*;

/**
 * @author Lior Gur
 */
public class NpmNativeModelHandler implements PackageNativeModelHandler {

    @Override
    public void mergeFields(PackageNativeModel packageNative, PackageNativeSearchResult versionResult) {
        packageNative.setName(getField(NPM_NAME, versionResult));
        packageNative.addKeywords(getField(NPM_KEYWORDS, versionResult));
        packageNative.addRepoKey(versionResult.getRepoKey());
        packageNative.setLastModified(getLatestLastModified(packageNative.getLastModified(), versionResult));
    }

    @Override
    public void mergeVersionFields(VersionNativeModel versionNative, PackageNativeSearchResult versionResult) {
        versionNative.setName(getField(NPM_VERSION, versionResult));
        versionNative.addRepoKey(versionResult.getRepoKey());
        versionNative.setLatestPath(
                getLatestPath(versionNative.getLatestPath(), versionNative.getLastModified(), versionResult));
        versionNative.setLastModified(getLatestLastModified(versionNative.getLastModified(), versionResult));
    }

    @Override
    public void mergeSummaryFields(NativeSummaryModel summary, PackageNativeSearchResult versionResult) {
        summary.setDescription(getLatestDescription(summary.getDescription(),
                summary.getLastModified(), versionResult));
        summary.setLatestPath(
                getLatestPath(summary.getLatestPath(), summary.getLastModified(), versionResult));
        summary.setLastModified(getLatestLastModified(summary.getLastModified(), versionResult));
    }

    @Override
    public Set<String> getPackagePropKeys() {
        return Sets.newHashSet(NPM_NAME, NPM_VERSION, NPM_KEYWORDS);
    }

    @Override
    public Set<String> getPackagePropKeysForDerby() {
        return Sets.newHashSet(NPM_NAME, NPM_VERSION);
    }

    @Override
    public Set<String> getVersionPropKeys() {
        return Sets.newHashSet(NPM_VERSION);
    }

    @Override
    public Set<String> getSummaryPropKeys() {
        return Sets.newHashSet(NPM_DESCRIPTION);
    }

    @Override
    public Set<String> getVersionExtraInfoPropKeys() {
        return Sets.newHashSet(NPM_KEYWORDS);
    }

    private String getField(String key, PackageNativeSearchResult pkg) {
        if (pkg.getExtraFields().get(key) == null) {
            return null;
        }
        String field = pkg.getExtraFields().get(key).isEmpty() ? null : pkg.getExtraFields().get(key).iterator().next();
        if (EMPTY_KEYWORD.equals(field)) {
            return null;
        }
        return field;
    }

    private long getLatestLastModified(long packageLastModifiedLong, PackageNativeSearchResult versionResult) {
        long resultLastModifiedLong = versionResult.getModifiedDate();
        if (packageLastModifiedLong == 0) {
            return resultLastModifiedLong;
        }
        Date packageLastModified = new Date(packageLastModifiedLong);
        Date resultLastModified = new Date(versionResult.getModifiedDate());
        if (resultLastModified.after(packageLastModified)) {
            return resultLastModifiedLong;
        }
        return packageLastModifiedLong;
    }

    private String getLatestDescription(String description, long lastModified,
            PackageNativeSearchResult versionResult) {
        String resultDescription = getField(NPM_DESCRIPTION, versionResult);
        if (StringUtils.isBlank(description)) {
            return resultDescription;
        }
        Date packageLastModified = new Date(lastModified);
        Date resultLastModified = new Date(versionResult.getModifiedDate());
        if (resultLastModified.after(packageLastModified)) {
            return resultDescription;
        }
        return description;
    }

    private String getLatestPath(String path, long lastModified, PackageNativeSearchResult versionResult) {
        String repoPath = getPath(versionResult.getRepoPath());
        if (StringUtils.isBlank(path)) {
            return repoPath;
        }
        Date packageLastModified = new Date(lastModified);
        Date resultLastModified = new Date(versionResult.getModifiedDate());
        if (resultLastModified.after(packageLastModified)) {
            return repoPath;
        }
        return path;
    }

    @Override
    public String getPath(RepoPath repoPath) {
        String path = repoPath.getPath();
        if (path.startsWith(".")) {
            path = path.replaceFirst("./", "");
        }
        return String.join("/", repoPath.getRepoKey(), path);
    }

    @Override
    public String getPackageName(PackageNativeSearchResult packageResult) {
        return getField(NPM_NAME, packageResult);
    }

    @Override
    public String getPackageVersion(PackageNativeSearchResult packageResult) {
        return getField(NPM_VERSION, packageResult);
    }

    @Override
    public String getNamePropKey() {
        return "npmName";
    }

    @Override
    public String getVersionPropKey() {
        return "npmVersion";
    }

}
