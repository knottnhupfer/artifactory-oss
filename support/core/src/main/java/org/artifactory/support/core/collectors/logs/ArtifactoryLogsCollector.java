package org.artifactory.support.core.collectors.logs;

import org.artifactory.common.ArtifactoryHome;
import org.jfrog.support.common.core.collectors.LogsCollector;

import java.io.File;

/**
 * @author Tamir Hadad
 */
public class ArtifactoryLogsCollector extends LogsCollector {

    private ArtifactoryHome home;

    //TODO [by tamir]: pass log dir
    public ArtifactoryLogsCollector(ArtifactoryHome home) {
        super();
        this.home = home;
        this.setFilesMatcher(new ArtifactorylogsMatcher());
    }

    @Override
    protected File getLogsDirectory() {
        return home.getLogDir();
    }
}
