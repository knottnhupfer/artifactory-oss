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

package org.artifactory.aql.result.rows.populate;

import org.artifactory.aql.model.AqlLogicalFieldEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yinon Avraham
 */
public final class LogicalFieldResultPopulators {

    private static final Logger log = LoggerFactory.getLogger(LogicalFieldResultPopulators.class);
    private static final FieldResultPopulator nullPopulator = (populationContext, field) -> {};
    private static final FieldResultPopulator itemVirtualReposPopulator = new ItemVirtualReposPopulator();

    private LogicalFieldResultPopulators() {}

    public static FieldResultPopulator getPopulator(DomainSensitiveField field) {
        if (field.getField() == AqlLogicalFieldEnum.itemVirtualRepos) {
            return itemVirtualReposPopulator;
        }
        log.warn("Unhandled logical field in result: {}", field);
        return nullPopulator;
    }

}
