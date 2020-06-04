package org.artifactory.api.rest.distribution.bundle.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.bundle.BundleTransactionStatus;

/**
 * @author Eli Skoran 07/05/2020
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
public class CloseTransactionStatusResponse {
    private BundleTransactionStatus status;
    private Integer artifactsCompleted;
    private Integer totalArtifacts;
}
