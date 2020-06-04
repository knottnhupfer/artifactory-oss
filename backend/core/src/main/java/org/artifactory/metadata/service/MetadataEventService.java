package org.artifactory.metadata.service;

import org.artifactory.api.repo.Async;
import org.artifactory.event.work.NodeEventOperator;
import org.artifactory.spring.ReloadableBean;
import org.jfrog.metadata.client.MetadataClient;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * A business service that handles Metadata Events. Responsible for starting the event pipeline and initializing
 * the {@link MetadataClient}. Additionally, this service implements the {@link NodeEventOperator} which provides
 * the logic for handling Metadata events.
 *
 * @author Uriah Levy
 */
public interface MetadataEventService extends NodeEventOperator, ReloadableBean {

    MetadataClient getMetadataClient();

    @Async
    void reindexAsync(List<String> reindexPaths);

    Response reindexSync(List<String> reindexPaths);

    String getMigrationCursorId();

    String getEventPipelineCursorId();
}
