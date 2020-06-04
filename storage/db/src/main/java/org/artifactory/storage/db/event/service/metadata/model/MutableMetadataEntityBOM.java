package org.artifactory.storage.db.event.service.metadata.model;

import lombok.Data;
import org.jfrog.metadata.client.model.MetadataVersionRepo;

import java.util.*;

/**
 * @author Uriah Levy
 * Raw metadata BOM. A mappable intermidiary to the various {@link org.jfrog.metadata.client.model.MetadataEntity}'s.
 */
@Data
public class MutableMetadataEntityBOM {
    private String pkgid;
    private String name;
    private String repoKey;
    private String repoType;
    private long created;
    private long modified;
    private String packageType;
    private String description;
    private String websiteUrl;
    private String issuesUrl;
    private List<String> licenses = new ArrayList<>();
    private long downloadCount;
    private Map<String, Set<String>> userProperties = new HashMap<>();
    private Map<String, String> qualifiers = new HashMap<>();
    private Set<String> tags = new HashSet<>();

    // Version metadata
    private String version;
    private List<MetadataVersionRepo> repos;

    // Artifact metadata
    List<MutableArtifactMetadata> artifactMetadata;
}
