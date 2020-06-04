package org.artifactory.storage.db.validators;

import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.common.ResourceUtils;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author barh
 */
public class OracleValidator extends CollationValidator {

    private static final Logger log = LoggerFactory.getLogger(OracleValidator.class);
    private JdbcHelper jdbcHelper;

    OracleValidator(JdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }


    @Override
    public boolean isValidCollation() {
        ResultSet resultSet = null;
        try {
            log.info("Validating session collation for oracle database");
            resultSet = jdbcHelper
                    .executeSelect(ResourceUtils.getResourceAsString(CollationQuery.ORACLE));
            resultSet.next();
            if (resultSet.getString(1).equals("Insensitive")) {
                log.error("SESSION BAD COLLATION -> {}", resultSet.getString(1));
                return false;
            }
        } catch (SQLException e) {
            log.warn("Validating session collation for oracle database failed");
        }
        finally {
            DbUtils.close(resultSet);
        }
        return true;
    }
}
