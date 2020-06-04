package org.artifactory.storage.db.conversion.version.v213;

import org.artifactory.common.ConstantValues;
import org.artifactory.storage.db.conversion.ConversionPredicate;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiPredicate;

/**
 * RTFACT-17784 postgres can't create index on values longer than specific size
 *
 * @author Yoaz Menda
 */
public class V213ConversionPredicate implements ConversionPredicate {

    private static final Logger log = LoggerFactory.getLogger(V213ConversionPredicate.class);

    @Override
    public BiPredicate<JdbcHelper, DbType> condition() {
        return (jdbcHelper, dbType) -> {
            // check if postgres because other databases didn't cause any problems in this conversion
            if (!dbType.equals(DbType.POSTGRESQL)) {
                return true;
            }
            // check if has props that are too long - if yes, don't perform conversion, just notify user
            try {
                if (!hasPropsLongerThanMaxAllowed(jdbcHelper)) {
                    return true;
                }
            } catch (SQLException e) {
                log.error("An error occurred while querying the database. conversion will be skipped");
                throw new RuntimeException("An error occurred while querying the database. conversion will be skipped", e);
            }
            log.error("Artifactory has properties with value longer than max allowed");
            throw new RuntimeException("Artifactory has properties with value longer than max allowed.  Conversion will be skipped");
        };
    }

    private boolean hasPropsLongerThanMaxAllowed(JdbcHelper jdbcHelper) throws SQLException {
        int maxPostgresPropValueLength = ConstantValues.dbPostgresPropertyValueMaxSize.getInt();
        String propsLongerThanQuery = "select prop_id from node_props where length(prop_value) > ? limit 1";
        try (ResultSet resultSet = jdbcHelper.executeSelect(propsLongerThanQuery, maxPostgresPropValueLength)) {
            return resultSet.next();
        }
    }

}
