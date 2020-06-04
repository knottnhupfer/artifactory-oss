package org.artifactory.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * @author Uriah Levy
 */
@MapperConfig(
        uses = ArtifactoryBaseMapper.class,
        unmappedTargetPolicy= ReportingPolicy.ERROR
)
public interface ArtifactoryCentralMapperConfig {
}
