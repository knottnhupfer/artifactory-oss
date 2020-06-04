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

package org.artifactory.addon.ha.message;

import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Shay Bagants
 */
@JsonTypeName("nugetRemoveEvent")
public class NuPkgRemoveBaseMessage extends HaBaseMessage {
    public String repoKey;
    public String packageId;
    public String packageVersion;

    public String packagePath;

    public NuPkgRemoveBaseMessage() {
        super("");
    }

    public NuPkgRemoveBaseMessage(String packagePath, String repoKey, String packageId, String packageVersion,
            String publishingMemberId) {
        super(publishingMemberId);
        this.repoKey = repoKey;
        this.packageId = packageId;
        this.packageVersion = packageVersion;
        this.packagePath = packagePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NuPkgRemoveBaseMessage that = (NuPkgRemoveBaseMessage) o;

        if (repoKey != null ? !repoKey.equals(that.repoKey) : that.repoKey != null) {
            return false;
        }
        if (packageId != null ? !packageId.equals(that.packageId) : that.packageId != null) {
            return false;
        }
        return packageVersion != null ? packageVersion.equals(that.packageVersion) : that.packageVersion == null;
    }

    @Override
    public int hashCode() {
        int result = repoKey != null ? repoKey.hashCode() : 0;
        result = 31 * result + (packageId != null ? packageId.hashCode() : 0);
        result = 31 * result + (packageVersion != null ? packageVersion.hashCode() : 0);
        return result;
    }
}
