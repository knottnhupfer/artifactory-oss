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

package org.artifactory.storage.event;

/**
 * Event types of on the nodes tree.
 *
 * @author Yossi Shaul
 */
public enum EventType {
    create(1),  // folder/file was created
    update(2),  // folder/file was updated
    delete(3),  // folder/file was deleted
    props(4);   // folder/file properties got modified (create, update or delete)

    /**
     * The event type code for persistence
     */
    private final int code;

    EventType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static EventType fromCode(int code) {
        switch (code) {
            case 1:
                return create;
            case 2:
                return update;
            case 3:
                return delete;
            case 4:
                return props;
            default:
                throw new IllegalArgumentException("Unknown event type code: " + code);
        }
    }
}
