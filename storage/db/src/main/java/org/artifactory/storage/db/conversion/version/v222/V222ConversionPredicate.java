package org.artifactory.storage.db.conversion.version.v222;

import lombok.NonNull;
import org.artifactory.storage.db.conversion.ConversionPredicate;
import org.artifactory.storage.db.conversion.DbConversionException;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiPredicate;

import static org.jfrog.storage.util.DbUtils.getColumnSize;

/**
 * @author AndreiK.
 */
public class V222ConversionPredicate implements ConversionPredicate {

    private static final Logger log = LoggerFactory.getLogger(V222ConversionPredicate.class);

    @NonNull
    @Override
    public BiPredicate<JdbcHelper, DbType> condition() {
        return (jdbcHelper, dbType) -> {
            try {
                // run conversion if column version in artifact_bundles is not equals to 32
                if (getColumnSize(jdbcHelper, dbType, "artifact_bundles", "version") != 32) {
                    log.info(
                            "Version column in artifact_bundles table is not 32 character length - Running conversion.");
                    return true;
                }
            } catch (Exception e) {
                log.error("Cannot run conversion 'v222' - Failed to retrieve schema metadata");
                throw new DbConversionException("Cannot run conversion 'v222' - Failed to retrieve schema metadata: " + e.getMessage(), e);
            }
            log.info(
                    "Version column in artifact_bundles table is already 32 character length.");
            return false;
        };
    }
}
