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

package org.artifactory.test;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.mime.MimeTypes;

import java.io.File;

/**
 * A stub of {@link ArtifactoryHome} for testing.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryHomeStub extends ArtifactoryHome {

    public ArtifactoryHomeStub() {
        this("./target/test/testhome");
    }
    public ArtifactoryHomeStub(String path) {
        super(new File(path));
        setArtifactorySystemProperties(new ArtifactorySystemProperties());
    }

    public void setMimeTypes(MimeTypes mimeTypes) {
        super.setMimeTypes(mimeTypes);
    }

    /**
     * Load artifactory system properties from java system properties and set on this configuration.
     */
    public ArtifactoryHomeStub loadSystemProperties() {
        ArtifactorySystemProperties props = new ArtifactorySystemProperties();
        props.loadArtifactorySystemProperties(null, null);
        setArtifactorySystemProperties(props);
        return this;
    }

    public ArtifactoryHomeStub bind() {
        ArtifactoryHome.bind(this);
        return this;
    }

    public ArtifactoryHomeStub setProperty(ConstantValues constant, String value) {
        return setProperty(constant.getPropertyName(), value);
    }

    public ArtifactoryHomeStub setProperty(String key, String value) {
        getArtifactoryProperties().setProperty(key, value);
        return this;
    }

    private void setArtifactorySystemProperties(ArtifactorySystemProperties props) {
        TestUtils.setField(this, "artifactorySystemProperties", props);
    }
}
