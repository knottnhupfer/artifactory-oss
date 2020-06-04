package org.artifactory.storage.db.validators;

import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author barh
 */
public abstract class CollationValidator {

    private static final Logger log = LoggerFactory.getLogger(CollationValidator.class);
    private static final Pattern pattern = Pattern.compile("(_cs|_bin)");

    protected abstract boolean isValidCollation();

    public void validate() {
        if (!isValidCollation() && ConstantValues.shutDownOnInvalidDBScheme.getBoolean()) {
            log.error("Shutting down artifactory due to incorrect database collation");
            throw new CollationException("incorrect database collation");
        }
    }

    boolean isValidCollation(String collation) {
        if (collation != null) {
            Matcher matcher = pattern.matcher(collation);
            if (!matcher.find()) {
                log.error("The database is configured with case insensitive collation: {}. We strongly recommend using case sensitive collation.", collation);
                return false;
            }
        }
        return true;
    }
}