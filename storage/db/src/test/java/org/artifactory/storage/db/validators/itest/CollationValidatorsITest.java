package org.artifactory.storage.db.validators.itest;

import org.artifactory.common.ConstantValues;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.validators.DBSchemeCollationValidatorFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class CollationValidatorsITest extends DbBaseTest {

    @BeforeMethod
    void changeShutDownProp() {
        artifactoryHomeBoundTest.setStringSystemProperty(ConstantValues.shutDownOnInvalidDBScheme, "true");
    }

    @Test
    void testValidator() {
        DBSchemeCollationValidatorFactory.create(dbProperties, jdbcHelper).validate();
    }
}
