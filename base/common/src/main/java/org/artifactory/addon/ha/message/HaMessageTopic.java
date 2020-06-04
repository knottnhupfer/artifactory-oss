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

/**
 * @author mamo
 */
public enum HaMessageTopic {
    CALCULATE_DEBIAN("calculateDebian"),
    CALCULATE_OPKG("calculateOpkg"),
    OFFLINE_TOPIC("putOffline"),
    CONFIG_CHANGE_TOPIC("configChange"),
    ACL_CHANGE_TOPIC("aclChange"),
    LICENSES_CHANGE_TOPIC("licensesChange"),
    NUPKG_TOPIC("nuPkgChange"),
    WATCHES_TOPIC("watchesChange");

    public final String topicName;

    HaMessageTopic(String topicName) {
        this.topicName = topicName;
    }

    public static HaMessageTopic toHaMessageTopic(String topic){
        for (HaMessageTopic haMessageTopic : values()) {
            if( haMessageTopic.topicName().equals(topic)){
                return haMessageTopic;
            }
        }
        return null;
    }

    public String topicName() {
        return topicName;
    }
}
