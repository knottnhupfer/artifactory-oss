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

package org.artifactory.api.bintray.distribution.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds model info that can only be retrieved with valid auth information that users of this model
 * supply to the {@link org.artifactory.api.bintray.distribution.DistributionService} when calling relevant methods
 *
 * @author Dan Feldman
 */
public class DistributionRepoCreationDetails {

    public String oauthAppConfigKey;
    public String oauthToken;
    public List<String> orgLicenses = new ArrayList<>();
    public boolean isOrgPremium = false;
    public String org;
    public String clientId;

}
