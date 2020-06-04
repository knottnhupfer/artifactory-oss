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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tree;

import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RestTreeNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;

/**
 * @author Chen Keinan
 */
@Component()
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BrowseTreeNodesService implements RestService<RestTreeNode> {

    @Override
    public void execute(ArtifactoryRestRequest<RestTreeNode> request, RestResponse response) {
        boolean isCompact = Boolean.parseBoolean(request.getQueryParamByKey("compacted")) &&
                ConstantValues.treebrowserFolderCompact.getBoolean();
        ContinueResult<? extends RestModel> items = request.getImodel().fetchItemTypeData(isCompact);
        if (isArchiveRequest(request)) {
            StreamingOutput streamingOutput = os -> write(items, os);
            response.iModel(streamingOutput);
            return;
        }
        response.iModel(items);
    }

    void write(ContinueResult<? extends RestModel> items, OutputStream os) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
        ObjectWriter jsonObjectWriter = new ObjectMapper()
                .setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .setAnnotationIntrospector(new JacksonAnnotationIntrospector())
                .writer();
        jsonObjectWriter.writeValue(writer, items);
    }

    boolean isArchiveRequest(ArtifactoryRestRequest<RestTreeNode> request) {
        return request.getRequest().getMethod().equalsIgnoreCase(HttpMethod.POST) &&
                request.getImodel().isArchiveExpendRequest();
    }
}