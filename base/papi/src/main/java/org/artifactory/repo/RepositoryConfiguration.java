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

package org.artifactory.repo;

/**
 * @author Yoav Landman
 */
public interface RepositoryConfiguration {

    String TYPE_KEY = "rclass";

    String getKey();

    String getPackageType();

    String getType();

    String getDescription();

    String getExcludesPattern();

    String getIncludesPattern();

    String getNotes();

    String getRepoLayoutRef();

    boolean isEnableNuGetSupport();

    boolean isEnableGemsSupport();

    boolean isEnableNpmSupport();

    boolean isEnableBowerSupport();

    boolean isEnabledChefSupport();

    boolean isEnableCocoaPodsSupport();

    boolean isEnableConanSupport();

    boolean isEnableDebianSupport();

    boolean isDebianTrivialLayout();

    boolean isEnableDistRepoSupport();

    boolean isEnablePypiSupport();

    boolean isEnablePuppetSupport();

    boolean isEnableDockerSupport();

    String getDockerApiVersion();

    boolean isEnableVagrantSupport();

    boolean isEnableGitLfsSupport();

    boolean isForceNugetAuthentication();

    boolean isEnableComposerSupport();

    /**
     * These setters are for the BeanUtilsBean configuration copy. keep them public.
     * usage in {@see org.artifactory.addon.rest.RestAddonImpl.createOrReplaceRepository}
     * of {@see org.apache.commons.beanutils.BeanUtilsBean#copyProperties}
     */
    void setEnableNpmSupport(boolean enableNpmSupport);

    void setEnableChefSupport(boolean enableChefSupport);

    void setEnableBowerSupport(boolean enableBowerSupport);

    void setEnableCocoaPodsSupport(boolean enableCocoaPodsSupport);

    void setEnableConanSupport(boolean enableConanSupport);

    void setEnableDebianSupport(boolean enableDebianSupport);

    void setEnableDistRepoSupport(boolean enableDistRepoSupport);

    void setEnablePypiSupport(boolean enablePypiSupport);

    void setEnablePuppetSupport(boolean enablePuppetSupport);

    void setEnableDockerSupport(boolean enableDockerSupport);

    void setEnableVagrantSupport(boolean enableVagrantSupport);

    void setEnableGitLfsSupport(boolean enableGitLfsSupport);

    void setEnableComposerSupport(boolean enableComposerSupport);
}
