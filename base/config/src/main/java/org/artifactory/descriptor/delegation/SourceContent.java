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

package org.artifactory.descriptor.delegation;

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Properties descriptor
 *
 * @author Michael Pasternak
*/
@XmlType(name = "SourceContent", propOrder = {"originAbsenceDetection"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public class SourceContent implements Descriptor {

    public SourceContent() {
        super();
    }

    @XmlElement(name = "originAbsenceDetection", required = true, namespace = Descriptor.NS)
    private boolean originAbsenceDetection = false;

    /**
    * Checks whether remote content absence check is enabled
    *
    * @return boolean
    */
    public boolean isOriginAbsenceDetection() {
        return originAbsenceDetection;
    }

    /**
     * Sets whether remote content absence check is enabled
     *
     * @param originAbsenceDetection
     */
    public void setOriginAbsenceDetection(boolean originAbsenceDetection) {
        this.originAbsenceDetection = originAbsenceDetection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SourceContent that = (SourceContent) o;

        return originAbsenceDetection == that.originAbsenceDetection;
    }

    @Override
    public int hashCode() {
        return (originAbsenceDetection ? 1 : 0);
    }
}
