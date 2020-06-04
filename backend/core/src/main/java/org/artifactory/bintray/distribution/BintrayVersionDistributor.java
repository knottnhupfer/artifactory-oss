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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.details.ProductDetails;
import com.jfrog.bintray.client.api.handle.ProductHandle;
import com.jfrog.bintray.client.api.handle.SubjectHandle;
import com.jfrog.bintray.client.api.handle.VersionHandle;
import com.jfrog.bintray.client.api.model.Product;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.docker.DockerAddon;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.bintray.BintrayParams;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.distribution.Distribution;
import org.artifactory.api.bintray.distribution.Distributor;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayPackageModel;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayProductModel;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayRepoModel;
import org.artifactory.api.bintray.distribution.reporting.model.BintrayVersionModel;
import org.artifactory.api.bintray.distribution.resolver.DistributionCoordinatesResolver;
import org.artifactory.api.bintray.distribution.rule.DistributionRuleTokens;
import org.artifactory.api.bintray.docker.BintrayDockerPushRequest;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.bintray.distribution.util.DistributionUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

import static org.artifactory.bintray.distribution.util.DistributionUtils.getValueFromToken;
import static org.artifactory.bintray.distribution.util.DistributionUtils.handleException;
import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;

/**
 * Distributes all files for a single version (denoted by a {@link org.artifactory.api.bintray.BintrayUploadInfo}
 * to Bintray, and takes care of the copy and set properties actions on the distributed artifacts on Artifactory side.
 *
 * @author Dan Feldman
 */
class BintrayVersionDistributor {
    private static final Logger log = LoggerFactory.getLogger(BintrayVersionDistributor.class);

    private Distribution distribution;
    private VersionHandle bintrayVersion;
    private SubjectHandle bintraySubject;
    private DistributionRepoDescriptor repoDescriptor;
    private BintrayRepoModel bintrayRepoModel;
    private DistributionReporter status;

    BintrayVersionDistributor(Distribution distribution, VersionHandle bintrayVersion, SubjectHandle bintraySubject,
            DistributionRepoDescriptor repoDescriptor, BintrayRepoModel bintrayRepoModel, DistributionReporter status) {
        this.distribution = distribution;
        this.bintrayVersion = bintrayVersion;
        this.bintraySubject = bintraySubject;
        this.repoDescriptor = repoDescriptor;
        this.bintrayRepoModel = bintrayRepoModel;
        this.status = status;
    }

    /**
     * for each of this {@param bintrayVersion}'s {@param versionCoordinates}:
     * - Deploy the artifact (using special api if needed)
     * - Copy the artifact to its target location in the distribution repo
     * - Attach the created package to the product if needed
     * - Sign the version's files if needed
     * - Publish the version's files if needed
     */
    void distributeVersion(Collection<DistributionCoordinatesResolver> versionResolvers) {
        boolean isDryRun = distribution.isDryRun();
        BintrayVersionModel bintrayVersionModel = getBintrayVersionModel(bintrayVersion);
        boolean artifactsWerePushed = pushArtifacts(versionResolvers, isDryRun, bintrayVersionModel);
        //No need for this whole shebang if no Artifacts reached Bintray.
        if (artifactsWerePushed) {
            //None of these operations cause the others to fail as they don't rely on each other
            handleProductIfNeeded(isDryRun);
            if (!isDryRun) {
                ContextHelper.get().beanForType(Distributor.class).signAndPublish(distribution, bintrayVersion,
                        versionResolvers.size(), versionResolvers.iterator().next().type, repoDescriptor);
            }
        }
        status.registerRepo(bintrayRepoModel);
    }

    private BintrayVersionModel getBintrayVersionModel(VersionHandle bintrayVersion) {
        return Optional.ofNullable(
                Optional.ofNullable(bintrayRepoModel.packages.get(bintrayVersion.pkg().name()))
                        .orElse(new BintrayPackageModel())
                        .versions.get(bintrayVersion.name()))
                .orElse(new BintrayVersionModel());
    }

    /**
     * @return true if at least one artifact was pushed to Bintray.
     */
    private boolean pushArtifacts(Collection<DistributionCoordinatesResolver> versionResolvers, boolean isDryRun,
            BintrayVersionModel bintrayVersionModel) {
        boolean artifactsWerePushed = false;
        for (DistributionCoordinatesResolver coordinates : versionResolvers) {
            try {
                if (!isDryRun) {
                    status.debug("Deploying artifact to Bintray: " + coordinates.toString(), log);
                    //Deploy artifacts to Bintray
                    pushArtifactToBintrayCoordinates(coordinates, distribution.isOverrideExistingFiles());
                    status.status("Successfully deployed artifact to Bintray: " + coordinates.toString(), log);
                    bintrayVersionModel.paths.add(coordinates.toString());
                    artifactsWerePushed = true;
                    copyArtifactsToDistributionRepo(coordinates);
                } else {
                    bintrayVersionModel.paths.add(coordinates.toString());
                    artifactsWerePushed = true;
                }
            } catch (RepoRejectException rre) {
                status.error(coordinates.artifactPath.toPath(), rre.getMessage(), rre.getErrorCode(), log);
                status.debug(coordinates.artifactPath.toPath(), rre.getMessage(), rre, log);
            } catch (Exception e) {
                handleException(coordinates.artifactPath.toPath(), "Error distributing " + coordinates.toString(), e,
                        status, log);
            }
        }
        return artifactsWerePushed;
    }

    /**
     * Decides on the correct API to use for each Artifact based on it's type and deploys it using the client
     */
    private void pushArtifactToBintrayCoordinates(DistributionCoordinatesResolver coordinates, boolean overrideExisting)
            throws BintrayCallException, UnsupportedOperationException, RepoRejectException {
        switch (coordinates.type) {
            case Docker:
                //Docker push always overrides existing.
                distributeDocker(coordinates);
                break;
            case Debian:
                distributeDebian(coordinates, overrideExisting);
                break;
            case Vagrant:
                distributeVagrant(coordinates, overrideExisting);
                break;
            case GitLfs:
            case Gems:
            case Pypi:
                throw new UnsupportedOperationException("Distributing packages of type " + coordinates.type +
                        " is unsupported.");
            default:
                distributeGeneric(coordinates, overrideExisting);
        }
    }

    private void distributeDocker(DistributionCoordinatesResolver coordinates) throws BintrayCallException {
        BintrayDockerPushRequest dockerRequest = new BintrayDockerPushRequest();
        dockerRequest.bintrayRepo = coordinates.getRepo();
        dockerRequest.async = false;
        dockerRequest.bintraySubject = bintraySubject.name();
        dockerRequest.dockerRepository = getDockerRepositoryToken(coordinates);
        dockerRequest.dockerTagName = getDockerTagToken(coordinates);
        dockerRequest.bintrayPackage = coordinates.getPkg();
        dockerRequest.bintrayTag = coordinates.getVersion();
        dockerRequest.sourcePath = coordinates.artifactPath;
        ContextHelper.get().beanForType(AddonsManager.class).addonByType(DockerAddon.class)
                .pushTagToBintray(coordinates.artifactPath.getRepoKey(), dockerRequest, repoDescriptor.getKey());
    }

    private void distributeDebian(DistributionCoordinatesResolver coordinates, boolean overrideExisting)
            throws BintrayCallException, RepoRejectException {
        String dist = getValueFromToken(coordinates, DistributionRuleTokens.Keys.DEB_DISTRIBUTION.key);
        String comp = getValueFromToken(coordinates, DistributionRuleTokens.Keys.DEB_COMPONENT.key);
        String arch = getValueFromToken(coordinates, DistributionRuleTokens.Keys.ARCHITECTURE.key);
        bintrayVersion.uploadDebian(coordinates.getPath(), dist, comp, arch, getArtifactInputStream(coordinates),
                overrideExisting);
    }

    private void distributeVagrant(DistributionCoordinatesResolver coordinates, boolean overrideExisting)
            throws BintrayCallException, RepoRejectException {
        String boxProvider = getValueFromToken(coordinates, DistributionRuleTokens.Keys.VAGRANT_PROVIDER.key);
        bintrayVersion.uploadVagrant(coordinates.getPath(), boxProvider, getArtifactInputStream(coordinates),
                overrideExisting);
    }

    private void distributeGeneric(DistributionCoordinatesResolver coordinates, boolean overrideExisting)
            throws BintrayCallException, RepoRejectException {
        bintrayVersion.upload(coordinates.getPath(), getArtifactInputStream(coordinates), overrideExisting);
    }

    /**
     * Creates the product if required and tests if the package is already contained in it (if it existed) - will add
     * new packages to the product as needed
     */
    private void handleProductIfNeeded(boolean isDryRun) {
        String productName = repoDescriptor.getProductName();
        if (StringUtils.isBlank(productName)) {
            return;
        }
        ProductDetails productDetails = new ProductDetails();
        productDetails.setName(productName);
        ProductHandle productHandle;
        BintrayProductModel bintrayProductModel = new BintrayProductModel(productName);
        try {
            productHandle = getOrCreateProduct(productDetails, isDryRun, bintrayProductModel);
        } catch (BintrayCallException bce) {
            if (bce.getMessage().contains("Max amount of products")) {
                status.warn(bce.getMessage(), bce.getStatusCode(), bce, log);
            } else {
                handleException(null, "Error retrieving information for product" + productName, bce, status, log);
            }
            return;
        } catch (Exception e) {
            handleException(null, "Error retrieving information for product" + productName, e, status, log);
            return;
        }
        attachPackageToProduct(productHandle, isDryRun, bintrayProductModel);
        status.registerProduct(bintrayProductModel);
    }

    private ProductHandle getOrCreateProduct(ProductDetails productDetails, boolean isDryRun,
            BintrayProductModel bintrayProductModel) throws IOException {
        ProductHandle product = bintraySubject.product(productDetails.getName());
        if (product.exists()) {
            bintrayProductModel.created = false;
        } else {
            if (!isDryRun) {
                status.status("Product " + productDetails.getName() + " doesn't exist, creating it", log);
                product = bintraySubject.createProduct(productDetails);
                status.debug("Product " + product.name() + " created", log);
            }
            bintrayProductModel.created = true;
        }
        return product;
    }

    private void attachPackageToProduct(ProductHandle productHandle, boolean isDryRun,
            BintrayProductModel bintrayProductModel) {
        try {
            String pkgForProduct = bintrayVersion.pkg().repository().name() + "/" + bintrayVersion.pkg().name()
                    //Can't have Bintray packages with '/' so we have to replace with ':' although we might have created
                    //it with '/' like we do in Docker
                    .replaceAll("/", ":");

            if (!isDryRun) {
                Product product = productHandle.get();
                if (!product.getPackages().contains(pkgForProduct)) {
                    status.status("Package " + pkgForProduct + " not contained in product " + product.getName() +
                            ", adding it.", log);
                    productHandle.addPackages(Lists.newArrayList(pkgForProduct));
                } else {
                    status.status("Package" + pkgForProduct + " already contained in product " + product.getName(), log);
                }
            }
            bintrayProductModel.attachedPackages.add(pkgForProduct);
        } catch (Exception e) {
            handleException(null, "Error attaching distributed packages to product " + productHandle.name(), e, status, log);
        }
    }

    /**
     * Copies the artifacts that were distributed using {@param coordinates} to this repo under the appropriate
     * tree structure --> root/bintray repo/bintray package/bintray version/path
     * Also writes the coordinates as properties on the artifacts.
     */
    private void copyArtifactsToDistributionRepo(DistributionCoordinatesResolver coordinates) {
        RepoPath pathToMove = coordinates.artifactPath;
        RepoPath artifactDistributionPath;
        RepoPath propertyWritePath;
        boolean dockerDistributionPathAlreadyExist = false;
        if (coordinates.type.equals(RepoType.Docker)) {
            //Path should point to the manifest - so move the parent (only docker v2 is supported)
            pathToMove = pathToMove.getParent();
            artifactDistributionPath = getDockerImageDistPath(coordinates);
            propertyWritePath = new RepoPathImpl(artifactDistributionPath, MANIFEST_FILENAME);
            //If parent path exists the copy will put it under existingPath/existingPath instead of overriding so adjust
            //the target path to the parent so we override the existing artifact.
            if (getRepoService().exists(artifactDistributionPath) && !artifactDistributionPath.isRoot()) {
                artifactDistributionPath = artifactDistributionPath.getParent();
                dockerDistributionPathAlreadyExist = true;
            }
        } else {
            artifactDistributionPath = getArtifactDistPath(coordinates);
            propertyWritePath = artifactDistributionPath;
        }
        copyToPathAndSetProperties(coordinates, pathToMove, artifactDistributionPath, propertyWritePath,
                dockerDistributionPathAlreadyExist);
    }

    private void copyToPathAndSetProperties(DistributionCoordinatesResolver coordinates, RepoPath pathToCopy,
            RepoPath artifactoryDistributionPath, RepoPath propertyWritePath,
            boolean dockerDistributionPathAlreadyExist) {
        status.status("Copying artifact " + pathToCopy + " to distribution repository under path "
                + artifactoryDistributionPath, log);
        if (pathToCopy != null && artifactoryDistributionPath != null) {
            String sourcePath = pathToCopy.toPath();
            String targetPath = artifactoryDistributionPath.toPath();
            if (sourcePath.equals(targetPath) || dockerDistributionPathAlreadyExist) {
                if (RepoType.Docker.equals(coordinates.type)) {
                    targetPath += PathUtils.getLastPathElement(sourcePath);
                }
                status.debug("Artifact was re-distributed (same source '" + sourcePath + "' and target '" + targetPath +
                        "' paths given to copy operation), skipping copy of distributed artifact.", log);
            } else {
                copyPathToDistRepo(pathToCopy, artifactoryDistributionPath);
            }
            writeCoordinateProperties(coordinates, sourcePath, propertyWritePath);
        }
    }

    private void copyPathToDistRepo(RepoPath sourcePath, RepoPath targetPath) {
        BasicStatusHolder copyStatus = getRepoService().copy(sourcePath, targetPath, false, true, false);
        if (copyStatus.hasErrors()) {
            copyStatus.getErrors()
                    .forEach(err -> status.error(sourcePath.toPath(), err.getMessage(), err.getStatusCode(), log));
        } else if (copyStatus.hasWarnings()) {
            copyStatus.getWarnings()
                    .forEach(warn -> status.warn(sourcePath.toPath(), warn.getMessage(), warn.getStatusCode(), log));
        }
    }

    private void writeCoordinateProperties(DistributionCoordinatesResolver coordinates, String sourcePath,
            RepoPath targetPath) {
        status.status(sourcePath, "Setting target distribution coordinates as properties on path " + targetPath, log);
        if (ContextHelper.get().beanForType(AuthorizationService.class).canAnnotate(targetPath)) {
            writeBintrayCoordinatesProps(sourcePath, targetPath, coordinates);
        } else {
            status.warn(sourcePath, "Can't write Bintray coordinates on artifact " + targetPath + ": user lacks " +
                    "annotation permission on path.", HttpStatus.SC_FORBIDDEN, log);
        }
    }

    /**
     * Writes the coordinates the artifact was deployed to in Bintray (represented by it's {@param coordinates}) to
     * the path it was copied to in Artifactory (represented by {@param pathToWrite})
     */
    private void writeBintrayCoordinatesProps(String sourcePath, RepoPath pathToWrite,
            DistributionCoordinatesResolver coordinates) {
        BintrayParams btParams = new BintrayParams();
        btParams.setRepo(coordinates.getRepo());
        btParams.setPackageId(coordinates.getPkg());
        btParams.setVersion(coordinates.getVersion());
        btParams.setPath(coordinates.getPath());
        btParams.setPackageType(coordinates.type.name());
        try {
            ContextHelper.get().beanForType(BintrayService.class).savePropertiesOnRepoPath(pathToWrite, btParams);
        } catch (Exception e) {
            handleException(sourcePath, "Error setting Bintray coordinates as properties on path "
                    + pathToWrite.toPath(), e, status, log);
        }
    }

    private InputStream getArtifactInputStream(DistributionCoordinatesResolver coordinates) throws RepoRejectException {
        RepoPath path = coordinates.artifactPath;
        if (getXrayAddon().isDownloadBlocked(path)) {
            throw new RepoRejectException("Repository '" + path.getRepoKey().replaceAll("-cache", "")
                    + "' blocked the transfer of artifact '" + path.getPath() + " due to its Xray blocking policy.",
                    HttpStatus.SC_FORBIDDEN);
        }
        return getRepoService().getResourceStreamHandle(path).getInputStream();
    }

    RepoPath getArtifactDistPath(DistributionCoordinatesResolver coordinates) {
        return RepoPathFactory.create(repoDescriptor.getKey(),
                Joiner.on("/").join(coordinates.getRepo(), coordinates.getPath()));
    }

    RepoPath getDockerImageDistPath(DistributionCoordinatesResolver coordinates) {
        // Changed on purpose to maintain consistency
        String dockerRepo = getDockerRepositoryToken(coordinates);
        String dockerManifest = getDockerTagToken(coordinates);
        return RepoPathFactory.create(repoDescriptor.getKey(),
                Joiner.on("/").join(coordinates.getRepo(), dockerRepo, dockerManifest));
    }

    private RepositoryService getRepoService() {
        return ContextHelper.get().beanForType(RepositoryService.class);
    }

    private XrayAddon getXrayAddon() {
        return ContextHelper.get().beanForType(AddonsManager.class).addonByType(XrayAddon.class);
    }

    private String getDockerRepositoryToken(DistributionCoordinatesResolver coordinates) {
        String dockerRepositoryToken;
        try {
            dockerRepositoryToken = DistributionUtils.getTokenValueByPropKey(coordinates, "docker.repoName");
        } catch (ItemNotFoundRuntimeException e) {
            dockerRepositoryToken = coordinates.getPkg();
        }
        return dockerRepositoryToken;
    }

    private String getDockerTagToken(DistributionCoordinatesResolver coordinates) {
        String dockerTagToken;
        try {
            dockerTagToken = DistributionUtils.getTokenValueByPropKey(coordinates, "docker.manifest");
        } catch (ItemNotFoundRuntimeException e) {
            dockerTagToken = coordinates.getVersion();
        }
        return dockerTagToken;
    }
}
