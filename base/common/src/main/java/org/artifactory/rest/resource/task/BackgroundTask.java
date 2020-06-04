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

package org.artifactory.rest.resource.task;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.time.format.ISODateTimeFormat;

import static org.artifactory.api.rest.search.result.LastDownloadRestResult.toIsoDateString;

/**
 * Data object to hold background tasks data. Translates into JSON string.
 *
 * @author Yossi Shaul
 */
public class BackgroundTask {

    private String id;
    private String type;
    private String state;
    private String description;
    /**
     * Start time in ISO8601 format
     */
    private String started;
    private String nodeId;

    @JsonCreator
    private BackgroundTask() {
    }

    /**
     * Time the task has started
     */
    public BackgroundTask(String id, String type, String state, String description, long started) {
        this.id = id;
        this.type = type;
        this.state = state;
        this.description = description;
        if (started > 0) {
            this.started = toIsoDateString(started);
        }
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

    public String getStarted() {
        return started;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @JsonIgnore
    public long getStartedMillis() {
        //copied from RestUtils
        return started == null ? 0 : ISODateTimeFormat.dateTime().parseMillis((started));
    }

    @Override
    public String toString() {
        return "BackgroundTask{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", state='" + state + '\'' +
                ", started='" + started + '\'' +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }
}
