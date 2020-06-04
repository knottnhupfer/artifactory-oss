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

package org.artifactory.addon;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import org.jfrog.storage.binstore.utils.Checksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Don't change public signatures on this class to lower access rights it messes up ProGuard!!
 * @author Gidi Shabat
 */
public class FooterMessage {
    private static final Logger log = LoggerFactory.getLogger(FooterMessage.class);

    private final String message;
    private final String type;
    private final String visibility;
    private final String dismissCode;
    private Long showFrom;
    private Long showUntil;

    //For jackson
    public FooterMessage() {
        message = null;
        type = null;
        visibility = null;
        dismissCode = null;
        showFrom = null;
        showUntil = null;
    }

    private FooterMessage(String message, FooterMessageType type, FooterMessageVisibility visibility) {
        this.message = message;
        this.type = type.name();
        this.visibility = visibility.name();
        this.dismissCode = null;
        this.showFrom = null;
        this.showUntil = null;
    }

    private FooterMessage(String message, FooterMessageType type, FooterMessageVisibility visibility, boolean dismissible) {
        this.message = message;
        this.type = type.name();
        this.visibility = visibility.name();
        this.dismissCode = getDismissCode(message, dismissible);
        this.showFrom = null;
        this.showUntil = null;
    }

    @Builder
    public FooterMessage(String message, String type, String visibility, boolean dismissible, final Long showFrom, final Long showUntil) {
        this.message = message;
        this.type = type;
        this.visibility = visibility;
        this.dismissCode = getDismissCode(message, dismissible);
        this.showFrom = showFrom;
        this.showUntil = showUntil;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getDismissCode() {
        return dismissCode;
    }

    public Long getShowFrom() {
        return showFrom;
    }

    public Long getShowUntil() {
        return showUntil;
    }

    public void clearTimeRange() {
        showFrom = null;
        showUntil = null;
    }

    /**
     * @return true if the time *now* is within the range defined for this message
     */
    @JsonIgnore
    public boolean shouldShowMessage() {
        Date now = new Date();
        if (showFrom != null && !new Date(showFrom).before(now)) {
            //showFrom.before(now) --> we're past showFrom so we should show
            log.debug("show from date '{}' for custom message not yet reached, now is: '{}'", showFrom, now);
            return false;
        } else if (showUntil != null && !new Date(showUntil).after(now)) {
            log.debug("show until date '{}' for custom message has passed, now is: '{}'", showUntil, now);
            return false;
        }
        return true;
    }

    public static FooterMessage createWarning(String message, FooterMessageVisibility visibility) {
        return new FooterMessage(message, FooterMessageType.warning, visibility);
    }

    public static FooterMessage createInfo(String message, FooterMessageVisibility visibility) {
        return new FooterMessage(message, FooterMessageType.info, visibility);
    }

    public static FooterMessage createDismissibleInfo(String message, FooterMessageVisibility visibility) {
        return new FooterMessage(message, FooterMessageType.info, visibility, true);
    }

    public static FooterMessage createError(String message, FooterMessageVisibility visibility) {
        return new FooterMessage(message, FooterMessageType.error, visibility);
    }

    public static FooterMessage createDismissibleError(String message, FooterMessageVisibility visibility) {
        return new FooterMessage(message, FooterMessageType.error, visibility, true);
    }

    enum FooterMessageType {
        info, warning, error
    }

    public enum FooterMessageVisibility {
        admin, user, all;

        public boolean isVisible(boolean adminPermission, boolean userPermission) {
            if (adminPermission) {
                return true;
            } else if (userPermission) {
                return this == user || this == all;
            } else {
                return this == all;
            }
        }

        public static boolean isVisible(String visibilityName, boolean admin, boolean notAnonymous) {
            try {
                FooterMessageVisibility visibility = FooterMessageVisibility.valueOf(visibilityName);
                return visibility.isVisible(admin, notAnonymous);
            } catch (Exception e) {
                return false;
            }
        }
    }

    private String getDismissCode(String msg, boolean dismissible) {
        if (dismissible && isNotBlank(msg)) {
            try {
                //md5 is enough for this
                Checksum msgMd5 = Checksum.md5();
                byte[] msgBytes = msg.getBytes();
                msgMd5.update(msgBytes, 0, msg.length());
                msgMd5.calc();
                return msgMd5.getChecksum();
            } catch (Exception e) {
                log.debug("Error calculating dismiss code for message '" + msg + "' --> ", e);
            }
        }
        return null;
    }
}
