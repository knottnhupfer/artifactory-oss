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

package org.artifactory.security;

/**
 * Defines the plugin realm authentication policy
 *
 * @author Shay Yaakov
 * @since 4.1.0
 */
public enum RealmPolicy {

    /**
     * Executed in the start of the authentication chain before any other built-in authentication realms (Ldap, Internal etc.)
     * When authentication succeeds, all other built-in realms are skipped. This is the default behavior
     */
    SUFFICIENT,

    /**
     * Executed additionally if at least one of the previous authentication provider has succeeded
     */
    ADDITIVE
}