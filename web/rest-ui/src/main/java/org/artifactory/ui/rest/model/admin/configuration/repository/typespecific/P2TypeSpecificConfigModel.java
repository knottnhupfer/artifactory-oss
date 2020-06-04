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

package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.p2.P2Repo;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.rest.common.exception.RepoConfigException;

import java.util.List;

import static java.util.Optional.ofNullable;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_SUPPRESS_POM_CHECKS;

/**
 * @author Dan Feldman
 */
public class P2TypeSpecificConfigModel extends MavenTypeSpecificConfigModel {

    //virtual
    private List<P2Repo> P2Repos;

    public P2TypeSpecificConfigModel() {
        this.suppressPomConsistencyChecks = DEFAULT_SUPPRESS_POM_CHECKS;
    }

    public List<P2Repo> getP2Repos() {
        return P2Repos;
    }

    public void setP2Repos(List<P2Repo> P2Repos) {
        this.P2Repos = P2Repos;
    }

    @Override
    public Boolean getSuppressPomConsistencyChecks() {
        return this.suppressPomConsistencyChecks;
    }

    @Override
    public void setSuppressPomConsistencyChecks(Boolean suppressPomConsistencyChecks) {
        this.suppressPomConsistencyChecks = suppressPomConsistencyChecks;
    }

    @Override
    public void validateLocalTypeSpecific() throws RepoConfigException {
        throw new RepoConfigException("Package type " + getRepoType().name()
                + " is unsupported in local repositories", SC_BAD_REQUEST);
    }

    @Override
    public void validateRemoteTypeSpecific() {
        setSuppressPomConsistencyChecks(ofNullable(getSuppressPomConsistencyChecks())
                .orElse(DEFAULT_SUPPRESS_POM_CHECKS));
        super.validateRemoteTypeSpecific();
    }

    @Override
    public void validateVirtualTypeSpecific(AddonsManager addonsManager) {
        setP2Repos(ofNullable(getP2Repos()).orElse(Lists.newArrayList()));
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.P2;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
