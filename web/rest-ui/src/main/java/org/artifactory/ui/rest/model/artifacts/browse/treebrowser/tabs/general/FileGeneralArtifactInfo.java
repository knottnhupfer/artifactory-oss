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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.checksums.Checksums;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.dependecydeclaration.DependencyDeclaration;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.trash.TrashItemDetails;
import org.artifactory.util.RepoPathUtils;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonFilter;

/**
 * @author Chen Keinan
 */
@JsonTypeName("file")
@JsonFilter("exclude fields")
@IgnoreSpecialFields(value = {"repoKey", "path"})
public class FileGeneralArtifactInfo extends GeneralArtifactInfo {

    private DependencyDeclaration dependencyDeclaration;
    private Checksums checksums;
    private Boolean bintrayInfoEnabled;
    private TrashItemDetails trash;

    FileGeneralArtifactInfo() {
    }

    @Override
    public void populateGeneralData() {
        RepoPath repoPath = retrieveRepoPath();
        RepositoryService repoService = retrieveRepoService();
        LocalRepoDescriptor localRepoDescriptor = repoService
                .localCachedOrDistributionRepoDescriptorByKey(repoPath.getRepoKey());
        super.setInfo(
                new org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.FileInfo(repoPath));
        markIfBintrayIsEnabled(repoPath);
        // update file info checksum
        updateFileInfoCheckSum(repoPath, localRepoDescriptor);
        if (localRepoDescriptor.getType() != RepoType.Distribution) {
            populateVirtualRepositories(localRepoDescriptor);
            updateDependencyDeclaration(repoPath, localRepoDescriptor);
            updateTrashDetails(repoPath);
        }
    }

    private void updateTrashDetails(RepoPath repoPath) {
        if (!RepoPathUtils.isTrash(repoPath)) {
            return;
        }
        Properties properties = ContextHelper.get().beanForType(PropertiesService.class).getProperties(repoPath);
        trash = new TrashItemDetails(properties);
    }

    private void markIfBintrayIsEnabled(RepoPath repoPath) {
        boolean hideInfo = ConstantValues.bintrayUIHideInfo.getBoolean();
        if (hideInfo) {
            bintrayInfoEnabled = false;
            return;
        }

        boolean validFile = isValidFile(repoPath);
        if (!validFile) {
            bintrayInfoEnabled = false;
            return;
        }

        boolean offlineMode = retrieveCentralConfigService().getDescriptor().isOfflineMode();
        if (offlineMode) {
            bintrayInfoEnabled = false;
            return;
        }

        ModuleInfo moduleInfo = retrieveRepoService().getItemModuleInfo(repoPath);
        if (moduleInfo.isIntegration()) {
            bintrayInfoEnabled = false;
            return;
        }

        BintrayService bintrayService = ContextHelper.get().beanForType(BintrayService.class);
        boolean hasSystemAPIKey = bintrayService.hasBintraySystemUser();
        boolean userExists = isUserExists();
        if (!hasSystemAPIKey && !userExists) {
            bintrayInfoEnabled = false;
            return;
        }

        // All good, enable it
        bintrayInfoEnabled = true;
    }

    private boolean isValidFile(RepoPath repoPath) {
        return NamingUtils.isJarVariant(repoPath.getName()) || NamingUtils.isPom(repoPath.getName());
    }

    private boolean isUserExists() {
        return !ContextHelper.get().beanForType(UserGroupService.class).currentUser().isTransientUser();
    }

    private void updateDependencyDeclaration(RepoPath repoPath, LocalRepoDescriptor localRepoDescriptor) {
        DependencyDeclaration localDependencyDeclaration = new DependencyDeclaration();
        localDependencyDeclaration.updateDependencyDeclaration("", repoPath, localRepoDescriptor);
        if (localDependencyDeclaration.getTypes() != null) {
            dependencyDeclaration = localDependencyDeclaration;
        }
    }

    private void updateFileInfoCheckSum(RepoPath repoPath, LocalRepoDescriptor localRepoDescriptor) {
        FileInfo fileInfo = retrieveRepoService().getFileInfo(repoPath);
        checksums = new Checksums();
        checksums.updateFileInfoCheckSum(fileInfo, localRepoDescriptor);
    }

    public DependencyDeclaration getDependencyDeclaration() {
        return dependencyDeclaration;
    }

    public Checksums getChecksums() {
        return checksums;
    }

    public Boolean getBintrayInfoEnabled() {
        return bintrayInfoEnabled;
    }

    public TrashItemDetails getTrash() {
        return trash;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToStringIgnoreSpecialFields(this);
    }
}
