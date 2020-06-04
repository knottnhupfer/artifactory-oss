package org.artifactory.build;

import org.jfrog.build.api.Build;

/**
 * {@see DetailedBuildRunImpl} does not (and should not) expose its {@link Build} outside of its package
 * {@see DetailedBuildRunImpl} cannot be moved elsewhere since plugins import it
 * Therefore I present you with:
 *
 *  @author Dan Feldman
 */
public class DetailedBuildRunAdapter {

    private final Build build;

    public DetailedBuildRunAdapter(DetailedBuildRun detailedBuildRun) {
        this.build = ((DetailedBuildRunImpl) detailedBuildRun).build;
    }

    public Build getBuild() {
        return build;
    }
}
