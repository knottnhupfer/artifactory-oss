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

package org.artifactory.storage.db.aql.itest.service;

import org.artifactory.storage.db.aql.itest.service.optimizer.FileTypeOptimization;
import org.artifactory.storage.db.aql.itest.service.optimizer.PropertyCriteriaRelatedWithOr;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;
import org.jfrog.storage.DbType;

/**
 * @author Gidi Shabat
 */
public class AqlQueryOptimizer {
    private QueryOptimizer optimizer;

    public AqlQueryOptimizer(DbType dbType) {
        // Since the optimisation in each database type is different we need to get the database type and accordingly init it relevant optimizations
        optimizer = loadOptimizerForDatabase(dbType);
    }

    private QueryOptimizer loadOptimizerForDatabase(DbType dbType) {
        switch (dbType) {
            case DERBY: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
            case MYSQL: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
            case MARIADB: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
            case ORACLE: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
            case MSSQL: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
            case POSTGRESQL: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
        }
        throw new RuntimeException("Unsupported database type" + dbType.name());
    }


    public void optimize(AqlQuery aqlQuery) {
        optimizer.optimize(aqlQuery);
    }
}
