package org.artifactory.security.access;

import org.artifactory.common.ConstantValues;
import org.jfrog.security.joinkey.JoinKeyBootstrapper;

import java.io.File;

/**
 * @author Nadav Yogev
 */
public class ArtifactoryJoinKeyBootstrapper extends JoinKeyBootstrapper {

    private File joinKeyFile;

    ArtifactoryJoinKeyBootstrapper(File joinKeyFile) {
        this.joinKeyFile = joinKeyFile;
    }

    @Override
    protected File getJoinKeyFile() {
        return joinKeyFile;
    }

    @Override
    protected long getWaitForKeyTimeoutValue() {
        return ConstantValues.joinKeyWaitingTimeout.getLong();
    }
}
