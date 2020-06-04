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

package org.artifactory.api.rest.constant;

/**
 * @author Michael Pasternak
 */
public interface ConfigRestConstants {
    String LOGO_URL_PATH = "logoUrl";
    String URL_BASE_PATH = "baseUrl";
    String PLATFORM_CONFIG = "platform/";
    String PLATFORM_URL_BASE_PATH = PLATFORM_CONFIG + "baseUrl";
    String MAIL_PATH = PLATFORM_CONFIG + "mail";
    String PROXY_PATH = PLATFORM_CONFIG + "proxies";
    String PLATFORM_SECURITY = PLATFORM_CONFIG + "security";
}
