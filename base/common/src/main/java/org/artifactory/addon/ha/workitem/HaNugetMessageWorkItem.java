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

package org.artifactory.addon.ha.workitem;

import org.artifactory.addon.ha.message.HaMessage;
import org.artifactory.addon.ha.message.HaMessageTopic;

import javax.annotation.Nonnull;

/**
 * @author Shay Bagants
 */
public class HaNugetMessageWorkItem extends HaMessageWorkItem {

    private String topicName = HaMessageTopic.NUPKG_TOPIC.topicName();
    private HaMessage haMessage;

    public HaNugetMessageWorkItem(HaMessage haMessage) {
        this.haMessage = haMessage;
    }

    @Override
    public HaMessage getMessage() {
        return haMessage;
    }

    @Override
    public HaMessageTopic getTopic() {
        return HaMessageTopic.NUPKG_TOPIC;
    }

    // we compare the ha message itself. The equals and hashcode matches, however, they are not the same as uniqueKey,
    // this is because we want to aggregate the nuget events so only single thread will work on the same type of events,
    // but we don't want that the workQueue will think that multiple events are the same files and will clean them
    // from the queue because it will think that these are duplications. See HaMessageWorkItem javadoc
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HaNugetMessageWorkItem that = (HaNugetMessageWorkItem) o;

        if (topicName != null ? !topicName.equals(that.topicName) : that.topicName != null) {
            return false;
        }
        return haMessage != null ? haMessage.equals(that.haMessage) : that.haMessage == null;
    }

    @Override
    public int hashCode() {
        int result = topicName != null ? topicName.hashCode() : 0;
        result = 31 * result + (haMessage != null ? haMessage.hashCode() : 0);
        return result;
    }

    @Nonnull
    @Override
    public String getUniqueKey() {
        return topicName;
    }
}
