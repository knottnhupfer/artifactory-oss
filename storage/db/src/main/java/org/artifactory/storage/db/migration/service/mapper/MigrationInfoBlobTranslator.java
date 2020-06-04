package org.artifactory.storage.db.migration.service.mapper;

import org.artifactory.storage.db.migration.model.MigrationInfoBlob;
import org.jfrog.common.JsonUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * @author Uriah Levy
 */
@Mapper(componentModel = "spring")
@Named("MigrationInfoBlobTranslator")
class MigrationInfoBlobTranslator {

    @Named("InfoBlobModelToByteArray")
    byte[] infoBlobModelToByteArray(MigrationInfoBlob blob) {
        if (blob != null) {
            return JsonUtils.getInstance().valueToByteArray(blob);
        }
        return new byte[0];
    }
}
