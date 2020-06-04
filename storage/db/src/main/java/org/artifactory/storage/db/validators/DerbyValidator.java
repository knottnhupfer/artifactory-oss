package org.artifactory.storage.db.validators;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author barh
 */
public class DerbyValidator extends CollationValidator {
    private ArtifactoryDbProperties props;
    private List<String> whiteList;
    private static final Logger log = LoggerFactory.getLogger(DerbyValidator.class);

    DerbyValidator(ArtifactoryDbProperties props) {
        this.props = props;
        whiteList = Arrays.asList("UCS_BASIC", "TERRITORY_BASED:IDENTICAL");
    }

    @Override
    public boolean isValidCollation() {
        log.info("Validating connection collation for derby database");
        String collation = extractCollationFromUrl(props.getConnectionUrl());
        if (collation != null && whiteList.stream().noneMatch(c -> c.equals(collation))) {
            log.error("DATABASE SCHEME BAD COLLATION -> {}", collation);
            return false;
        }
        return true;
    }

    private String extractCollationFromUrl(String url) {
        if (url != null) {
            String[] split = url.split(";");
            for (String param : split) {
                String upperParam = StringUtils.upperCase(param);
                if (upperParam.contains("COLLATION")) {
                    return upperParam.substring(upperParam.indexOf('=') + 1);
                }
            }
        }
        return null;
    }

}
