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

package org.artifactory.storage.db.security.service.access;

import org.artifactory.model.xstream.security.UserImpl;
import org.artifactory.model.xstream.security.UserProperty;
import org.artifactory.security.UserPropertyInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper.matchedValueAndkeySuffix;

/**
 * @author Saffi Hartal
 */
public class UserPropertiesSearchHelperTest {
    @Test
    public void testMatchedValueAndkeySuffix() throws Exception {
        UserImpl user = new UserImpl();
        Set<UserPropertyInfo> props = new HashSet<>();
        user.setUserProperties(props);
        props.add(new UserProperty("hello", "world"));
        Assert.assertFalse(matchedValueAndkeySuffix(user, "basicauth", "found"));
        props.add(new UserProperty("basicauth", "found"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "found"));

        // not found
        Assert.assertFalse(matchedValueAndkeySuffix(user, "basicauth", "notfound"));
        Assert.assertFalse(matchedValueAndkeySuffix(user, "prefix.basicauth", "found"));

        props.clear();
        Assert.assertFalse(matchedValueAndkeySuffix(user, "basicauth", "found"));
        props.add(new UserProperty("prefix.basicauth", "found"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "found"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "prefix.basicauth", "found"));
        props.add(new UserProperty("hello", "world"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "found"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "prefix.basicauth", "found"));

        props.clear();
        props.add(new UserProperty("hello", "world"));
        Assert.assertFalse(matchedValueAndkeySuffix(user, "basicauth", "also"));
        props.add(new UserProperty("basicauth", "found"));
        props.add(new UserProperty("prefix.basicauth", "also"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "also"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "found"));


    }

}