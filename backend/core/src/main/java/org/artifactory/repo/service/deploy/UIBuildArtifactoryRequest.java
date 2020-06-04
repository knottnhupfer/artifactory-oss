package org.artifactory.repo.service.deploy;

import org.apache.commons.io.IOUtils;
import org.artifactory.api.build.request.BuildArtifactoryRequest;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * BE EXTREMELY CAREFUL
 *  * It is a necessity of the build-info repo to buffer the entire content of a build json - whatever {@param inputStream}
 *  * you initialize this class with will be read into memory in its entirety!
 *
 * @author Dan Feldman
 */
public class UIBuildArtifactoryRequest extends ArtifactoryDeployRequest implements BuildArtifactoryRequest {
    private static final Logger log = LoggerFactory.getLogger(UIBuildArtifactoryRequest.class);

    private byte[] content;

    UIBuildArtifactoryRequest(RepoPath pathToUpload, InputStream inputStream, long contentLength, long lastModified,
            Properties properties, boolean trustServerChecksums) {
        super(pathToUpload, null, contentLength, lastModified, properties, trustServerChecksums);
        try {
            content = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("Failed to retrieve build json content from stream: ", e);
            content = new byte[0];
        }
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void replaceContent(byte[] newContent) {
        content = newContent;
    }

    @Override
    public void setRepoPath(RepoPath repoPath) {
        super.setRepoPath(repoPath);
    }
}
