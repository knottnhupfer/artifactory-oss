package org.artifactory.storage.db.conversion.version.v215;

import org.artifactory.storage.db.conversion.ConversionPredicate;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.function.BiPredicate;

import static org.jfrog.storage.util.DbUtils.columnExists;

/**
 * @author Yoaz Menda
 */
public class V215ConversionPredicate implements ConversionPredicate {

    private static final Logger log = LoggerFactory.getLogger(V215ConversionPredicate.class);

    private static final String COLUMN_NAME = "original_component_details";
    private static final String TABLE_NAME = "bundle_files";

    @Override
    public BiPredicate<JdbcHelper, DbType> condition() {
        return (jdbcHelper, dbType) -> {
            try {
                if (!originalContentTypeColumExist(jdbcHelper, dbType)) {
                    log.info("Column " + COLUMN_NAME + " doesn't exist in table " + TABLE_NAME);
                    return true;
                }
            } catch (SQLException e) {
                log.error("An error occurred while querying the database");
                throw new RuntimeException("An error occurred while querying the database", e);
            }
            log.info("Column " + COLUMN_NAME + " already exist in table " + TABLE_NAME);
            return false;
        };
    }

    private boolean originalContentTypeColumExist(JdbcHelper jdbcHelper, DbType dbType) throws SQLException {
        return columnExists(jdbcHelper, dbType, TABLE_NAME, COLUMN_NAME);
    }

}
