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

package org.artifactory.rest.common.service;

import java.lang.annotation.*;

/**
 * @author Chen Keinan
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface IgnoreSpecialFields {
    /**
     *  list of fields define in this method will be ignored during json serialization
     *  the class that have this annotation need to :
     *  1.  override toString by calling this function: JsonUtil.jsonToStringIgnoreSpecialFields(this);
     *  2.  set @JsonFilter("exclude fields") annotation for this class
     *
     *  Note: for conditional ignore special fields
     *  must implements ISpecialFields interface
     * @return list of fields to be ignored on return
     */
    String[] value() default {};
}
