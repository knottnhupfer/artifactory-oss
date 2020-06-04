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

package org.artifactory.api.bintray.distribution.resolver;

import java.util.regex.Pattern;

/**
 * @author Yinon Avraham
 */
public enum DistributionRuleFilterType {

    repo("repo"),
    path("path");

    public static final Pattern GENERAL_CAP_GROUP_PATTERN = createCapGroupPattern("[a-z]+");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private final String qualifier;
    private final Pattern capGroupPattern;

    DistributionRuleFilterType(String qualifier) {
        this.qualifier = qualifier;
        this.capGroupPattern = createCapGroupPattern(qualifier);
    }

    private static Pattern createCapGroupPattern(String qualifier) {
        return Pattern.compile("(\\$\\{" + qualifier + ":[a-zA-Z0-9]+\\})"); // ${qualifier:group_id}
    }

    public String getQualifier() {
        return qualifier;
    }

    public Pattern getCaptureGroupPattern() {
        return capGroupPattern;
    }

    /**
     * Tries to extract a capture group's number.
     *
     * @param group the captured group text, e.g. "${repo:1}"
     * @return the int value of the group
     */
    public int getGroupNumber(String group) throws NumberFormatException {
        return Integer.parseInt(getGroupId(group));
    }

    /**
     * Extract a capture group's name.
     *
     * @param group the captured group text, e.g. "${repo:1}" or "${path:foo}"
     * @return the name of the group
     */
    public String getGroupName(String group) {
        return getGroupId(group);
    }

    /**
     * Check whether a given group is a named group (i.e. the group id is no a number)
     *
     * @param group the capture group reference to check
     * @return <code>true</code> if the given capture group reference is a named group, <code>false</code> otherwise
     * (i.e. references the group by its number)
     */
    public boolean isNamedGroup(String group) {
        return !NUMBER_PATTERN.matcher(getGroupId(group)).matches();
    }

    private String getGroupId(String group) {
        int start = ("${" + qualifier + ":").length();
        return group.substring(start, group.length() - 1);
    }
}
