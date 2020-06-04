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

package org.artifactory.aql.action;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.aql.model.AqlActionEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.result.AqlRestResult.Row;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.descriptor.property.Property;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static org.artifactory.aql.action.AqlActionException.Reason.*;

/**
 * Updates the property for a given row
 *
 * @author Dan Feldman
 */
public class AqlUpdateItemPropertyAction implements AqlPropertyAction {
    private static final Logger log = LoggerFactory.getLogger(AqlUpdateItemPropertyAction.class);

    private boolean dryRun = true;
    private List<String> propKeys = Lists.newArrayList();
    private String newValue;

    @Override
    public Row doAction(Row row) throws AqlActionException {
        //Should also be verified in query construction phase but just in case.
        if (!AqlDomainEnum.properties.equals(row.getDomain())) {
            String msg = "Skipping delete action for row, only properties domain is supported - row has domain: "
                    + row.getDomain();
            log.debug(msg);
            throw new AqlActionException(msg, UNSUPPORTED_FOR_DOMAIN);
        }
        if (propKeys.isEmpty()) {
            String msg = "Skipping update property action for row, missing required property key inclusion";
            log.debug(msg);
            throw new AqlActionException(msg, UNEXPECTED_CONTENT);
        }
        // Because this action is used with properties domain rows are in the inflated rows 'items' field
        if (StringUtils.isBlank(row.itemRepo) && StringUtils.isBlank(row.itemPath) && StringUtils.isBlank(row.itemName)) {
            if (row.items == null) {
                throw new AqlActionException("Cannot resolve artifact path from given row.",
                        AqlActionException.Reason.UNEXPECTED_CONTENT);
            }
            for (Row item : row.items) {
                updateRowProperties(AqlUtils.fromAql(item));
            }
        } else {
            updateRowProperties(AqlUtils.fromAql(row));
        }
        return row;
    }

    private void updateRowProperties(RepoPath itemPath) throws AqlActionException {
        if (itemPath == null) {
            throw new AqlActionException("Cannot resolve artifact path from given row.",
                    AqlActionException.Reason.UNEXPECTED_CONTENT);
        }
        try {
            propKeys.stream()
                    .filter(Objects::nonNull)
                    .map(Property::new)
                    .forEach(prop -> getPropsService().editProperty(itemPath, null, prop, true, newValue));
        } catch (Exception e) {
            log.debug("", e);
            throw new AqlActionException("Failed to update properties '" + propKeys + "' for path " + itemPath
                    + " with new value '" + newValue +"'. Check the log for more info.", ACTION_FAILED);
        }
    }

    @Override
    public String getName() {
        return AqlActionEnum.updateProperty.name;
    }

    @Override
    public boolean supportsDomain(AqlDomainEnum domain) {
        return AqlDomainEnum.properties.equals(domain);
    }

    @Override
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public boolean isDryRun() {
        return dryRun;
    }

    @Override
    public List<String> getKeys() {
        return propKeys;
    }

    @Override
    public void addKey(String key) {
        propKeys.add(key);
    }

    @Override
    public String getValue() {
        return newValue;
    }

    @Override
    public void setValue(String newValue) {
        this.newValue = newValue;
    }

    private PropertiesService getPropsService() {
        return ContextHelper.get().beanForType(PropertiesService.class);
    }
}
