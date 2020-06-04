package org.artifactory.storage.db.validators;

import ch.qos.logback.core.db.dialect.DBUtil;
import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.common.ResourceUtils;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author barh
 * This validator matches collations for the following db types : MSSQL, MYSQL, MARIADB.
 */
public class GeneralValidator extends CollationValidator {

    private JdbcHelper jdbcHelper;
    private static final Logger log = LoggerFactory.getLogger(GeneralValidator.class);
    private String queryPath;

    GeneralValidator(JdbcHelper jdbcHelper, String queryResourcePath) {
        this.jdbcHelper = jdbcHelper;
        this.queryPath = queryResourcePath;
    }

    @Override
    public boolean isValidCollation() {
        ResultSet resultSet = null;
        try {
            log.info("Validating scheme collation for database");
            resultSet = jdbcHelper
                    .executeSelect(ResourceUtils.getResourceAsString(queryPath));
            resultSet.next();
            String collation = StringUtils.lowerCase(resultSet.getString(1));
            return isValidCollation(collation);
        } catch (SQLException e) {
            log.warn("Validating database scheme collation failed");
        } finally {
            DbUtils.close(resultSet);
        }
        return true;
    }
}
