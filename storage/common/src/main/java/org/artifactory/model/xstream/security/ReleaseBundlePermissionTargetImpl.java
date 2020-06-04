package org.artifactory.model.xstream.security;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.artifactory.security.MutableReleaseBundlePermissionTarget;
import org.artifactory.security.ReleaseBundlePermissionTarget;

import java.util.ArrayList;

import static java.util.Collections.emptyList;

/**
 * @author Inbar Tal
 */
@XStreamAlias("target")
public class ReleaseBundlePermissionTargetImpl extends RepoPermissionTargetImpl implements
        MutableReleaseBundlePermissionTarget {

    public ReleaseBundlePermissionTargetImpl() {
        this("");
    }

    public ReleaseBundlePermissionTargetImpl(String name) {
        super(name, emptyList(), emptyList());
    }

    public ReleaseBundlePermissionTargetImpl(ReleaseBundlePermissionTarget copy) {
        super(copy.getName(),
                new ArrayList<>(copy.getRepoKeys()),
                new ArrayList<>(copy.getIncludes()),
                new ArrayList<>(copy.getExcludes())
        );
    }
}
