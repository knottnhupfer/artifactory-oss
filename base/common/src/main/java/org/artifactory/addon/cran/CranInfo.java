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

package org.artifactory.addon.cran;

/**
 * @author Inbar Tal
 */
public class CranInfo {

    private String name;
    private String version;
    private String title;
    private String author;
    private String maintainer;
    private String description;
    private String license;
    private String needsCompilation;

    public CranInfo(String name, String version, String title, String author, String maintainer,
            String description, String license, String needsCompilation) {
        this.name = name;
        this.version = version;
        this.title = title;
        this.author = author;
        this.maintainer = maintainer;
        this.description = description;
        this.license = license;
        this.needsCompilation = needsCompilation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getNeedsCompilation() {
        return needsCompilation;
    }

    public void setNeedsCompilation(String needsCompilation) {
        this.needsCompilation = needsCompilation;
    }
}
