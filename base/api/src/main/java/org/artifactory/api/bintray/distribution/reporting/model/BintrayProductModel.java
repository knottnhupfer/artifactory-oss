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

package org.artifactory.api.bintray.distribution.reporting.model;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author Dan Feldman
 */
public class BintrayProductModel {

    public String productName;
    public Boolean created;
    public Set<String> attachedPackages = Sets.newHashSet();

    public BintrayProductModel(String productName) {
        this.productName = productName;
    }

    public void merge(BintrayProductModel productModel) {
        this.attachedPackages.addAll(productModel.attachedPackages);
    }

    public String getProductName() {
        return productName;
    }

    public Boolean getCreated() {
        return created;
    }

    public Set<String> getAttachedPackages() {
        return attachedPackages;
    }
}
