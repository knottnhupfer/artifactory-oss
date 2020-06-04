package org.artifactory.ui.rest.service.admin.configuration.servers;

import lombok.Data;
import org.artifactory.servers.ServerModel;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@Data
public class ServersStatusResponse {

    @JsonProperty("nodes")
    List<ServerModel> servers = new ArrayList<>();

    public void add(ServerModel server) {
        servers.add(server);
    }
}
