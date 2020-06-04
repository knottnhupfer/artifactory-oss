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

package org.artifactory.ui.rest.model.admin.configuration.repository;

import org.artifactory.rest.common.model.RestModel;

/**
 * @author Dan Feldman
 */
public interface BasicRepositoryConfigModel extends RestModel {

    void setPublicDescription(String publicDescription);

    String getPublicDescription();

    void setInternalDescription(String internalDescription);

    String getInternalDescription();

    String getIncludesPattern();

    void setIncludesPattern(String includesPattern);

    String getExcludesPattern();

    void setExcludesPattern(String excludesPattern);

    String getLayout();

    void setLayout(String layout);
}
