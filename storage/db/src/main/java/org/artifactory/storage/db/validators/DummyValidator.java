package org.artifactory.storage.db.validators;

/**
 * @author barh
 */
public class DummyValidator extends CollationValidator {

    @Override
    public boolean isValidCollation() {
        return true;
    }
}
