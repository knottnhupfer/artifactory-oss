package org.artifactory.rest.resource.build;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tamir Hadad
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildsDeletionModel {
    private String buildName;
    private List<String> buildNumbers;
    private boolean deleteArtifacts = false;
    private boolean deleteAll = false;
}