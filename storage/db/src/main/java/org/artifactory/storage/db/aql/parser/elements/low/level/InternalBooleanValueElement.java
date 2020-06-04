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
import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;

import java.util.List;

/**
 * @author Dan Feldman
 */
public class InternalBooleanValueElement extends InternalParserElement {

    @Override
    public ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context) {
        int min = Integer.MAX_VALUE;
        String string;
        // It is not string wrapped by semicolon therefore try to resolve string using delimiters
        for (String delimiter : AqlParser.DELIMITERS) {
            if (".".equals(delimiter)) {
                continue;
            }
            int i = queryRemainder.indexOf(delimiter);
            if (i >= 0 && i < min) {
                min = i;
            }
        }
        if ((min == Integer.MAX_VALUE && StringUtils.isBlank(queryRemainder)) || min == 0) {
            return new ParserElementResultContainer[0];
        }

        if (min != Integer.MAX_VALUE) {
            string = queryRemainder.substring(0, min).trim();

        } else {
            string = queryRemainder.trim();
        }
        for (String usedKey : AqlParser.USED_KEYS) {
            if (string.equals(usedKey)) {
                return new ParserElementResultContainer[0];
            }
        }
        String trim = StringUtils.replaceOnce(queryRemainder, string, "").trim();
        context.update(trim);
        return new ParserElementResultContainer[]{new ParserElementResultContainer(trim, string)};
    }

    @Override
    public List<String> next() {
        List<String> result = Lists.newArrayList();
        result.add("<boolean>");
        return result;
    }
}
