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

package org.artifactory.bintray.distribution;

import com.google.common.collect.Multimap;
import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.details.RepositoryDetails;
import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.handle.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.bintray.BintrayUploadInfo;
import org.artifactory.api.bintray.distribution.Distribution;
import org.artifactory.api.bintray.distribution.DistributionService;
import org.artifactory.api.bintray.distribution.Distributor;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayPackageModel;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayRepoModel;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayVersionModel;
import org.artifactory.api.bintray.distribution.resolver.DistributionCoordinatesResolver;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.repo.DistributionRepo;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.artifactory.bintray.distribution.util.DistributionUtils.handleException;

/**
 * Responsible for all Distribution actions performed against Bintray
 *
 * @author Dan Feldman
 */
@Component
public class DistributorImpl implements Distributor {
    private static final Logger log = LoggerFactory.getLogger(DistributorImpl.class);

    @Autowired
    private InternalRepositoryService repoService;

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private AuthorizationService authService;


    @Override
    public DistributionReporter distribute(Distribution distribution) {
        if (distribution.isDryRun() && distribution.isAsync()) {
            log.debug("Dry run distribution called with async - setting async to false");
            distribution.setAsync(false);
        }
        if (distribution.isAsync()) {
            ContextHelper.get().beanForType(Distributor.class).distributeInternal(distribution);
            return new DistributionReporter(!distribution.isDryRun());
        } else {
            return distributeInternal(distribution);
        }
    }

    @Override
    public DistributionReporter distributeInternal(Distribution distribution) {
        DistributionReporter status = new DistributionReporter(!distribution.isDryRun());
        if (StringUtils.isBlank(distribution.getTargetRepo())) {
            status.error("No distribution repo specified to use for distributing the requested artifact(s).", SC_BAD_REQUEST, log);
            return status;
        }
        DistributionRepo distRepo = repoService.distributionRepoByKey(distribution.getTargetRepo());
        if (!verifyCanDistribute(distRepo, distribution.getTargetRepo(), distribution.getPackagesRepoPaths(), status)) {
            return status;
        }
        DistributionRepoDescriptor descriptor = distRepo.getDescriptor();
        BintrayVersionPathsMapper mapper = new BintrayVersionPathsMapper(descriptor, distributionService, repoService, status);
        Multimap<BintrayUploadInfo, DistributionCoordinatesResolver> filesPerVersion = mapper.mapPaths(distribution.getPackagesRepoPaths());
        if (filesPerVersion.isEmpty()) {
            status.error("No valid paths to distribute to Bintray, aborting.", HttpStatus.SC_BAD_REQUEST, log);
            return status;
        }
        SubjectHandle bintraySubject = createBintraySubject(distRepo.getClient(), descriptor);
        status.status("Starting distribution for requested paths using repo: " + distribution.getTargetRepo(), log);
        filesPerVersion.keySet().stream()
                .filter(version -> validateDistributionPaths(filesPerVersion.get(version), status))
                .forEach(version -> distributeVersion(distribution, descriptor, filesPerVersion, bintraySubject, version, status));
        return status;
    }

    private void distributeVersion(Distribution distribution, DistributionRepoDescriptor descriptor,
            Multimap<BintrayUploadInfo, DistributionCoordinatesResolver> filesPerVersion,
            SubjectHandle bintraySubject, BintrayUploadInfo version, DistributionReporter status) {
        try {
            BintrayRepoModel bintrayRepoModel = new BintrayRepoModel(version);
            VersionHandle bintrayVersion = fetchBintrayVersion(bintraySubject, distribution, version, bintrayRepoModel, status);
            new BintrayVersionDistributor(distribution, bintrayVersion, bintraySubject, descriptor,
                    bintrayRepoModel, status).distributeVersion(filesPerVersion.get(version));
        } catch (Exception e) {
            //Already logged
        }
    }

    @Override
    public void signAndPublish(Distribution distribution, VersionHandle bintrayVersion, int versionFilesSize,
            RepoType type, DistributionRepoDescriptor repoDescriptor) {
        //This action uses a separate reporter to avoid messages sometimes appearing and sometimes not.
        DistributionReporter status = new DistributionReporter(!distribution.isDryRun());
        boolean canSign = canSignType(type);
        boolean canPublish = canPublish(distribution, type);
        if (canSign || canPublish) {
            String versionName = bintrayVersion.pkg().name() + "/" + bintrayVersion.name();
            String gpgPassphrase = getGpgPassphrase(distribution, repoDescriptor, versionName, status);
            if (canSign) {
                signVersionIfNeeded(bintrayVersion, gpgPassphrase, versionFilesSize, status);
            }
            if (canPublish) {
                //Default publish is true
                publishFiles(bintrayVersion, gpgPassphrase, status);
            }
        }
    }

    /**
     * Outputs the status of this request's signing info - whether the user passed it or the descriptor demands it
     * and returns the passphrase itself if signing is required for this version
     * NOTE: If the descriptor requires signing but no passphrase is present this method returns an empty string to
     * signify that and allow the publish and sign operations to attempt to sign without a passphrase.
     */
    @Nullable
    private String getGpgPassphrase(Distribution distribution, DistributionRepoDescriptor repoDescriptor, String verName,
            DistributionReporter status) {
        String passphrase = null;
        if (StringUtils.isNotBlank(distribution.getGpgPassphrase())) {
            //passing the passphrase from api overrides anything
            status.status("GPG Passphrase was passed to the command - version " + verName + " will be signed", log);
            passphrase = distribution.getGpgPassphrase();
        } else if (repoDescriptor.isGpgSign()) {
            if (StringUtils.isNotBlank(repoDescriptor.getGpgPassPhrase())) {
                //descriptor says sign and has passphrase
                status.status("Repository " + repoDescriptor.getKey() + " is configured to automatically sign versions"
                        + " - version " + verName + " will be signed", log);
                passphrase = repoDescriptor.getGpgPassPhrase();
            } else {
                //descriptor says sign and no passphrase - attempt to sign anyway the private key might not be required
                status.status("Repository " + repoDescriptor.getKey() + " is configured to automatically sign versions"
                        + " and no passphrase was given - attempting to sign version " + verName + " without " +
                        "a passphrase", log);
                passphrase = "";
            }
        }
        return passphrase;
    }

    /**
     * Signs all files in {@param version} except for maybe metadata - see doc of {@link DistributorImpl#publishFiles}
     */
    private void signVersionIfNeeded(VersionHandle version, String gpgPassphrase, int versionFileCount,
            DistributionReporter status) {
        try {
            //BLank string means gpg sign without password
            if (gpgPassphrase != null && gpgPassphrase.equals("")) {
                version.sign(versionFileCount);
            } else if (gpgPassphrase != null) {
                version.sign(gpgPassphrase, versionFileCount);
            }
        } catch (Exception e) {
            handleException(null, "Error signing version " + version.pkg().name() + "/" + version.name(), e, status, log);
        }
    }

    /**
     * Publishes all files in {@param version}, Also signs metadata if required.
     * The way this flow works in Bintray forces us to publish + sign (with the header) if we want metadata files
     * (i.e. debian metadata) signed and the sign AGAIN if we want the actual artifacts signed.
     */
    private void publishFiles(VersionHandle version, String gpgPassphrase, DistributionReporter status) {
        String verName = version.pkg().name() + "/" + version.name();
        status.status("Publishing files in version: " + verName, log);
        try {
            if (gpgPassphrase != null) {
                version.publish(gpgPassphrase);
            } else {
                version.publish();
            }
        } catch (BintrayCallException bce) {
            handleException(null, "Error publishing files in version " + verName, bce, status, log);
        }
    }


    /**
     * Get the subject for this repo's oauth app (the org used to create it)
     */
    private SubjectHandle createBintraySubject(Bintray client, DistributionRepoDescriptor descriptor) {
        return client.subject(descriptor.getBintrayApplication().getOrg());
    }

    /**
     * Gets or creates the whole 'chain' up to the required Bintray version
     * (meaning also the repo and package to 'reach' it) that the given {@param uploadInfo} points to.
     * Gets the proper {@link VersionHandle} based on the {@param uploadInfo}'s type
     */
    private VersionHandle fetchBintrayVersion(SubjectHandle subjectHandle, Distribution distribution,
            BintrayUploadInfo uploadInfo, BintrayRepoModel bintrayRepoModel, DistributionReporter status) throws Exception {
        VersionHandle bintrayVersion;
        //Docker package/version are created by the DockerPusher we use later - so only create a repo and return
        //an unverified VersionHandle for the future version the pusher will create.
        if (RepoType.Docker.getType().equalsIgnoreCase(uploadInfo.getRepositoryDetails().getType())) {
            bintrayVersion = getDockerVersion(subjectHandle, uploadInfo, distribution.isDryRun(), bintrayRepoModel, status);
        } else {
            bintrayVersion = getOrCreateVersionForCoordinates(subjectHandle, uploadInfo,
                    distribution.isDryRun(), distribution.isOverrideExistingFiles(), bintrayRepoModel, status);
        }
        return bintrayVersion;
    }

    /**
     * @return A {@link VersionHandle} for the version that will be created in Bintray for these {@param coordinates}.
     * will only get or create the repo as the package and version are created by the Bintray Docker Pusher.
     */
    private VersionHandle getDockerVersion(SubjectHandle subject, BintrayUploadInfo uploadInfo, boolean isDryRun,
            BintrayRepoModel bintrayRepoModel, DistributionReporter status) throws Exception {
        //For Docker, only need to create repo, package and version are created by the docker pusher later.
        RepositoryHandle btRepo = getOrCreateRepo(uploadInfo.getRepositoryDetails(), subject, isDryRun, bintrayRepoModel, status);
        PackageHandle btPackage = btRepo.pkg(uploadInfo.getPackageDetails().getName());
        VersionHandle btVersion = btPackage.version(uploadInfo.getVersionDetails().getName());
        reportDockerVersion(btRepo, btPackage, btVersion, bintrayRepoModel, status);
        return btVersion;
    }

    /**
     * Determines whether the {@param btPackage} and {@param btVersion} that should be created in Bintray exist or not,
     * and makes relevant entries in {@param status}
     */
    private void reportDockerVersion(RepositoryHandle btRepo, PackageHandle btPackage, VersionHandle btVersion,
            BintrayRepoModel bintrayRepoModel, DistributionReporter status) throws BintrayCallException {
        BintrayPackageModel bintrayPackageModel = bintrayRepoModel.packages.get(btPackage.name());
        if (bintrayPackageModel != null) {
            try {
                bintrayPackageModel.created = !btPackage.exists();
            } catch (Exception e) {
                handleException(null, "Error retrieving info for package " + btRepo.name() + "/" + btPackage.name(), e,
                        status, log);
                throw e;
            }
            BintrayVersionModel bintrayVersionModel = bintrayPackageModel.versions.get(btVersion.name());
            if (bintrayVersionModel != null) {
                try {
                    bintrayVersionModel.created = !btVersion.exists();
                } catch (Exception e) {
                    handleException(null, "Error retrieving info for version " + btRepo.name() + "/" + btPackage.name()
                            + "/" + btVersion.name(), e, status, log);
                    throw e;
                }
            }
        }
    }

    /**
     * @return A {@link VersionHandle} for the version that will be created in Bintray for these {@param coordinates}.
     * will get or create the repo, package and version if needed.
     */
    private VersionHandle getOrCreateVersionForCoordinates(SubjectHandle subject, BintrayUploadInfo uploadInfo,
            boolean isDryRun, boolean overrideExistingVersions, BintrayRepoModel bintrayRepoModel,
            DistributionReporter status) throws Exception {
        RepositoryHandle btRepo = getOrCreateRepo(uploadInfo.getRepositoryDetails(), subject, isDryRun, bintrayRepoModel, status);
        //pkg defaults are only for creation, we don't override
        BintrayPackageModel bintrayPackageModel = Optional.ofNullable(
                bintrayRepoModel.packages.get(uploadInfo.getPackageDetails().getName())).orElse(new BintrayPackageModel());
        PackageHandle btPkg = getOrCreatePackage(uploadInfo.getPackageDetails(), btRepo, bintrayPackageModel, isDryRun, status);
        BintrayVersionModel bintrayVersionModel = Optional.ofNullable(
                bintrayPackageModel.versions.get(uploadInfo.getVersionDetails().getName())).orElse(new BintrayVersionModel());
        return getOrCreateVersion(uploadInfo.getVersionDetails(), btPkg, bintrayVersionModel, overrideExistingVersions, isDryRun, status);
    }

    /**
     * Create or update an existing Bintray Repository with the specified info
     *
     * @param repositoryDetails BintrayUploadInfo representing the supplied json file
     * @param subjectHandle     SubjectHandle retrieved by the Bintray Java Client
     * @param status            status holder of entire operation
     * @return a RepositoryHandle   pointing to the created/updated repository
     * @throws Exception on any unexpected error thrown by the Bintray client
     */
    private RepositoryHandle getOrCreateRepo(RepositoryDetails repositoryDetails, SubjectHandle subjectHandle,
            boolean isDryRun, BintrayRepoModel bintrayRepoModel, DistributionReporter status) throws Exception {
        String repoName = repositoryDetails.getName();
        RepositoryHandle bintrayRepoHandle = subjectHandle.repository(repoName);
        try {
            if (bintrayRepoHandle.exists()) {
                bintrayRepoModel.created = false;
            } else {
                //Repo doesn't exist - create it using the RepoDetails
                status.status("Creating repo " + repoName + " for subject " + bintrayRepoHandle.owner().name(), log);
                if (!isDryRun) {
                    bintrayRepoHandle = subjectHandle.createRepo(repositoryDetails);
                }
                bintrayRepoModel.created = true;
            }
        } catch (Exception e) {
            handleException(null, "Error retrieving information for repo " + repositoryDetails.getName(), e, status, log);
            throw e;
        }
        //Repo exists and should not be updated
        return bintrayRepoHandle;
    }

    /**
     * Create or update an existing Bintray Package with the specified info
     *
     * @param pkgDetails       PackageDetails for creating the package
     * @param repositoryHandle RepositoryHandle retrieved by the Bintray Java Client
     * @param status           status holder of entire operation
     * @return a PackageHandle pointing to the created/updated package
     * @throws Exception on any unexpected error thrown by the Bintray client
     */
    private PackageHandle getOrCreatePackage(PackageDetails pkgDetails, RepositoryHandle repositoryHandle,
            BintrayPackageModel bintrayPackageModel, boolean isDryRun, DistributionReporter status) throws Exception {
        PackageHandle packageHandle;
        packageHandle = repositoryHandle.pkg(pkgDetails.getName());
        try {
            if (packageHandle.exists()) {
                bintrayPackageModel.created = false;
            } else {
                status.status("Package " + pkgDetails.getName() + " doesn't exist, creating it", log);
                if (!isDryRun) {
                    packageHandle = repositoryHandle.createPkg(pkgDetails);
                    status.debug("Package " + packageHandle.get().name() + " created", log);
                }
                bintrayPackageModel.created = true;
            }
        } catch (Exception e) {
            handleException(null, "Error retrieving information for package " + pkgDetails.getRepo() + "/"
                    + pkgDetails.getName(), e, status, log);
            throw e;
        }
        return packageHandle;
    }

    /**
     * Create or update an existing Bintray Package with the specified info
     *
     * @param versionDetails VersionDetails for creating the version
     * @param packageHandle  PackageHandle retrieved by the Bintray Java Client or by {@link #getOrCreatePackage}
     * @param status         status holder of entire operation
     * @return a VersionHandle pointing to the created/updated version
     * @throws Exception on any unexpected error thrown by the Bintray client
     */
    private VersionHandle getOrCreateVersion(VersionDetails versionDetails, PackageHandle packageHandle,
            BintrayVersionModel bintrayVersionModel, boolean updateExisting, boolean isDryRun,
            DistributionReporter status) throws Exception {
        VersionHandle versionHandle = packageHandle.version(versionDetails.getName());
        status.debug("Override existing version is set to " + updateExisting, log);
        String versionName = versionDetails.getName();
        try {
            if (versionHandle.exists()) {
                bintrayVersionModel.created = false;
                if (updateExisting && !isDryRun) {
                    //Override only version Attributes
                    versionHandle.updateAttributes(versionDetails.getAttributes());
                    status.debug("Attributes updated for version " + versionName, log);
                }
            } else {
                status.status("Version " + versionName + " doesn't exist, creating it", log);
                if (!isDryRun) {
                    versionHandle = packageHandle.createVersion(versionDetails);
                    status.debug("Version " + versionName + " created", log);
                }
                bintrayVersionModel.created = true;
            }
        } catch (Exception e) {
            handleException(null, "Error retrieving information for version" + packageHandle.repository().name() + "/" +
                    packageHandle.name() + "/" + versionDetails.getName(), e, status, log);
            throw e;
        }
        return versionHandle;
    }

    /**
     * Requirements are a valid {@param distributionRepoKey},
     * {@link org.artifactory.descriptor.repo.BintrayApplicationConfig} for this repo and deploy permission on the
     * target repo's root.
     *
     * @return the target repo's descriptor
     */
    private boolean verifyCanDistribute(@Nullable DistributionRepo repo, String distributionRepoKey,
            List<String> requestedPaths, DistributionReporter status) {
        boolean canDistribute = true;
        if (repo == null) {
            status.error("No such Distribution repo " + distributionRepoKey, HttpStatus.SC_NOT_FOUND, log);
            canDistribute = false;
        } else if (repo.getDescriptor().getBintrayApplication() == null) {
            status.error("Repo " + distributionRepoKey + " does not have any Bintray OAuth application defined, " +
                    "aborting.", HttpStatus.SC_BAD_REQUEST, log);
            canDistribute = false;
        } else if (!authService.canDeploy(RepoPathFactory.create(distributionRepoKey, "."))) {
            status.error(String.format(
                    "The user: '%s' Is missing the required permission to distribute to repo %s, aborting.",
                    authService.currentUsername(), distributionRepoKey), HttpStatus.SC_FORBIDDEN, log);
            canDistribute = false;
        } else if (requestedPaths == null || requestedPaths.isEmpty()) {
            status.error("No valid paths were given to distribute", SC_BAD_REQUEST, log);
            canDistribute = false;
        }
        return canDistribute;
    }

    private boolean canSignType(RepoType type) {
        boolean canSignType;
        switch (type) {
            case Docker:
            case NuGet:
            case Npm:
                canSignType = false;
                break;
            default:
                canSignType = true;
        }
        return canSignType;
    }

    private boolean canPublish(Distribution distribution, RepoType type) {
        boolean canPublishType;
        canPublishType = !RepoType.Docker.equals(type);
        return canPublishType && (distribution.isPublish() == null || (distribution.isPublish() != null && distribution.isPublish()));
    }

    private boolean validateDistributionPaths(Collection<DistributionCoordinatesResolver> versionResolvers, DistributionReporter status) {
        if (versionDistributesArtifactsToSamePath(versionResolvers)) {
            //Use the first resolver just to get log info from - they all have identical coordinates
            hardcodedPathError(versionResolvers.iterator().next(), status);
            return false;
        }
        return true;
    }

    /**
     * Identifies a user error that will cause more than one of the artifacts that are distributed to a version
     * to be deployed to the exact same path.
     */
    private boolean versionDistributesArtifactsToSamePath(Collection<DistributionCoordinatesResolver> versionResolvers) {
        long distinctPaths = versionResolvers.stream()
                .map(DistributionCoordinatesResolver::getPath)
                .distinct()
                .count();
        //Less distinct paths then all paths --> there were duplicate paths
        return (distinctPaths < versionResolvers.size()) && versionResolvers.size() > 1;
    }

    private void hardcodedPathError(DistributionCoordinatesResolver dummyResolver, DistributionReporter status) {
        status.error("One or more of the artifacts that were mapped to version " + dummyResolver.getRepo() + "/"
                + dummyResolver.getPkg() + "/" + dummyResolver.getVersion() + " by rule " + dummyResolver.ruleName
                + " are being mapped to the same path '" + dummyResolver.getPath() + "'.  This version will not be " +
                "distributed to Bintray to allow you to recover.", HttpStatus.SC_BAD_REQUEST, log);
    }
}
