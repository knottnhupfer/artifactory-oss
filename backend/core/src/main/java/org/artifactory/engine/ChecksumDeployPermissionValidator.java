package org.artifactory.engine;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.api.search.exception.InvalidChecksumException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.search.InternalSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.artifactory.request.range.stream.SingleRangeSkipInputStream.log;
import static org.artifactory.util.HttpUtils.getSha1Checksum;
import static org.artifactory.util.HttpUtils.getSha256Checksum;

/**
 * @author Lior Gur
 */
@Component
public class ChecksumDeployPermissionValidator {

    @Autowired
    private InternalSearchService searchService;

    @Autowired
    private AuthorizationService authService;

    public boolean isChecksumDeployRequest(ArtifactoryRequest request) {
        return Boolean.parseBoolean(request.getHeader(ArtifactoryRequest.CHECKSUM_DEPLOY));
    }

    protected boolean hasReadPermission(ArtifactoryRequest request, ArtifactoryResponse response)
            throws IOException {

        String sha1 = getSha1Checksum(request);
        String sha2 = getSha256Checksum(request);

        String checksum = null;
        ChecksumType checksumType = null;
        if (StringUtils.isNotBlank(sha1)) {
            checksum = sha1;
            checksumType = ChecksumType.sha1;
        } else if (StringUtils.isNotBlank(sha2)) {
            checksum = sha2;
            checksumType = ChecksumType.sha256;
        } else {
            response.sendError(SC_BAD_REQUEST, "Checksum values not provided", log);
        }

        return ConstantValues.ignoreChecksumDeployPermissionCheck.getBoolean() || authService.isAdmin() ||
                hasReadPermissionToChecksumFile(response, checksum, checksumType, request.getRepoKey());
    }

    private boolean hasReadPermissionToChecksumFile(ArtifactoryResponse response, String checksum,
            ChecksumType checksumType, String repoKey)
            throws IOException {

        int limit = ConstantValues.maxArtifactsPermissionTestOnChecksumDeploy.getInt();
        ChecksumSearchControls csControls = new ChecksumSearchControls();
        csControls.addChecksum(checksumType, checksum);
        csControls.setLimit(limit);

        try {
            //Get list of artifacts using Aql with parameters checksum & checksumType
            Set<RepoPath> repoPaths = searchService
                    .searchArtifactsByChecksum(csControls);

            if (repoPaths.isEmpty()) {
                String msg = checksumType + ":" + checksum + " not found";
                response.sendError(SC_NOT_FOUND, msg, log);
                return false;

            }
            for (RepoPath repoPath : repoPaths) {
                if (authService.canRead(repoPath)) {
                    return true;
                }
            }
            // Getting here means that no permission found for an artifact with this checksum
            // In this case we will search using Aql with parameters checksum, checksumType & Repo
            // And than check permission on the results
            if (repoPaths.size() == limit) {
                csControls.setSelectedRepoForSearch(Lists.newArrayList(repoKey));
                repoPaths = searchService
                        .searchArtifactsByChecksum(csControls);
                for (RepoPath repoPath : repoPaths) {
                    if (authService.canRead(repoPath)) {
                        return true;
                    }
                }
            }
            String msg = checksumType + ":" + checksum + " not found";
            response.sendError(SC_NOT_FOUND, msg, log);
            return false;

        } catch (InvalidChecksumException e) {
            e.printStackTrace();
        }
        return true;
    }
}
