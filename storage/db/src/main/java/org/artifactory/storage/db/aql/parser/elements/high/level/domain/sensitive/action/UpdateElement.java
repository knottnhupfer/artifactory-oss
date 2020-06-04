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

package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.action;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.DomainSensitiveParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.filter.FilterComplexElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.filter.FilterComplexTailElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.ForkParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Dan Feldman
 */
public class UpdateElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        FilterComplexElement filterComplexElement = provide(FilterComplexElement.class);
        FilterComplexTailElement complexTailElement = provide(FilterComplexTailElement.class);
        ForkParserElement filter = fork(empty, forward(filterComplexElement, complexTailElement));
        return forward(updateAction, openBrackets, filter, closeBrackets);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
