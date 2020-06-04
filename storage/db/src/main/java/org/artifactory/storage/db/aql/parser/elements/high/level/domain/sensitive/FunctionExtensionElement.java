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

package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.filter.FilterComplexElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.filter.FilterComplexTailElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class FunctionExtensionElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        FilterComplexTailElement filterComplexTail = provide(FilterComplexTailElement.class);
        FilterComplexElement filterComplex = provide(FilterComplexElement.class);
        return forward(quotes, provide(FunctionElement.class), quotes, colon, openParenthesis,
                fork(filterComplex, forward(filterComplex, filterComplexTail)), closeParenthesis);
    }
}
