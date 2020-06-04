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

package org.artifactory.repo.webdav.methods;

import junit.framework.Assert;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.common.StatusHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author nadavy
 */
@Test
public class LockMethodXxeTest {

    @Test
    public void testLockMethod() throws IOException {
        LockMethod lockMethod = new LockMethod();
        String useXxe = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<!DOCTYPE updateProfile [ " +
                " <!ELEMENT updateProfile (#PCDATA)> " +
                " <!ENTITY start \"&lt;![CDATA[\">" +
                " <!ENTITY file SYSTEM \"\">" +
                " <!ENTITY end \"]]&gt;\">" +
                " <!ENTITY all \"&start;&file;&end;\">" +
                " ]>" +
                "<updateProfile>" +
                " <owner><href>&all;</href></owner>" +
                "</updateProfile>";
        request.getParameter(useXxe);
        lockMethod.handle(request, response);
        Assert.assertFalse(response.isSuccessful());

        response.setStatus(1); // reset status
        String dontUseXxe = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<updateProfile>\n" +
                " <owner><href>A</href></owner>\n" +
                "</updateProfile>  ";
        request.getParameter(dontUseXxe);
        lockMethod.handle(request, response);
        Assert.assertTrue(response.isSuccessful());
    }

    private ArtifactoryResponse response = new ArtifactoryResponse() {
        private boolean success = true;

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public void setLastModified(long lastModified) {

        }

        @Override
        public void setEtag(String etag) {

        }

        @Override
        public void setSha1(String sha1) {

        }

        @Override
        public void setSha2(String sha2) {

        }

        @Override
        public void setMd5(String md5) {

        }

        @Override
        public void setRangeSupport(String bytes) {

        }

        @Override
        public long getContentLength() {
            return 1;
        }

        @Override
        public void setContentLength(long length) {

        }

        @Override
        public void setContentType(String contentType) {

        }

        @Override
        public Writer getWriter() throws IOException {
            return null;
        }

        @Override
        public void sendInternalError(Exception exception, Logger logger) throws IOException {

        }

        @Override
        public void sendError(int statusCode, String reason, Logger logger) throws IOException {
            success = false;
        }

        @Override
        public void sendError(StatusHolder statusHolder) throws IOException {
            success = false;
        }

        @Override
        public void sendStream(InputStream is) throws IOException {

        }

        @Override
        public void sendSuccess() {
        }

        @Override
        public int getStatus() {
            return 0;
        }

        @Override
        public void setStatus(int statusCode) {
            if (statusCode == 1) {
                success = true;
            }
        }

        @Override
        public void setHeader(String header, String value) {

        }

        @Override
        public void setRedirect(String url) {

        }

        @Override
        public boolean isCommitted() {
            return false;
        }

        @Override
        public boolean isSuccessful() {
            return success;
        }

        @Override
        public void flush() {

        }

        @Override
        public void sendAuthorizationRequired(String message, String realm) throws IOException {

        }

        @Override
        public boolean isPropertiesQuery() {
            return false;
        }

        @Override
        public String getPropertiesMediaType() {
            return null;
        }

        @Override
        public void setPropertiesMediaType(String propsQueryFormat) {

        }

        @Override
        public void close(Closeable closeable) {

        }
    };

    private ArtifactoryRequest request = new ArtifactoryRequest() {
        String inputString;

        @Override
        public String getRepoKey() {
            return null;
        }

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public boolean isMetadata() {
            return false;
        }

        @Override
        public boolean isRecursive() {
            return false;
        }

        @Override
        public long getModificationTime() {
            return 0;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean isDirectoryRequest() {
            return false;
        }

        @Override
        public RepoPath getRepoPath() {
            return null;
        }

        @Override
        public boolean isChecksum() {
            return false;
        }

        @Override
        public boolean isFromAnotherArtifactory() {
            return false;
        }

        @Override
        public boolean isHeadOnly() {
            return false;
        }

        @Override
        public long getLastModified() {
            return 0;
        }

        @Override
        public long getIfModifiedSince() {
            return 0;
        }

        @Override
        public boolean hasIfModifiedSince() {
            return false;
        }

        @Override
        public boolean isNewerThan(long time) {
            return false;
        }

        @Override
        public Enumeration<String> getHeadersKeys() {
            return null;
        }

        @Override
        public Enumeration<String> getHeaderValues(@Nonnull String headerName) {
            return null;
        }

        @Override
        public String getServletContextUrl() {
            return null;
        }

        @Override
        public String getUri() {
            return null;
        }

        @Override
        public Properties getProperties() {
            return null;
        }

        @Override
        public boolean hasProperties() {
            return false;
        }

        @Override
        public Map<String, String[]> getParameters() {
            return null;
        }

        @Override
        public String getParameter(String name) {
            this.inputString = name;
            return null;
        }

        @Override
        public String[] getParameterValues(String name) {
            return new String[0];
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(inputString.getBytes());
        }

        @Override
        public long getContentLength() {
            return 1;
        }

        @Override
        public String getClientAddress() {
            return null;
        }

        @Override
        public String getZipResourcePath() {
            return null;
        }

        @Override
        public boolean isZipResourceRequest() {
            return false;
        }

        @Override
        public boolean isNoneMatch(String etag) {
            return false;
        }

        @Override
        public boolean hasIfNoneMatch() {
            return false;
        }
    };
}
