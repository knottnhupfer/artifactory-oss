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

package org.artifactory.aql.result.rows;

import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.AqlEagerResult;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Saffi Hartal
 */
public class AqlTestHelper {

    static public void compareResults(BiConsumer<Object, Object> checkThat, AqlEagerResult queryResult, Stream<AqlRowResult> queryResultStream2) {
        final int[] cnt = {0};
        try (Stream<AqlRowResult> queryResultStream = queryResultStream2) {
            queryResultStream.map(it -> (AqlBaseFullRowImpl) it).forEach(it -> {
                AqlBaseFullRowImpl result = (AqlBaseFullRowImpl) queryResult.getResult(cnt[0]);
                List<Object> a = goodFields(result.map);
                List<Object> b = goodFields(it.map);
                checkThat.accept(a, b);
                cnt[0]++;
            });
        }
    }

    public static List<Object> goodFields(Map<AqlFieldEnum, Object> map) {
        return map.entrySet().stream().map(item -> item.getValue()).filter(good -> good instanceof String || good instanceof Number).collect(Collectors.toList());
    }
}
