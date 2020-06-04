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

package org.artifactory.request.range;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.DateUtils;

import java.util.Date;

/**
 * The class isolates the HTTP If-Range behaviour
 *
 * @author Gidi Shabat
 */
public class IfRangeSelector {

    private String ifRange;
    private String actualSha1;
    private String headerEtag;
    private long actualLastModified;
    private long headerEtagLastModified;

    /**
     * this Constructor is being used in case that the If-Range header does not exist
     */
    public IfRangeSelector() {
    }

    /**
     * this Constructor is being used in case that the If-Range header contains ETag
     */
    public IfRangeSelector(String headerEtag, String actualSha1) {
        this.ifRange = headerEtag;
        this.headerEtag = headerEtag;
        this.actualSha1 = actualSha1;
    }

    /**
     * this Constructor is being used in case that the If-Range header contains modification date
     */
    public IfRangeSelector(String ifRange, long headerEtagLastModified, long actualLastModified) {
        this.ifRange = ifRange;
        this.headerEtagLastModified = headerEtagLastModified;
        this.actualLastModified = actualLastModified;
    }

    /**
     * Creates  IfRangeHandler instance according to the If-Range header existence and value
     */
    public static IfRangeSelector constructIfRange(String ifRange, long lastModified, String sha1) {
        if (StringUtils.isBlank(ifRange)) {
            return new IfRangeSelector();
        }
        Date date = DateUtils.parseDate(ifRange);
        if (date == null) {
            return new IfRangeSelector(ifRange, sha1);
        } else {
            return new IfRangeSelector(ifRange, date.getTime(), lastModified);
        }
    }

    /**
     * Returns true if the header If-Range exist and the file has been changes according to one of the following
     * 1. In case that the If-Range contains date then: A file has been changed if its modification date is after the modification date in the If-Range header
     * 2. In case that the If-Range contains ETag then: A file has been changed if the ETAg does not start with the sha1.
     */
    public boolean isIfChange() {
        if (StringUtils.isBlank(ifRange)) {
            return false;
        }
        if (headerEtag != null) {
            return !headerEtag.startsWith(actualSha1);
        } else {
            // Compare second since HTTP doesn't support milli seconds
            return headerEtagLastModified / 1000 < actualLastModified / 1000;
        }
    }
}
