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
public class HaAclMessageWorkItem extends HaMessageWorkItem {

    private String topicName = HaMessageTopic.ACL_CHANGE_TOPIC.topicName();
    private HaMessage haMessage;

    public HaAclMessageWorkItem(HaMessage haMessage) {
        this.haMessage = haMessage;
    }

    @Override
    public HaMessage getMessage() {
        return haMessage;
    }

    @Override
    public HaMessageTopic getTopic() {
        return HaMessageTopic.ACL_CHANGE_TOPIC;
    }

    // equals and hashcode are always the topic name because we want to aggregate the reloadLicense requests and do it once.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HaAclMessageWorkItem that = (HaAclMessageWorkItem) o;

        return topicName != null ? topicName.equals(that.topicName) : that.topicName == null;
    }

    @Override
    public int hashCode() {
        return topicName != null ? topicName.hashCode() : 0;
    }

    @Nonnull
    @Override
    public String getUniqueKey() {
        return topicName;
    }
}
