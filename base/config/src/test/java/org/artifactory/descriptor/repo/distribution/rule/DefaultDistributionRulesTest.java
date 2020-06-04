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

package org.artifactory.descriptor.repo.distribution.rule;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Shay Yaakov
 */
@Test
public class DefaultDistributionRulesTest {

    public void testDefaultRules() throws Exception {
        List<DistributionRule> defaultRules = DefaultDistributionRules.getDefaultRules();
        assertEquals(defaultRules.size(), 14);
        assertEquals(defaultRules.get(0).getName(), "Bower-default");
        assertEquals(defaultRules.get(1).getName(), "CocoaPods-default");
        assertEquals(defaultRules.get(2).getName(), "Conan-default");
        assertEquals(defaultRules.get(3).getName(), "Debian-default");
        assertEquals(defaultRules.get(4).getName(), "Docker-default");
        assertEquals(defaultRules.get(5).getName(), "Gradle-default");
        assertEquals(defaultRules.get(6).getName(), "Ivy-default");
        assertEquals(defaultRules.get(7).getName(), "Maven-default");
        assertEquals(defaultRules.get(8).getName(), "Npm-default");
        assertEquals(defaultRules.get(9).getName(), "NuGet-default");
        assertEquals(defaultRules.get(10).getName(), "Opkg-default");
        assertEquals(defaultRules.get(11).getName(), "Rpm-default");
        assertEquals(defaultRules.get(12).getName(), "Sbt-default");
        assertEquals(defaultRules.get(13).getName(), "Vagrant-default");
    }

    public void testDefaultProductRules() throws Exception {
        List<DistributionRule> defaultProductRules = DefaultDistributionRules.getDefaultProductRules();
        assertEquals(defaultProductRules.size(), 14);
        assertEquals(defaultProductRules.get(0).getName(), "Bower-product-default");
        assertEquals(defaultProductRules.get(1).getName(), "CocoaPods-product-default");
        assertEquals(defaultProductRules.get(2).getName(), "Conan-product-default");
        assertEquals(defaultProductRules.get(3).getName(), "Debian-product-default");
        assertEquals(defaultProductRules.get(4).getName(), "Docker-product-default");
        assertEquals(defaultProductRules.get(5).getName(), "Gradle-product-default");
        assertEquals(defaultProductRules.get(6).getName(), "Ivy-product-default");
        assertEquals(defaultProductRules.get(7).getName(), "Maven-product-default");
        assertEquals(defaultProductRules.get(8).getName(), "Npm-product-default");
        assertEquals(defaultProductRules.get(9).getName(), "NuGet-product-default");
        assertEquals(defaultProductRules.get(10).getName(), "Opkg-product-default");
        assertEquals(defaultProductRules.get(11).getName(), "Rpm-product-default");
        assertEquals(defaultProductRules.get(12).getName(), "Sbt-product-default");
        assertEquals(defaultProductRules.get(13).getName(), "Vagrant-product-default");
    }
}