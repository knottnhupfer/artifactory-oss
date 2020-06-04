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

package org.artifactory.storage.db.aql.parser.elements;


import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;

import java.util.List;

/**
 * The parser is actually group of parser elements that represent the language possibilities tree.
 * Each element represent intersection and its sub-tree in the possibilities tree.
 *
 * @author Gidi Shabat
 */
public interface ParserElement {


    /**
     * Returns possible matches between the queryRemainder to this parserElement.
     *
     * @param queryRemainder The left over for other parser elements to parse
     * @param context        Path resolution context
     * @return Possible parsing paths
     */
    ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context);

    /**
     * One time initialization of the parsing tree.
     * TODO: should be internal only
     */
    void initialize();

    /**
     * Returns the next possibilities
     *
     * @return Possible parsing elements
     */
    List<String> next();
}
