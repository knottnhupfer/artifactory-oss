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

package org.artifactory.request;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.artifactory.common.ArtifactoryConfigurationAdapter;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.request.range.RangeAwareContext;
import org.jfrog.config.ConfigurationManager;
import org.jfrog.config.wrappers.ConfigurationManagerImpl;
import org.jfrog.storage.binstore.ifc.SkippableInputStream;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.artifactory.request.range.ResponseWithRangeSupportHelper.createRangeAwareContext;

/**
 * @author Gidi Shabat
 */
@Test
public class ResponseWithHttpRangeSupportHelperTest {
    private byte[] content;

    @BeforeClass
    public void init() {
        ArtifactoryHome home = new ArtifactoryHome(new File("./target/test/DebianEventsTest"));
        ConfigurationManager configurationManager = ConfigurationManagerImpl.create(new ArtifactoryConfigurationAdapter(home));
        configurationManager.initDbProperties();
        configurationManager.initDefaultFiles();
        home.initPropertiesAndReload();
        ArtifactoryHome.bind(home);
        content = "0123456789ABCDEFGI".getBytes();
    }

    @Test
    public void noRangeRequest() throws IOException {
        testDummyRangesReturnAll(null, true);
        testDummyRangesReturnAll(null, false);
    }

    @Test
    public void emptyRangeRequest() throws IOException {
        String range = "";
        testDummyRangesReturnAll(range, true);
        testDummyRangesReturnAll(range, false);
    }

    @Test
    public void invalidRangeRequest() throws IOException {
        String range = "ranges:a-b";
        testDummyRangesReturnAll(range, true);
        testDummyRangesReturnAll(range, false);
    }

    @Test
    public void singleRangeRequest() throws IOException {
        for (int first = 0; first < content.length - 1; first++) {
            for (int last = first; last < content.length; last++) {
                String range = "byte=" + first + "-" + last;
                testSingleRangeFirstLast(range, first, last);
            }
            // Test with start only range
            String range = "byte=" + first + "-";
            testSingleRangeFirstLast(range, first, content.length - 1);
        }
    }

    @Test
    public void rangeEndsHigherThanContentLength() throws IOException {
        int start = 0;
        int end = content.length + 100;
        String range = "byte=" + start + "-" + end;
        testSingleRangeFirstLast(range, start, end);
    }

    @Test
    public void rangeStartsHigherThanContentLength() throws IOException {
        int start = content.length + 100;
        int end = start + 100;
        String range = "byte=" + start + "-" + end;
        InputStream inputStream = createTestInputStream(true);
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        Assert.assertTrue(context.getContentLength() == 0);
        Assert.assertTrue(context.getStatus() == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
        Assert.assertTrue(context.getContentRange().equals("bytes */"+content.length));
    }

    @Test
    public void multiRangeRequest() throws IOException {
        String range = "bytes=1-3,7-10";
        testMultiRangeRequest(range, true);
        testMultiRangeRequest(range, false);
    }

    @Test
    public void multiRangeDescendingOrderRequest() throws IOException {
        String range = "bytes=7-10,1-3";
        testMultiRangeRequest(range, true);
        testMultiRangeRequest(range, false);
    }

    @Test
    public void multiWithPartialErrorRangeDescendingOrderRequest() throws IOException {
        String range = "bytes=7-10,12-a,1-3";
        testMultiRangeRequest(range, true);
        testMultiRangeRequest(range, false);
    }

    @Test
    public void multiWithMergeRangeDescendingOrderRequest() throws IOException {
        String range = "bytes=7-10,1-6";
        testSingleRangeFirstLast(range, 1, 10);
    }

    private void assertSameContentNoStatus(RangeAwareContext context, String suffixMsg) throws IOException {
        // Assert content length
        Assert.assertEquals(content.length, context.getContentLength(), "Wrong content length for " + suffixMsg);
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        Assert.assertEquals(resultContent, content, "Expecting no change in the content for " + suffixMsg);
        //Assert status
        Assert.assertEquals(context.getStatus(), -1, "Expecting no status for " + suffixMsg);
        // Assert content type
        Assert.assertEquals(context.getContentType(), "pdf",
                "Expecting no change in the content type for " + suffixMsg);
        // Assert content range
        Assert.assertEquals(context.getContentRange(), null, "Expecting no content range for " + suffixMsg);
    }

    private void testDummyRangesReturnAll(String range, boolean skippable) throws IOException {
        InputStream inputStream = createTestInputStream(skippable);
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        assertSameContentNoStatus(context, "range " + range + " skippable " + skippable);
    }

    private void testSingleRangeFirstLast(String range, int expectedFirst, int expectedLast) throws IOException {
        int expectedContentLength = Math.min(expectedLast - expectedFirst + 1, content.length);
        byte[] expectedBytes = new byte[expectedContentLength];
        System.arraycopy(content, expectedFirst, expectedBytes, 0, expectedContentLength);
        int expectedStatus = 206;
        String expectedContentRange =
                "bytes " + expectedFirst + "-" + Math.min(content.length - 1, expectedLast) + "/" + content.length;

        internalTestSimpleRange(true, range, expectedContentLength, expectedBytes, expectedStatus,
                expectedContentRange);
        internalTestSimpleRange(false, range, expectedContentLength, expectedBytes, expectedStatus,
                expectedContentRange);
    }

    private void internalTestSimpleRange(boolean skippable,
            String range,
            int expectedContentLength,
            byte[] expectedBytes,
            int expectedStatus,
            String expectedContentRange) throws IOException {
        InputStream inputStream = createTestInputStream(skippable);
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(expectedContentLength, context.getContentLength(), "Wrong Length");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        Assert.assertEquals(resultContent, expectedBytes, "Wrong content for " + range + " skippable " + skippable);
        //Assert status
        Assert.assertEquals(context.getStatus(), expectedStatus,
                "Wrong status for " + range + " skippable " + skippable);
        // Assert content type
        Assert.assertEquals(context.getContentType(), "pdf",
                "Expecting no change in the content type for " + range + " skippable " + skippable);
        // Assert content range
        Assert.assertEquals(context.getContentRange(), expectedContentRange,
                "Wrong Content Range for " + range + " skippable " + skippable);
    }

    private void testMultiRangeRequest(String range, boolean skippable) throws IOException {
        InputStream inputStream = createTestInputStream(skippable);
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        String suffixMsg = "range " + range + " skippable " + skippable;
        // Assert content length
        Assert.assertEquals(context.getContentLength(), 196, "Wrong content length for " + suffixMsg);
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        String expectedResult = "--BCD64322345343217845286A\r\n" +
                "Content-Type: pdf\r\n" +
                "Content-Range: bytes 1-3/18\r\n" +
                "\r\n" +
                "123\r\n" +
                "--BCD64322345343217845286A\r\n" +
                "Content-Type: pdf\r\n" +
                "Content-Range: bytes 7-10/18\r\n" +
                "\r\n" +
                "789A--BCD64322345343217845286A--\r\n";
        Assert.assertEquals(resultContent, expectedResult.getBytes(), "Wrong content for " + suffixMsg);
        //Assert status
        Assert.assertEquals(context.getStatus(), 206, "Wrong status for " + suffixMsg);
        // Assert content type
        Assert.assertEquals(context.getContentType(), "multipart/byteranges; boundary=BCD64322345343217845286A",
                "Wrong content type for " + suffixMsg);
        // Assert content range
        Assert.assertEquals(context.getContentRange(), null, "Wrong range for " + suffixMsg);
    }

    private InputStream createTestInputStream(boolean skippable) {
        InputStream inputStream;
        if (skippable) {
            inputStream = new ByteArrayInputStream(content);
        } else {
            inputStream = new NonSkippableStream(content);
        }
        return inputStream;
    }

    static class NonSkippableStream extends ByteArrayInputStream implements SkippableInputStream {
        NonSkippableStream(byte[] buf) {
            super(buf);
        }

        @Override
        public synchronized long skip(long n) {
            throw new RuntimeException("I told you not to call me!");
        }

        @Override
        public boolean isSkippable() {
            return false;
        }
    }

}
