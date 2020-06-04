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

package org.artifactory.api.security.access;

import org.jfrog.access.common.ServiceId;
import org.jfrog.access.common.SubjectFQN;

import javax.annotation.Nonnull;

/**
 * @author Yinon Avraham.
 */
public class GenericTokenSpec extends TokenSpec<GenericTokenSpec> {

    private final SubjectFQN subject;

    private GenericTokenSpec(@Nonnull String subject) {
        this.subject = SubjectFQN.fromFullyQualifiedName(subject);
    }

    /**
     * Create a new generic token specification.
     * @param subject the subject
     * @return a new empty generic token specification
     */
    @Nonnull
    public static GenericTokenSpec create(@Nonnull String subject) {
        return new GenericTokenSpec(subject);
    }

    @Override
    public SubjectFQN createSubject(ServiceId serviceId) {
        return subject;
    }

    public static boolean accepts(String subject) {
        try {
            SubjectFQN.fromFullyQualifiedName(subject);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
