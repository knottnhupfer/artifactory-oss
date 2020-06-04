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

package org.artifactory.model.xstream.security;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.artifactory.security.MutableRepoPermissionTarget;
import org.artifactory.security.RepoPermissionTarget;
import org.jfrog.client.util.PathUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;

/**
 *
 */

//XStream kept here to backwards support security.xml import
@XStreamAlias("target")
public class RepoPermissionTargetImpl implements MutableRepoPermissionTarget {

    protected String name;
    protected List<String> includes = new ArrayList<>();
    protected List<String> excludes = new ArrayList<>();
    private List<String> repoKeys = new ArrayList<>();

    public RepoPermissionTargetImpl() {
        this("", singletonList(ANY_REPO));
    }

    public RepoPermissionTargetImpl(String name) {
        this(name, singletonList(ANY_REPO));
    }

    public RepoPermissionTargetImpl(String name, List<String> repoKeys) {
        this.name = name;
        this.repoKeys = new ArrayList<>(repoKeys);
        this.includes.add(ANY_PATH);
    }

    public RepoPermissionTargetImpl(String name, List<String> repoKeys, List<String> includes, List<String> excludes) {
        this.name = name;
        this.repoKeys = repoKeys;
        this.includes = includes;
        this.excludes = excludes;
    }

    public RepoPermissionTargetImpl(String name, List<String> includes, List<String> excludes) {
        this.name = name;
        this.includes = includes;
        this.excludes = excludes;
    }

    public RepoPermissionTargetImpl(RepoPermissionTarget copy) {
        this(copy.getName(),
                new ArrayList<>(copy.getRepoKeys()),
                new ArrayList<>(copy.getIncludes()),
                new ArrayList<>(copy.getExcludes())
        );
    }

    public RepoPermissionTargetImpl(String name, List<String> repoKeys, String includes, String excludes) {
        this(name, repoKeys);
        setIncludesPattern(includes);
        setExcludesPattern(excludes);
    }

    @Override
    public List<String> getRepoKeys() {
        return repoKeys;
    }

    @Override
    public void setRepoKeys(List<String> repoKeys) {
        this.repoKeys = new ArrayList<>(repoKeys);
        Collections.sort(this.repoKeys);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    @Override
    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    @Override
    public void setIncludesPattern(String includesPattern) {
        this.includes = PathUtils.includesExcludesPatternToStringList(includesPattern);
    }

    @Override
    public void setExcludesPattern(String excludesPattern) {
        this.excludes = PathUtils.includesExcludesPatternToStringList(excludesPattern);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<String> getIncludes() {
        return this.includes;
    }

    @Override
    public List<String> getExcludes() {
        return this.excludes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepoPermissionTargetImpl that = (RepoPermissionTargetImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}