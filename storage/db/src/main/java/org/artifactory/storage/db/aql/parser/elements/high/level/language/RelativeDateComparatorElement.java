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

package org.artifactory.storage.db.aql.parser.elements.high.level.language;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlRelativeDateComparatorEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

/**
 * @author gidis
 */
public class RelativeDateComparatorElement extends LazyParserElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> result = Lists.newArrayList();
        AqlRelativeDateComparatorEnum[] values = AqlRelativeDateComparatorEnum.values();
        for (AqlRelativeDateComparatorEnum value : values) {
            result.add(forward(new InternalNameElement(value.signature, true)));
        }
        ParserElement[] array = new ParserElement[result.size()];
        return fork(result.toArray(array));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
