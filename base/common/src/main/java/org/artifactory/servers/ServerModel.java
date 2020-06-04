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

package org.artifactory.servers;

import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * @author Rotem Kfir
 */
@Data
@Accessors(fluent = true)
public class ServerModel {

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("service_id")
    private String serviceId;

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty
    private String url;

    @JsonProperty
    private String version;

    @JsonProperty("last_heartbeat")
    private long lastHeartbeat;

    @JsonProperty("heartbeat_stale")
    private boolean heartbeatStale;

    @JsonProperty("start_time")
    private long startTime;

    @JsonProperty
    private ServerStatus status;

    @JsonProperty("status_details")
    private String statusDetails;


    public enum ServerStatus {
        ONLINE("Online"), PARTIALLY_ONLINE("Partially Online"), DOWN("Down");

        private String displayName;

        ServerStatus(String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return displayName;
        }

        @JsonCreator
        public static ServerStatus fromValue(String value) {
            for (ServerStatus status : values()) {
                if (status.getDisplayName().equals(value)) {
                    return status;
                }
            }
            return null;
        }
    }
}
