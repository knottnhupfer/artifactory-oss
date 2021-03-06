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
public class HaOpkgMessageWorkItem extends HaMessageWorkItem {

    private String topicName = HaMessageTopic.CALCULATE_OPKG.topicName();
    private HaMessage haMessage;
    private final String uniqueKey;


    public HaOpkgMessageWorkItem(HaMessage haMessage) {
        this.haMessage = haMessage;
        this.uniqueKey = getClass().getName() + ":" + System.currentTimeMillis() + System.nanoTime() + ":" + Math.random();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HaOpkgMessageWorkItem that = (HaOpkgMessageWorkItem) o;

        if (topicName != null ? !topicName.equals(that.topicName) : that.topicName != null) {
            return false;
        }
        return uniqueKey != null ? uniqueKey.equals(that.uniqueKey) : that.uniqueKey == null;
    }

    @Override
    public int hashCode() {
        int result = topicName != null ? topicName.hashCode() : 0;
        result = 31 * result + (uniqueKey != null ? uniqueKey.hashCode() : 0);
        return result;
    }

    @Override
    public HaMessage getMessage() {
        return haMessage;
    }

    @Override
    public HaMessageTopic getTopic() {
        return HaMessageTopic.CALCULATE_OPKG;
    }

    @Nonnull
    @Override
    public String getUniqueKey() {
        return topicName;
    }
}
