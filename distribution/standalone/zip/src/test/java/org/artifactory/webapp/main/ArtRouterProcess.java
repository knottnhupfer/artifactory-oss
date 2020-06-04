package org.artifactory.webapp.main;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.SystemUtils;
import org.artifactory.webapp.WebappUtils;
import org.jfrog.common.platform.test.helpers.RouterProcess;

/**
 * @author Tamir Hadad
 */
public class ArtRouterProcess extends RouterProcess {

    public ArtRouterProcess(RouterProcessConfig config) {
        super(config);
    }

    @Override
    protected String getJoinKey() {
        return AccessProcess.TEST_JFROG_JOIN_KEY;
    }

    @Override
    protected String getExecutablePath() {
        return Lists.newArrayList(WebappUtils.getRouterExecutable(SystemUtils.IS_OS_MAC).getAbsolutePath()).get(0);
    }
}
