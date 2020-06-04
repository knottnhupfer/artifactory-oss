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

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author mamo
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HaBaseMessage.class, name = "haBaseEvent"), // for events with no content, e.g. reload config/acls...
        @JsonSubTypes.Type(value = NuPkgAddBaseMessage.class, name = "nugetAddEvent"),
        @JsonSubTypes.Type(value = NuPkgRemoveBaseMessage.class, name = "nugetRemoveEvent"),
        @JsonSubTypes.Type(value = WatchesHaMessage.AddWatch.class, name = "watchesAddEvent"),
        @JsonSubTypes.Type(value = WatchesHaMessage.DeleteAllWatches.class, name = "watchesDeleteAllEvent"),
        @JsonSubTypes.Type(value = WatchesHaMessage.DeleteUserWatches.class, name = "watchesDeleteUserWatchesEvent"),
        @JsonSubTypes.Type(value = WatchesHaMessage.DeleteAllUserWatches.class, name = "watchesDeleteAllUserWatchesEvent"),
        @JsonSubTypes.Type(value = HaOpkgMessage.class, name = "opkgEvent")
})
public interface HaMessage {

    String getPublishingMemberId();

    void setPublishingMemberId(String publishingMemberId);
}
