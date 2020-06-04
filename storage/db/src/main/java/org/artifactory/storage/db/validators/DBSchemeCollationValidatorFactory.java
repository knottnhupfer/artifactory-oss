package org.artifactory.storage.db.validators;

import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.util.JdbcHelper;

/**
 * @author barh
 */
public class DBSchemeCollationValidatorFactory {

    //POSTGRESQL has explicit case for clarification
    public static CollationValidator create(ArtifactoryDbProperties dbProperties, JdbcHelper jdbcHelper) {
        switch (dbProperties.getDbType()) {
            case MYSQL:
                return new GeneralValidator(jdbcHelper, CollationQuery.MYSQL);
            case MARIADB:
                return new GeneralValidator(jdbcHelper, CollationQuery.MARIA_DB);
            case MSSQL:
                return new MssqlValidator(dbProperties, jdbcHelper, CollationQuery.MSSQL);
            case DERBY:
                return new DerbyValidator(dbProperties);
            case ORACLE:
                return new OracleValidator(jdbcHelper);
            case POSTGRESQL:
                return new DummyValidator();
            default:
                return new DummyValidator();
        }
    }
}
