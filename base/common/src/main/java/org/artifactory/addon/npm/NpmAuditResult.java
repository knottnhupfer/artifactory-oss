package org.artifactory.addon.npm;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NpmAuditResult {
    int status;
    boolean successful;
    String error;
    String report;
}
