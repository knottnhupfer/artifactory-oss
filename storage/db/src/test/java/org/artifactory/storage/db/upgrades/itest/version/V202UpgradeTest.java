/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.conversion.version.v202.V202ConversionPredicate;
import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.artifactory.storage.db.version.converter.ConditionalDBSqlConverter;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test DB version v202 (art version v531)
 * This conversion runs conditionally for upgrades from version < 4.4.1 up to 5.3.0 * that had their v108
 * (art version v441 -> v4141) conversion messed up.
 *
 * @author Dan Feldman
 */
@Test
public class V202UpgradeTest extends UpgradeBaseTest {

    //Conversion from <= 4.4.1 to 5.3.0 omitted the v441 conversion (i.e. v108 db version)
    //To simulate this all conversions are run here up to 5.3.0 without 4.4.1 and then 5.3.1 (db v202) is run
    public void testConversionFromV441ToV530() throws IOException, SQLException {
        rollBackTo300Version();
        //now at db v100 run all conversions up to v201 omitting v108
        executeSqlStream(getDbSchemaUpgradeSql("v310", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v311", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v410", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v420", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v440", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v500", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v500a", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v530", dbProperties.getDbType()));

        //v441 (db v108) should be missing
        assertFalse(columnExists("users", "credentials_expired"));
        //run conditional conversion v202
        ConditionalDBSqlConverter v202 = new ConditionalDBSqlConverter("v441", new V202ConversionPredicate());
        v202.convert(jdbcHelper, dbProperties.getDbType());
        //v108 should have been run
        assertTrue(columnExists("users", "credentials_expired"));
    }

    //Tests that the conditional conversion will not run if not needed (upgrading to 5.3.1 from anything later then 4.4.1)
    public void testConversionFromV4142ToV530() throws IOException, SQLException {
        rollBackTo300Version();
        //now at db v100 run all conversions up to v201 omitting v108
        executeSqlStream(getDbSchemaUpgradeSql("v310", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v311", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v410", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v420", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v440", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v441", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v500", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v500a", dbProperties.getDbType()));
        executeSqlStream(getDbSchemaUpgradeSql("v530", dbProperties.getDbType()));

        //v441 (db v108) should have run ok
        assertTrue(columnExists("users", "credentials_expired"));
        //run conditional conversion v202
        try {
            ConditionalDBSqlConverter v202 = new ConditionalDBSqlConverter("v441", new V202ConversionPredicate());
            v202.convert(jdbcHelper, dbProperties.getDbType());
        } catch (Exception e) {
            throw new RuntimeException("Conditional db V202 conversion should not have run: " + e.getMessage(), e);
        }
    }
}
