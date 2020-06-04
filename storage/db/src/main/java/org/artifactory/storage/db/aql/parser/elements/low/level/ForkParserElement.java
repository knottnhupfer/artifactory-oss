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

package org.artifactory.storage.db.aql.parser.elements.low.level;


import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import java.util.Collections;
import java.util.List;

/**
 * @author Gidi Shabat
 */
public class ForkParserElement implements ParserElement {

    private final List<ParserElement> elements = Lists.newArrayList();

    public ForkParserElement(ParserElement... elements) {
        Collections.addAll(this.elements, elements);
    }

    @Override
    public void initialize() {
        for (ParserElement element : elements) {
            element.initialize();
        }
    }

    @Override
    public List<String> next() {
        List<String> list = Lists.newArrayList();
        for (ParserElement element : elements) {
            List<String> next = element.next();
            if (next.size() > 0) {
                list.addAll(next);
            }
        }
        return list;
    }

    @Override
    public ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context) {
        List<ParserElementResultContainer> results = Lists.newArrayList();
        for (ParserElement element : elements) {
            ParserElementResultContainer[] parserResults = element.peelOff(queryRemainder, context);
            for (ParserElementResultContainer parserResult : parserResults) {
                if (StringUtils.isBlank(parserResult.getQueryRemainder())) {
                    return new ParserElementResultContainer[]{parserResult};
                } else {
                    results.add(parserResult);
                }
            }
        }
        if (results.isEmpty()) {
            return new ParserElementResultContainer[0];
        } else {
            return results.toArray(new ParserElementResultContainer[results.size()]);
        }
    }

}
