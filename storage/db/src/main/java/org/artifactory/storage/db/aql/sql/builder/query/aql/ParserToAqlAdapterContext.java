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

package org.artifactory.storage.db.aql.sql.builder.query.aql;

import com.google.common.collect.Lists;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.jfrog.security.util.Pair;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class ParserToAqlAdapterContext<T extends AqlRowResult> extends AdapterContext<T> {

    private int index;
    private List<Pair<ParserElement, String>> elements = Lists.newArrayList();

    ParserToAqlAdapterContext(List<Pair<ParserElement, String>> elements) {
        this.elements = elements;
        index = elements.size() - 1;
    }

    public Pair<ParserElement, String> getElement() {
        return elements.get(index);
    }

    void decrementIndex(int i) {
        index = index - i;
    }

    public int getIndex() {
        return index;
    }

    void resetIndex() {
        index = elements.size() - 1;
    }

    public boolean hasNext() {
        return index >= 0;
    }
}
