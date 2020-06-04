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

package org.artifactory.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 8/4/11
 * Time: 9:30 PM
 *
 * @author Fred Simon
 */
public abstract class BasicFactory {
    private static final Logger log = LoggerFactory.getLogger(BasicFactory.class);

    public static <T> T createInstance(Class<T> clazz, String className) {
        T result = null;
        try {
            Class<T> cls =
                    (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(className);
            result = cls.newInstance();
        } catch (Exception e) {
            log.error("Could not create the default factory object due to:" + e.getMessage(), e);
        }
        return result;
    }
}
