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

package org.artifactory.test;

import org.testng.annotations.Test;

import javax.annotation.Nonnull;

import static org.testng.AssertJUnit.fail;

/**
 * @author Saffi Hartal
 */
@Test
public class CheckCompilationSettings {

    @Test(expectedExceptions = NullPointerException.class)
    public void annotationsTurnedOff(){
        try {
            throwNullPointerException(null);
        } catch (IllegalArgumentException e){
            fail("Please configure intllij - 1. Preferences -> Build, Execution, Deployment -> Compiler -> Java Compiler -> Additional command line parameters: -parameters\n" +
                    "\n" +
                    "2. Preferences -> Build, Execution, Deployment -> Compiler -> Uncheck Add runtime assertions...\n" +
                    "\n" +
                    "3. Preferences -> Build, Execution, Deployment -> Compiler -> Annotation Processors -> Uncheck Enabled annotation processing for Default\n" +
                    "\n" +
                    "4. Preferences -> Plugins -> Make sure lombok plugin installed\n" +
                    "\n");
        }
    }

    private void throwNullPointerException(@Nonnull Object o) {
        o.hashCode();
    }
}
