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

package org.artifactory.aql.model;


import org.artifactory.aql.action.AqlAction;
import org.artifactory.aql.action.AqlDeleteItemAction;
import org.artifactory.aql.action.AqlFindAction;
import org.artifactory.aql.action.AqlUpdateItemPropertyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.artifactory.aql.model.AqlDomainEnum.items;
import static org.artifactory.aql.model.AqlDomainEnum.properties;

/**
 * Aql Actions are specific to domains as the action performed defers based on it (i.e. delete item / delete property)
 * An action is performed on the result set returned by the query as it is returned to the caller.
 *
 * @author Dan Feldman
 */
public enum AqlActionEnum {

    // find is actually a special case that supports all domains as is, no need to dupe it for all domains
    find("find", items, AqlFindAction.class),
    deleteItem("delete", items, AqlDeleteItemAction.class),
    updateProperty("update", properties, AqlUpdateItemPropertyAction.class);

    private static final Logger log = LoggerFactory.getLogger(AqlActionEnum.class);

    public String name;
    private AqlDomainEnum domain;
    private Class<? extends AqlAction> action;

    AqlActionEnum(String name, AqlDomainEnum domain, Class<? extends AqlAction> action) {
        this.name = name;
        this.domain = domain;
        this.action = action;
    }

    public static AqlAction getAction(String actionName, AqlDomainEnum domain) {
        actionName = actionName.toLowerCase();
        try {
            // find is actually a special case that supports all domains as is
            if (find.name().equals(actionName)) {
                return find.action.newInstance();
            }
            for (AqlActionEnum value : values()) {
                if (value.name.equals(actionName) && value.domain.equals(domain)) {
                    return value.action.newInstance();
                }
            }
        } catch (IllegalAccessException | InstantiationException e) {
            log.debug("Unsupported action '" + actionName + "' for domain " + domain, e);
        }
        return null;
    }
}
