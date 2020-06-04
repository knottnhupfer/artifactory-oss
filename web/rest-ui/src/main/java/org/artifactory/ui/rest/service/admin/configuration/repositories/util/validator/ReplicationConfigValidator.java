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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.cron.CronUtils;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.*;
import static org.artifactory.repo.config.RepoConfigDefaultValues.*;

/**
 * Service validates values in the model and sets default values as needed.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReplicationConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(ReplicationConfigValidator.class);

    private AddonsManager addonsManager;
    private CentralConfigService configService;

    @Autowired
    public ReplicationConfigValidator(AddonsManager addonsManager, CentralConfigService configService) {
        this.addonsManager = addonsManager;
        this.configService = configService;
    }

    /**
     * Validates the given local repo replication models and sets default values where needed and nulls are given
     */
    public void validateLocalModels(List<LocalReplicationConfigModel> replications) throws RepoConfigException {
        long enabledReplications = replications.stream()
                .filter(LocalReplicationConfigModel::isEnabled)
                .count();
        if (!replications.isEmpty() && !addonsManager.isLicenseInstalled()) {
            throw new RepoConfigException("Replication is only available with a pro license and above", SC_FORBIDDEN);
        } else if (enabledReplications > 1 && !addonsManager.isHaLicensed()) {
            throw new RepoConfigException("Multi-push replication is only available with an Enterprise license.",
                    SC_FORBIDDEN);
        }
        if (replications.size() > 1) {
            checkForDuplicateUrls(replications);
        }
        for (LocalReplicationConfigModel replication : replications) {
            if (!CronUtils.isValid(replication.getCronExp())) {
                throw new RepoConfigException("Invalid cron expression", SC_BAD_REQUEST);
            }
            replication.setEnabled(Optional.of(replication.isEnabled()).orElse(false));
            //Required field, but don't fail validation for it as default is in place
            replication.setSocketTimeout(Optional.of(replication.getSocketTimeout()).orElse(15000));
            replication.setSyncDeletes(Optional.of(replication.isSyncDeletes()).orElse(DEFAULT_SYNC_PROPERTIES));
            replication.setSyncProperties(Optional.of(replication.isSyncProperties()).orElse(DEFAULT_SYNC_PROPERTIES));
            replication.setSyncStatistics(Optional.of(replication.isSyncStatistics()).orElse(DEFAULT_REPLICATION_SYNC_STATISTICS));
            replication.setCheckBinaryExistenceInFilestore(Optional.of(replication.isCheckBinaryExistenceInFilestore())
                    .orElse(DEFAULT_REPLICATION_CHECK_BINARY_EXISTENCE_IN_FILESTORE));
            replication.setEnableEventReplication(Optional.of(replication.isEnableEventReplication()).orElse(DEFAULT_EVENT_REPLICATION));
            String proxyKey = replication.getProxy();
            if ((isNotBlank(proxyKey)) && (configService.getDescriptor().getProxy(proxyKey) == null)) {
                throw new RepoConfigException("Invalid proxy configuration name", SC_BAD_REQUEST);
            }
            if (StringUtils.isBlank(replication.getUrl())) {
                throw new RepoConfigException("Replication url is required", SC_BAD_REQUEST);
            }
            if (StringUtils.isBlank(replication.getUsername())) {
                throw new RepoConfigException("Replication username is required", SC_BAD_REQUEST);
            }
        }
    }

    /**
     * Validates the given local repo replication models and sets default values where needed and nulls are given
     * Returns one remote Config model(because only one is allowed) or fails if the model contains more than one
     */
    public RemoteReplicationConfigModel validateRemoteModel(RemoteRepositoryConfigModel repo)
            throws RepoConfigException {

        //No config given
        List<RemoteReplicationConfigModel> replications = repo.getReplications();
        if (replications == null || replications.isEmpty() || replications.get(0) == null) {
            log.debug("No replication configuration given for repo {}", repo.getGeneral().getRepoKey());
            return null;
        }
        if (!addonsManager.isLicenseInstalled()) {
            throw new RepoConfigException("Replication is only available with a pro license", SC_FORBIDDEN);
        } else if (repo.getReplications().size() > 1) {
            throw new RepoConfigException("Only one pull replication configuration is allowed", SC_BAD_REQUEST);
        }
        RemoteReplicationConfigModel replication = replications.get(0);
        if (isNotBlank(replication.getCronExp()) && Boolean.TRUE.equals(replication.isEnabled())
                && !CronUtils.isValid(replication.getCronExp())) {
            throw new RepoConfigException("Invalid cron expression", SC_BAD_REQUEST);
        }
        if (Boolean.TRUE.equals(replication.isEnabled()) &&
                (repo.getAdvanced().getNetwork() == null
                        || StringUtils.isBlank(repo.getAdvanced().getNetwork().getUsername())
                        || StringUtils.isBlank(repo.getAdvanced().getNetwork().getPassword()))) {
            throw new RepoConfigException("Pull replication requires non-anonymous authentication to the remote " +
                    "repository. Please make sure to fill-in the 'Username' and 'Password' fields in the " +
                    "'Advanced Settings' tab. ", SC_UNAUTHORIZED);
        }
        replication.setEnabled(Optional.of(replication.isEnabled()).orElse(false));
        replication.setEnableEventReplication(Optional.of(replication.isEnableEventReplication()).orElse(DEFAULT_EVENT_REPLICATION));
        replication.setSyncDeletes(Optional.of(replication.isSyncDeletes()).orElse(DEFAULT_REPLICATION_SYNC_DELETES));
        replication.setSyncProperties(Optional.of(replication.isSyncProperties()).orElse(DEFAULT_SYNC_PROPERTIES));
        return replication;
    }

    public void validateAllTargetReplicationLicenses(LocalRepoDescriptor repo,
            List<LocalReplicationDescriptor> replications) throws RepoConfigException {
        String failMessage = "Multi Push Replication is supported for targets with an enterprise license only";
        String errorMessage = null; //is returned if something unexpected happened during tests
        int numOfActiveReplication = 0;
        int numOfTargetsFailed = 0;
        int numberOfTargetSucceeded = 0;
        int numOfReplications = replications.size();
        for (LocalReplicationDescriptor replication : replications) {
            try {
                if (!replication.isEnabled()) {
                    continue;
                }
                numOfActiveReplication++;
                addonsManager.addonByType(ReplicationAddon.class).validateTargetLicense(replication, repo,
                        numOfReplications);
                numberOfTargetSucceeded++;
            } catch (Exception error) {
                if (error.getMessage().equals(failMessage)) {
                    numOfTargetsFailed++;
                } else {
                    errorMessage = "Error occurred while testing replication config for url '" + replication.getUrl()
                            + "': " + error.getMessage();
                }
            }
        }
        if (numOfActiveReplication == numberOfTargetSucceeded) {
            log.debug("All replication targets for repo {} tested successfully", repo.getKey());
        } else if (isNotBlank(errorMessage)) {
            throw new RepoConfigException(errorMessage, SC_BAD_REQUEST);
        } else if (numOfActiveReplication == numOfTargetsFailed && numOfActiveReplication != 0) {
            throw new RepoConfigException(failMessage, SC_BAD_REQUEST);
        } else {
            throw new RepoConfigException("Note: " + failMessage, SC_BAD_REQUEST);
        }
    }

    private void checkForDuplicateUrls(List<LocalReplicationConfigModel> replications) throws RepoConfigException {
        Set<String> allItems = new HashSet<>(); //util set to catch duplicate urls
        Set<String> duplicates = replications.stream()
                .map(LocalReplicationConfigModel::getUrl)
                .filter(url -> !allItems.add(url))
                .collect(Collectors.toSet());
        //duplicates now contains all duplicate urls if any
        if (!duplicates.isEmpty()) {
            throw new RepoConfigException("Url '" + duplicates.iterator().next() + "' already exists as a " +
                    "replication target for this repository", SC_BAD_REQUEST);
        }
    }
}
