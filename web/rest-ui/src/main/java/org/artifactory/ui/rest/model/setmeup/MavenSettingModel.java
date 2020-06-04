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

package org.artifactory.ui.rest.model.setmeup;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.rest.common.model.BaseModel;

import java.util.Set;

/**
 * @author chen keinan
 */
public class MavenSettingModel extends BaseModel {

    private String release;
    private String snapshot;
    private String pluginRelease;
    private String pluginSnapshot;
    private String mirror;
    private String mavenSnippet;
    private String settings;
    private String savedSnippetName;
    private String password;
    private Set<String> releases = Sets.newTreeSet(
            (o1, o2) -> StringUtils.containsIgnoreCase(o1, "release") && !StringUtils.containsIgnoreCase(o1, "plugin") ?
                    -1 : 1);
    private Set<String> pluginReleases = Sets.newTreeSet(
            (o1, o2) -> StringUtils.containsIgnoreCase(o1, "plugin") || StringUtils.containsIgnoreCase(o1, "release") ?
                    -1 : 1);
    private Set<String> snapshots = Sets.newTreeSet((o1, o2) ->
            StringUtils.containsIgnoreCase(o1, "snapshot") && !StringUtils.containsIgnoreCase(o1, "plugin") ? -1 : 1);
    private Set<String> pluginSnapshots = Sets.newTreeSet(
            (o1, o2) -> StringUtils.containsIgnoreCase(o1, "plugin") || StringUtils.containsIgnoreCase(o1, "snapshot") ?
                    -1 : 1);
    private Set<String> anyMirror = Sets.newTreeSet((o1, o2) -> StringUtils.containsIgnoreCase(o1, "release") ? 1 : -1);

    public MavenSettingModel(){}

    public MavenSettingModel (String mavenSnippet){
        this.mavenSnippet = mavenSnippet;
    }

    public MavenSettingModel(String settings, String savedSnippetName) {
        this.settings = settings;
        this.savedSnippetName = savedSnippetName;
    }

    public Set<String> getReleases() {
        return releases;
    }

    public void setReleases(Set<String> releases) {
        this.releases = releases;
    }

    public Set<String> getPluginReleases() {
        return pluginReleases;
    }

    public void setPluginReleases(Set<String> pluginReleases) {
        this.pluginReleases = pluginReleases;
    }

    public Set<String> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(Set<String> snapshots) {
        this.snapshots = snapshots;
    }

    public Set<String> getPluginSnapshots() {
        return pluginSnapshots;
    }

    public void setPluginSnapshots(Set<String> pluginSnapshots) {
        this.pluginSnapshots = pluginSnapshots;
    }

    public Set<String> getAnyMirror() {
        return anyMirror;
    }

    public void setAnyMirror(Set<String> anyMirror) {
        this.anyMirror = anyMirror;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getPluginRelease() {
        return pluginRelease;
    }

    public void setPluginRelease(String pluginRelease) {
        this.pluginRelease = pluginRelease;
    }

    public String getPluginSnapshot() {
        return pluginSnapshot;
    }

    public void setPluginSnapshot(String pluginSnapshot) {
        this.pluginSnapshot = pluginSnapshot;
    }

    public String getMirror() {
        return mirror;
    }

    public void setMirror(String mirror) {
        this.mirror = mirror;
    }

    public String getMavenSnippet() {
        return mavenSnippet;
    }

    public void setMavenSnippet(String mavenSnippet) {
        this.mavenSnippet = mavenSnippet;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public String getSavedSnippetName() {
        return savedSnippetName;
    }

    public void setSavedSnippetName(String savedSnippetName) {
        this.savedSnippetName = savedSnippetName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void clearProps(){
        releases = null;
        pluginReleases = null;
        snapshots = null;
        pluginSnapshots = null;
        anyMirror = null;
    }
}
