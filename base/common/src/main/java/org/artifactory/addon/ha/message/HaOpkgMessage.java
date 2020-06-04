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

package org.artifactory.addon.ha.message;

import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Set;

/**
 * @author Dan Feldman
 */
@JsonTypeName("opkgEvent")
public class HaOpkgMessage extends HaBaseMessage implements HaMessage {

    public static final String HA_FAILED_MSG = "Failed to send Opkg calculation message to server";

    private Set<OpkgEventCalculationRestModel> newEvents;
    private boolean async;

    public HaOpkgMessage() {
        super("");
    }

    public HaOpkgMessage(Set<OpkgEventCalculationRestModel> newEvents, boolean async, String publishingMemberId) {
        super(publishingMemberId);
        this.newEvents = newEvents;
        this.async = async;
    }

    public Set<OpkgEventCalculationRestModel> getNewEvents() {
        return newEvents;
    }

    public boolean isAsync() {
        return async;
    }
}
