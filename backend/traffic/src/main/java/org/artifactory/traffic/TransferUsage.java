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

package org.artifactory.traffic;

/**
 * @author Lior Azar
 */
public class TransferUsage {

    private long download; // downloaded bytes (without the excluded ips)
    private long xrayDownload; // downloaded bytes by xray (without the excluded ips)
    private long upload;    // uploaded bytes (without the excluded ips)
    private long xrayUpload; // uploaded bytes by xray (without the excluded ips)
    private long redirect;    // redirected bytes (without the excluded ips)
    private long excludedDownload;  // excluded download traffic in bytes
    private long excludedXrayDownload; // excluded xray download traffic in bytes
    private long excludedUpload; // excluded upload traffic in bytes
    private long excludedXrayUpload; // excluded xray upload traffic in bytes
    private long excludedRedirect; // excluded redirect traffic in bytes


    public long getDownload() {
        return download;
    }

    public long getXrayDownload() {
      return xrayDownload;
    }

    public long getUpload() {
        return upload;
    }

    public long getXrayUpload() {
        return xrayUpload;
    }

    public long getRedirect() {
        return redirect;
    }

    public long getExcludedDownload() {
        return excludedDownload;
    }

    public long getExcludedXrayDownload() {
        return excludedXrayDownload;
    }

    public long getExcludedUpload() {
        return excludedUpload;
    }

    public long getExcludedXrayUpload() {
        return excludedXrayUpload;
    }

    public long getExcludedRedirect() {
        return excludedRedirect;
    }

    public void setDownload(long download) {
        this.download = download;
    }

    public void setXrayDownload(long xrayDownload) {
        this.xrayDownload = xrayDownload;
    }

    public void setUpload(long upload) {
        this.upload = upload;
    }

    public void setXrayUpload(long xrayUpload) {
        this.xrayUpload = xrayUpload;
    }

    public void setRedirect(long redirect) {
        this.redirect = redirect;
    }

    void setExcludedDownload(long excludedDownload) {
        this.excludedDownload = excludedDownload;
    }

    public void setExcludedXrayDownload(long excludedXrayDownload) {
        this.excludedXrayDownload = excludedXrayDownload;
    }

    void setExcludedUpload(long excludedUpload) {
        this.excludedUpload = excludedUpload;
    }

    public void setExcludedXrayUpload(long excludedXrayUpload) {
        this.excludedXrayUpload = excludedXrayUpload;
    }

    void setExcludedRedirect(long excludedRedirect) {
        this.excludedRedirect = excludedRedirect;
    }
}
