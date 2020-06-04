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

package org.artifactory.storage.db.xray.dao;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A data access table for xray.
 *
 * @author Shay Bagants
 */
@Repository
public class XrayDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(XrayDao.class);

    @Autowired
    public XrayDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    /**
     * Get the list of artifacts that are allowed to be indexed (including these which already indexed or being indexed)
     *
     * @param repoKey    The repository key
     * @param extensions The list of extensions (possible candidates for xray index) to search
     * @param fileNames  The list of filenames (possible candidates for xray index) to search
     * @return The amount of artifacts that are potential for xray indexing
     */
    public int getPotentialForIndex(String repoKey, Set<String> extensions, Set<String> fileNames) {
        assertValidArguments(repoKey, extensions, fileNames);
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(node_name) FROM nodes WHERE node_type=1 AND repo = ? AND (");
        List<String> params = Lists.newArrayList(repoKey);
        sb.append(buildExtensionsCriteria(extensions, fileNames, params));
        sb.append(")");
        try {
            return jdbcHelper.executeSelectCount(sb.toString(), params.toArray());
        } catch (SQLException e) {
            log.error("Failed to count the number of potential for xray indexing for repo '" + repoKey + "'");
            log.debug("Failed to execute query", e);
            throw new StorageException(e);
        }
    }

    private String buildExtensionsCriteria(Set<String> extensions, Set<String> fileNames, List<String> params) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = extensions.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (i > 0) {
                sb.append(" OR ");
            }
            sb.append("node_name like ?");
            params.add("%" + it.next());
            i++;
        }

        it = fileNames.iterator();
        while (it.hasNext()) {
            if (i > 0) {
                sb.append(" OR ");
            }
            sb.append("node_name=?");
            params.add(it.next());
        }

        return sb.toString();
    }

    //Usually, this should not be here and there should be no handler with empty both 'extensions' and 'fileNames', however
    //if all of these will be empty, it might cause huge searches on the db.
    private void assertValidArguments(String repoKey, Set<String> extensions, Set<String> fileNames) {
        if (CollectionUtils.isNullOrEmpty(extensions) && CollectionUtils.isNullOrEmpty(fileNames)) {
            throw new IllegalArgumentException("extensions and file names potential cannot be empty");
        }
        if (StringUtils.isBlank(repoKey)) {
            throw new IllegalArgumentException("Repository key cannot be empty");
        }
    }

    /**
     * Used to delete all old Xray properties from DB.
     * Gets a list of prop_id values and deletes them in batches according to {@param batchThreshold}
     *
     * @throws StorageException in case failed to delete from DB
     */
    public void bulkDeleteXrayProperties(List<Long> params, int batchThreshold) {
        List<Long> fullList = new ArrayList<>(params);
        List<Long> subList;
        int passed = 0;
        int numDeleted = 0;
        int toDelete = Math.min(batchThreshold + passed, fullList.size());
        try {
            while (passed <= toDelete && (subList = fullList.subList(passed, toDelete)).size() > 0) {
                String query = getDeleteQueryMultipleValuesSql(subList.size());
                int currentDelete = jdbcHelper.executeUpdate(query, subList.toArray());
                numDeleted += currentDelete;
                log.debug("Deleted {} Xray properties from DB", currentDelete);
                passed += subList.size();
                toDelete = Math.min(batchThreshold + passed, fullList.size());
            }
        } catch (SQLException e) {
            log.error("Failed to delete Xray properties from DB");
            log.debug("Failed to execute query", e);
            throw new StorageException(e);
        }
        log.debug("Deleted a total of {} Xray properties from DB", numDeleted);
    }

    public String getDeleteQueryMultipleValuesSql(int numberOfValues) {
        return "DELETE FROM node_props WHERE prop_id IN (" +
                IntStream.range(0, numberOfValues).mapToObj(i -> "?").collect(Collectors.joining(", ")) + ")";
    }
}
