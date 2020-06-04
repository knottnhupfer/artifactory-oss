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
 * @author Gidi Shabat
 */
public class InternalNameElement extends InternalParserElement {

    private String name;
    private boolean ignoreCase;

    public InternalNameElement(String name) {
        this.name = name;
        this.ignoreCase = false;
    }

    public InternalNameElement(String name, boolean ignoreCase) {
        this.name = name;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context) {
        int min = Integer.MAX_VALUE;
        String string = "";
        for (String delimiter : AqlParser.DELIMITERS) {
            int i = queryRemainder.indexOf(delimiter);
            if (i >= 0 && i < min) {
                min = i;
                if (min == 0) {
                    string = delimiter;
                    break;
                }
            }
        }
        if (min == Integer.MAX_VALUE && StringUtils.isBlank(queryRemainder)) {
            return new ParserElementResultContainer[0];
        }
        if (min != Integer.MAX_VALUE) {
            if (min > 0) {
                string = queryRemainder.substring(0, min).trim();
            }

        } else {
            string = queryRemainder.trim();
        }
        String cleanName = ignoreCase ? name.toLowerCase() : name;
        String cleanString = ignoreCase ? string.toLowerCase() : string;
        if (cleanString.equals(cleanName)) {
            String trim = StringUtils.replaceOnce(queryRemainder, string, "").trim();
            context.update(trim);
            return new ParserElementResultContainer[]{new ParserElementResultContainer(trim, string)};
        } else {
            return new ParserElementResultContainer[0];
        }
    }

    @Override
    public List<String> next() {
        List<String> result = Lists.newArrayList();
        if (ignoreCase) {
            result.add(name);
        } else {
            result.add(name.toLowerCase());
        }
        return result;
    }
}
