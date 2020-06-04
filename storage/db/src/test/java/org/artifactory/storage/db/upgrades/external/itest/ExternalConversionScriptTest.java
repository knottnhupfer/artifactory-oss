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

package org.artifactory.storage.db.upgrades.external.itest;

import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This test makes the {@link org.artifactory.storage.db.version.converter.DbSqlConverterUtil} invoke an external
 * conversion script.
 *
 * @author Uriah Levy
 */
@Test
public class ExternalConversionScriptTest extends UpgradeBaseTest {

    public void testExternalConversionScriptRan() throws Exception {
        // This creates an alternative derby_v570.sql file that creates a dummy column
        prepareTestInstanceForExternalConversion(false);
        // Expect our dummy column to exist
        assertTrue(columnExists("users", "foo"));
    }

    public void testExternalEmptyConversionScriptRan() throws Exception {
        // This creates an alternative derby_v570.sql file that creates a dummy column
        prepareTestInstanceForExternalConversion(true);
        // Expect our dummy column to not exist
        assertFalse(columnExists("users", "foo"));
    }
}
