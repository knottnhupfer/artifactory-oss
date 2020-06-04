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

package org.artifactory.repo.service.trash;

import com.google.common.collect.Sets;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aggregates and polls all the items when needing to determine if a path can be sent to trash
 *
 * @author Shay Yaakov
 */
@Service
@Reloadable(beanClass = NonTrashableItems.class, initAfter = DbService.class, listenOn = CentralConfigKey.none)
public class NonTrashableItemsImpl implements NonTrashableItems, BeanNameAware, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(NonTrashableItemsImpl.class);

    private ArtifactoryApplicationContext context;
    private Set<NonTrashableItem> trashables = Sets.newHashSet();
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
        Collection<NonTrashableItem> allTrashablesIncludingMe = context.beansForType(NonTrashableItem.class).values();
        Object thisAsBean = context.getBean(beanName);
        trashables.addAll(allTrashablesIncludingMe.stream()
                .filter(nonTrashableItem -> nonTrashableItem != thisAsBean).collect(Collectors.toList()));
        log.debug("Loaded trashables: {}", trashables);
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
    public boolean skipTrash(LocalRepoDescriptor descriptor, String path) {
        for (NonTrashableItem nonTrashableItem : trashables) {
            if (nonTrashableItem.skipTrash(descriptor, path)) {
                return true;
            }
        }

        return false;
    }
}
