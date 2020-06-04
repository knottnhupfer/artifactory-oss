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

package org.artifactory.api;

import org.artifactory.api.context.ArtifactoryContext;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * An internal service used during integration tests.
 *
 * @author Yossi Shaul
 */
public interface TestService {

    /**
     * Register a transaction leak information for test verifications.
     *
     * @param context Artifactory context
     * @param details String representation of the detected leak
     */
    void transactionLeak(ArtifactoryContext context, String details);

    /**
     * @return List of detected transaction leaks
     */
    @Nonnull
    List<String> getTransactionLeaks();

}
