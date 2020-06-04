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

package org.artifactory.storage.db.util.blob;

import org.artifactory.api.jackson.JacksonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.iostreams.streams.in.OutputToInputStream;
import org.jfrog.storage.wrapper.BlobWrapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A blob wrapper that lazily creates an input stream out of an Object using JSON serialization.
 *
 * @author freds
 */
class JsonBlobWrapper extends BlobWrapper {
    JsonBlobWrapper(Object jsonObject) {
        super(new OutputToInputStream() {
            @Override
            protected void write(OutputStream outputStream) throws IOException {
                try (JsonGenerator jsonGenerator = JacksonFactory.createJsonGenerator(outputStream)) {
                    jsonGenerator.writeObject(jsonObject);
                }
            }
        });
    }
}
