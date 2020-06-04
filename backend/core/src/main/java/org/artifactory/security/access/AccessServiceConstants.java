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

package org.artifactory.security.access;

import org.jfrog.access.common.ServiceType;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.String.join;
import static org.jfrog.access.common.ServiceId.ANY_INSTANCE_ID_REGEX;
import static org.jfrog.access.common.ServiceId.createServiceIdRegex;

/**
 * @author Yinon Avraham.
 */
public abstract class AccessServiceConstants {

    private AccessServiceConstants() {
        //utility class
    }

    /**
     * The Artifactory service type, used as part of the service ID.
     */
    public static final String ARTIFACTORY_SERVICE_TYPE = ServiceType.ARTIFACTORY;

    /**
     * All Artifactory service types, including the currently effective name and any deprecated names.
     */
    private static final List<String> ARTIFACTORY_SERVICE_TYPES = Arrays.asList(ARTIFACTORY_SERVICE_TYPE, "jf-artifactory");

    private static final String ARTIFACTORY_SERVICE_TYPES_REGEX = "(" + join("|", ARTIFACTORY_SERVICE_TYPES) + ")";

    /**
     * A regular expression of all artifactory service types - current and deprecated (for backward compatibility)
     */
    public static final String ARTIFACTORY_SERVICE_ID_REGEX = createServiceIdRegex(ARTIFACTORY_SERVICE_TYPES_REGEX);

    public static final String APPLIED_PERMISSIONS = "applied-permissions";

    /**
     * A pattern that matches a service ID with either of the artifactory service type (current or deprecated) and any
     * instance ID - specific or any (specified by "*")
     */
    public static final Pattern ARTIFACTORY_SERVICE_ANY_ID_PATTERN = Pattern.compile(createServiceIdRegex(
            ARTIFACTORY_SERVICE_TYPES_REGEX, ANY_INSTANCE_ID_REGEX));
}
