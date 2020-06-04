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

package org.artifactory.addon.go;

/**
 * Specifies the git provider
 *
 * @author Liz Dashevski
 */
public enum GoGitProvider {
    Github("github.com/"),
    BitBucket("bitbucket.org/"), //unsupported by vgo for now
    Gopkgin("gopkg.in/"),
    Golang("golang.org/"),
    K8s("k8s.io/"),
    GoGoogleSource("go.googlesource.com/"),
    CloudGoogleSource("cloud.google.com/"),
    Artifactory("");

    private final String prefix;

    GoGitProvider(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return getPrefix();
    }

    public String getPrefix() {
        return prefix;
    }
}

