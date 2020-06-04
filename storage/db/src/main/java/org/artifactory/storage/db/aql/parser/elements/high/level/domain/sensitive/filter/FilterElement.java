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

package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.filter;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.DomainSensitiveParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.FunctionExtensionElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.criteria.*;

/**
 * @author Gidi Shabat
 */
public class FilterElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        return fork(provide(CriteriaEqualsKeyPropertyElement.class), provide(CriteriaEqualsValuePropertyElement.class),
                provide(CriteriaKeyPropertyElement.class), provide(CriteriaValuePropertyElement.class),
                provide(EqualsCriteriaElement.class), provide(DefaultCriteriaElement.class),
                provide(CriteriaEqualsPropertyElement.class), provide(CriteriaDefaultPropertyElement.class),
                provide(CriteriaRelativeDateElement.class),provide(FunctionExtensionElement.class));
    }

}
