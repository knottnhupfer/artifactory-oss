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

package org.artifactory.storage.db.build.entity;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

/**
 * Date: 10/30/12
 * Time: 12:21 PM
 *
 * @author freds
 */
public class BuildEntity extends BuildEntityRecord {

    public BuildEntity(long buildId, String buildName, String buildNumber, long buildDate, String ciUrl, long created,
            String createdBy, long modified, String modifiedBy) {
        super( buildId, buildName, buildNumber, buildDate, ciUrl, created, createdBy, modified, modifiedBy);
    }

    public BuildEntity(long buildId, String buildName, String buildNumber, long buildDate, String ciUrl, long created,
            String createdBy, long modified, String modifiedBy,
          Set<BuildProperty> properties,
          SortedSet<BuildPromotionStatus> promotions) {
        this(   new BuildEntityRecord(
                    buildId, buildName, buildNumber, buildDate, ciUrl, created, createdBy, modified, modifiedBy
                ),
                properties,
                promotions);
    }

    public BuildEntity(BuildEntityRecord record,
            Set<BuildProperty> properties,
            SortedSet<BuildPromotionStatus> promotions) {
        super( record );
        this.properties = ImmutableSet.copyOf(properties);
        this.promotions = ImmutableSortedSet.copyOf(promotions);
    }

    /**
     * Initialized as null, and can (and should) be set only once
     */
    private ImmutableSet<BuildProperty> properties = null;

    /**
     * Initialized as null, and can (and should) be set only once
     */
    private ImmutableSortedSet<BuildPromotionStatus> promotions = null;

    public ImmutableSortedSet<BuildPromotionStatus> getPromotions() {
        if (promotions == null) {
            throw new IllegalStateException("Build object was not initialized correctly! Promotions missing.");
        }
        return promotions;
    }

    public void setPromotions(Collection<BuildPromotionStatus> promotions) {
        if (this.promotions != null) {
            throw new IllegalStateException("Cannot set Promotions already set!");
        }
        if (promotions == null) {
            throw new IllegalArgumentException("Cannot set promotions to null");
        }
        this.promotions = ImmutableSortedSet.copyOf(promotions);
    }

    public ImmutableSet<BuildProperty> getProperties() {
        if (properties == null) {
            throw new IllegalStateException("Build object was not initialized correctly! Properties missing.");
        }
        return properties;
    }

    public void setProperties(Collection<BuildProperty> properties) {
        if (this.properties != null) {
            throw new IllegalStateException("Cannot set Properties already set!");
        }
        if (properties == null) {
            throw new IllegalArgumentException("Cannot set properties to null");
        }
        this.properties = ImmutableSet.copyOf(properties);
    }

    public boolean isIdentical(BuildEntity b) {
        ImmutableSet<BuildProperty> oprops = b.getProperties();
        if (oprops.size() != getProperties().size()) {
            return false;
        }
        for (BuildProperty prop : properties) {
            boolean foundIdentical = false;
            for (BuildProperty oprop : oprops) {
                if (oprop.isIdentical(prop)) {
                    foundIdentical = true;
                }
            }
            if (!foundIdentical) {
                return false;
            }
        }
        return Objects.equal(promotions, b.promotions)
                && StringUtils.equals(buildName, b.buildName)
                && StringUtils.equals(buildNumber, b.buildNumber)
                && buildDate == b.buildDate
                && StringUtils.equals(ciUrl, b.ciUrl)
                && created == b.created
                && StringUtils.equals(createdBy, b.createdBy)
                && modified == b.modified
                && StringUtils.equals(modifiedBy, b.modifiedBy);
    }
}
