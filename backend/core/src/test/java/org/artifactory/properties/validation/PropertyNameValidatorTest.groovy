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

package org.artifactory.properties.validation

import org.artifactory.exception.ValidationException
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Tomer Mayost
 */
class PropertyNameValidatorTest extends Specification {

    @Unroll
    def "check validation with ##propName"() {


        when:
        PropertyNameValidator.validate(propName)

        then:
        noExceptionThrown()

        where:
        propName << ["xyz", "xx3df", "latest:build", "ff_gh"]
    }

    @Unroll
    def "fail on bad validation with #badPropName"() {


        when:
        PropertyNameValidator.validate(badPropName)

        then:
        thrown(ValidationException)

        where:
        badPropName << ["x yz", "3ss", "sd!", "w@", "fds#", "fds\$fr", "df^yy", "fg%gbfd", "fd&jj",
                        "kk*kk", " ", "", null, "fs/f", "das\\gfd", "ds~sf"
                        , "ss+s", "sd>f", "ww>w", "oo=o", "hh;h", "fgg,g", "vv±f", "aa§a",
                        "das`fds", "fds[", "fd]fds", "qw{d", "tr}fd", "fds(f", "as)fd", ""]
    }
}
