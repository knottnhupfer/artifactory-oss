package org.artifactory.metadata.service.provider;

import org.artifactory.fs.FileInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.md.Properties;
import org.artifactory.storage.db.event.service.metadata.model.MutableMetadataEntityBOM;

/**
 * @author Uriah Levy
 */
@FunctionalInterface
interface MetadataResolver {

    void apply(MutableMetadataEntityBOM metadataBom, Properties properties, FileInfo fileInfo, StatsInfo stats);
}
