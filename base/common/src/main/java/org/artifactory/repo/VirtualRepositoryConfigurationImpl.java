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

package org.artifactory.repo;


import com.fasterxml.jackson.annotation.JsonFilter;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.*;

import java.util.List;
import java.util.Map;

import static org.artifactory.descriptor.repo.RepoType.*;
import static org.artifactory.repo.RepoDetailsType.VIRTUAL;

/**
 * Virtual repository configuration
 *
 * @author Tomer Cohen
 * @see org.artifactory.descriptor.repo.VirtualRepoDescriptor
 */
@JsonFilter("typeSpecificFilter")
public class VirtualRepositoryConfigurationImpl extends RepositoryConfigurationBase
        implements VirtualRepositoryConfiguration {

    @IncludeTypeSpecific(repoType = VIRTUAL)
    private List<String> repositories;
    @IncludeTypeSpecific(repoType = VIRTUAL)
    private boolean artifactoryRequestsCanRetrieveRemoteArtifacts = false;
    @IncludeTypeSpecific(packageType = Docker, repoType = VIRTUAL)
    private boolean resolveDockerTagsByTimestamp = false;
    @IncludeTypeSpecific(packageType = {Maven, RepoType.Gradle, RepoType.Ivy, RepoType.SBT, P2}, repoType = VIRTUAL)
    private String keyPair = "";
    @IncludeTypeSpecific(packageType = {Maven, RepoType.Gradle, RepoType.Ivy, RepoType.SBT}, repoType = VIRTUAL)
    private String pomRepositoryReferencesCleanupPolicy = "discard_active_reference";
    @IncludeTypeSpecific(repoType = VIRTUAL)
    private String defaultDeploymentRepo;
    @IncludeTypeSpecific(packageType = {RepoType.Bower, RepoType.Go, RepoType.Npm}, repoType = VIRTUAL)
    private boolean externalDependenciesEnabled = false;
    @IncludeTypeSpecific(packageType = {RepoType.Bower, RepoType.Go, RepoType.Npm}, repoType = VIRTUAL)
    private String externalDependenciesRemoteRepo;
    @IncludeTypeSpecific(packageType = {RepoType.Bower, RepoType.Go, RepoType.Npm}, repoType = VIRTUAL)
    private List<String> externalDependenciesPatterns;
    @IncludeTypeSpecific(packageType = {RepoType.Chef, RepoType.Conda, RepoType.CRAN, RepoType.Conan, Debian, RepoType.Helm,
            RepoType.Npm, RepoType.YUM}, repoType = VIRTUAL)
    private long virtualRetrievalCachePeriodSecs = 600;
    @IncludeTypeSpecific(packageType = Maven, repoType = VIRTUAL)
    private boolean forceMavenAuthentication = false;
    @IncludeTypeSpecific(packageType = Debian, repoType = VIRTUAL)
    private String debianDefaultArchitectures;
    @IncludeTypeSpecific(packageType = Debian, repoType = VIRTUAL)
    private List<String> optionalIndexCompressionFormats;
    @IncludeTypeSpecific(packageType = P2, repoType = VIRTUAL)
    private List<String> p2Urls;

    public VirtualRepositoryConfigurationImpl() {
    }

    public VirtualRepositoryConfigurationImpl(VirtualRepoDescriptor repoDescriptor) {
        super(repoDescriptor, TYPE);
        setArtifactoryRequestsCanRetrieveRemoteArtifacts(repoDescriptor.isArtifactoryRequestsCanRetrieveRemoteArtifacts());
        setResolveDockerTagsByTimestamp(repoDescriptor.isResolveDockerTagsByTimestamp());
        String keyPair = repoDescriptor.getKeyPair();
        if (StringUtils.isNotBlank(keyPair)) {
            setKeyPair(keyPair);
        }
        Map<String, String> pomCleanupPolicies = extractXmlValueFromEnumAnnotations(PomCleanupPolicy.class);
        pomCleanupPolicies.entrySet().stream()
                .filter(pomCleanupPolicy -> pomCleanupPolicy.getKey()
                        .equals(repoDescriptor.getPomRepositoryReferencesCleanupPolicy().name()))
                .forEach(pomCleanupPolicy -> setPomRepositoryReferencesCleanupPolicy(pomCleanupPolicy.getKey()));
        List<RepoDescriptor> repositories = repoDescriptor.getRepositories();
        setRepositories(Lists.transform(repositories, RepoDescriptor::getKey));
        if (repoDescriptor.getDefaultDeploymentRepo() != null) {
            setDefaultDeploymentRepo(repoDescriptor.getDefaultDeploymentRepo().getKey());
        }
        if (repoDescriptor.getExternalDependencies() != null) {
            ExternalDependenciesConfig externalDependencies = repoDescriptor.getExternalDependencies();
            setExternalDependenciesEnabled(externalDependencies.isEnabled());
            if (externalDependencies.getRemoteRepo() != null) {
                setExternalDependenciesRemoteRepo(externalDependencies.getRemoteRepo().getKey());
            }
            setExternalDependenciesPatterns(externalDependencies.getPatterns());
        }
        if (repoDescriptor.getVirtualCacheConfig() != null) {
            setVirtualRetrievalCachePeriodSecs(repoDescriptor.getVirtualCacheConfig().getVirtualRetrievalCachePeriodSecs());
        }
        if (repoDescriptor.getP2() != null) {
            setP2Urls(repoDescriptor.getP2().getUrls());
        }
        setOptionalIndexCompressionFormats(repoDescriptor.getDebianOptionalIndexCompressionFormats());
        setForceMavenAuthentication(repoDescriptor.isForceMavenAuthentication());
        setDebianDefaultArchitectures(repoDescriptor.getDebianDefaultArchitectures());
    }

    @Override
    public String getDebianDefaultArchitectures() {
        return debianDefaultArchitectures;
    }

    public void setDebianDefaultArchitectures(String debianDefaultArchitectures) {
        this.debianDefaultArchitectures = debianDefaultArchitectures;
    }

    @Override
    public boolean isArtifactoryRequestsCanRetrieveRemoteArtifacts() {
        return artifactoryRequestsCanRetrieveRemoteArtifacts;
    }

    public void setArtifactoryRequestsCanRetrieveRemoteArtifacts(
            boolean artifactoryRequestsCanRetrieveRemoteArtifacts) {
        this.artifactoryRequestsCanRetrieveRemoteArtifacts = artifactoryRequestsCanRetrieveRemoteArtifacts;
    }

    @Override
    public boolean isResolveDockerTagsByTimestamp() {
        return resolveDockerTagsByTimestamp;
    }

    public void setResolveDockerTagsByTimestamp(boolean resolveDockerTagsByTimestamp) {
        this.resolveDockerTagsByTimestamp = resolveDockerTagsByTimestamp;
    }

    @Override
    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    public String getPomRepositoryReferencesCleanupPolicy() {
        return pomRepositoryReferencesCleanupPolicy;
    }

    public void setPomRepositoryReferencesCleanupPolicy(String pomRepositoryReferencesCleanupPolicy) {
        this.pomRepositoryReferencesCleanupPolicy = pomRepositoryReferencesCleanupPolicy;
    }

    @Override
    public String getDefaultDeploymentRepo() {
        return defaultDeploymentRepo;
    }

    public void setDefaultDeploymentRepo(String defaultDeploymentRepo) {
        this.defaultDeploymentRepo = defaultDeploymentRepo;
    }

    @Override
    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    @Override
    public boolean isExternalDependenciesEnabled() {
        return externalDependenciesEnabled;
    }

    public void setExternalDependenciesEnabled(boolean externalDependenciesEnabled) {
        this.externalDependenciesEnabled = externalDependenciesEnabled;
    }

    @Override
    public String getExternalDependenciesRemoteRepo() {
        return externalDependenciesRemoteRepo;
    }

    public void setExternalDependenciesRemoteRepo(String externalDependenciesRemoteRepo) {
        this.externalDependenciesRemoteRepo = externalDependenciesRemoteRepo;
    }

    @Override
    public List<String> getExternalDependenciesPatterns() {
        return externalDependenciesPatterns;
    }

    @Override
    public long getVirtualRetrievalCachePeriodSecs() {
        return virtualRetrievalCachePeriodSecs;
    }

    public void setVirtualRetrievalCachePeriodSecs(long virtualRetrievalCachePeriodSecs) {
        this.virtualRetrievalCachePeriodSecs = virtualRetrievalCachePeriodSecs;
    }

    public void setExternalDependenciesPatterns(List<String> externalDependenciesPatterns) {
        this.externalDependenciesPatterns = externalDependenciesPatterns;
    }

    public List<String> getOptionalIndexCompressionFormats() {
        return this.optionalIndexCompressionFormats;
    }

    public void setOptionalIndexCompressionFormats(List<String> optionalIndexCompressionFormats) {
        this.optionalIndexCompressionFormats = optionalIndexCompressionFormats;
    }

    @Override
    public boolean isForceMavenAuthentication() {
        return forceMavenAuthentication;
    }

    public void setForceMavenAuthentication(boolean forceMavenAuthentication) {
        this.forceMavenAuthentication = forceMavenAuthentication;
    }

    public List<String> getP2Urls() {
        return p2Urls;
    }

    public void setP2Urls(List<String> p2Urls) {
        this.p2Urls = p2Urls;
    }
}
