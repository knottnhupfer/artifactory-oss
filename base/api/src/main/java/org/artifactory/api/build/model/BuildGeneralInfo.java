package org.artifactory.api.build.model;

import lombok.Builder;
import lombok.Data;

/**
 * Transition Model for the UI since getting A build object requires full-read permission while getting the info
 * depicted here only requires basic-read.
 *
 * @author Dan Feldman
 */
@Builder
@Data
public class BuildGeneralInfo {

    private String buildName;
    private String buildNumber;
    private String ciUrl;
    private String releaseStatus;
    private String agent;
    private String buildAgent;
    private String lastBuildTime;
    private String duration;
    private String principal;
    private String artifactoryPrincipal;
    private String url;
    private Long time;
    private String buildStat;
    private Boolean userCanDistribute;
    private Boolean isBuildFullView;
    private Boolean canManage;
    private Boolean canDelete;
}
