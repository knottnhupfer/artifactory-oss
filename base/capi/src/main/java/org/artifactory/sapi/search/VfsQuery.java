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

package org.artifactory.sapi.search;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Collection;

/**
 * Date: 8/5/11
 * Time: 6:04 PM
 *
 * @author Fred Simon
 */
public interface VfsQuery {
    VfsQuery expectedResult(@Nonnull VfsQueryResultType itemType);

    VfsQuery setSingleRepoKey(String repoKey);

    VfsQuery setRepoKeys(Collection<String> repoKeys);

    VfsQuery orderByAscending(@Nonnull String propertyName);

    VfsQuery orderByDescending(@Nonnull String propertyName);

    VfsQuery name(@Nonnull String nodeName);

    VfsQuery archiveName(@Nonnull String entryName);

    VfsQuery archivePath(@Nonnull String entryPath);

    VfsQuery prop(@Nonnull String propertyName);

    VfsQuery comp(@Nonnull VfsComparatorType comparator);

    VfsQuery func(@Nonnull VfsFunctionType function);

    VfsQuery val(String... values);

    VfsQuery val(@Nonnull Long value);

    VfsQuery val(@Nonnull Calendar value);

    VfsQuery nextBool(@Nonnull VfsBoolType bool);

    VfsQuery startGroup();

    VfsQuery endGroup(@Nullable VfsBoolType bool);

    VfsQuery endGroup();

    VfsQuery addPathFilters(String... folderNames);

    VfsQuery addPathFilter(String pathSearch);

    @Nonnull
    VfsQueryResult execute(long limit);
}
