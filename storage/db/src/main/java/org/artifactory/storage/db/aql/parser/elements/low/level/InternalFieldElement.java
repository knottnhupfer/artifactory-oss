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
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlOperatorEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class InternalFieldElement extends InternalParserElement {

    @Override
    public ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context) {
        String string;
        int index = StringUtils.indexOf(queryRemainder, "\"");
        if (index >= 0) {
            string = queryRemainder.substring(0, index);
            if (AqlComparatorEnum.value(string) != null) {
                return new ParserElementResultContainer[0];
            }
            if (AqlOperatorEnum.value(string) != null) {
                return new ParserElementResultContainer[0];
            }
            if (!AqlPhysicalFieldEnum.isKnownSignature(string.toLowerCase())) {
                return new ParserElementResultContainer[0];
            }
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
        AqlOperatorEnum[] values = AqlOperatorEnum.values();
        for (AqlOperatorEnum value : values) {
            result.add(value.signature);
        }
        return result;
    }
}
