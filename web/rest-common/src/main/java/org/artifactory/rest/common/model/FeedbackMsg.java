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

package org.artifactory.rest.common.model;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties({"hasMessages"})
public class FeedbackMsg extends BaseModel {

    String error;
    String warn;
    String info;
    String url;
    List<String> errors;
    boolean hasMessages;

    public boolean hasMessages() {
        return hasMessages;
    }

    public void setHasMessages(boolean hasMessages) {
        this.hasMessages = hasMessages;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        if (StringUtils.isNotBlank(error)) {
            this.hasMessages = true;
            this.error = error;
        }
    }

    @JsonIgnore
    public boolean hasError() {
        return StringUtils.isNotBlank(error);
    }

    public String getWarn() {
        return warn;
    }

    public void setWarn(String warn) {
        if (StringUtils.isNotBlank(warn)) {
            this.hasMessages = true;
            this.warn = warn;
        }
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        if (StringUtils.isNotBlank(info)) {
            this.hasMessages = true;
            this.info = info;
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.hasMessages = true;
        this.errors = errors;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.hasMessages = true;
        this.url = url;
    }
}
