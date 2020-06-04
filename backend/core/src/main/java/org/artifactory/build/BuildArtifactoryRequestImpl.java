package org.artifactory.build;

import org.apache.commons.io.IOUtils;
import org.artifactory.api.build.request.BuildArtifactoryRequest;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.servlet.HttpArtifactoryRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * BE EXTREMELY CAREFUL
 * It is a necessity of the build-info repo to buffer the entire content of a build json - whatever {@param httpRequest}
 * you initialize this class with will be read into memory in its entirety!
 *
 * @author Yuval Reches
 * @author Gidi Shabat
 */
public class BuildArtifactoryRequestImpl extends HttpArtifactoryRequest implements BuildArtifactoryRequest {

    private byte[] content;

    public BuildArtifactoryRequestImpl(HttpServletRequest httpRequest) throws IOException {
        super(httpRequest);
        content = IOUtils.toByteArray(httpRequest.getInputStream());
    }

    @Override
    public InputStream getInputStream() throws IOException {
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
