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

package org.artifactory.util;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

/**
 * Tests the ExceptionUtils.
 *
 * @author Yossi Shaul
 */
@Test
public class ExceptionUtilsTest {

    public void testGetCauseOfType() {
        IOException ioException = new IOException();
        IllegalArgumentException e = new IllegalArgumentException((new RuntimeException(ioException)));
        Throwable ioCause = ExceptionUtils.getCauseOfType(e, IOException.class);
        assertSame(ioCause, ioException, "Should return the same wrapped io exception");
        Throwable notFound = ExceptionUtils.getCauseOfType(e, IllegalStateException.class);
        assertNull(notFound, "Should not have found this type of exception");
    }

    public void testGetRootCauseNotNested() {
        IOException ioException = new IOException();
        Throwable rootCause = ExceptionUtils.getRootCause(ioException);
        assertSame(rootCause, ioException, "Should return the same io exception");
    }

    public void testGetRootCauseNested() {
        IOException ioe = new IOException();
        Exception exception = new Exception(ioe);
        Throwable rootCause = ExceptionUtils.getRootCause(exception);
        assertSame(rootCause, ioe, "Should return the io exception");
    }

    public void toRuntimeExceptionOfAlreadyRuntimeException() {
        IllegalArgumentException alreadyRuntimeException = new IllegalArgumentException();
        RuntimeException result = ExceptionUtils.toRuntimeException(alreadyRuntimeException);
        assertSame(result, alreadyRuntimeException);
    }

    public void toRuntimeExceptionOfNotRuntimeException() {
        Exception exception = new Exception();
        RuntimeException result = ExceptionUtils.toRuntimeException(exception);
        assertNotSame(result, exception);
        assertSame(result.getCause(), exception);

    }

}
