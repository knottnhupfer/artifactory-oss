package org.artifactory.event.priority.service.model;

import org.artifactory.mapper.ArtifactoryCentralMapperConfig;
import org.artifactory.storage.db.event.entity.StoragePrioritizedNodeEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Uriah Levy
 */
@Mapper(config = ArtifactoryCentralMapperConfig.class, componentModel = "spring")
public interface PrioritizedNodeEventMapper {

    StoragePrioritizedNodeEvent toStorageEvent(PrioritizedNodeEvent event);

    @Mapping(target = "status", ignore = true)
    PrioritizedNodeEvent toEvent(StoragePrioritizedNodeEvent event);
}
