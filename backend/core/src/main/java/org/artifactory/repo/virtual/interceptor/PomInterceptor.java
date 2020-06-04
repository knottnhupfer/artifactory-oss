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

package org.artifactory.repo.virtual.interceptor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.repo.exception.maven.BadPomException;
import org.artifactory.descriptor.repo.PomCleanupPolicy;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.SaveResourceContext;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.repo.virtual.interceptor.transformer.PomTransformer;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.storage.StorageException;
import org.artifactory.util.ExceptionUtils;
import org.jfrog.storage.binstore.exceptions.BinaryRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Intercepts pom resources, transforms the pom according to the policy and saves it to the local storage.
 *
 * @author Eli Givoni
 */
@Component
public class PomInterceptor extends VirtualRepoInterceptorBase {
    private static final Logger log = LoggerFactory.getLogger(PomInterceptor.class);

    private InternalRepositoryService repoService;
    private final Cache<String, String> notModifiedPomsCache;

    @Inject
    public PomInterceptor(InternalRepositoryService repoService) {
        this.repoService = repoService;
        notModifiedPomsCache = CacheBuilder.newBuilder()
                .maximumSize(100_000)
                .expireAfterAccess(12, TimeUnit.HOURS)
                .build();
    }

    @Override
    @Nonnull
    public RepoResource onBeforeReturn(VirtualRepo virtualRepo, InternalRequestContext context, RepoResource resource) {
        // intercept only poms
        if (!MavenNaming.isPom(context.getResourcePath())) {
            return resource;
        }

        PomCleanupPolicy cleanupPolicy = virtualRepo.getPomRepositoryReferencesCleanupPolicy();
        if (cleanupPolicy.equals(PomCleanupPolicy.nothing)) {
            return resource;
        }

        if (isPomNotModifiedCached(resource)) {
            if (log.isTraceEnabled()) {
                log.trace("Cache hit of unmodified pom '{}'", resource.getRepoPath().toPath());
            }
            return resource;
        }

        String transformedPom;
        PomTransformer transformer;
        String transformErrorMsgPrefix = "Failed to transform pom file";
        try {
            String pomAsString = getResourceContentAndBumpStats(context, resource);
            transformer = new PomTransformer(pomAsString, cleanupPolicy);
            transformedPom = transformer.transform();
        } catch (RepoRejectException | BinaryRejectedException rre) {
            log.debug(transformErrorMsgPrefix, rre);
            return new UnfoundRepoResource(resource.getRepoPath(), transformErrorMsgPrefix + ": " + rre.getMessage(),
                    rre.getErrorCode());
        } catch (IOException e) {
            if (ExceptionUtils.getRootCause(e) instanceof BadPomException) {
                log.error("{} : {}", transformErrorMsgPrefix, e.getMessage());
            } else {
                log.debug(transformErrorMsgPrefix, e);
                log.error(e.getMessage());
            }
            return new UnfoundRepoResource(resource.getRepoPath(), transformErrorMsgPrefix + ": " + e.getMessage());
        } catch (StorageException e) {
            log.debug(transformErrorMsgPrefix, e);
            log.error("{} : {}", transformErrorMsgPrefix, e.getMessage());
            return new UnfoundRepoResource(resource.getRepoPath(), transformErrorMsgPrefix + ": " + e.getMessage());
        }

        if (!transformer.isPomChanged()) {
            cacheNotModifiedPom(resource);
            return resource;
        }

        String resourcePath = resource.getResponseRepoPath().getPath();
        RepoPath virtualCachePath = InternalRepoPathFactory.create(virtualRepo.getKey(), resourcePath);
        MutableFileInfo fileInfo = InfoFactoryHolder.get().createFileInfo(virtualCachePath);
        long now = System.currentTimeMillis();
        fileInfo.setCreated(now);
        fileInfo.setLastModified(now);
        fileInfo.createTrustedChecksums();
        fileInfo.setSize(transformedPom.length());
        RepoResource transformedResource = new FileResource(fileInfo);

        try {
            SaveResourceContext saveResourceContext = new SaveResourceContext.Builder(transformedResource,
                    IOUtils.toInputStream(transformedPom, "utf-8")).build();
            transformedResource = repoService.saveResource(virtualRepo, saveResourceContext);
        } catch (IOException e) {
            String message = "Failed to import file to local storage";
            log.error(message, e);
            return new UnfoundRepoResource(resource.getRepoPath(), message + ": " + e.getMessage());
        } catch (RepoRejectException | BinaryRejectedException rre) {
            String message = "Failed to import file to local storage";
            log.debug(message, rre);
            return new UnfoundRepoResource(resource.getRepoPath(), message + ": " + rre.getMessage(),
                    rre.getErrorCode());
        }
        return transformedResource;
    }

    private boolean isPomNotModifiedCached(RepoResource resource) {
        return getResourceCacheKey(resource).filter(s -> notModifiedPomsCache.getIfPresent(s) != null).isPresent();
    }

    private void cacheNotModifiedPom(RepoResource resource) {
        getResourceCacheKey(resource).ifPresent(s -> notModifiedPomsCache.put(s, ""));
    }

    private Optional<String> getResourceCacheKey(RepoResource resource) {
        return Optional.ofNullable(resource.getInfo().getSha1());
    }

    private String getResourceContentAndBumpStats(InternalRequestContext context, RepoResource resource)
            throws IOException, RepoRejectException {
        String repoKey = resource.getResponseRepoPath().getRepoKey();
        Repo repository = repoService.repositoryByKey(repoKey);
        ResourceStreamHandle handle = repoService.getResourceStreamHandle(context, repository, resource);
        InputStream inputStream = handle.getInputStream();
        return streamToStringOrEmpty(inputStream);
    }

    private String streamToStringOrEmpty(InputStream inputStream) throws IOException {
        String pomAsString = "";
        if (inputStream != null) {
            try {
                pomAsString = IOUtils.toString(inputStream, "utf-8");
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        return pomAsString;
    }
}