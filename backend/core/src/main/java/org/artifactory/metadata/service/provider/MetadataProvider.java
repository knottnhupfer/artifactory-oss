package org.artifactory.metadata.service.provider;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.md.Properties;
import org.artifactory.storage.db.event.service.metadata.model.MutableMetadataEntityBOM;

import javax.annotation.Nullable;

import java.util.Set;

/**
 * @author Uriah Levy
 */
interface MetadataProvider {

    void supplement(MutableMetadataEntityBOM metadataBOM, FileInfo fileInfo, Properties properties,
            @Nullable StatsInfo stats);

    Set<String> getLeadFileExtensions();

    RepoType getRepoType();
}
