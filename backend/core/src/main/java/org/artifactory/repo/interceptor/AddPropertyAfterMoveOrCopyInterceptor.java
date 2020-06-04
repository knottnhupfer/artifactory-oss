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

package org.artifactory.repo.interceptor;

import com.google.common.collect.Multiset;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.PropertiesAddon;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.property.Property;
import org.artifactory.md.Properties;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.InterceptorMoveCopyContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * Interceptor for adding properties as a post copy or post move operations.
 */
public class AddPropertyAfterMoveOrCopyInterceptor extends StorageInterceptorAdapter {
    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
                          Properties properties, InterceptorMoveCopyContext ctx) {
        super.afterCopy(sourceItem, targetItem, statusHolder, properties, ctx);
        addOrOverrideProperties(targetItem, properties, ctx);
    }

    @Override
    public void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
                          Properties properties, InterceptorMoveCopyContext ctx) {
        super.afterMove(sourceItem, targetItem, statusHolder, properties, ctx);
        addOrOverrideProperties(targetItem, properties, ctx);
    }

    private void addOrOverrideProperties(VfsItem targetItem, Properties properties, InterceptorMoveCopyContext ctx) {
        PropertiesAddon propertiesAddon = addonsManager.addonByType(PropertiesAddon.class);
        if (ctx.isOverrideProperties()) {
            propertiesAddon.setProperties(targetItem.getRepoPath(), properties);
        } else {
            Multiset<String> keys = properties.keys();
            for (String key : keys) {
                Set<String> valuesForKey = properties.get(key);
                Property property = new Property();
                property.setName(key);
                String[] values = new String[valuesForKey.size()];
                valuesForKey.toArray(values);
                propertiesAddon.addProperty(targetItem.getRepoPath(), null, property, values);
            }
        }
    }
}
