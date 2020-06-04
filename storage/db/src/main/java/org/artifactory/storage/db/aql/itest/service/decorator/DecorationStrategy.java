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

package org.artifactory.storage.db.aql.itest.service.decorator;

import org.artifactory.aql.model.AqlField;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.model.AqlVariable;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQueryElement;
import org.artifactory.storage.db.aql.sql.builder.query.aql.Criterion;

import java.util.List;

/**
 * @author Shay Yaakov
 */
public interface DecorationStrategy {

    <T extends AqlRowResult> void decorate(AqlQuery<T> aqlQuery, AqlQueryDecoratorContext decoratorContext);

    /**
     * Returns the index of the next {@link Criterion} containing {@param field} starting from {@param index}, -1 if no
     * such criteria exists from that index.
     */
    default int findNextFieldCriteria(int start, AqlPhysicalFieldEnum field, List<AqlQueryElement> aqlQueryElements) {
        int index = start;
        for (; index < aqlQueryElements.size(); index++) {
            AqlQueryElement element = aqlQueryElements.get(index);
            if (element instanceof Criterion) {
                Criterion criterion = (Criterion) element;
                AqlVariable var1 = criterion.getVariable1();
                if (var1 instanceof AqlField && ((AqlField)var1).getFieldEnum() == field) {
                    return index;
                }
            }
        }
        return -1;
    }
}
