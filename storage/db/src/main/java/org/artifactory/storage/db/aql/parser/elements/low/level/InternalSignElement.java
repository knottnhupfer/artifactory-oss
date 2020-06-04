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
import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class InternalSignElement extends InternalParserElement {
    private String sign;

    public InternalSignElement(String sign) {
        this.sign = sign;
    }

    @Override
    public ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context) {
        if (queryRemainder.startsWith(sign)) {
            String string;
            if ("]".equals(sign) || "[".equals(sign) || ")".equals(sign) || "(".equals(sign)) {
                string = queryRemainder.replaceFirst("\\" + sign, "").trim();
            } else {
                string = queryRemainder.replaceFirst("[" + sign + "]", "").trim();
            }
            context.update(string);
            return new ParserElementResultContainer[]{new ParserElementResultContainer(string, sign)};
        } else {
            return new ParserElementResultContainer[0];
        }
    }

    @Override
    public List<String> next() {
        List<String> result = Lists.newArrayList();
        result.add(sign);
        return result;
    }
}



