package org.artifactory.mapper;

import org.jfrog.common.mapper.Validatable;
import org.joda.time.format.ISODateTimeFormat;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * @author Uriah Levy
 */
@Mapper(componentModel = "spring")
@Named("ArtifactoryBaseMapper")
public interface ArtifactoryBaseMapper {
    @AfterMapping // TODO doesn't work
    default void validate(@MappingTarget Validatable object) {
        object.validate();
    }


    @Named("EpochToIso8601")
    default String epochToIso8601(long epoch) {
        return ISODateTimeFormat.dateTime().print(epoch);
    }
}
