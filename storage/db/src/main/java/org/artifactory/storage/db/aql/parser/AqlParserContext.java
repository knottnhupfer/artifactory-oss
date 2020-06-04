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

package org.artifactory.storage.db.aql.parser;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import java.util.List;

/**
 * This context is being used in case of parser syntax error it provide an accurate location of the syntax error.
 *
 * @author Gidi Shabat
 */
public class AqlParserContext {
    private String queryRemainder;
    private List<ParserElement> elements = Lists.newArrayList();

    /**
     * Each time a parser element success(matches sub string), the parser peels off the relevant sub string from the string query
     * and update the context with the remaining query
     * @param query
     */
    public void update(String query) {
        if (this.queryRemainder == null || query.length() < this.queryRemainder.length()) {
            this.queryRemainder = query;
        }
    }

    public String getQueryRemainder() {
        return queryRemainder;
    }

    public void addElement(ParserElement element) {
        elements.add(element);
    }

    public List<ParserElement> getElements() {
        return elements;
    }
}
