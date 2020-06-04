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

package org.artifactory.repo.cache.expirable;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.local.IsLocalGenerated;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.db.DbService;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Aggregates and polls all the cache expirable components when needing to determine if a path can expire
 *
 * @author Noam Y. Tenne
 */
@Service
@Reloadable(beanClass = CacheExpiry.class, initAfter = DbService.class, listenOn = CentralConfigKey.none)
public class CacheExpiryImpl implements CacheExpiry, BeanNameAware, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(CacheExpiryImpl.class);

    private ArtifactoryApplicationContext context;
    private Set<CacheExpiryChecker> expirable = Sets.newHashSet();
    private Set<CacheExpiryChecker> localGenerated = Sets.newHashSet();
    private String beanName;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = ((ArtifactoryApplicationContext) applicationContext);
    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void init() {
        Set<CacheExpiryChecker> allExpiryChecker = new HashSet<>(
                context.beansForType(CacheExpiryChecker.class).values());
        expirable = Collections.unmodifiableSet(allExpiryChecker);
        log.debug("Loaded expirable: {}", expirable);
        localGenerated = Collections
                .unmodifiableSet(new HashSet<>(context.beansForType(CacheExpiryChecker.class).values()));
        log.debug("Loaded localGenerated: {}", localGenerated);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public boolean isExpirable(RepoType repoType, String repoKey, @Nonnull String path) {
        if (StringUtils.isNotBlank(path)) {
            for (CacheExpiryChecker cacheExpirable : expirable) {
                if (cacheExpirable.isExpirable(repoType, repoKey, path)) {
                    if (log.isTraceEnabled()) {
                        log.trace("{} Repo {} of type ({}) isExpirable match for path {} ",
                                cacheExpirable.getClass().getSimpleName(), repoKey, repoType, path);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isLocalGenerated(@Nonnull RepoType repoType, @Nonnull String repoKey, @Nonnull String path) {
        // self generated must be expirable for now.
        return isMatchAnyLocalGenerated(this.localGenerated.stream().map(it -> it), repoType, repoKey,
                path);
    }

    static boolean isMatchAnyLocalGenerated(Stream<IsLocalGenerated> localGenerated, RepoType repoType, String repoKey,
            String path) {
        return StringUtils.isNotBlank(path) &&
                localGenerated.peek(it -> {
                    if (log.isTraceEnabled()) { // debug log
                        if (it.isLocalGenerated(repoType, repoKey, path)) {
                            log.trace("{} Repo {} of type ({}) is LocalGenerated match for path {} ",
                                    it.getClass().getSimpleName(), repoKey, repoType, path);
                        }
                    }
                })
                        .anyMatch(it -> it.isLocalGenerated(repoType, repoKey, path));
    }
}
