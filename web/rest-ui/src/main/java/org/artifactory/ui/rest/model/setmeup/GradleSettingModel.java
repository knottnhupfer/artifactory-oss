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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author chen keinan
 */
public class GradleSettingModel extends BaseModel {

    private String pluginRepoKey;
    private String libsResolverRepoKey;
    private String libsPublisherRepoKey;
    private Boolean pluginUseMaven;
    private Boolean resolverUseMaven;
    private Boolean publisherUseMaven;
    private Boolean pluginUseIvy;
    private Boolean resolverUseIvy;
    private Boolean publisherUseIvy;
    private String pluginResolverLayout;
    private String libsResolverLayout;
    private String libsPublisherLayouts;
    private String snippetName;
    private String savedSnippetName;
    private String password;

    private Set<String> libsPublisher = Sets.newTreeSet((o1, o2) ->
            StringUtils.containsIgnoreCase(o1, "release") && !StringUtils.containsIgnoreCase(o1, "plugin") ? -1 : 1);
    private Set<String> pluginResolver = Sets.newTreeSet(
            (o1, o2) -> StringUtils.containsIgnoreCase(o1, "plugin") && StringUtils.containsIgnoreCase(o1, "release") ?
                    -1 : 1);
    private Set<String> LibsResolver = Sets.newTreeSet((o1, o2) ->
            StringUtils.containsIgnoreCase(o1, "release") && !StringUtils.containsIgnoreCase(o1, "plugin") ? -1 : 1);
    private List<String> layouts = new ArrayList<>();
    private String gradleSnippet;

    public GradleSettingModel() {}

    public GradleSettingModel(String gradleSnippet) {
        this.gradleSnippet=gradleSnippet;
    }

    public GradleSettingModel(String snippetName, String savedSnippetName) {
        this.snippetName = snippetName;
        this.savedSnippetName = savedSnippetName;
    }


    public Set<String> getLibsPublisher() {
        return libsPublisher;
    }

    public void setLibsPublisher(Set<String> libsPublisher) {
        this.libsPublisher = libsPublisher;
    }

    public Set<String> getPluginResolver() {
        return pluginResolver;
    }

    public void setPluginResolver(Set<String> pluginResolver) {
        this.pluginResolver = pluginResolver;
    }

    public Set<String> getLibsResolver() {
        return LibsResolver;
    }

    public void setLibsResolver(Set<String> libsResolver) {
        LibsResolver = libsResolver;
    }

    public List<String> getLayouts() {
        return layouts;
    }

    public void setLayouts(List<String> layouts) {
        this.layouts = layouts;
    }

    public String getPluginRepoKey() {
        return pluginRepoKey;
    }

    public void setPluginRepoKey(String pluginRepoKey) {
        this.pluginRepoKey = pluginRepoKey;
    }

    public String getLibsResolverRepoKey() {
        return libsResolverRepoKey;
    }

    public void setLibsResolverRepoKey(String libsResolverRepoKey) {
        this.libsResolverRepoKey = libsResolverRepoKey;
    }

    public String getLibsPublisherRepoKey() {
        return libsPublisherRepoKey;
    }

    public void setLibsPublisherRepoKey(String libsPublisherRepoKey) {
        this.libsPublisherRepoKey = libsPublisherRepoKey;
    }

    public String getPluginResolverLayout() {
        return pluginResolverLayout;
    }

    public void setPluginResolverLayout(String pluginResolverLayout) {
        this.pluginResolverLayout = pluginResolverLayout;
    }

    public String getLibsResolverLayout() {
        return libsResolverLayout;
    }

    public void setLibsResolverLayout(String libsResolverLayout) {
        this.libsResolverLayout = libsResolverLayout;
    }

    public String getLibsPublisherLayouts() {
        return libsPublisherLayouts;
    }

    public void setLibsPublisherLayouts(String libsPublisherLayouts) {
        this.libsPublisherLayouts = libsPublisherLayouts;
    }

    public Boolean getPluginUseMaven() {
        return pluginUseMaven;
    }

    public void setPluginUseMaven(Boolean pluginUseMaven) {
        this.pluginUseMaven = pluginUseMaven;
    }

    public Boolean getResolverUseMaven() {
        return resolverUseMaven;
    }

    public void setResolverUseMaven(Boolean resolverUseMaven) {
        this.resolverUseMaven = resolverUseMaven;
    }

    public Boolean getPublisherUseMaven() {
        return publisherUseMaven;
    }

    public void setPublisherUseMaven(Boolean publisherUseMaven) {
        this.publisherUseMaven = publisherUseMaven;
    }

    public Boolean getPluginUseIvy() {
        return pluginUseIvy;
    }

    public void setPluginUseIvy(Boolean pluginUseIvy) {
        this.pluginUseIvy = pluginUseIvy;
    }

    public Boolean getResolverUseIvy() {
        return resolverUseIvy;
    }

    public void setResolverUseIvy(Boolean resolverUseIvy) {
        this.resolverUseIvy = resolverUseIvy;
    }

    public Boolean getPublisherUseIvy() {
        return publisherUseIvy;
    }

    public void setPublisherUseIvy(Boolean publisherUseIvy) {
        this.publisherUseIvy = publisherUseIvy;
    }

    public String getGradleSnippet() {
        return gradleSnippet;
    }

    public void setGradleSnippet(String gradleSnippet) {
        this.gradleSnippet = gradleSnippet;
    }

    public String getSnippetName() {
        return snippetName;
    }

    public void setSnippetName(String snippetName) {
        this.snippetName = snippetName;
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

    public void clearProps() {
        pluginResolver = null;
        LibsResolver = null;
        libsPublisher = null;
        layouts = null;
    }
}
