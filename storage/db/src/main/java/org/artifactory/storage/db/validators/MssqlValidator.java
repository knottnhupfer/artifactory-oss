package org.artifactory.storage.db.validators;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.jfrog.common.ResourceUtils;
import org.jfrog.storage.JdbcHelper;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author barh
 */
public class MssqlValidator extends CollationValidator {
    private ArtifactoryDbProperties props;
    private JdbcHelper jdbcHelper;
    private String queryResourcePath;
    private static final Logger log = LoggerFactory.getLogger(MssqlValidator.class);

    MssqlValidator(ArtifactoryDbProperties props, JdbcHelper jdbcHelper, String queryResourcePath) {
        this.props = props;
        this.jdbcHelper = jdbcHelper;
        this.queryResourcePath = queryResourcePath;
    }

    @Override
    public boolean isValidCollation() {
        log.info("Validating scheme collation for mssql database");
        String dbname = extractDbNameFromUrl(props.getConnectionUrl());
        log.debug("Database name: {}", dbname);
        if(dbname != null) {
            String collation = extractCollation(dbname);
            log.debug("Collation is : {}", collation);
            return isValidCollation(collation);
        }
        return true;
    }

    private String extractCollation(String dbname) {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper
                    .executeSelect(ResourceUtils.getResourceAsString(queryResourcePath), dbname);
            resultSet.next();
            return StringUtils.lowerCase(resultSet.getString(1));
        } catch (SQLException e) {
            log.warn("Validating database scheme collation failed");
        } finally {
            DbUtils.close(resultSet);
        }
        return null;
    }

    private String extractDbNameFromUrl(String url) {
        if (url != null) {
            String[] split = url.split(";");
            for (String param : split) {
                String upperParam = StringUtils.upperCase(param);
                if (upperParam.contains("DATABASENAME")) {
                    return param.substring(param.indexOf('=') + 1);
                }
            }
        }
        return null;
    }

}
