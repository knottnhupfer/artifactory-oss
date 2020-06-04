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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.google.common.io.Files;
import org.jfrog.security.ssl.CertificateGenerationException;
import org.jfrog.security.ssl.CertificateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.SkipException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.x500.X500Principal;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.Thread.sleep;
import static org.testng.Assert.*;

/**
 * Helper methods for testing.
 *
 * @author Yossi Shaul
 */
public class TestUtils {

    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class[] paramTypes, Object[] params) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            Object result = method.invoke(null, params);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeStaticMethodNoArgs(Class<?> clazz, String methodName) {
        return invokeStaticMethod(clazz, methodName, null, null);
    }

    public static Object invokeMethodNoArgs(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(target);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object[] params) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    public static <T> T[] arrayOf(T... values) {
        return values;
    }

    /**
     * Set the {@link java.lang.reflect.Field field} with the given <code>name</code> on the
     * provided {@link Object target object} to the supplied <code>value</code>.
     * <p/>
     * Assumes the field is declared in the specified target class.
     *
     * @param target the target object on which to set the field
     * @param name   the name of the field to set
     * @param value  the value to set
     */
    public static void setField(Object target, String name, Object value) {
        try {
            Field field = findField(target.getClass(), name);
            if (field == null) {
                throw new IllegalArgumentException("Could not find field [" + name + "] on target [" + target + "]");
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the {@link java.lang.reflect.Field field} with the given <code>name</code> on the
     * provided {@link Object target object}.
     * <p/>
     * Assumes the field is declared in the specified target class.
     *
     * @param target the target object on which to set the field
     * @param name   the name of the field to get
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object target, String name, Class<T> type) {
        try {
            Field field = findField(target.getClass(), name);
            if (field == null) {
                throw new IllegalArgumentException("Could not find field [" + name + "] on target [" + target + "]");
            }
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with
     * the supplied <code>name</code>. Searches all
     * superclasses up to {@link Object}.
     *
     * @param clazz the class to introspect
     * @param name  the name of the field (may be <code>null</code> if type is specified)
     * @return the corresponding Field object, or <code>null</code> if not found
     */
    public static Field findField(Class<?> clazz, String name) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    public static void setLoggingLevel(Class clazz, Level level) {
        setLoggingLevel(clazz.getName(), level);
    }

    public static void setLoggingLevel(String name, Level level) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger(name).setLevel(level);
    }

    @Nonnull
    public static String extractHost(@Nullable String host) {
        return (host == null || "0.0.0.0".equals(host)) ? "127.0.0.1" : host;
    }

    public static void verifyErrorJson(String resString, int status, String msg) {
        JsonObject jsonObject = Json.parse(resString).asObject();
        JsonValue errors = jsonObject.get("errors");
        Assert.assertNotNull(errors);
        Assert.assertTrue(errors.isArray());
        JsonArray errorsArray = errors.asArray();
        Assert.assertEquals(errorsArray.size(), 1);
        JsonObject firstError = errorsArray.get(0).asObject();
        Assert.assertNotNull(firstError);
        Assert.assertEquals(firstError.get("status").asInt(), status);
        Assert.assertEquals(firstError.get("message").asString(), msg);
    }

    /**
     * Creates a temp directory.
     * This method tries to create the temp directory under the Maven target directory. Otherwise it will be under the
     * temp directory.
     *
     * @return Temp directory location
     */
    public static File createTempDir(Class clazz) {
        try {
            String classDir = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
            File targetDir = new File(classDir, "..");
            File testDir =
                    targetDir.exists() ? new File(targetDir.getCanonicalFile(),
                            "tests/test-" + System.currentTimeMillis() + "-" + new Random().nextInt()) :
                            Files.createTempDir();
            if (!testDir.exists()) {
                org.apache.commons.io.FileUtils.forceMkdir(testDir);
            }
            return testDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void waitForNextRoundSecond(long delta) {
        try {
            long now = System.currentTimeMillis();
            long requested = (now / 1000 + 1) * 1000 + delta;
            requested = requested < now ? (now / 1000 + 2) * 1000 + delta : requested;
            long sleepTime = Math.max(requested - now, 0);
            log.info("Waiting {}ms for next round second with delta of {}: {} (now: {})", sleepTime, delta, requested, now);
            sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void waitSince(long millis, long sinceTime) {
        long now = System.currentTimeMillis();
        try {
            sleep(Math.max(sinceTime + millis - now, 0));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void skipTest(@Nonnull String message) {
        throw new SkipException(message);
    }

    public static void assertMatches(String actual, String expectedRegex) {
        assertMatches(actual, expectedRegex, null);
    }

    public static void assertMatches(String actual, String expectedRegex, String message) {
        actual = actual == null ? "" : actual;
        Matcher matcher = Pattern.compile(expectedRegex, Pattern.DOTALL).matcher(actual);
        if (!matcher.matches()) {
            fail(formatFailMessage(message, actual, expectedRegex));
        }
    }

    /**
     * Asserts that the value is equals of bigger than <code>min</code> and higher or equals to <code>max</code>.
     *
     * @param value The value to check
     * @param min   Minimum value
     * @param max   Maximum value
     */
    public static void assertInRange(long value, long min, long max) {
        assertTrue(value >= min, "Value lower than minimum: value: " + value + " min: " + min);
        assertTrue(value <= max, "Value higher than maximum: value: " + value + " max: " + max);
    }

    /**
     * Assert that the given throwable is of the expected type or was caused by an exception of the expected type.
     * @param throwable the exception to check
     * @param expectedCause the expected exception type
     */
    public static void assertCausedBy(Throwable throwable, Class<? extends Throwable> expectedCause) {
        assertCausedBy(throwable, expectedCause, null);
    }

    /**
     * Assert that the given throwable is of the expected type or was caused by an exception of the expected type.
     * @param throwable the exception to check
     * @param expectedCause the expected exception type
     * @param expectedMessageRegex a regular expression of the expected message (or <code>null</code> to ignore the message)
     */
    public static void assertCausedBy(Throwable throwable, Class<? extends Throwable> expectedCause, String expectedMessageRegex) {
        expectedMessageRegex = expectedMessageRegex == null ? ".*" : expectedMessageRegex;
        if (expectedCause.equals(throwable.getClass())) {
            assertMatches(throwable.getMessage(), expectedMessageRegex, "Exception message does not match the expected pattern.");
            return;
        }
        Throwable cause = throwable.getCause();
        while (cause != null && cause != throwable) {
            if (expectedCause.equals(cause.getClass())) {
                assertMatches(cause.getMessage(), expectedMessageRegex, "Exception message does not match the expected pattern.");
                return;
            }
            throwable = cause;
            cause = throwable.getCause();
        }
        fail(formatFailMessage("Exception was not caused as expected", throwable.toString(), expectedCause.getName()));
    }

    private static String formatFailMessage(String message, Object actual, Object expected) {
        return (message == null ? "" : message + " ") + "expected [" + expected + "] but found [" + actual + "]";
    }

    public static <T> TimedResult<T> callWithTiming(Callable<T> callable) {
        long start = System.currentTimeMillis();
        T result;
        try {
            result = callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long timing = System.currentTimeMillis() - start;
        return new TimedResult<>(result, timing);
    }

    public static <T> T assertTiming(long duration, long deviation, Callable<T> callable) {
        TimedResult<T> timedResult = callWithTiming(callable);
        long min = duration - deviation;
        long max = duration + deviation;
        try {
            assertInRange(timedResult.getTime(), min, max);
        } catch (RuntimeException e) {
            fail("Timing not in the expected duration window of " + min + ".." + max + " : " + e.getMessage());
        }
        return timedResult.getResult();
    }

    public static void assertTiming(long duration, long deviation, Action action) {
        assertTiming(duration, deviation, () -> { action.call(); return null; });
    }

    public interface Action {
        void call() throws Exception;
    }

    public static class TimedResult<T> {
        private final T result;
        private final long time;

        public TimedResult(T result, long time) {
            this.result = result;
            this.time = time;
        }

        public T getResult() {
            return result;
        }

        public long getTime() {
            return time;
        }
    }

    public static Certificate createCertificate(KeyPair keyPair)
            throws CertificateGenerationException, CertificateEncodingException, CertificateException {
        X500Principal subject = new X500Principal("CN=test");
        X509Certificate x509Certificate = CertificateHelper.generateSignedCertificate(
                subject, keyPair.getPrivate(),
                subject, keyPair.getPublic(),
                BigInteger.ONE, 10000, true, 0);
        Certificate certificate = CertificateFactory.getInstance("X509")
                .generateCertificate(new ByteArrayInputStream(x509Certificate.getEncoded()));
        return certificate;
    }

    public static void assertArrayEquals(Object[] s1, Object[] s2) {
        if (Arrays.equals(s1, s2)) {
            return;
        }
        assertEquals(s1.length, s2.length, "array " + Arrays.toString(s1) +
                " not same length as " + Arrays.toString(s2));
        for (Object e1 : s1) {
            boolean found = false;
            for (Object e2 : s2) {
                if (e1.equals(e2)) {
                    found = true;
                }
            }
            assertTrue(found, "Element " + e1 + " not found in  array " + Arrays.toString(s2));
        }
    }

    public static void repeat(int count, Runnable action) {
        IntStream.range(0, count).forEach(i -> action.run());
    }
}
