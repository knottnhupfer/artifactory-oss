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

package org.artifactory.info;

import com.google.common.collect.Lists;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ha.HaNodeProperties;
import org.jfrog.support.common.core.collectors.system.info.BasePropInfoGroup;
import org.jfrog.support.common.core.collectors.system.info.InfoObject;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Yoav Luft
 */
public class HaPropInfo extends BasePropInfoGroup {
    @Override
    public boolean isInUse() {
        return ArtifactoryHome.get().isHaConfigured();
    }

    @Override
    public InfoObject[] getInfo(boolean isSupportBundles) {
        if (ArtifactoryHome.get().isHaConfigured()) {
            HaNodeProperties haNodeProperties = ArtifactoryHome.get().getHaNodeProperties();
            if (haNodeProperties != null) {
                Properties nodeProps = haNodeProperties.getProperties();
                List<InfoObject> infoObjects = Lists.newArrayList();
                for (Map.Entry<Object, Object> prop : nodeProps.entrySet()) {
                    InfoObject infoObject = new InfoObject(prop.getKey().toString(), prop.getValue().toString());
                    infoObjects.add(infoObject);
                }
                return  infoObjects.toArray(new InfoObject[infoObjects.size()]);
            }
        }
        // else
        return new InfoObject[0];
    }

    @Override
    protected String getGroupName() {
        return "High Availability Node Info";
    }
}
