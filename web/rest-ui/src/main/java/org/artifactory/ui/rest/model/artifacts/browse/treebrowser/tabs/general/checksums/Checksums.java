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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.checksums;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties("checksumsMatch")
public class Checksums {

    private String sha2;
    private String sha1;
    private String sha1Value;
    private String md5;
    private boolean showFixChecksums; //Signifies if the 'fix checksums' button should be shown on UI
    private String message;           //If show fix checksums button this holds the relevant warning

    public String getSha2() {
        return sha2;
    }

    public void setSha2(String sha2) {
        this.sha2 = sha2;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha1Value() {
        return sha1Value;
    }

    public void setSha1Value(String sha1Value) {
        this.sha1Value = sha1Value;
    }

    public boolean isShowFixChecksums() {
        return showFixChecksums;
    }

    public void setShowFixChecksums(boolean showFixChecksums) {
        this.showFixChecksums = showFixChecksums;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void updateFileInfoCheckSum(FileInfo fileInfo, LocalRepoDescriptor localRepoDescriptor) {
        boolean isLocalRepo = localRepoDescriptor.isLocal();
        ChecksumInfo md5Info = setChecksum(ChecksumType.md5, fileInfo, isLocalRepo, this::setMd5);
        ChecksumInfo sha1Info = setChecksum(ChecksumType.sha1, fileInfo, isLocalRepo, this::setSha1);
        ChecksumInfo sha2Info = handleSha2(fileInfo, isLocalRepo);
        if (sha1Info != null) {
            sha1Value = sha1Info.getActual();
        }
        //No need to show message if at least 2 checksums match
        if (checksumsOk(md5Info, sha1Info, sha2Info)) {
            showFixChecksums = false;
        } else {
            AuthorizationService authService = ContextHelper.get().getAuthorizationService();
            boolean userHasPermissionsToFix =
                    !authService.isAnonymous() && authService.canDeploy(fileInfo.getRepoPath());
            if (!RepoType.Docker.equals(localRepoDescriptor.getType()) && userHasPermissionsToFix) {
                showFixChecksums = true;
            }
            message = prepareFixChecksumsMessage(userHasPermissionsToFix, isLocalRepo, md5Info, sha1Info);
        }
    }

    private ChecksumInfo setChecksum(ChecksumType checksumType, FileInfo fileInfo, boolean isLocalRepo,
            Consumer<String> setter) {
        String checksum = "";
        ChecksumInfo checksumInfo = getChecksumOfType(fileInfo, checksumType);
        if (checksumInfo != null) {
            checksum = buildChecksumString(checksumInfo, isLocalRepo);
        }
        setter.accept(checksum);
        return checksumInfo;
    }

    private ChecksumInfo handleSha2(FileInfo fileInfo, boolean isLocalRepo) {
        ChecksumInfo checksumInfo = getChecksumOfType(fileInfo, ChecksumType.sha256);
        if ((checksumInfo == null || StringUtils.isBlank(checksumInfo.getActual()))) {
            //Should be accompanied by an InternalDbService sha2 state check but its not visible here and we don't care that much in UI.
            String sha2FromProperties = getSha2FromProperties(fileInfo.getRepoPath());
            if (ChecksumType.sha256.isValid(sha2FromProperties)) {
                checksumInfo = new ChecksumInfo(ChecksumType.sha256, ChecksumInfo.TRUSTED_FILE_MARKER,
                        sha2FromProperties);
            }
        }
        String checksum = ChecksumType.sha256 + ": Not Calculated";
        if (checksumInfo != null && isNotBlank(checksumInfo.getActual())) {
            checksum = buildChecksumString(checksumInfo, isLocalRepo);
        }
        setSha2(checksum);
        return checksumInfo;
    }

    private ChecksumInfo getChecksumOfType(org.artifactory.fs.FileInfo file, ChecksumType checksumType) {
        return file.getChecksumsInfo().getChecksumInfo(checksumType);
    }

    private String prepareFixChecksumsMessage(boolean userHasPermissionsToFix, boolean isLocalRepo,
                                              ChecksumInfo md5Info, ChecksumInfo sha1Info) {
        StringBuilder fixMessage = new StringBuilder();
        if (isAllChecksumsMissing(sha1Info, md5Info)) {
            if (isLocalRepo) {
                fixMessage.append("Client did not publish a checksum value.\n");
            } else {
                fixMessage.append("Remote checksum doesn't exist.\n");
            }
        } else if (isAllChecksumsBroken(sha1Info, md5Info) || isOneOkOtherBroken(sha1Info, md5Info) ||
                isOneMissingOtherBroken(sha1Info, md5Info)) {
            String repoClass = isLocalRepo ? "Uploaded" : "Remote";
            fixMessage = new StringBuilder().append(repoClass).append(" checksum doesn't match the actual checksum.\n ")
                    .append("Please redeploy the artifact with a correct checksum.");
        }
        if (userHasPermissionsToFix) {
            fixMessage.append("If you trust the ").append(isLocalRepo ? "uploaded" : "remote")
                    .append(" artifact you can accept the actual checksum by clicking the 'Fix Checksum' button.");
        }
        return fixMessage.toString();
    }

    private boolean isChecksumMatch(ChecksumInfo info) {
        return info != null && info.checksumsMatch();
    }

    private boolean isChecksumBroken(ChecksumInfo info) {
        return info != null && !info.checksumsMatch();
    }

    private boolean isChecksumMissing(ChecksumInfo info) {
        return info == null || info.getOriginal() == null;
    }

    /**
     * @return Check if one of the {@link ChecksumType} is ok and the other broken
     */
    private boolean isOneOkOtherBroken(ChecksumInfo sha1Info, ChecksumInfo md5Info) {
        return (isChecksumMatch(sha1Info) && isChecksumBroken(md5Info))
                || (isChecksumMatch(md5Info) && isChecksumBroken(sha1Info));
    }

    /**
     * @return Check if one of the {@link ChecksumType} is missing and the other is broken (i.e don't match).
     */
    private boolean isOneMissingOtherBroken(ChecksumInfo sha1Info, ChecksumInfo md5Info) {
        return isChecksumMatch(sha1Info) && isChecksumBroken(md5Info) || (isChecksumMatch(md5Info)
                && isChecksumBroken(sha1Info));
    }

    private boolean checksumsOk(ChecksumInfo md5Info, ChecksumInfo sha1Info, ChecksumInfo sha2Info) {
        boolean md5Match = md5Info != null && md5Info.checksumsMatch();
        boolean sha1Match = sha1Info != null && sha1Info.checksumsMatch();
        boolean sha2Ok = sha2Info != null && isNotBlank(sha2Info.getActual()); // match not applicable for sha2
        return md5Match && sha1Match && sha2Ok || isOneMissingOthersMatch(md5Match, sha1Match, sha2Ok);
    }

    /**
     * @return Check if one of the {@link ChecksumType} is missing and the other matches.
     */
    private boolean isOneMissingOthersMatch(boolean md5Match, boolean sha1Match, boolean sha2Ok) {
        return md5Match ? (sha1Match || sha2Ok) : (sha1Match && sha2Ok);
    }

    /**
     * @return Check that all {@link ChecksumType}s are broken (but are <b>NOT<b/> missing)
     */
    private boolean isAllChecksumsBroken(ChecksumInfo... checksumInfos) {
        for (ChecksumInfo type : checksumInfos) {
            if (!isChecksumBroken(type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Check that all {@link ChecksumType}s are <b>missing<b/>
     */
    private boolean isAllChecksumsMissing(ChecksumInfo... checksumInfos) {
        for (ChecksumInfo type : checksumInfos) {
            if (!isChecksumMissing(type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @deprecated Prefer usage of native sha2 (and checking if instance already migrated)
     */
    @Nullable
    @Deprecated
    private String getSha2FromProperties(RepoPath path) {
        Properties properties = ContextHelper.get().beanForType(PropertiesService.class).getProperties(path);
        return properties.getFirst("sha256");
    }

    private String buildChecksumString(ChecksumInfo checksumInfo, boolean isLocalRepo) {
        StringBuilder sb = new StringBuilder()
                .append(checksumInfo.getType()).append(": ")
                .append(checksumInfo.getActual()).append(" (")
                .append(isLocalRepo ? "Uploaded" : "Remote").append(": ");
        if (checksumInfo.getOriginal() != null) {
            if (checksumInfo.checksumsMatch()) {
                sb.append("Identical");
            } else {
                sb.append(checksumInfo.getOriginal());
            }
        } else {
            sb.append("None");
        }

        return sb.append(")").toString();
    }
}
