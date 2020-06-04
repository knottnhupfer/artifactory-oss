package org.artifactory.storage.db.validators;

/**
 * @author barh
 */
class CollationException extends RuntimeException {
    CollationException(String cause) {
        super(cause);
    }
}
