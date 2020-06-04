package org.artifactory.request;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.output.NullWriter;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.artifactory.common.ConstantValues;
import org.artifactory.util.HttpUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;

/**
 * A Docker specific manifest response which holds the OutputStream and some of the basic headers for later use.
 * When going through the {@link org.artifactory.api.request.DownloadService} the download traffic is counted.
 *
 * Docker specification is expressed by returning specific headers back to the client, pay attention to not change
 * this behaviour.
 *
 * @author Inbar Tal
 */
public class DockerManifestResponse extends ArtifactoryResponseBase {
    private static final Logger log = LoggerFactory.getLogger(DockerManifestResponse.class);

    private ByteArrayOutputStream out;
    private Multimap<String, Object> headers = HashMultimap.create();
    private String statusMessage;

    @Override
    public OutputStream getOutputStream() {
        if (out == null) {
            out = new ByteArrayOutputStream();
        }
        return out;
    }

    @Override
    public void sendErrorInternal(int statusCode, String reason) {
        setStatus(statusCode);
        statusMessage = reason;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Returns the byte array representing the response.
     *
     * @return The response result as a stream.
     */
    public byte[] getResultAsByteArray() {
        if (out != null) {
            return out.toByteArray();
        }
        return null;
    }

    public Multimap<String, Object> getHeaders() {
        return headers;
    }

    @Override
    public void setLastModified(long lastModified) {
        headers.put(HttpHeaders.LAST_MODIFIED, new Date(lastModified));
    }

    @Override
    public void setContentLength(long length) {
        super.setContentLength(length);
        headers.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(length));
    }

    @Override
    public void setContentType(String contentType) {
        // Do not implement this method, we set the content type later on in the flow (after the schema conversion if needed)
    }

    @Override
    public Writer getWriter() throws IOException {
        return new NullWriter();
    }

    @Override
    public void setHeader(String header, String value) {
        // nope
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void flush() {
        // nope
    }

    @Override
    public void sendAuthorizationRequired(String message, String realm) throws IOException {
        super.sendError(HttpStatus.SC_FORBIDDEN, message, log);
    }

    @Override
    public void setEtag(String etag) {
        if (etag != null) {
            headers.put(HttpHeaders.ETAG, etag);
        } else {
            log.trace("Could not register a null etag with the response.");
        }
    }

    @Override
    public void setSha1(String sha1) {
        if (sha1 != null) {
            headers.put(ArtifactoryRequest.CHECKSUM_SHA1, sha1);
        } else {
            log.trace("Could not register a null sha1 tag with the response.");
        }
    }

    @Override
    public void setSha2(String sha2) {
        if (sha2 != null) {
            headers.put(ArtifactoryRequest.CHECKSUM_SHA256, sha2);
        } else {
            log.trace("Could not register a null sha256 tag with the response.");
        }
    }

    @Override
    public void setMd5(String md5) {
        if (md5 != null) {
            headers.put(ArtifactoryRequest.CHECKSUM_MD5, md5);
        } else {
            log.trace("Could not register a null md5 tag with the response.");
        }
    }

    @Override
    public void setRangeSupport(String rangeSupport) {
        if (rangeSupport != null) {
            headers.put(ArtifactoryRequest.ACCEPT_RANGES, rangeSupport);
        } else {
            log.trace("Could not register a null range support tag with the response.");
        }
    }

    @Override
    public void setFilename(String filename) {
        if (StringUtils.isNotBlank(filename)) {
            headers.put(ArtifactoryRequest.FILE_NAME, HttpUtils.encodeQuery(filename));
        } else {
            log.trace("Could not register a null filename with the response.");
        }
    }

    @Override
    public void setContentDispositionAttachment(String filename) {
        if (ConstantValues.responseDisableContentDispositionFilename.getBoolean() || StringUtils.isBlank(filename)) {
            headers.put("Content-Disposition", "attachment");
        } else {
            headers.put("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        }
    }
}
