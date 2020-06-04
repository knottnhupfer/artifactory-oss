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

package org.artifactory.storage.fs.lock.aop;

import org.artifactory.sapi.common.Lock;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcher;

import java.lang.reflect.Method;

/**
 * Date: 8/5/11
 * Time: 12:17 PM
 *
 * @author Fred Simon
 */
public class LockingMethodMatcher extends StaticMethodMatcher {

    public LockingMethodMatcher() {
    }

    @Override
    public boolean matches(Method method, Class targetClass) {
        if (method.isAnnotationPresent(Lock.class)) {
            return true;
        }
        // The method may be on an interface, so let's check on the target class as well.
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        if (specificMethod != method && (specificMethod.isAnnotationPresent(Lock.class))) {
            System.err.println("FOUND ONLY IN SPECIFIC METHOD FOR " + method.toGenericString());
            return true;
        }
        return false;
    }
}
