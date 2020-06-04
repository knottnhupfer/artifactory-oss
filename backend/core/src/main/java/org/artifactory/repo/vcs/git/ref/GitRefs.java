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

package org.artifactory.repo.vcs.git.ref;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a git repository tags and branches.
 * Follows the get command 'curl https://github.com/twbs/bootstrap.git/info/refs?service=git-upload-pack'
 *
 * @author Shay Yaakov
 */
public class GitRefs {

    public static final String REFS_FILENAME = "gitrefs";
    public Set<GitRef> tags = Sets.newLinkedHashSet();
    public Set<GitRef> branches = Sets.newLinkedHashSet();

    public boolean isEmpty() {
        return tags.isEmpty() && branches.isEmpty();
    }

    public boolean contains(String tagOrBrnach) {
        for (GitRef tagRef : tags) {
            if (StringUtils.equalsIgnoreCase(tagRef.name, tagOrBrnach)) {
                return true;
            }
        }
        for (GitRef branchRef : branches) {
            if (StringUtils.equalsIgnoreCase(branchRef.name, tagOrBrnach)) {
                return true;
            }
        }
        return false;
    }

    public void addTags(Set<String> tagStrings) {
        this.tags.addAll(tagStrings.stream()
                        .map(tagString -> new GitRef(tagString, null, false))
                        .collect(Collectors.toList()));
    }

    public void merge(GitRefs other) {
        if (other == null) {
            return;
        }

        tags.addAll(other.tags);
        branches.addAll(other.branches);
    }

    public InputStream constructOriginalRefsStream() {
        final StringBuilder sb = new StringBuilder();
        for (GitRef tag : tags) {
            String commit = StringUtils.isNotBlank(tag.commitId) ? tag.commitId : "0000000000000000000000000000000000000000";
            sb.append("0000").append(commit).append(" refs/tags/").append(tag.name).append("\n");
        }
        for (GitRef branch : branches) {
            String commit = StringUtils.isNotBlank(branch.commitId) ? branch.commitId : "0000000000000000000000000000000000000000";
            sb.append("0000").append(commit).append(" refs/heads/").append(branch.name).append("\n");
        }

        return IOUtils.toInputStream(sb.toString());
    }
}
